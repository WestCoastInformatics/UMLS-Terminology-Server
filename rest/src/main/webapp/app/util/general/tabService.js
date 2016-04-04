// Tab service
tsApp.service('tabService', [ '$location', 'utilService', 'gpService', 'securityService', 'metadataService',
  function($location, utilService, gpService, securityService, metadataService) {
    console.debug('configure tabService');
    // Available tabs
    this.tabs = [ {
      link : 'source',
      label : 'Sources',
      role : 'USER',
      enabled : true

    }, {
      link : 'content',
      label : 'Content',
      role : false,
      enabled : true
    }, {
      link : 'metadata',
      label : 'Metadata',
      role : false,
      enabled : true
    }, {
      link : 'admin',
      label : 'Admin',
      role : 'USER',
      enabled : true

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
    
    this.setTabEnabledByLabel = function(label, enabledStatus) {
      console.debug("set tab enabled", label, enabledStatus);
      for (var i = 0; i < this.tabs.length; i++) {
        //console.debug("  " + this.tabs[i].label, label);
        if (this.tabs[i].label === label) {
          this.tabs[i].enabled = enabledStatus;
          break;
        }
      }
    }
    
  /*  
    TODO This fails due to authorization token not set, really shouldn't be here anyway...
     
    
   // tab initialization
    metadataService.initTerminologies().then(function(response) {
      console.debug('Tab Service terminologies retrieved: ', response.count);
      if (response.count == 0) {
        console.log('No Terminologies loaded, disabling Content tab');
       this.setTabEnabledByLabel('content', false)
      }
    });*/
    
    

  } ]);