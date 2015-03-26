package com.wci.umls.server.model.meta;

/**
 * Represents high level type information associated with an atom.
 */
public interface TermType extends Abbreviation {

  /**
   * Indicates whether or not this term type is obsolete.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isObsolete();

  /**
   * Sets the obsolete flag.
   * 
   * @param obselete the obsolete flag
   */
  public void setObsolete(boolean obselete);

  /**
   * Sets the name variant type.
   * 
   * @param nameVariantType the name variant type
   */
  public void setNameVariantType(String nameVariantType);

  /**
   * Sets the code variant type.
   * 
   * @param codeVariantType the code variant type
   */
  public void setCodeVariantType(String codeVariantType);

  /**
   * Sets the hierarchical type.
   * 
   * @param hierarchicalType the hierarchical type
   */
  public void setHierarchicalType(String hierarchicalType);

  /**
   * Sets the usage.
   * 
   * @param usage the usage
   */
  public void setUsage(String usage);

  /**
   * Sets the style.
   * 
   * @param style the style
   */
  public void setStyle(String style);

}
