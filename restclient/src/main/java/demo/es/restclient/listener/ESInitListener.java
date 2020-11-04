package demo.es.restclient.listener;

import demo.es.restclient.annotation.ESDocument;
import demo.es.restclient.dao.FooRepo;
import demo.es.restclient.config.ElasticsearchConfig;
import demo.es.restclient.util.BeanUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
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

import javax.persistence.Table;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
//public class ESInitListener implements ApplicationContextInitializer {//容器上下文的初始化类  context.initializer.classes
//    @Override
//    public void initialize(ConfigurableApplicationContext applicationContext) {
//        System.out.println("-----MyApplicationContextInitializer initialize-----");
//        this.restHighLevelClient = applicationContext.getBean(RestHighLevelClient.class);
//        System.out.println("---------------"+this.restHighLevelClient);
//    }
//}
public class ESInitListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ElasticsearchConfig elasticsearchConfig;
    @Autowired
    private FooRepo fooRepo;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().containsBean("restHighLevelClient")) {
            try {
                initES();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initES() throws IOException, IllegalAccessException {
        initIndex();
        String init = elasticsearchConfig.getInit();
        log.info("-----------------------isInitES:" + init);
        if ("yes".equalsIgnoreCase(init)) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("demo.es.restclient.entity"))
                    .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
            );
            Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(ESDocument.class);
            List<String> tableList = classSet.stream().filter(e -> {
                Table table = e.getAnnotation(Table.class);
                if (StringUtils.isNotBlank(table.name())) {
                    return true;
                }
                return false;
            }).map(e -> {
                Table table = e.getAnnotation(Table.class);
                return table.name();
            }).collect(Collectors.toList());
            initData(tableList);
            System.out.println(classSet.stream().findAny().get().getName());
        }

    }

    private void initIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("myindex");
        boolean exists = this.restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("myindex");
            createIndexRequest.settings(Settings.builder()
                    .put("index.number_of_shards", 5)
                    .put("index.number_of_replicas", 0)
            );
            CreateIndexResponse createIndexResponse = this.restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info(createIndexResponse.toString());
        }
    }

    private void initData(List<String> tableList) throws IOException, IllegalAccessException {
        for (String tableName : tableList) {
            // 索引数据
//            List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from " + tableName);
            List data = (List) fooRepo.findAll();
            if (data != null && data.size() > 0) {
                log.info("批量初始化");
                BulkRequest bulkRequest = new BulkRequest();
                for (Object obj : data) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    BeanUtils.transformBeanToEsMap(obj, jsonMap);
                    bulkRequest.add(new IndexRequest("myindex").type(tableName)
                            .id((String) jsonMap.get("id")).source(jsonMap));
                }
                BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info(bulkResponse.toString());
            }
        }
    }

}

