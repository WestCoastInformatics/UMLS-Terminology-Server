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
    logInfo("  inputPath = " + getInputPath());
    fireProgressEvent(0, "Starting...");

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      service.setTransactionPerOperation(false);
      service.beginTransaction();

      //
      // Handle AttributeIdentity
      // id|terminologyId|terminology|ownerId|ownerType|ownerQualifier|name|hashcode
      //
      if (new File(getInputPath(), "attributeIdentity.txt").exists()) {
        logInfo("  Load attribute identity");

        final BufferedReader in =
            new BufferedReader(new FileReader(new File(getInputPath(),
                "attributeIdentity.txt")));
        String line;
        int ct = 0;
        while ((line = in.readLine()) != null) {
          if (isCancelled()) {
            in.close();
            return;
          }
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final AttributeIdentity identity = new AttributeIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setTerminologyId(fields[1]);
          identity.setTerminology(fields[2]);
          identity.setOwnerId(fields[3]);
          identity.setOwnerType(IdType.valueOf(fields[4]));
          identity.setOwnerQualifier(fields[5]);
          identity.setName(fields[6]);
          identity.setHashCode(fields[7]);
          service.addAttributeIdentity(identity);
          if (++ct % commitCt == 0) {
            service.commitClearBegin();
          }
        }
        in.close();
        service.commitClearBegin();
        logInfo("    count = " + ct);
      }

      //
      // Handle SemanticTypeIdentity
      // id|conceptTerminologyId|terminology|semanticType
      //
      if (new File(getInputPath(), "semanticTypeComponentIdentity.txt")
          .exists()) {
        logInfo("  Load semanticType identity");

        final BufferedReader in =
            new BufferedReader(new FileReader(new File(getInputPath(),
                "semanticTypeComponentIdentity.txt")));
        String line;
        int ct = 0;
        while ((line = in.readLine()) != null) {
          if (isCancelled()) {
            in.close();
            return;
          }
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final SemanticTypeComponentIdentity identity =
              new SemanticTypeComponentIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setConceptTerminologyId(fields[1]);
          identity.setTerminology(fields[2]);
          identity.setSemanticType(fields[3]);
          service.addSemanticTypeComponentIdentity(identity);
        }
        if (++ct % commitCt == 0) {
          service.commitClearBegin();
        }
        service.commitClearBegin();
        in.close();
        logInfo("    count = " + ct);
      }

      // TODO: AtomIdentity, etc.

      service.commit();
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
