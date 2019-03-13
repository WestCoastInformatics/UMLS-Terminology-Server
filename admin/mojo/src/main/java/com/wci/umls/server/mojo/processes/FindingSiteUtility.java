package com.wci.umls.server.mojo.processes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.rest.client.ContentClientRest;

public class FindingSiteUtility {

  private NeoplasmConceptSearcher conceptSearcher;

  /** The finding site potential terms map cache. */
  protected Map<String, Map<SctNeoplasmConcept, Set<String>>> findingSitePotentialTermsMapCache =
      new HashMap<>();

  /** The non finding site strings. */
  final static protected List<String> nonFindingSiteStrings = Arrays.asList("of", "part",
      "structure", "system", "and/or", "and", "region", "area", "or", "the");

  /** The top level body structure ids. */
  final protected List<String> topLevelBodyStructureIds =
      Arrays.asList("86762007", "20139000", "39937001", "81745001", "387910009", "127882003",
          "64033007", "117590005", "21514008", "76752008", "113331007", "363667005", "31610004");

  protected ContentClientRest client;

  protected String sourceTerminology;

  protected String sourceVersion;

  protected String targetTerminology;

  protected String targetVersion;

  protected String authToken;

  protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();

  public FindingSiteUtility(ContentClientRest contentClient, String st, String sv, String tt,
      String tv, String token) {
    client = contentClient;
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;
    authToken = token;
  }

  /**
   * Based on a finding site, identify all the finding site's ancestors up to
   * levels specified by topLevelBodyStructureIds.
   *
   * @param findingSites the finding sites
   * @return the sets the
   * @throws Exception the exception
   */
  public Set<SctNeoplasmConcept> identifyPotentialFSConcepts(Set<String> findingSites)
    throws Exception {
    Set<SctNeoplasmConcept> retConcepts = new HashSet<>();

    for (String site : findingSites) {
      // Get the finding site as a concept
      SctNeoplasmConcept fsConcept = conceptSearcher.getSctConceptFromDesc(site);
      retConcepts.add(fsConcept);

      if (findingSitePotentialTermsMapCache.containsKey(fsConcept.getConceptId())) {
        return retConcepts;
      }

      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms = new HashMap<>();
      findingSitePotentialTermsMapCache.put(fsConcept.getConceptId(), potentialFSConTerms);

      if (topLevelBodyStructureIds.contains(fsConcept.getConceptId())) {
        SctNeoplasmConcept mapCon = null;
        if (NeoplasmConceptSearcher.canPopulateFromFiles) {
          mapCon = conceptSearcher.populateSctConcept(fsConcept.getConceptId(), null);
        } else {
          Concept c = client.getConcept(fsConcept.getConceptId(), sourceTerminology, sourceVersion,
              null, authToken);
          mapCon = conceptSearcher.populateSctConcept(c.getTerminologyId(), null);
        }

        Set<String> bucket = new HashSet<>();
        potentialFSConTerms.put(mapCon, bucket);
      } else {
        // Get all fsCon's ancestors
        String topLevelSctId = null;
        final ConceptList ancestorResults = client.findAncestorConcepts(fsConcept.getConceptId(),
            sourceTerminology, sourceVersion, false, pfsLimitless, authToken);

        // Find the body structure hierarchy it falls under
        for (Concept ancestor : ancestorResults.getObjects()) {
          if (topLevelBodyStructureIds.contains(ancestor.getTerminologyId())) {
            topLevelSctId = ancestor.getTerminologyId();
            break;
          }
        }

        // Have list of possibleFindingSites. Test them for matches
        if (topLevelSctId == null) {
          System.out.println(
              "ERROR ERROR ERROR: Found a finding site without an identified top level BS ancestor: "
                  + fsConcept.getConceptId() + "---" + fsConcept.getName());
          return null;
        }

        // TODO: Because can't do ancestors via ECL, need this work around
        // Identify all descendants of top level bodyStructure concept
        pfsLimitless.setExpression("<< " + topLevelSctId);
        final SearchResultList descendentResults =
            client.findConcepts(sourceTerminology, sourceVersion, null, pfsLimitless, authToken);
        pfsLimitless.setExpression(null);

        // Create a list of concepts that are both ancestors of fsConcept and
        // descendents of topLevelBodyStructure Concept
        // TODO: This could be a Rest Call in of itself
        for (Concept ancestor : ancestorResults.getObjects()) {

          for (SearchResult potentialFindingSite : descendentResults.getObjects()) {
            if (ancestor.getTerminologyId().equals(potentialFindingSite.getTerminologyId())) {
              SctNeoplasmConcept mapCon = null;
              if (NeoplasmConceptSearcher.canPopulateFromFiles) {
                mapCon = conceptSearcher.populateSctConcept(ancestor.getTerminologyId(), null);
              } else {
                Concept c = client.getConcept(ancestor.getTerminologyId(), sourceTerminology,
                    sourceVersion, null, authToken);
                mapCon = conceptSearcher.populateSctConcept(c.getTerminologyId(), null);
              }

              Set<String> bucket = new HashSet<>();
              potentialFSConTerms.put(mapCon, bucket);
              break;
            }
          }
        }
      }

      for (SctNeoplasmConcept testCon : potentialFSConTerms.keySet()) {
        for (SctNeoplasmDescription desc : testCon.getDescs()) {
          String normalizedStr = desc.getDescription().toLowerCase();
          for (String s : nonFindingSiteStrings) {
            normalizedStr = normalizedStr.replaceAll("\\b" + s + "s" + "\\b", " ").trim();
            normalizedStr = normalizedStr.replaceAll("\\b" + s + "\\b", " ").trim();
          }

          normalizedStr = normalizedStr.replaceAll(" {2,}", " ").trim();

          if (!potentialFSConTerms.get(testCon).contains(normalizedStr)) {
            potentialFSConTerms.get(testCon).add(normalizedStr);
          }
        }
      }
    }

    return retConcepts;
  }

  public void setConceptSearcher(NeoplasmConceptSearcher searcher) {
    conceptSearcher = searcher;
  }

  public List<String> getNonFindingSiteStrings() {
    return nonFindingSiteStrings;
  }

  public Map<String, Map<SctNeoplasmConcept, Set<String>>> getFindingSitePotentialTermsMapCache() {
    return findingSitePotentialTermsMapCache;
  }

}
