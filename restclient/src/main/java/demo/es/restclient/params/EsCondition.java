package demo.es.restclient.params;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.ArrayList;
import java.util.List;

public class EsCondition {

    private String index;
    private String type;
    private int pageIndex;
    private int pageSize;
    private String[] includeFields;
    private String[] excludeFields;
    // 全文检索
    private String searchText;
    private QueryBuilder queryBuilder;
    // 排序
    private List<SortBuilder<?>> sorts = new ArrayList<>();

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(String[] includeFields) {
        this.includeFields = includeFields;
    }

    public String[] getExcludeFields() {
        return excludeFields;
    }

    public void setExcludeFields(String[] excludeFields) {
        this.excludeFields = excludeFields;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public List<SortBuilder<?>> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortBuilder<?>> sorts) {
        this.sorts = sorts;
    }

    public List<SortBuilder<?>> sorts() {
        this.sorts = new ArrayList<>();
        return this.sorts;
    }

}
