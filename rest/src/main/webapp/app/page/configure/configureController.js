// Configuration controller
tsApp.controller('ConfigureCtrl', [ '$scope', '$http', '$location', 'configureService',

function($scope, $http, $location, configureService) {
  console.debug('configure ConfigureCtrl');
  
  // flag for whether to show configuration contents
  $scope.requiresConfiguration = null;

  // user-configurable fields
  $scope.dbName = null;
  $scope.dbUser = null;
  $scope.dbPassword = null;
  $scope.appDir = null;

  // configures the application
  $scope.configure = function() {
    console.log('Configuring database');
    configureService.configure($scope.dbName, $scope.dbUser, $scope.dbPassword, $scope.appDir).then(function() {
      $scope.requiresConfiguration = false;
    });
  }
  
  $scope.enterApp = function() {
    $location.path('/login');
  }

  //
  // Initialization: Check that application is configured
  //
  configureService.isConfigured().then(function(isConfigured) {
    if (isConfigured) {
      $location.path('/login');
    } else {
      $scope.requiresConfiguration = true;
    }
  });
} ]);
