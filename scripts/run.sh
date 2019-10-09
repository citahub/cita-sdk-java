#!/usr/bin/env bash

if [ ${TEST_TYPE} == UT ]; then
  echo "run unit test"
  bash scripts/run-unit-test.sh
  exit $?
fi

if [ ${TEST_TYPE} == ST ]; then
  echo "run system test"
  bash scripts/run-system-test.sh
  bash scripts/travis-upload-test-report-to-gh-pages.sh $?
fi
