// Tab controller
tsApp.controller('TabCtrl', [ '$scope', '$routeParams', 'securityService', 'tabService',
  'projectService', 'configureService', '$location',
  function($scope, $routeParams, securityService, tabService, projectService, configureService, $location) {
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

    // for ng-show on the tabs themselves
    $scope.isShowing = function() {
      var route = $location.path();
      
      //console.debug('isShowing', securityService.isLoggedIn(), route.indexOf('configure') == -1, securityService.isLoggedIn() && route && route.indexOf('configure') == -1)
      
      return securityService.isLoggedIn() && route && route.indexOf('configure') == -1;
    };

    // for ng-show on an individual tab
    $scope.isTabShowing = function(tab) {

      // Don't show tabs on the "simple" view
      // TODO: probably pack this into a parameter of tabs
      if ($routeParams.mode == 'simple') {
        return false;
      }

      // show tabs without a role
      if (!tab.role) {
        return true;
      }
      // show ANY tabs if the user has any role (e.g. "Edit")
      if (tab.role == 'ANY') {
        return !!$scope.userProjectsInfo.anyRole;
      }
      // Show USER tabs if the user has that application role (e.g. "Admin")
      if (tab.role == 'USER') {
        return securityService.isUser();
      }
      // Show ADMINISTRATOR tabs if the user has that application role (e.g.
      // "Upload")
      if (tab.role == 'ADMINISTRATOR') {
        return securityService.isAdmin();
      }
      return false;
    };
  } ]);