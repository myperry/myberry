#!/bin/sh

#===========================================================================================
# Java Environment Setting
#===========================================================================================

if [ ! -f "$JAVA_HOME/bin/java" ];then
  echo "Please set the JAVA_HOME variable in your environment, We need java(x64)!"
  exit 1
fi

export JAVA=$JAVA_HOME/bin/java
export BASE_DIR=$(dirname $0)/..

#===========================================================================================
# JVM Configuration
#===========================================================================================

JAVA_OPT="${JAVA_OPT} -server -Xmx128m -Xms128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=64m"
JAVA_OPT="${JAVA_OPT} -cp .:$BASE_DIR/conf:$BASE_DIR/lib/*"

$JAVA ${JAVA_OPT} $@