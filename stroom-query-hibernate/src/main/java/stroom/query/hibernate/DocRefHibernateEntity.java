package stroom.query.hibernate;

import stroom.query.audit.service.DocRefEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class DocRefHibernateEntity extends DocRefEntity {

    @Id
    @Column(name=UUID)
    public String getUuid() {
        return super.getUuid();
    }

    @Column(name=NAME)
    public String getName() {
        return super.getName();
    }

    @Column(name=CREATE_TIME)
    public Long getCreateTime() {
        return super.getCreateTime();
    }

    @Column(name=UPDATE_TIME)
    public Long getUpdateTime() {
        return super.getUpdateTime();
    }

    @Column(name=CREATE_USER)
    public String getCreateUser() {
        return super.getCreateUser();
    }

    @Column(name=UPDATE_USER)
    public String getUpdateUser() {
        return super.getUpdateUser();
    }
}
