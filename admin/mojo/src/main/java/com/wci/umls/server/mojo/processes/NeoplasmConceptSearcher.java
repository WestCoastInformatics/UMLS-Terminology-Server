package com.wci.umls.server.mojo.processes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  static private Map<String, SctNeoplasmConcept> neoplasmConcepts = null;

  public void setup(ContentClientRest contentClient, String st, String sv, String tt, String tv,
    String token) {
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

    return populateSctConcept(con);
  }

  /**
   * Populate neoplasm sct concept.
   *
   * @param result the result
   * @return the sct neoplasm concept
   * @throws Exception the exception
   */
  public SctNeoplasmConcept populateSctConcept(SctNeoplasmConcept con) throws Exception {
    populateDescriptions(con);
    String conName =  con.getDescs().iterator().next().getDescription();
    populateRelationships(con, conName);

    if (con.getName() == null) {
      con.setName(conName);
    }

    return con;
  }

  /**
   * Populate neoplasm relationships.
   *
   * @param con the con
   * @param desc 
   * @throws Exception the exception
   */
  public void populateRelationships(SctNeoplasmConcept con, String desc) throws Exception {
      con.setRels(relParser.getRelationships(con));
      
      if (con.getRels() == null) {
        con.setRels(new HashSet<SctRelationship>());
      }
      
      if (con.getRels().isEmpty()) {
      RelationshipList relsList = client.findConceptRelationships(con.getConceptId(),
          sourceTerminology, sourceVersion, null, pfsLimitless, authToken);

      for (final Relationship<?, ?> relResult : relsList.getObjects()) {
        SctRelationship rel = relParser.parse(con.getName(), relResult);
        if (rel != null) {
          rel.setDescription(desc);
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
    con.setDescs(descParser.getDescriptions(con));
    
    if (con.getDescs() == null) {
      con.setDescs(new HashSet<SctNeoplasmDescription>());
    }
    
    if (con.getDescs().isEmpty()) {
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

  public Collection<SctNeoplasmConcept> getAllNeoplasmConcepts() {
    try {
      if (neoplasmConcepts == null) {
        neoplasmConcepts = new HashMap<>();

        for (String conId : descParser.getAllNeoplasmConceptIds()) {
          neoplasmConcepts.put(conId, populateSctConcept(conId, null));
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return neoplasmConcepts.values();
  }


  /**
   * Returns the neoplasm concept's relationship targets based on the provided
   * relationship type.
   *
   * @param con the con
   * @param relType the rel type
   * @return the dest rels
   */
  protected Set<SctRelationship> getDestRels(SctNeoplasmConcept con, String relType) {
    Set<SctRelationship> targets = new HashSet<>();

    for (SctRelationship rel : con.getRels()) {
      if (rel.getRelationshipType().equals(relType)) {
        targets.add(rel);
      }
    }

    return targets;
  }

  public SctNeoplasmConcept getSctConcept(String conId) {
    if (neoplasmConcepts == null) {
      getAllNeoplasmConcepts();
    }
    
    return neoplasmConcepts.get(conId);
  }

  public Set<SctNeoplasmDescription> lookupDescs(SctNeoplasmConcept con) {
    // TODO Auto-generated method stub
    return null;
  }
}
