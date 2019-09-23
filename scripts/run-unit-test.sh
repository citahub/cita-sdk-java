#!/usr/bin/env bash

../gradlew --info check jacocoRootTestReport
bash <(curl -s https://codecov.io/bash)