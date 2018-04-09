#!/usr/bin/env bash

docker-compose -f stroomQueryTestDb.yml up -d
#assume that the db will be fully spun up by the time gradle runs the int tests
./gradlew clean build "$@"
docker-compose -f stroomQueryTestDb.yml stop



