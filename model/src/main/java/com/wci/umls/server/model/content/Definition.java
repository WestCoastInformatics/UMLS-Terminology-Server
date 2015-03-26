package com.wci.umls.server.model.content;


/**
 * Represents a definition.
 */
public interface Definition extends Component {

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

}