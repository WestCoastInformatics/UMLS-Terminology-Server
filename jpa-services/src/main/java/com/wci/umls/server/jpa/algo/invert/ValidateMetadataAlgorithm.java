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
import java.util.HashSet;
import java.util.List;
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
public class ValidateMetadataAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  private String srcFullPath;
  
  /** The check names. */
  private List<String> checkNames;
  
  /**
   * Instantiates an empty {@link ValidateMetadataAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ValidateMetadataAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("VALIDATEMETADATA");
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
      throw new LocalException("Metadata Validation requires a project to be set");
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

    checkFileExist(srcFullPath, "MRDOC.RRF");
    checkFileExist(srcFullPath, "termgroups.src");
    checkFileExist(srcFullPath, "mergefacts.src");

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
    
    // read in file attributes.src
    BufferedReader in = new BufferedReader(new FileReader(
        new File(srcFullPath + File.separator + "MRDOC.RRF")));
    String fileLine = "";

    Set<String> uniqueFields = new HashSet<>();
    
    // do field and line checks
    // initialize caches
    while ((fileLine = in.readLine()) != null) {

      String[] fields = FieldedStringTokenizer.split(fileLine, "|");

      // check each row has the correct number of fields
      if (checkNames.contains("#META_1")) {
        if (fields.length != 4) {
          result.addError(
              "META_1: incorrect number of fields in MRDOC.RRF row: "
                  + fileLine);
        }
      }
      
      // check invalid type for dockey
      if (checkNames.contains("#META_2")) {
        if ((fields[2].equals("tty_class") && !fields[0].equals("TTY"))
          || (fields[2].equals("rela_inverse") && !fields[0].equals("RELA"))) {
          result.addError(
              "META_2: invalid type for dockey in MRDOC.RRF: "
                  + fileLine);
        }
      }

      // check duplicate dockey and expl
      if (checkNames.contains("#META_3")) {
        if (!fields[0].equals("TTY")){
          if (uniqueFields
              .contains(fields[0] + "|" + fields[1] + "|" + fields[2] + "|"
                  + fields[3])) {
            result.addError("META_3: Duplicate dockey and expl fields in MRDOC.RRF: " + fields[0] + "|" + fields[1] + "|" + fields[2] + "|"
                + fields[3]);
          } else {
            uniqueFields.add(fields[0] + "|" + fields[1] + "|" + fields[2] + "|"
                + fields[3]);
          }
        }
      }   
      
      // check invalid null values
      if (checkNames.contains("#META_4")) {
        if (fields[1].equals("")) {
          if (fields[0].equals("RELA")) {
            if ((fields[2].equals("rela_inverse") && fields[3].equals(""))
                || (fields[2].equals("expanded_form")
                    && fields[3].equals("Empty relationship attribute"))) {
              // allowed for null rela - so ignore
            } else {
              result.addError(
                  "META_4: Invalid null values in MRDOC.RRF: " + fields[0] + "|"
                      + fields[1] + "|" + fields[2] + "|" + fields[3]);
            }
          } else {
            result.addError(
                "META_4: Invalid null values in MRDOC.RRF: " + fields[0] + "|"
                    + fields[1] + "|" + fields[2] + "|" + fields[3]);
          }
        }
        
      }
    }
    in.close();
    
    // read in file termgroups.src
    in = new BufferedReader(new FileReader(
        new File(srcFullPath + File.separator + "termgroups.src")));
    fileLine = "";

    
    // do field and line checks
    // initialize caches
    while ((fileLine = in.readLine()) != null) {

      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      
      // check each row has the correct number of fields
      if (checkNames.contains("#META_6")) {
        if (fields.length != 6) {
          result.addError(
              "META_6: incorrect number of fields in termgroups.src row: "
                  + fileLine);
        }
      }
      
      // check tty in each line must be present in the termgroup
      if (checkNames.contains("#META_7")) {
          if (!fields[0].endsWith("/" + fields[5])) {
            result.addError("META_7: tty must be present in the termgroup in termgroups.src: " + fields[0] + "|" + fields[5]);
          } 
      }   
         
    }
    in.close();
    
    
    // read in file mergefacts.src
    in = new BufferedReader(new FileReader(
        new File(srcFullPath + File.separator + "mergefacts.src")));
    fileLine = "";

    
    // do field and line checks
    // initialize caches
    while ((fileLine = in.readLine()) != null) {

      String[] fields = FieldedStringTokenizer.split(fileLine, "|");
      
      // check each row has the correct number of fields
      if (checkNames.contains("#META_8")) {
        if (fields.length != 12) {
          result.addError(
              "META_8: incorrect number of fields in mergefacts.src row: "
                  + fileLine);
        }
      }
      
      // check self referential mergefacts
      if (checkNames.contains("#META_9")) {
         if (fields[0].equals(fields[2]) && fields[8].equals(fields[10]) && fields[9].equals(fields[11])) {
           result.addError("META_8: self referential mergefacts in mergefacts.src row: "
               + fileLine);
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
        "The names of the validation checks to run", "e.g. #META_1", 200,
        AlgorithmParameter.Type.MULTI, "");

    List<String> validationChecks = new ArrayList<>();
    validationChecks.add("#META_1");
    validationChecks.add("#META_2");
    validationChecks.add("#META_3");
    validationChecks.add("#META_4");
    validationChecks.add("#META_5");
    validationChecks.add("#META_6");
    validationChecks.add("#META_7");
    validationChecks.add("#META_8");
    validationChecks.add("#META_9");
    validationChecks.add("#META_10");
    
    Collections.sort(validationChecks);
    param.setPossibleValues(validationChecks);
    params.add(param);
    
    return params;
  }

  @Override
  public String getDescription() {
    return "Validation checks related to metadata in the inversion files.";
  }
}