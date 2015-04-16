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
 * @param <T> the type
 */
public interface SubsetMember<T> extends ComponentHasAttributes {

	/**
	 * Returns the member.
	 *
	 * @return the member
	 */
	public T getMember();
	
	/**
	 * Sets the member.
	 *
	 * @param member the new member
	 */
	public void setMember(T member);
}
