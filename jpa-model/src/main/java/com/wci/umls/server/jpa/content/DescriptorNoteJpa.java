package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Descriptor;

/**
 * The Class DescriptorNoteJpa.
 */
@Entity
@Table(name = "descriptor_notes")
@Audited
@Indexed
@XmlRootElement(name = "note")
public class DescriptorNoteJpa extends AbstractNote {

  /** The descriptor. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  private Descriptor descriptor = null;

  /**
   * Instantiates a new descriptor note jpa.
   */
  public DescriptorNoteJpa() {

  }

  /**
   * Instantiates a new descriptor note jpa.
   *
   * @param note the note
   */
  public DescriptorNoteJpa(DescriptorNoteJpa note) {
    super(note);
    this.descriptor = note.getDescriptor();
  }

  /**
   * Gets the descriptor.
   *
   * @return the descriptor
   */
  @XmlTransient
  public Descriptor getDescriptor() {
    return this.descriptor;
  }

  /**
   * Sets the descriptor.
   *
   * @param descriptor the new descriptor
   */
  public void setDescriptor(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  /**
   * Returns the descriptor id.
   *
   * @return the descriptor id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getDescriptorId() {
    return (descriptor != null) ? descriptor.getId() : 0;
  }
  

  /**
   * Returns the descriptor name.
   *
   * @return the descriptor name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getDescriptorName() {
    return (descriptor != null) ? descriptor.getName() : "";
  }
  
  /**
   * Returns the descriptor name.
   *
   * @return the descriptor name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getDescriptorTerminologyId() {
    return (descriptor != null) ? descriptor.getTerminologyId() : "";
  }

  /**
   * Sets the descriptor id.
   *
   * @param descriptorId the descriptor id
   */
  @SuppressWarnings("unused")
  private void setDescriptorId(Long descriptorId) {
    if (descriptor == null) {
      descriptor = new DescriptorJpa();
    }
    descriptor.setId(descriptorId);
  }
}
