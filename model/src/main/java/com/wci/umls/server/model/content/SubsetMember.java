/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * SubsetMember: SubsetMember.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasAttributes;


/**
 * Represents membership of something in a {@link Subset}.
 */
public interface SubsetMember extends Component, HasAttributes {

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
