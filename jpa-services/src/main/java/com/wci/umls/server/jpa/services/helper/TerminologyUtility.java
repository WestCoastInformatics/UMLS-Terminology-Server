/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.services.MetadataService;

/**
 * Loads and serves configuration.
 */
public class TerminologyUtility {

  /** The additional type hierarchy. */
  public static Map<String, Set<String>> additionalTypeHierarchy =
      new HashMap<>();

  /**
   * Returns the active parent concepts.
   *
   * @param concept the concept
   * @return the active parent concepts
   * @throws Exception the exception
   */
  public static List<Concept> getActiveParentConcepts(Concept concept)
    throws Exception {
    if (concept == null) {
      throw new Exception(
          "Unexpected null concept passed to getActiveParentConcepts.");
    }
    final List<Concept> results = new ArrayList<>();
    for (ConceptRelationship rel : concept.getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete()) {
        results.add(rel.getTo());
      }
    }
    return results;
  }

  /**
   * Returns the active parent descriptors.
   *
   * @param descriptor the descriptor
   * @return the active parent descriptors
   * @throws Exception the exception
   */
  public static List<Descriptor> getActiveParentDescriptors(
    Descriptor descriptor) throws Exception {
    if (descriptor == null) {
      throw new Exception(
          "Unexpected null descriptor passed to getActiveParentDescriptors.");
    }
    final List<Descriptor> results = new ArrayList<>();
    for (DescriptorRelationship rel : descriptor.getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete()) {
        results.add(rel.getTo());
      }
    }
    return results;
  }

  /**
   * Returns the descendant types.
   *
   * @param typeValue the type value
   * @param terminology the terminology
   * @param version the version
   * @return the descendant types
   * @throws Exception the exception
   */
  public static Set<String> getDescendantTypes(String typeValue,
    String terminology, String version) throws Exception {
    if (additionalTypeHierarchy.isEmpty()) {
      MetadataService service = new MetadataServiceJpa();
      for (AdditionalRelationshipType type : service
          .getAdditionalRelationshipTypes(terminology, version).getObjects()) {
        additionalTypeHierarchy.put(
            terminology + version + type.getAbbreviation(),
            new HashSet<String>());
      }
      for (AdditionalRelationshipType type : service
          .getAdditionalRelationshipTypes(terminology, version).getObjects()) {
        while (type.getSuperType() != null) {
          additionalTypeHierarchy.get(
              terminology + version + type.getSuperType().getAbbreviation())
              .add(type.getAbbreviation());
          type = type.getSuperType();
        }
      }
      service.close();
      Logger.getLogger(TerminologyUtility.class).info(
          "  Additional descendant type map - " + additionalTypeHierarchy);
    }
    return additionalTypeHierarchy.get(terminology + version + typeValue);
  }

  /**
   * Returns the active parent codes.
   *
   * @param code the code
   * @return the active parent codes
   * @throws Exception the exception
   */
  public static List<Code> getActiveParentCodes(Code code) throws Exception {
    if (code == null) {
      throw new Exception(
          "Unexpected null code passed to getActiveParentCodes.");
    }
    final List<Code> results = new ArrayList<>();
    for (CodeRelationship rel : code.getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete()) {
        results.add(rel.getTo());
      }
    }
    return results;
  }

  /**
   * Gets the UUID for a string using the null namespace.
   * 
   * @param value to make a UUID from
   * @return the the UUID
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public static UUID getUuid(String value) throws NoSuchAlgorithmException,
    UnsupportedEncodingException {

    MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");

    String namespace = "00000000-0000-0000-0000-000000000000";
    String encoding = "UTF-8";
    if (value == null) {
      return UUID.fromString(namespace);
    }
    UUID namespaceUUID = UUID.fromString(namespace);

    // Generate the digest.
    sha1Algorithm.reset();

    // Generate the digest.
    sha1Algorithm.reset();
    if (namespace != null) {
      sha1Algorithm.update(getRawBytes(namespaceUUID));
    }

    sha1Algorithm.update(value.getBytes(encoding));
    byte[] sha1digest = sha1Algorithm.digest();

    sha1digest[6] &= 0x0f; /* clear version */
    sha1digest[6] |= 0x50; /* set to version 5 */
    sha1digest[8] &= 0x3f; /* clear variant */
    sha1digest[8] |= 0x80; /* set to IETF variant */

    long msb = 0;
    long lsb = 0;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (sha1digest[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (sha1digest[i] & 0xff);
    }

    return new UUID(msb, lsb);

  }

  /**
   * Returns the raw bytes for the UUID.
   * 
   * @param uuid the uuid
   * @return the raw bytes
   */
  private static byte[] getRawBytes(UUID uuid) {
    String id = uuid.toString();
    byte[] rawBytes = new byte[16];

    for (int i = 0, j = 0; i < 36; ++j) {
      // Need to bypass hyphens:
      switch (i) {
        case 8:
        case 13:
        case 18:
        case 23:
          ++i;
          break;
        default:
          break;
      }
      char c = id.charAt(i);

      if (c >= '0' && c <= '9') {
        rawBytes[j] = (byte) ((c - '0') << 4);
      } else if (c >= 'a' && c <= 'f') {
        rawBytes[j] = (byte) ((c - 'a' + 10) << 4);
      }

      c = id.charAt(++i);

      if (c >= '0' && c <= '9') {
        rawBytes[j] |= (byte) (c - '0');
      } else if (c >= 'a' && c <= 'f') {
        rawBytes[j] |= (byte) (c - 'a' + 10);
      }
      ++i;
    }
    return rawBytes;
  }

}
