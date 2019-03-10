/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Generic object to contain search results.
 */
public interface SearchResult {

	/**
	 * Returns the id.
	 *
	 * @return the id
	 */
	public Long getId();

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id);

	/**
	 * Returns the terminology id.
	 *
	 * @return the terminologyId
	 */
	public String getTerminologyId();

	/**
	 * Sets the terminology id.
	 *
	 * @param terminologyId
	 *            the terminologyId to set
	 */
	public void setTerminologyId(String terminologyId);

	/**
	 * Returns the code id.
	 *
	 * @return the codeId
	 */
	public String getCodeId();

	/**
	 * Sets the code id.
	 *
	 * @param codeId
	 *            the codeId to set
	 */
	public void setCodeId(String codeId);

	/**
	 * Returns the terminology.
	 *
	 * @return the terminology
	 */
	public String getTerminology();

	/**
	 * Sets the terminology.
	 *
	 * @param terminology
	 *            the terminology to set
	 */
	public void setTerminology(String terminology);

	/**
	 * Returns the version.
	 *
	 * @return the version
	 */
	public String getVersion();

	/**
	 * Sets the version.
	 *
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version);

	/**
	 * Returns the value.
	 *
	 * @return the value
	 */
	public String getValue();

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value);

	/**
	 * Sets the leaf node.
	 *
	 * @param leafNode
	 *            the leafNode to set
	 */
	public void setLeafNode(boolean leafNode);

	/**
	 * Returns the leaf node.
	 *
	 * @return the leafNode
	 */
	public boolean isLeafNode();

	/**
	 * Sets the post coordination.
	 *
	 * @param postCoordination
	 *            the postCoordination to set
	 */
	public void setPostCoodinated(boolean postCoordination);

	/**
	 * Returns the post coordination.
	 *
	 * @return the postCoordination
	 */
	public boolean isPostCoodinated();

	/**
	 * Indicates whether or not obsolete is the case.
	 *
	 * @return <code>true</code> if so, <code>false</code> otherwise
	 */
	public boolean isObsolete();

	/**
	 * Sets the obsolete.
	 *
	 * @param obsolete
	 *            the obsolete
	 */
	public void setObsolete(boolean obsolete);

	/**
	 * Returns the score.
	 *
	 * @return the score
	 */
	public Float getScore();

	/**
	 * Sets the score.
	 *
	 * @param score
	 *            the score
	 */
	public void setScore(Float score);

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public IdType getType();

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(IdType type);

	/**
	 * Gets the property.
	 *
	 * @return the property
	 */
	public KeyValuePair getProperty();

	/**
	 * Sets the property.
	 *
	 * @param property
	 *            the new property
	 */
	public void setProperty(KeyValuePair property);

	/**
	 * Returns the workflow status.
	 *
	 * @return the workflow status
	 */
	public WorkflowStatus getWorkflowStatus();

	/**
	 * Sets the workflow status.
	 *
	 * @param workflowStatus
	 *            the workflow status
	 */
	public void setWorkflowStatus(WorkflowStatus workflowStatus);
}
