// Metadata controller
console.debug("configure MetadataCtrl");
tsApp.controller('MetadataCtrl', [
  '$scope',
  'tabService',
  'securityService',
  'metadataService',
  function($scope, tabService, securityService, metadataService) {

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Metadata') {
      tabService.setSelectedTabByLabel('Metadata');
    }

    // $scope.$watch('component', function() {
    // // n/a
    // });

    // the currently viewed terminology (set by default or user)
    $scope.metadata = metadataService.getMetadata();
    $scope.user = securityService.getUser();

    // TODO: remove this when done - this is just for metadata testing
    metadataService.getTerminology('SNOMEDCT_US', '2014_09_01').then(
      function(data) {
        console.debug("resolve");
        metadataService.setTerminology(data).then(function(data) {
          console.debug("$scope.metadata = " , $scope.metadata);
        });
      });
    
  } ]);