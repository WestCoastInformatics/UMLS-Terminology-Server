// Validation Service
tsApp.service('validationService', [ '$http', '$q', 'gpService', 'utilService',
  function($http, $q, gpService, utilService) {
    console.debug('configure validationService');

    // validate concept
    this.validateConcept = function(concept, projectId) {
      console.debug('validateConcept');
      var deferred = $q.defer();

      // validate concept
      gpService.increment();
      $http.put(validationUrl + 'cui', concept).then(
      // success
      function(response) {
        console.debug('  result = ', response.data);
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

    // get all validation check names
    this.getValidationCheckNames = function() {
      console.debug('getValidationCheckNames');
      var deferred = $q.defer();

      gpService.increment();
      $http.get(validationUrl + 'checks').then(
      // success
      function(response) {
        console.debug('  validation checks = ', response.data);
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
  } ]);
