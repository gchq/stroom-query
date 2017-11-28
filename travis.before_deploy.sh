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

echo -e "STROOM_QUERY_VERSION: [${GREEN}${STROOM_QUERY_VERSION}${NC}]"

echo -e "Updating values in ${GREEN}bintray.json${NC}"

#Use jq to change some of the values in the bintray.json file, via a tmp file
jq ".version.name = \"${STROOM_QUERY_VERSION}\" | .version.desc = \"stroom-query-${STROOM_QUERY_VERSION}\" | .version.released = \"$(date +%Y-%m-%d)\" | .version.vcs_tag = \"${STROOM_QUERY_VERSION}\"" bintray.json > bintray.json.tmp

echo "The following changes have been applied to bintray.json"

diff bintray.json bintray.json.tmp

mv bintray.json.tmp bintray.json
