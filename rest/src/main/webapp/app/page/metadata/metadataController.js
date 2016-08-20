// Metadata controller
tsApp.controller('MetadataCtrl', [
  '$scope',
  '$http',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'configureService',
  'securityService',
  'projectService',
  'metadataService',
  function($scope, $http, $location, gpService, utilService, tabService, configureService,
    securityService, projectService, metadataService) {
    console.debug("configure MetadataCtrl");

    // Set up tabs and controller
    tabService.setShowing(true);
    utilService.clearError();
    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();

    // Scope vars
    $scope.metadata = metadataService.getModel();
    $scope.resultsCollapsed = {};

    // pretty print
    $scope.getItemName = function(item) {
      if (item) {
        return item.name.replace(/_/g, ' ');
      }
    }

    //
    // Initialize
    //

    $scope.initialize = function() {

      // If terminology is blank, then redirect to /content to set a terminology
      if (!$scope.metadata.terminologies) {
        $location.path("/content");
      }

      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/metadata');

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