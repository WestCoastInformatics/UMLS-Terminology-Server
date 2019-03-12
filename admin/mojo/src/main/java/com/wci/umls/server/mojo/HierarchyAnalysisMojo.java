/**
 * Copyright 2018 West Coast Informatics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.services.SecurityService;

/**
 * Used to search the term-server database for concepts with a matching string.
 * 
 * See matcher/pom.xml for a sample invocation.
 *
 * @author Jesse Efron
 */
@Mojo(name = "hierarchy-analyzer", defaultPhase = LifecyclePhase.PACKAGE)
public class HierarchyAnalysisMojo extends AbstractContentAnalysisMojo {

  private static final String NEOPLASM_HIERARCHY = "Neoplasm";

  /** The output file path for descriptions. */
  private String outputDescFilePath;

  /** The output file path for relationships. */
  private String outputRelFilePath;

  /** The output file path for relationships. */
  private String inputFilePath = null;
  // "C:\\Users\\yishai\\Desktop\\Neoplasm\\sctNeoplasmInputFile2.txt";

  /** The output file path. */
  private boolean testing = false;

  final private String eclDesc = "< 123037004";

  final private String hierarchyName = "FindingSite";

  private final List<String> NEOPLASM_SYNONYMS = Arrays.asList("neoplasm",
      "neoplasms", "neoplastic", "tumor", "tumorous", "tumoru", "tumour",
      "tumoural", "tumours", "cancer", "cancerous", "cancerphobia", "carcinoma",
      "carcinomas", "carcinomatosis", "carcinomatous", "carcinoma-induced",
      "carcinomaphobia", "Adenocarcinoma", "adenoma", "Chondromatosis",
      "Chromaffinoma", "Glioma", "neoplasia", "Pheochromocytoma",
      "Proliferating pilar cyst", "Thymoma", "Melanoma", "Melanocytic",
      "Lipoma", "mesothelioma", "sarcoma", "fibroma", "papilloma", "Lymphoma",
      "Chondroma", "squamous");

  private final List<String> FALSE_POSITIVE_BODY_STRUCTURES = Arrays.asList(
      "Borst-Jadassohn", "Brodie", "Brooke", "Buschke-Löwenstein",
      "Buschke-Lowenstein", "Degos", "Enzinger", "Ferguson-Smith",
      "Gougerot and Carteaud", "Ito", "Jadassohn", "Langerhans", "Leser-Trélat",
      "Malherbe", "Nikolowski", "Ota", "Pinkus", "Queyrat", "Reed",
      "Stewart and Treves", "Vater", "adulthood", "ambiguous lineage", "care",
      "childhood", "elderly", "infancy", "xiphoid process");

  private final List<String> UNCERTAINTY =
      Arrays.asList("undetermined significance", "unknown origin",
          "unknown origin or ill-defined site", "unknown primary",
          "uncertain behavior", "uncertain behaviour",
          "uncertain or unknown behavior", "uncertain or unknown behaviour");

  private Map<String, String> apostropheMap = new HashMap<>();

  private int counter = 0;

  private final List<String> BODY_STRUCTURE_SPLIT = Arrays.asList(" of ",
      " in ", " from ", ", with ", "with ", " due to ", " - ", "-", ", ");

  private final List<String> BODY_STRUCTURE_DASH_SPLIT_EXCEPTIONS =
      Arrays.asList("-cell", "pharyngo-", "mucosa-", "cardio-", "gastro-",
          "ill-", "intra-", "lower-", "non-", "two-", "upper-", "co-", "para-");

  private Set<String> distinctBodyStructures = new HashSet<>();

  private List<String> bodyStructuresRequireSecondaryInfo = Arrays.asList(
      "arterial cartilage", "blood vessel", "bone ",
      "bone and arterial cartilage", "bones", "bone structure", "brain",
      "connective and soft tissue", "connective and soft tissues",
      "connective tissue", "epithelium", "lymph node",
      "lymph node from neoplasm", "lymph node sites", "lymph nodes", "mucosa",
      "mucous membrane", "muscle", "non-pigmented epithelium",
      "peripheral nerve", "peripheral nerves", "pigmented epithelium", "ribs",
      "skin", "skin and subcutaneous tissue", "skin and/or subcutaneous tissue",
      "skin structure", "soft tissue", "soft tissues", "spinal cord", "uterus",
      "vermilion border", "bertebra", "vertevral column", "vestibule");

