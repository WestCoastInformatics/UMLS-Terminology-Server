package com.wci.umls.server.mojo.model;

// TODO: Auto-generated Javadoc
/**
 * The Class SctSourceDescription.
 *
 * @author ${author}
 */
public class SctSourceDescription {

	/** The description. */
	private String description;

	/** The neoplasm synonym. */
	private String neoplasmSynonym;

	/** The pathology. */
	private String pathology;

	/** The body structure. */
	private String bodyStructure;

	/** The second info. */
	private String secondInfo;

	/** The uncertainty. */
	private String uncertainty;

	/** The is stage. */
	private boolean isStage = false;

	/** The primary or secondary. */
	private String primaryOrSecondary;

	/** The benign or malignant. */
	private String benignOrMalignant;

	/** The upper or lower. */
	private String upperOrLower;

	/** The left or right. */
	private String leftOrRight;

	/** The is metastatic. */
	private boolean isMetastatic = false;

	/** The is in situ. */
	private boolean isInSitu = false;

	/** The is node. */
	private boolean isNode = false;

	/** The is local recurrance. */
	private boolean isLocalRecurrance = false;

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
	 * @param description
	 *            the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the neoplasm synonym.
	 *
	 * @return the neoplasm synonym
	 */
	public String getNeoplasmSynonym() {
		return neoplasmSynonym;
	}

	/**
	 * Sets the neoplasm synonym.
	 *
	 * @param neoplasmSynonym
	 *            the neoplasm synonym
	 */
	public void setNeoplasmSynonym(String neoplasmSynonym) {
		this.neoplasmSynonym = neoplasmSynonym;
	}

	/**
	 * Returns the pathology.
	 *
	 * @return the pathology
	 */
	public String getPathology() {
		return pathology;
	}

	/**
	 * Sets the pathology.
	 *
	 * @param pathology
	 *            the pathology
	 */
	public void setPathology(String pathology) {
		this.pathology = pathology;
	}

	/**
	 * Returns the body structure.
	 *
	 * @return the body structure
	 */
	public String getBodyStructure() {
		return bodyStructure;
	}

	/**
	 * Sets the body structure.
	 *
	 * @param bodyStructure
	 *            the body structure
	 */
	public void setBodyStructure(String bodyStructure) {
		this.bodyStructure = bodyStructure;
	}

	/**
	 * Returns the second info.
	 *
	 * @return the second info
	 */
	public String getSecondInfo() {
		return secondInfo;
	}

	/**
	 * Sets the second info.
	 *
	 * @param secondInfo
	 *            the second info
	 */
	public void setSecondInfo(String secondInfo) {
		this.secondInfo = secondInfo;
	}

	/**
	 * Returns the uncertainty.
	 *
	 * @return the uncertainty
	 */
	public String getUncertainty() {
		return uncertainty;
	}

	/**
	 * Sets the uncertainty.
	 *
	 * @param uncertainty
	 *            the uncertainty
	 */
	public void setUncertainty(String uncertainty) {
		this.uncertainty = uncertainty;
	}

	/**
	 * Indicates whether or not stage is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isStage() {
		return isStage;
	}

	/**
	 * Sets the stage.
	 *
	 * @param isStage
	 *            the stage
	 */
	public void setStage(boolean isStage) {
		this.isStage = isStage;
	}

	/**
	 * Returns the primary or secondary.
	 *
	 * @return the primary or secondary
	 */
	public String getPrimaryOrSecondary() {
		return primaryOrSecondary;
	}

	/**
	 * Sets the primary or secondary.
	 *
	 * @param primaryOrSecondary
	 *            the primary or secondary
	 */
	public void setPrimaryOrSecondary(String primaryOrSecondary) {
		this.primaryOrSecondary = primaryOrSecondary;
	}

	/**
	 * Returns the benign or malignant.
	 *
	 * @return the benign or malignant
	 */
	public String getBenignOrMalignant() {
		return benignOrMalignant;
	}

	/**
	 * Sets the benign or malignant.
	 *
	 * @param benignOrMalignant
	 *            the benign or malignant
	 */
	public void setBenignOrMalignant(String benignOrMalignant) {
		this.benignOrMalignant = benignOrMalignant;
	}

	/**
	 * Returns the upper or lower.
	 *
	 * @return the upper or lower
	 */
	public String getUpperOrLower() {
		return upperOrLower;
	}

	/**
	 * Sets the upper or lower.
	 *
	 * @param upperOrLower
	 *            the upper or lower
	 */
	public void setUpperOrLower(String upperOrLower) {
		this.upperOrLower = upperOrLower;
	}

	/**
	 * Returns the left or right.
	 *
	 * @return the left or right
	 */
	public String getLeftOrRight() {
		return leftOrRight;
	}

	/**
	 * Sets the left or right.
	 *
	 * @param leftOrRight
	 *            the left or right
	 */
	public void setLeftOrRight(String leftOrRight) {
		this.leftOrRight = leftOrRight;
	}

	/**
	 * Indicates whether or not metastatic is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isMetastatic() {
		return isMetastatic;
	}

	/**
	 * Sets the metastatic.
	 *
	 * @param isMetastatic
	 *            the metastatic
	 */
	public void setMetastatic(boolean isMetastatic) {
		this.isMetastatic = isMetastatic;
	}

	/**
	 * Indicates whether or not in situ is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isInSitu() {
		return isInSitu;
	}

	/**
	 * Sets the in situ.
	 *
	 * @param isInSitu
	 *            the in situ
	 */
	public void setInSitu(boolean isInSitu) {
		this.isInSitu = isInSitu;
	}

