// Content controller
console.debug('configure ContentCtrl');
tsApp.controller('ContentCtrl', [
  '$scope',
  '$http',
  '$modal',
  'gpService',
  'errorService',
  'tabService',
  'securityService',
  function($scope, $http, $modal, gpService, errorService, tabService,
    securityService) {

    // Handle resetting tabs on "back" button
    if (tabService.selectedTab.label != 'Content') {
      tabService.setSelectedTabByLabel('Content');
    }

  } ]);
