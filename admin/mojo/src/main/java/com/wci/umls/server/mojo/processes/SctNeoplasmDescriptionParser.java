/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;

/**
 * The Class SctNeoplasmDescriptionParser.
 */
public class SctNeoplasmDescriptionParser {

  /** The neoplasm synonyms. */
  private final List<String> NEOPLASM_SYNONYMS = Arrays.asList("neoplasm", "neoplasms",
      "neoplastic", "tumor", "tumorous", "tumoru", "tumour", "tumoural", "tumours", "cancer",
      "cancerous", "cancerphobia", "carcinoma", "carcinomas", "carcinomatosis", "carcinomatous",
      "carcinoma-induced", "carcinomaphobia", "Adenocarcinoma", "adenoma", "Chondromatosis",
      "Chromaffinoma", "Glioma", "neoplasia", "Pheochromocytoma", "Proliferating pilar cyst",
      "Thymoma", "Melanoma", "Melanocytic", "Lipoma", "mesothelioma", "sarcoma", "fibroma",
      "papilloma", "Lymphoma", "Chondroma", "squamous");

  /** The false positive body structures. */
  private final List<String> FALSE_POSITIVE_BODY_STRUCTURES =
      Arrays.asList("Borst-Jadassohn", "Brodie", "Brooke", "Buschke-Löwenstein",
          "Buschke-Lowenstein", "Degos", "Enzinger", "Ferguson-Smith", "Gougerot and Carteaud",
          "Ito", "Jadassohn", "Langerhans", "Leser-Trélat", "Malherbe", "Nikolowski", "Ota",
          "Pinkus", "Queyrat", "Reed", "Stewart and Treves", "Vater", "adulthood",
          "ambiguous lineage", "care", "childhood", "elderly", "infancy", "xiphoid process");

  /** The uncertainty. */
  private final List<String> UNCERTAINTY =
      Arrays.asList("undetermined significance", "unknown origin",
          "unknown origin or ill-defined site", "unknown primary", "uncertain behavior",
          "uncertain behaviour", "uncertain or unknown behavior", "uncertain or unknown behaviour");

  /** The apostrophe map. */
  private Map<String, String> apostropheMap = new HashMap<>();

  /** The counter. */
  private int counter = 0;

  /** The body structure split. */
  private final List<String> BODY_STRUCTURE_SPLIT =
      Arrays.asList(" of ", " in ", " from ", ", with ", "with ", " due to ", " - ", "-", ", ");

  /** The body structure dash split exceptions. */
  private final List<String> BODY_STRUCTURE_DASH_SPLIT_EXCEPTIONS =
      Arrays.asList("-cell", "pharyngo-", "mucosa-", "cardio-", "gastro-", "ill-", "intra-",
          "lower-", "non-", "two-", "upper-", "co-", "para-");

  /** The distinct body structures. */
  private Set<String> distinctBodyStructures = new HashSet<>();

  /** The body structures require secondary info. */
  private List<String> bodyStructuresRequireSecondaryInfo = Arrays.asList("arterial cartilage",
      "blood vessel", "bone ", "bone and arterial cartilage", "bones", "bone structure", "brain",
      "connective and soft tissue", "connective and soft tissues", "connective tissue",
      "epithelium", "lymph node", "lymph node from neoplasm", "lymph node sites", "lymph nodes",
      "mucosa", "mucous membrane", "muscle", "non-pigmented epithelium", "peripheral nerve",
      "peripheral nerves", "pigmented epithelium", "ribs", "skin", "skin and subcutaneous tissue",
      "skin and/or subcutaneous tissue", "skin structure", "soft tissue", "soft tissues",
      "spinal cord", "uterus", "vermilion border", "bertebra", "vertevral column", "vestibule");

  /** The output file path. */
  private boolean testing = false;

  /** The output file path for relationships. */
  private final String previousExecutionInputFilePath =
      "src\\main\\resources\\allNeoplasmDescs.txt";

