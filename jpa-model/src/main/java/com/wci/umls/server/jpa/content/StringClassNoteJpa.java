package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.model.content.StringClass;

/**
 * The Class StringClassNoteJpa.
 */
@Entity
@Table(name = "string_class_notes")
@Audited
@XmlRootElement(name = "notes")
public class StringClassNoteJpa extends AbstractNote implements Note {

  
  /** The stringClass. */
  @ManyToOne(targetEntity = StringClassJpa.class, optional = false)
  private StringClass stringClass = null;
  
  /**
   * Instantiates a new stringClass note jpa.
   */
  public StringClassNoteJpa() {
    
  }
  
  /**
   * Instantiates a new stringClass note jpa.
   *
   * @param note the note
   */
  public StringClassNoteJpa(StringClassNoteJpa note) {
    super(note);
    this.stringClass = note.getStringClass();
  }
  
  /**
   * Gets the stringClass.
   *
   * @return the stringClass
   */
  public StringClass getStringClass() {
    return this.stringClass;
  }
  
  /**
   * Sets the stringClass.
   *
   * @param stringClass the new stringClass
   */
  public void setStringClass(StringClass stringClass) {
    this.stringClass = stringClass;
  }
  
  /**
   * Returns the stringClass id.
   *
   * @return the stringClass id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getStringClassId() {
    return (stringClass != null) ? stringClass.getId() : 0;
  }

  /**
   * Sets the stringClass id.
   *
   * @param stringClassId the stringClass id
   */
  @SuppressWarnings("unused")
  private void setStringClassId(Long stringClassId) {
    if (stringClass == null) {
      stringClass = new StringClassJpa();
    }
    stringClass.setId(stringClassId);
  }
}
