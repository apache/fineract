#!/bin/bash

VERSION=$1
if [ -z $VERSION ]; then
  echo "Usage: $0 version [releasedDate]"
  exit 1;
fi

RELDATE=$2
if [ -z $RELDATE ]; then
  RELDATE=$(date '+%d/%b/%y')
fi

APPDIR=apps/community-app
"$APPDIR/release.sh" "$VERSION" "$RELDATE"
echo "releaseVersion=$VERSION.RELEASE" > mifosng-provider/gradle.properties

