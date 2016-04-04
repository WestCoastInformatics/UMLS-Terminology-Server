package com.wci.umls.server;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;

/**
 * Generically represents a data file associated with a source provider.
 */
public interface SourceDataFile extends HasId, HasLastModified, HasName {

  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath();

  /**
   * Sets the path.
   *
   * @param path the new path
   */
  public void setPath(String path);

  /**
   * Gets the source data name.
   *
   * @return the source data name
   */
  public SourceData getSourceData();

  /**
   * Sets the source data name.
   *
   * @param sourceData the new source data
   */
  public void setSourceData(SourceData sourceData);

  /**
   * Gets the size.
   *
   * @return the size
   */
  public Long getSize();

  /**
   * Sets the size.
   *
   * @param size the new size
   */
  public void setSize(Long size);

  /**
   * Checks if is directory.
   *
   * @return true, if is directory
   */
  public boolean isDirectory();

  /**
   * Sets the directory.
   *
   * @param directory the new directory
   */
  public void setDirectory(boolean directory);

}
