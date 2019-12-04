#!/usr/bin/env bash

DEBUG_LOG_PATH=./debug_log.txt
# generate sdk and update docker image
rm -r console/build/libs/* | true
./gradle-5.0/bin/gradle shadowJar > ${DEBUG_LOG_PATH}
test -e console/build/libs/console-*-all.jar ||  echo "build sdk failed !!!"
test -e console/build/libs/console-*-all.jar ||  exit 1
docker pull ${DOCKER_IMAGE_URL}:${CITA_DOCKER_IMAGE_TAG_NAME} > ${DEBUG_LOG_PATH}

# get cita_quality code
cd ..
test -e cita_quality | git clone ${SYSTEM_TEST_CODE_URL}
cp cita-sdk-java/console/build/libs/console-*-all.jar  ./cita_quality/systemTest/console.jar
cd ./cita_quality/systemTest
system_test_dir=$(pwd)
git checkout change-cryptape-citahub | git pull

# use local sdk change the maven sdk
CITA_SDk_VERSION=$( cat pom.xml | grep -A 1 ">core<" | grep version | awk -F "[<>]" '{print $3}')
CITA_SDK_LOCAL_FILE_PATH=${system_test_dir}/console-${CITA_SDk_VERSION}-all.jar
rm -r ${CITA_SDK_LOCAL_FILE_PATH} | true
cp -r  ./console.jar  ${CITA_SDK_LOCAL_FILE_PATH}

echo ${CITA_SDK_LOCAL_FILE_PATH}
TARGET_TEXT_LINE=$(($( cat pom.xml | grep -n  ">core<" | awk -F ":" '{print $1}')+2))
echo ${TARGET_TEXT_LINE}
CITA_SDK_LOCAL_FILE_PATH=${CITA_SDK_LOCAL_FILE_PATH//\//\\/}
echo ${CITA_SDK_LOCAL_FILE_PATH}
sed -i "${TARGET_TEXT_LINE} s/^/<systemPath>${CITA_SDK_LOCAL_FILE_PATH}<\/systemPath>\n/" pom.xml
sed -i "${TARGET_TEXT_LINE} s/^/<scope>system<\/scope>\n/" pom.xml
cat pom.xml | grep -A 3  ">core<"

# install cita-cli and run test
rm -r target/*
make install-cita-cli > ${DEBUG_LOG_PATH}
mvn test -Dtest=com.cryptape.function.sdkcase.**
MAVEN_TEST_RESULT=$?

# collect result
test -e $HOME/tmp || mkdir $HOME/tmp
test -e $HOME/tmp/${TRAVIS_JOB_ID} || mkdir $HOME/tmp/${TRAVIS_JOB_ID}
cp -R ${system_test_dir}/target/surefire-reports/html/* $HOME/tmp/${TRAVIS_JOB_ID}/
exit ${MAVEN_TEST_RESULT}
