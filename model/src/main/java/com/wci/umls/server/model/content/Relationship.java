/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;

// TODO: Auto-generated Javadoc
/**
 * Represents a relationship between two {@link Component}s.
 *
 * @param <S> the "from" object type
 * @param <T> the "to" object type
 */
public interface Relationship<S extends Component, T extends Component> extends
    Component {

  /**
   * Returns the from.
   *
   * @return the from
   */
  public S getFrom();

  /**
   * Sets the from.
   *
   * @param component the from
   */
  public void setFrom(S component);

  /**
   * Returns the to.
   *
   * @return the to
   */
  public T getTo();

  /**
   * Sets the to.
   *
   * @param component the to
   */
  public void setTo(T component);

  /**
   * Returns the relationship label.
   *
   * @return the relationship label
   */
  public RelationshipType getRelationshipLabel();

  /**
   * Sets the relationship label.
   *
   * @param relationshipLabel the relationship label
   */
  public void setRelationshipLabel(RelationshipType relationshipLabel);

  /**
   * Returns the additional relationship label.
   *
   * @return the additional relationship label
   */
  public AdditionalRelationshipType getAdditionalRelationshipLabel();

  /**
   * Sets the additional relationship label.
   *
   * @param additionalRelationshipLabel the additional relationship label
   */
  public void setAdditionalRelationshipLabel(
    AdditionalRelationshipType additionalRelationshipLabel);

  /**
   * Returns the relationship group. This is a mechanism for binding sets of
   * relationships together to either simulate one level of anonymous
   * subclassing or for other purposes.
   * 
   * @return the relationship group
   */
  public String getGroup();

  /**
   * Sets the relationship group.
   * 
   * @param group the relationship group
   */
  public void setGroup(String group);

  /**
   * Indicates whether or not this is the relation direction asserted by the
   * terminology.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAssertedDirection();

  /**
   * Sets the asserted direction flag.
   * 
   * @param assertedDirection the asserted direction flag
   */
  public void setAssertedDirection(boolean assertedDirection);

  /**
   * Indicates whether the relationship is inferred.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInferred();

  /**
   * Sets the inferred flag.
   *
   * @param inferred the inferred flag
   */
  public void setInferred(boolean inferred);

  /**
   * Indicates whether the relationship is stated.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStated();

  /**
   * Sets the stated flag.
   *
   * @param stated the stated flag
   */
  public void setStated(boolean stated);

}