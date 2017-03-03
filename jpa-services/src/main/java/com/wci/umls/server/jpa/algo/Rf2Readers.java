/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Container for RF2 readers.
 */
public class Rf2Readers {

  /** The sorted rf2 dir. */
  public File inputDir;

  /** The readers. */
  private Map<Keys, PushBackReader> readers = new HashMap<>();

  /**
   * The Enum Keys.
   */
  public enum Keys {

    /** The concept. */
    CONCEPT,

    /** The description. */
    DESCRIPTION,

    /** The definition. */
    DEFINITION,

    /** The relationship. */
    RELATIONSHIP,

    /** The association reference. */
    ASSOCIATION_REFERENCE,

    /** The attribute value. */
    ATTRIBUTE_VALUE,

    // /** The complex map. */
//    COMPLEX_MAP,

    /** The description type. */
    DESCRIPTION_TYPE,

    /** The extended map. */
    EXTENDED_MAP,

    /** The language. */
    LANGUAGE,

    /** The module dependency. */
    MODULE_DEPENDENCY,

    /** The refset descriptor. */
    REFSET_DESCRIPTOR,

    /** The simple. */
    SIMPLE,

    /** The simple map. */
    SIMPLE_MAP;

  }

  /**
   * Instantiates an empty {@link Rf2Readers}.
   *
   * @param inputDir the input dir
   * @throws Exception if anything goes wrong
   */
  public Rf2Readers(File inputDir) throws Exception {
    this.inputDir = inputDir;
  }

  /**
   * Open readers.
   *
   * @throws Exception the exception
   */
  public void openReaders() throws Exception {

    readers.put(Keys.CONCEPT, getReader("conceptsByConcept.sort"));
    readers.put(Keys.DESCRIPTION, getReader("descriptionsByConcept.sort"));
    readers.put(Keys.DEFINITION, getReader("definitionsByConcept.sort"));
    readers.put(Keys.RELATIONSHIP,
        getReader("relationshipsAllBySourceConcept.sort"));
    readers.put(Keys.ASSOCIATION_REFERENCE,
        getReader("associationReferenceRefsetsByRefCompId.sort"));
    readers.put(Keys.ATTRIBUTE_VALUE,
        getReader("attributeValueRefsetsByRefCompId.sort"));
//    readers.put(Keys.COMPLEX_MAP, getReader("complexMapRefsetsByConcept.sort"));
    readers.put(Keys.DESCRIPTION_TYPE,
        getReader("descriptionTypeByRefset.sort"));
    readers.put(Keys.EXTENDED_MAP,
        getReader("extendedMapRefsetsByConcept.sort"));
    readers.put(Keys.LANGUAGE, getReader("languageRefsetsByDescription.sort"));
    readers.put(Keys.MODULE_DEPENDENCY,
        getReader("moduleDependencyByRefset.sort"));
    readers.put(Keys.REFSET_DESCRIPTOR,
        getReader("refsetDescriptorByRefset.sort"));
    readers.put(Keys.SIMPLE, getReader("simpleRefsetsByConcept.sort"));
    readers.put(Keys.SIMPLE_MAP, getReader("simpleMapRefsetsByConcept.sort"));

  }

  /**
   * Close readers.
   *
   * @throws Exception the exception
   */
  public void closeReaders() throws Exception {
    for (final BufferedReader reader : readers.values()) {
      try {
        reader.close();
      } catch (Exception e) {
        // do nothing;
      }
    }
  }

  /**
   * Returns the reader.
   *
   * @param filename the filename
   * @return the reader
   * @throws Exception the exception
   */
  private PushBackReader getReader(String filename) throws Exception {
    File file = new File(inputDir, filename);
    if (file != null && file.exists()) {
      return new PushBackReader(new BufferedReader(
          new InputStreamReader(new FileInputStream(file), "UTF-8")));
    } else {
      // if no file, return an empty stream
      return new PushBackReader(new StringReader(""));
    }
  }

  /**
   * Returns the reader.
   *
   * @param key the key
   * @return the reader
   */
  public PushBackReader getReader(Keys key) {
    return readers.get(key);
  }

}
