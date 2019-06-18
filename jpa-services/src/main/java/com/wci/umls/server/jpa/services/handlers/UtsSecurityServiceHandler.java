/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.SecurityServiceHandler;

/**
 * Implements a security handler that authorizes via UTS authentication.
 */
public class UtsSecurityServiceHandler extends AbstractConfigurable
    implements SecurityServiceHandler {

  /** The properties. */
  private Properties properties;

  /**
   * Instantiates an empty {@link UtsSecurityServiceHandler}.
   */
  public UtsSecurityServiceHandler() {
    // do nothing
  }

  /* see superclass */
  @Override
  public User authenticate(String username, String password) throws Exception {

    final String licenseCode = properties.getProperty("license.code");
    if (licenseCode == null) {
      throw new Exception("License code must be specified.");
    }
    String data = URLEncoder.encode("licenseCode", "UTF-8") + "="
        + URLEncoder.encode(licenseCode, "UTF-8");
    data += "&" + URLEncoder.encode("user", "UTF-8") + "="
        + URLEncoder.encode(username, "UTF-8");
    data += "&" + URLEncoder.encode("password", "UTF-8") + "="
        + URLEncoder.encode(password, "UTF-8");

    final String urlProp = properties.getProperty("url");
    if (urlProp == null) {
      throw new Exception("URL must be specified.");
    }

    URL url = new URL(urlProp);
    URLConnection conn = url.openConnection();
    conn.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write(data);
    wr.flush();

    BufferedReader rd =
        new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    boolean authenticated = false;
    while ((line = rd.readLine()) != null) {
      if (line.toLowerCase().contains("true")) {
        authenticated = true;
      }
    }
    wr.close();
    rd.close();

    if (!authenticated) {
      throw new LocalException("Username or password invalid.");
    }

    /*
     * Synchronize the information sent back from ITHSDO with the User object.
     * Add a new user if there isn't one matching the username If there is, load
     * and update that user and save the changes
     */
    User returnUser;
    try( SecurityService securityService = new SecurityServiceJpa();)
    {
    	returnUser = securityService.getUser(username);
    }

    if (returnUser == null)
    {
    	returnUser = new UserJpa();
      final String authUserName = username;
      final String authEmail = "test@example.com";
      final String authGivenName = "UTS User - " + username;
      final String authSurname = "";
            
    	returnUser.setName(authGivenName + " " + authSurname);
    	returnUser.setEmail(authEmail);
    	returnUser.setUserName(authUserName);
    	returnUser.setApiUsageCount(0l);
    	returnUser.setUserToken(null);
    	returnUser.setLoginCount(null);
    	returnUser.setApplicationRole(UserRole.VIEWER);
    }
    return returnUser;
  }

  /* see superclass */
  @Override
  public boolean timeoutUser(String user) {
    return true;
  }

  /* see superclass */
  @Override
  public String computeTokenForUser(String user) {
    String token = UUID.randomUUID().toString();
    return token;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "UTS Security Service Handler";
  }

}
