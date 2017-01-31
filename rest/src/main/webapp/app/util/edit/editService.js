// Meta Editing Service
var editUrl = 'edit';
tsApp.service('editService', [
  '$http',
  '$q',
  '$uibModal',
  'gpService',
  'utilService',
  function($http, $q, $uibModal, gpService, utilService) {
    
    var editEnabled = false;
    
    this.enabeEditing = function() {
      editEnabled = true;
    }
    
    this.disableEditing = function() {
      editEnabled = false;
    }
    
    this.isEditingEnabled = function() {
      return editEnabled;
    }

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
    
 
 // add semanticType
    this.addSemanticType = function(projectId, conceptId, semanticType) {
      console.debug('addSemanticType', projectId, conceptId, semanticType);
      var deferred = $q.defer();

      gpService.increment();
      $http.put(editUrl + '/sty?projectId=' + projectId + '&conceptId=' + conceptId, semanticType).then(
      // success
      function(response) {
        console.debug('  semanticType = ' + response.data);
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

    // update semanticType
    this.updateSemanticType = function(projectId, conceptId, semanticType) {
      console.debug('updateSemanticType', projectId, conceptId, semanticType);
      var deferred = $q.defer();

      gpService.increment();
      $http.post(editUrl + '/sty?projectId=' + projectId + '&conceptId=' + conceptId, semanticType).then(
      // success
      function(response) {
        console.debug('  successful update semanticType');
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

    // remove semanticType
    this.removeSemanticType = function(projectId, conceptId, semanticTypeId) {
      console.debug('removeSemanticType', projectId, conceptId, semanticTypeId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](
        editUrl + '/sty/' + semanticTypeId + '?projectId=' + projectId + '&conceptId=' + conceptId).then(
      // success
      function(response) {
        console.debug('  successful remove semanticType');
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
    
    this.getCallbacks = function() {
      return {
        enableEditing : enableEditing,
        disableEditing : disableEditing,
        isEditingEnabled : isEditingEnabled,
        addAtom : addAtom,
        updateAtom : updateAtom,
        removeAtom : removeAtom
      }
    }

    // end
  } ]);
