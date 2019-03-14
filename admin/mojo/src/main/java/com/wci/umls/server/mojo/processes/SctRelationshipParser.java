package com.wci.umls.server.mojo.processes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctRelationship;

public class SctRelationshipParser {

  private static final int CONCEPT_ID_COLUMN = 0;

  private static final int CONCEPT_NAME_COLUMN = 1;

  private static final int TYPE_COLUMN = 2;

  private static final int TARGET_COLUMN = 3;

  private static final int ROLE_GROUP_COLUMN = 4;

  /** The output file path for relationships. */
  private final String neoplasmInputFilePath =
      "src\\main\\resources\\neoplasmRels.txt";

  private Map<String, Set<SctRelationship>> neoplasmRels = new HashMap<>();

  /** The output file path for relationships. */
  private final String findingSiteInputFilePath =
      "src\\main\\resources\\findingSiteRels.txt";

  private Map<String, Set<SctRelationship>> findingSiteRels = new HashMap<>();

  /** The output file path. */
  public SctRelationship parse(String conName,
    final Relationship<?, ?> relObject) {

    SctRelationship rel = new SctRelationship();

    if (!relObject.isObsolete() && relObject.isInferred()
        && !relObject.isStated()) {
      // Concept Name
      rel.setDescription(conName);

      // Relationship Type
      if (relObject.getRelationshipType().equals("Is a")) {
        rel.setRelationshipType(relObject.getRelationshipType());
      } else {
        rel.setRelationshipType(relObject.getAdditionalRelationshipType());
      }

      // Relationship Destination
      rel.setRelationshipDestination(relObject.getTo().getName());

      // Role Group
      rel.setRoleGroup(Integer.parseInt(relObject.getGroup()));
      return rel;
    }

    return null;
  }

  public boolean readAllNeoplasmRelsFromFile() throws IOException {
    // Preprocess file to identify unique body structures
    return parseInputFile(neoplasmInputFilePath, neoplasmRels);
  }

  public boolean readAllFindingSitesFromFile() throws IOException {
    return parseInputFile(findingSiteInputFilePath, findingSiteRels);
  }

  private boolean parseInputFile(String filePath,
    Map<String, Set<SctRelationship>> rels) {
    String line = null;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));

      // Don't want header
      line = reader.readLine();
      line = reader.readLine();
      while (line != null) {
        String[] columns = line.split("\t");

        if (!rels.containsKey(columns[CONCEPT_ID_COLUMN])) {
          rels.put(columns[CONCEPT_ID_COLUMN], new HashSet<SctRelationship>());
        }

        SctRelationship rel = new SctRelationship();
        rel.setDescription(columns[CONCEPT_NAME_COLUMN]);
        rel.setRelationshipType(columns[TYPE_COLUMN]);
        rel.setRelationshipDestination(columns[TARGET_COLUMN]);
        rel.setRoleGroup(Integer.parseInt(columns[ROLE_GROUP_COLUMN]));

        rels.get(columns[CONCEPT_ID_COLUMN]).add(rel);

        line = reader.readLine();
      }

      reader.close();
    } catch (FileNotFoundException e) {
      System.out.println("File doesn't exist: " + filePath);
      return false;
    } catch (IOException e) {
      System.out
          .println("Faililng on line: " + line + " with: " + e.getMessage());
      return false;
    }

    return true;
  }

  public Set<SctRelationship> getNeoplasmRels(SctNeoplasmConcept con) {
    return neoplasmRels.get(con.getConceptId());
  }

  public Set<SctRelationship> getFindingSiteRels(SctNeoplasmConcept con) {
    return findingSiteRels.get(con.getConceptId());
  }

  public Set<SctRelationship> getRelationships(SctNeoplasmConcept con) {
    Set<SctRelationship> rels = getNeoplasmRels(con);

    if (rels == null) {
      rels = getFindingSiteRels(con);
    }

    return rels;
  }
}
