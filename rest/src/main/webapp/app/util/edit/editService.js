// Meta Editing Service
var editUrl = 'edit';
tsApp.service('editService', [
  '$http',
  '$q',
  '$uibModal',
  'gpService',
  'utilService',
  'appConfig',
  function($http, $q, $uibModal, gpService, utilService, appConfig) {

    var canEdit = appConfig['deploy.simpleedit.enabled'] == true
      || appConfig['deploy.simpleedit.enabled'] == 'true';
    var editEnabled = false;
    
    console.debug('editService init', canEdit, appConfig);

    this.enableEditing = function() {
      editEnabled = true;
    }

    this.disableEditing = function() {
      editEnabled = false;
    }

    this.isEditingEnabled = function() {
      return editEnabled && canEdit;
    }

    this.canEdit = function() {
      return canEdit;
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
      $http.put(editUrl + '/sty?projectId=' + projectId + '&conceptId=' + conceptId, semanticType)
        .then(
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

    // remove semanticType
    this.removeSemanticType = function(projectId, conceptId, semanticTypeId) {
      console.debug('removeSemanticType', projectId, conceptId, semanticTypeId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](
        editUrl + '/sty/' + semanticTypeId + '?projectId=' + projectId + '&conceptId=' + conceptId)
        .then(
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
    
 // add concept
    this.addConcept = function(projectId, conceptId, concept) {
      console.debug('addConcept', projectId, conceptId, concept);
      var deferred = $q.defer();

      gpService.increment();
      $http.put(editUrl + '/concept?projectId=' + projectId + '&conceptId=' + conceptId, concept).then(
      // success
      function(response) {
        console.debug('  concept = ' + response.data);
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

    // update concept
    this.updateConcept = function(projectId, conceptId, concept) {
      console.debug('updateConcept', projectId, conceptId, concept);
      var deferred = $q.defer();

      gpService.increment();
      $http.post(editUrl + '/concept?projectId=' + projectId + '&conceptId=' + conceptId, concept).then(
      // success
      function(response) {
        console.debug('  successful update concept');
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

    // remove concept
    this.removeConcept = function(projectId, conceptId, conceptId) {
      console.debug('removeConcept', projectId, conceptId, conceptId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](
        editUrl + '/concept/' + conceptId + '?projectId=' + projectId + '&conceptId=' + conceptId).then(
      // success
      function(response) {
        console.debug('  successful remove concept');
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
        enableEditing : this.enableEditing,
        disableEditing : this.disableEditing,
        isEditingEnabled : this.isEditingEnabled,
        canEdit : this.canEdit,
        addAtom : this.addAtom,
        updateAtom : this.updateAtom,
        removeAtom : this.removeAtom,
        addSemanticType : this.addSemanticType,
        removeSemanticType : this.removeSemanticType,
        addConcept : this.addConcept,
        updateConcept : this.updateConcept,
        removeConcept : this.removeConcept
      }
    }

    // end
  } ]);
