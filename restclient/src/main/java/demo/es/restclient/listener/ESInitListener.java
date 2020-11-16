package demo.es.restclient.listener;

import demo.es.restclient.annotation.ESDocument;
import demo.es.restclient.dao.FooRepo;
import demo.es.restclient.config.ElasticsearchConfig;
import demo.es.restclient.util.BeanUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ESInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ElasticsearchConfig elasticsearchConfig;
    @Autowired
    private FooRepo fooRepo;
    private boolean stopExecute=false;//终止执行
    private final static long MAX_SIZE = 100000000;//10^8≈95M

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Thread initEsData = new Thread("initEsData") {
            @Override
            public void run() {
                long t1=System.currentTimeMillis();
                log.info("初始化ES开始...");
                if (event.getApplicationContext().containsBean("restHighLevelClient")) {
                    try {
                        initES();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log.info("初始化ES完毕!"+(System.currentTimeMillis()-t1)/1000+"S");
            }
        };
        initEsData.start();
    }

    private void initES() throws IOException, IllegalAccessException, NoSuchFieldException {
        String init = elasticsearchConfig.getInit();
        log.info("-----------------------isInitES:" + init);
        if ("yes".equalsIgnoreCase(init)) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("demo.es.restclient.entity"))
                    .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
            );
            Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(ESDocument.class);
            List<Class<?>> entityList = classSet.stream().filter(e -> {
                Entity entity = e.getAnnotation(Entity.class);
                if (entity != null) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            initData(entityList);
        }

    }

    private void initData(List<Class<?>> classes) {
        log.info("ES-data初始化开始!"+System.currentTimeMillis());
        for (Class clazz : classes) {
            if (stopExecute) {//终止执行
                break;
            }
            ESDocument esDocument = (ESDocument) clazz.getAnnotation(ESDocument.class);
            String indexName = clazz.getSimpleName().toLowerCase();
            String typeName = clazz.getSimpleName();
            int pageSize = 1000;
            if (esDocument != null) {
                if (StringUtils.isNotBlank(esDocument.indexName())) {
                    indexName = esDocument.indexName();
                }
                if (StringUtils.isNotBlank(esDocument.typeName())) {
                    typeName = esDocument.typeName();
                }
                pageSize = esDocument.pageSize();
            }
            // 初始化索引
            initIndex(indexName);
            // 索引数据
//            long totalCount = fooRepo.count();
//            long pageCount = totalCount / pageSize + 1;//页数从第一页开始+1
//            pageCount = pageCount + (totalCount % pageSize == 0 ? 0 : 1);//余数不为0，页数+1
//            for (int i = 1; i < pageCount; i++) {
//                if (stopExecute) {
//                    break;
//                }
//                System.out.println("执行次数:" + i);
//                process(clazz, indexName, typeName, i, pageSize);
//            }
            process(clazz, indexName, typeName, 0, pageSize);
        }

        System.out.println("es初始化完成");
        log.info("ES-data初始化结束!"+System.currentTimeMillis());
    }

    private void initIndex(String indexName) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            boolean exists = this.restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (!exists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                //单机服务，副本分片设置为0
                createIndexRequest.settings(Settings.builder()
                        .put("index.number_of_shards", 5)
                        .put("index.number_of_replicas", 0)
                );
                CreateIndexResponse createIndexResponse = this.restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                log.info(createIndexResponse.toString());
            }
        } catch (ConnectException connectException) {
            stopExecute = true;
            log.error(connectException.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void process(Class clazz,String indexName, String typeName, int pageIndex, int pageSize) {
        try {
//            StringBuffer hql = new StringBuffer();
//            hql.append("select t from " + clazz.getSimpleName() + " t  where  1=1  ");
//            if ("MbbInterfaceDetail".equalsIgnoreCase(typeName)) {//接口日志特殊处理
//                //对超过10M的在数据库手动标记为删除，并且不同步到es中
//                //select gid,DBMS_LOB.GETLENGTH(t.TO_DATA_SERIA) from MBB_INTERFACE_DETAIL t where DBMS_LOB.GETLENGTH(t.TO_DATA_SERIA) > 10485760;
//                hql.append(" and isDelete=0");
//            }
//            hql.append(" order by id");
//            fooRepo.findAll(page);
            List data = (List) fooRepo.findAll();
            if (data != null && data.size() > 0) {
                BulkRequest bulkRequest = new BulkRequest();
                for (Object obj : data) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    BeanUtils.transformBeanToEsMap(obj, jsonMap);
                    bulkRequest.add(new UpdateRequest().index(indexName).type(typeName)
                            .id((String) jsonMap.get("id")).doc(jsonMap)
                            .upsert(jsonMap));//upsert:当文档不存在时，新增(up_insert)
                }
                processBulkRequest(bulkRequest);
            }
        } catch (ConnectException connectException) {
            stopExecute = true;
            log.error(connectException.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void processBulkRequest(BulkRequest bulkRequest) throws Exception {
        System.out.println("estimatedSize:"+bulkRequest.estimatedSizeInBytes());
        System.out.println("request-descrip:"+bulkRequest.getDescription());
        if (bulkRequest.estimatedSizeInBytes() > MAX_SIZE) {//请求内容大小超过10^8kb≈95M，分而治之
            System.out.println("分治bulk请求");
            List<DocWriteRequest<?>> requests = bulkRequest.requests();
            int reqSize = requests.size();
            if (reqSize == 1) {
                System.out.println("文档id: "+requests.get(0).id());
                //单个请求超过阈值，不执行
                if (bulkRequest.estimatedSizeInBytes() > MAX_SIZE) {//95M
                    return;
                }

                callBulkRequest(bulkRequest);
                return;
            }
            int middleIndex = reqSize / 2;
            List<DocWriteRequest<?>> req1 = requests.subList(0, middleIndex);
            List<DocWriteRequest<?>> req2 = requests.subList(middleIndex, reqSize);
            BulkRequest bulk1 = new BulkRequest();
            BulkRequest bulk2 = new BulkRequest();
            for (DocWriteRequest<?> docWriteRequest : req1) {
                bulk1.add(docWriteRequest);
            }
            for (DocWriteRequest<?> docWriteRequest : req2) {
                bulk2.add(docWriteRequest);
            }
            processBulkRequest(bulk1);
            processBulkRequest(bulk2);
        } else {
            callBulkRequest(bulkRequest);
        }
    }

    private void callBulkRequest(BulkRequest bulkRequest) throws IOException {
        if (bulkRequest.numberOfActions() > 0) {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//            MestarLogger.info(bulkResponse.toString());
            //异步方式，数据量大的时候，并发请求容易把es打死
//            restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new EsBulkAsyncListener());
        }
    }

    public class EsBulkAsyncListener implements ActionListener {

        @Override
        public void onResponse(Object o) {
            log.info(o.toString());
        }

        @Override
        public void onFailure(Exception e) {
            if (e.getClass().isAssignableFrom(ConnectException.class)) {
                stopExecute = true;
            }
            log.error(e.getMessage(), e);
        }
    }

}

