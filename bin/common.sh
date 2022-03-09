# Common.sh

ROOT=$(dirname ${LOC})

get_maven_val(){
  mvn help:evaluate -Dexpression=${1} -f ${ROOT}/pom.xml | grep -v INFO
}

VERSION=$(get_maven_val project.version)
NAME=$(get_maven_val project.artifactId)

CLASSPATH=${ROOT}/target/${NAME}-${VERSION}-jar-with-dependencies.jar
export CLASSPATH

PACKAGE="io.github.hindmasj.redis.client"
MAIN="${PACKAGE}.RedisClient"
ENCRYPT="${PACKAGE}.Cryptography"
PARSE="${PACKAGE}.GeoipFileParser"
SEARCH="${PACKAGE}.DemoSearch"
PROTOCOLS_PARSE="${PACKAGE}.ProtocolsFileParser"
SERVICES_PARSE="${PACKAGE}.ServicesFileParser"
