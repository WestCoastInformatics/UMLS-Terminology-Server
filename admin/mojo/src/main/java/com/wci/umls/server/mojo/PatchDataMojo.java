/**
 * Copyright 2015 West Coast Informatics, LLC
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

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 */
@Mojo(name = "patch", defaultPhase = LifecyclePhase.PACKAGE)
public class PatchDataMojo extends AbstractMojo {

  /** The start. */
  @Parameter
  String start;

  /** The end. */
  @Parameter
  String end;

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public PatchDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Patch data");
      getLog().info("  start = " + start);
      getLog().info("  end = " + end);

      // Patch 20170101 - SAMPLE
      // See corresponding src/main/resources/patchData20170101.sql file
      if ("20170101".compareTo(start) >= 0 && "20170101".compareTo(end) <= 0) {
        // tbd
      }

      getLog().info("Done ...");
    } catch (Exception e) {
      Logger.getLogger(getClass()).error("Unexpected exception", e);
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
