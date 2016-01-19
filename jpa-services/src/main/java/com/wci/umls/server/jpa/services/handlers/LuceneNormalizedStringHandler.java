/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.wci.umls.server.services.handlers.NormalizedStringHandler;

/**
 * Implements a normalized string handler based on the Lucune
 * {@link StandardAnalyzer}.
 */
public class LuceneNormalizedStringHandler implements NormalizedStringHandler {

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getNormalizedString(String string) {
    List<String> result = new ArrayList<String>();
    StandardAnalyzer analyzer = new StandardAnalyzer();
    try {
      TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
      stream.reset();
      while (stream.incrementToken()) {
        result.add(stream.getAttribute(CharTermAttribute.class).toString());
      }
    } catch (IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    } finally {
      analyzer.close();
    }
    Collections.sort(result);
    StringBuilder normalizedString = new StringBuilder();
    Iterator<String> iter = result.iterator();
    while (iter.hasNext()) {
      normalizedString.append(iter.next()).append(iter.hasNext() ? " " : "");
    }
    return normalizedString.toString();

  }
  
  /* see superclass */
  @Override
  public String getName() {
    return "Lucene normalized string handler";
  }


}
