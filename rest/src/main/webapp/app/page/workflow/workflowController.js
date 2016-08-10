// Workflow controller
tsApp.controller('WorkflowCtrl', [
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
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, configureService,
    securityService, workflowService, utilService, configureService, projectService, reportService,
    $uibModal) {
    console.debug("configure WorkflowCtrl");

    tabService.setShowing(true);

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on 'back' and 'reload' events button
    tabService.setSelectedTabByLabel('Workflow');

    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();

    $scope.projectRole = null;
    $scope.configs = [];
    $scope.config = null;
    $scope.projects = [];
    $scope.project = null;
    $scope.recordTypes = [ 'N', 'R' ];
    $scope.selected = {
      bin : null,
      clusterType : null
    };

    // Paging variables
    $scope.visibleSize = 4;
    $scope.pageSize = 10;
    $scope.paging = {};
    $scope.paging['record'] = {
      page : 1,
      filter : '',
      typeFilter : '',
      sortField : 'clusterId',
      ascending : true
    };

    // Set the workflow config
    $scope.setConfig = function(config) {
      $scope.config = config;
      $scope.getBins($scope.project.id, config);
    }

    // Retrieve all bins with project and type
    $scope.getBins = function(projectId, config) {
      workflowService.getWorkflowBins(projectId, config.type).then(
      // Success
      function(data) {
        $scope.bins = data.worklists;
        $scope.bins.totalCount = $scope.bins.length;
      });
    };

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

        // Get bins
        $scope.getConfigs();

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

    // Retrieve all projects
    $scope.getConfigs = function() {
      workflowService.getWorkflowConfigs($scope.project.id).then(
      // Success
      function(data) {
        $scope.configs = data;
        if ($scope.configs.length == 1) {
          $scope.setConfig($scope.configs[0]);
        }
      });
    };

    // Selects a bin (setting $scope.selected.bin)
    // clusterType is optional
    $scope.selectBin = function(bin, clusterType) {
      $scope.selected.bin = bin;
      $scope.selected.clusterType = clusterType;
      $scope.selected.concept = null;

      if (clusterType && clusterType == 'default') {
        $scope.paging['record'].filter = ' NOT clusterType:[* TO *]';
      } else if (clusterType && clusterType != 'all') {
        $scope.paging['record'].filter = clusterType;
      } else if (clusterType == 'all') {
        $scope.paging['record'].filter = '';
      }
      $scope.getRecords($scope.selected.bin);
    };

    // Selects a concept (setting $scope.selected.concept)
    $scope.selectConcept = function(concept) {
      // Set the concept for display
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
      });
    };

    // Get records
    $scope.getRecords = function(bin) {

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

      workflowService.findTrackingRecordsForWorkflowBin($scope.project.id, bin.id, pfs).then(
      // Success
      function(data) {
        bin.records = data.worklists;
        bin.records.totalCount = data.totalCount;
      });

    };

    // Regenerate bins
    $scope.regenerateBins = function() {
      console.debug('clear and regenerateBins');
      workflowService.clearBins($scope.project.id, $scope.config).then(
      // Success
      function(response) {
        workflowService.regenerateBins($scope.project.id, $scope.config.type).then(
        // Success
        function(response) {
          $scope.getBins($scope.project.id, $scope.config.type);
        });
      });
    };

    // enable/disable
    $scope.toggleEnable = function(bin) {
      console.debug('enable/disable bin');
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(
          function(response) {
            var workflowBinDefinition = response;
            if (workflowBinDefinition.enabled) {
              workflowBinDefinition.enabled = false;
            } else {
              workflowBinDefinition.enabled = true;
            }
            workflowService.updateWorkflowBinDefinition($scope.project.id, workflowBinDefinition)
              .then(function(response) {
                $scope.regenerateBins();
              });
          });
    };

    // remove bin/definition
    $scope.removeBin = function(bin) {
      workflowService.getWorkflowBinDefinition($scope.project.id, bin.name, $scope.config.type)
        .then(
        // Success
        function(response) {
          var workflowBinDefinition = response;

          workflowService.removeWorkflowBinDefinition(workflowBinDefinition).then(
          // Successs
          function(response) {
            $scope.regenerateBins();
          });
        });
    };

    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };

    // link to error handling
    function handleError(errors, error) {
      utilService.handleDialogError(errors, error);
    }

    //
    // MODALS
    //

    // Create checklist modal
    $scope.openCreateChecklistModal = function(bin, clusterType) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addChecklist.html',
        backdrop : 'static',
        controller : CreateChecklistModalCtrl,
        resolve : {
          projectId : function() {
            return $scope.project.id;
          },
          bin : function() {
            return bin;
          },
          clusterType : function() {
            return clusterType;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(project) {

      });
    };

    // Create worklist modal
    $scope.openCreateWorklistModal = function(bin, clusterType, availableClusterCt) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addWorklist.html',
        backdrop : 'static',
        controller : CreateWorklistModalCtrl,
        resolve : {
          projectId : function() {
            return $scope.project.id;
          },
          bin : function() {
            return bin;
          },
          user : function() {
            return $scope.user;
          },
          clusterType : function() {
            return clusterType;
          },
          availableClusterCt : function() {
            return availableClusterCt;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(project) {
        $scope.getBins($scope.project.id, $scope.config);
      });
    };

    // Edit bin modal
    $scope.openEditBinModal = function(lbin) {
      console.debug('openEditBinModal ');

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : EditBinModalCtrl,
        backdrop : 'static',
        resolve : {
          bin : function() {
            return lbin;
          },
          workflowConfig : function() {
            return $scope.currentWorkflowConfig;
          },
          bins : function() {
            return $scope.bins;
          },
          config : function() {
            return $scope.config;
          },
          project : function() {
            return $scope.project;
          },
          projects : function() {
            return $scope.projects;
          },
          action : function() {
            return 'Edit';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.regenerateBins();
      });
    };

    // Clone bin modal
    $scope.openCloneBinModal = function(lbin) {
      console.debug('openCloneBinModal ');

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : EditBinModalCtrl,
        backdrop : 'static',
        resolve : {
          bin : function() {
            return lbin;
          },
          workflowConfig : function() {
            return $scope.currentWorkflowConfig;
          },
          bins : function() {
            return $scope.bins;
          },
          config : function() {
            return $scope.config;
          },
          project : function() {
            return $scope.project;
          },
          projects : function() {
            return $scope.projects;
          },
          action : function() {
            return 'Clone';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.regenerateBins();
      });
    };

    // Add bin modal
    $scope.openAddBinModal = function(lbin) {
      console.debug('openAddBinModal ');

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/editBin.html',
        controller : EditBinModalCtrl,
        backdrop : 'static',
        resolve : {
          bin : function() {
            return undefined;
          },
          workflowConfig : function() {
            return $scope.currentWorkflowConfig;
          },
          bins : function() {
            return $scope.bins;
          },
          config : function() {
            return $scope.config;
          },
          project : function() {
            return $scope.project;
          },
          projects : function() {
            return $scope.projects;
          },
          action : function() {
            return 'Add';
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(data) {
        $scope.regenerateBins();
      });
    };

    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {

      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/workflow');

      $scope.getProjects();
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