#!/bin/sh

#
# note:
#  OSX: JAVA_HOME to change JAVA 1.5 (java1.6 occured "Failed to get kernl.argmax: Bad address")
#      /System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
#      to
#      /System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home
#    shell > export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home
#

#
# debug
#
#JVM_OPTIONS="-server -Xmx512m -Xms256m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:+CMSParallelRemarkEnabled -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=32 -XX:TargetSurvivorRatio=90 -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"
#JMX_OPTIONS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7900 -Dcom.sun.management.jmxremote.authenticate=false"
#JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Djna.encoding=UTF-8 -Djna.protected=false -Djna.dump_memory=true -Dtemperance.pid.dir=/tmp"
#MEMCACHED="localhost:11211"
#MECABRC="/opt/local/etc/mecabrc"
#TP_PORT="17001"

JVM_OPTIONS="-server -Xmx512m -Xms256m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:+CMSParallelRemarkEnabled -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=32 -XX:TargetSurvivorRatio=90"
JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Djna.encoding=UTF-8 -Djna.protected=false -Djna.dump_memory=false -Dtemperance.pid.dir=/var/run"
JMX_OPTIONS=""
MEMCACHED="localhost:11211"
MECABRC="/etc/mecabrc"
TP_PORT="17001"
TP_INI_THREADS="300"
TP_MAX_THREADS="500"

export JAVA_HOME=$JAVA_HOME
export _JAVA_OPTIONS=$JAVA_OPTIONS

DIR=$(cd $(dirname $0);pwd)

LIB=$DIR
LIB=$LIB:"$DIR/dist/temperance-0.07.jar"
LIB=$LIB:"$DIR/resources"
LIB=$LIB:"$DIR/lib/libmecab-0.01.jar"
LIB=$LIB:"$DIR/lib/libmemcached-0.01.jar"
LIB=$LIB:"$DIR/lib/akuma-1.3.jar"
LIB=$LIB:"$DIR/lib/jna.jar"
LIB=$LIB:"$DIR/lib/commons-cli-1.0.jar"
LIB=$LIB:"$DIR/lib/commons-logging-1.1.jar"
LIB=$LIB:"$DIR/lib/log4j-1.2.13.jar"
LIB=$LIB:"$DIR/lib/jparsec-2.0.jar"
LIB=$LIB:"$DIR/lib/protobuf/protobuf-java-2.2.0.jar"
LIB=$LIB:"$DIR/lib/protobuf/protobuf-socket-rpc.jar"
LIB=$LIB:"$DIR/lib/msgpack/msgpack-0.3.jar"
LIB=$LIB:"$DIR/lib/msgpack/msgpack-rpc-0.3.0.jar"

$JAVA_HOME/bin/java $JVM_OPTIONS $JAVA_OPTIONS $JMX_OPTIONS -cp $LIB temperance.Start -memc $MEMCACHED -mecabrc $MECABRC -p $TP_PORT -iniThreads $TP_INI_THREADS -maxThreads $TP_MAX_THREADS -verbose_thread -daemonize