  /** The all descs. */
  private Map<String, Set<SctNeoplasmDescription>> neoplasmDescs = new HashMap<>();

  /** The all descs. */
  private Map<String, Set<SctNeoplasmDescription>> genericDescs = new HashMap<>();

  /** The desc to con map. */
  private Map<String, String> neoplasmDescsToConIdMap = new HashMap<>();

  /** The desc to con map. */
  private Map<String, String> genericDescsToConIdMap = new HashMap<>();

  /** The finding site input file path. */
  private final String findingSiteInputFilePath = "src\\main\\resources\\findingSiteDescs.txt";

  /** The finding site input file path. */
  private final String eclInputFilePath = "src\\main\\resources\\analyzer-Descriptions-250.txt";

  /** The finding site desc to con map. */
  private Map<String, String> findingSiteDescToConIdMap = new HashMap<>();

  /** The finding site desc to con map. */
  private Map<String, String> eclDescToConIdMap = new HashMap<>();

  /** The all finding site descs. */
  private Map<String, Set<SctNeoplasmDescription>> allFindingSiteDescs = new HashMap<>();

  /** The all finding site descs. */
  private Map<String, Set<SctNeoplasmDescription>> allEclDescs = new HashMap<>();

  /**
   * Instantiates an empty {@link SctNeoplasmDescriptionParser}.
   */
  public SctNeoplasmDescriptionParser() {
    try {
      // Preprocess file to identify unique body structures
      BufferedReader reader = new BufferedReader(new FileReader(previousExecutionInputFilePath));

      String line = reader.readLine(); // Don't want header
      line = reader.readLine();
      while (line != null) {
        String[] columns = line.split("\t");
        if (columns.length > 4 && !columns[4].isEmpty()) {
          distinctBodyStructures.add(columns[4]);
        }
        line = reader.readLine();
      }
      reader.close();

      apostropheMap.put("Meckel's diverticulum", "Meckel diverticulum");
      apostropheMap.put("Waldeyer's ring", "Waldeyer ring");
      apostropheMap.put("Bartholin's gland", "Bartholin gland");
      apostropheMap.put("Douglas' pouch", "the pouch of Douglas");
      apostropheMap.put("Gartner's duct", "Gartner duct");
    } catch (Exception e) {
      System.out.println("Failed processing input file: '" + previousExecutionInputFilePath
          + "' with exception: " + e.getMessage());
    }
  }

