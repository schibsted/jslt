ARTIFACT_PATH=../build/libs/jslt*-all.jar
ARTIFACT=`basename $ARTIFACT_PATH`
set -e
aws s3 cp $ARTIFACT_PATH s3://spt-data-dev-lmg/lambda/jstl2/
aws lambda update-function-code --function-name JSTL2Demo --s3-bucket spt-data-dev-lmg --s3-key lambda/jstl2/$ARTIFACT --publish
aws s3 cp lambda.html s3://spt-data-dev-public-web/jstl2.html
