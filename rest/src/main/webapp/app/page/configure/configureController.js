// Administration controller
tsApp.controller('ConfigureCtrl', [
  '$scope',
  '$http',
  'configureService',

  function($scope, $http, configureService) {
    console.debug('configure ConfigureCtrl');
    
    $scope.dbName = null;
    $scope.dbUser = null;
    $scope.dbPassword = null;
    $scope.appDir = null;

    $scope.configure = function() {
      console.log('Configuring: ' + $scope.dbName, $scope.dbUser, $scope.dbPassword,
        $scope.appDir);
      configureService
        .configure($scope.dbName, $scope.dbUser, $scope.dbPassword, $scope.appDir);
    }
  } ]);
