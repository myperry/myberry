#!/bin/sh

#export MYBERRY_HOME=/app

if [ ! -f "${MYBERRY_HOME}/bin/runserver.sh" ];then
  echo "Please set the MYBERRY_HOME variable in your environment!"
  exit 1;
fi

sh ${MYBERRY_HOME}/bin/runserver.sh org.myberry.server.ServerStartup $@
