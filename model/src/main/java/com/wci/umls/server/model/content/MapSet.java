/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * MapSet: MapSet.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;

import java.util.List;

import javax.xml.transform.Source;

import com.wci.umls.server.model.meta.Terminology;

/**
 * Represents a group of {@link Mapping}s between one {@link Terminology} and
 * another.
 */
public interface MapSet extends ComponentHasAttributes {

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   * 
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the "to" source.
   * 
   * @return the "to" source
   */
  public Source getToSource();

  /**
   * Sets the "to" source.
   * 
   * @param toSource the "to" source
   */
  public void setToSource(Source toSource);

  /**
   * Returns the "to" root source. This is needed for map sets that do not have
   * a versioned "to" source.
   * 
   * @return the "to" root source
   */
  public String getToRootSource();

  /**
   * Sets the "to" root source.
   * 
   * @param toRootSource the "to" root source
   */
  public void setToRootSource(String toRootSource);

  /**
   * Returns the "from" source.
   * 
   * @return the "from" source
   */
  public Source getFromSource();

  /**
   * Sets the "from" source.
   * 
   * @param fromSource the "from" source
   */
  public void setFromSource(Source fromSource);

  /**
   * Returns the "from" root source. This is needed for map sets that do not
   * have a versioned "from" source.
   * 
   * @return the "from" root source
   */
  public String getFromRootSource();

  /**
   * Sets the "from" root source.
   * 
   * @param fromRootSource the "from" root source
   */
  public void setFromRootSource(String fromRootSource);

  /**
   * Returns the complexity.
   * 
   * @return the complexity
   */
  public String getComplexity();

  /**
   * Sets the complexity.
   * 
   * @param complexity the complexity
   */
  public void setComplexity(String complexity);

  /**
   * Returns the from complexity.
   * 
   * @return the from complexity
   */
  public String getFromComplexity();

  /**
   * Sets the from complexity.
   * 
   * @param fromComplexity the from complexity
   */
  public void setFromComplexity(String fromComplexity);

  /**
   * Returns the to complexity.
   * 
   * @return the to complexity
   */
  public String getToComplexity();

  /**
   * Sets the to complexity.
   * 
   * @param toComplexity the to complexity
   */
  public void setToComplexity(String toComplexity);

  /**
   * Returns the to exhaustive.
   * 
   * @return the to exhaustive
   */
  public String getToExhaustive();

  /**
   * Returns the from exhaustive.
   * 
   * @return the from exhaustive
   */
  public String getFromExhaustive();

  /**
   * Sets the to exhaustive.
   * 
   * @param toExhaustive the to exhaustive
   */
  public void setToExhaustive(String toExhaustive);

  /**
   * Sets the from exhaustive.
   * 
   * @param fromExhaustive the from exhaustive
   */
  public void setFromExhaustive(String fromExhaustive);

  /**
   * Returns the separator code.
   * 
   * @return the separator code
   */
  public String getSeparatorCode();

  /**
   * Sets the separator code.
   * 
   * @param separatorCode the separator code
   */
  public void setSeparatorCode(String separatorCode);

  /**
   * Returns the umls separator.
   * 
   * @return the umls separator
   */
  public String getUmlsSeparator();

  /**
   * Sets the umls separator.
   * 
   * @param umlsSeparator the umls separator
   */
  public void setUmlsSeparator(String umlsSeparator);

  /**
   * Returns the {@link MapObject} used to represent a mapping to nothing. Only
   * certain sources make use of this construct.
   * 
   * @return the {@link MapObject} used to represent a mapping to nothing
   */
  public MapObject getMapObjectForNullMapping();

  /**
   * Sets the type.
   * 
   * @param type the type
   */
  public void setType(String type);

  /**
   * Returns the type.
   * 
   * @return the type
   */
  public String getType();

  /**
   * Sets the {@link MapObject} used to represent a mapping to nothing.
   * 
   * @param toNullObject the {@link MapObject} used to represent a mapping to
   *          nothing
   */
  public void setMapObjectForNullMapping(MapObject toNullObject);

  /**
   * Returns the mappings.
   * 
   * @return the mappings
   */
  public List<Mapping> getMappings();

  /**
   * Sets the mappings.
   * 
   * @param mappings the mappings
   */
  public void setMappings(List<Mapping> mappings);

  /**
   * Adds the mapping.
   * @param mapping the mapping
   */
  public void addMapping(Mapping mapping);

  /**
   * Removes the mapping.
   * @param mapping the mapping
   */
  public void removeMapping(Mapping mapping);
}
