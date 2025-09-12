/*
Copyright (c) 2017 eBay, Inc.
This program is licensed under the terms of the eBay Common Development and 
Distribution License (CDDL) Version 1.0 (the "License") and any subsequent 
version thereof released by eBay.  The then-current version of the License 
can be found at https://www.codebase.ebay.com/Licenses.html and in the 
eBaySDKLicense file that is under the eBay SDK install directory.
*/

package com.ebay.sdk;

/**
 * Defines credentials to be used for making eBay API call. Set either
 * eBay token (seteBayToken()) or ApiAccount plus eBayAccount since
 * they are exclusive to each other.
 * <br>
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: eBay Inc.</p>
 * @author Weijun Li
 * @version 1.0
 */
public class ApiCredential {

  private ApiAccount apiAccount = new ApiAccount();
  private eBayAccount ebayAccount = new eBayAccount();
  private String eBayToken = "";
 //setting OAuthToken 02/17/2017 
  private String oAuthToken ="";

    /**
   * Constructor.
   */
  public ApiCredential() {
  }

  /**
   * Get the ApiAccount to use.
   * @return the ApiAccount instance.
   */
  public ApiAccount getApiAccount(){
    return this.apiAccount;
  }

  /**
   * Set the ApiAccount to use. If you set eBay token string, the ApiAccount
   * and eBayAccount properties will be ignored.
   * @param apiAccount the ApiAccount instance to be set to.
   */
  public void setApiAccount(ApiAccount apiAccount) {
    this.apiAccount = apiAccount;
  }

  /**
   * Get the eBay auction account to use.
   * @return the eBayAccount instance.
   */
  public eBayAccount geteBayAccount() {
    return this.ebayAccount;
  }

  /**
   * Set the eBay auction account to use. If you set eBay token string,
   * the ApiAccount and eBayAccount properties will be ignored.
   * @param ebayAccount the eBayAccount to be set to.
   */
  public void seteBayAccount(eBayAccount ebayAccount) {
    this.ebayAccount = ebayAccount;
  }

  /**
   * Get the eBay token string.
   * @return The eBay token string.
   */
  public String geteBayToken() {
    return this.eBayToken;
  }

  /**
   * Set the eBay token string.
   * If you set eBay token string, the OAuthToken, 
   * ApiAccount and eBayAccount properties will be ignored.
   * @param eBayToken The eBay token string to be set to.
   */
  public void seteBayToken(String eBayToken) {
    this.eBayToken = eBayToken;
  }
  
  /**
   * Get the eBay OAuthToken string.
   * @return The eBay OAuthToken string.
   */
  public String getOAuthToken() {
    return this.oAuthToken;
  }

  /**
   * Set the eBay OAuthToken string. 
   * If eBay OAuthToken string is set, the ApiAccount
   * and eBayAccount properties will be ignored.
   * @param oAuthToken The eBay token string to be set to.
   */
  public void setOAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
	
  }
}
