#!/bin/sh

#
# note:
#  OSX: JAVA_HOME to change JAVA 1.5 (java1.6 occured "Failed to get kernl.argmax: Bad address")
#      /System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
#      to
#      /System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home
#    shell > export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home
#

JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Dtemperance.pid.dir=/tmp"
MEMCACHED="localhost:11211"
MECABRC="/opt/local/etc/mecabrc"

export JAVA_HOME=$JAVA_HOME
export _JAVA_OPTIONS=$JAVA_OPTIONS

DIR=$(cd $(dirname $0);pwd)

LIB=$DIR
#LIB=$LIB:"$DIR/dist/temperance-0.01.jar"
LIB=$LIB:"$DIR/bin"
LIB=$LIB:"$DIR/resources"
LIB=$LIB:"$DIR/lib/libmecab-0.01.jar"
LIB=$LIB:"$DIR/lib/libmemcached-0.01.jar"
LIB=$LIB:"$DIR/lib/akuma-1.3.jar"
LIB=$LIB:"$DIR/lib/jna.jar"
LIB=$LIB:"$DIR/lib/commons-cli-1.0.jar"
LIB=$LIB:"$DIR/lib/protobuf-java-2.2.0.jar"
LIB=$LIB:"$DIR/lib/protobuf-socket-rpc.jar"
LIB=$LIB:"$DIR/lib/jparsec-2.0.jar"

$JAVA_HOME/bin/java -cp $LIB temperance.Start -memc $MEMCACHED -mecabrc $MECABRC -p 17001 -daemonize
