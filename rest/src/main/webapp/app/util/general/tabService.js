// Tab service
tsApp.service('tabService', [ '$route', '$location', 'utilService', 'gpService', 'securityService',
  'appConfig', function($route, $location, utilService, gpService, securityService, appConfig) {
    console.debug('configure tabService');

    this.showTabs = true;

    // Available tabs
    // TODO Make private, with accessor
    this.tabs = [];

    if (appConfig.enabledTabs) {
      securityService.getUser();
      var tabArray = appConfig.enabledTabs.split(',');
      for (var i = 0; i < tabArray.length; i++) {
        switch (tabArray[i]) {
        case 'source':
          if (securityService.hasPrivilegesOf('USER')) {
            this.tabs.push({
              link : 'source',
              label : 'Sources',
              role : 'USER'
            });
          }
          break;
        case 'content':
          this.tabs.push({
            link : 'content',
            label : 'Content',
            role : false
          });
          break;
        case 'metadata':
          this.tabs.push({
            link : 'metadata',
            label : 'Metadata',
            role : false
          });
          break;
        case 'admin':
          if (securityService.hasPrivilegesOf('USER')) {
            this.tabs.push({
              link : 'admin',
              label : 'Admin',
              role : 'USER'

            });
          }
          break;
        case 'default':
          console.error('Invalid tab ' + tabArray[i] + ' specified, skipping');
        }
      }
    }
    this.setShowing = function(showTabs) {
      console.debug('setShowing', showTabs);
      this.showTabs = showTabs;
    };

    this.isShowing = function() {
      return this.showTabs;
    };

    // the selected tab
    this.selectedTab = null;

    // Sets the selected tab
    this.setSelectedTab = function(tab) {
      this.selectedTab = tab;
      $location.path(tab.link);
    };

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is selected
    this.setSelectedTabByLabel = function(label) {
      console.debug("set selected tab", label, this.tabs);
      for (var i = 0; i < this.tabs.length; i++) {
        console.debug("  " + this.tabs[i].label, label);
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          $location.path(this.selectedTab.link);
          break;
        }
      }
    };

    this.setSelectedTabByIndex = function(index) {
      console.debug("set selected tab", index);
      this.selectedTab = this.tabs[index];
      $location.path(this.selectedTab.link);
    }

  } ]);