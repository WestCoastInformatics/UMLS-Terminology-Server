/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * JAXB enabled implementation of a {@link SearchResult}.
 */
@XmlRootElement(name = "searchResult")
public class SearchResultJpa implements SearchResult {

	/** The id. */
	private Long id;

	/** The terminology id. */
	private String terminologyId;

	/** The value. */
	private String codeId;

	/** The terminology. */
	private String terminology;

	/** The version. */
	private String version;

	/** The type. */
	private IdType type;

	/** The property. */
	private KeyValuePair property;

	/** The value. */
	private String value;

	/** The value. */
	private boolean leafNode;

	/** The value. */
	private boolean postCoordination;

	/** The obsolete. */
	private boolean obsolete;

	/** The score. */
	private Float score = null;

	/** The workflow status. */
	private WorkflowStatus workflowStatus;

	/**
	 * Instantiates a new search result jpa.
	 */
	public SearchResultJpa() {
		// left empty
	}

	/**
	 * Instantiates a new search result jpa.
	 *
	 * @param result
	 *            the result
	 */
	public SearchResultJpa(SearchResult result) {
		id = result.getId();
		terminology = result.getTerminology();
		terminologyId = result.getTerminologyId();
		codeId = result.getCodeId();
		version = result.getVersion();
		value = result.getValue();
		leafNode = result.isLeafNode();
		postCoordination = result.isPostCoodinated();
		obsolete = result.isObsolete();
		score = result.getScore();
		property = result.getProperty();
		type = result.getType();
		workflowStatus = result.getWorkflowStatus();
	}

	/* see superclass */
	@Override
	@XmlElement
	public Long getId() {
		return this.id;
	}

	/* see superclass */
	@Override
	public void setId(Long id) {
		this.id = id;

	}

	/* see superclass */
	@Override
	@XmlElement
	public String getTerminologyId() {
		return this.terminologyId;
	}

	/* see superclass */
	@Override
	public void setTerminologyId(String terminologyId) {
		this.terminologyId = terminologyId;

	}

	/* see superclass */
	@Override
	@XmlElement
	public String getCodeId() {
		return this.codeId;
	}

	/* see superclass */
	@Override
	public void setCodeId(String codeId) {
		this.codeId = codeId;
	}

	/* see superclass */
	@Override
	public String getTerminology() {
		return this.terminology;
	}

	/* see superclass */
	@Override
	public void setTerminology(String terminology) {
		this.terminology = terminology;
	}

	/* see superclass */
	@Override
	public String getVersion() {
		return this.version;
	}

	/* see superclass */
	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	/* see superclass */
	@Override
	@XmlElement
	public String getValue() {
		return this.value;
	}

	/* see superclass */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/* see superclass */
	@Override
	@XmlElement
	public boolean isLeafNode() {
		return this.leafNode;
	}

	/* see superclass */
	@Override
	public void setLeafNode(boolean leafNode) {
		this.leafNode = leafNode;
	}

	/* see superclass */
	@Override
	@XmlElement
	public boolean isPostCoodinated() {
		return this.postCoordination;
	}

	/* see superclass */
	@Override
	public void setPostCoodinated(boolean postCoordination) {
		this.postCoordination = postCoordination;
	}

	/* see superclass */
	@Override
	public boolean isObsolete() {
		return obsolete;
	}

	/* see superclass */
	@Override
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	/* see superclass */
	@Override
	public Float getScore() {
		return score;
	}

	/* see superclass */
	@Override
	public void setScore(Float score) {
		this.score = score;
	}

	/* see superclass */
	@Override
	public IdType getType() {
		return this.type;
	}

	/* see superclass */
	@Override
	public void setType(IdType type) {
		this.type = type;
	}

	/* see superclass */
	@Override
	public KeyValuePair getProperty() {
		return this.property;
	}

	/* see superclass */
	@Override
	public void setProperty(KeyValuePair property) {
		this.property = property;
	}

	/* see superclass */
	@Override
	public WorkflowStatus getWorkflowStatus() {
		return workflowStatus;
	}

	/* see superclass */
	@Override
	public void setWorkflowStatus(WorkflowStatus workflowStatus) {
		this.workflowStatus = workflowStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (obsolete ? 1231 : 1237);
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((terminology == null) ? 0 : terminology.hashCode());
		result = prime * result + ((terminologyId == null) ? 0 : terminologyId.hashCode());
		result = prime * result + ((codeId == null) ? 0 : codeId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + (leafNode ? 1231 : 1237);
		result = prime * result + (postCoordination ? 1231 : 1237);
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((workflowStatus == null) ? 0 : workflowStatus.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchResultJpa other = (SearchResultJpa) obj;
		if (obsolete != other.obsolete)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		if (terminology == null) {
			if (other.terminology != null)
				return false;
		} else if (!terminology.equals(other.terminology))
			return false;
		if (terminologyId == null) {
			if (other.terminologyId != null)
				return false;
		} else if (!terminologyId.equals(other.terminologyId))
			return false;
		if (codeId == null) {
			if (other.codeId != null)
				return false;
		} else if (!codeId.equals(other.codeId))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (leafNode != other.leafNode)
			return false;
		if (postCoordination != other.postCoordination)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (workflowStatus == null) {
			if (other.workflowStatus != null)
				return false;
		} else if (!workflowStatus.equals(other.workflowStatus))
			return false;

		return true;
	}

	/* see superclass */

	@Override
	public String toString() {
		return "SearchResultJpa [id=" + id + ", terminologyId=" + terminologyId + ", codeId=" + codeId + ",  value="
				+ value + ",  leafNode=" + leafNode + ",  postCoordination=" + postCoordination + ", terminology="
				+ terminology + ", version=" + version + ", type=" + type + ", property=" + property + ", obsolete="
				+ obsolete + ", score=" + score + ", workflowStatus=" + workflowStatus + "]";
	}

}