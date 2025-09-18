#!/usr/bin/bash

cleanup() {
    # come back to this directory
    cd $pwd
}

trap cleanup EXIT

pwd=`pwd`
target=$1
branch=${2:-main}

# easier to move to the target repo and restore after instead of staying in this repo because specifying the target 
# directory in for each app (git, mvn, etc if it can be specified at all)
cd $target
# start from a clean target
git restore --staged .
git checkout .
git clean -fd
git checkout $branch

# new .gitignore entries
echo "" >> .gitignore
cat $pwd/gitignore >> .gitignore
# remove all class and jar files that can be generated from source
find . -name *.class -o -name *.jar | xargs rm
# eBLBaseComponents
## generate types from WSDL + remove related java source files generated from WSDL
mkdir -p trading-api-eBLBaseComponents trading-api-eBLBaseComponents/src/jaxws
cp $pwd/pom.xml .
cp $pwd/trading-api-eBLBaseComponents/pom.xml trading-api-eBLBaseComponents
cp $pwd/trading-api-eBLBaseComponents/src/jaxws/custom-binding.xml \
	$pwd/trading-api-eBLBaseComponents/src/jaxws/jaxb-binding.xjb \
	trading-api-eBLBaseComponents/src/jaxws
# sdkcore
mkdir -p trading-api-sdkcore
cp $pwd/trading-api-sdkcore/pom.xml trading-api-sdkcore
mkdir -p trading-api-sdkcore/src/main/java/com/ebay/sdk
cp -r source/core/src/com/ebay/sdk/* trading-api-sdkcore/src/main/java/com/ebay/sdk
mkdir -p trading-api-sdkcore/src/main/java
cp -r source/helper/src/* trading-api-sdkcore/src/main/java

## Fortunately this works because the imports follow a consistent spacing format.  OpenRewrite would have been 
## preferred to refactor this but attempting use this was successful.
find trading-api-sdkcore/src/main/java -name *.java | xargs sed -i -e \
    "s/^import javax\.xml\.ws/import jakarta.xml.ws/" \
    -e "s/^import javax\.xml\.soap/import jakarta.xml.soap/" \
    -e "s/^import javax\.xml\.bind/import jakarta.xml.bind/"
## Drop anything core or helper class that uses a *Call class or a *Type that does not have a source file in the 
## source repo.  These removed classes may be reintroduced later but will need to be evaluated on a case-by-case basis.
##
## Files are removed individually because it is hard to join class references in files with Call/Type classes.
rm trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/GetCategoryFeaturesHelper.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/CategoriesDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/DetailsDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/FeaturesDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/eBayDetailsHelper1.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/ui/DialogAccount.java

## Temporarily remove anything related to Java AWT + Swing in order to focus first on the API.  API and UI should be 
## compiled and packaged separately.  Catches import statements but any fully-qualified references will need to be 
## addressed individually.
cp $pwd/trading-api-sdkcore/src/main/java/com/ebay/sdk/ApiCall.java $pwd/trading-api-sdkcore/src/main/java/com/ebay/sdk/ApiCredential.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk
find trading-api-sdkcore -name *.java | xargs grep -l -e "^import java\.awt\..*;$" -e  "^import javax\.swing\..*;$" | xargs rm
# build
mvn clean install