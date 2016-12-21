// Meta Editing Service
var editUrl = 'edit';
tsApp.service('editService', [
  '$http',
  '$q',
  '$uibModal',
  'gpService',
  'utilService',
  function($http, $q, $uibModal, gpService, utilService) {

    // add atom
    this.addAtom = function(projectId, conceptId, atom) {
      console.debug('addAtom', projectId, conceptId, atom);
      var deferred = $q.defer();

      gpService.increment();
      $http.put(editUrl + '/atom?projectId=' + projectId + '&conceptId=' + conceptId, atom).then(
      // success
      function(response) {
        console.debug('  atom = ' + response.data);
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
    }

    // update atom
    this.updateAtom = function(projectId, conceptId, atom) {
      console.debug('updateAtom', projectId, conceptId, atom);
      var deferred = $q.defer();

      gpService.increment();
      $http.post(editUrl + '/atom?projectId=' + projectId + '&conceptId=' + conceptId, atom).then(
      // success
      function(response) {
        console.debug('  successful update atom');
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
    }

    // remove atom
    this.removeAtom = function(projectId, conceptId, atomId) {
      console.debug('removeAtom', projectId, conceptId, atomId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](
        editUrl + '/atom/' + atomId + '?projectId=' + projectId + '&conceptId=' + conceptId).then(
      // success
      function(response) {
        console.debug('  successful remove atom');
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
    }

    // end
  } ]);
