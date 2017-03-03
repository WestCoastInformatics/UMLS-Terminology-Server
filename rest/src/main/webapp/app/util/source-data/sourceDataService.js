// Source data service
var sourceDataUrl = 'file';
tsApp.service('sourceDataService',
  [
    '$http',
    '$location',
    '$q',
    '$cookies',
    'utilService',
    'gpService',
    function($http, $location, $q, ngCookies, utilService, gpService) {

      // cached loader names
      var sourceDataHandlers = null;

      // Get details for all currently uploaded files
      this.findSourceDataFiles = function(query) {
        console.debug('find source data files', query);
        var deferred = $q.defer();
        gpService.increment();
        $http.get(sourceDataUrl + '/find?query=' + encodeURI(query), {}).then(
        // Success
        function(response) {
          console.debug('  data = ', response.data);
          gpService.decrement();
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          gpService.decrement();
          utilService.handleError(response);
          deferred.reject(response);
        });
        return deferred.promise;
      };

      // Removes soure data file
      this.removeSourceDataFile = function(id) {
        console.debug('remove source data file', id);
        var deferred = $q.defer();
        gpService.increment();
        $http['delete'](sourceDataUrl + '/remove/' + id).then(
        // Success
        function(response) {
          console.debug('  data = ', response.data);
          gpService.decrement();
          deferred.resolve();
        },
        // Error
        function(response) {
          gpService.decrement();
          utilService.handleError(response);
          deferred.reject(response);
        });
        return deferred.promise;
      };

      // Save or add the source data file
      this.updateSourceDataFile = function(file) {
        console.debug('update source data file', file);
        var deferred = $q.defer();
        if (file.id) {
          gpService.increment();
          $http.post(sourceDataUrl + '/update', file).then(
          // Success
          function(response) {
            gpService.decrement();
            deferred.resolve(sourceDataFile);
          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response);
          });
        } else {
          gpService.increment();
          $http.put(sourceDataUrl + '/add', file).then(
          // Success
          function(response) {
            gpService.decrement();
            deferred.resolve(response);
          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response);
          });
        }
        return deferred.promise;
      };

      // update or add the source data
      this.updateSourceData = function(data) {
        console.debug('update source data', data);
        var deferred = $q.defer();
        if (data.id) {
          gpService.increment();
          $http.post(sourceDataUrl + '/data/update', data).then(
          // Success
          function(response) {
            gpService.decrement();
            deferred.resolve(data.data);
          },
          // Error
          function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.reject(response);
          });
        } else {
          gpService.increment();
          $http.put(sourceDataUrl + '/data/add', data).then(
          // Success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // Error
          function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.reject(response);
          });
        }
        return deferred.promise;
      };

      // Remove the source data
      this.removeSourceData = function(id) {
        console.debug('remove source data', id);
        var deferred = $q.defer();
        gpService.increment();
        $http['delete'](sourceDataUrl + '/data/remove/' + id).then(
        // Success
        function(response) {
          gpService.decrement();
          deferred.resolve();
        },
        // Error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
          deferred.reject(response);
        });
        return deferred.promise;
      };

      // find source data
      this.findSourceData = function(query, disableGlassPane) {
        console.debug('find source data', query);
        var deferred = $q.defer();
        if (!disableGlassPane) {
          gpService.increment();
        }
        $http.get(sourceDataUrl + '/data/find?query=' + encodeURI(query), {}).then(
        // Success
        function(response) {
          console.debug("  data =", response.data);
          if (!disableGlassPane) {
            gpService.decrement();
          }
          deferred.resolve(response.data);
        }, // Error
        function(response) {
          utilService.handleError(response);
          if (!disableGlassPane) {
            gpService.decrement();
          }
          deferred.reject(response);
        });
        return deferred.promise;
      };

      // get loaders
      this.getSourceDataHandlers = function() {
        console.debug('get source data handlers');
        var deferred = $q.defer();

        if (sourceDataHandlers) {
          deferred.resolve(sourceDataHandlers);
        } else {
          gpService.increment();
          $http.get(sourceDataUrl + '/data/sourceDataHandlers').then(
          // Success
          function(response) {
            console.debug("  data =", response.data);
            sourceDataHandlers = response.data.keyValuePairs;
            gpService.decrement();
            deferred.resolve(response.data.keyValuePairs);
          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response);
          });
        }
        return deferred.promise;
      };

      this.getSourceData = function(id, suppressGlassPane) {
        console.debug('loading source data from id ' + id);
        var deferred = $q.defer();
        if (!id) {
          utilService.setError('Attempted to load from null or undefined id');
          deferred.reject('No id specified');
        } else {
          if (!suppressGlassPane) {
            gpService.increment();
          }
          $http.get(sourceDataUrl + '/data/id/' + id).then(function(response) {
            if (!suppressGlassPane) {
              gpService.decrement();
            }
            deferred.resolve(response.data);
          },
          // Error
          function(response) {
            utilService.handleError(response);
            if (!suppressGlassPane) {
              gpService.decrement();
            }
            deferred.resolve(null);
          });
        }
        return deferred.promise;
      };

      this.loadFromSourceData = function(sourceData) {

        console.debug('loading source data for ' + sourceData.name);
        var deferred = $q.defer();
        if (!sourceData) {
          utilService.setError('Attempted to load from null or undefined source data');
          deferred.reject('No source data specified');
        } else {
          gpService.increment();

          $http.post(sourceDataUrl + '/data/load?background=true', sourceData).then(
            function(response) {
              gpService.decrement();
              deferred.resolve();
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
        }
        return deferred.promise;
      };

      this.removeFromSourceData = function(sourceData) {

        console.debug('removing from source data for ' + sourceData.name);
        var deferred = $q.defer();
        if (!sourceData) {
          utilService.setError('Attempted to remove from null or undefined source data');
          deferred.reject('No source data specified');
        } else {
          gpService.increment();

          $http.post(sourceDataUrl + '/data/remove?background=true', sourceData).then(
            function(response) {
              gpService.decrement();
              deferred.resolve();
            },
            // Error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response);
            });
        }
        return deferred.promise;
      };

      this.cancelSourceDataProcess = function(sourceData) {
        console.debug('cancelSourceDataProcess', sourceData);
        var deferred = $q.defer();
        $http.post(sourceDataUrl + '/data/cancel', sourceData).then(function(response) {
          deferred.resolve("Successfully cancelled source data process");
        }, function(error) {
          utilService.handleError(error);
          deferred.reject('Error cancelling source data process');
        });
        return deferred.promise;
      };

      this.getSourceDataLog = function(terminology, version, activity, lines) {
        console.debug('getSourceDataLog', terminology, version, activity, lines);
        var deferred = $q.defer();

        if (!terminology && !version) {
          utilService
            .setError('Must specify all of terminology and version to retrieve log entries');
          deferred.reject(null);
        }

        else {

          $http.get(
            sourceDataUrl + '/log?terminology=' + terminology + '&version=' + version
              + (activity ? '&activity=' + activity : '') + (lines ? '&lines=' + lines : '')).then(
            function(response) {
              deferred.resolve(response.data);
            }, function(error) {
              utilService.handleError(error);

              deferred.reject('Error retrieving source data log entries');
            });
        }
        return deferred.promise;
      };

      this.getSourceDatas = function() {
        var deferred = $q.defer();
        $http.get(sourceDataUrl + '/data/all').then(function(response) {
          deferred.resolve(response.data);
        }, function(error) {
          utilService.handleError(error);

          deferred.reject('Error retrieving source datas');
        });
        return deferred.promise;
      };

      // end.

    } ]);