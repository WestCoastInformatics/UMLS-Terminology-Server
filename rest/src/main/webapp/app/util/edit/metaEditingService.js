// Meta Editing Service
var metaEditingUrl = 'meta';
tsApp
  .service('metaEditingService',
    [
      '$http',
      '$q',
      '$rootScope',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, gpService, utilService) {
        console.debug('configure metaEditingService');

        // add atom
        this.addAtom = function(projectId, activityId, concept, atom, overrideWarnings) {
          console.debug('add atom');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/atom/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), atom).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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
        
        // update atom
        this.updateAtom = function(projectId, activityId, concept, atom, overrideWarnings) {
          console.debug('update atom');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/atom/update?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), atom).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // add attribute
        this.addAttribute = function(projectId, activityId, concept, attribute, overrideWarnings) {
          console.debug('add atom');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/attribute/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), attribute).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // add relationship
        this.addRelationship = function(projectId, activityId, concept, relationship,
          overrideWarnings) {
          console.debug('add atom');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/relationship/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), relationship).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // add semantic type
        this.addSemanticType = function(projectId, activityId, concept, semanticType,
          overrideWarnings) {
          console.debug('add semantic type');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/sty/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + '&semanticType='
              + encodeURIComponent(semanticType)
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // approve concept
        this.approveConcept = function(projectId, activityId, concept, overrideWarnings) {
          console.debug('approve concept');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/concept/approve?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // merge concepts
        this.mergeConcepts = function(projectId, activityId, concept1, concept2, overrideWarnings) {
          console.debug('merge concepts', concept1.lastModified);
          var deferred = $q.defer();

          // Merge concepts
          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/concept/merge?projectId='
              + projectId
              + '&conceptId='
              + concept1.id
              + '&lastModified='
              + concept1.lastModified
              + '&conceptId2='
              + concept2.id
              + (activityId ? "&activityId=" + activityId : "")
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // move atoms
        this.moveAtoms = function(projectId, activityId, concept1, concept2, atomIds,
          overrideWarnings) {
          console.debug('move atoms');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/atom/move?projectId='
              + projectId
              + '&conceptId='
              + concept1.id
              + '&lastModified='
              + concept1.lastModified
              + '&conceptId2='
              + concept2.id
              + (activityId ? "&activityId=" + activityId : "")
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), atomIds).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // remove atom
        this.removeAtom = function(projectId, activityId, concept, atomId, overrideWarnings) {
          console.debug('remove atom');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/atom/remove/'
              + atomId
              + '?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // remove attribute
        this.removeAttribute = function(projectId, activityId, concept, attributeId,
          overrideWarnings) {
          console.debug('remove attribute');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/attribute/remove/'
              + attributeId
              + '?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // remove relationship
        this.removeRelationship = function(projectId, activityId, concept, relationshipId,
          overrideWarnings) {
          console.debug('remove relationship');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/relationship/remove/'
              + relationshipId
              + '?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // remove semantic type
        this.removeSemanticType = function(projectId, activityId, concept, semanticTypeId,
          overrideWarnings) {
          console.debug('remove semantic type');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/sty/remove/'
              + semanticTypeId
              + '?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), null).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // split concept
        this.splitConcept = function(projectId, activityId, concept1, atomIds,
          copyRelationships, copySemanticTypes, relationshipType, overrideWarnings) {
          console.debug('split concept');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/concept/split?projectId='
              + projectId
              + '&conceptId='
              + concept1.id
              + '&lastModified='
              + concept1.lastModified
              + (activityId ? "&activityId=" + activityId : "")
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : '')
              + (copyRelationships != null && copyRelationships != '' ? '&copyRelationships='
                + copyRelationships : '')
              + (copySemanticTypes != null && copySemanticTypes != '' ? '&copySemanticTypes='
                + copySemanticTypes : '') 
              + (relationshipType != null && relationshipType != '' ? '&relationshipType=' 
                + relationshipType : ''), atomIds).then(
          // success
          function(response) {
            console.debug('  validation = ', response.data);
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

        // undo action
        this.undoAction = function(projectId, activityId, molecularActionId, force) {
          console.debug('undoAction');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl + '/action/undo?projectId=' + projectId + '&molecularActionId='
              + molecularActionId + (activityId ? "&activityId=" + activityId : "")
              + (force ? "&force=" + force : ""), null).then(
          // success
          function(response) {
            console.debug('  successful undo');
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

        // redo action
        this.redoAction = function(projectId, activityId, molecularActionId, force) {
          console.debug('redoAction');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl + '/action/redo?projectId=' + projectId + '&molecularActionId='
              + molecularActionId + (activityId ? "&activityId=" + activityId : "")
              + (force ? "&force=" + force : ""), null).then(
          // success
          function(response) {
            console.debug('  successful redo');
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
