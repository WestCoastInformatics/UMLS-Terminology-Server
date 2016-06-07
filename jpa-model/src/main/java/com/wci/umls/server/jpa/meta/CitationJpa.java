/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.model.meta.Citation;

/**
 * JPA and JAXB enabled implementation of {@link Citation}.
 */
@Entity
@Table(name = "citations")
@Audited
@XmlRootElement(name = "citation")
public class CitationJpa implements Citation {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The address. */
  @Column(nullable = true, length = 1000)
  private String address;

  /** The author. */
  @Column(nullable = true, length = 1000)
  private String author;

  /** The availability statement. */
  @Column(nullable = true, length = 4000)
  private String availabilityStatement;

  /** The content designator. */
  @Column(nullable = true)
  private String contentDesignator;

  /** The date of publication. */
  @Column(nullable = true)
  private String dateOfPublication = null;

  /** The date of revision. */
  @Column(nullable = true)
  private String dateOfRevision = null;

  /** The edition. */
  @Column(nullable = true)
  private String edition;

  /** The editor. */
  @Column(nullable = true)
  private String editor;

  /** The extent. */
  @Column(nullable = true)
  private String extent;

  /** The location. */
  @Column(nullable = true)
  private String location;

  /** The medium designator. */
  @Column(nullable = true)
  private String mediumDesignator;

  /** The notes. */
  @Column(nullable = true, length = 4000)
  private String notes;

  /** The organization. */
  @Column(nullable = true)
  private String organization;

  /** The place of publication. */
  @Column(nullable = true)
  private String placeOfPublication;

  /** The publisher. */
  @Column(nullable = true)
  private String publisher;

  /** The Series. */
  @Column(nullable = true)
  private String series;

  /** The title. */
  @Column(nullable = true)
  private String title;

  /** The unstructured value. */
  @Column(nullable = true, length = 4000)
  private String unstructuredValue;

