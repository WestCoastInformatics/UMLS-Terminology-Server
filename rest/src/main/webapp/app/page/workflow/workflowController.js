// Workflow controller
tsApp.controller('WorkflowCtrl', [
  '$scope',
  '$http',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'workflowService',
  'utilService',
  'configureService',
  'projectService',
  'reportService', 
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    workflowService, utilService, configureService, projectService, reportService, $uibModal) {
    console.debug("configure WorkflowCtrl");

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Workflow');

    $scope.user = securityService.getUser();
    $scope.projectRole;
    $scope.binTypeOptions = []; 
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';
    $scope.currentProject = {id : 1239500};
    $scope.recordTypes = [ 'N', 'R' ];
    $scope.projects;
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
    
    // Configure tab and accordion
    $scope.configureTab = function() {
      $scope.user.userPreferences.lastTab = '/workflow';
      //securityService.updateUserPreferences($scope.user.userPreferences);
    };

   // Workflow Bins Changed handler
    $scope.$on('workflow:workflowBinsChanged', function(event, data) {
      console.debug('on workflow:workflowBinsChanged', data);
      $scope.getBins($scope.currentProject.id, $scope.currentBinType);
    });
    
    //
    // Initialize
    //

    $scope.initialize = function() {

      // Handle users with user preferences
      if ($scope.user.userPreferences) {
        $scope.configureTab();
      }
      
      projectService.getProjects().then(
        // success
        function(data) {

          $scope.projects = data.projects;
          $scope.currentProject = $scope.projects[0];
          $scope.projectRole = $scope.currentProject.userRoleMap[$scope.user.userName];
          if ($scope.projectRole == 'ADMINISTRATOR') {
            $scope.roleOptions = [ 'ADMINISTRATOR', 'REVIEWER', 'AUTHOR' ];
          } else if ($scope.projectRole == 'REVIEWER') {
            $scope.roleOptions = [ 'REVIEWER', 'AUTHOR' ];
          } else if ($scope.projectRole == 'AUTHOR') {
            $scope.roleOptions = [ 'AUTHOR' ];
          }
          
          $scope.getBins($scope.currentProject.id, $scope.currentBinType);
          $scope.getBinTypes();
        });
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

    // Set the bin type
    $scope.setBinType = function(binType) {
      $scope.currentBinType = binType;
      //$scope.user.userPreferences.binType = $scope.currentBinType;
      //securityService.updateUserPreferences($scope.user.userPreferences);
      for (var i = 0; i < $scope.workflowConfigs.length; i++) {
        if ($scope.workflowConfigs[i].type == binType) {
          $scope.currentWorkflowConfig = $scope.workflowConfigs[i];
        }
      }
      $scope.getBins($scope.currentProject.id, binType);
    }
    
    // Retrieve all bins with project and type
    $scope.getBins = function(projectId, type) {
      console.debug('getBins', projectId, type);

      workflowService.getWorkflowBins(projectId, type).then(function(response) {
        $scope.bins = response.worklists;
        $scope.bins.totalCount = $scope.bins.length;
      });
    };

    // Set the project
    $scope.setProject = function(project) {
      $scope.currentProject = project;
      workflowService.fireProjectChanged($scope.currentProject);
      //$scope.user.userPreferences.project = $scope.currentProject;
      //securityService.updateUserPreferences($scope.user.userPreferences);
      //$scope.getProjects();

      $scope.getBins($scope.currentProject.id, $scope.currentBinType);
    }
    
    // Retrieve all projects
    $scope.getProjects = function() {
      console.debug('getProjects');

      projectService.getProjects().then(function(response) {
        $scope.projects = response;
        $scope.currentProject = $scope.projects.projects[0];

        $scope.getBins($scope.currentProject.id, $scope.currentBinType);
      });
    };
    
    // Retrieve all projects
    $scope.getBinTypes = function() {
      console.debug('getBinTypes');

      workflowService.getWorkflowConfigs($scope.currentProject.id).then(function(response) {
        $scope.workflowConfigs = response;
        for (var i=0; i<$scope.workflowConfigs.totalCount; i++) {
          $scope.binTypeOptions.push($scope.workflowConfigs.worklists[i].type);
        }
        if ($scope.binTypeOptions.length == 1) {
          $scope.setBinType($scope.binTypeOptions[0]);
        }
      });
    };
    
    // Selects a bin (setting $scope.selected.bin)    
    // clusterType is optional
    $scope.selectBin = function(bin, clusterType) {
      $scope.selected.bin = bin;   
      $scope.selected.clusterType = clusterType;
      
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
      reportService.getConceptReport($scope.currentProject.id, $scope.selected.concept.id).then(
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
        queryRestriction : $scope.paging['record'].filter != undefined && 
          $scope.paging['record'].filter != "" ? $scope.paging['record'].filter
          : null
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

        workflowService.findTrackingRecordsForWorkflowBin($scope.currentProject.id, bin.id,
          pfs).then(
        // Success
        function(data) {
          bin.records = data.worklists;
          bin.records.totalCount = data.totalCount;
        });
 

    };


    
    // Regenerate bins
    $scope.regenerateBins = function() {
      console.debug('clear and regenerateBins');
      workflowService.clearBins($scope.currentProject.id, $scope.currentBinType).then(
        function(response) {

          workflowService.regenerateBins($scope.currentProject.id, $scope.currentBinType).then(
            function(response) {
              $scope.getBins($scope.currentProject.id, $scope.currentBinType);
            });
        });
    };
    
    // enable/disable
    $scope.toggleEnable = function(bin) {
      console.debug('enable/disable bin');
      workflowService.getWorkflowBinDefinition($scope.currentProject.id, bin.name, $scope.currentBinType).then(
        function(response) {
          var workflowBinDefinition = response;
          if (workflowBinDefinition.enabled) {
            workflowBinDefinition.enabled = false;
          } else {
            workflowBinDefinition.enabled = true;
          }
          workflowService.updateWorkflowBinDefinition($scope.currentProject.id, workflowBinDefinition).then(
            function(response) {
              $scope.regenerateBins();
            });
        });
    };
    
    // remove bin/definition
    $scope.removeBin = function(bin) {
      console.debug('remove bin/definition');
      if(!window.confirm('Are you sure you want to remove the ' + bin.name + ' bin?')) {
        return;
      }
      workflowService.getWorkflowBinDefinition($scope.currentProject.id, bin.name, $scope.currentBinType).then(
        function(response) {
          var workflowBinDefinition = response;
          
          workflowService.removeWorkflowBinDefinition(workflowBinDefinition).then(
            function(response) {
              $scope.regenerateBins();
            },
            // Error
            function(data) {
              handleError($scope.errors, data);
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
            return $scope.currentProject.id;
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
            return $scope.currentProject.id;
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
        $scope.getBins($scope.currentProject.id, $scope.currentBinType);
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
          binType : function() {
            return $scope.currentBinType;
          },
          project : function() {
            return $scope.currentProject;
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
          binType : function() {
            return $scope.currentBinType;
          },
          project : function() {
            return $scope.currentProject;
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
          binType : function() {
            return $scope.currentBinType;
          },
          project : function() {
            return $scope.currentProject;
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
  } ]);