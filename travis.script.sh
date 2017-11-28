#!/bin/bash

#exit script on any error
set -e

#Shell Colour constants for use in 'echo -e'
#e.g.  echo -e "My message ${GREEN}with just this text in green${NC}"
RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m' # No Colour 

#establish what version of stroom we are building, export it to make it 
#available to other scripts run by travis
if [ -n "$TRAVIS_TAG" ]; then
    #Tagged commit so use that as our stroom version, e.g. v6.0.0
    STROOM_QUERY_VERSION="${TRAVIS_TAG}"

    #upload the maven artefacts to bintray
    EXTRA_BUILD_ARGS="bintrayUpload"
else
    #No tag so use the branch name as the version, e.g. dev-SNAPSHOT
    STROOM_QUERY_VERSION="${TRAVIS_BRANCH}-SNAPSHOT"
    EXTRA_BUILD_ARGS=""
fi

#Exporting variables does not seem to work across scripts so do it via a file
echo "export STROOM_QUERY_VERSION=${STROOM_QUERY_VERSION}" >> env.sh

#Dump all the travis env vars to the console for debugging
echo -e "TRAVIS_BUILD_NUMBER:  [${GREEN}${TRAVIS_BUILD_NUMBER}${NC}]"
echo -e "TRAVIS_COMMIT:        [${GREEN}${TRAVIS_COMMIT}${NC}]"
echo -e "TRAVIS_BRANCH:        [${GREEN}${TRAVIS_BRANCH}${NC}]"
echo -e "TRAVIS_TAG:           [${GREEN}${TRAVIS_TAG}${NC}]"
echo -e "TRAVIS_PULL_REQUEST:  [${GREEN}${TRAVIS_PULL_REQUEST}${NC}]"
echo -e "TRAVIS_EVENT_TYPE:    [${GREEN}${TRAVIS_EVENT_TYPE}${NC}]"
echo -e "STROOM_QUERY_VERSION: [${GREEN}${STROOM_QUERY_VERSION}${NC}]"


#Run the build (including running maven install task to generate poms
./gradlew -Pversion=$STROOM_QUERY_VERSION clean build install ${EXTRA_BUILD_ARGS}
