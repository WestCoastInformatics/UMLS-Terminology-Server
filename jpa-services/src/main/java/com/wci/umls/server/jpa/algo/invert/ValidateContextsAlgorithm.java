/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.invert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;


import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;

/**
 * Implementation of an algorithm to save information before an insertion.
 */
public class ValidateContextsAlgorithm extends AbstractInsertMaintReleaseAlgorithm {
/*
  # contexts.src fields
  # 1  = source_atom_id_1
  # 2  = relationship_name
  # 3  = relationship_attribute
  # 4  = source_atom_id_2
  # 5  = source
  # 6  = source_of_label
  # 7  = hcd
  # 8  = parent_treenum
  # 9  = release_mode
  # 10 = source_rui
  # 11 = relationship_group
  # 12 = sg_id_1
  # 13 = sg_type_1
  # 14 = sg_qualifier_1
  # 15 = sg_id_2
  # 16 = sg_type_2
  # 17 = sg_qualifier_2*/

  /**  The src full path. */
  private String srcFullPath;
  
  /** The check names. */
  private List<String> checkNames;
  
  /**  The max test cases. */
  private int maxTestCases = 50;
  
  /** Monitor the number of errors already logged for each of the test cases */
  private Integer[] errorTallies = new Integer[maxTestCases];
  
  
  /**
   * Instantiates an empty {@link ValidateAttributesAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ValidateContextsAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("VALIDATEATTRIBUTES");
    setLastModifiedBy("admin");
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new LocalException("Contexts Validation requires a project to be set");
    }

    // Go through all the files needed by insertion and check for presence
    // Check the input directories
    srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir") + "/"
            + getProcess().getInputPath();

    final Path realPath = Paths.get(srcFullPath).toRealPath();
    setSrcDirFile(new File(realPath.toString()));

    if (!getSrcDirFile().exists()) {
      throw new LocalException(
          "Specified input directory does not exist - " + srcFullPath);
    }

    checkFileExist(srcFullPath, "contexts.src");
    checkFileExist(srcFullPath, "classes_atoms.src");
    checkFileExist(srcFullPath, "sources.src");
    checkFileExist(srcFullPath, "MRDOC.RRF");

    // Ensure permissions are sufficient to write files
    try {
      final File outputFile = new File(srcFullPath, "testFile.txt");

      final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
      out.print("Test");
      out.close();

      // Remove test file
      outputFile.delete();
    } catch (Exception e) {
      throw new LocalException("Unable to write files to " + srcFullPath
          + " - update permissions before continuing validation.");
    }

    // Makes sure editing is turned off before continuing
    /*if(getProject().isEditingEnabled()){
      throw new LocalException("Editing is turned on - disable before continuing insertion.");
    }*/
    
    // Makes sure automations are turned off before continuing
    if(getProject().isAutomationsEnabled()){
      throw new LocalException("Automations are turned on - disable before continuing validation.");
    }
    
