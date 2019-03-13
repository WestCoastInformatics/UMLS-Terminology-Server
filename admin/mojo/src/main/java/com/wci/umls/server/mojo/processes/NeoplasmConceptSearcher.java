package com.wci.umls.server.mojo.processes;

import java.util.HashMap;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.rest.client.ContentClientRest;

public class NeoplasmConceptSearcher {

  /** The acronym expansion map. */
  static private HashMap<String, SctNeoplasmConcept> conceptsFromDescsCache = new HashMap<>();

  static public boolean canPopulateFromFiles = false;

  static private SctRelationshipParser relParser;

  static private SctNeoplasmDescriptionParser descParser;

  protected ContentClientRest client;

  protected String sourceTerminology;

  protected String sourceVersion;

  protected String targetTerminology;

  protected String targetVersion;

  protected String authToken;

  /** The pfs limited. */
  protected PfsParameterJpa pfsLimited = new PfsParameterJpa();

  /** The pfs minimal. */
  protected PfsParameterJpa pfsMinimal = new PfsParameterJpa();

  protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();


  public void setup(ContentClientRest contentClient, String st,
      String sv, String tt, String tv, String token) {
    pfsMinimal.setStartIndex(0);
    pfsMinimal.setMaxResults(5);
    pfsLimited.setStartIndex(0);
    pfsLimited.setMaxResults(30);

    client = contentClient;
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;
    authToken = token;
  }

  /**
   * Identifies the concept based on a provided description.
   *
   * @param desc the desc
   * @return the sct concept from desc
   * @throws Exception the exception
   */
  public SctNeoplasmConcept getSctConceptFromDesc(String desc) throws Exception {
    if (canPopulateFromFiles) {
      String conId = descParser.getConIdFromDesc(desc);
      return populateSctConcept(conId, desc);
    } else {
      if (!conceptsFromDescsCache.containsKey(desc)) {
        final SearchResultList possibleMatches = client.findConcepts(sourceTerminology,
            sourceVersion, "\"" + desc + "\"", pfsMinimal, authToken);

        for (SearchResult result : possibleMatches.getObjects()) {
          if (!result.isObsolete()) {
            SctNeoplasmConcept retConcept =
                populateSctConcept(result.getTerminologyId(), result.getValue());
            conceptsFromDescsCache.put(desc, retConcept);
          }
        }
      }
    }

    return conceptsFromDescsCache.get(desc);
  }

  /**
   * Populate neoplasm sct concept.
   *
   * @param result the result
   * @return the sct neoplasm concept
   * @throws Exception the exception
   */
  public SctNeoplasmConcept populateSctConcept(String conId, String name) throws Exception {
    SctNeoplasmConcept con = new SctNeoplasmConcept(conId, name);
    populateRelationships(con);
    populateDescriptions(con);

    if (name == null) {
      con.setName(con.getDescs().iterator().next().getDescription());
    }

    return con;
  }

  /**
   * Populate neoplasm relationships.
   *
   * @param con the con
   * @throws Exception the exception
   */
  public void populateRelationships(SctNeoplasmConcept con) throws Exception {

    if (canPopulateFromFiles) {
      con.setRels(relParser.getRelationships(con));
    } else {
      RelationshipList relsList = client.findConceptRelationships(con.getConceptId(),
          sourceTerminology, sourceVersion, null, pfsLimitless, authToken);

      for (final Relationship<?, ?> relResult : relsList.getObjects()) {
        SctRelationship rel = relParser.parse(con.getName(), relResult);
        if (rel != null) {
          con.getRels().add(rel);
        }
      }
    }
  }

  /**
   * Populate neoplasm descriptions.
   *
   * @param con the con
   * @throws Exception the exception
   */
  public void populateDescriptions(SctNeoplasmConcept con) throws Exception {
    if (canPopulateFromFiles) {
      con.setDescs(descParser.getDescriptions(con));
    } else {
      Concept fullCon =
          client.getConcept(con.getConceptId(), sourceTerminology, sourceVersion, null, authToken);

      for (final Atom atom : fullCon.getAtoms()) {
        if (isValidDescription(atom)) {
          SctNeoplasmDescription desc = new SctNeoplasmDescription();
          desc.setDescription(atom.getName());
          con.getDescs().add(desc);
        }
      }
    }
  }

  /**
   * Indicates whether or not valid description for analysis purposes.
   *
   * @param atom the atom
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isValidDescription(Atom atom) {
    return (!atom.isObsolete() && !atom.getTermType().equals("Fully specified name")
        && !atom.getTermType().equals("Definition"));
  }

  public static void setDescParser(SctNeoplasmDescriptionParser dp) {
    descParser = dp;
  }

  public static void setRelParser(SctRelationshipParser rp) {
    relParser = rp;
  }

}
