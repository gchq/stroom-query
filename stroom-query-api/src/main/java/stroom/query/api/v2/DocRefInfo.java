package stroom.query.api.v2;

import stroom.docref.DocRef;

import java.util.Objects;

public class DocRefInfo {
    private DocRef docRef;
    private Long createTime;
    private Long updateTime;
    private String createUser;
    private String updateUser;
    private String otherInfo;

    public DocRefInfo() {

    }

    public DocRef getDocRef() {
        return docRef;
    }

    public void setDocRef(DocRef docRef) {
        this.docRef = docRef;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocRefInfo that = (DocRefInfo) o;
        return Objects.equals(docRef, that.docRef) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(updateUser, that.updateUser) &&
                Objects.equals(otherInfo, that.otherInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docRef, createTime, updateTime, createUser, updateUser, otherInfo);
    }

    public static class Builder {
        private final DocRefInfo instance;

        public Builder() {
            this.instance = new DocRefInfo();
        }

        public Builder docRef(final DocRef value) {
            this.instance.docRef = value;
            return this;
        }

        public Builder createTime(final Long value) {
            this.instance.setCreateTime(value);
            return this;
        }

        public Builder createUser(final String value) {
            this.instance.setCreateUser(value);
            return this;
        }

        public Builder updateTime(final Long value) {
            this.instance.setUpdateTime(value);
            return this;
        }

        public Builder updateUser(final String value) {
            this.instance.setUpdateUser(value);
            return this;
        }

        public Builder otherInfo(final String value) {
            this.instance.setOtherInfo(value);
            return this;
        }

        public DocRefInfo build() {
            return instance;
        }
    }
}
