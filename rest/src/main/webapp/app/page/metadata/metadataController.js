// Metadata controller
console.debug("configure MetadataCtrl");
tsApp.controller('MetadataCtrl', [ '$scope', 'gpService', 'tabService',
  'securityService', 'metadataService',
  function($scope, gpService, tabService, securityService, metadataService) {
    // Handle resetting tabs on "back" button
    gpService.increment();
    if (tabService.selectedTab.label != 'Metadata') {
      tabService.setSelectedTabByLabel('Metadata');
    }

    // $scope.$watch('component', function() {
    // // n/a
    // });

    // the currently viewed terminology (set by default or user)
    $scope.metadata = metadataService.getModel();
    $scope.user = securityService.getUser();
    gpService.decrement();

  } ]);