package demo.es.restclient.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 模板方法，搜索es
 */
@Service
public class EsQueryService extends AbstractEsQuery {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public List queryList() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("bar", "aa")
//                .fuzziness(Fuzziness.AUTO)
//                .prefixLength(3)
//                .maxExpansions(10);
//        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("createDate").order(SortOrder.DESC));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        if (hits != null && hits.getTotalHits() > 0) {
            return Arrays.stream(hits.getHits()).map(e -> {
                return e.getSourceAsMap();
            }).collect(Collectors.toList());
        }
        return null;
    }
}
