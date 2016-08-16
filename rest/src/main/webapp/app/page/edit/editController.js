// Edit controller
tsApp.controller('EditCtrl',
  [
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
      securityService, workflowService, utilService, configureService, projectService,
      reportService, metaEditingService, contentService, $uibModal) {
      console.debug("configure EditCtrl");

      // Set up tabs and controller
      tabService.setShowing(true);
      utilService.clearError();
      tabService.setSelectedTabByLabel('Edit');
      $scope.user = securityService.getUser();
      projectService.getUserHasAnyRole();

      // Selected variables
      $scope.selected = {
        project : null,
        projectRole : null,
        worklist : null,
        record : null,
        concept : null,
        worklistMode : 'Available'
      };

      // Lists
      $scope.lists = {
        records : [],
        configs : [],
        projects : [],
        concepts : [],
        projectRoles : [],
        recordTypes : workflowService.getRecordTypes(),
        worklistModes : [ 'Available', 'Assigned', 'Checklists' ]
      }

      // Paging variables
      $scope.resetPaging = function() {
        $scope.paging = {};
        $scope.paging['worklists'] = utilService.getPaging();
        $scope.paging['worklists'].sortField = 'lastModified';
        $scope.paging['worklists'].pageSize = 5;
        $scope.paging['worklists'].callback = {
          getPagedList : getWorklists
        };

        $scope.paging['records'] = utilService.getPaging();
        $scope.paging['records'].sortField = 'clusterId';
        $scope.paging['records'].callback = {
          getPagedList : getRecords
        };
      }
      $scope.resetPaging();

      // Get $scope.lists.worklists
      // switch based on type
      $scope.getWorklists = function() {
        getWorklists();
      }
      function getWorklists() {
        if ($scope.selected.worklistMode == 'Assigned') {
          $scope.getAssignedWorklists($scope.selected.project.id, $scope.selected.projectRole);
        } else if ($scope.selected.worklistMode == 'Available') {
          $scope.getAvailableWorklists($scope.selected.project.id, $scope.selected.projectRole);
        } else if ($scope.selected.worklistMode == 'Checklists') {
          $scope.findChecklists($scope.selected.project.id, $scope.selected.projectRole);
        }
      }

      // Get all available worklists with project and type
      $scope.getAvailableWorklists = function(projectId) {
        var paging = $scope.paging['worklists'];
        var pfs = {
          startIndex : (paging.page - 1) * paging.pageSize,
          maxResults : paging.pageSize,
          sortField : paging.sortField,
          ascending : paging.sortAscending,
          queryRestriction : paging.filter
        };

        workflowService.findAvailableWorklists(projectId, $scope.user.userName,
          $scope.selected.projectRole, pfs).then(
        // Success
        function(data) {
          $scope.lists.worklists = data.worklists;
          $scope.lists.worklists.totalCount = data.totalCount
          $scope.resetSelected();
        });
      };

      // Get assigned worklists with project and type
      $scope.getAssignedWorklists = function(projectId, role) {
        console.debug('getAssignedWorklists', projectId, role);
        var paging = $scope.paging['worklists'];
        var pfs = {
          startIndex : (paging.page - 1) * paging.pageSize,
          maxResults : paging.pageSize,
          sortField : paging.sortField,
          ascending : paging.sortAscending,
          queryRestriction : paging.filter
        };

        workflowService.findAssignedWorklists(projectId, $scope.user.userName, role, pfs).then(
        // Success
        function(data) {
          $scope.lists.worklists = data.worklists;
          $scope.lists.worklists.totalCount = data.totalCount;
          $scope.resetSelected();
        });
      };

      // Find checklists
      $scope.findChecklists = function(projectId, role) {
        var paging = $scope.paging['worklists'];
        var pfs = {
          startIndex : (paging.page - 1) * paging.pageSize,
          maxResults : paging.pageSize,
          sortField : paging.sortField,
          ascending : paging.sortAscending,
          queryRestriction : paging.filter
        };

        workflowService.findChecklists(projectId, $scope.query, pfs).then(
        // Success
        function(data) {
          $scope.lists.worklists = data.checklists;
          $scope.lists.worklists.totalCount = data.totalCount;
          resetSelected();
        });
      }

      // handle change in project role
      $scope.changeProjectRole = function() {
        // save the change
        securityService.saveRole($scope.user.userPreferences, $scope.selected.projectRole);
      }

      // Set the project
      $scope.setProject = function(project) {
        $scope.selected.project = project;

        // Get role for project (requires a lookup and will save user prefs
        projectService.getRoleForProject($scope.user, $scope.selected.project.id).then(
        // Success
        function(data) {
          // Get role and set role options
          $scope.selected.projectRole = data.role;
          $scope.lists.projectRoles = data.options;

          // Get worklists
          $scope.getWorklists();
        });

      }

      // Get all projects for the user
      $scope.getProjects = function() {
        projectService.getProjectsForUser($scope.user).then(
        // Success
        function(data) {
          $scope.lists.projects = data.projects;
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

        // Don't push concepts on if in available modes
        if ($scope.worklistMode != 'Available') {
          $scope.lists.concepts = [];

          for (var i = 0; i < record.concepts.length; i++) {
            contentService.getConcept(record.concepts[i].id, $scope.selected.project.id).then(
              function(data) {
                $scope.lists.concepts.push(data);
              });
          }
        }
      }

      // from the worklist, select the record after the currently selected one
      $scope.selectNextRecord = function(record) {
        for (var i = 0; i < $scope.lists.records.length - 1; i++) {
          if (record.id == $scope.lists.records[i].id) {
            $scope.selectRecord($scope.lists.records[++i]);
            return;
          }
        }
        // next record is not loaded, need to get more
        $scope.paging['records'].page += 1;
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
        reportService.getConceptReport($scope.selected.project.id, $scope.selected.concept.id)
          .then(
          // Success
          function(data) {
            $scope.selected.concept.report = data;
          });

        // keep concept list selection in-synch
        for (var i = 0; i < $scope.lists.concepts.length; i++) {
          if (concept.id == $scope.lists.concepts[i].id) {
            $scope.selected.data = $scope.lists.concepts[i];
          }
        }
      };

      // Get $scope.lists.records
      $scope.getRecords = function(selectFirst) {
        getRecords();
      }
      function getRecords(selectFirst) {
        var paging = $scope.paging['records'];
        var pfs = {
          startIndex : (paging.page - 1) * paging.pageSize,
          maxResults : paging.pageSize,
          sortField : paging.sortField,
          ascending : paging.sortAscending,
          queryRestriction : paging.filter
        };

        if ($scope.paging['records'].typeFilter) {
          var value = $scope.paging['records'].typeFilter;

          // Handle status
          if (value == 'N') {
            pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '') + ' workflowStatus:N*';
          } else if (value == 'R') {
            pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '') + ' workflowStatus:R*';
          }
        }

        if ($scope.selected.worklistMode == 'Available'
          || $scope.selected.worklistMode == 'Assigned') {
          workflowService.findTrackingRecordsForWorklist($scope.selected.project.id,
            $scope.selected.worklist.id, pfs).then(
          // Success
          function(data) {
            $scope.lists.records = data.records;
            $scope.lists.records.totalCount = data.totalCount;
            if (selectFirst) {
              $scope.selectRecord($scope.lists.records[0]);
            }
          });
        } else if ($scope.selected.worklistMode == 'Checklists') {
          workflowService.findTrackingRecordsForChecklist($scope.selected.project.id,
            $scope.selected.worklist.id, pfs).then(
          // Success
          function(data) {
            scope.lists.records = data.records;
            scope.lists.records.totalCount = data.totalCount;
          });
        }

      }
      ;

      // finish worklist
      $scope.performWorkflowAction = function(worklist) {
        workflowService
          .performWorkflowAction($scope.selected.project.id, worklist.id, $scope.user.userName,
            $scope.selected.project.userRoleMap[$scope.user.userName], 'FINISH').then(
          // Success
          function(data) {
            $scope.initialize();
          },
          // Error
          function(data) {
            utilService.handleDialogError($scope.errors, data);
          });
      }

      // claim worklist
      $scope.assignWorklistToSelf = function(worklist) {
        workflowService
          .performWorkflowAction($scope.selected.project.id, worklist.id, $scope.user.userName,
            $scope.selected.project.userRoleMap[$scope.user.userName], 'ASSIGN').then(
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
            });
          });
      }

      // unassign worklist
      $scope.unassignWorklist = function(worklist) {
        var editor;
        if ($scope.selected.projectRole == 'AUTHOR') {
          editor = $scope.joinAuthors(worklist).trim();
        } else if ($scope.selected.projectRole == 'REVIEWER') {
          editor = $scope.joinReviewers(worklist).trim();
        }
        workflowService.performWorkflowAction($scope.selected.project.id, worklist.id, editor,
          $scope.selected.project.userRoleMap[$scope.user.userName], 'UNASSIGN').then(
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

      $scope.getActivityId = function() {
        if ($scope.selected.worklist) {
          return $scope.selected.worklist.name;
        } else {
          return null;
        }
      }
      // Approve concept
      $scope.approveConcept = function(concept) {
        contentService.getConcept(concept.id, $scope.selected.project.id).then(
          function(data) {
            var concept = data;
            metaEditingService.approveConcept($scope.selected.project.id, $scope.getActivityId(),
              concept, false).then(
            // Success
            function(data) {

            });
          });

      }

      // Approves all selector concepts and moves on to next record
      $scope.approveNext = function() {
        for (var i = 0; i < $scope.lists.concepts.length; i++) {
          $scope.approveConcept($scope.lists.concepts[i]);
        }
        $scope.selectNextRecord($scope.selected.record);
      }

      // unselects options from all tables
      $scope.resetSelected = function() {
        $scope.selected.worklist = null;
        $scope.selected.record = null;
        $scope.selected.concept = null;
        $scope.resetPaging();
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
            project : function() {
              return $scope.selected.project;
            },
            activityId : function() {
              return $scope.getActivityId();
            },
            concept : function() {
              return $scope.selected.data;
            },
            data : function() {
              return $scope.lists.concepts;
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
            project : function() {
              return $scope.selected.project;
            },
            activityId : function() {
              return $scope.getActivityId();
            },
            record : function() {
              return lrecord;
            }
          }
        });

        modalInstance.result.then(
        // Success
        function(data) {
          contentService.getConcept(data.id, $scope.selected.project.id).then(
          // Success
          function(data) {
            $scope.lists.concepts.push(data);
          });
        });

      };

      //
      // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
      //
      $scope.initialize = function() {
        // configure tab
        securityService.saveTab($scope.user.userPreferences, '/edit');
        $scope.getProjects();
      };

      //
      // Initialization: Check that application is configured
      //
      configureService.isConfigured().then(
      // Success
      function(isConfigured) {
        if (!isConfigured) {
          $location.path('/configure');
        } else {
          $scope.initialize();
        }
      });

      // end
    } ]);