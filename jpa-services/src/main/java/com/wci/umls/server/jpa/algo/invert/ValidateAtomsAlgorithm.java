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
import java.util.regex.Pattern;

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
public class ValidateAtomsAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  private String srcFullPath;
  
  /** The check names. */
  private List<String> checkNames;
  
  /**  The max test cases. */
  private int maxTestCases = 50;
  
  /** Monitor the number of errors already logged for each of the test cases */
  private Integer[] errorTallies = new Integer[maxTestCases];
  
  
  /**
   * Instantiates an empty {@link ValidateAtomsAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ValidateAtomsAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("VALIDATEATOMS");
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
      throw new LocalException("Atom Validation requires a project to be set");
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

    checkFileExist(srcFullPath, "classes_atoms.src");
    checkFileExist(srcFullPath, "sources.src");
    checkFileExist(srcFullPath, "termgroups.src");
    checkFileExist(srcFullPath, "attributes.src");

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
    
    // read in file termgroups.src
    BufferedReader in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "termgroups.src")));
    String fileLine = "";
    Map<String, String> termgroupToSuppressMap = new HashMap<>();
    
    // cache termgroups
    while ((fileLine = in.readLine()) != null) {
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      termgroupToSuppressMap.put(fields[0], fields[2]);
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
    
    // read in file attributes.src
    in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "attributes.src")));
    Set<String> styIntelProds = new HashSet<>();
    
    // cache sources
    while ((fileLine = in.readLine()) != null) {
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      if (fields[3].equals("SEMANTIC_TYPE") && fields[4].equals("Intellectual Product")) {
        styIntelProds.add(fields[1]);
      }
    }       
    
/*    # Fields:
      # 1  = src_atom_id
      # 2  = source
      # 3  = termgroup
      # 4  = code
      # 5  = status
      # 6  = tobereleased
      # 7  = released
      # 8  = atom_name
      # 9  = suppressible
      # 10 = source_aui
      # 11 = source_cui
      # 12 = source_dui
      # 13 = language
      # 14 = order_id
      # 15 = last_release_cui
*/
      
    final int ct =
        filterFileForCount(getSrcDirFile(), "classes_atoms.src", null, null);
    logInfo("  Steps: " + ct + " atom rows to process");
    
    // Set the number of steps to the number of lines to be processed
    setSteps(ct);

    
    // read in file classes_atoms.src
    in = new BufferedReader(new FileReader(new File(srcFullPath + File.separator + "classes_atoms.src")));
    ValidationResult result = new ValidationResultJpa();
    Map<String, String> lowerToNativeMap = new HashMap<>();
    Set<String> vabCodes = new HashSet<>();
    Set<String> rabCodes = new HashSet<>();
    Set<String> uniqueAuiFields = new HashSet<>();
    
    // do field and line checks
    // initialize caches
    while ((fileLine = in.readLine()) != null) {
      
      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      
      // check each row has the correct number of fields
      if (checkNames.contains("#ATOMS_1")) {
        if (fields.length != 14) {
          if (underErrorTallyThreashold("#ATOMS_1")) {
            result
              .addError("ATOMS_1: incorrect number of fields in classes_atoms.src row: "
                  + fileLine);
          }
        }
      }
      
      // check first field is an integer
      if (checkNames.contains("#ATOMS_2")) {
        try {
          Integer.parseInt(fields[0]);
        } catch (Exception e) {
          if (underErrorTallyThreashold("#ATOMS_2")) {
            result
              .addError("ATOMS_2: First field in classes_atoms.src must be an integer. "
                  + fields[0]);
          }
        }
      }
      
      // check last field is an integer
      if (checkNames.contains("#ATOMS_3")) {
        try {
          Integer.parseInt(fields[13]);
        } catch (Exception e) {
          if (underErrorTallyThreashold("#ATOMS_3")) {
            result.addError("ATOMS_3: Last field in classes_atoms.src must be an integer. "
              + fields[0]);
          }
        }
      }
      
      // check for angle brackets in string field
      if (checkNames.contains("#ATOMS_4")) {
        if (fields[8].contains("<") && fields[8].contains(">")) {
          if (underErrorTallyThreashold("#ATOMS_4")) {
            result.addWarning(
              "ATOMS_4: String field in classes_atoms.src should not have angle brackets. "
                  + fields[8]);
          }
        }
      }
      
      // check for valid termgroup
      if (checkNames.contains("#ATOMS_5")) {
        if (!termgroupToSuppressMap.containsKey(fields[2]) && !fields[2].startsWith("SRC/")) {
          if (underErrorTallyThreashold("#ATOMS_5")) {
            result.addError(
              "ATOMS_5: Termgroup in classes_atoms.src is invalid: " + fields[2]);
          }
        }
      }
      
      // check for duplicate case-sensitive strings
      if (checkNames.contains("#ATOMS_6")) {
        if (lowerToNativeMap.values().contains(fields[7])) {
          result.addWarning("ATOMS_6: Duplicate case-sensitive strings: " + fields[7]);

          // check for duplicate case-insensitive strings
        } else if (lowerToNativeMap.keySet()
            .contains(fields[7].toLowerCase())) {
          result.addWarning(
              "ATOMS_6: Duplicate case-insensitive strings: " + fields[7].toLowerCase());

          // add it to the map
        } else {
          lowerToNativeMap.put(fields[7].toLowerCase(), fields[7]);
        }
      }
      
      // check code must be equal to SCUI, SDUI or SAUI unless all of them are null
      if (checkNames.contains("#ATOMS_8")) {
        if (!fields[9].isEmpty() && !fields[10].isEmpty() && !fields[11].isEmpty()
            && !fields[3].equals(fields[9]) && !fields[3].equals(fields[10]) && !fields[3].equals(fields[11])) {
          if (underErrorTallyThreashold("#ATOMS_8")) {
            result.addError("ATOMS_8: Code must be equal to SAUI, SCUI or SDUI: " + fields[0] + ":" + fields[3]);         
          }
        }
      }

      // check for valid sources
      if (checkNames.contains("#ATOMS_9")) {
        if (!(fields[1].equals("SRC") || sourcesToLatMap.containsKey(fields[1]))) {
          if (underErrorTallyThreashold("#ATOMS_9")) {
            result.addError("ATOMS_9: Source must be listed in sources.src: " + fields[1]);
          }
        }
      }
      
      // check for valid codes on SRC/VAB and SRC/VPT atoms
      if (checkNames.contains("#ATOMS_10")) {
        if ((fields[2].equals("SRC/VPT") || fields[2].equals("SRC/VAB"))
            && !sourcesToLatMap.containsKey(fields[3].substring(2))) {
          if (underErrorTallyThreashold("#ATOMS_10")) {
            result.addError("ATOMS_10: Code field must be a valid source for SRC/VPT and SRC/VAB rows: " + fields[3].substring(2));
          }
        }
      }
      
      // check for valid names on SRC/VAB atoms
      if (checkNames.contains("#ATOMS_11")) {
        if ((fields[2].equals("SRC/VAB"))
            && !sourcesToLatMap.containsKey(fields[7])) {
          if (underErrorTallyThreashold("#ATOMS_11")) {
            result.addError("ATOMS_11: Name field must be a valid source for SRC/VAB rows: " + fields[7]);
          }
        }
      }
      
      // check for valid names on SRC/RAB atoms
      if (checkNames.contains("#ATOMS_12")) {
        if ((fields[2].equals("SRC/RAB"))
            && !rootSources.contains(fields[7])) {
          if (underErrorTallyThreashold("#ATOMS_12")) {
            result.addError("ATOMS_12: Name field must be a valid source for SRC/RAB rows: " + fields[7]);
          }
        }
      }
      
      // check for valid codes on SRC/RAB and SRC/RPT atoms
      if (checkNames.contains("#ATOMS_13")) {
        if ((fields[2].equals("SRC/RPT") || fields[2].equals("SRC/RAB"))
            && !rootSources.contains(fields[3].substring(2))) {
          if (underErrorTallyThreashold("#ATOMS_13")) {
            result.addError("ATOMS_13: Code field must be a valid source for SRC/RPT and SRC/RAB rows: " + fields[3].substring(2));
          }
        }
      }
      
      
      // check for duplicate SRC/VAB codes
      if (checkNames.contains("#ATOMS_14")) {
        if (fields[2].equals("SRC/VAB") && vabCodes.contains(fields[3])) {

          if (underErrorTallyThreashold("#ATOMS_14")) {
            result.addError(
              "ATOMS_14: Duplicate SRC/VAB codes: " + fields[7].toLowerCase());
          }
        } else if (fields[2].equals("SRC/VAB")){
          vabCodes.add(fields[3]);
        }
      }
      
      // check for duplicate SRC/RAB codes
      if (checkNames.contains("#ATOMS_15")) {
        if (fields[2].equals("SRC/RAB") && rabCodes.contains(fields[3])) {
          if (underErrorTallyThreashold("#ATOMS_15")) {
            result.addError(
              "ATOMS_15: Duplicate SRC/RAB codes: " + fields[7].toLowerCase());
          }
        } else if (fields[2].equals("SRC/RAB")){
          rabCodes.add(fields[3]);
        }
      }
      
      // check for non-unique AUI fields
      if (checkNames.contains("#ATOMS_16")) {
        if (uniqueAuiFields.contains(fields[2] + "|" + fields[7] + "|" + fields[3] + "|" + 
            fields[9] + "|" + fields[10] + "|" + fields[11])) {
          if (underErrorTallyThreashold("#ATOMS_16")) {
            result.addError("ATOMS_16: Duplicate AUI fields: " + fields[2] + "|" + fields[7] + "|" + fields[3] + "|" + 
              fields[9] + "|" + fields[10] + "|" + fields[11]);
          }
        } else  {
          uniqueAuiFields.add(fields[2] + "|" + fields[7] + "|" + fields[3] + "|" + 
              fields[9] + "|" + fields[10] + "|" + fields[11]);
        }
      }
      
      // check if the LAT matches sources.src file
      if (checkNames.contains("#ATOMS_17")) {
        if (!fields[1].equals("SRC")) {
          if (!(sourcesToLatMap.containsKey(fields[1])
              && sourcesToLatMap.get(fields[1]).equals(fields[12]))) {
            if (underErrorTallyThreashold("#ATOMS_17")) {
              result.addError(
                "ATOMS_17: Lat field must match language of the source: "
                    + fields[1] + ":" + fields[12]);
            }
          }
        }
      }
      

      // check suppressibility of atom matches expected from termgroups.src file
      if (checkNames.contains("#ATOMS_18")) {
        if (!fields[1].equals("SRC")) {
          String atomSuppress = fields[8];
          String tgSuppress = termgroupToSuppressMap.get(fields[2]);

          if (underErrorTallyThreashold("#ATOMS_18")) {
            if (tgSuppress.equals("Y") && !atomSuppress.equals("Y")
                && !atomSuppress.equals("O")) {
              result.addError(
                  "ATOMS_18: Atom suppressibility must match termgroup suppressibility case 1: "
                      + atomSuppress + ":" + tgSuppress + " : " + fileLine);
            } else if (tgSuppress.equals("N") && atomSuppress.equals("Y")) {
              result.addError(
                  "ATOMS_18: Atom suppressibility must match termgroup suppressibility case 2: "
                      + atomSuppress + ":" + tgSuppress + " : " + fileLine);
            } else if (tgSuppress.equals("Y") && !atomSuppress.equals("O")) {
              result.addError(
                  "ATOMS_18: Atom suppressibility must match termgroup suppressibility: case 3 "
                      + atomSuppress + ":" + tgSuppress + " : " + fileLine);
            }
          }
        }
      }
      
      // check for XML chars in string field
      if (checkNames.contains("#ATOMS_21")) {
        String str = fields[7];
        String pattern = ".*[&#][a-zA-Z0-9]+;.*";
        if (str.contains("quot")) {
          if (Pattern.matches(pattern, str)  ) {
            if (underErrorTallyThreashold("#ATOMS_21")) {
              result.addError(
                "ATOMS_21: String contains an XML character: " + fields[7]);
            }
          }    
        }
      }
      
      // check all VPT atoms have STY of Intellectual Property
      if (checkNames.contains("#ATOMS_22")) {
        if ((fields[2].equals("SRC/VPT") && !styIntelProds.contains(fields[0]))) {
          if (underErrorTallyThreashold("#ATOMS_22")) {
            result.addError("ATOMS_22: All VPT atoms have STY of Intellectual Property: " + fields[0]);
          }
        }
      }     
      // Update the progress
      updateProgress();
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
        "The names of the validation checks to run", "e.g. #ATOMS_1", 200,
        AlgorithmParameter.Type.MULTI, "");

    List<String> validationChecks = new ArrayList<>();
    validationChecks.add("#ATOMS_1");
    validationChecks.add("#ATOMS_2");
    validationChecks.add("#ATOMS_3");
    validationChecks.add("#ATOMS_4");
    validationChecks.add("#ATOMS_5");
    validationChecks.add("#ATOMS_6");
    validationChecks.add("#ATOMS_7");
    validationChecks.add("#ATOMS_8");
    validationChecks.add("#ATOMS_9");
    validationChecks.add("#ATOMS_10");
    validationChecks.add("#ATOMS_11");
    validationChecks.add("#ATOMS_12");
    validationChecks.add("#ATOMS_13");
    validationChecks.add("#ATOMS_14");
    validationChecks.add("#ATOMS_15");
    validationChecks.add("#ATOMS_16");
    validationChecks.add("#ATOMS_17");
    validationChecks.add("#ATOMS_18");
    validationChecks.add("#ATOMS_19");
    validationChecks.add("#ATOMS_20");
    validationChecks.add("#ATOMS_21");
    validationChecks.add("#ATOMS_22");
    
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
    return "Validation checks related to atoms in the inversion files.";
  }
}