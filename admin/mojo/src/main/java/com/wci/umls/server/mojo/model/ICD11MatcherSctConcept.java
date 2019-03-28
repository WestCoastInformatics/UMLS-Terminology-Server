package com.wci.umls.server.mojo.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;

// TODO: Auto-generated Javadoc
/**
 * The Class SctSourceConcept.
 *
 * @author ${author}
 */
public class ICD11MatcherSctConcept {

  /** The concept id. */
  private String conceptId;

  /** The concept name. */
  private String name;

  /** The descs. */
  private Set<SctNeoplasmDescription> descs = new HashSet<>();

  /** The rels. */
  private Set<ICD11MatcherRelationship> rels = new HashSet<>();

  public ICD11MatcherSctConcept(String conId, String value) {
    this.conceptId = conId;
    this.name = value;
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  public String getConceptId() {
    return conceptId;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
  }

  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the concept id.
   *
   * @param name the concept name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the descs.
   *
   * @return the descs
   */
  public Set<SctNeoplasmDescription> getDescs() {
    return descs;
  }

  /**
   * Sets the descs.
   *
   * @param descs the descs
   */
  public void setDescs(Set<SctNeoplasmDescription> descs) {
    this.descs = descs;
  }

  /**
   * Returns the rels.
   *
   * @return the rels
   */
  public Set<ICD11MatcherRelationship> getRels() {
    return rels;
  }

  /**
   * Sets the rels.
   *
   * @param rels the rels
   */
  public void setRels(Set<ICD11MatcherRelationship> rels) {
    this.rels = rels;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((descs == null) ? 0 : descs.hashCode());
    result = prime * result + ((rels == null) ? 0 : rels.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    ICD11MatcherSctConcept other = (ICD11MatcherSctConcept) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (descs == null) {
      if (other.descs != null)
        return false;
    } else if (!descs.equals(other.descs))
      return false;
    if (rels == null) {
      if (other.rels != null)
        return false;
    } else if (!rels.equals(other.rels))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SctSourceConcept [conceptId=" + conceptId + ", name=" + name
        + ", descs=" + descs + ", rels=" + rels + "]";
  }

}
