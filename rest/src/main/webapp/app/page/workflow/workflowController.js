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
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    workflowService, configureService) {
    console.debug("configure WorkflowCtrl");

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Workflow');

    $scope.user = securityService.getUser();
    $scope.binTypeOptions = ['MUTUALLY_EXCLUSIVE', 'AD_HOC'];
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';

    // Configure tab and accordion
    $scope.configureTab = function() {
      $scope.user.userPreferences.lastTab = '/workflow';
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    //
    // Initialize
    //

    $scope.initialize = function() {

      // If terminology is blank, then redirect to /content to set a terminology
      /*if (!$scope.metadata.terminologies) {
        $location.path("/content");
      }*/

      // Handle users with user preferences
      if ($scope.user.userPreferences) {
        $scope.configureTab();
      }
      
      $scope.getBins(1239500, 'MUTUALLY_EXCLUSIVE');
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

    
    $scope.setRole = function() {
      $scope.user.userPreferences.binType = $scope.currentBinType;
      securityService.updateUserPreferences($scope.user.userPreferences);
      //projectService.fireProjectChanged($scope.project);
    };
    
    $scope.getBins = function(projectId, type) {
      console.debug('getBins', projectId, type);

      workflowService.getWorkflowBins(projectId, type).then(function(response) {
        $scope.bins = response;
      });
    };
    
  } ]);