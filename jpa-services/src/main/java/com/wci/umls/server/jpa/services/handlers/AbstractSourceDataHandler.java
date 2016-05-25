package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.jpa.algo.RemoveTerminologyAlgorithm;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Abstract implementation of SourceDataHandler.
 */
public abstract class AbstractSourceDataHandler implements SourceDataHandler {

  /** Listeners. */
  protected List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  protected SourceData sourceData;

  @Override
  public abstract void reset() throws Exception;

  @Override
  public abstract void compute() throws Exception;

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
  public void cancel() throws Exception {

    if (sourceData == null || sourceData.getId() == null) {
      throw new Exception(
          "Cannot cancel: source data not specified, or its id is null");
    }

    // get the algorithm running for this source data if it exists
    SourceDataService sourceDataService = new SourceDataServiceJpa();
    try {
      Algorithm algo =
          sourceDataService.getRunningProcessForId(sourceData.getId());
      algo.cancel();
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(
          "Error attempting to cancel process for source data "
              + sourceData.getName());
      throw new Exception(e);
    } finally {
      sourceDataService.close();
    }
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

    final SourceDataService sourceDataService = new SourceDataServiceJpa();

    // set status to removing
    sourceData.setStatus(SourceData.Status.REMOVING);
    sourceDataService.updateSourceData(sourceData);

    // instantiate and set algorithm parameters
    RemoveTerminologyAlgorithm algo = new RemoveTerminologyAlgorithm();
    algo.setTerminology(sourceData.getTerminology());
    algo.setVersion(sourceData.getVersion());
    sourceDataService.registerSourceDataAlgorithm(sourceData.getId(), algo);

    try {
      algo.compute();
      sourceData.setStatus(SourceData.Status.REMOVAL_COMPLETE);
    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.REMOVAL_FAILED);
      throw new Exception(e);
    } finally {
      sourceDataService.updateSourceData(sourceData);
      sourceDataService.unregisterSourceDataAlgorithm(sourceData.getId());
      sourceDataService.close();

    }
  }

  @Override
  public abstract String getName();

  @Override
  public abstract boolean checkPreconditions() throws Exception;

}
