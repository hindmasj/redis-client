#!/bin/bash

LOC=$(dirname $(readlink -f ${BASH_SOURCE[0]}))

. ${LOC}/common.sh

java ${PROTOCOLS_PARSE}
java ${SERVICES_PARSE}
