// Login controller
tsApp.controller('LoginCtrl', [
  '$rootScope',
  '$scope',
  '$http',
  '$location',
  'securityService',
  'utilService',
  'configureService',
  'tabService',
  'appConfig',
  '$uibModal',
  function($rootScope, $scope, $http, $location, securityService, utilService, configureService,
    tabService, appConfig, $uibModal) {
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
        function(data) {
          utilService.clearError();
          securityService.setUser(data);
          // set request header authorization and reroute
          $http.defaults.headers.common.Authorization = data.authToken;
          
          // show registration popup
          var requiresReg = securityService.requiresRegistration(data);
          if (requiresReg !== "") {
          	$uibModal.open({
              templateUrl: 'app/util/register/registerModal.html',
              backdrop : (requiresReg === "WARN") ? 'none' : 'static',
              controller : 'RegisterModalCtrl',
              bindToController : true,
            });
          }
          
          // if license required, go to license page
          if (appConfig['deploy.license.enabled'] === 'true') {
            $location.path('/license');
          }
          // otherwise
          else {
            // Route the user to starting tab
            tabService.routeAuthorizedUser(data.userPreferences);
          }
        },
        // error
        function(data) {
          utilService.handleError({
            data : data
          });
        }
      );
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