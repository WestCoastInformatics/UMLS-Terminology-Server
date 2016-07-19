// Project Service
tsApp
  .service(
    'reportService',
    [
      '$http',
      '$q',
      '$rootScope',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, gpService, utilService) {
        console.debug('configure reportService');
        

        // get concept report
        this.getConceptReport = function(projectId, conceptId) {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(reportUrl + 'concept?projectId=' + projectId + "&conceptId=" + conceptId).then(
          // success
          function(response) {
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


        

        // end
      } ]);
