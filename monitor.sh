#!/bin/sh

JAVA_OPTIONS="-Dfile.encoding=UTF-8"

TARGET_HOST="localhost"
TARGET_PORT="17001"
INTERVAL="1.0"

export JAVA_HOME=$JAVA_HOME
export _JAVA_OPTIONS=$JAVA_OPTIONS

DIR=$(cd $(dirname $0);pwd)

LIB=$DIR
LIB=$LIB:"$DIR/dist/temperance-0.03.jar"
LIB=$LIB:"$DIR/resources"
LIB=$LIB:"$DIR/lib/commons-cli-1.0.jar"
LIB=$LIB:"$DIR/lib/protobuf/protobuf-java-2.2.0.jar"
LIB=$LIB:"$DIR/lib/protobuf/protobuf-socket-rpc.jar"

$JAVA_HOME/bin/java $JAVA_OPTIONS -cp $LIB temperance.Monitor -th $TARGET_HOST -tp $TARGET_PORT --interval $INTERVAL
