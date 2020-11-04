package demo.es.restclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableTransactionManagement
//@EnableAspectJAutoProxy(exposeProxy = true)
@Slf4j
@SpringBootApplication
public class PureEsApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(PureEsApplication.class, args);
    }

    @Autowired
    private DataSourceDemo dataSourceDemo;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dataSourceDemo.showConnection();
        dataSourceDemo.showData();
    }

}