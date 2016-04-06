// Tab service
tsApp.service('tabService', [ '$location', 'utilService', 'gpService', 'securityService',
  'metadataService', function($location, utilService, gpService, securityService, metadataService) {
    console.debug('configure tabService');
    // Available tabs
    this.tabs = [ {
      link : 'source',
      label : 'Sources',
      role : 'USER'
    }, {
      link : 'content',
      label : 'Content',
      role : false
    }, {
      link : 'metadata',
      label : 'Metadata',
      role : false
    }, {
      link : 'admin',
      label : 'Admin',
      role : 'USER'

    } ];

    // the selected tab
    this.selectedTab = this.tabs[0];

    // Sets the selected tab
    this.setSelectedTab = function(tab) {
      this.selectedTab = tab;
      $location.path(tab.link);
    };

    // sets the selected tab by label
    // to be called by controllers when their
    // respective tab is selected
    this.setSelectedTabByLabel = function(label) {
      console.debug("set selected tab", label);
      for (var i = 0; i < this.tabs.length; i++) {
        console.debug("  " + this.tabs[i].label, label);
        if (this.tabs[i].label === label) {
          this.selectedTab = this.tabs[i];
          $location.path(this.selectedTab.link);
          break;
        }
      }
    };

  } ]);