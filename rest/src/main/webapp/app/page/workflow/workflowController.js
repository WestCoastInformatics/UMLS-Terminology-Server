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
    // TODO: this list should be dynamic
    $scope.binTypeOptions = ['MUTUALLY_EXCLUSIVE', 'AD_HOC'];
    $scope.currentBinType = 'MUTUALLY_EXCLUSIVE';
    $scope.projectId = 1239500;

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
      
      $scope.getBins($scope.projectId, $scope.currentBinType);
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
      $scope.getBins($scope.projectId, binType);
    }
    
    // Retrieve all bins with project and type
    $scope.getBins = function(projectId, type) {
      console.debug('getBins', projectId, type);

      workflowService.getWorkflowBins(projectId, type).then(function(response) {
        $scope.bins = response;
      });
    };
    
    // Convert date to a string
    $scope.toDate = function(lastModified) {
      return utilService.toDate(lastModified);

    };
  } ]);