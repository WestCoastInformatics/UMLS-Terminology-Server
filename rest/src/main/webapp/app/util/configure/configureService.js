// Content Service
tsApp
  .service(
    'configureService',
    [
      '$rootScope',
      '$http',
      '$q',
      '$location',
      'gpService',
      'utilService',

      function($rootScope, $http, $q, $location, gpService, utilService) {
        console.debug("configure configureService");

        var configured = null;

        this.isConfigured = function() {
          console.debug('isConfigured call')
          var deferred = $q.defer();

          if (configured) {
            console.debug('  configuration previously detected');
            deferred.resolve(configured);
          } else {
            console.debug('  checking configuration');

            $http.get(configureUrl + 'configured').then(function(isConfigured) {
              console.debug('    configured: ' + isConfigured.data);
              configured = isConfigured.data;
              deferred.resolve(isConfigured.data);
            }, // error
            function(response) {
              utilService.handleError(response);
              deferred.resolve(false);
            });
          }
          return deferred.promise;
        }

        this.configure = function(dbName, userName, userPassword, appDir) {
          var deferred = $q.defer();

          var config = {
            'db.name' : dbName,
            'db.user' : userName,
            'db.password' : userPassword,
            'app.dir' : appDir

          };
          gpService.increment();
          $http.post(configureUrl + 'configure', config).then(function() {
            gpService.decrement();
            deferred.resolve();
            
            $rootScope.isConfigured = true;
            $location.path('/login');
          }, function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.resolve();
          });
          return deferred.promise;
        }

        // end

      } ]);
