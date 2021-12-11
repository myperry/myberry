#!/bin/sh

MAIN_CLASS=org.myberry.server.ServerStartup
PIDPROC=`ps -ef | grep "${MAIN_CLASS}" | grep -v 'grep'| awk '{print $2}'`

if [ -z "$PIDPROC" ];then
 echo "$MAIN_CLASS is not running"
 exit 0
fi

echo "PIDPROC: "$PIDPROC
for PID in $PIDPROC
do
if kill -15 $PID
   then echo "MYBERRY $MAIN_CLASS (Pid:$PID) was force stopped at " `date`
fi
done
echo stop finished.
