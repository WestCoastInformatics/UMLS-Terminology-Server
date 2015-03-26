package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.helpers.HasAttributes;
import com.wci.umls.server.helpers.HasDefinitions;
import com.wci.umls.server.helpers.HasRelationships;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.TermType;

/**
 * Represents a single atomic unit of meaning. It's a name from a vocabulary
 * with associated identifiers.
 */
public interface Atom extends Component, HasAttributes, HasDefinitions,
    HasRelationships<AtomRelationship> {

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
   * Returns the string class.
   *
   * @return the string class
   */
  public StringClass getStringClass();

  /**
   * Sets the string class.
   *
   * @param stringClass the string class
   */
  public void setStringClass(StringClass stringClass);

  /**
   * Returns the lexical class.
   *
   * @return the lexical class
   */
  public LexicalClass getLexicalClass();

  /**
   * Sets the lexical class.
   *
   * @param lexicalClass the lexical class
   */
  public void setLexicalClass(LexicalClass lexicalClass);

  /**
   * Returns the code.
   *
   * @return the code
   */
  public Code getCode();

  /**
   * Sets the code.
   *
   * @param code the code
   */
  public void setCode(Code code);

  /**
   * Returns the descriptor.
   *
   * @return the descriptor
   */
  public Descriptor getDescriptor();

  /**
   * Sets the descriptor.
   *
   * @param descriptor the descriptor
   */
  public void setDescriptor(Descriptor descriptor);

  /**
   * Returns the concepts.
   *
   * @return the concepts
   */
  public List<Concept> getConcepts();

  /**
   * Sets the concepts.
   *
   * @param concepts the concepts
   */
  public void setConcepts(List<Concept> concepts);

  /**
   * Adds the concept.
   *
   * @param concept the concept
   */
  public void addConcept(Concept concept);

  /**
   * Removes the concept.
   *
   * @param concept the concept
   */
  public void removeConcept(Concept concept);

  /**
   * Returns the language.
   *
   * @return the language
   */
  public Language getLanguage();

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(Language language);

  /**
   * Returns the term type.
   *
   * @return the term type
   */
  public TermType getTermType();

  /**
   * Sets the term type.
   *
   * @param termType the term type
   */
  public void setTermType(TermType termType);

}