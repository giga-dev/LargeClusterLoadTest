#!/bin/bash
DIRNAME=$(dirname ${BASH_SOURCE[0]})

if [[ -z "${GS_DIR}" ]]; then
    echo "GS_DIR environment variable is not set"
    exit 1
fi


#SERVER=""

if [[ -n "${SERVER}" ]]; then
    SERVER_PARAM="--server=${SERVER}"
fi


${GS_DIR}/bin/gs.sh ${SERVER_PARAM} pu deploy mirror ${DIRNAME}/mirror/target/mirror.jar

${GS_DIR}/bin/gs.sh ${SERVER_PARAM} pu deploy --partitions=8 --backups=1 --max-instances-per-vm=1 space ${DIRNAME}/space/target/space.jar

${GS_DIR}/bin/gs.sh ${SERVER_PARAM} pu deploy crew-members-feeder ${DIRNAME}/crew-members-feeder/target/crew-members-feeder.jar

${GS_DIR}/bin/gs.sh ${SERVER_PARAM} pu deploy benchmark ${DIRNAME}/benchmark/target/benchmark.jar
