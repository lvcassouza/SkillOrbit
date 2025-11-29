#!/usr/bin/env sh

APP_BASE_NAME=`basename "$0"`
APP_HOME=`dirname "$0"`
cd "$APP_HOME"

DEFAULT_JVM_OPTS=""

GRADLE_WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_WRAPPER_MAIN="org.gradle.wrapper.GradleWrapperMain"

CLASSPATH="$GRADLE_WRAPPER_JAR"

JAVA_CMD="java"

exec "$JAVA_CMD" $DEFAULT_JVM_OPTS -cp "$CLASSPATH" $GRADLE_WRAPPER_MAIN "$@"

