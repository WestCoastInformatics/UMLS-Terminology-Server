// Inversion Service
var inversionUrl = 'inversion';
tsApp.service('inversionService', [
  '$http',
  '$q',
  'Upload',
  'gpService',
  'utilService',
  function($http, $q, Upload, gpService, utilService) {

    this.getSourceIdRange = function(projectId, terminology) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(inversionUrl + '/range/' + projectId + '/' + terminology ).then(
      // success
      function(response) {
        console.debug('  source id range = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    
    this.requestSourceIdRange = function(projectId, terminology, numberofids, beginSourceId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(inversionUrl + '/range/' + projectId + '/' + terminology  + '/' + numberofids + 
        (beginSourceId ? ('?beginSourceId=' + beginSourceId) : '')).then(
      // success
      function(response) {
        console.debug('  requested source id range = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    
    this.updateSourceIdRange = function(projectId, terminology, numberofids) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(inversionUrl + '/range/update/' + projectId + '/' + terminology  + '/' + numberofids).then(
      // success
      function(response) {
        console.debug('  submitted adjustment on source id range = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    
    this.removeSourceIdRange = function(id) {
      console.debug('removeSourceIdRange', id);
      var deferred = $q.defer();

        gpService.increment();
        $http['delete'](
          inversionUrl + '/range/' + id )
          .then(function(response) {
            console.debug('  successful remove source id range');
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            // return the original concept without additional annotation
            deferred.reject();
          });

        return deferred.promise;
      
    };

    // end
  } ]);