  /**
   * Instantiates an empty {@link CitationJpa}.
   */
  public CitationJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CitationJpa} from the specified parameters.
   *
   * @param citation the citation
   */
  public CitationJpa(Citation citation) {
    address = citation.getAddress();
    author = citation.getAuthor();
    availabilityStatement = citation.getAvailabilityStatement();
    contentDesignator = citation.getContentDesignator();
    dateOfPublication = citation.getDateOfPublication();
    dateOfRevision = citation.getDateOfRevision();
    edition = citation.getEdition();
    editor = citation.getEditor();
    extent = citation.getExtent();
    id = citation.getId();
    location = citation.getLocation();
    mediumDesignator = citation.getMediumDesignator();
    notes = citation.getNotes();
    organization = citation.getOrganization();
    placeOfPublication = citation.getPlaceOfPublication();
    publisher = citation.getPublisher();
    series = citation.getSeries();
    title = citation.getTitle();
    unstructuredValue = citation.getUnstructuredValue();
  }

  /**
   * Instantiates a {@link CitationJpa} from the specified parameters.
   *
   * @param scitline the mrsab line
   */
  public CitationJpa(String scitline) {
    // 0 Author name(s),
    // 1 Personal author address,
    // 2 Organization author(s),
    // 3 Title,
    // 4 Content Designator,
    // 5 Medium Designator,
    // 6 Edition,
    // 7 Place of Pub.,
    // 8 Publisher,
    // 9 Date of pub. or copyright,
    // 10 Date of revision,
    // 11 Location,
    // 12 Extent,
    // 13 Series,
    // 14 Avail. Statement (URL),
    // 15 Language,
    // 16 Notes.
    String[] fields = FieldedStringTokenizer.split(scitline, ";");
    if (fields.length == 0) {
      return;
    }

    // Handle legacy data
    if (fields.length < 17) {
      this.unstructuredValue = scitline;
    } else {
      author = fields[0];
      address = fields[1];
      this.organization = fields[2];
      this.title = fields[3];
      this.contentDesignator = fields[4];
      this.mediumDesignator = fields[5];
      this.edition = fields[6];
      this.placeOfPublication = fields[7];
      this.publisher = fields[8];
      this.dateOfPublication = fields[9];
      this.dateOfRevision = fields[10];
      this.location = fields[11];
      this.extent = fields[12];
      this.series = fields[13];
      this.availabilityStatement = fields[14];
      // no language
      this.notes = fields[16];
    }
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public String getAddress() {
    return address;
  }

  /* see superclass */
  @Override
  public void setAddress(String address) {
    this.address = address;
  }

  /* see superclass */
  @Override
  public String getAuthor() {
    return author;
  }

  /* see superclass */
  @Override
  public void setAuthor(String author) {
    this.author = author;
  }

  /* see superclass */
  @Override
  public String getAvailabilityStatement() {
    return availabilityStatement;
  }

  /* see superclass */
  @Override
  public void setAvailabilityStatement(String availabilityStatement) {
    this.availabilityStatement = availabilityStatement;
  }

  /* see superclass */
  @Override
  public String getContentDesignator() {
    return contentDesignator;
  }

  /* see superclass */
  @Override
  public void setContentDesignator(String contentDesignator) {
    this.contentDesignator = contentDesignator;
  }

  /* see superclass */
  @Override
  public String getDateOfPublication() {
    return dateOfPublication;
  }

  /* see superclass */
  @Override
  public void setDateOfPublication(String dateOfPublication) {
    this.dateOfPublication = dateOfPublication;
  }

  /* see superclass */
  @Override
  public String getDateOfRevision() {
    return dateOfRevision;
  }

  /* see superclass */
  @Override
  public void setDateOfRevision(String dateOfRevision) {
    this.dateOfRevision = dateOfRevision;
  }

  /* see superclass */
  @Override
  public String getEdition() {
    return edition;
  }

  /* see superclass */
  @Override
  public void setEdition(String edition) {
    this.edition = edition;
  }

  /* see superclass */
  @Override
  public String getEditor() {
    return editor;
  }

  /* see superclass */
  @Override
  public void setEditor(String editor) {
    this.editor = editor;
  }

  /* see superclass */
  @Override
  public String getExtent() {
    return extent;
  }

  /* see superclass */
  @Override
  public void setExtent(String extent) {
    this.extent = extent;
  }

  /* see superclass */
  @Override
  public String getLocation() {
    return location;
  }

  /* see superclass */
  @Override
  public void setLocation(String location) {
    this.location = location;
  }

  /* see superclass */
  @Override
  public String getMediumDesignator() {
    return mediumDesignator;
  }

  /* see superclass */
  @Override
  public void setMediumDesignator(String mediumDesignator) {
    this.mediumDesignator = mediumDesignator;
  }

  /* see superclass */
  @Override
  public String getNotes() {
    return notes;
  }

  /* see superclass */
  @Override
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /* see superclass */
  @Override
  public String getOrganization() {
    return organization;
  }

  /* see superclass */
  @Override
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /* see superclass */
  @Override
  public String getPlaceOfPublication() {
    return placeOfPublication;
  }

  /* see superclass */
  @Override
  public void setPlaceOfPublication(String placeOfPublication) {
    this.placeOfPublication = placeOfPublication;
  }

  /* see superclass */
  @Override
  public String getPublisher() {
    return publisher;
  }

  /* see superclass */
  @Override
  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  /* see superclass */
  @Override
  public String getSeries() {
    return series;
  }

  /* see superclass */
  @Override
  public void setSeries(String series) {
    this.series = series;
  }

  /* see superclass */
  @Override
  public String getTitle() {
    return title;
  }

  /* see superclass */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  /* see superclass */
  @Override
  public String getUnstructuredValue() {
    return unstructuredValue;
  }

  /* see superclass */
  @Override
  public void setUnstructuredValue(String unstructuredValue) {
    this.unstructuredValue = unstructuredValue;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((author == null) ? 0 : author.hashCode());
    result =
        prime
            * result
            + ((availabilityStatement == null) ? 0 : availabilityStatement
                .hashCode());
    result =
        prime * result
            + ((contentDesignator == null) ? 0 : contentDesignator.hashCode());
    result =
        prime * result
            + ((dateOfPublication == null) ? 0 : dateOfPublication.hashCode());
    result =
        prime * result
            + ((dateOfRevision == null) ? 0 : dateOfRevision.hashCode());
    result = prime * result + ((edition == null) ? 0 : edition.hashCode());
    result = prime * result + ((editor == null) ? 0 : editor.hashCode());
    result = prime * result + ((extent == null) ? 0 : extent.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result =
        prime * result
            + ((mediumDesignator == null) ? 0 : mediumDesignator.hashCode());
    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
    result =
        prime * result + ((organization == null) ? 0 : organization.hashCode());
    result =
        prime
            * result
            + ((placeOfPublication == null) ? 0 : placeOfPublication.hashCode());
    result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
    result = prime * result + ((series == null) ? 0 : series.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result =
        prime * result
            + ((unstructuredValue == null) ? 0 : unstructuredValue.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CitationJpa other = (CitationJpa) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (author == null) {
      if (other.author != null)
        return false;
    } else if (!author.equals(other.author))
      return false;
    if (availabilityStatement == null) {
      if (other.availabilityStatement != null)
        return false;
    } else if (!availabilityStatement.equals(other.availabilityStatement))
      return false;
    if (contentDesignator == null) {
      if (other.contentDesignator != null)
        return false;
    } else if (!contentDesignator.equals(other.contentDesignator))
      return false;
    if (dateOfPublication == null) {
      if (other.dateOfPublication != null)
        return false;
    } else if (!dateOfPublication.equals(other.dateOfPublication))
      return false;
    if (dateOfRevision == null) {
      if (other.dateOfRevision != null)
        return false;
    } else if (!dateOfRevision.equals(other.dateOfRevision))
      return false;
    if (edition == null) {
      if (other.edition != null)
        return false;
    } else if (!edition.equals(other.edition))
      return false;
    if (editor == null) {
      if (other.editor != null)
        return false;
    } else if (!editor.equals(other.editor))
      return false;
    if (extent == null) {
      if (other.extent != null)
        return false;
    } else if (!extent.equals(other.extent))
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    if (mediumDesignator == null) {
      if (other.mediumDesignator != null)
        return false;
    } else if (!mediumDesignator.equals(other.mediumDesignator))
      return false;
    if (notes == null) {
      if (other.notes != null)
        return false;
    } else if (!notes.equals(other.notes))
      return false;
    if (organization == null) {
      if (other.organization != null)
        return false;
    } else if (!organization.equals(other.organization))
      return false;
    if (placeOfPublication == null) {
      if (other.placeOfPublication != null)
        return false;
    } else if (!placeOfPublication.equals(other.placeOfPublication))
      return false;
    if (publisher == null) {
      if (other.publisher != null)
        return false;
    } else if (!publisher.equals(other.publisher))
      return false;
    if (series == null) {
      if (other.series != null)
        return false;
    } else if (!series.equals(other.series))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (unstructuredValue == null) {
      if (other.unstructuredValue != null)
        return false;
    } else if (!unstructuredValue.equals(other.unstructuredValue))
      return false;
    return true;
  }
}
