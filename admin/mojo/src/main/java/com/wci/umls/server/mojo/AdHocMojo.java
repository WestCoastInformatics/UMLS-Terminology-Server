/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which performs an ad hoc task.
 * 
 * See admin/db/pom.xml for sample usage
 * 
 * @goal ad-hoc
 * @phase package
 */
public class AdHocMojo extends AbstractMojo {

  /** The terminology. */
  String terminology = "UMLS";

  /** The version. */
  String version = "latest";

  /**
   * Instantiates a {@link AdHocMojo} from the specified parameters.
   */
  public AdHocMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {

      getLog().info("Ad Hoc Mojo");

      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
