#!/usr/bin/env bash

copy_to_root() {
    # new .gitignore entries
    echo "" >> .gitignore
    cat $pwd/gitignore >> .gitignore
    # parent pom
    copy_maven_pom
    # gradle
    cp -r $pwd/build.gradle.kts $pwd/settings.gradle.kts $pwd/gradle.properties $pwd/local.properties $pwd/mvnvm.properties $pwd/gradlew $pwd/gradlew.bat $pwd/gradle/ .
}

copy_to_eBLBaseComponents() {
    module_dir="trading-api-eBLBaseComponents"
    copy_maven_pom $module_dir
    copy_gradle_build $module_dir

    ## generate types from WSDL + remove related java source files generated from WSDL
    mkdir -p $module_dir $module_dir/src/jaxws
    cp $pwd/$module_dir/pom.xml $module_dir
    cp $pwd/$module_dir/src/jaxws/custom-binding.xml \
        $pwd/$module_dir/src/jaxws/jaxb-binding.xjb \
        $module_dir/src/jaxws
}

copy_to_sdkcore() {
    module_dir="trading-api-sdkcore"
    copy_maven_pom $module_dir

    mkdir -p $module_dir/$mvn_src_dir/com/ebay/sdk
    cp -r source/core/src/com/ebay/sdk $module_dir/$mvn_src_dir/com/ebay

    ## Temporarily remove anything related to Java AWT + Swing in order to focus first on the API.  API and UI should be 
    ## compiled and packaged separately.  Catches import statements but any fully-qualified references will need to be 
    ## addressed individually.
    cp $pwd/$module_dir/$mvn_src_dir/com/ebay/sdk/ApiCall.java $pwd/$module_dir/$mvn_src_dir/com/ebay/sdk/ApiCredential.java \
        $module_dir/$mvn_src_dir/com/ebay/sdk
    find $module_dir -name *.java | xargs grep -l -e "^import java\.awt\..*;$" -e  "^import javax\.swing\..*;$" | xargs rm

    javax_to_jakarta $module_dir
    echo "$module_dir/$mvn_src_dir/com/ebay/sdk/TimeFilter.java" | calendar_to_instant
}

copy_to_ui() {
    module_dir="trading-api-ui"
    copy_maven_pom $module_dir
    copy_source_files $module_dir
    # only keep files that reference AWT/Swing
    find $module_dir -name *.java | xargs grep -L -e "^import java\.awt\..*;$" -e  "^import javax\.swing\..*;$" | xargs rm
    # this file was not covered in the common copy_source_files method because it doesn't follow Java packaging structure
    mkdir -p $module_dir/$mvn_src_dir/com/ebay/sdk/helper/ui
    cp source/core/src/DialogFetchToken.java $module_dir/$mvn_src_dir/com/ebay/sdk/helper/ui
    # drop these files because they are part of the sdkcore module
    rm $module_dir/$mvn_src_dir/com/ebay/sdk/ApiCall.java $module_dir/$mvn_src_dir/com/ebay/sdk/ApiCredential.java
    # bring in the bare minimum Call classes needed for the UI classes
    mkdir -p $module_dir/$mvn_src_dir/com/ebay/sdk/call
    cp $pwd/$module_dir/$mvn_src_dir/com/ebay/sdk/call/FetchTokenCall.java \
        $pwd/$module_dir/$mvn_src_dir/com/ebay/sdk/call/GetSessionIDCall.java \
        $module_dir/$mvn_src_dir/com/ebay/sdk/call
}

copy_calls_to_1331() {
    module_dir="trading-api-calls-1331"
    copy_maven_pom $module_dir
    mkdir -p $module_dir/$mvn_src_dir/com/ebay/sdk/call
    cp -r source/apiCalls/src/com/ebay/sdk/call $module_dir/$mvn_src_dir/com/ebay/sdk
    find $module_dir -name *Call.java | calendar_to_instant
}

copy_to_sdkcore_android() {
    module_dir="trading-api-sdkcore-android"
    src_main="src/main"

    mkdir -p $module_dir/$src_main
    cp $pwd/$module_dir/build.gradle.kts $module_dir 
    cp -r $pwd/$module_dir/$src_main $module_dir/src
}

copy_maven_pom() {
    module_dir=${1:-.}
    mkdir -p $module_dir
    cp $pwd/$module_dir/pom.xml $module_dir
}

copy_gradle_build() {
    module_dir=${1:-.}
    mkdir -p $module_dir
    cp $pwd/$module_dir/build.gradle.kts $module_dir
}

javax_to_jakarta() {
    target=$1
    ## Fortunately this works because the imports follow a consistent spacing format.  OpenRewrite would have been
    ## preferred to refactor this but attempting use this was successful.
    find $module_dir/$mvn_src_dir -name *.java | xargs sed -i -e \
        "s/^import javax\.xml\.ws/import jakarta.xml.ws/" \
        -e "s/^import javax\.xml\.soap/import jakarta.xml.soap/" \
        -e "s/^import javax\.xml\.bind/import jakarta.xml.bind/"
}

calendar_to_instant() {
    xargs sed -i \
        -e "s/java\.util\.Calendar/java.time.Instant/g" \
        -e "s/Calendar/Instant/g"
}

if [[ "$(basename "$0")" == "restructure.sh" ]]; then
    cleanup() {
        # come back to this directory
        cd $pwd
    }

    # clear out everything and point to the desired branch
    reset_repo() {
        git restore --staged .
        git checkout .
        git clean -fd
        git checkout $1
    }

    trap cleanup EXIT

    pwd=`pwd`
    target=$1
    api_version=$2
    branch=${3:-main}

    mvn_src_dir="src/main/java"

    # easier to move to the target repo and restore after instead of staying in this repo because specifying the target 
    # directory in for each app (git, mvn, etc if it can be specified at all)
    cd $target && reset_repo $branch

    module_pids=()
    copy_to_root & module_pids+=($!)
    copy_to_eBLBaseComponents & module_pids+=($!)
    copy_to_sdkcore & module_pids+=($!)
    #copy_to_ui & module_pids+=($!)
    copy_calls_to_1331 & module_pids+=($!)
    copy_to_sdkcore_android & module_pids+=($!)
    wait $module_pids

    # build
    ./gradlew clean publishToMavenLocal -PebayApiVersion=$2
fi