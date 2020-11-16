package demo.es.restclient.service;

import demo.es.restclient.params.EsCondition;
import demo.es.restclient.params.Page;
import io.micrometer.core.instrument.util.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 支持单个文档全部走es，
 * 如果要多表join，建议可以在es中查询主表ids，再去关系数据库join
 */
@Service
public class EsQueryService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public List<Map<String,Object>> queryList(EsCondition condition) throws IOException {
        SearchHits hits = getSearchHits(condition);
        if (hits != null && hits.getTotalHits() > 0) {
            return Arrays.stream(hits.getHits()).map(e -> {
                return e.getSourceAsMap();
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Page queryPage(EsCondition condition) throws IOException {
        SearchHits hits = getSearchHits(condition);
        Page page = new Page();
        if (hits != null && hits.getTotalHits() > 0) {
            page.setTotal(hits.getTotalHits());
            page.setList(Arrays.stream(hits.getHits()).map(e -> {
                return e.getSourceAsMap();
            }).collect(Collectors.toList()));
        }
        return page;
    }

    private SearchHits getSearchHits(EsCondition condition) throws IOException {
        // params
        String index = condition.getIndex();
        String type = condition.getType();
        String searchText = condition.getSearchText();
        int pageIndex = condition.getPageIndex();
        int pageSize = condition.getPageSize();
        pageIndex = pageIndex <= -1 ? 0 : pageIndex;
        pageSize = pageSize >= 1000 ? 1000 : pageSize;
        pageSize = pageSize <= 0 ? 10 : pageSize;

        // buildRequest
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(pageIndex);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // fields
        String[] includeFields = condition.getIncludeFields() == null ? new String[]{} : condition.getIncludeFields();
        String[] excludeFields = condition.getExcludeFields() == null ? new String[]{} : condition.getExcludeFields();
        if (!CollectionUtils.isEmpty(Arrays.asList(includeFields)) || !CollectionUtils.isEmpty(Arrays.asList(excludeFields))) {
            searchSourceBuilder.fetchSource(includeFields, excludeFields);
        }

        // query条件
        QueryBuilder queryBuilder = condition.getQueryBuilder();
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        if (StringUtils.isNotBlank(searchText)) {
            queryBuilder = searchSourceBuilder.query();
            if (queryBuilder != null) {
                if (queryBuilder.getClass().isAssignableFrom(BoolQueryBuilder.class)) {
                    ((BoolQueryBuilder) queryBuilder).must(QueryBuilders.queryStringQuery(searchText));
                } else {
                    BoolQueryBuilder bool = QueryBuilders.boolQuery();
                    bool.must(queryBuilder);
                    bool.must(QueryBuilders.queryStringQuery(searchText));
                    searchSourceBuilder.query(bool);
                }
            } else {
                searchSourceBuilder.query(QueryBuilders.queryStringQuery(searchText));
            }
        }

        // sorts
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        List<SortBuilder<?>> sorts = condition.getSorts();
        if (sorts != null && sorts.size() > 0) {
            sorts.stream().forEach(sort -> {
                searchSourceBuilder.sort(sort);
            });
        }

        // do search
        searchRequest.indices(index);
        searchRequest.types(type);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits();
    }

    /**
     * 查询主键
     * @param condition
     * @return
     */
    public List<String> queryIds(EsCondition condition) throws IOException {
        // params
        String index = condition.getIndex();
        String type = condition.getType();
        String searchText = condition.getSearchText();
        int pageIndex = condition.getPageIndex();
        int pageSize = condition.getPageSize();
        pageIndex = pageIndex <= -1 ? 0 : pageIndex;
        pageSize = pageSize >= 1000 ? 1000 : pageSize;
        pageSize = pageSize <= 0 ? 10 : pageSize;

        // buildRequest
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(pageIndex);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // fields
        String[] includeFields = new String[]{"id"};

        // query条件
        QueryBuilder queryBuilder = condition.getQueryBuilder();
        if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }
        if (StringUtils.isNotBlank(searchText)) {
            queryBuilder = searchSourceBuilder.query();
            if (queryBuilder != null) {
                if (queryBuilder.getClass().isAssignableFrom(BoolQueryBuilder.class)) {
                    ((BoolQueryBuilder) queryBuilder).must(QueryBuilders.queryStringQuery(searchText));
                } else {
                    BoolQueryBuilder bool = QueryBuilders.boolQuery();
                    bool.must(queryBuilder);
                    bool.must(QueryBuilders.queryStringQuery(searchText));
                    searchSourceBuilder.query(bool);
                }
            } else {
                searchSourceBuilder.query(QueryBuilders.queryStringQuery(searchText));
            }
        }

        // do search
        searchRequest.indices(index);
        searchRequest.types(type);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        if (hits != null && hits.getTotalHits() > 0) {
            return Arrays.stream(hits.getHits()).map(e -> {
                return e.getSourceAsMap().get("id").toString();
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
