#!/bin/bash
# A shell script to deploy binary for development to AppEngine.
# It will deploy the binary to be of the version_id passed in so that it can
# be tested before switched to default version using the AppEngine admin
# interface.

if [ "$#" -ne 1 ]; then
  echo "Usage: deploy_dev.sh version_id"
  exit 1
fi

VERSION_ID=$1

# Replaces appengine.app.version id found in the pom.xml.
# Args:
#   $1: target version id to replace the id in the pom.xml.
#   $2: POM file to replace version id.
replace_pom_version_id() {
  APPENGINE_VERSION='appengine.app.version'
  TARGET_VERSION_ID=$1
  POM_FILE=$2

  sed "s/$APPENGINE_VERSION.*$APPENGINE_VERSION/"\
"$APPENGINE_VERSION\>$TARGET_VERSION_ID\<\/$APPENGINE_VERSION/g" \
$POM_FILE > "$POM_FILE.bak"
  mv $POM_FILE.bak $POM_FILE
}

replace_pom_version_id $VERSION_ID 'quizz-web/pom.xml'
replace_pom_version_id $VERSION_ID 'quizz-tasks/pom.xml'
mvn install -U && cd quizz-ear && mvn appengine:update
