#!/bin/sh

JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Dtemperance.pid.dir=/tmp"

export JAVA_HOME=$JAVA_HOME
export _JAVA_OPTIONS=$JAVA_OPTIONS

DIR=$(cd $(dirname $0);pwd)

LIB=$DIR
#LIB=$LIB:"$DIR/dist/temperance-0.01.jar"
LIB=$LIB:"$DIR/bin"
LIB=$LIB:"$DIR/resources"
LIB=$LIB:"$DIR/lib/akuma-1.3.jar"
LIB=$LIB:"$DIR/lib/libmecab-0.01.jar"
LIB=$LIB:"$DIR/lib/libmemcached-0.01.jar"
LIB=$LIB:"$DIR/lib/commons-cli-1.0.jar"
LIB=$LIB:"$DIR/lib/protobuf-java-2.2.0.jar"
LIB=$LIB:"$DIR/lib/protobuf-socket-rpc.jar"
LIB=$LIB:"$DIR/lib/jna.jar"

$JAVA_HOME/bin/java -cp $LIB temperance.Stop
