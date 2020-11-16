package demo.es.restclient.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page {

    private long total = 0l;
    private List<Map<String, Object>> list = new ArrayList<>();

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }
}
