package com.wci.umls.server.model.meta;

/**
 * Represents a structured citation.
 */
public interface SourceCitation {

  /**
   * Returns the address.
   * 
   * @return the address
   */
  public String getAddress();

  /**
   * Sets the address.
   * 
   * @param address the address
   */
  public void setAddress(String address);

  /**
   * Returns the author.
   * 
   * @return the author
   */
  public String getAuthor();

  /**
   * Sets the author.
   * 
   * @param author the author
   */
  public void setAuthor(String author);

  /**
   * Returns the content designator.
   * 
   * @return the content designator
   */
  public String getContentDesignator();

  /**
   * Sets the content designator.
   * 
   * @param contentDesignator the content designator
   */
  public void setContentDesignator(String contentDesignator);

  /**
   * Returns the date of publication.
   * 
   * @return the date of publication
   */
  public String getDateOfPublication();

  /**
   * Sets the date of publication.
   * 
   * @param dateOfPublication the date of publication
   */
  public void setDateOfPublication(String dateOfPublication);

  /**
   * Returns the date of revision.
   * 
   * @return the date of revision
   */
  public String getDateOfRevision();

  /**
   * Sets the date of revision.
   * 
   * @param dateOfRevision the date of revision
   */
  public void setDateOfRevision(String dateOfRevision);

  /**
   * Returns the edition.
   * 
   * @return the edition
   */
  public String getEdition();

  /**
   * Sets the edition.
   * 
   * @param edition the edition
   */
  public void setEdition(String edition);

  /**
   * Returns the editor.
   * 
   * @return the editor
   */
  public String getEditor();

  /**
   * Sets the editor.
   * 
   * @param editor the editor
   */
  public void setEditor(String editor);

  /**
   * Returns the location.
   * 
   * @return the location
   */
  public String getLocation();

  /**
   * Sets the location.
   * 
   * @param location the location
   */
  public void setLocation(String location);

  /**
   * Returns the medium designator.
   * 
   * @return the medium designator
   */
  public String getMediumDesignator();

  /**
   * Sets the medium designator.
   * 
   * @param mediumDesignator the medium designator
   */
  public void setMediumDesignator(String mediumDesignator);

  /**
   * Returns the organization.
   * 
   * @return the organization
   */
  public String getOrganization();

  /**
   * Sets the organization.
   * 
   * @param organization the organization
   */
  public void setOrganization(String organization);

  /**
   * Returns the place of publication.
   * 
   * @return the place of publication
   */
  public String getPlaceOfPublication();

  /**
   * Sets the place of publication.
   * 
   * @param placeOfPublication the place of publication
   */
  public void setPlaceOfPublication(String placeOfPublication);

  /**
   * Returns the publisher.
   * 
   * @return the publisher
   */
  public String getPublisher();

  /**
   * Sets the publisher.
   * 
   * @param publisher the publisher
   */
  public void setPublisher(String publisher);

  /**
   * Returns the title.
   * 
   * @return the title
   */
  public String getTitle();

  /**
   * Sets the title.
   * 
   * @param title the title
   */
  public void setTitle(String title);

  /**
   * Returns the source.
   * 
   * @return the source
   */
  public Terminology getSource();

  /**
   * Sets the source.
   * 
   * @param source the source
   */
  public void setSource(Terminology source);

  /**
   * Returns the extent.
   * 
   * @return the extent
   */
  public String getExtent();

  /**
   * Sets the extent.
   * 
   * @param extent the extent
   */
  public void setExtent(String extent);

  /**
   * Returns the series.
   * 
   * @return the series
   */
  public String getSeries();

  /**
   * Sets the series.
   * 
   * @param series the series
   */
  public void setSeries(String series);

  /**
   * Returns the availability statement.
   * 
   * @return the availability statement
   */
  public String getAvailabilityStatement();

  /**
   * Sets the availability statement.
   * 
   * @param availabilityStatement the availability statement
   */
  public void setAvailabilityStatement(String availabilityStatement);

  /**
   * Returns the notes.
   * 
   * @return the notes
   */
  public String getNotes();

  /**
   * Sets the notes.
   * 
   * @param notes the notes
   */
  public void setNotes(String notes);

  /**
   * Returns the value. Used for legacy data where citation information fields
   * are not normalized or structured.
   * 
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value. Used for legacy data where citation information fields are
   * not normalized or structured.
   * 
   * @param value the value
   */
  public void setValue(String value);

}