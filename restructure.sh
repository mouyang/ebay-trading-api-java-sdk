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
# generate types from WSDL + remove related java source files generated from WSDL
mkdir -p trading-api-eBLBaseComponents trading-api-eBLBaseComponents/src/jaxws
cp $pwd/pom.xml .
cp $pwd/trading-api-eBLBaseComponents/pom.xml trading-api-eBLBaseComponents
## the mv is not required but it will show up in `git status` as a moved file as a 
mv build/custom-binding.xml build/jaxb-binding.xjb trading-api-eBLBaseComponents/src/jaxws
cp $pwd/trading-api-eBLBaseComponents/src/jaxws/custom-binding.xml $pwd/trading-api-eBLBaseComponents/src/jaxws/jaxb-binding.xjb trading-api-eBLBaseComponents/src/jaxws
rm -rf source/core/src/com/ebay/soap/eBLBaseComponents/*.java

# build
mvn clean package