// Meta Editing Service
var metaEditingUrl = 'meta';
tsApp
  .service('metaEditingService',
    [
      '$http',
      '$q',
      '$uibModal',
      'gpService',
      'utilService',
      function($http, $q, $uibModal, gpService, utilService) {

        // add atom
        this.addAtom = function(projectId, activityId, concept, atom, overrideWarnings) {
          return addAtom(projectId, activityId, concept, atom, overrideWarnings);
        }
        function addAtom(projectId, activityId, concept, atom, overrideWarnings) {
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Add atom', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    addAtom(projectId, activityId, concept, atom, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
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
        this.updateAtom = function(projectId, activityId, concept, atom, overrideWarnings) {
          return updateAtom(projectId, activityId, concept, atom, overrideWarnings);
        }
        function updateAtom(projectId, activityId, concept, atom, overrideWarnings) {
          console.debug('update atom');
          var deferred = $q.defer();

          // Remove extra properties that may have been attached to definitions by the UI
          for (var j = 0; j < atom.definitions.length; j++) {
            delete atom.definitions[j].atomElement;
            delete atom.definitions[j].atomElementStr;
          }
          
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Update Atom', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    updateAtom(projectId, activityId, concept, atom, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // add attribute
        this.addAttribute = function(projectId, activityId, concept, attribute, overrideWarnings) {
          return addAttribute(projectId, activityId, concept, attribute, overrideWarnings);
        }
        function addAttribute(projectId, activityId, concept, attribute, overrideWarnings) {
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
        }
        ;

        // add relationship
        this.addRelationship = function(projectId, activityId, concept, relationship,
          overrideWarnings) {
          return addRelationship(projectId, activityId, concept, relationship, overrideWarnings);
        }
        function addRelationship(projectId, activityId, concept, relationship, overrideWarnings) {
          console.debug('add relationship');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/relationship/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), relationship).then(
            // success
            function(response) {
              console.debug('  validation = ', response.data);
              gpService.decrement();
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Add Relationship', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    addRelationship(projectId, activityId, concept, relationship, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }
        
        // add relationships
        this.addRelationships = function(projectId, activityId, concept, relationships,
          overrideWarnings) {
          return addRelationships(projectId, activityId, concept, relationships, overrideWarnings);
        }
        function addRelationships(projectId, activityId, concept, relationships, overrideWarnings) {
          console.debug('add relationships');
          var deferred = $q.defer();

          gpService.increment();
          $http.post(
            metaEditingUrl
              + '/relationships/add?projectId='
              + projectId
              + '&conceptId='
              + concept.id
              + (activityId ? "&activityId=" + activityId : "")
              + '&lastModified='
              + concept.lastModified
              + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
                + overrideWarnings : ''), relationships).then(
            // success
            function(response) {
              console.debug('  validation = ', response.data);
              gpService.decrement();
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Add Relationships', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    addRelationships(projectId, activityId, concept, relationships, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }        

        // add semantic type
        this.addSemanticType = function(projectId, activityId, concept, semanticType,
          overrideWarnings) {
          return addSemanticType(projectId, activityId, concept, semanticType, overrideWarnings);
        }
        function addSemanticType(projectId, activityId, concept, semanticType, overrideWarnings) {

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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Add Semantic Type', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    addSemanticType(projectId, activityId, concept, semanticType, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // approve concept
        this.approveConcept = function(projectId, activityId, concept, overrideWarnings) {
          return approveConcept(projectId, activityId, concept, overrideWarnings);
        }
        function approveConcept(projectId, activityId, concept, overrideWarnings) {

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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Approve Concept', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    approveConcept(projectId, activityId, concept, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // merge concepts
        this.mergeConcepts = function(projectId, activityId, concept1, concept2, overrideWarnings) {
          return mergeConcepts(projectId, activityId, concept1, concept2, overrideWarnings);
        }
        function mergeConcepts(projectId, activityId, concept1, concept2, overrideWarnings) {

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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Merge Concepts', concept1);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    mergeConcepts(projectId, activityId, concept1, concept2, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // move atoms
        this.moveAtoms = function(projectId, activityId, concept1, concept2, atomIds,
          overrideWarnings) {
          return moveAtoms(projectId, activityId, concept1, concept2, atomIds, overrideWarnings);
        }
        function moveAtoms(projectId, activityId, concept1, concept2, atomIds, overrideWarnings) {
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Move Concepts', concept1);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    moveAtoms(projectId, activityId, concept1, concept2, atomIds, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
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
        this.removeAtom = function(projectId, activityId, concept, atomId, overrideWarnings) {
          return removeAtom(projectId, activityId, concept, atomId, overrideWarnings);
        }
        function removeAtom(projectId, activityId, concept, atomId, overrideWarnings) {
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Remove Atom', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    removeAtom(projectId, activityId, concept, atomId, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // remove attribute
        this.removeAttribute = function(projectId, activityId, concept, attributeId,
          overrideWarnings) {
          return removeAttribute(projectId, activityId, concept, attributeId, overrideWarnings);
        }
        function removeAttribute(projectId, activityId, concept, attributeId, overrideWarnings) {
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
        }

        // remove relationship
        this.removeRelationship = function(projectId, activityId, concept, relationshipId,
          overrideWarnings) {
          return removeRelationship(projectId, activityId, concept, relationshipId,
            overrideWarnings);
        }
        function removeRelationship(projectId, activityId, concept, relationshipId,
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Remove Relationship', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    removeRelationship(projectId, activityId, concept, relationshipId, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // remove semantic type
        this.removeSemanticType = function(projectId, activityId, concept, semanticTypeId,
          overrideWarnings) {
          return removeSemanticType(projectId, activityId, concept, semanticTypeId,
            overrideWarnings);
        }
        function removeSemanticType(projectId, activityId, concept, semanticTypeId,
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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Remove Semantic Type', concept);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    removeSemanticType(projectId, activityId, concept, semanticTypeId, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

        // split concept
        this.splitConcept = function(projectId, activityId, concept1, atomIds, copyRelationships,
          copySemanticTypes, relationshipType, overrideWarnings) {
          return splitConcept(projectId, activityId, concept1, atomIds, copyRelationships,
            copySemanticTypes, relationshipType, overrideWarnings);
        }
        function splitConcept(projectId, activityId, concept1, atomIds, copyRelationships,
          copySemanticTypes, relationshipType, overrideWarnings) {

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
              if (response.data.errors.length > 0
                || (!overrideWarnings && response.data.warnings.length > 0)) {
                var modalInstance = openActionErrorsModal(response.data.errors,
                  response.data.warnings, 'Split Concept', concept1);
                modalInstance.result.then(
                // Success
                function(data) {
                  if (data) {
                    splitConcept(projectId, activityId, concept1, atomIds, copyRelationships,
                      copySemanticTypes, relationshipType, true).then(
                    // Success
                    function(data) {
                      deferred.resolve(data);
                    });
                  }
                });
              } else {
                deferred.resolve(response.data);
              }
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
          return deferred.promise;
        }

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
        }

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
        }

        // MODALS

        // Open the actions/errors modal following a molecular action
        var openActionErrorsModal = function(errors, warnings, activityId, lconcept) {

          return $uibModal.open({
            templateUrl : 'app/util/edit/actionErrorsWarnings.html',
            controller : 'ActionErrorsCtrl',
            backdrop : 'static',
            resolve : {
              errors : function() {
                return errors;
              },
              warnings : function() {
                return warnings;
              },
              action : function() {
                return activityId;
              },
              concept : function() {
                return lconcept;
              }
            }
          });

        };
        // end
      } ]);
