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
  'configureService',
  'projectService',
  'reportService', 
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    workflowService, configureService, projectService, reportService, $uibModal) {
    console.debug("configure WorkflowCtrl");

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Workflow');

    $scope.user = securityService.getUser();
    $scope.binTypeOptions = []; 
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';
    // TODO: need to bootstrap this
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

          $scope.projects = data;
          $scope.projects = data;
          $scope.currentProject = $scope.projects.projects[0];

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
      $scope.getBins($scope.currentProject.id, binType);
    }
    
    // Retrieve all bins with project and type
    $scope.getBins = function(projectId, type) {
      console.debug('getBins', projectId, type);

      workflowService.getWorkflowBins(projectId, type).then(function(response) {
        $scope.bins = response;
        $scope.bins.totalCount = $scope.bins.length;
      });
    };

    // Set the project
    $scope.setProject = function(project) {
      $scope.currentProject = project;
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
        for (i=0; i<$scope.workflowConfigs.length; i++) {
          $scope.binTypeOptions.push($scope.workflowConfigs[i].type);
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

    // Get $scope.records
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

        workflowService.findTrackingRecordsForWorkflowBin(bin.projectId, bin.id,
          pfs).then(
        // Success
        function(data) {
          bin.records = data.worklists;
          bin.records.totalCount = data.totalCount;
        });
 

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
    
    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };
    
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

    
  } ]);