#!/bin/sh

#
# debug
#
#JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Djna.encoding=UTF-8 -Djna.protected=true -Djna.dump_memory=true -Dtemperance.pid.dir=/tmp"

JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Djna.encoding=UTF-8 -Djna.protected=false -Djna.dump_memory=false -Dtemperance.pid.dir=/var/run"
TP_PORT="17001"

export JAVA_HOME=$JAVA_HOME
export _JAVA_OPTIONS=$JAVA_OPTIONS

DIR=$(cd $(dirname $0);pwd)

LIB=$DIR
LIB=$LIB:"$DIR/dist/temperance-0.12.jar"
LIB=$LIB:"$DIR/resources"
LIB=$LIB:"$DIR/lib/libmecab-0.01.jar"
LIB=$LIB:"$DIR/lib/libmemcached-0.04.jar"
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

$JAVA_HOME/bin/java $JAVA_OPTIONS -cp $LIB temperance.Stop -p $TP_PORT
