package demo.es.restclient.aop;

import demo.es.restclient.annotation.ESDocument;
import demo.es.restclient.annotation.SyncESData;
import demo.es.restclient.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.Table;
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
                log.info("批量保存");
                List list = (List) data;
                String typeName = getTypeName(list.get(0));
                BulkRequest bulkRequest = new BulkRequest();
                for (Object obj : list) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    BeanUtils.transformBeanToEsMap(obj, jsonMap);
                    bulkRequest.add(new IndexRequest("myindex").type(typeName)
                            .id((String) jsonMap.get("id")).source(jsonMap));
                }
                BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info(bulkResponse.toString());
            } else {//
                String typeName = getTypeName(data);
                Map<String, Object> jsonMap = new HashMap<>();
                BeanUtils.transformBeanToEsMap(data, jsonMap);
                IndexRequest indexRequest = new IndexRequest("myindex").type(typeName)
                        .id((String) jsonMap.get("id")).source(jsonMap);
                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                log.info(indexResponse.toString());
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
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

    private String getTypeName(Object data) {
        ESDocument esDocument = data.getClass().getAnnotation(ESDocument.class);
        String typeName = esDocument.typeName();
        if (StringUtils.isEmpty(typeName)) {
            Table table = data.getClass().getAnnotation(Table.class);
            typeName = table.name();
        }
        return typeName;
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
