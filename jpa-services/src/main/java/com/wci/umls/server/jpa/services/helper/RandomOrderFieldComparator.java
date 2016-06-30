package com.wci.umls.server.jpa.services.helper;

import java.util.Random;

import org.apache.lucene.search.FieldCache.Parser;
import org.apache.lucene.search.FieldComparator;

/**
 * Helper method to enable lucene search random order results.
 */
public class RandomOrderFieldComparator extends FieldComparator.IntComparator {

  /**
   * @param numHits
   * @param field
   * @param parser
   * @param missingValue
   */
  public RandomOrderFieldComparator(int numHits, String field, Parser parser,
      Integer missingValue) {
    super(numHits, field, parser, missingValue);
  }

  private final Random random = new Random();

  @Override
  public Integer value(int slot) {
    return random.nextInt();
  }

}
