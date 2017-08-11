#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 currentMajorVersion newMajorVersion" >&2
    echo "E.g: $0 v1 v2" >&2
    exit 1
fi

oldMajorVersion=$1
newMajorVersion=$1

echo "Uplifting major version from $oldMajorVersion to $newMajorVersion"







echo "Completed, ensure you check all changes made before committing"
exit 0
