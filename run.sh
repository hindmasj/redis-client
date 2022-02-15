#!/bin/bash

LOC=$(dirname $(readlink -f ${BASH_SOURCE[0]}))

get_maven_val(){
  mvn help:evaluate -Dexpression=${1} -f ${LOC}/pom.xml | grep -v INFO
}

VERSION=$(get_maven_val project.version)
NAME=$(get_maven_val project.artifactId)

CLASSPATH=${LOC}/target/${NAME}-${VERSION}-jar-with-dependencies.jar
export CLASSPATH

MAIN=io.github.hindmasj.redis.client.RedisClient
java ${MAIN}
