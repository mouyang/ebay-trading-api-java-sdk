eBay Trading API Java SDK 2.0

Purpose

* Convert the eBay Trading API repository (https://github.com/eBay/trading-api-java-sdk) to a standard Maven structure
* Java 11 and Android compatibility because the current repository is only compatible with Java 8 on the desktop.

Approach

This repository codifies the conversion steps to meet the stated purpose and the repository conversion steps from the repository itself.  This facilitates development because conversion as code makes it really to test and re-test any changes.  When converting a repository, there is no need to track mistakes and intermediate changes.  Only the final repository state matters.  

This project generates classes generated from WSDL documents provided by eBay via wsimport.  Because the WSDL documents are versioned, **each project build is tied to a specific verion of the eBay Trading API.  Therefore if an SDK has not been published officially for your target API version, build this project from source for the target API version of your choice.**

Currently this project uses Gradle for trading-api-sdkcore-android and Maven for everything else.  This is because of the original developer's lack of knowledge of Android + Gradle at the time.  Gradle is required to build an AAR.  The Maven modules will eventually be migrated to Gradle because Maven doesn't have a plugin to build AAR files.

Environment

This project was built under the following tools and conditions.  Not all of the tool versions are strict but there is no guarantee that it will work outside of these parameters.
* Java SE 11.0.27 2025-04-15 LTS - Java 11+ is a requirement
* Maven 3.9.9
* Gradle 8.13
* Windows 11 10.0 amd64
* eBay Trading API version 1379

Installation Steps

* Clone the eBay Trading API repository in <EBAY_TRADING_API_DIR>
* Run the conversion script `./restructure.sh <EBAY_TRADING_API_DIR> <EBAY_API_VERSION>`

Changes Made
* For wsimport generated classes - xs:dateTime no longer outputs java.util.Calendar.  It now outputs java.time.Instant.
* non-UI and UI classes have been split into two modules (trading-api-sdkcore and trading-api-sdkcore-ui).  This was done in hopes this would work in Android immediately but it did not in part because of AWT and Swing dependencies which are not available in Android.
* java.xml imports replaced with jakarta.xml to be compliant with Java 11.
* Bare minimum Call classes in source/core/src/com/ebay/sdk/call just to get UI classes to compile: FetchTokenCall, GetSessionIDCall.  These still need to be reviewed to see what API versions are compatible with these Call classes.

Outstanding Issues

The Call classes (source/apiCalls) have not been ported.  The root of this is that the Call classes are API version-dependent whereas the SDK strives to be version-independent.  As a result:  
* Some classes in source/core/src/com/ebay/sdk/helper have been removed because they use Call and Type classes that no longer exist: GetCategoryFeaturesHelper, cache/CategoriesDownloader, cache/DetailsDownloader, cache/FeaturesDownloader, eBayDetailsHelper1, ui/DialogAccount.
* Some classes in source/core/com/ebay/sdk that qualify for trading-api-sdkcore have been rewritten to temporarily remove AWT + Swing dependencies: ApiCall, ApiCredential.  These removed dependencies need to be replaced with something that is not UI-dependent.
* tests (source/SanityTest, source/PerfTest) have not been ported.

A strategy to build API version-dependent functionality is needed before Call classes can be ported.

Other Issues
* samples and tutorials directories have not been ported - While they are useful for learning, they are not critical to getting a working build and therefore they are currently deprioritized until rest of the project has been stabilized.
