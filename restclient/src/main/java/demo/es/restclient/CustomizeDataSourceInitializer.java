package demo.es.restclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * 加入JPA依赖后，spring-boot启动时不会执行data.sql；
 * 需要自定义DataSourceInitializer，指定初始化执行的sql脚本，data.sql
 */
@Configuration
public class CustomizeDataSourceInitializer {

    @Value("classpath:data.sql")
    private Resource functionScript;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(functionScript);
//        populator.setSeparator("$$");//使用存储过程时，需要配置这个分隔符
        return populator;
    }
}