  @Override
  public void execute() throws MojoFailureException {
    try {
      /**
       * Name of targetTerminology to be loaded.
       */
      targetTerminology = "SNOMEDCT";

      /**
       * The targetVersion.
       */
      targetVersion = "latest";

      getLog().info("Hierarchy Analyzer (" + hierarchyName + ") Mojo");
      getLog().info("  runConfig = " + runConfig);
      getLog().info("  targetTerminology = " + targetTerminology);
      getLog().info("  targetVersion = " + targetVersion);
      getLog().info("  ecl = " + eclDesc);
      getLog().info("  userName = " + userName);

      /*
       * Error Checking
       */
      if (targetTerminology == null || targetTerminology.isEmpty()) {
        throw new Exception(
            "Must define a targetTerminology to search against i.e. SNOMEDCT");
      }
      if (targetVersion == null || targetVersion.isEmpty()) {
        throw new Exception(
            "Must define a targetVersion to search against i.e. latest");
      }
      if (eclDesc == null || eclDesc.isEmpty()) {
        throw new Exception("Must specify an ecl expression");
      }

      /*
       * Setup
       */

      apostropheMap.put("Meckel's diverticulum", "Meckel diverticulum");
      apostropheMap.put("Waldeyer's ring", "Waldeyer ring");
      apostropheMap.put("Bartholin's gland", "Bartholin gland");
      apostropheMap.put("Douglas' pouch", "the pouch of Douglas");
      apostropheMap.put("Gartner's duct", "Gartner duct");

      PrintWriter outputDescFile = prepareDescOutputFile();
      PrintWriter outputRelFile = prepareRelOutputFile();
      outputDescFile.flush();
      outputRelFile.flush();
      
      Properties properties = setupProperties();
      final ContentClientRest client = new ContentClientRest(properties);
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(userName, userPassword).getAuthToken();
      service.close();

      if (inputFilePath == null) {

        PfsParameterJpa pfs = new PfsParameterJpa();

        pfs.setExpression(eclDesc);

        final SearchResultList results = client.findConcepts(targetTerminology,
            targetVersion, null, pfs, authToken);
        getLog().info("Have " + results.getTotalCount() + " ECL Results");

        for (SearchResult result : results.getObjects()) {
          Concept con = client.getConcept(result.getId(), null, authToken);

          for (Atom atom : con.getAtoms()) {
            // Only process active & non-FSN & non-Definition descs
            if (!atom.isObsolete()
                && !atom.getTermType().equals("Fully specified name")
                && !atom.getTermType().equals("Definition")) {
              String desc = atom.getName();
              processDesc(result.getTerminologyId(), desc, outputDescFile);
            }
          }
          
          RelationshipList relsList =
              client.findConceptRelationships(con.getTerminologyId(), targetTerminology,
                  targetVersion, null, new PfsParameterJpa(), authToken);

          for (final Relationship<?, ?> relResult : relsList.getObjects()) {
            SctRelationship rel = relParser.parse(con.getName(), relResult);
            exportRels(rel, con.getTerminologyId(), outputRelFile);
          }

          if (!clearCache(outputDescFile, outputRelFile)) {
            break;
          }
        }
      } else {
        SctNeoplasmDescriptionParser descParser =
            new SctNeoplasmDescriptionParser();

        // Now parse to write out contents
        BufferedReader reader =
            new BufferedReader(new FileReader(inputFilePath));

        String line = reader.readLine(); // Don't want header
        line = reader.readLine();
        Set<String> conceptsProcessed = new HashSet<>();
        try {
          while (line != null) {
            String[] columns = line.split("\t");
            if (testing) {
              if (!columns[0].equals("100731000119107")
                  && !columns[0].equals("100721000119109"))
                continue;
            }

            // Concept con = client.getConcept(Long.parseLong(columns[0]), null,
            // authToken);

            processDesc(descParser, columns[0], columns[1], outputDescFile);

            if (!conceptsProcessed.contains(columns[0])) {

              RelationshipList relsList =
                  client.findConceptRelationships(columns[0], targetTerminology,
                      targetVersion, null, new PfsParameterJpa(), authToken);

              for (final Relationship<?, ?> relResult : relsList.getObjects()) {
                SctRelationship rel = relParser.parse(columns[2], relResult);
                exportRels(rel, columns[2], outputRelFile);
              }

              conceptsProcessed.add(columns[0]);
            }

            line = reader.readLine();

            if (!clearCache(outputDescFile, outputRelFile)) {
              break;
            }
          }
        } catch (Exception e) {
          System.out.println("Failed processing: " + line + " with exception: "
              + e.getMessage());
        }

        reader.close();
      }

      outputDescFile.close();
      outputRelFile.close();

      getLog().info("");
      getLog().info("Finished processing...");
      getLog().info("Output avaiable at: " + outputDescFilePath + " and at "
          + outputRelFilePath);

    } catch (

    Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private void processDesc(SctNeoplasmDescriptionParser descParser,
    String conId, String descString, PrintWriter outputDescFile) {

    try {
      if (testing) {
        // desc = "Pathological fracture of hip due to neoplastic disease";
        if (counter == 0) {
          descString = "";
        } else if (counter == 1) {
          descString = "Neoplasm of Meckel's diverticulum";
          /*
           * } else if (counter == 2) { descString =
           * "Neoplasm of uncertain behaviour of salivary gland duct"; } else if
           * (counter == 3) { descString =
           * "Mixed cell type lymphosarcoma of lymph nodes of head"; } else if
           * (counter == 4) { descString =
           * "Hodgkin's paragranuloma of intrathoracic lymph nodes";
           */
        } else {
          descString = "";
        }

        if (counter > 4 || descString.isEmpty()) {
          return;
        }
        /*
         * if (testing && (descString.equals("Melanocytic nevus of lip") ||
         * startPause)) { startPause = true; }
         */
      }

      if (descString.startsWith("Chondroma of periosteum")
          || descString.startsWith("Benign chondroblastoma of bone")) {
        int a = 2;
      }

      outputDescFile.write(conId);
      outputDescFile.write("\t");
      SctNeoplasmDescription desc = descParser.parse(descString);

      outputDescFile.print(desc.printForExcel());

      outputDescFile.println();
    } catch (Exception e) {
      System.out.println("Failed processing: '" + descString
          + "' with exception: " + e.getMessage());
    }

  }

  private boolean clearCache(PrintWriter outputDescFile,
    PrintWriter outputRelFile) {
    if (counter++ % 50 == 0) {
      outputDescFile.flush();
      outputRelFile.flush();
    } else if (counter % 500 == 0) {
      getLog().info("Have processed " + counter + " concepts");
    }

    if (testing && counter > 4) { // result.getTerminologyId().equals("109830000"))
                                  // {
      return false;
    }

    return true;
  }

  private void processDesc(String conId, String desc,
    PrintWriter outputDescFile) throws Exception {
    // TODO
    // 2) malignant tumor of * malignant tumour of---> NO
    // 7) Handle greater vestibular (Bartholin's) gland???
    // Lymph Node handling
    // Body Structure i.e. "of anterior wall of x" should not have "anterior
    // wall"
    // (as body Struct) and "x" (as secondary),
    // --> it should have "anterior wall of x" as body structure
    // 'Pathology Fixes' --> To should be treated same as "of" in these cases.
    // Need
    // to check how will break other aspects

    try {
      // desc = "Pathological fracture of hip due to neoplastic disease";

      if (!hierarchyName.equals(NEOPLASM_HIERARCHY)) {
        outputDescFile.write(conId);
        outputDescFile.write("\t");
        outputDescFile.println(desc.trim());
      } else {
        if (testing) {
          if (counter == 0) {
            desc = "";
          } else if (counter == 1) {
            desc = "Neoplasm of uterus affecting pregnancy";
          } else if (counter == 2) {
            desc = "Neoplasm of uncertain behaviour of salivary gland duct";
            /*
             * } else if (counter == 3) { desc =
             * "Mixed cell type lymphosarcoma of lymph nodes of head";
             * 
             * } else if (counter == 4) { desc =
             * "Hodgkin's paragranuloma of intrathoracic lymph nodes";
             */
          } else {
            desc = "";
          }

          if (counter > 4 || desc.isEmpty()) {
            return;
          }
          /*
           * if (testing && (desc.equals("Melanocytic nevus of lip") ||
           * startPause)) { startPause = true; }
           */
        }

        if (desc.startsWith("Chondroma of periosteum")
            || desc.startsWith("Benign chondroblastoma of bone")) {
          int a = 2;
        }

        outputDescFile.write(conId);
        outputDescFile.write("\t");
        outputDescFile.print(desc.trim());

        // Pathology Representation
        outputDescFile.print("\t");
        for (String syn : NEOPLASM_SYNONYMS) {
          if (desc.toLowerCase().contains(syn.toLowerCase())) {
            outputDescFile.print(syn.trim());
            break;
          }
        }

        boolean containsBodyStructure = true;
        String secondaryInfo = null;

        String splitKeyWord = identifySplitKeyword(desc);
        // of body structure
        if (splitKeyWord != null && desc.contains(splitKeyWord)) {
          int bodyStructIdx = desc.indexOf(splitKeyWord);

          if (splitKeyWord.equals(" of ")) {
            // Ignore Local Recurrence 'of'
            if (desc.startsWith("Local recurrence of")) {
              bodyStructIdx =
                  desc.substring("Local recurrence of".length()).indexOf(" of ")
                      + "Local recurrence of".length();
            }

            // Ignore 'of' overlapping lesion
            if (desc.substring(bodyStructIdx).toLowerCase()
                .contains("of overlapping lesion")) {
              bodyStructIdx = bodyStructIdx + desc
                  .substring(bodyStructIdx + "of overlapping lesion".length())
                  .indexOf(" of ") + "of overlapping lesion".length();
            }
          }

          // Ignore 'of' uncertain xyz
          for (String uncertainStr : UNCERTAINTY) {
            String afterSplitWord =
                desc.substring(bodyStructIdx + splitKeyWord.length()).trim()
                    .toLowerCase();
            if (afterSplitWord.startsWith(uncertainStr)) {
              String secondSplitKeyWord = identifySplitKeyword(afterSplitWord);

              if (secondSplitKeyWord != null) {
                bodyStructIdx =
                    desc.indexOf(splitKeyWord) + splitKeyWord.length();
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

          if (FALSE_POSITIVE_BODY_STRUCTURES.contains(
              desc.substring(desc.indexOf("of") + "of".length()).trim())) {
            containsBodyStructure = false;
          }

          // Print content prior to Body Structure
          if (!containsBodyStructure) {
            outputDescFile.print("\t");
            outputDescFile.print(desc.trim());
            outputDescFile.print("\t");
            outputDescFile.print("\t");
          } else {
            // Body Structure found... Print part prior to Body Structure
            outputDescFile.print("\t");

            // Print Pathology
            if (desc.substring(0, bodyStructIdx).trim()
                .endsWith(" " + splitKeyWord.trim())) {
              outputDescFile.print(desc.substring(0, bodyStructIdx).trim()
                  .substring(0, bodyStructIdx - 3).trim());
            } else {
              outputDescFile.print(desc.substring(0, bodyStructIdx).trim());
            }

            // Identify Body Structure and if available, causation and/or
            // secondary
            // Pathology
            outputDescFile.print("\t");
            String bodyStruct = desc.substring(bodyStructIdx + 4);
            String originalBodyStruture = bodyStruct;

            if (bodyStruct.contains("(") && bodyStruct.trim().endsWith(")")
                && !bodyStruct.endsWith("(s)")) {
              // If ends with paranthesis (unless plural), make part in
              // parenthesis as
              // secondary Info
              secondaryInfo =
                  bodyStruct.substring(bodyStruct.indexOf("(")).trim();
              secondaryInfo =
                  secondaryInfo.substring(1, secondaryInfo.length() - 1);
              bodyStruct =
                  bodyStruct.substring(0, bodyStruct.indexOf("(")).trim();
            } else if (bodyStruct.contains("affecting")) {
              // If contains "affecting", make everything afterwards (including
              // the word) as
              // secondary Info
              secondaryInfo =
                  bodyStruct.substring(bodyStruct.indexOf("affecting")).trim();
              bodyStruct = bodyStruct
                  .substring(0, bodyStruct.indexOf("affecting")).trim();
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
                        .substring(
                            bodyStruct.indexOf(splitStr) + splitStr.length())
                        .trim();

                    if (secondaryInfo.endsWith(")")) {
                      secondaryInfo = secondaryInfo.substring(0,
                          secondaryInfo.lastIndexOf(")"));
                    }
                    bodyStruct = bodyStruct
                        .substring(0, bodyStruct.indexOf(splitStr)).trim();
                  }
                  break;
                }
              }
            }

            if (bodyStruct.contains("'")
                && apostropheMap.containsKey(bodyStruct)) {
              bodyStruct = apostropheMap.get(bodyStruct);
            }

            // Check for Lymph Node special case
            if (bodyStruct.contains("lymph node")
                && !bodyStruct.trim().startsWith("lymph node")) {
              if (secondaryInfo == null || secondaryInfo.isEmpty()) {
                secondaryInfo = bodyStruct
                    .substring(0, bodyStruct.indexOf("lymph node")).trim();
                bodyStruct = bodyStruct
                    .substring(bodyStruct.indexOf("lymph node")).trim();
              }
            }

            // Check to see if
            if (!bodyStructuresRequireSecondaryInfo.contains(bodyStruct)
                && secondaryInfo != null) {
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
              outputDescFile.print(bodyStruct.trim());
            }

            // Print Secondary Information
            outputDescFile.print("\t");
            if (secondaryInfo != null) {
              outputDescFile.print(secondaryInfo.trim());
            }
          }
        } else {
          outputDescFile.print("\t");
          outputDescFile.print("\t");
          outputDescFile.print("\t");
        }

        outputBooleanValues(outputDescFile, desc);

        outputDescFile.println();
      }
    } catch (Exception e) {
      System.out.println("Failed processing: '" + desc + "' with exception: "
          + e.getMessage());
    }

  }

  private String identifySplitKeyword(String desc) {
    if (!desc.contains("due to")) {
      if (!desc.contains(" to ") && desc.contains(" of ")) {
        // i.e. Neoplasm of hypogastric lymph nodes
        return " of ";
      } else if (desc.contains(" to ") && !desc.contains(" of ")) {
        // i.e. Cancer metastatic to choroid
        return " to ";
      } else if (!desc.matches(".* to .* to .*")
          && !desc.matches(".* of .* of .*")
          && desc.matches(".* to .* of .*")) {
        // i.e. Cancer metastatic to lymph nodes of lower limb
        return " to ";
      }
    } else {
      if (!desc.matches(".* to .* to .*")
          && desc.matches(".* of .* due to .*")) {
        // i.e. Pathological fracture of hip due to neoplastic disease
        return " of ";
      } else if (!desc.contains("of") && !desc.matches(".* to .* to .*")) {
        // i.e. Pelvis fracture due to tumor
        return null;
      }
    }

    return null;
  }

  private void outputBooleanValues(PrintWriter outputDescFile, String desc) {
    // Has Uncertainty
    outputDescFile.print("\t");
    for (String uncertainStr : UNCERTAINTY) {
      if (desc.toLowerCase().contains(uncertainStr)) {
        outputDescFile.print(uncertainStr.trim());
        break;
      }
    }

    // Has 'Cancer Stage'
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("stage")) {
      outputDescFile.print("X");
    }

    // Is primary or secondary
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("primary")) {
      outputDescFile.print("Primary");
    } else if (desc.toLowerCase().contains("secondary")) {
      outputDescFile.print("Secondary");
    }

    // Is Benign or Malignant
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("benign")) {
      outputDescFile.print("Benign");
    } else if (desc.toLowerCase().contains("malignant")) {
      outputDescFile.print("Malignant");
    }

