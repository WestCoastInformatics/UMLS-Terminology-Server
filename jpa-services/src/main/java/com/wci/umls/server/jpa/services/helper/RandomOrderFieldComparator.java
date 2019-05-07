package com.wci.umls.server.jpa.services.helper;

import java.util.Date;
import java.util.Random;

import org.apache.lucene.search.FieldComparator;

/**
 * Helper method to enable lucene search random order results.
 */
public class RandomOrderFieldComparator extends FieldComparator.LongComparator {

  /** The random number generator. */
  private final Random random = new Random(new Date().getTime());

  /**
   * Instantiates a {@link RandomOrderFieldComparator} from the specified
   * parameters.
   *
   * @param numHits the num hits
   * @param field the field
   * @param parser the parser
   * @param missingValue the missing value
   */
  public RandomOrderFieldComparator(int numHits, String field, Long missingValue) {
    super(numHits, field, missingValue);
  }

  /* see superclass */
  @Override
  public Long value(int slot) {
    return random.nextLong();
  }

  /* see superclass */
  @Override
  public int compare(int slot1, int slot2) {
    return random.nextInt();
  }

  /* see superclass */
  @Override
  public int compareBottom(int doc) {
    return random.nextInt();
  }
}
