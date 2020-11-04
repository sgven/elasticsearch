package demo.es.restclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface SyncESData {

    static final String REQ_PARAMS = "params";//请求参数
    static final String REQ_BACK = "back";//返回值

    String dataSource() default REQ_PARAMS;//数据来源，请求参数 或 返回值，默认取请求参数
}
