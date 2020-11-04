package demo.es.restclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
public class DataSourceDemo {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Log4j | @Slf4j：注解在类上；为类提供一个 属性名为log 的 log4j 日志对像
     * log识别不出时，IDEA需要安装Lombok插件，并重启IDEA
     * 此时log能识别出来了，但启动时编译还是报错，需要修改配置Settings：
     * 1.Settings ->  Build, Execution, Deployment-> Compiler->Annotation Processors，勾选Enable  Annotation Processors
     * 2.Settings ->  Build, Execution, Deployment-> Compiler->Java Compiler改为默认的Javac（估计可能是Eclipse没有安装Lombok插件）
     */
    public void showConnection() throws SQLException {
        log.info(dataSource.toString());
        Connection conn = dataSource.getConnection();
        log.info(conn.toString());
        conn.close();
    }

    public void showData() {
        jdbcTemplate.queryForList("select * from FOO")
                .forEach(row -> log.info(row.toString()));
    }
}
