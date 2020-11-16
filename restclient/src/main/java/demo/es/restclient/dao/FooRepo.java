package demo.es.restclient.dao;

import demo.es.restclient.entity.Foo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

//public interface FooRepo extends CrudRepository<Foo, String> {
public interface FooRepo extends JpaRepository<Foo, String> {
}