	/**
	 * Indicates whether or not node is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isNode() {
		return isNode;
	}

	/**
	 * Sets the node.
	 *
	 * @param isNode
	 *            the node
	 */
	public void setNode(boolean isNode) {
		this.isNode = isNode;
	}

	/**
	 * Indicates whether or not local recurrance is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isLocalRecurrance() {
		return isLocalRecurrance;
	}

	/**
	 * Sets the local recurrance.
	 *
	 * @param isLocalRecurrance
	 *            the local recurrance
	 */
	public void setLocalRecurrance(boolean isLocalRecurrance) {
		this.isLocalRecurrance = isLocalRecurrance;
	}

	public String printForExcel() {
		StringBuffer str = new StringBuffer();

		str.append(description + "\t");
		str.append(processForExcel(neoplasmSynonym));
		str.append(processForExcel(pathology));
		str.append(processForExcel(bodyStructure));
		str.append(processForExcel(secondInfo));
		str.append(processForExcel(uncertainty));
		str.append(processForExcel(isStage));
		str.append(processForExcel(primaryOrSecondary));
		str.append(processForExcel(benignOrMalignant));
		str.append(processForExcel(upperOrLower));
		str.append(processForExcel(leftOrRight));
		str.append(processForExcel(isMetastatic));
		str.append(processForExcel(isInSitu));
		str.append(processForExcel(isNode));
		str.append(processForExcel(isLocalRecurrance));

		return str.toString();
	}

	private Object processForExcel(String str) {
		if (str != null) {
			return str + "\t";
		}
		return "\t";
	}

	private Object processForExcel(boolean val) {
		if (val) {
			return "X\t";
		}
		return "\t";
	}


	/* see superclass */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((neoplasmSynonym == null) ? 0 : neoplasmSynonym.hashCode());
		result = prime * result + ((pathology == null) ? 0 : pathology.hashCode());
		result = prime * result + ((bodyStructure == null) ? 0 : bodyStructure.hashCode());
		result = prime * result + ((secondInfo == null) ? 0 : secondInfo.hashCode());
		result = prime * result + ((uncertainty == null) ? 0 : uncertainty.hashCode());
		result = prime * result + (isStage ? 1231 : 1237);
		result = prime * result + ((primaryOrSecondary == null) ? 0 : primaryOrSecondary.hashCode());
		result = prime * result + ((benignOrMalignant == null) ? 0 : benignOrMalignant.hashCode());
		result = prime * result + ((upperOrLower == null) ? 0 : upperOrLower.hashCode());
		result = prime * result + ((leftOrRight == null) ? 0 : leftOrRight.hashCode());
		result = prime * result + (isMetastatic ? 1231 : 1237);
		result = prime * result + (isInSitu ? 1231 : 1237);
		result = prime * result + (isNode ? 1231 : 1237);
		result = prime * result + (isLocalRecurrance ? 1231 : 1237);
		return result;
	}

	/* see superclass */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SctSourceDescription other = (SctSourceDescription) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (neoplasmSynonym == null) {
			if (other.neoplasmSynonym != null)
				return false;
		} else if (!neoplasmSynonym.equals(other.neoplasmSynonym))
			return false;
		if (pathology == null) {
			if (other.pathology != null)
				return false;
		} else if (!pathology.equals(other.pathology))
			return false;
		if (bodyStructure == null) {
			if (other.bodyStructure != null)
				return false;
		} else if (!bodyStructure.equals(other.bodyStructure))
			return false;
		if (secondInfo == null) {
			if (other.secondInfo != null)
				return false;
		} else if (!secondInfo.equals(other.secondInfo))
			return false;
		if (uncertainty == null) {
			if (other.uncertainty != null)
				return false;
		} else if (!uncertainty.equals(other.uncertainty))
			return false;
		if (isStage != other.isStage)
			return false;
		if (primaryOrSecondary == null) {
			if (other.primaryOrSecondary != null)
				return false;
		} else if (!primaryOrSecondary.equals(other.primaryOrSecondary))
			return false;
		if (benignOrMalignant == null) {
			if (other.benignOrMalignant != null)
				return false;
		} else if (!benignOrMalignant.equals(other.benignOrMalignant))
			return false;
		if (upperOrLower == null) {
			if (other.upperOrLower != null)
				return false;
		} else if (!upperOrLower.equals(other.upperOrLower))
			return false;
		if (leftOrRight == null) {
			if (other.leftOrRight != null)
				return false;
		} else if (!leftOrRight.equals(other.leftOrRight))
			return false;
		if (isMetastatic != other.isMetastatic)
			return false;
		if (isInSitu != other.isInSitu)
			return false;
		if (isNode != other.isNode)
			return false;
		if (isLocalRecurrance != other.isLocalRecurrance)
			return false;
		return true;
	}

	/* see superclass */
	@Override
	public String toString() {
		return "SctSourceDescription [description=" + description + ", neoplasmSynonym=" + neoplasmSynonym
				+ ", pathology=" + pathology + ", bodyStructure=" + bodyStructure + ", secondInfo=" + secondInfo
				+ ", uncertainty=" + uncertainty + ", isStage=" + isStage + ", primaryOrSecondary=" + primaryOrSecondary
				+ ", benignOrMalignant=" + benignOrMalignant + ", upperOrLower=" + upperOrLower + ", leftOrRight="
				+ leftOrRight + ", isMetastatic=" + isMetastatic + ", isInSitu=" + isInSitu + ", isNode=" + isNode
				+ ", isLocalRecurrance=" + isLocalRecurrance + "]";
	}
}
