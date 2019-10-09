#!/usr/bin/env bash
# This script is used to run on Travis CI to publish generated files to GitHub pages
# 1. Create a orphan branch named gh-pages: https://help.github.com/en/articles/creating-project-pages-using-the-command-line#create-a-master-branch
# 2. Enabling GitHub Pages to publish your site from gh-pages: https://help.github.com/en/articles/configuring-a-publishing-source-for-github-pages#enabling-github-pages-to-publish-your-site-from-master-or-gh-pages

# NOTES: all UPPER-CASE variables are ENV variables, lower-case variables are local defined variables

echo "Maven test result code :"$1
MAVEN_TEST_RESULT=$1

echo "Starting to update gh-pages"
# gh page info
owner_name=`echo $TRAVIS_REPO_SLUG|cut -d / -f 1`
repo_name=`echo $TRAVIS_REPO_SLUG|cut -d / -f 2`
gh_pages_url="https://cryptape.github.io/cita-sdk-java"
test_report_url="$gh_pages_url/reports/${TRAVIS_JOB_ID}/"

test_result_mark="×"
if [ ${MAVEN_TEST_RESULT} -eq 0 ]; then
  test_result_mark="✔"
fi

# go to home
cd $HOME

# using token clone gh-pages branch
git clone --quiet --branch=gh-pages  ${TEST_REPORT_CODE_URL} gh-pages > /dev/null

#go into diractory and copy data we're interested in to that directory
cd gh-pages
mkdir -p reports

# TODO: delete outdated reports
# add new report
mv $HOME/tmp/${TRAVIS_JOB_ID} reports/
ls -l reports/${TRAVIS_JOB_ID}

# update index.html
echo "<div><a href=\"$test_report_url\">Test report </a> for <a href=\"$TRAVIS_JOB_WEB_URL\">Build #$TRAVIS_JOB_NUMBER</a> $test_result_mark</div>" >> index.html
# setup git user
git config user.email "travis@travis-ci.org"
git config user.name "Travis CI"

# add, commit and push files
commit_message="Test report $test_report_url for CI build $travis_build_url"
git add -f .
git commit -m "$commit_message"
git push -fq origin gh-pages > /dev/null
echo "Test report uploaded to $test_report_url"

# return maven test result
exit ${MAVEN_TEST_RESULT}

