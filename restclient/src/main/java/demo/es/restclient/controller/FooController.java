package demo.es.restclient.controller;

import demo.es.restclient.entity.Foo;
import demo.es.restclient.service.FooService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("foo")
public class FooController {

    @Autowired
    private FooService fooService;

    @RequestMapping(value = "insert", method = RequestMethod.POST)
    public void insertFoo(@RequestBody Foo foo) {
        fooService.insertFoo(foo);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public void updateFoo(@RequestBody Foo foo) {
        fooService.updateFoo(foo);
    }

    @RequestMapping(value = "insertBatch", method = RequestMethod.POST)
    public void insertBatch(@RequestBody List<Foo> foos) {
        fooService.insertBatch(foos);
    }

    @RequestMapping(value = "updateBatch", method = RequestMethod.POST)
    public void updateBatch(@RequestBody List<Foo> foos) {
        fooService.updateBatch(foos);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void delete(@RequestParam String id) {
        fooService.deleteFoo(id);
    }

    @RequestMapping(value = "deleteBatch", method = RequestMethod.POST)
    public void deleteBatch(@RequestBody List<String> ids) {
        fooService.deleteByIds(ids);
    }

    @RequestMapping(value = "queryFoo", method = RequestMethod.POST)
    public List queryFoo() {
        List back = null;
        try {
            back = fooService.queryFoo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return back;
    }



}
