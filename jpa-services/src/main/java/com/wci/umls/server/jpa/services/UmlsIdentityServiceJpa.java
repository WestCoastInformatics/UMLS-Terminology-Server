/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
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

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(String hashCode)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get attribute identity " + hashCode);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from AttributeIdentity a "
              + "where hashCode = :hashCode");
      query.setParameter("hashCode", hashCode);

      return (AttributeIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public Long getNextAttributeId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next attribute id");

    Long attId = null;
    Long styId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AttributeIdentity a ");
      attId = (Long) query.getSingleResult();
    } catch (NoResultException e) {
      attId = 1L;
    }
    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select max(a.id) from SemanticTypeComponentIdentity a ");
      styId = (Long) query.getSingleResult();
    } catch (NoResultException e) {
      styId = 1L;
    }
    // Return the max
    return Math.max(attId, styId);
  }

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(AttributeIdentity identity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get attribute identity " + identity);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from AttributeIdentity a "
              + "where terminology = :terminology "
              + "and terminologyId = :terminologyId" + "and ownerId = :ownerId"
              + "and ownerType = :ownerType"
              + "and ownerQualifier = :ownerQualifier" + "and name = :name"
              + "and hashcode = :hashcode");
      query.setParameter("terminology", identity.getTerminology());
      query.setParameter("terminologyId", identity.getTerminologyId());
      query.setParameter("ownerId", identity.getOwnerId());
      query.setParameter("ownerType", identity.getOwnerType());
      query.setParameter("ownerQualifier", identity.getOwnerQualifier());
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

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(
    String hashCode) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get semanticTypeComponent identity "
            + hashCode);

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from SemanticTypeComponentIdentity a "
              + "where hashCode = :hashCode");
      query.setParameter("hashCode", hashCode);

      return (SemanticTypeComponentIdentity) query.getSingleResult();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public Long getNextSemanticTypeComponentId() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get next semanticTypeComponent id");

    Long attId = null;
    Long styId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AttributeIdentity a ");
      attId = (Long) query.getSingleResult();
    } catch (NoResultException e) {
      attId = 1L;
    }
    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select max(a.id) from SemanticTypeComponentIdentity a ");
      styId = (Long) query.getSingleResult();
    } catch (NoResultException e) {
      styId = 1L;
    }
    // Return the max
    return Math.max(attId, styId);
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
          manager.createQuery("select a from SemanticTypeComponentIdentity a "
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

}