    return validationResult;
  }

  /**
   * Check file exist.
   *
   * @param srcFullPath the src full path
   * @param fileName the file name
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void checkFileExist(String srcFullPath, String fileName)
    throws Exception {

    File sourceFile = new File(srcFullPath + File.separator + fileName);
    if (!sourceFile.exists()) {
      throw new Exception(fileName
          + " file doesn't exist at specified input directory: " + srcFullPath);
    }

  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // No Molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);
    
    ValidationResult result = new ValidationResultJpa();
    
    // read in file classes_atoms.src
    BufferedReader in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "classes_atoms.src")));
    String fileLine = "";
    Set<String> sauis = new HashSet<>();
    
    // cache 
    while ((fileLine = in.readLine()) != null) {
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      sauis.add(fields[0]);
    } 
    in.close();
    
    // read in file sources.src
    in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "sources.src")));
    Map<String, String> sourcesToLatMap = new HashMap<>();
    Set<String> rootSources = new HashSet<>();
    
    // cache sources
    while ((fileLine = in.readLine()) != null) {
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      sourcesToLatMap.put(fields[0], fields[15]);
      rootSources.add(fields[4]);
    } 
    in.close();
    
    // read in file MRDOC.RRF
    in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "MRDOC.RRF")));
    Set<String> relas = new HashSet<>();
    
    // cache relas
    while ((fileLine = in.readLine()) != null) {
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      if (fields[0].equals("RELA")) {
        relas.add(fields[1]);
      }
    } 
    in.close();
    
    // read in file contexts.src
    in = new BufferedReader(new FileReader(
        new File(srcFullPath + File.separator + "contexts.src")));
    fileLine = "";

    Set<String> uniqueFields = new HashSet<>();

    // do field and line checks
    // initialize caches
    while ((fileLine = in.readLine()) != null) {

      String[] fields = FieldedStringTokenizer.split(fileLine, "|");

      // check each row has the correct number of fields
      if (checkNames.contains("#CXTS_1")) {
        if (fields.length != 17) {
          if (underErrorTallyThreashold("#CXTS_1")) {
            result.addError(
              "CXTS_1: incorrect number of fields in attributes.src row: "
                  + fileLine);
          }
        }
      }

      // check for PAR with null PTR
      if (checkNames.contains("#CXTS_2")) {
        if (fields[1].equals("PAR") && fields[7].equals("")) {
          if (underErrorTallyThreashold("#CXTS_2")) {
            result.addError("CXTS_2: PAR with null PTR in contexts.src: " + fields[1] + fields[7]);
          }
        }
      }
      
      // check for non unique RUI fields
      if (checkNames.contains("#CXTS_3")) {
        if (uniqueFields.contains(
            fields[4] + "|" + fields[1] + "|" + fields[2] + "|" + fields[11] + "|" + 
                fields[12] + "|" + fields[13] + "|" + fields[14] + "|" + fields[15] + "|" +
                fields[16] + "|" + fields[7]
        )) {
          if (underErrorTallyThreashold("#CXTS_3")) {
            result.addError(
              "CXTS_3: Non unique RUI fields in contexts.src: " + fileLine);
          }
        } else {
          uniqueFields.add(
              fields[4] + "|" + fields[1] + "|" + fields[2] + "|" + fields[11] + "|" + 
                  fields[12] + "|" + fields[13] + "|" + fields[14] + "|" + fields[15] + "|" +
                  fields[16] + "|" + fields[7]);
        }

      }
      
      // check for SIB rel with non-null RELA (sab UWDA is an exception)
      if (checkNames.contains("#CXTS_4")) {    
        if (fields[1].equals("SIB") && !fields[4].equals("UWDA") && !fields[2].equals("")) {
          if (underErrorTallyThreashold("#CXTS_4")) {
            result.addError(
              "CXTS_4: SIB rel with non-null RELA in contexts.src: " + fileLine);
          }
        }
      }
      
      // check VSAB ne source of label
      if (checkNames.contains("#CXTS_5")) {    
        if (!fields[4].equals(fields[5])) {
          if (underErrorTallyThreashold("#CXTS_5")) {
            result.addError(
              "CXTS_5: VSAB not equal to source of label in contexts.src: " + fields[4] + "|" + fields[5]);
          }
        }
      }  
      
      // check context treepos subset of parent treepos
      if (checkNames.contains("#CXTS_6")) {
        if (fields[1].equals("PAR") && !fields[7].contains(fields[3])) {
          if (underErrorTallyThreashold("#CXTS_6")) {
            result.addError("CXTS_6: Parent mismatch for: " + fields[3] + "|" + fields[7]);
          }
        }
      }
      
      if (checkNames.contains("#CXTS_7")) {
        if (underErrorTallyThreashold("#CXTS_7")) {
          String[] nodes = FieldedStringTokenizer.split(fields[7], ".");
          Set<String> nodeSet = new HashSet<>();
          Collections.addAll(nodeSet, nodes);
          if (nodeSet.size() != nodes.length) {
            result.addError("CXTS_7: Cycle in parent treepos: " + fields[7]);
          }
          for (String node : nodes) {
            if (!sauis.contains(node)) {
              result.addError("CXTS_7: Invalid node in parent treepos: " + node
                  + ":" + fields[7]);
            }
          }
        }
      }
      
      // check SRC_ATOM_ID_1 not equal to SGID_1
      if (checkNames.contains("#CXTS_8")) {

        if (fields[12].equals("SRC_ATOM_ID")) {
          if (!fields[0].equals(fields[11])) {
            if (underErrorTallyThreashold("#CXTS_8")) {
              result.addError("CXTS_8: SRC_ATOM_ID_1 not equal to SGID_1: " + fields[0]
                + ":" + fields[11]);
            }
          }
        }

      }
      
      // check SRC_ATOM_ID_2 not equal to SGID_2
      if (checkNames.contains("#CXTS_9")) {

        if (fields[15].equals("SRC_ATOM_ID")) {
          if (!fields[3].equals(fields[14])) {
            if (underErrorTallyThreashold("#CXTS_9")) {
              result.addError("CXTS_9: SRC_ATOM_ID_2 not equal to SGID_2: " + fields[3]
                + ":" + fields[14]);
            }
          }
        }
      }
      
      // check if VSAB is not in sources.src file
      if (checkNames.contains("#CXTS_10")) {
        if (!sourcesToLatMap.containsKey(fields[4])) {
          if (underErrorTallyThreashold("#CXTS_10")) {
            result.addError("CXTS_10: VSAB is not in the sources.src file: " + fields[4]);
          }
        }
      }
      
      // check if RELA is in MRDOC.RRF file
      if (checkNames.contains("#CXTS_11")) {
        if (!fields[2].equals("") && !fields[2].equals("isa") && !relas.contains(fields[2])) {
          if (underErrorTallyThreashold("#CXTS_11")) {
            result.addError("CXTS_11: RELA is not in the MRDOC.RRF file: " + fields[2]);
          }
        }
      }     
    }
    in.close();
    
    // print warnings and errors to log
    if (result.getWarnings().size() > 0) {
      for (String warning : result.getWarnings()) {
        logInfo(warning);
      }
    }
    if (result.getErrors().size() > 0) {
      for (String error : result.getErrors()) {
        logError(error);
      }
      throw new Exception(this.getName() + " Failed");
    }

    logInfo("Finished " + getName());
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("checkNames") != null) {
      checkNames =
          Arrays.asList(String.valueOf(p.getProperty("checkNames")).split(";"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    // Integrity check names
    AlgorithmParameter param = new AlgorithmParameterJpa("Validation Checks", "checkNames",
        "The names of the validation checks to run", "e.g. #CXTS_1", 200,
        AlgorithmParameter.Type.MULTI, "");

    List<String> validationChecks = new ArrayList<>();
    validationChecks.add("#CXTS_1");
    validationChecks.add("#CXTS_2");
    validationChecks.add("#CXTS_3");
    validationChecks.add("#CXTS_4");
    validationChecks.add("#CXTS_5");
    validationChecks.add("#CXTS_6");
    validationChecks.add("#CXTS_7");
    validationChecks.add("#CXTS_8");
    validationChecks.add("#CXTS_9");
    validationChecks.add("#CXTS_10");
    validationChecks.add("#CXTS_11");
    validationChecks.add("#CXTS_12");
    validationChecks.add("#CXTS_13");
    
    Collections.sort(validationChecks);
    param.setPossibleValues(validationChecks);
    params.add(param);
    
    return params;
  }
  
  // check if the number of errors logged for each test case is greater or less than 10
  private boolean underErrorTallyThreashold(String testName) {
    int index = Integer.parseInt(testName.substring(testName.indexOf("_") + 1));
    Integer value = errorTallies[index];
    if (value == null) {
      value = 1;
    } else {
      value = value + 1;
    }
    errorTallies[index] = value;
    return value <= 10;
  }
  
  @Override
  public String getDescription() {
    return "Validation checks related to contexts in the inversion files.";
  }
}