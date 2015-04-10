/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a kind of relationship.
 */
public interface RelationshipType extends Abbreviation {

	/**
	 * Returns the inverse.
	 * 
	 * @return the inverse
	 */
	public RelationshipType getInverse();

	/**
	 * Sets the inverse.
	 * 
	 * @param inverse the inverse
	 */
	public void setInverse(RelationshipType inverse);

}
