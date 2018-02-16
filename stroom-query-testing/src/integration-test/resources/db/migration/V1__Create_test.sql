-- Hibernate
CREATE TABLE test_hibernate_entity (
    dataSourceUuid  VARCHAR(255) NOT NULL,
    id 				VARCHAR(255) NOT NULL,
    flavour         VARCHAR(255) NOT NULL,
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (dataSourceUuid, id)
) ENGINE=InnoDB DEFAULT CHARSET latin1;

CREATE TABLE test_hibernate_doc_ref (
    uuid            VARCHAR(255) NOT NULL,
    name            VARCHAR(127) NOT NULL,
    clanName        VARCHAR(255),
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (uuid)
) ENGINE=InnoDB DEFAULT CHARSET latin1;

-- JOOQ
CREATE TABLE test_jooq_entity (
    dataSourceUuid  VARCHAR(255) NOT NULL,
    id 				VARCHAR(255) NOT NULL,
    colour          VARCHAR(255) NOT NULL,
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (dataSourceUuid, id)
) ENGINE=InnoDB DEFAULT CHARSET latin1;

CREATE TABLE test_jooq_doc_ref (
    uuid            VARCHAR(255) NOT NULL,
    name            VARCHAR(127) NOT NULL,
    planetNam       VARCHAR(255),
    updateUser      VARCHAR(255) NOT NULL,
    updateTime      BIGINT UNSIGNED NOT NULL,
    createUser      VARCHAR(255) NOT NULL,
    createTime      BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (uuid)
) ENGINE=InnoDB DEFAULT CHARSET latin1;