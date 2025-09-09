#!/usr/bin/bash

restructure() {
	# remove all class and jar files that can be generated from source
	find . -name *.class -o -name *.jar | xargs rm	
}

pwd=`pwd`
target=$1
branch=${2:-main}

cd $target
# start from a clean target
git restore --staged .
git checkout .
git clean -fd
git checkout $branch
restructure
# come back to this directory
cd $pwd