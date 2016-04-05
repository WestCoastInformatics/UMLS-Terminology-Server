package com.wci.umls.server.rest.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * The Class AbstractSourceDataHandler.
 */
public class AbstractSourceDataHandler implements SourceDataHandler {

  /** Listeners. */
  protected List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  protected SourceData sourceData;

  @Override
  public void reset() throws Exception {
    throw new Exception("Reset method must be overriden by source data handler");
  }

  @Override
  public void compute() throws Exception {
    throw new Exception(
        "Compute method must be overriden by source data handler");
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setSourceData(SourceData sourceData) {
    this.sourceData = sourceData;
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    // n/a
  }

  @Override
  public void remove() throws Exception {

    // check prerequisites
    if (sourceData == null) {
      throw new Exception("Cannot remove terminology -- no source data set");
    }
    if (sourceData.getTerminology() == null) {
      throw new Exception(
          "Cannot remove terminology -- no terminology name set");
    }
    if (sourceData.getVersion() == null) {
      throw new Exception("Cannot remove terminology -- no version set");
    }

    //
    final Properties config = ConfigUtility.getConfigProperties();
    final SecurityServiceRest securityService = new SecurityServiceRestImpl();
    final ContentServiceRest contentService = new ContentServiceRestImpl();
    final SourceDataService sourceDataService = new SourceDataServiceJpa();
    final String adminAuthToken =
        securityService.authenticate(config.getProperty("admin.user"),
            config.getProperty("admin.password")).getAuthToken();

    // set status to removing
    sourceData.setStatus(SourceData.Status.REMOVING);
    sourceDataService.updateSourceData(sourceData);

    try {
      contentService.removeTerminology(sourceData.getTerminology(),
          sourceData.getVersion(), adminAuthToken);
      sourceData.setStatus(SourceData.Status.REMOVAL_COMPLETE);
      sourceDataService.updateSourceData(sourceData);
    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.REMOVAL_FAILED);
      sourceDataService.updateSourceData(sourceData);
    } finally {
      sourceDataService.close();
    }
  }

  @Override
  public String getName() {
    return null;
  }

}
