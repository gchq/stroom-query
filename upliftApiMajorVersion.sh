#!/bin/bash

PACKAGE_PREFIX_QUERY_API="stroom.query.api"
PACKAGE_PREFIX_DATASOURCE_API="stroom.datasource.api"
SRC_DIR_API="./stroom-query-api/src"
SRC_DIR_COMMON="./stroom-query-common/src"

if [ "$#" -ne 2 ]; then
    echo "ERROR Invalid arguments" >&2
    echo "Usage: $0 currentMajorVersion newMajorVersion" >&2
    echo "E.g: $0 v1 v2" >&2
    exit 1
fi

oldMajorVersion=$1
newMajorVersion=$2

echo "Uplifting major version from $oldMajorVersion to $newMajorVersion"

echo ""
echo "Renaming package directories"
for versionedDir in `find ${SRC_DIR_API} -type d -name "*${oldMajorVersion}"`; do 
    newDir=`echo "${versionedDir}" | sed "s/${oldMajorVersion}$/${newMajorVersion}/"`
    echo "renaming directory ${versionedDir} to ${newDir}"; 
    git mv "${versionedDir}" "${newDir}"
done

echo ""
echo "Re-versioning ${PACKAGE_PREFIX_QUERY_API} package references in 'stroom-query-api' code"
echo "About to change `find ${SRC_DIR_API} -type f -name "*.java" | xargs grep "${PACKAGE_PREFIX_QUERY_API}.${oldMajorVersion}" | wc -l` lines"
find ${SRC_DIR_API} -type f -name "*.java" | xargs sed -i "s/${PACKAGE_PREFIX_QUERY_API}\.${oldMajorVersion}/${PACKAGE_PREFIX_QUERY_API}.${newMajorVersion}/g"

echo ""
echo "Re-versioning ${PACKAGE_PREFIX_QUERY_API} package references in 'stroom-query-common' code"
echo "About to change `find ${SRC_DIR_COMMON} -type f -name "*.java" | xargs grep "${PACKAGE_PREFIX_QUERY_API}.${oldMajorVersion}" | wc -l` lines"
find ${SRC_DIR_COMMON} -type f -name "*.java" | xargs sed -i "s/${PACKAGE_PREFIX_QUERY_API}\.${oldMajorVersion}/${PACKAGE_PREFIX_QUERY_API}.${newMajorVersion}/g"

echo ""
echo "Re-versioning ${PACKAGE_PREFIX_DATASOURCE_API} package references in 'stroom-query-api' code"
echo "About to change `find ${SRC_DIR_API} -type f -name "*.java" | xargs grep "${PACKAGE_PREFIX_DATASOURCE_API}.${oldMajorVersion}" | wc -l` lines"
find ${SRC_DIR_API} -type f -name "*.java" | xargs sed -i "s/${PACKAGE_PREFIX_DATASOURCE_API}\.${oldMajorVersion}/${PACKAGE_PREFIX_DATASOURCE_API}.${newMajorVersion}/g"

echo ""
echo "Re-versioning ${PACKAGE_PREFIX_DATASOURCE_API} package references in 'stroom-query-common' code"
echo "About to change `find ${SRC_DIR_COMMON} -type f -name "*.java" | xargs grep "${PACKAGE_PREFIX_DATASOURCE_API}.${oldMajorVersion}" | wc -l` lines"
find ${SRC_DIR_COMMON} -type f -name "*.java" | xargs sed -i "s/${PACKAGE_PREFIX_DATASOURCE_API}\.${oldMajorVersion}/${PACKAGE_PREFIX_DATASOURCE_API}.${newMajorVersion}/g"


echo ""
echo "Completed, ensure you check all changes made before committing"
exit 0
