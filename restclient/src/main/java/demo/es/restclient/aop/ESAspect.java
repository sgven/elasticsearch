package demo.es.restclient.aop;

import demo.es.restclient.annotation.ESDocument;
import demo.es.restclient.annotation.SyncESData;
import demo.es.restclient.util.BeanUtils;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ESAspect {

    /**
     * annotationAspect 注解方式
     */
    @Pointcut("@annotation(demo.es.restclient.annotation.SyncESData)")
    public void annotationAspect() {

    }

    /**
     * classAspect 类方式
     */
    // @Pointcut("execution(* com.epichust.service.interfaces..*(..))")
    // public void classAspect() {
    //
    // }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Around("annotationAspect() && @annotation(anno)")
    public Object around(ProceedingJoinPoint joinPoint, SyncESData anno) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();
        String dataSource = anno.dataSource();
        Object retVal = null;
        Object data = null;
        Class entityClass = null;
        try {
            retVal = joinPoint.proceed();
            if (SyncESData.REQ_PARAMS.equalsIgnoreCase(dataSource)) {
                if (args !=null && args.length > 0) {
                    data = args[0];
                }
            } else {
                Assert.isTrue(retVal != null, "The retVal must not be null");
                data = retVal;
            }
            if (data.getClass().isAssignableFrom(ArrayList.class)) {//批量保存
                entityClass = ((List) data).get(0).getClass();
            } else {
                entityClass = data.getClass();
            }
            ESDocument esDocument = (ESDocument) entityClass.getAnnotation(ESDocument.class);
            String indexName = entityClass.getSimpleName().toLowerCase();
            String typeName = entityClass.getSimpleName();
            if (esDocument != null) {
                if (StringUtils.isNotBlank(esDocument.indexName())) {
                    indexName = esDocument.indexName();
                }
                if (StringUtils.isNotBlank(esDocument.typeName())) {
                    typeName = esDocument.typeName();
                }
            }
            initIndex(indexName);
            if (data.getClass().isAssignableFrom(ArrayList.class)) {//批量保存
                List list = (List) data;
                BulkRequest bulkRequest = new BulkRequest();
                for (Object obj : list) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    BeanUtils.transformBeanToEsMap(obj, jsonMap);
                    bulkRequest.add(new UpdateRequest().index(indexName).type(typeName)
                            .id((String) jsonMap.get("id")).doc(jsonMap)
                            .upsert(jsonMap));
                }
                restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new EsAsyncListener());
            } else {
                Map<String, Object> jsonMap = new HashMap<>();
                BeanUtils.transformBeanToEsMap(data, jsonMap);
                UpdateRequest updateRequest = new UpdateRequest().index(indexName).type(typeName)
                        .id((String) jsonMap.get("id")).doc(jsonMap)
                        .upsert(jsonMap);
                restHighLevelClient.updateAsync(updateRequest, RequestOptions.DEFAULT, new EsAsyncListener());
            }
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            /**
             注意：
             事务没生效时，这里没有对异常做处理，异常被吞掉了；
             想要用切面注解和@Transactional注解对方法同时增强，想要事务生效，
             这里记得一定要将异常抛出来
             */
            throw throwable;
        } finally {

        }

        return retVal;
    }

    public class EsAsyncListener implements ActionListener {

        @Override
        public void onResponse(Object o) {
//            log.info(o.toString());
        }

        @Override
        public void onFailure(Exception e) {
            log.error(e.getMessage(), e);
        }
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
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) throws NoSuchMethodException {
        /* 拦截的实体类，就是当前正在执行的controller */
        Object target = proceedingJoinPoint.getTarget();
        /* 拦截的方法名称。当前正在执行的方法 */
        String methodName = proceedingJoinPoint.getSignature().getName();
        /* 拦截的参数类型 */
        Signature signature = proceedingJoinPoint.getSignature();

        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        Class[] parameterTypes = ((MethodSignature) signature).getMethod().getParameterTypes();
        Method method = target.getClass().getMethod(methodName, parameterTypes);

        return method;
    }
}
