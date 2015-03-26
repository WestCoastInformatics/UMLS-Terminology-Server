/*************************************************************
 * ReleaseProperty: ReleaseProperty.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.meta;

/**
 * Represents a generic way to extend release information.
 * 
 * @author Tun Tun Naing (tun-tun.naing@lmco.com)
 */
public interface ReleaseProperty {

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name);

	/**
	 * Returns the value.
	 * 
	 * @return the value
	 */
	public String getValue();

	/**
	 * Sets the value.
	 * 
	 * @param value the value
	 */
	public void setValue(String value);

	/**
	 * Returns the release.
	 * 
	 * @return the release
	 */
	public ReleaseInfo getRelease();

	/**
	 * Sets the release.
	 * 
	 * @param releaseInfo the release
	 */
	public void setRelease(ReleaseInfo releaseInfo);

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public Long getId();

	/**
	 * Sets the id.
	 * 
	 * @param id the id
	 */
	public void setId(Long id);
}
