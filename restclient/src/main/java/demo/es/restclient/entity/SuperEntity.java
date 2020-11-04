package demo.es.restclient.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public abstract class SuperEntity extends IdEntity {

//    protected String createId;
    protected Date createDate;
//    protected String modifyId;
//    protected Date modifyDate;
//    protected int isActive;
    protected int isDelete;

    @Column(name = "IS_DELETE")
    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    @Column(name = "CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
