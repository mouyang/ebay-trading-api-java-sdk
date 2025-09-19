eBay Trading API Java SDK 2.0

Purpose

* Convert the eBay Trading API repository (https://github.com/eBay/trading-api-java-sdk) to a standard Maven structure
* Java 11 and Android compatibility because the current repository is only compatible with Java 8 on the desktop.

Approach

This repository codifies the conversion steps to meet the stated purpose and the repository conversion steps from the repository itself.  This facilitates development because conversion as code makes it really to test and re-test any changes.  When converting a repository, there is no need to track mistakes and intermediate changes.  Only the final repository state matters.  

Minimum Build Requirements

* Java SE 11
* Maven 3

Installation Steps

After pulling this repository, 

# Pull the eBay Trading API repository
# Run the conversion script `./restructure.sh <EBAY_TRADING_API_DIR>`