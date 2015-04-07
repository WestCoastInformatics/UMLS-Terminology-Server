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
   * @param obsolete the obsolete
   */
  public void setObsolete(boolean obsolete);

  /**
   * Indicates whether or not this term type is suppressible.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSuppressible();

  /**
   * Sets the suppressible flag.
   *
   * @param suppressible the suppressible
   */
  public void setSuppressible(boolean suppressible);

  /**
   * Sets the name variant type.
   * 
   * @param nameVariantType the name variant type
   */
  public void setNameVariantType(String nameVariantType);

  /**
   * Returns the name variant type.
   *
   * @return the name variant type
   */
  public String getNameVariantType();

  /**
   * Sets the code variant type.
   * 
   * @param codeVariantType the code variant type
   */
  public void setCodeVariantType(String codeVariantType);

  /**
   * Returns the code variant type.
   *
   * @return the code variant type
   */
  public String getCodeVariantType();

  /**
   * Sets the hierarchical type.
   * 
   * @param hierarchicalType the hierarchical type
   */
  public void setHierarchicalType(String hierarchicalType);

  /**
   * Returns the hierarchical type.
   *
   * @return the hierarchical type
   */
  public String getHierarchicalType();

  /**
   * Sets the usage.
   * 
   * @param usage the usage
   */
  public void setUsage(String usage);
  
  /**
   * Returns the usage.
   *
   * @return the usage
   */
  public String getUsage();

  /**
   * Sets the style.
   * 
   * @param style the style
   */
  public void setStyle(TermTypeStyle style);

  /**
   * Returns the style.
   *
   * @return the style
   */
  public TermTypeStyle getStyle();

}
