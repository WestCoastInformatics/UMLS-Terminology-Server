/*
 *    Copyright 2015 West Coast Informatics, LLC
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
public class RrfReaders {

  /** The sorted rf2 dir. */
  public File inputDir;

  /** The readers. */
  private Map<Keys, PushBackReader> readers = new HashMap<>();

  /**
   * The Enum Keys.
   */
  public enum Keys {

    /** The mrconso. */
    MRCONSO,

    /** The mrdef. */
    MRDEF,

    /** The mrdoc. */
    MRDOC,

    /** The mrmap. */
    MRMAP,

    /** The mrrank. */
    MRRANK,

    /** The mrrel. */
    MRREL,

    /** The mrhier. */
    MRHIER,

    /** The mrsab. */
    MRSAB,

    /** The mrsat. */
    MRSAT,

    /** The mrsty. */
    MRSTY,

    /** The mrcui. */
    MRCUI,

    /** The mraui. */
    MRAUI,

    /** The srdef. */
    SRDEF;

  }

  /**
   * Instantiates an empty {@link RrfReaders}.
   *
   * @param inputDir the input dir
   * @throws Exception if anything goes wrong
   */
  public RrfReaders(File inputDir) throws Exception {
    this.inputDir = inputDir;
  }

  /**
   * Open readers.
   *
   * @throws Exception the exception
   */
  public void openReaders() throws Exception {

    // N/A - sorting is assumed

    // readers.put(Keys.MRCONSO, getReader("consoByConcept.sort"));
    // readers.put(Keys.MRDEF, getReader("defByConcept.sort"));
    // readers.put(Keys.MRDOC, getReader("docByKey.sort"));
    // readers.put(Keys.MRMAP, getReader("mapByConcept.sort"));
    // readers.put(Keys.MRRANK, getReader("rankByRank.sort"));
    // readers.put(Keys.MRREL, getReader("relByConcept.sort"));
    // readers.put(Keys.MRHIER, getReader("relByConcept.sort"));
    // readers.put(Keys.MRSAB, getReader("sabBySab.sort"));
    // readers.put(Keys.MRSAT, getReader("satByConcept.sort"));
    // readers.put(Keys.MRSTY, getReader("styByConcept.sort"));
    // readers.put(Keys.SRDEF, getReader("srdef.sort"));
    // readers.put(Keys.MRCUI, getReader("cuiHistory.sort"));
    // readers.put(Keys.MRAUI, getReader("auiHistory.sort"));

  }

  /**
   * Open original readers.
   *
   * @param prefix the prefix (e.g. MR or RXN)
   * @throws Exception the exception
   */
  public void openOriginalReaders(String prefix) throws Exception {

    for (final Keys key : Keys.values()) {
      readers.put(key,
          getReader(key.toString().replace("MR", prefix) + ".RRF"));
    }
    readers.put(Keys.SRDEF, getReader("SRDEF"));
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
