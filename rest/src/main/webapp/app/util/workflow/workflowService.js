// Workflow Service
var workflowUrl = 'workflow';
tsApp.service('workflowService', [
  '$http',
  '$q',
  '$rootScope',
  'gpService',
  'utilService',
  function($http, $q, $rootScope, gpService, utilService) {
    console.debug('configure workflowService');

    // broadcasts a workflow change
    this.fireWorklistChanged = function(worklist) {
      $rootScope.$broadcast('workflow:worklistChanged', worklist);
    };
    
    this.fireWorkflowBinsChanged = function(worklist) {
      $rootScope.$broadcast('workflow:workflowBinsChanged', worklist);
    };

    // get all workflow paths
    this.getWorkflowPaths = function() {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/paths').then(
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

    // add workflow config
    this.addWorkflowConfig = function(projectId, workflowConfig) {
      console.debug('addWorkflowConfig');
      var deferred = $q.defer();

      // Add workflow config
      gpService.increment();
      $http.post(workflowUrl + '/config/add?projectId=' + projectId, workflowConfig).then(
      // success
      function(response) {
        console.debug('  workflowConfig = ', response.data);
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

    // update workflow config
    this.updateWorkflowConfig = function(projectId, workflowConfig) {
      console.debug();
      var deferred = $q.defer();

      // Update workflow config
      gpService.increment();
      $http.post(workflowUrl + '/config/update?projectId=' + projectId, workflowConfig).then(
      // success
      function(response) {
        console.debug('  workflow config = ', response.data);
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

    // update worklist
    this.updateWorklist = function(projectId, worklist) {
      console.debug();
      var deferred = $q.defer();

      // Update worklist
      gpService.increment();
      $http.post(workflowUrl + '/worklist/update?projectId=' + projectId, worklist).then(
      // success
      function(response) {
        console.debug('  worklist = ', response.data);
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

    
    // remove workflow config
    this.removeWorkflowConfig = function(workflowConfig) {
      console.debug();
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/config/' + workflowConfig.id + "/remove").then(
      // success
      function(response) {
        console.debug('  workflow config = ', response.data);
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

    // add workflow bin Definition
    this.addWorkflowBinDefinition = function(projectId, workflowConfigId, workflowBinDefinition) {
      console.debug('addWorkflowBinDefinition');
      var deferred = $q.defer();

      // Add workflow bin Definition
      gpService.increment();
      $http.post(
        workflowUrl + '/definition/add?projectId=' + projectId + '&configId=' + workflowConfigId,
        workflowBinDefinition).then(
      // success
      function(response) {
        console.debug('  workflowBinDefinition = ', response.data);
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

    // update workflow binDefinition
    this.updateWorkflowBinDefinition = function(projectId, workflowBinDefinition) {
      console.debug();
      var deferred = $q.defer();

      // Update workflow binDefinition
      gpService.increment();
      $http.post(workflowUrl + '/definition/update?projectId=' + projectId, workflowBinDefinition)
        .then(
        // success
        function(response) {
          console.debug('  workflow bin Definition = ', response.data);
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

    // remove workflow bin Definition
    this.removeWorkflowBinDefinition = function(workflowBinDefinition) {
      console.debug();
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/definition/' + workflowBinDefinition.id + "/remove").then(
      // success
      function(response) {
        console.debug('  workflow bin Definition = ', response.data);
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

    // clear workflow bins
    this.clearBins = function(projectId, workflowBinType) {
      console.debug();
      var deferred = $q.defer();

      // clear workflow bins
      gpService.increment();
      $http.post(workflowUrl + '/clear?projectId=' + projectId, workflowBinType).then(
      // success
      function(response) {
        console.debug('  clear workflow bins = ', response.data);
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

    // regenerate workflow bins
    this.regenerateBins = function(projectId, workflowBinType) {
      console.debug();
      var deferred = $q.defer();

      // regenerate workflow bins
      gpService.increment();
      $http.post(workflowUrl + '/bins?projectId=' + projectId, workflowBinType).then(
      // success
      function(response) {
        console.debug('  regenerate workflow bins = ', response.data);
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

    // Find assigned work
    this.findAssignedWork = function(projectId, userName, pfs) {

      console.debug('findAssignedWork', projectId, userName, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/records/assigned?projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Find assigned worklists
    this.findAssignedWorklists = function(projectId, userName, pfs) {

      console.debug('findAssignedWorklists', projectId, userName, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklists/assigned?projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Find available work
    this.findAvailableWork = function(projectId, userName, pfs) {

      console.debug('findAvailableWork', projectId, userName, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/records/available?projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Find available worklists
    this.findAvailableWorklists = function(projectId, userName, pfs) {

      console.debug('findAvailableWorklists', projectId, userName, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklists/available?projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Finds checklists
    this.findChecklists = function(projectId, query, pfs) {

      console.debug('findChecklists', projectId, query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(workflowUrl + '/checklist?projectId=' + projectId, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Finds worklists
    this.findWorklists = function(projectId, query, pfs) {

      console.debug('findWorklists', projectId, query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(workflowUrl + '/worklist?projectId=' + projectId, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Finds generated concept reports
    this.findGeneratedConceptReports = function(projectId, query, pfs) {

      console.debug('findGeneratedConceptReports', projectId, query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(workflowUrl + '/report?projectId=' + projectId + '&query=' + query,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // get tracking records for concept
    this.getTrackingRecordsForConcept = function(conceptId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/records?conceptId=' + conceptId).then(
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

    // get all workflow bins
    this.getWorkflowBins = function(projectId, type) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/bin/all?projectId=' + projectId + '&type=' + type).then(
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
    
    // get worklist
    this.getWorklist = function(projectId, worklistId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/worklist/' + worklistId + '?projectId=' + projectId).then(
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

    // get checklist
    this.getChecklist = function(projectId, checklistId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/checklist/' + checklistId + '?projectId=' + projectId).then(
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
    
    // get all workflow configs
    this.getWorkflowConfigs = function(projectId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/config/all?projectId=' + projectId).then(
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

    // perform workflow action
    this.performWorkflowAction = function(projectId, worklistId, userName, role, action) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(
        workflowUrl + '/worklist/action?projectId=' + projectId + '&worklistId=' + worklistId
          + '&userName=' + userName + '&userRole=' + role + '&action=' + action).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Create a checklist
    this.createChecklist = function(projectId, workflowBinId, clusterType, name, description,
      randomize, excludeOnWorklist, query, pfs) {

      console.debug('createChecklist', projectId, workflowBinId, clusterType, name, description,
        randomize, excludeOnWorklist, query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/checklist/add?projectId=' + projectId + '&workflowBinId=' + workflowBinId
          + '&clusterType=' + clusterType + '&name=' + name + '&description=' + description
          + '&randomize=' + randomize + '&excludeOnWorklist=' + excludeOnWorklist + '&query='
          + query, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // Create a worklist
    this.createWorklist = function(projectId, workflowBinId, clusterType, pfs) {

      console.debug('createWorklist', projectId, workflowBinId, clusterType, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklist/add?projectId=' + projectId + '&workflowBinId=' + workflowBinId
          + '&clusterType=' + clusterType, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  output = ', response.data);
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

    // remove worklist
    this.removeWorklist = function(projectId, worklistId) {
      console.debug();
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/worklist/' + worklistId + "/remove?projectId=" + projectId)
        .then(
        // success
        function(response) {
          console.debug('  worklist = ', response.data);
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

    // remove checklist
    this.removeChecklist = function(projectId, checklistId) {
      console.debug();
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/checklist/' + checklistId + "/remove?projectId=" + projectId)
        .then(
        // success
        function(response) {
          console.debug('  checklist = ', response.data);
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

    // find tracking records for worklist
    this.findTrackingRecordsForWorklist = function(projectId, worklistId, pfs) {
      console.debug('findTrackingRecordsForWorklist');
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.post(workflowUrl + '/worklist/' + worklistId + '/records?projectId=' + projectId, pfs)
        .then(
        // success
        function(response) {
          console.debug('  trackingRecords = ', response.data);
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

    // find tracking records for checklist
    this.findTrackingRecordsForChecklist = function(projectId, checklistId, pfs) {
      console.debug('findTrackingRecordsForChecklist');
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http
        .post(workflowUrl + '/checklist/' + checklistId + '/records?projectId=' + projectId, pfs)
        .then(
        // success
        function(response) {
          console.debug('  trackingRecords = ', response.data);
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

    // find tracking records for workflow bin
    this.findTrackingRecordsForWorkflowBin = function(projectId, binId, pfs) {
      console.debug('findTrackingRecordsForWorkflowBin');
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.post(workflowUrl + '/bin/' + binId + '/records?projectId=' + projectId, pfs).then(
      // success
      function(response) {
        console.debug('  trackingRecords = ', response.data);
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

    this.addChecklistNote = function(projectId, listId, note) {
      console.debug('add checklist note', projectId, listId, note);
      var deferred = $q.defer();

      // Add list
      gpService.increment();
      $http.put(workflowUrl + '/checklist/' + listId + '/note/add?projectId=' + projectId, note, {
        headers : {
          'Content-type' : 'text/plain'
        }
      }).then(
      // success
      function(response) {
        console.debug('  note = ', response.data);
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

    this.removeChecklistNote = function(projectId, noteId) {
      console.debug('remove checklist note', projectId, noteId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](workflowUrl + '/checklist/note/' + noteId + '/remove?projectId=' + projectId)
        .then(
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
    
    this.addWorklistNote = function(projectId, listId, note) {
      console.debug('add worklist note', projectId, listId, note);
      var deferred = $q.defer();

      // Add list
      gpService.increment();
      $http.put(workflowUrl + '/worklist/' + listId + '/note/add?projectId=' + projectId, note, {
        headers : {
          'Content-type' : 'text/plain'
        }
      }).then(
      // success
      function(response) {
        console.debug('  note = ', response.data);
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

    this.removeWorklistNote = function(projectId, noteId) {
      console.debug('remove worklist note', projectId, noteId);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](workflowUrl + '/worklist/note/' + noteId + '/remove?projectId=' + projectId)
        .then(
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
