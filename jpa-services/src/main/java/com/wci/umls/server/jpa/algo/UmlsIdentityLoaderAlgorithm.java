/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.meta.AtomIdentityJpa;
import com.wci.umls.server.jpa.meta.AttributeIdentityJpa;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
import com.wci.umls.server.jpa.meta.StringClassIdentityJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.UmlsIdentityService;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class UmlsIdentityLoaderAlgorithm
    extends AbstractTerminologyLoaderAlgorithm {

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
      // id|terminologyId|terminology|ownerId|ownerType|ownerQualifier|hashcode
      //
      if (new File(getInputPath(), "attributeIdentity.txt").exists()) {
        logInfo("  Load attribute identity");

        final BufferedReader in = new BufferedReader(
            new FileReader(new File(getInputPath(), "attributeIdentity.txt")));
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
          identity.setComponentId(fields[3]);
          identity.setComponentType(IdType.valueOf(fields[4]));
          identity.setComponentTerminology(fields[5]);
          identity.setHashCode(fields[6]);
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

        final BufferedReader in = new BufferedReader(new FileReader(
            new File(getInputPath(), "semanticTypeComponentIdentity.txt")));
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

      //
      // Handle AtomIdentity
      // id|stringClassId|terminology|terminologyId|termType|code|conceptId|descriptorId
      //
      if (new File(getInputPath(), "atomIdentity.txt").exists()) {
        logInfo("  Load atom identity");

        final BufferedReader in = new BufferedReader(
            new FileReader(new File(getInputPath(), "atomIdentity.txt")));
        String line;
        int ct = 0;
        while ((line = in.readLine()) != null) {
          if (isCancelled()) {
            in.close();
            return;
          }
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final AtomIdentity identity = new AtomIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setStringClassId(fields[1]);
          identity.setTerminology(fields[2]);
          identity.setTerminologyId(fields[3]);
          identity.setTermType(fields[4]);
          identity.setCodeId(fields[5]);
          identity.setConceptId(fields[6]);
          identity.setDescriptorId(fields[7]);
          service.addAtomIdentity(identity);
          if (++ct % commitCt == 0) {
            service.commitClearBegin();
          }
        }
        in.close();
        service.commitClearBegin();
        logInfo("    count = " + ct);
      }

      //
      // Handle StringClassIdentity
      // id|name|language
      //
      if (new File(getInputPath(), "stringIdentity.txt").exists()) {
        logInfo("  Load string identity");

        final BufferedReader in = new BufferedReader(
            new FileReader(new File(getInputPath(), "stringIdentity.txt")));
        String line;
        int ct = 0;
        while ((line = in.readLine()) != null) {
          if (isCancelled()) {
            in.close();
            return;
          }
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final StringClassIdentity identity = new StringClassIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setName(fields[1]);
          identity.setLanguage(fields[2]);
          service.addStringClassIdentity(identity);
          if (++ct % commitCt == 0) {
            service.commitClearBegin();
          }
        }
        in.close();
        service.commitClearBegin();
        logInfo("    count = " + ct);
      }

      //
      // Handle LexicalClassIdentity
      // id|normalizedName
      //
      if (new File(getInputPath(), "lexicalClassIdentity.txt").exists()) {
        logInfo("  Load lexicalClass identity");

        final BufferedReader in = new BufferedReader(new FileReader(
            new File(getInputPath(), "lexicalClassIdentity.txt")));
        String line;
        int ct = 0;
        while ((line = in.readLine()) != null) {
          if (isCancelled()) {
            in.close();
            return;
          }
          final String[] fields = FieldedStringTokenizer.split(line, "|");
          final LexicalClassIdentity identity = new LexicalClassIdentityJpa();
          identity.setId(Long.valueOf(fields[0]));
          identity.setNormalizedName(fields[1]);
          service.addLexicalClassIdentity(identity);
          if (++ct % commitCt == 0) {
            service.commitClearBegin();
          }
        }
        in.close();
        service.commitClearBegin();
        logInfo("    count = " + ct);
      }

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
