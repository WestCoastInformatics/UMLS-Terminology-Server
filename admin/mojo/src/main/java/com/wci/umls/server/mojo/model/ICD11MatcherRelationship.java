package com.wci.umls.server.mojo.model;

// TODO: Auto-generated Javadoc
/**
 * The Class SctRelationship.
 *
 * @author ${author}
 */
public class ICD11MatcherRelationship extends AbstractSctComponent {
	/** The description. */
	private String description;

	/**  The relationship type. */
	private String relationshipType;

	/**  The relationship destination. */
	private String relationshipDestination;

	/**  The role group. */
	private int roleGroup;

	/**
	 * Returns the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the relationship type.
	 *
	 * @return the relationship type
	 */
	public String getRelationshipType() {
		return relationshipType;
	}

	/**
	 * Sets the relationship type.
	 *
	 * @param relationshipType the relationship type
	 */
	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	/**
	 * Returns the relationship destination.
	 *
	 * @return the relationship destination
	 */
	public String getRelationshipDestination() {
		return relationshipDestination;
	}

	/**
	 * Sets the relationship destination.
	 *
	 * @param relationshipDestination the relationship destination
	 */
	public void setRelationshipDestination(String relationshipDestination) {
		this.relationshipDestination = relationshipDestination;
	}

	/**
	 * Returns the role group.
	 *
	 * @return the role group
	 */
	public int getRoleGroup() {
		return roleGroup;
	}

	/**
	 * Sets the role group.
	 *
	 * @param roleGroup the role group
	 */
	public void setRoleGroup(int roleGroup) {
		this.roleGroup = roleGroup;
	}

	/* (non-Javadoc)
	 * @see com.wci.umls.server.mojo.model.AbstractSctComponent#printForExcel()
	 */
	/* see superclass */
	@Override
	public String printForExcel() {
		StringBuffer str = new StringBuffer();

		str.append(processForExcel(description));
		str.append(processForExcel(relationshipType));
		str.append(processForExcel(relationshipDestination));
		str.append(processForExcel(roleGroup));

		return str.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
	    int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((relationshipType == null) ? 0 : relationshipType.hashCode());
		result = prime * result + ((relationshipDestination == null) ? 0 : relationshipDestination.hashCode());
		result = prime * result + roleGroup;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/* see superclass */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ICD11MatcherRelationship other = (ICD11MatcherRelationship) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (relationshipType == null) {
			if (other.relationshipType != null)
				return false;
		} else if (!relationshipType.equals(other.relationshipType))
			return false;
		if (relationshipDestination == null) {
			if (other.relationshipDestination != null)
				return false;
		} else if (!relationshipDestination.equals(other.relationshipDestination))
			return false;
		if (roleGroup != other.roleGroup)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/* see superclass */
	@Override
	public String toString() {
		return "SctSourceRelationship [description=" + description + ", relationshipType=" + relationshipType
				+ ", relationshipDestination=" + relationshipDestination + ", roleGroup=" + roleGroup + "]";
	}
}