    // Is Upper or Lower
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("upper")) {
      outputDescFile.print("Upper");
    } else if (desc.toLowerCase().contains("lower")) {
      outputDescFile.print("Lower");
    }

    // Is Right or Left
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("right")) {
      outputDescFile.print("Right");
    } else if (desc.toLowerCase().contains("left")) {
      outputDescFile.print("Left");
    }

    // Is Metastic
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("metastatic")) {
      outputDescFile.print("X");
    }

    // Has in situ
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("in situ")) {
      outputDescFile.print("X");
    }

    // Has Node
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("node")) {
      outputDescFile.print("X");
    }

    // Is Local recurrence
    outputDescFile.print("\t");
    if (desc.toLowerCase().contains("local recurrence")) {
      outputDescFile.print("X");
    }

  }

  /**
   * Prepare description output file.
   *
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  private PrintWriter prepareDescOutputFile()
    throws FileNotFoundException, UnsupportedEncodingException {

    final LocalDateTime now = LocalDateTime.now();
    final String timestamp = partialDf.format(now);
    final String month =
        now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());

    File userFolder = new File(hierarchyName);
    userFolder.mkdirs();

    // Setup Description File
    File fd = new File(userFolder.getPath() + File.separator
        + hierarchyName + "-Descriptions-" + month + timestamp + ".txt");
    outputDescFilePath = fd.getAbsolutePath();
    getLog().info("Creating file at: " + outputDescFilePath);

    final FileOutputStream fos = new FileOutputStream(fd);
    final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    PrintWriter pw = new PrintWriter(osw);

    pw.write("Concept Id");
    pw.write("\t");
    pw.print("Concept Description");
    pw.write("\t");
    
    if (hierarchyName.equals(NEOPLASM_HIERARCHY)) {
      pw.print("Neoplasm Synonym");
      pw.write("\t");
      pw.print("Pathology");
      pw.write("\t");
      pw.print("Body Structure");
      pw.write("\t");
      pw.print("Secondary Information");
      pw.write("\t");
      pw.print("Uncertainty");
      pw.write("\t");
      pw.print("Has Cancer Stage");
      pw.write("\t");
      pw.print("Primary or Secondary");
      pw.write("\t");
      pw.print("Benign or Malignant");
      pw.write("\t");
      pw.print("Upper or Lower");
      pw.write("\t");
      pw.print("Right or Left");
      pw.write("\t");
      pw.print("Is Metastatic");
      pw.write("\t");
      pw.print("Has In Situ");
      pw.write("\t");
      pw.print("Has Node");
      pw.write("\t");
      pw.print("Is Local recurrence");
    }
    
    pw.println();
    return pw;
  }

  /**
   * Prepare relationship output file.
   *
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  private PrintWriter prepareRelOutputFile()
    throws FileNotFoundException, UnsupportedEncodingException {

    final LocalDateTime now = LocalDateTime.now();
    final String timestamp = partialDf.format(now);
    final String month =
        now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());

    File userFolder = new File(hierarchyName);
    userFolder.mkdirs();

    // Setup Description File
    File fd = new File(userFolder.getPath() + File.separator
        + hierarchyName + "-Relationships-" + month + timestamp + ".txt");
    outputRelFilePath = fd.getAbsolutePath();
    getLog().info("Creating file at: " + outputRelFilePath);

    final FileOutputStream fos = new FileOutputStream(fd);
    final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    PrintWriter pw = new PrintWriter(osw);

    pw.write("Concept Id");
    pw.write("\t");
    pw.print("Concept Name");
    pw.write("\t");
    pw.print("Relationship Type");
    pw.write("\t");
    pw.print("Relationship Destination");
    pw.write("\t");
    pw.print("Role Group");

    pw.println();
    return pw;
  }
}
