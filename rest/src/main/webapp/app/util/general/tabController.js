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

    // User project info (for checking "anyrole")
    $scope.userProjectsInfo = projectService.getUserProjectsInfo();

    // Set selected tab (change the view)
    $scope.setSelectedTab = function(tab) {
      tabService.setSelectedTab(tab);
    };

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is selected
    this.setSelectedTabByLabel = function(label) {
      console.debug('setting tab by label', label, this.tabs);
      for (var i = 0; i < this.tabs.length; i++) {
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          break;
        }
      }
    };

    // Set 'active' or not
    $scope.tabClass = function(tab) {
      if (tabService.selectedTab == tab) {
        return 'active';
      } else {
        return '';
      }
    };

    // Indicate whether any tabs should be showing
    $scope.isShowing = function() {
      return tabService.isShowing();
    };

    // for ng-show on an individual tab
    $scope.isTabShowing = function(tab) {

      // show tabs without a role
      if (!tab.role) {
        return true;
      }
      else return securityService.hasPrivilegesOf(tab.role);
    };
  } ]);