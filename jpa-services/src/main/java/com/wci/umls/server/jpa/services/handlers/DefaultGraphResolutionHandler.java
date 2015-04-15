/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Default implementation of {@link GraphResolutionHandler}. This connects
 * graphs at the level at which CascadeType.ALL is used in the data model.
 */
public class DefaultGraphResolutionHandler implements GraphResolutionHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Concept, java.util.Set)
   */
  @Override
  public void resolve(Concept concept, Set<String> isaRelTypeIds) {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolveEmpty
   * (com.wci.umls.server.model.content.Concept)
   */
  @Override
  public void resolveEmpty(Concept concept) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Atom)
   */
  @Override
  public void resolve(Atom atom) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Relationship)
   */
  @Override
  public void resolve(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.meta.SemanticType)
   */
  @Override
  public void resolve(SemanticType sty) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Descriptor, java.util.Set)
   */
  @Override
  public void resolve(Descriptor descriptor, Set<String> isaRelTypeIds) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Code, java.util.Set)
   */
  @Override
  public void resolve(Code descriptor, Set<String> isaRelTypeIds) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.LexicalClass)
   */
  @Override
  public void resolve(LexicalClass lexicalClass) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.StringClass)
   */
  @Override
  public void resolve(StringClass stringClass) {
    // TODO Auto-generated method stub

  }

}
