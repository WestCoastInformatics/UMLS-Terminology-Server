// Tab controller
tsApp.controller('TabCtrl', [
  '$scope',
  '$routeParams',
  'securityService',
  'tabService',
  'projectService',
  'configureService',
  '$location',
  function($scope, $routeParams, securityService, tabService, projectService, configureService,
    $location) {
    console.debug('configure TabCtrl');

    // Setup tabs
    $scope.tabs = tabService.tabs;
    
    $scope.isShowing = tabService.isShowing;

    // User project info (for checking "anyrole")
    $scope.userProjectsInfo = projectService.getUserProjectsInfo();

    // Set selected tab (change the view)
    $scope.setSelectedTab = function(tab) {
      tabService.setSelectedTab(tab);
    };

    // Set 'active' or not
    $scope.tabClass = function(tab) {
      if (tabService.selectedTab == tab) {
        return 'active';
      } else {
        return '';
      }
    };

  } ]);