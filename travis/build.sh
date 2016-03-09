#!/bin/bash

echo "Building branch: $TRAVIS_BRANCH on slug: $TRAVIS_REPO_SLUG is pull request: $TRAVIS_PULL_REQUEST"

if [[ "${TRAVIS_PULL_REQUEST}" = "false"  ]] && [[ $TRAVIS_BRANCH == 'master' ]] && [[ $TRAVIS_REPO_SLUG == 'Substeps/substeps-framework' ]]; then
   mvn deploy --settings travis/settings.xml
else
  mvn verify
fi