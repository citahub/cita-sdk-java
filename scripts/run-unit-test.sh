#!/usr/bin/env bash

# start a CITA chain for running tests， and init config
OLD_ADMIN_PRIVATE_KEY=0x5a0257a4778057a8a7d97809bd209055b2fbafa654ce7d31ec7191066b9225e6
OLD_ADMIN_ADDRESS=0x827b05e4b070167a360e123fd06566eeb1a79fa5
TEST_ADMIN_PRIVATE_KEY=0x5f0258a4778057a8a7d97809bd209055b2fbafa654ce7d31ec7191066b9225e6
TEST_ADMIN_ADDRESS=0x4b5ae4567ad5d9fb92bc9afd6a657e6fa13a2523
docker pull ${DOCKER_IMAGE_URL}:sha3
docker run --rm -d -p 60702:1337 --name=test_sdk_cita ${DOCKER_IMAGE_URL}:sha3
sleep 2m
docker ps
sed -i "s/:1337/:60702/" ./tests/src/test/resources/config.properties.example
sed -i "s/${OLD_ADMIN_ADDRESS}/${TEST_ADMIN_ADDRESS}/" ./tests/src/test/resources/config.properties.example
sed -i "s/${OLD_ADMIN_PRIVATE_KEY}/${TEST_ADMIN_PRIVATE_KEY}/" ./tests/src/test/resources/config.properties.example
cp -r ./tests/src/test/resources/config.properties.example ./tests/src/test/resources/config.properties
cat ./tests/src/test/resources/config.properties

# run unit and integration tests
./gradlew --info check jacocoRootTestReport
UNIT_TEST_RESULT=$?
echo "UNIT_TEST_RESULT：${UNIT_TEST_RESULT}"

# if success, upload code coverage metrics
if [ ${UNIT_TEST_RESULT} -eq 0 ]; then
   echo "Upload code coverage metrics"
   bash <(curl -s https://codecov.io/bash)
fi

# remove docker container
docker rm -f test_sdk_cita

# exit UNIT TEST CODE
exit ${UNIT_TEST_RESULT}