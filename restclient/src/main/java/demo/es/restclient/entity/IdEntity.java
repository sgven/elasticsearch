package demo.es.restclient.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
public abstract class IdEntity implements Serializable {

    //1.使用orm框架进行保存时，entity需要指定ID和id生成策略
    //2.主键生成策略，需要和ID字段类型相匹配
    //  sequence <==> Long，如果不指定策略，默认就是sequence，由hibernate自动生成序列
    //  IDENTITY <==> hibernate不生成，使用数据库IDENTITY自增主键的方式生成主键
    //3.默认策略不满足，可以指定id生成器
    @Id
    @GeneratedValue(generator = "uuid-generator")
    @GenericGenerator(name = "uuid-generator", strategy = "uuid")
    private String id;

    @Column(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
