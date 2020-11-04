package demo.es.restclient.dao;

import demo.es.restclient.entity.Foo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FooDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void insert(Foo foo) {
        jdbcTemplate.update("INSERT INTO FOO (ID, BAR) VALUES (?,?)", foo.getId(),foo.getBar());
    }

    public List query() {
        return jdbcTemplate.queryForList("select * from foo");
    }


}
