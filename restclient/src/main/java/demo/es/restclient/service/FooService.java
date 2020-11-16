package demo.es.restclient.service;

import demo.es.restclient.annotation.SyncESData;
import demo.es.restclient.dao.FooRepo;
import demo.es.restclient.dao.FooDao;
import demo.es.restclient.entity.Foo;
import demo.es.restclient.params.EsCondition;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FooService {

    @Autowired
    private FooDao fooDao;
    @Autowired
    private FooRepo fooRepo;
    @Autowired
    private EsQueryService esQueryService;

    @SyncESData
    @Transactional
    public void insertFoo(Foo foo) {
        foo.setCreateDate(new Date());
        Foo save = fooRepo.save(foo);
        BeanUtils.copyProperties(save,foo);
    }

    @SyncESData
    @Transactional
    public void updateFoo(Foo foo) {
        fooRepo.save(foo);
    }

    @SyncESData(dataSource = SyncESData.REQ_BACK)
    @Transactional
    public List insertBatch(List<Foo> foos) {
        foos.stream().forEach(e -> {e.setCreateDate(new Date());});
        List back = (List) fooRepo.saveAll(foos);
        return back;
    }

    @SyncESData
    @Transactional
    public void updateBatch(List<Foo> foos) {
        fooRepo.saveAll(foos);
    }

    @SyncESData(dataSource = SyncESData.REQ_BACK)
    @Transactional
    public Foo deleteFoo(String id) {
        Optional<Foo> opt = fooRepo.findById(id);
        Foo foo = opt.get();
        foo.setIsDelete(1);
        return foo;
    }

    @SyncESData(dataSource = SyncESData.REQ_BACK)
    @Transactional
    public List<Foo> deleteByIds(List<String> ids) {
        List<Foo> foos = (List<Foo>) fooRepo.findAllById(ids);
        foos.stream().forEach(e -> {e.setIsDelete(1);});
        fooRepo.saveAll(foos);
        return foos;
    }

    public List queryFoo() throws Exception{
        //模板方法，先走es查询，如果为空，再走数据库查询
        EsCondition esCondition = new EsCondition();
        esCondition.setIndex("foo");
        esCondition.setType("Foo");
        esCondition.setQueryBuilder(QueryBuilders.matchAllQuery());
        List data = esQueryService.queryList(esCondition);
        if (data == null || data.size() ==0) {//数据库查询
            data = fooDao.query();
        }
        return data;
    }

}
