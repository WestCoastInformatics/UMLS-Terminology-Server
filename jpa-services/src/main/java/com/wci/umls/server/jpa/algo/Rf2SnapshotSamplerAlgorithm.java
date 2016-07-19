/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class Rf2SnapshotSamplerAlgorithm implements Algorithm {

  /** The input path. */
  protected String inputPath = null;

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The output concepts. */
  private Set<String> outputConcepts;

  /** The output descriptions. */
  private Set<String> outputDescriptions;

  /** The input concepts. */
  private Set<String> inputConcepts;

  /** The readers. */
  private Rf2Readers readers;

  /** The keep inferred. */
  private boolean keepInferred = false;

  /** The keep descendants. */
  private boolean keepDescendants = false;

  /** The chd par map. */
  Map<String, Set<String>> chdParMap = new HashMap<>();

  /** The chd par map. */
  Map<String, Set<String>> parChdMap = new HashMap<>();

  /** The other map. */
  Map<String, Set<String>> otherMap = new HashMap<>();

  /**
   * Instantiates an empty {@link Rf2SnapshotSamplerAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2SnapshotSamplerAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(Rf2Readers readers) {
    this.readers = readers;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    try {
      Logger.getLogger(getClass()).info("Start sampling snapshot");

      // load relationships
      Logger.getLogger(getClass()).info("  Load relationships");
      loadRelationshipMaps();

      Logger.getLogger(getClass()).info(
          "    chdPar count = " + chdParMap.size());
      // Logger.getLogger(getClass()).info("    chdPar = " + chdParMap);
      Logger.getLogger(getClass()).info("    other count = " + otherMap.size());
      // Logger.getLogger(getClass()).info("    other = " + otherMap);

      Logger.getLogger(getClass()).info("  Find initial concepts");
      // 1. Find initial concepts
      Set<String> concepts = new HashSet<>();
      Set<String> descriptions = new HashSet<>();
      concepts.addAll(inputConcepts);
      Logger.getLogger(getClass()).info("    count = " + concepts.size());

      // 1b. Get descendants if indicated
      if (keepDescendants) {
        for (final String concept : new HashSet<>(concepts)) {
          Set<String> desc = getDescendantsHelper(concept);
          Logger.getLogger(getClass()).info("    desc count = " + desc.size());
          concepts.addAll(desc);
        }
      }

      // 2. Find other related concepts
      Logger.getLogger(getClass()).info("  Add distance 1 related concepts");
      for (final String concept : new HashSet<>(concepts)) {
        if (otherMap.get(concept) != null) {
          Logger.getLogger(getClass()).info(
              "    add concepts = " + otherMap.get(concept));
          concepts.addAll(otherMap.get(concept));
        }
      }
      Logger.getLogger(getClass()).info("    count = " + concepts.size());

      int prevCt = -1;
      do {
        prevCt = concepts.size();

        readers.closeReaders();
        readers.openReaders();

        // 3. Find metadata concepts (definitionStatusId, typeId,
        Logger.getLogger(getClass()).info("  Get metadata concepts");
        addConceptMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after concepts) = " + concepts.size());

        addDescriptionMetadata(concepts, descriptions);
        Logger.getLogger(getClass()).info(
            "    count (after descriptions) = " + concepts.size());
        Logger.getLogger(getClass()).info(
            "    count of descriptions (after descriptions) = "
                + descriptions.size());

        addRelationshipMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after relationships) = " + concepts.size());

        addAttributeValueMetadata(concepts, descriptions);
        Logger.getLogger(getClass()).info(
            "    count (after attribute value) = " + concepts.size());

        addAssociationReferenceMetadata(concepts, descriptions);
        Logger.getLogger(getClass()).info(
            "    count (after association reference) = " + concepts.size());

        addSimpleMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after simple) = " + concepts.size());

        addSimpleMapMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after simple map) = " + concepts.size());

        addComplexMapMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after complex map) = " + concepts.size());

        addLanguageMetadata(concepts, descriptions);
        Logger.getLogger(getClass()).info(
            "    count (after language) = " + concepts.size());

        addMetadataMetadata(concepts);
        Logger.getLogger(getClass()).info(
            "    count (after metadata) = " + concepts.size());

        // 4. Find all concepts on path to root (e.g. walk up ancestors)
        for (final String chd : chdParMap.keySet()) {
          if (concepts.contains(chd)) {
            concepts.addAll(chdParMap.get(chd));
          }
        }
        Logger.getLogger(getClass()).info(
            "    count (after ancestors) = " + concepts.size());
        Logger.getLogger(getClass()).info("    prev count = " + prevCt);

      } while (concepts.size() != prevCt);

      // Set output concepts
      outputConcepts = concepts;
      outputDescriptions = descriptions;

      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Adds the concept metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addConceptMetadata(Set<String> concepts) throws Exception {

    String line = "";
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.CONCEPT);
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) { // header

        // Add definition status id
        if (concepts.contains(fields[0])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
        }
      }
    }

  }

  /**
   * Adds the description metadata.
   *
   * @param concepts the concepts
   * @param descriptions the descriptions
   * @throws Exception the exception
   */
  private void addDescriptionMetadata(Set<String> concepts,
    Set<String> descriptions) throws Exception {

    String line = "";
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION);
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) {

        // If concept id matches, add description metadata
        if (concepts.contains(fields[4])) {
          descriptions.add(fields[0]);
          concepts.add(fields[3]);
          concepts.add(fields[6]);
          concepts.add(fields[8]);
        }

      }
    }
  }

  /**
   * Adds the relationship metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addRelationshipMetadata(Set<String> concepts) throws Exception {
    String line = "";
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.RELATIONSHIP);
    // Iterate over relationships
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header
      if (!fields[0].equals("id")) {

        // check keep inferred logic
        if (!keepInferred && fields[8].equals("900000000000011006")) {
          continue;
        }

        // Add metadata for matching entries - both concepts must be present
        if (concepts.contains(fields[4]) && concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[7]);
          concepts.add(fields[8]);
          concepts.add(fields[9]);
        }
      }
    }
  }

  /**
   * Adds the attribute value metadata.
   *
   * @param concepts the concepts
   * @param descriptions the descriptions
   * @throws Exception the exception
   */
  private void addAttributeValueMetadata(Set<String> concepts,
    Set<String> descriptions) throws Exception {

    String line = "";

    // Iterate through attribute value entries
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.ATTRIBUTE_VALUE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) { // header

        // Add metadata if attached to description or concept
        if (concepts.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
          // value id
          concepts.add(fields[6]);
        }
        if (descriptions.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
          // value id
          descriptions.add(fields[6]);
        }
      }
    }
  }

  /**
   * Adds the association reference metadata.
   *
   * @param concepts the concepts
   * @param descriptions the descriptions
   * @throws Exception the exception
   */
  private void addAssociationReferenceMetadata(Set<String> concepts,
    Set<String> descriptions) throws Exception {

    String line = "";

    // Iterate through attribute value entries
    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.ASSOCIATION_REFERENCE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) { // header

        // Add metadata if attached to description or concept
        if (concepts.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
          // targetComponent id
          concepts.add(fields[6]);
        }
        if (descriptions.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
          // target component id
          descriptions.add(fields[6]);
        }
      }
    }
  }

  /**
   * Adds the simple metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addSimpleMetadata(Set<String> concepts) throws Exception {

    String line = "";

    // Iterate through attribute value entries
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) { // header

        // Add metadata if attached to description or concept
        if (concepts.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
        }
      }
    }
  }

  /**
   * Adds the simple map metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addSimpleMapMetadata(Set<String> concepts) throws Exception {

    String line = "";

    // Iterate through attribute value entries
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE_MAP);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) { // header

        // Add metadata if attached to description or concept
        if (concepts.contains(fields[5])) {
          // module id
          concepts.add(fields[3]);
          // refset id
          concepts.add(fields[4]);
        }
      }
    }
  }

  /**
   * Adds the complex map metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addComplexMapMetadata(Set<String> concepts) throws Exception {

    String line = "";

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.COMPLEX_MAP);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) {

        if (concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
          concepts.add(fields[11]);
        }
      }
    }

    // handle extended too

    reader = readers.getReader(Rf2Readers.Keys.EXTENDED_MAP);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) {

        if (concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
          concepts.add(fields[11]);
        }
      }
    }
  }

  /**
   * Adds the language metadata.
   *
   * @param concepts the concepts
   * @param descriptions the descriptions
   * @throws Exception the exception
   */
  private void addLanguageMetadata(Set<String> concepts,
    Set<String> descriptions) throws Exception {

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.LANGUAGE);
    String line;
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals("id")) {

        if (descriptions.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
          concepts.add(fields[6]);
        }
      }
    }
  }

  /**
   * Adds the language metadata.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  private void addMetadataMetadata(Set<String> concepts) throws Exception {

    String line = "";

    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.MODULE_DEPENDENCY);
    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals("id")) {

        if (concepts.contains(fields[3]) || concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
        }
      }
    }

    reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION_TYPE);
    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals("id")) {

        if (concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
          concepts.add(fields[6]);
        }
      }
    }

    reader = readers.getReader(Rf2Readers.Keys.REFSET_DESCRIPTOR);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals("id")) {

        if (concepts.contains(fields[5])) {
          concepts.add(fields[3]);
          concepts.add(fields[4]);
          concepts.add(fields[6]);
          concepts.add(fields[7]);
        }
      }
    }

  }

  /**
   * Load relationship maps.
   *
   * @throws Exception the exception
   */
  private void loadRelationshipMaps() throws Exception {
    String line = "";
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.RELATIONSHIP);
    // Iterate over relationships
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header and keep only active entries
      if (!fields[0].equals("id") && fields[2].equals("1")) {

        // check keep inferred logic
        if (!keepInferred && fields[8].equals("900000000000011006")) {
          continue;
        }
        // Configure relationship
        final ConceptRelationship rel = new ConceptRelationshipJpa();
        rel.setTerminologyId(fields[0]);
        rel.setObsolete(!fields[2].equals("1"));
        rel.setRelationshipType(fields[7]); // typeId
        rel.setHierarchical(rel.getRelationshipType().equals("116680003"));
        rel.setInferred(fields[8].equals("900000000000011006"));
        rel.setStated(fields[8].equals("900000000000010007"));
        // get concepts from cache, they just need to have ids
        final Concept sourceConcept = new ConceptJpa();
        sourceConcept.setTerminologyId(fields[4]);
        rel.setFrom(sourceConcept);
        final Concept destinationConcept = new ConceptJpa();
        destinationConcept.setTerminologyId(fields[5]);
        rel.setTo(destinationConcept);

        // active inferred isa
        if (rel.getRelationshipType().equals("116680003")
            && (rel.isInferred() || !keepInferred) && !rel.isObsolete()) {
          if (!chdParMap.containsKey(rel.getFrom().getTerminologyId())) {
            chdParMap.put(rel.getFrom().getTerminologyId(),
                new HashSet<String>());
          }
          chdParMap.get(rel.getFrom().getTerminologyId()).add(
              rel.getTo().getTerminologyId());

          if (!parChdMap.containsKey(rel.getTo().getTerminologyId())) {
            parChdMap
                .put(rel.getTo().getTerminologyId(), new HashSet<String>());
          }
          parChdMap.get(rel.getTo().getTerminologyId()).add(
              rel.getFrom().getTerminologyId());
        }

        // active, not isa => other
        else if (!rel.isObsolete()
            && !rel.getRelationshipType().equals("116680003")) {
          if (!otherMap.containsKey(rel.getFrom().getTerminologyId())) {
            otherMap.put(rel.getFrom().getTerminologyId(),
                new HashSet<String>());
          }
          otherMap.get(rel.getFrom().getTerminologyId()).add(
              rel.getTo().getTerminologyId());

        }
      }
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   *
   * @param pct percent done
   * @param note progress note
   * @throws Exception the exception
   */
  public void fireProgressEvent(int pct, String note) throws Exception {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    throw new UnsupportedOperationException("cannot cancel.");
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    readers = null;
  }

  /**
   * Returns the output concepts.
   *
   * @return the output concepts
   */
  public Set<String> getOutputConcepts() {
    return outputConcepts;
  }

  /**
   * Returns the output descriptions.
   *
   * @return the output descriptions
   */
  public Set<String> getOutputDescriptions() {
    return outputDescriptions;
  }

  /**
   * Returns the input concepts.
   *
   * @return the input concepts
   */
  public Set<String> getInputConcepts() {
    return inputConcepts;
  }

  /**
   * Sets the input concepts.
   *
   * @param inputConcepts the input concepts
   */
  public void setInputConcepts(Set<String> inputConcepts) {
    this.inputConcepts = inputConcepts;
  }

  /**
   * Sets the keep inferred.
   *
   * @param keepInferred the keep inferred
   */
  public void setKeepInferred(boolean keepInferred) {
    this.keepInferred = keepInferred;
  }

  /**
   * Sets the keep descendants.
   *
   * @param keepDescendants the keep descendants
   */
  public void setKeepDescendants(boolean keepDescendants) {
    this.keepDescendants = keepDescendants;
  }

  /**
   * Sets the input path.
   *
   * @param inputPath the input path
   */
  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  /**
   * Returns the descendant concepts.
   *
   * @param concept the concept
   * @return the descendant concepts
   */
  public Set<String> getDescendantsHelper(String concept) {
    final Set<String> descendants = new HashSet<>();
    if (!parChdMap.containsKey(concept)) {
      return descendants;
    }
    for (final String chd : parChdMap.get(concept)) {
      descendants.addAll(getDescendantsHelper(chd));
    }
    return descendants;
  }

}
