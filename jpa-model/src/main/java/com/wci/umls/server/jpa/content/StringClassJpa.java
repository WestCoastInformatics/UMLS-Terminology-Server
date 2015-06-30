/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.content.StringClass;

/**
 * JPA-enabled implementation of {@link StringClass}.
 */
@Entity
@Table(name = "string_classes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "stringClass")
@Indexed
public class StringClassJpa extends AbstractAtomClass implements StringClass {

  /** The marker sets. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(sFetchMode.JOIN)
  @CollectionTable(name = "lexical_class_marker_sets")
  @Column(nullable = true)
  List<String> markerSets;

  /**
   * Instantiates an empty {@link StringClassJpa}.
   */
  public StringClassJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link StringClassJpa} from the specified parameters.
   *
   * @param stringClass the string class
   * @param deepCopy the deep copy
   */
  public StringClassJpa(StringClass stringClass, boolean deepCopy) {
    super(stringClass, deepCopy);
    markerSets = stringClass.getMarkerSets();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasMarkerSets#getMarkerSets()
   */
  @Override
  public List<String> getMarkerSets() {
    return markerSets;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasMarkerSets#setMarkerSets(java.util.List)
   */
  @Override
  public void setMarkerSets(List<String> markerSets) {
    this.markerSets = markerSets;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasMarkerSets#addMarkerSet(java.lang.String)
   */
  @Override
  public void addMarkerSet(String markerSet) {
    if (markerSets == null) {
      markerSets = new ArrayList<String>();
    }
    markerSets.add(markerSet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasMarkerSets#removeMarkerSet(java.lang.String)
   */
  @Override
  public void removeMarkerSet(String markerSet) {
    if (markerSets == null) {
      markerSets = new ArrayList<String>();
    }
    markerSets.remove(markerSet);

  }
}
