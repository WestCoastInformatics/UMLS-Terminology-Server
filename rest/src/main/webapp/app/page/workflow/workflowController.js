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
  '$uibModal',
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    workflowService, configureService, projectService, $uibModal) {
    console.debug("configure WorkflowCtrl");

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Workflow');

    $scope.user = securityService.getUser();
    $scope.binTypeOptions = []; // = ['MUTUALLY_EXCLUSIVE', 'AD_HOC'];
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';
    $scope.currentProject = 1239500;
    $scope.projects;


    
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
    
    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);
    };
    
    //
    // MODALS
    //

    // Create checklist modal
    $scope.openCreateChecklistModal = function(bin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addChecklist.html',
        backdrop : 'static',
        controller : CreateChecklistModalCtrl,
        resolve : {
          projectId : function() {
            return $scope.currentProject.id;
          },
          binId : function() {
            return bin.id;
          },
          user : function() {
            return $scope.user;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(project) {
       
      });
    };

    // Create worklist modal
    $scope.openCreateWorklistModal = function(bin) {

      var modalInstance = $uibModal.open({
        templateUrl : 'app/page/workflow/addWorklist.html',
        backdrop : 'static',
        controller : CreateWorklistModalCtrl,
        resolve : {
          projectId : function() {
            return $scope.currentProject.id;
          },
          binId : function() {
            return bin.id;
          },
          user : function() {
            return $scope.user;
          }
        }
      });

      modalInstance.result.then(
      // Success
      function(project) {
       
      });
    };

    
  } ]);