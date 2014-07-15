#!/bin/bash
# A shell script to deploy binary for development to AppEngine.
# It will deploy the binary to be of the version_id passed in to the given
# appengine id so that it can be tested before switching to default version
# using the AppEngine admin interface.
# For dev testing, do:
#   ./deploy.sh SOME_VERSION_ID
# and it will be deployed to crowd-power.
#
# For prod, do:
#   ./deploy.sh SOME_VERSION_ID SOME_APP_ID
# and it will be deployed to $SOME_APP_ID.

if [ "$#" -lt 1 ]; then
  echo "Usage: deploy.sh version_id [app_id]"
  exit 1
fi

VERSION_ID=$1
APP_ID='crowd-power'
# Endpoint web client id for dev version.
CLIENT_ID='572385566876.apps.googleusercontent.com'

if [ "$#" -gt 1 ]; then
  APP_ID=$2
fi

# Replaces config found in the pom.xml such as version id and appengine id.
# Args:
#   $1: target version id to replace the id in the pom.xml.
#   $2: Appengine id to push to.
#   $3: Endpoint client id for authentication purpose.
replace_pom_config() {
  POM_FILE='pom.xml'
  chmod 664 $POM_FILE

  # Modifies the version id.
  APPENGINE_VERSION='appspot.app.version'
  TARGET_VERSION_ID=$1
  # Replaces the value surrounded by $APPENGINE_VERSION with the
  # $TARGET_VERSION_ID.
  sed "s/$APPENGINE_VERSION.*$APPENGINE_VERSION/"\
"$APPENGINE_VERSION\>$TARGET_VERSION_ID\<\/$APPENGINE_VERSION/g" \
$POM_FILE > "$POM_FILE.bak"
  mv $POM_FILE.bak $POM_FILE

  # Modifies the app id.
  APPENGINE_ID='appspot.app.id'
  TARGET_APP_ID=$2
  # Replaces the value surrounded by $APPENGINE_ID with the $TARGET_APP_ID.
  sed "s/$APPENGINE_ID.*$APPENGINE_ID/"\
"$APPENGINE_ID\>$TARGET_APP_ID\<\/$APPENGINE_ID/g" \
$POM_FILE > "$POM_FILE.bak"
  mv $POM_FILE.bak $POM_FILE

  # Modifies the endpoint client id.
  CLIENT_ID='auth.client.id'
  TARGET_CLIENT_ID=$3
  # Replaces the value surrounded by $CLIENT_ID with the $TARGET_CLIENT_ID.
  sed "s/$CLIENT_ID.*$CLIENT_ID/"\
"$CLIENT_ID\>$TARGET_CLIENT_ID\<\/$CLIENT_ID/g" \
$POM_FILE > "$POM_FILE.bak"
  mv $POM_FILE.bak $POM_FILE

  # Updates the app id in cron file.
  CRON_FILE='quizz-web/src/main/webapp/WEB-INF/cron.xml'
  chmod 664 $CRON_FILE
  # Replaces the gs_bucket_name with the $TARGET_APP_ID's gs_bucket_name.
  sed "s/gs_bucket_name=.*\.appspot/"\
"gs_bucket_name=$TARGET_APP_ID\.appspot/g" \
$CRON_FILE > "$CRON_FILE.bak"
  mv $CRON_FILE.bak $CRON_FILE
}

# Copy stuffs to local disk.
rm -R -f ~/quizz
cd .. && cp -rf quizz ~/ && cd ~/quizz
replace_pom_config $VERSION_ID $APP_ID $CLIENT_ID
mvn install -U && cd quizz-ear && mvn appengine:update
