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
import com.wci.umls.server.model.content.LexicalClass;

/**
 * The Class LexicalClassNoteJpa.
 */
@Entity
@Table(name = "lexical_class_notes")
@Audited
@XmlRootElement(name = "notes")
public class LexicalClassNoteJpa extends AbstractNote implements Note {

  
  /** The lexicalClass. */
  @ManyToOne(targetEntity = LexicalClassJpa.class, optional = false)
  private LexicalClass lexicalClass = null;
  
  /**
   * Instantiates a new lexicalClass note jpa.
   */
  public LexicalClassNoteJpa() {
    
  }
  
  /**
   * Instantiates a new lexicalClass note jpa.
   *
   * @param note the note
   */
  public LexicalClassNoteJpa(LexicalClassNoteJpa note) {
    super(note);
    this.lexicalClass = note.getLexicalClass();
  }
  
  /**
   * Gets the lexicalClass.
   *
   * @return the lexicalClass
   */
  public LexicalClass getLexicalClass() {
    return this.lexicalClass;
  }
  
  /**
   * Sets the lexicalClass.
   *
   * @param lexicalClass the new lexicalClass
   */
  public void setLexicalClass(LexicalClass lexicalClass) {
    this.lexicalClass = lexicalClass;
  }
  
  /**
   * Returns the lexicalClass id.
   *
   * @return the lexicalClass id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getLexicalClassId() {
    return (lexicalClass != null) ? lexicalClass.getId() : 0;
  }

  /**
   * Sets the lexicalClass id.
   *
   * @param lexicalClassId the lexicalClass id
   */
  @SuppressWarnings("unused")
  private void setLexicalClassId(Long lexicalClassId) {
    if (lexicalClass == null) {
      lexicalClass = new LexicalClassJpa();
    }
    lexicalClass.setId(lexicalClassId);
  }
}