  /**
   * Parses the.
   *
   * @param descString the desc string
   * @return the sct neoplasm description
   */
  public SctNeoplasmDescription parse(String descString, boolean isNeoplasm) {

    SctNeoplasmDescription desc = new SctNeoplasmDescription();

    try {
      // desc = "Pathological fracture of hip due to neoplastic disease";

      if (!isNeoplasm) {
        desc.setDescription(descString.trim());
      } else {

        if (testing) {
          if (counter == 0) {
            descString = "";
          } else if (counter == 1) {
            descString = "Neoplasm of uterus affecting pregnancy";
          } else if (counter == 2) {
            descString = "Neoplasm of uncertain behaviour of salivary gland duct";
            /*
             * } else if (counter == 3) { desc =
             * "Mixed cell type lymphosarcoma of lymph nodes of head";
             * 
             * } else if (counter == 4) { desc =
             * "Hodgkin's paragranuloma of intrathoracic lymph nodes";
             */
          } else {
            descString = "";
          }

          if (counter > 4 || descString.isEmpty()) {
            return null;
          }
        }

        desc.setDescription(descString.trim());

        // Pathology Representation
        for (String syn : NEOPLASM_SYNONYMS) {
          if (descString.toLowerCase().contains(syn.toLowerCase())) {
            desc.setNeoplasmSynonym(syn.trim());
            break;
          }
        }

        boolean containsBodyStructure = true;
        String secondaryInfo = null;

        String splitKeyWord = identifySplitKeyword(descString);
        // of body structure
        if (splitKeyWord != null && descString.contains(splitKeyWord)) {
          int bodyStructIdx = descString.indexOf(splitKeyWord);

          if (splitKeyWord.equals(" of ")) {
            // Ignore Local Recurrence 'of'
            if (descString.startsWith("Local recurrence of")) {
              bodyStructIdx = descString.substring("Local recurrence of".length()).indexOf(" of ")
                  + "Local recurrence of".length();
            }

            // Ignore 'of' overlapping lesion
            if (descString.substring(bodyStructIdx).toLowerCase()
                .contains("of overlapping lesion")) {
              bodyStructIdx = bodyStructIdx + descString
                  .substring(bodyStructIdx + "of overlapping lesion".length()).indexOf(" of ")
                  + "of overlapping lesion".length();
            }
          }

          // Ignore 'of' uncertain xyz
          for (String uncertainStr : UNCERTAINTY) {
            String afterSplitWord =
                descString.substring(bodyStructIdx + splitKeyWord.length()).trim().toLowerCase();
            if (afterSplitWord.startsWith(uncertainStr)) {
              String secondSplitKeyWord = identifySplitKeyword(afterSplitWord);

              if (secondSplitKeyWord != null) {
                bodyStructIdx = descString.indexOf(splitKeyWord) + splitKeyWord.length();
                bodyStructIdx += afterSplitWord.indexOf(secondSplitKeyWord);
              } else {
                containsBodyStructure = false;
              }

              /*
               * if (!afterSplitWord.endsWith(uncertainStr)) { secondaryIdx =
               * afterSplitWord.indexOf(splitKeyWord.trim()) + bodyStructIdx +
               * splitKeyWord.length(); secondaryInfo =
               * desc.substring(secondaryIdx).trim(); }
               */ break;
            }
          }

          if (FALSE_POSITIVE_BODY_STRUCTURES
              .contains(descString.substring(descString.indexOf("of") + "of".length()).trim())) {
            containsBodyStructure = false;
          }

          // Print content prior to Body Structure
          if (!containsBodyStructure) {
            if (secondaryInfo == null) {
              desc.setPathology(descString.trim());
            }
          } else {
            // Body Structure found... Print part prior to Body Structure

            // Print Pathology
            if (descString.substring(0, bodyStructIdx).trim().endsWith(" " + splitKeyWord.trim())) {
              desc.setPathology(descString.substring(0, bodyStructIdx).trim()
                  .substring(0, bodyStructIdx - 3).trim());
            } else {
              desc.setPathology(descString.substring(0, bodyStructIdx).trim());
            }

            // Identify Body Structure and if available, causation and/or
            // secondary
            // Pathology
            String bodyStruct = descString.substring(bodyStructIdx + 4);
            String originalBodyStruture = bodyStruct;

            if (bodyStruct.contains("(") && bodyStruct.trim().endsWith(")")
                && !bodyStruct.endsWith("(s)")) {
              // If ends with paranthesis (unless plural), make part in
              // parenthesis as
              // secondary Info
              secondaryInfo = bodyStruct.substring(bodyStruct.indexOf("(")).trim();
              secondaryInfo = secondaryInfo.substring(1, secondaryInfo.length() - 1);
              bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf("(")).trim();
            } else if (bodyStruct.contains("affecting")) {
              // If contains "affecting", make everything afterwards (including
              // the word) as
              // secondary Info
              secondaryInfo = bodyStruct.substring(bodyStruct.indexOf("affecting")).trim();
              bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf("affecting")).trim();
            } else {
              for (String splitStr : BODY_STRUCTURE_SPLIT) {
                if (bodyStruct.contains(splitStr)) {
                  boolean isException = false;

                  if (splitStr.trim().equals("-")) { // && !desc.matches(".*
                                                     // [(].*-.*") --> Removing
                                                     // this
                                                     // fixed skin (T-cell, skin
                                                     // (chronic T-cell and
                                                     // others

                    for (String exception : BODY_STRUCTURE_DASH_SPLIT_EXCEPTIONS) {
                      if (bodyStruct.contains(exception)) {
                        isException = true;
                        break;
                      }
                    }
                  }

                  if (!isException) {
                    secondaryInfo = bodyStruct
                        .substring(bodyStruct.indexOf(splitStr) + splitStr.length()).trim();

                    if (secondaryInfo.endsWith(")")) {
                      secondaryInfo = secondaryInfo.substring(0, secondaryInfo.lastIndexOf(")"));
                    }
                    bodyStruct = bodyStruct.substring(0, bodyStruct.indexOf(splitStr)).trim();
                  }
                  break;
                }
              }
            }

            if (bodyStruct.contains("'") && apostropheMap.containsKey(bodyStruct)) {
              bodyStruct = apostropheMap.get(bodyStruct);
            }

            // Check for Lymph Node special case
            if (bodyStruct.contains("lymph node") && !bodyStruct.trim().startsWith("lymph node")) {
              if (secondaryInfo == null || secondaryInfo.isEmpty()) {
                secondaryInfo = bodyStruct.substring(0, bodyStruct.indexOf("lymph node")).trim();
                bodyStruct = bodyStruct.substring(bodyStruct.indexOf("lymph node")).trim();
              }
            }

            // Check to see if
            if (!bodyStructuresRequireSecondaryInfo.contains(bodyStruct) && secondaryInfo != null) {
              for (String structure : distinctBodyStructures) {
                if (secondaryInfo.contains(structure)) {
                  bodyStruct = originalBodyStruture;
                  secondaryInfo = null;
                  break;
                }
              }
            }

            // Print out body structure
            if (!FALSE_POSITIVE_BODY_STRUCTURES.contains(bodyStruct)) {
              if (bodyStruct.startsWith("the ")) {
                bodyStruct = bodyStruct.substring("the ".length()).trim();
              }
              desc.setBodyStructure(bodyStruct.trim());
            }

            // Print Secondary Information
            if (secondaryInfo != null) {
              desc.setSecondInfo(secondaryInfo.trim());
            }
          }
        }

        outputBooleanValues(descString, desc);
      }
    } catch (Exception e) {
      System.out
          .println("Failed processing: '" + descString + "' with exception: " + e.getMessage());
      return null;
    }
    return desc;
  }

  /**
   * Identify split keyword.
   *
   * @param desc the desc
   * @return the string
   */
  private String identifySplitKeyword(String desc) {
    if (!desc.contains("due to")) {
      if (!desc.contains(" to ") && desc.contains(" of ")) {
        // i.e. Neoplasm of hypogastric lymph nodes
        return " of ";
      } else if (desc.contains(" to ") && !desc.contains(" of ")) {
        // i.e. Cancer metastatic to choroid
        return " to ";
      } else if (!desc.matches(".* to .* to .*") && !desc.matches(".* of .* of .*")
          && desc.matches(".* to .* of .*")) {
        // i.e. Cancer metastatic to lymph nodes of lower limb
        return " to ";
      }
    } else {
      if (!desc.matches(".* to .* to .*") && desc.matches(".* of .* due to .*")) {
        // i.e. Pathological fracture of hip due to neoplastic disease
        return " of ";
      } else if (!desc.contains("of") && !desc.matches(".* to .* to .*")) {
        // i.e. Pelvis fracture due to tumor
        return null;
      }
    }

    return null;
  }

  /**
   * Output boolean values.
   *
   * @param descString the desc string
   * @param desc the desc
   */
  private void outputBooleanValues(String descString, SctNeoplasmDescription desc) {
    // Has Uncertainty
    for (String uncertainStr : UNCERTAINTY) {
      if (descString.toLowerCase().contains(uncertainStr)) {
        desc.setUncertainty(uncertainStr.trim());
        break;
      }
    }

    // Has 'Cancer Stage'
    if (descString.toLowerCase().contains("stage")) {
      desc.setStage(true);
    }

    // Is primary or secondary
    if (descString.toLowerCase().contains("primary")) {
      desc.setPrimaryOrSecondary("Primary");
    } else if (descString.toLowerCase().contains("secondary")) {
      desc.setPrimaryOrSecondary("Secondary");
    }

    // Is Benign or Malignant
    if (descString.toLowerCase().contains("benign")) {
      desc.setBenignOrMalignant("Benign");
    } else if (descString.toLowerCase().contains("malignant")) {
      desc.setBenignOrMalignant("Malignant");
    }

    // Is Upper or Lower
    if (descString.toLowerCase().contains("upper")) {
      desc.setUpperOrLower("Upper");
    } else if (descString.toLowerCase().contains("lower")) {
      desc.setUpperOrLower("Lower");
    }

    // Is Right or Left
    if (descString.toLowerCase().contains("right")) {
      desc.setLeftOrRight("Right");
    } else if (descString.toLowerCase().contains("left")) {
      desc.setLeftOrRight("Left");
    }

    // Is Metastic
    if (descString.toLowerCase().contains("metastatic")) {
      desc.setMetastatic(true);
    }

    // Has in situ
    if (descString.toLowerCase().contains("in situ")) {
      desc.setInSitu(true);
    }

    // Has Node
    if (descString.toLowerCase().contains("node")) {
      desc.setNode(true);
    }

    // Is Local recurrence
    if (descString.toLowerCase().contains("local recurrence")) {
      desc.setLocalRecurrance(true);
    }
  }

  /**
   * Read all descs from file.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean readAllNeoplasmDescsFromFile() throws IOException {
    // Preprocess file to identify unique body structures
    return parseInputFile(previousExecutionInputFilePath, neoplasmDescs, neoplasmDescsToConIdMap,
        true);
  }

  public boolean readAllFindingSitesFromFile() throws IOException {
    return parseInputFile(findingSiteInputFilePath, allFindingSiteDescs, findingSiteDescToConIdMap,
        false);
  }

  public boolean readAllEclFromFile() {
    return parseInputFile(eclInputFilePath, allEclDescs, eclDescToConIdMap, false);
  }

  private boolean parseInputFile(String filePath, Map<String, Set<SctNeoplasmDescription>> descs,
    Map<String, String> descsToConMap, boolean isNeoplasm) {
    String line = null;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));

      line = reader.readLine(); // Don't want header
      line = reader.readLine();

      while (line != null) {
        int counter = 0;
        String origLine = line;
        String id = null;
        SctNeoplasmDescription desc = new SctNeoplasmDescription();

        if (isNeoplasm) {
          while (line.contains("\t")) {
            String subStr = line.substring(0, line.indexOf("\t"));
            if (counter == 0) {
              id = subStr;
            } else if (counter == 1) {
              desc.setDescription(subStr);
            } else if (counter == 2) {
              desc.setNeoplasmSynonym(subStr);
            } else if (counter == 3) {
              desc.setPathology(subStr);
            } else if (counter == 4) {
              desc.setBodyStructure(subStr);
            } else if (counter == 5) {
              desc.setSecondInfo(subStr);
            } else if (counter == 6) {
              desc.setUncertainty(subStr);
            } else if (counter == 7) {
              desc.setStage(!subStr.isEmpty());
            } else if (counter == 8) {
              desc.setPrimaryOrSecondary(subStr);
            } else if (counter == 9) {
              desc.setBenignOrMalignant(subStr);
            } else if (counter == 10) {
              desc.setUpperOrLower(subStr);
            } else if (counter == 11) {
              desc.setLeftOrRight(subStr);
            } else if (counter == 12) {
              desc.setMetastatic(!subStr.isEmpty());
            } else if (counter == 13) {
              desc.setInSitu(!subStr.isEmpty());
            } else if (counter == 14) {
              desc.setNode(!subStr.isEmpty());
            }

            line = line.substring(line.indexOf("\t") + "\t".length());
            counter++;
          }
          desc.setLocalRecurrance(!origLine.substring(origLine.lastIndexOf("\t")).isEmpty());
        } else {
          String[] columns = line.split("\t");
          id = columns[0];
          desc.setDescription(columns[1]);
        }

        if (!descs.containsKey(id)) {
          descs.put(id, new HashSet<SctNeoplasmDescription>());
        }

        descs.get(id).add(desc);
        descsToConMap.put(desc.getDescription(), id);

        line = reader.readLine();
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.out.println("File doesn't exist: " + filePath);
      return false;
    } catch (IOException e) {
      System.out.println("Faililng on line: " + line + " with: " + e.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Returns the finding site descs.
   *
   * @param con the con
   * @return the finding site descs
   */
  public Set<SctNeoplasmDescription> getFindingSiteDescs(ICD11MatcherSctConcept con) {
    return allFindingSiteDescs.get(con.getConceptId());
  }

  /**
   * Returns the con finding site from desc.
   *
   * @param desc the desc
   * @return the con finding site from desc
   */
  public String getFindingSiteConIdFromDesc(String desc) {
    return findingSiteDescToConIdMap.get(desc);
  }

  /**
   * Returns the finding site descs.
   *
   * @param con the con
   * @return the finding site descs
   */
  public Set<SctNeoplasmDescription> getEclDescs(ICD11MatcherSctConcept con) {
    return allEclDescs.get(con.getConceptId());
  }

  /**
   * Returns the con finding site from desc.
   *
   * @param desc the desc
   * @return the con finding site from desc
   */
  public String getEclConIdFromDesc(String desc) {
    return eclDescToConIdMap.get(desc);
  }

  /**
   * Returns the descs.
   *
   * @param con the con
   * @return the descs
   */
  public Set<SctNeoplasmDescription> getNeoplasmDescs(ICD11MatcherSctConcept con) {
    return neoplasmDescs.get(con.getConceptId());
  }

  /**
   * Returns the con from desc.
   *
   * @param desc the desc
   * @return the con from desc
   */
  public String getNeoplasmConIdFromDesc(String desc) {
    return neoplasmDescsToConIdMap.get(desc);
  }

  public Set<SctNeoplasmDescription> getDescriptions(ICD11MatcherSctConcept con) {
    Set<SctNeoplasmDescription> descs = getNeoplasmDescs(con);

    if (descs == null) {
      descs = getFindingSiteDescs(con);
    }

    if (descs == null) {
      descs = getEclDescs(con);
    }

    if (descs == null) {
      descs = getGenericDescs(con);
    }

    return descs;
  }

  private Set<SctNeoplasmDescription> getGenericDescs(ICD11MatcherSctConcept con) {
    return genericDescs.get(con.getConceptId());
  }

  public String getConIdFromDesc(String desc) {
    String conId = getNeoplasmConIdFromDesc(desc);

    if (conId == null) {
      conId = getFindingSiteConIdFromDesc(desc);
    }

    if (conId == null) {
      conId = getEclConIdFromDesc(desc);
    }

    if (conId == null) {
      conId = getGenericConIdFromDesc(desc);
    }

    return conId;
  }

  private String getGenericConIdFromDesc(String desc) {
    return genericDescsToConIdMap.get(desc);
  }

  public Collection<String> getAllNeoplasmConceptIds() {
    return neoplasmDescsToConIdMap.values();
  }

  public boolean readDescsFromFile(String rulePath) {
    return parseInputFile(rulePath + File.separator + "analyzer-Descriptions.txt", genericDescs,
        genericDescsToConIdMap, false);
  }
}
