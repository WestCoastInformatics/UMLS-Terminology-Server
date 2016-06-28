/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.meta.AttributeIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.UmlsIdentityService;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class UmlsIdentityLoaderAlgorithm extends
    AbstractTerminologyLoaderAlgorithm {

  /**
   * Instantiates an empty {@link UmlsIdentityLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public UmlsIdentityLoaderAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Umls Identity Loader");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputPath = " + getInputPath());
    fireProgressEvent(0, "Starting...");

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      //
      // Handle AttributeIdentity
      // id|terminologyId|terminology|ownerId|ownerType|ownerQualifier|hashcode
      //
      if (new File(getInputPath(), "attributeIdentity.txt").exists()) {

        final BufferedReader in =
            new BufferedReader(new FileReader(new File(getInputPath(),
                "attributeIdentity.txt")));
        String line;
        while ((line = in.readLine()) != null) {
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final AttributeIdentity identity = new AttributeIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setTerminologyId(fields[1]);
          identity.setTerminology(fields[2]);
          identity.setOwnerId(fields[3]);
          identity.setOwnerType(IdType.valueOf(fields[4]));
          identity.setOwnerQualifier(fields[5]);
          identity.setHashCode(fields[6]);
          service.addAttributeIdentity(identity);
        }
        in.close();
      }

      //
      // Handle SemanticTypeIdentity
      // id|conceptTerminologyId|terminology|semanticType
      //
      if (new File(getInputPath(), "semanticTypeComponentIdentity.txt")
          .exists()) {

        final BufferedReader in =
            new BufferedReader(new FileReader(new File(getInputPath(),
                "semanticTypeComponentIdentity.txt")));
        String line;
        while ((line = in.readLine()) != null) {
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final SemanticTypeComponentIdentity identity =
              new SemanticTypeComponentIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setConceptTerminologyId(fields[1]);
          identity.setTerminology(fields[2]);
          identity.setSemanticType(fields[3]);
          service.addSemanticTypeComponentIdentity(identity);
        }
        in.close();
      }

      // TODO: AtomIdentity, etc.

      fireProgressEvent(0, "Finished...");
    } catch (Exception e) {
      logError("FAILED to assign identity");
      throw e;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    return new RrfFileSorter().getFileVersion(new File(getInputPath()));
  }

  /* see superclass */
  @Override
  public void computeTransitiveClosures() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void computeTreePositions() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void computeExpressionIndexes() throws Exception {
    // n/a
  }

}
