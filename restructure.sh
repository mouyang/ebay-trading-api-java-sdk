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
## the mv is not required but it will show up in `git status` as a moved file as a 
mv build/custom-binding.xml build/jaxb-binding.xjb trading-api-eBLBaseComponents/src/jaxws
cp $pwd/trading-api-eBLBaseComponents/src/jaxws/custom-binding.xml $pwd/trading-api-eBLBaseComponents/src/jaxws/jaxb-binding.xjb trading-api-eBLBaseComponents/src/jaxws
rm -rf source/core/src/com/ebay/soap/eBLBaseComponents/*.java
# sdkcore
mkdir -p trading-api-sdkcore trading-api-sdkcore/src/main/java
cp $pwd/trading-api-sdkcore/pom.xml trading-api-sdkcore
## must copy+remove instead of moving when working with multiple directories.  `mv` on the second and subsequent 
## directories will fail because the target is not empty. 
for dir in source/core/src source/helper/src; do
    cp -r $dir/* trading-api-sdkcore/src/main/java
    rm -rf $dir
done
## Fortunately this works because the imports follow a consistent spacing format.  OpenRewrite would have been 
## preferred to refactor this but attempting use this was successful.
find trading-api-sdkcore/src/main/java -name *.java | xargs sed -i -e \
    "s/^import javax\.xml\.ws/import jakarta.xml.ws/" \
    -e "s/^import javax\.xml\.soap/import jakarta.xml.soap/" \
    -e "s/^import javax\.xml\.bind/import jakarta.xml.bind/"
## Drop anything core or helper class that uses a *Call class or a *Type that does not have a source file in the 
## source repo.  These removed classes may be reintroduced later but will need to be evaluated on a case-by-case basis.
rm trading-api-sdkcore/src/main/java/DialogFetchToken.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/GetCategoryFeaturesHelper.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/CategoriesDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/DetailsDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/cache/FeaturesDownloader.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/eBayDetailsHelper1.java \
    trading-api-sdkcore/src/main/java/com/ebay/sdk/helper/ui/DialogAccount.java
# build
mvn clean install