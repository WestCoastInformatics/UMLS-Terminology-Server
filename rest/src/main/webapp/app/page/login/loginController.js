// Login controller
tsApp.controller('LoginCtrl', [
  '$rootScope',
  '$scope',
  '$http',
  '$location',
  'securityService',
  'gpService',
  'utilService',
  'projectService',
  'configureService',
  'tabService',
  'appConfig',
  function($rootScope, $scope, $http, $location, securityService, gpService, utilService,
    projectService, configureService, tabService, appConfig) {
    console.debug('configure LoginCtrl');
    
    // disable tabs in login view
    tabService.setShowing(false);

    // pass config to scope
    $scope.appConfig = appConfig;

    // Login function
    $scope.login = function(name, password) {
      if (!name) {
        alert("You must specify a user name");
        return;
      } else if (!password) {
        alert("You must specify a password");
        return;
      }

      securityService.authenticate(name, password).then(
      // success
      function(response) {
        utilService.clearError();
        
        securityService.setUser(response);

        // set request header authorization and reroute
        $http.defaults.headers.common.Authorization = response.authToken;
        projectService.getUserHasAnyRole();

        // if license required, go to license page
        if (appConfig.licenseEnabled === 'true') {
          console.debug('gpc: ', gpService.glassPane.counter);
          $location.path('/license');
        }

        // otherwise, use previous tab in preferences (if it exists)
        else if (response.userPreferences && response.userPreferences.lastTab) {
          $location.path(response.userPreferences.lastTab);
        }

        // if no previous preferences (first visit), go to source for initial
        // file upload or content based on role
        else {
          if (tabService.tabs.length == 0) {
            handleError('No tabs configured')
          }
          $location.path(tabService.tabs[0].link);

        }
      
      },

      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Logout function
    $scope.logout = function() {
      securityService.logout();
    };

    //
    // Initialization: Check that application is configured
    //

    $scope.initialize = function() {
      // Clear user info
      securityService.clearUser();

      // Declare the user
      $scope.user = securityService.getUser();

    };

    // Check if configured, send to configure, or initialize
    configureService.isConfigured().then(function(isConfigured) {
      if (!isConfigured) {
        $location.path('/configure');
      } else {
        $scope.initialize();
      }
    });

  } ]);