// Metadata controller
tsApp.controller('MetadataCtrl', [
  '$scope',
  '$http',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'metadataService',
  'configureService',
  function($scope, $http, $location, gpService, utilService, tabService, securityService,
    metadataService, configureService) {
    console.debug("configure MetadataCtrl");

    tabService.setShowing(true);

    // Clear error
    utilService.clearError();

    // Handle resetting tabs on "back" button
    tabService.setSelectedTabByLabel('Metadata');

    // the currently viewed terminology (set by default or user)
    $scope.user = securityService.getUser();
    $scope.metadata = metadataService.getModel();
    $scope.resultsCollapsed = {};

    // Configure tab and accordion
    $scope.configureTab = function() {
      $scope.user.userPreferences.lastTab = '/metadata';
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    //
    // Initialize
    //

    $scope.initialize = function() {

      // If terminology is blank, then redirect to /content to set a terminology
      if (!$scope.metadata.terminologies) {
        $location.path("/content");
      }

      // Handle users with user preferences
      else if ($scope.user.userPreferences) {
        $scope.configureTab();
      }
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

  } ]);