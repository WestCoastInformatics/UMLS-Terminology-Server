// Metadata controller
tsApp.controller('MetadataCtrl',
  [
    '$scope',
    '$location',
    'gpService',
    'utilService',
    'tabService',
    'securityService',
    'metadataService',
    function($scope, $location, gpService, utilService, tabService, securityService,
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
      if (!$scope.metadata.terminology) {
        $location.path("/content");
      }

    } ]);