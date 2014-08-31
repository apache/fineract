#!/bin/bash
cd mifosng-provider
# NOTE: The --info, while quite a bit more verbose, is VERY useful to understand failures on Travis,
# where you do not have access to any files like build/reports/tests/index.html, only the Console. 
# @see http://mrhaki.blogspot.ch/2013/05/gradle-goodness-show-more-information.html
# @see http://forums.gradle.org/gradle/topics/whats_new_in_gradle_1_1_test_logging for alternative 
./gradlew --info clean licenseMain licenseTest licenseIntegrationTest test
