#!/bin/bash
export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which javac)))))
echo 'Set JAVA_HOME='$JAVA_HOME
export PATH=$PATH:$JAVA_HOME/bin
echo 'Set PATH='$PATH
export CLASSPATH=.:$JAVA_HOME/lib
echo 'Set CLASSPATH='$CLASSPATH
