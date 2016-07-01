/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringIdentity;
import com.wci.umls.server.services.UmlsIdentityService;

/**
 * JPA and JAXB enabled implementation of {@link UmlsIdentityService}.
 */
public class UmlsIdentityServiceJpa extends RootServiceJpa implements
    UmlsIdentityService {

  /**
   * Instantiates an empty {@link UmlsIdentityServiceJpa}.
   *
   * @throws Exception the exception
   */
  public UmlsIdentityServiceJpa() throws Exception {
    super();

  }

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get attribute identity " + id);
    return getObject(id, AttributeIdentity.class);
  }

  @Override
  public Long getNextAttributeId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next attribute id");

    Long attId = 0L;
    Long styId = 0L;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AttributeIdentityJpa a ");
      Long attId2 = (Long) query.getSingleResult();
      attId = attId2 != null ? attId2 : attId;
    } catch (NoResultException e) {
      attId = 0L;
    }
    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select max(a.id) from SemanticTypeComponentIdentityJpa a ");
      Long styId2 = (Long) query.getSingleResult();
      styId = styId2 != null ? styId2 : styId;
    } catch (NoResultException e) {
      styId = 0L;
    }
    // Return the max
    return Math.max(attId, styId)+1;
  }

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(AttributeIdentity identity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get attribute identity " + identity);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from AttributeIdentityJpa a "
              + "where terminology = :terminology "
              + "and terminologyId = :terminologyId "
              + "and componentId = :componentId " + "and componentType = :componentType "
              + "and componentTerminology = :componentTerminology " + "and name = :name "
              + "and hashcode = :hashcode");
      query.setParameter("terminology", identity.getTerminology());
      query.setParameter("terminologyId", identity.getTerminologyId());
      query.setParameter("componentId", identity.getComponentId());
      query.setParameter("componentType", identity.getComponentType());
      query.setParameter("componentTerminology", identity.getComponentTerminology());
      query.setParameter("name", identity.getName());
      query.setParameter("hashcode", identity.getHashcode());

      return (AttributeIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public AttributeIdentity addAttributeIdentity(
    AttributeIdentity attributeIdentity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add attribute identity "
            + attributeIdentity.toString());
    return addObject(attributeIdentity);
  }

  /* see superclass */
  @Override
  public void updateAttributeIdentity(AttributeIdentity attributeIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - update attribute identity "
            + attributeIdentity.toString());

    updateObject(attributeIdentity);
  }

  /* see superclass */
  @Override
  public void removeAttributeIdentity(Long attributeIdentityId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove attribute identity "
            + attributeIdentityId);

    AttributeIdentity identity = getAttributeIdentity(attributeIdentityId);
    removeObject(identity, AttributeIdentity.class);
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get semanticTypeComponent identity " + id);
    return getObject(id, SemanticTypeComponentIdentity.class);
  }

  @Override
  public Long getNextSemanticTypeComponentId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next semanticTypeComponent id");

    Long attId = 0L;
    Long styId = 0L;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AttributeIdentity a ");
      Long attId2 = (Long) query.getSingleResult();
      attId = attId2 != null ? attId2 : attId;
    } catch (NoResultException e) {
      attId = 0L;
    }
    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select max(a.id) from SemanticTypeComponentIdentityJpa a ");
      styId = (Long) query.getSingleResult();
    } catch (NoResultException e) {
      styId = 0L;
    }
    // Return the max
    return Math.max(attId, styId)+1;
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity identity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get semanticTypeComponent identity "
            + identity);

    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select a from SemanticTypeComponentIdentityJpa a "
                  + "where terminology = :terminology "
                  + "and conceptTerminologyId = :conceptTerminologyId"
                  + "and semanticType = :semanticType");

      query.setParameter("terminology", identity.getTerminology());
      query.setParameter("concdeptTerminologyId",
          identity.getConceptTerminologyId());
      query.setParameter("semanticType", identity.getSemanticType());
      return (SemanticTypeComponentIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity addSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add semanticTypeComponent identity "
            + semanticTypeComponentIdentity.toString());
    return addObject(semanticTypeComponentIdentity);
  }

  /* see superclass */
  @Override
  public void updateSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - update semanticTypeComponent identity "
            + semanticTypeComponentIdentity.toString());

    updateObject(semanticTypeComponentIdentity);
  }

  /* see superclass */
  @Override
  public void removeSemanticTypeComponentIdentity(
    Long semanticTypeComponentIdentityId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove semanticTypeComponent identity "
            + semanticTypeComponentIdentityId);

    SemanticTypeComponentIdentity identity =
        getSemanticTypeComponentIdentity(semanticTypeComponentIdentityId);
    removeObject(identity, SemanticTypeComponentIdentity.class);
  }

  @Override
  public AtomIdentity getAtomIdentity(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get atom identity " + id);
    return getObject(id, AtomIdentity.class);
  }

  @Override
  public Long getNextAtomId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next atom id");

    Long atomId = 0L;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AtomIdentityJpa a ");
      Long atomId2 = (Long) query.getSingleResult();
      atomId = atomId2!=null?atomId2:atomId;
    } catch (NoResultException e) {
      atomId = 0L;
    }
    // Return the max
    return atomId+1;
  }

  @Override
  public AtomIdentity getAtomIdentity(AtomIdentity identity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get atom identity " + identity);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from AtomIdentityJpa a "
              + "where stringClassId = : stringClassId"
              + "where terminology = :terminology "
              + "and terminologyId = :terminologyId "
              + "where termType = : termType"
              + "where code = code: "
              + "where conceptId = : conceptId"
              + "where descriptorId = : descriptorId"
              );
      query.setParameter("stringClassId", identity.getStringClassId());
      query.setParameter("terminology", identity.getTerminology());
      query.setParameter("terminologyId", identity.getTerminologyId());
      query.setParameter("termType", identity.getTermType());
      query.setParameter("code", identity.getCode());
      query.setParameter("conceptId", identity.getConceptId());
      query.setParameter("descriptorId", identity.getDescriptorId());

      return (AtomIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public AtomIdentity addAtomIdentity(AtomIdentity atomIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add atom identity "
            + atomIdentity.toString());
    return addObject(atomIdentity);
  }

  @Override
  public void updateAtomIdentity(AtomIdentity atomIdentity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - update atom identity "
            + atomIdentity.toString());

    updateObject(atomIdentity);
  }

  @Override
  public void removeAtomIdentity(Long atomIdentityId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove atom identity "
            + atomIdentityId);

    AtomIdentity identity = getAtomIdentity(atomIdentityId);
    removeObject(identity, AtomIdentity.class);
  }

  @Override
  public StringIdentity getStringIdentity(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get string identity " + id);
    return getObject(id, StringIdentity.class);
  }

  @Override
  public Long getNextStringId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next string id");

    Long stringId = 0L;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from StringIdentityJpa a ");
      Long stringId2 = (Long) query.getSingleResult();
      stringId = stringId2!=null?stringId2:stringId;
    } catch (NoResultException e) {
      stringId = 0L;
    }
    // Return the max
    return stringId+1;
  }

  @Override
  public StringIdentity getStringIdentity(StringIdentity identity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get string identity " + identity);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from StringIdentityJpa a "
              + "where caseInsensitiveId = :caseInsensitiveId "
              + "and string = :string");
      query.setParameter("id", identity.getId());
      query.setParameter("string", identity.getString());

      return (StringIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public StringIdentity addStringIdentity(StringIdentity stringIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add string identity "
            + stringIdentity.toString());
    return addObject(stringIdentity);
  }

  @Override
  public void updateStringIdentity(StringIdentity stringIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - update string identity "
            + stringIdentity.toString());

    updateObject(stringIdentity);
  }

  @Override
  public void removeStringIdentity(Long stringIdentityId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove string identity "
            + stringIdentityId);

    StringIdentity identity = getStringIdentity(stringIdentityId);
    removeObject(identity, StringIdentity.class);
  }

  @Override
  public LexicalClassIdentity getLexicalClassIdentity(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get lexical class identity " + id);
    return getObject(id, LexicalClassIdentity.class);
  }

  @Override
  public Long getNextLexicalClassId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next lexicalClass id");

    Long lexicalClassId = 0L;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from LexicalClassIdentityJpa a ");
      Long lexicalClassId2 = (Long) query.getSingleResult();
      lexicalClassId = lexicalClassId2!=null?lexicalClassId2:lexicalClassId;
    } catch (NoResultException e) {
      lexicalClassId = 0L;
    }
    // Return the max
    return lexicalClassId+1;
  }

  @Override
  public LexicalClassIdentity getLexicalClassIdentity(
    LexicalClassIdentity identity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get lexicalClass identity " + identity);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from LexicalClassIdentityJpa a "
              + "where normalizedString = :normalizedString ");
      query.setParameter("normalizedString", identity.getNormalizedString());

      return (LexicalClassIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public LexicalClassIdentity addLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add lexicalClass identity "
            + lexicalClassIdentity.toString());
    return addObject(lexicalClassIdentity);
  }

  @Override
  public void updateLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - update lexicalClass identity "
            + lexicalClassIdentity.toString());

    updateObject(lexicalClassIdentity);
  }

  @Override
  public void removeLexicalClassIdentity(Long lexicalClassIdentityId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove lexicalClass identity "
            + lexicalClassIdentityId);

    LexicalClassIdentity identity = getLexicalClassIdentity(lexicalClassIdentityId);
    removeObject(identity, LexicalClassIdentity.class);
  }

}
