package demo.es.restclient.entity;

import demo.es.restclient.annotation.ESDocument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.BigInteger;

@ESDocument
@Entity
@Table(name = "foo")
public class Foo extends SuperEntity {

    private String bar;

    @Column(name = "BAR")
    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    private BigDecimal money;

    @Column(name = "MONEY")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    private BigInteger age;

    @Column(name = "AGE")
    public BigInteger getAge() {
        return age;
    }

    public void setAge(BigInteger age) {
        this.age = age;
    }
}
