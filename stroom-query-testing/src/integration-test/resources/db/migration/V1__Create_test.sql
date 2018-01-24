-- Following Simon Holywell's style guide: http://www.sqlstyle.guide/
CREATE TABLE test_entity (
    dataSourceUuid  VARCHAR(255) NOT NULL,
    id 				VARCHAR(255) NOT NULL,
    flavour         VARCHAR(255) NOT NULL,
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (dataSourceUuid, id)
);

CREATE TABLE test_doc_ref (
    uuid            VARCHAR(255) NOT NULL,
    name            VARCHAR(127) NOT NULL,
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (uuid)
);