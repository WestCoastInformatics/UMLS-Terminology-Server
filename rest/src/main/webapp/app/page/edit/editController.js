// Edit controller
tsApp.controller('EditCtrl', [
  '$scope',
  '$http',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'configureService',
  'securityService',
  'workflowService',
  'utilService',
  'configureService',
  'projectService',
  'reportService',
  'metaEditingService',
  'contentService',
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, configureService,
    securityService, workflowService, utilService, configureService, projectService, reportService,
    metaEditingService, contentService, $uibModal) {
    console.debug("configure EditCtrl");

    tabService.setShowing(true);

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on 'back' and 'reload' events button
    tabService.setSelectedTabByLabel('Edit');

    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();

    $scope.projectRole = null;
    $scope.project = null;
    $scope.recordTypes = [ 'N', 'R' ];
    $scope.modes = [ 'Available', 'Assigned', 'Checklists' ];
    $scope.mode = $scope.modes[0];
    $scope.projects = [];
    $scope.selected = {
      bin : null,
      worklist : null,
      clusterType : null,
      record : null,
      concept : null
    };
    $scope.data = [];

    // Paging variables
    $scope.visibleSize = 4;
    $scope.pageSize = 5;
    $scope.paging = {};
    $scope.paging['worklist'] = {
      page : 1,
      filter : '',
      typeFilter : '',
      sortField : 'name',
      ascending : true
    };
    $scope.paging['record'] = {
      page : 1,
      filter : '',
      typeFilter : '',
      sortField : 'clusterId',
      ascending : true
    };

    // Get worklists/checklists
    $scope.getWorklists = function() {
      if ($scope.mode == 'Assigned') {
        $scope.getAssignedWorklists($scope.project.id, $scope.projectRole);
      } else if ($scope.mode == 'Available') {
        $scope.getAvailableWorklists($scope.project.id, $scope.projectRole);
      } else if ($scope.mode == 'Checklists') {
        $scope.findChecklists($scope.project.id, $scope.projectRole);
      }
    }

    // Retrieve all available worklists with project and type
    $scope.getAvailableWorklists = function(projectId) {
      console.debug('getAvailableWorklists', projectId);

      var pfs = {
        startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging['worklist'].sortField,
        ascending : $scope.paging['worklist'].ascending == null ? false
          : $scope.paging['worklist'].ascending,
        queryRestriction : $scope.paging['worklist'].filter != undefined
          && $scope.paging['worklist'].filter != "" ? $scope.paging['worklist'].filter : null
      };

      workflowService.findAvailableWorklists(projectId, $scope.user.userName, $scope.projectRole,
        pfs).then(function(response) {
        $scope.worklists = response.worklists;
        $scope.worklists.totalCount = $scope.worklists.length;
        resetSelected();
      });
    };

    // Retrieve assigned worklists with project and type
    $scope.getAssignedWorklists = function(projectId, role) {
      console.debug('getAssignedWorklists', projectId, role);

      var pfs = {
        startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging['worklist'].sortField,
        ascending : $scope.paging['worklist'].ascending == null ? false
          : $scope.paging['worklist'].ascending,
        queryRestriction : $scope.paging['worklist'].filter != undefined
          && $scope.paging['worklist'].filter != "" ? $scope.paging['worklist'].filter : null
      };

      workflowService.findAssignedWorklists(projectId, $scope.user.userName, role, pfs).then(
        function(response) {
          $scope.worklists = response.worklists;
          $scope.worklists.totalCount = $scope.worklists.length;
          resetSelected();
        });
    };

    $scope.findChecklists = function(projectId, role) {
      var pfs = {
        startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging['worklist'].sortField,
        ascending : $scope.paging['worklist'].ascending == null ? false
          : $scope.paging['worklist'].ascending,
        queryRestriction : $scope.paging['worklist'].filter != undefined
          && $scope.paging['worklist'].filter != "" ? $scope.paging['worklist'].filter : null
      };

      workflowService.findChecklists(projectId, $scope.query, pfs).then(function(data) {
        $scope.worklists = data.checklists;
        $scope.worklists.totalCount = data.totalCount;
        resetSelected();
      });
    }

    // Set the project
    $scope.setProject = function(project) {
      $scope.project = project;

      // Get role for project (requires a lookup and will save user prefs
      projectService.getRoleForProject($scope.user, $scope.project.id).then(
      // Success
      function(data) {
        // Get role and set role options
        $scope.projects.role = data;
        $scope.roleOptions = projectService.getRoleOptions($scope.projects.role);

        // Get worklists
        $scope.getWorklists();

        // Fire project changed (for directives)
        projectService.fireProjectChanged($scope.project);
      });

    }

    // Retrieve all projects
    $scope.getProjects = function() {
      projectService.getProjectsForUser($scope.user).then(
      // Success
      function(data) {
        $scope.projects = data.projects;
        $scope.setProject(data.project);
      });

    };

    // Selects a worklist (setting $scope.selected.worklist).
    // Looks up current release info and records.
    $scope.selectWorklist = function(worklist) {
      $scope.selected.worklist = worklist;
      $scope.selected.concept = null;
      if ($scope.value == 'Worklist') {
        $scope.parseStateHistory(worklist);
      }
      $scope.getRecords(worklist, true);
    };

    // select record from 'Cluster' list
    $scope.selectRecord = function(record) {
      $scope.selected.record = record;
      $scope.data = [];

      for (var i = 0; i < record.concepts.length; i++) {
        contentService.getConcept(record.concepts[i].id, $scope.project.id).then(function(data) {
          $scope.data.push(data);
        }, function(data) {
          handleError($scope.errors, data);
        });
      }
    }

    // from the worklist, select the record after the currently selected one
    $scope.selectNextRecord = function(record) {
      for (var i = 0; i < $scope.selected.worklist.records.length - 1; i++) {
        if (record.id == $scope.selected.worklist.records[i].id) {
          $scope.selectRecord($scope.selected.worklist.records[++i]);
          return;
        }
      }
      // next record is not loaded, need to get more
      $scope.paging['record'].page += 1;
      $scope.getRecords($scope.selected.worklist, true);
    }

    // select concept & get concept report
    $scope.selectConcept = function(concept) {
      $scope.selected.concept = {
        terminologyId : concept.terminologyId,
        terminology : concept.terminology,
        version : concept.version,
        id : concept.id
      };
      reportService.getConceptReport($scope.project.id, $scope.selected.concept.id).then(
      // Success
      function(data) {
        $scope.selected.concept.report = data;
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });

      // keep concept list selection in-synch
      for (var i = 0; i < $scope.data.length; i++) {
        if (concept.id == $scope.data[i].id) {
          $scope.selected.data = $scope.data[i];
        }
      }
    };

    // Get $scope.records
    $scope.getRecords = function(worklist, selectFirst) {

      var pfs = {
        startIndex : ($scope.paging['record'].page - 1) * $scope.pageSize,
        maxResults : $scope.pageSize,
        sortField : $scope.paging['record'].sortField,
        ascending : $scope.paging['record'].ascending == null ? false
          : $scope.paging['record'].ascending,
        queryRestriction : $scope.paging['record'].filter != undefined
          && $scope.paging['record'].filter != "" ? $scope.paging['record'].filter : null
      };

      if ($scope.paging['record'].typeFilter) {
        var value = $scope.paging['record'].typeFilter;

        // Handle inactive
        if (value == 'N') {
          if (pfs.queryRestriction != null)
            pfs.queryRestriction += ' AND workflowStatus:NEEDS_REVIEW';
          else
            pfs.queryRestriction = 'workflowStatus:NEEDS_REVIEW';
        } else if (value == 'R') {
          if (pfs.queryRestriction != null)
            pfs.queryRestriction += ' AND workflowStatus:READY_FOR_PUBLICATION';
          else
            pfs.queryRestriction = 'workflowStatus:READY_FOR_PUBLICATION';
        }

      }

      if ($scope.mode == 'Available' || $scope.mode == 'Assigned') {

        workflowService.findTrackingRecordsForWorklist(worklist.projectId, worklist.id, pfs).then(
        // Success
        function(data) {
          worklist.records = data.worklists;
          worklist.records.totalCount = data.totalCount;
          if (selectFirst) {
            $scope.selectRecord(worklist.records[0]);
          }
        });
      } else if ($scope.mode == 'Checklists') {
        workflowService.findTrackingRecordsForChecklist(worklist.projectId, worklist.id, pfs).then(
        // Success
        function(data) {
          worklist.records = data.worklists;
          worklist.records.totalCount = data.totalCount;
        });
      }

    };

    // finish worklist
    $scope.performWorkflowAction = function(worklist) {
      workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
        $scope.project.userRoleMap[$scope.user.userName], 'FINISH').then(
      // Success
      function(data) {
        $scope.initialize();
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    }

    // claim worklist
    $scope.assignWorklistToSelf = function(worklist) {
      workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
        $scope.project.userRoleMap[$scope.user.userName], 'ASSIGN').then(
      // Success
      function(data) {

        // If user has a team, update worklist
        securityService.getUserByName($scope.user.userName).then(

        // Success
        function(data) {
          $scope.user = data;
          if ($scope.user.team) {
            worklist.team = $scope.user.team;
          }
          $scope.getWorklists();
        },
        // Error
        function(data) {
          handleError($scope.errors, data);
        });
      },
      // Error
      function(data) {
        handleError($scope.errors, data);
      });
    }

    // unassign worklist
    $scope.unassignWorklist = function(worklist) {
      var editor;
      if ($scope.projectRole == 'AUTHOR') {
        editor = $scope.joinAuthors(worklist).trim();
      } else if ($scope.projectRole == 'REVIEWER') {
        editor = $scope.joinReviewers(worklist).trim();
      }
      workflowService.performWorkflowAction($scope.project.id, worklist.id, editor,
        $scope.project.userRoleMap[$scope.user.userName], 'UNASSIGN').then(
      // Success
      function(data) {
        $scope.getWorklists();
      });
    };

    // return string of authors
    $scope.joinAuthors = function(worklist) {
      var joinedAuthors = '';
      var editors = new Array();
      if (worklist.authors && worklist.authors.length > 0) {
        editors = worklist.authors;
      }
      for (var i = 0; i < editors.length; i++) {
        joinedAuthors += editors[i];
        joinedAuthors += ' ';
      }
      return joinedAuthors;
    };

    // return string of reviewers
    $scope.joinReviewers = function(worklist) {
      var joinedReviewers = '';
      var editors = new Array();
      if (worklist.reviewers && worklist.reviewers.length > 0) {
        editors = worklist.reviewers;
      }
      for (var i = 0; i < editors.length; i++) {
        joinedReviewers += editors[i];
        joinedReviewers += ' ';
      }
      return joinedReviewers;
    };

    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };

    // Approve concept
    $scope.approveConcept = function(concept) {
      contentService.getConcept(concept.id, $scope.project.id).then(function(data) {
        var concept = data;
        metaEditingService.approveConcept($scope.project.id, concept, false).then(
        // Success
        function(data) {

        },
        // Error
        function(data) {
          handleError($scope.errors, data);
        });
      }, function(data) {
        handleError($scope.errors, data);
      });

    }

    // Approves all selector concepts and moves on to next record
    $scope.approveNext = function() {
      for (var i = 0; i < $scope.data.length; i++) {
        $scope.approveConcept($scope.data[i]);
      }
      $scope.selectNextRecord($scope.selected.record);
    }

    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError(errors, error);
    }

    // unselects options from all tables
    function resetSelected() {
      $scope.selected = {
        bin : null,
        clusterType : null,
        record : null,
        concept : null
      };
    }

    //
    // MODALS
    //

    // Merge modal
    $scope.openMergeModal = function(lconcept) {
      console.debug('openMergeModal ', lconcept);
      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/merge.html',
        controller : MergeModalCtrl,
        backdrop : 'static',
        resolve : {
          concept : function() {
            return $scope.selected.data;
          },
          data : function() {
            return $scope.data;
          },
          project : function() {
            return $scope.project;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.handleWorkflow(data);
      });

    };

    // Add concept modal
    $scope.openAddConceptModal = function(lrecord) {
      console.debug('openAddConceptModal ', lrecord);
      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/edit/addConcept.html',
        controller : AddConceptModalCtrl,
        backdrop : 'static',
        size : 'lg',
        resolve : {
          record : function() {
            return lrecord;
          },
          project : function() {
            return $scope.project;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        contentService.getConcept(data.id, $scope.project.id).then(function(data) {
          $scope.data.push(data);
        }, function(data) {
          handleError($scope.errors, data);
        });
      });

    };

    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {

      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/workflow');

      // Get projects
      $scope.getProjects();
      $scope.getWorklists();
    };

    //
    // Initialization: Check that application is configured
    //
    configureService.isConfigured().then(function(isConfigured) {
      if (!isConfigured) {
        $location.path('/configure');
      } else {
        $scope.initialize();
      }
    });

    // end
  } ]);