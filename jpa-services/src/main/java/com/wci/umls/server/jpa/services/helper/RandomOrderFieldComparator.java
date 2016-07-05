package com.wci.umls.server.jpa.services.helper;

import java.util.Random;

import org.apache.lucene.search.FieldCache.Parser;
import org.apache.lucene.search.FieldComparator;

/**
 * Helper method to enable lucene search random order results.
 */
public class RandomOrderFieldComparator extends FieldComparator.IntComparator {

  /** The random number genreator. */
  private final Random random = new Random();

  /**
   * Instantiates a {@link RandomOrderFieldComparator} from the specified
   * parameters.
   *
   * @param numHits the num hits
   * @param field the field
   * @param parser the parser
   * @param missingValue the missing value
   */
  public RandomOrderFieldComparator(int numHits, String field, Parser parser,
      Integer missingValue) {
    super(numHits, field, parser, missingValue);
  }

  /* see superclass */
  @Override
  public Integer value(int slot) {
    return random.nextInt();
  }

}
