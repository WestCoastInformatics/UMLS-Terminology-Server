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
    $scope.selected = {
      metadata : metadataService.getModel(),
      precedenceList : []
    }
    
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
      if (!$scope.selected.metadata.terminology) {
        $location.path("/content");
      }

      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/metadata');

      metadataService.getPrecedenceList($scope.selected.metadata.terminology.terminology,
        $scope.selected.metadata.terminology.version).then(
      // Success
      function(data) {
        $scope.selected.precedenceList = data.precedence;
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

  } ]);