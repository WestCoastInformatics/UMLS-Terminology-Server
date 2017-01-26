/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractReportAlgorithm extends AbstractAlgorithm {

  /** The email. */
  private String email;

  /**
   * Instantiates an empty {@link AbstractReportAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractReportAlgorithm() throws Exception {
    // n/a
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters
   * @throws Exception the exception
   */
  @Override
  public void setParameters(List<AlgorithmParameter> parameters)
    throws Exception {
    final Properties props = new Properties();
    for (final AlgorithmParameter param : parameters) {
      props.setProperty(param.getFieldName(), param.getValue());
    }
    setProperties(props);
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
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("email") != null) {
      email = p.getProperty("email");
    }
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
