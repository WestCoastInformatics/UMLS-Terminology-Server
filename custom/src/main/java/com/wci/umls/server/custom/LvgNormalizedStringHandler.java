/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.custom;

import java.util.Hashtable;
import java.util.Properties;

import com.wci.umls.server.services.handlers.NormalizedStringHandler;

import gov.nih.nlm.nls.lvg.Api.LuiNormApi;

/**
 * Implementation of string normalization based on NLM's LVG. Requires a local
 * LVG installation and the "lvg.dir" property to be defined.
 * 
 * Presently, this also requires the lvg2014dist.jar to be locally installed in
 * an src/main/resources directory (which means it may not run properly on the
 * server). What we *really* want is a maven reference to this artifact.
 */
public class LvgNormalizedStringHandler implements NormalizedStringHandler {

  /** The lvg dir. */
  private String LVG_DIR;

  /** The api. */
  private LuiNormApi api;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("lvg.dir") == null) {
      throw new Exception("Required property lvg.dir is missing");
    }
    LVG_DIR = p.getProperty("lvg.dir");
    Hashtable<String, String> properties = new Hashtable<String, String>();
    properties.put(gov.nih.nlm.nls.lvg.Lib.Configuration.LVG_DIR,
        LVG_DIR + "/");
    // Use default config
    api = new LuiNormApi(LVG_DIR + "/data/config/lvg.properties", properties);
  }

  /* see superclass */
  @Override
  public String getNormalizedString(String string) throws Exception {
    return api.Mutate(string);
  }

  /* see superclass */
  @Override
  public String getName() {
    return "LVG Normalized String Handler";
  }

}
