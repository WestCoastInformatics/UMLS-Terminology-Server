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
      console.debug("IS TAB SHOWING", tab, $scope.userProjectsInfo);
      // show tabs without a role requirement
      if (!tab.role && !tab.projectRole) {
        return true;
      }

      // Show tabs with an application role requirement if met
      if (tab.role && securityService.hasPrivilegesOf(tab.role)) {
        return true;
      }

      // Show tabls with an "anyrole" requirement
      if (tab.projectRole && $scope.userProjectsInfo.anyrole) {
        return true;
      }
      console.debug("  false");
    };

    // end
  } ]);