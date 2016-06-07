/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultComputePreferredNameHandler;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;

/**
 * Integration testing for {@link DefaultComputePreferredNameHandler}.
 */
public class ProjectJpaTest {

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    // n/a
  }

  /**
   * /** Test normal use of the handler object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testProjectNormalUse001() throws Exception {
    Logger.getLogger(getClass()).info("TEST testProjectNormalUse001");


    ProjectService projectService = new ProjectServiceJpa();
    SecurityService securityService = new SecurityServiceJpa();
    try {
      // Add Project
      User adminUser = securityService.getUser("admin");
      User guestUser = securityService.getUser("guest");

      Project project = new ProjectJpa();
      project.setName("Test Project");
      project.setLastModifiedBy("dss");
      project.setLastModified(new Date());
      project.setTerminology("SNOMECT");
      project.setBranch("branch");
      project.setDescription("Test Project Description");
      project.setFeedbackEmail("dshapiro@wcinformatics.com");
      PrecedenceList precedenceList = new PrecedenceListJpa();
      KeyValuePairList prec = new KeyValuePairList();
      KeyValuePair pair = new KeyValuePair("MTH", "PN");
      prec.addKeyValuePair(pair);
      pair = new KeyValuePair("RXNORM", "MIN");
      prec.addKeyValuePair(pair);
      precedenceList.setPrecedence(prec);
      // TODO:
      project.setPrecedenceList(null);
      project.setPublic(true);
      Map<String, String> semanticTypeCategoryMap = new HashMap<>();
      semanticTypeCategoryMap.put("T001", "CHEMICAL");
      semanticTypeCategoryMap.put("T002", "CHEMICAL");
      project.setSemanticTypeCategoryMap(semanticTypeCategoryMap);
      Map<User, UserRole> userRoleMap = new HashMap<>();
      userRoleMap.put(adminUser, UserRole.ADMINISTRATOR);
      project.setUserRoleMap(userRoleMap);
      List<String> validationChecks = new ArrayList<>();
      validationChecks.add("validation check 1");
      validationChecks.add("validation check 2");
      project.setValidationChecks(validationChecks);
      List<String> validCategories = new ArrayList<>();
      validCategories.add("category1");
      validCategories.add("category2");
      project.setValidCategories(validCategories);

      Project addedProject = projectService.addProject(project);
      Logger.getLogger(getClass()).info(project);
      assertEquals(project.getName(), "Test Project");

      Project retrievedProject =
          projectService.getProject(addedProject.getId());
      assertEquals(project, retrievedProject);
      assertEquals(project.getPrecedenceList(),
          retrievedProject.getPrecedenceList());
      assertEquals(project.getValidCategories(),
          retrievedProject.getValidCategories());
      assertEquals(project.getValidationChecks(),
          retrievedProject.getValidationChecks());

      project.setName("Test Project2");
      project.setLastModifiedBy("dss2");
      project.setLastModified(new Date());
      project.setTerminology("SNOMECT2");
      project.setBranch("branch2");
      project.setDescription("Test Project Description2");
      project.setFeedbackEmail("dshapiro2@wcinformatics.com");
      precedenceList.removeTerminologyTermType("MTH", "PN");
      precedenceList.addTerminologyTermType("MSH", "MH");
      // TODO:
      project.setPrecedenceList(null);
      project.setPublic(false);
      semanticTypeCategoryMap.remove("T001", "CHEMICAL");
      semanticTypeCategoryMap.put("T003", "NON-CHEMICAL");
      project.setSemanticTypeCategoryMap(semanticTypeCategoryMap);

      userRoleMap.remove(adminUser, UserRole.ADMINISTRATOR);
      userRoleMap.put(guestUser, UserRole.VIEWER);
      project.setUserRoleMap(userRoleMap);
      validationChecks.remove("validation check 1");
      validationChecks.add("validation check 3");
      project.setValidationChecks(validationChecks);
      validCategories.remove("category1");
      validCategories.add("category3");
      project.setValidCategories(validCategories);

      projectService.updateProject(project);

      Project updatedProject = projectService.getProject(addedProject.getId());
      assertEquals(project, updatedProject);
      assertEquals(project.getPrecedenceList(),
          updatedProject.getPrecedenceList());
      assertEquals(project.getValidCategories(),
          updatedProject.getValidCategories());
      assertEquals(project.getValidationChecks(),
          updatedProject.getValidationChecks());

      projectService.removeProject(project.getId());

    } catch (Exception e) {
      throw e;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
