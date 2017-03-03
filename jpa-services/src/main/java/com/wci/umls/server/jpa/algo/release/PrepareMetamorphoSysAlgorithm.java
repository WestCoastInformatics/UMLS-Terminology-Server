/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm to prepare MetamorphoSys.
 */
public class PrepareMetamorphoSysAlgorithm extends AbstractAlgorithm {

  /** The email. */
  private String email;

  /**
   * Instantiates an empty {@link PrepareMetamorphoSysAlgorithm}.
   *
   * @throws Exception the exception
   */
  public PrepareMetamorphoSysAlgorithm() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    // Check the process input path
    final String path =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    final File pathAsFile = new File(path);
    if (!pathAsFile.exists()) {
      throw new LocalException(
          "Input path specified in process does not exist");
    }

    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // Send an email
    final Properties p = ConfigUtility.getConfigProperties();
    if (email != null) {
      final String mailTo =
          email != null ? email : p.getProperty("mail.smtp.to");
      logInfo("  sending email = " + mailTo);
      ConfigUtility.sendEmail(
          "Prepare MetamorphoSys for " + getProcess().getTerminology()
              + " Release " + getProcess().getVersion(),
          p.getProperty("mail.smtp.from"), mailTo,
          "Prepare MetamorphoSys for release. \n\nPut mmsys.zip file into "
              + p.getProperty("source.data.dir") + "/"
              + getProcess().getInputPath() + "/mr/mmsys.zip\n\n"
              + "  Prepare config/" + getProcess().getVersion()
              + ", including release.dat and mmsys.prop",
          p);
    } else {
      logInfo("  DO NOT send email");

    }
    logInfo("Finished " + getName());
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("email") != null) {
      email = p.getProperty("email");
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    final AlgorithmParameter email = new AlgorithmParameterJpa(
        "Notification emails", "email", "Email addresses for notification",
        "e.g. a@b.com", 4000, AlgorithmParameter.Type.TEXT,
        ConfigUtility.getConfigProperties().getProperty("mail.smtp.to"));
    params.add(email);
    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }

  /**
   * Returns the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email.
   *
   * @param email the email
   */
  public void setEmail(String email) {
    this.email = email;
  }
}
