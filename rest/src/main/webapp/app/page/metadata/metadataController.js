// Metadata controller
tsApp.controller('MetadataCtrl', [
  '$scope',
  '$location',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'metadataService',
  function($scope, $location, gpService, utilService, tabService,
    securityService, metadataService) {
    console.debug("configure MetadataCtrl");

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Metadata') {
      tabService.setSelectedTabByLabel('Metadata');
    }

    // the currently viewed terminology (set by default or user)
    $scope.metadata = metadataService.getModel();
    $scope.user = securityService.getUser();

    // If terminology is blank, then reload
    if (!$scope.metadata.terminology) {
      utilService.handleError({
        data : "User is no longer logged in."
      });
      $location.path("/");
    }

  } ]);