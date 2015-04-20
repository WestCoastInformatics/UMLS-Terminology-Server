/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * SubsetMember: SubsetMember.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;



/**
 * Represents membership of something in a {@link Subset}.
 */
public interface SubsetMember extends ComponentHasAttributes {

	/**
	 * Returns the subset.
	 * 
	 * @return the subset
	 */
	public Subset getSubset();

	/**
	 * Sets the subset.
	 * 
	 * @param Subset the subset
	 */
	public void setSubset(Subset Subset);

}
