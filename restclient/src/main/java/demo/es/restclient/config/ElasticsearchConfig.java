package demo.es.restclient.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class ElasticsearchConfig {

    @Value("${es.init}")
    private String init;

    @Value("${es.host}")
    private String host;

    public String getInit() {
        return init;
    }


    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http"),
//                        new HttpHost("localhost", 9201, "http")));
                        new HttpHost(host, 9200, "http"),
                        new HttpHost(host, 9201, "http")));
        return client;
    }

}
