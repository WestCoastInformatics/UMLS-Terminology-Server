/**
 * Copyright 2016 West Coast Informatics, LLC
 */
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
   * Indicates whether or not exclude is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isExclude();

  /**
   * Sets the exclude.
   *
   * @param exclude the exclude
   */
  public void setExclude(boolean exclude);

  /**
   * Indicates whether or not norm exclude is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isNormExclude();

  /**
   * Sets the norm exclude.
   *
   * @param normExclude the norm exclude
   */
  public void setNormExclude(boolean normExclude);
  
  /**
   * Sets the name variant type.
   * 
   * @param nameVariantType the name variant type
   */
  public void setNameVariantType(NameVariantType nameVariantType);

  /**
   * Returns the name variant type.
   *
   * @return the name variant type
   */
  public NameVariantType getNameVariantType();

  /**
   * Sets the code variant type.
   * 
   * @param codeVariantType the code variant type
   */
  public void setCodeVariantType(CodeVariantType codeVariantType);

  /**
   * Returns the code variant type.
   *
   * @return the code variant type
   */
  public CodeVariantType getCodeVariantType();

  /**
   * Sets the hierarchical type flag.
   * 
   * @param hierarchicalType the hierarchical type flag
   */
  public void setHierarchicalType(boolean hierarchicalType);

  /**
   * Indicates whether or not hierarchical type is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isHierarchicalType();

  /**
   * Sets the usage type.
   * 
   * @param usageType the usage type
   */
  public void setUsageType(UsageType usageType);

  /**
   * Returns the usageType.
   *
   * @return the usageType
   */
  public UsageType getUsageType();

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
