// Metadata controller
tsApp.controller('MetadataCtrl',
  [
    '$scope',
    '$http',
    '$location',
    'gpService',
    'utilService',
    'tabService',
    'securityService',
    'metadataService',
    function($scope, $http, $location, gpService, utilService, tabService, securityService,
      metadataService) {
      console.debug("configure MetadataCtrl");

      // Handle resetting tabs on "back" button
      if (tabService.selectedTab.label != 'Metadata') {
        tabService.setSelectedTabByLabel('Metadata');
      }

      // the currently viewed terminology (set by default or user)
      $scope.user = securityService.getUser();
      $scope.metadata = metadataService.getModel();

      // If terminology is blank, then redirect to /content to set a terminology
      if (!$scope.metadata.terminologies) {
        $location.path("/content");
      }

      // Configure tab and accordion
      $scope.configureTab = function() {
        // skip guest user
        if ($http.defaults.headers.common.Authorization == 'guest') {
          return;
        }
        $scope.user.userPreferences.lastTab = '/metadata';
        
        securityService.updateUserPreferences($scope.user.userPreferences);
      };

      // Handle users with user preferences
      if ($scope.user.userPreferences) {
        $scope.configureTab();
      }
    } ]);