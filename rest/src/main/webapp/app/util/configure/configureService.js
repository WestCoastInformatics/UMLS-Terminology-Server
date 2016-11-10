// Configure Service
var configureUrl = 'configure';
tsApp.service('configureService', [ '$rootScope', '$http', '$q', '$location', 'gpService',
  'utilService', function($rootScope, $http, $q, $location, gpService, utilService) {

    // Configured status flag
    var configured = null;

    // Check whether system is configured
    this.isConfigured = function() {
      var deferred = $q.defer();

      if (configured != null) {
        deferred.resolve(configured);
      } else {

        $http.get(configureUrl + '/configured').then(function(isConfigured) {
          configured = isConfigured.data;
          deferred.resolve(isConfigured.data);
        }, // error
        function(response) {
          utilService.handleError(response);
          deferred.resolve(false);
        });
      }
      return deferred.promise;
    };

    // Configure system
    this.configure = function(dbName, userName, userPassword, appDir) {
      var deferred = $q.defer();

      var config = {
        'db.name' : dbName,
        'db.user' : userName,
        'db.password' : userPassword,
        'app.dir' : appDir

      };
      gpService.increment();
      $http.post(configureUrl + '/configure', config).then(
      // Success
      function() {
        configured = true;
        gpService.decrement();
        deferred.resolve();

      },
      // Error
      function(response) {
        gpService.decrement();
        utilService.handleError(response);
        deferred.reject();
      });
      return deferred.promise;
    };

    // Destroy configuration
    this.destroy = function() {
      var deferred = $q.defer();
      gpService.increment();
      $http['delete'](configureUrl + '/destroy').then(
      // Success
      function(response) {
        gpService.decrement();
        deferred.resolve();
      },
      // Error
      function(error) {
        gpService.decrement();
        utilService.handleError();

        deferred.reject('Error destroying database');
      });
      return deferred.promise;
    };

    // Get config properties
    this.getConfigProperties = function() {
      console.debug("get config properties");
      var deferred = $q.defer();

      gpService.increment();
      $http.get(configureUrl + '/properties').then(
      // success
      function(response) {
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        gpService.decrement();
        utilService.handleError(response);
        deferred.reject();
      });
      return deferred.promise;
    };
    // end

  } ]);
