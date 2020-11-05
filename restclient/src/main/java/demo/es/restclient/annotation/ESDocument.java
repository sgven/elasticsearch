package demo.es.restclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * es文档注解
 * es6以后，强制一个index下只能有一个type
 * es7以后，将完全移除type
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface ESDocument {

    String indexName() default "";//索引名称，必须小写

    String typeName() default "";//数据类型

}
