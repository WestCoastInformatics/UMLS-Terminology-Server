// Process Service
var processUrl = 'process';
tsApp.service('processService', [
  '$http',
  '$q',
  'Upload',
  'gpService',
  'utilService',
  function($http, $q, Upload, gpService, utilService) {
    console.debug('configure processService');

    // add algorithm config
    this.addAlgorithmConfig = function(projectId, algo) {
      console.debug('addAlgorithmConfig', projectId, algo);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.put(processUrl + '/config/algo/add?projectId=' + projectId, algo).then(
      // success
      function(response) {
        console.debug('  algo = ', response.data);
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

    // add process config
    this.addProcessConfig = function(projectId, process) {
      console.debug('addProcessConfig', projectId, process);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.put(processUrl + '/config/add?projectId=' + projectId, process).then(
      // success
      function(response) {
        console.debug('  process = ', response.data);
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

    // find process config
    this.findProcessConfigs = function(projectId, query, pfs) {
      console.debug('findProcessConfig', projectId, query, pfs);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.post(
        processUrl + '/config?projectId=' + projectId
          + (query ? '&query=' + utilService.prepQuery(query) : ''), utilService.prepPfs(pfs))
        .then(
        // success
        function(response) {
          console.debug('  processes = ', response.data);
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

    // get algorithm config
    this.getAlgorithmConfig = function(projectId, id) {
      console.debug('getAlgorithmConfig', projectId, id);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(processUrl + '/config/algo/' + id + '?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  algo = ', response.data);
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

    // get process config
    this.getProcessConfig = function(projectId, id) {
      console.debug('getProcessConfig', projectId, id);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(processUrl + '/config/' + id + '?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  process = ', response.data);
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

    // remove algorithm config
    this.removeAlgorithmConfig = function(projectId, id) {
      console.debug('removeAlgorithmConfig', projectId, id);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http['delete'](processUrl + '/config/algo/' + id + '/remove?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  successful delete');
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

    // remove process config
    this.removeProcessConfig = function(projectId, id) {
      console.debug('removeProcessConfig', projectId, id);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http['delete'](processUrl + '/config/' + id + '/remove?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  successful delete');
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

    // update algorithm config
    this.updateAlgorithmConfig = function(projectId, algo) {
      console.debug('updateAlgorithmConfig', projectId, algo);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.post(processUrl + '/config/algo/update?projectId=' + projectId, algo).then(
      // success
      function(response) {
        console.debug('  algo = ', response.data);
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

    // update process config
    this.updateProcessConfig = function(projectId, process) {
      console.debug('updateProcessConfig', projectId, process);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.post(processUrl + '/config/update?projectId=' + projectId, process).then(
      // success
      function(response) {
        console.debug('  process = ', response.data);
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

    // find process exec
    this.findProcessExecs = function(projectId, query, pfs) {
      console.debug('findProcessExecs', projectId, query, pfs);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.post(
        processUrl + '/exec?projectId=' + projectId
          + (query ? '&query=' + utilService.prepQuery(query) : ''), utilService.prepPfs(pfs))
        .then(
        // success
        function(response) {
          console.debug('  processes = ', response.data);
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

    // get process exec
    this.getProcessExec = function(projectId, id) {
      console.debug('getProcessExec', projectId, id);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(processUrl + '/exec/' + id + '?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  process = ', response.data);
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
