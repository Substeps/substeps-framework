#!/usr/bin/env bash
set -ev

## TODO - Look into how Travis interacts with git tags, may move to "if tagged then release"

##if [ "not a pull request" ] && [ "deploy enabled" ] && [ "is a release branch" ]; then
##     perform a maven release cycle
##elif [ "not a pull request" ] && [ "deploy enabled" ]; then
##     perform a maven deploy cycle
##else
##     perform a maven build, test and integration test cycle
##fi

echo "TRAVIS_PULL_REQUEST: ${TRAVIS_PULL_REQUEST}"
echo "DEPLOY_ENABLED: ${DEPLOY_ENABLED}"
echo "TRAVIS_BRANCH: ${TRAVIS_BRANCH}"
echo "TRAVIS_TAG: ${TRAVIS_TAG}"

if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${DEPLOY_ENABLED}" = "true" ] && [ "${TRAVIS_BRANCH}" = "release" ]; then
    mvn --batch-mode --settings travis/settings.xml release:clean release:prepare release:perform
elif [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${DEPLOY_ENABLED}" = "true" ]; then
    mvn deploy --settings travis/settings.xml
else
    mvn verify
fi