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
      $rootScope.$broadcast('termServer::worklistChanged', worklist);
    };

    this.getRecordTypes = function() {
      return [ 'N', 'R' ];
    }

    this.getConfigTypes = function() {
      return [ 'MUTUALLY_EXCLUSIVE', 'QUALITY_ASSURANCE', 'AD_HOC' ];
    }

    // get all workflow paths
    this.getWorkflowPaths = function() {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/paths').then(
      // success
      function(response) {
        console.debug('  paths = ', response.data);
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
    this.addWorkflowConfig = function(projectId, config) {
      console.debug('addWorkflowConfig');
      var deferred = $q.defer();

      // Add workflow config
      gpService.increment();
      $http.post(workflowUrl + '/config/add?projectId=' + projectId, config).then(
      // success
      function(response) {
        console.debug('  config = ', response.data);
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
    this.updateWorkflowConfig = function(projectId, config) {
      console.debug('updateWorkflowConfig', projectId, config);
      var deferred = $q.defer();

      // Update workflow config
      gpService.increment();
      $http.post(workflowUrl + '/config/update?projectId=' + projectId, config).then(
      // success
      function(response) {
        console.debug('  successful update workflow config');
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
      console.debug('updateWorklist', projectId, worklist);
      var deferred = $q.defer();

      // Update worklist
      gpService.increment();
      $http.post(workflowUrl + '/worklist/update?projectId=' + projectId, worklist).then(
      // success
      function(response) {
        console.debug('  successful update worklist');
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
    this.removeWorkflowConfig = function(projectId, configId) {
      console.debug('removeWorkflowConfig', projectId, configId);
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/config/' + configId + "/remove?projectId=" + projectId).then(
      // success
      function(response) {
        console.debug('  successful remove workflow config');
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
    this.addWorkflowBinDefinition = function(projectId, workflowBinDefinition, positionAfterId) {
      console.debug('addWorkflowBinDefinition');
      var deferred = $q.defer();

      // Add workflow bin Definition
      gpService.increment();
      $http.post(
        workflowUrl + '/definition/add?projectId=' + projectId
          + (positionAfterId ? '&positionAfterId=' + positionAfterId : ''), workflowBinDefinition)
        .then(
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
      console.debug('updateWorkflowBinDefinition', projectId, workflowBinDefinition);
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
    this.removeWorkflowBinDefinition = function(projectId, definitionId) {
      console.debug('removeWorkflowBinDefinition', projectId, definitionId);
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/definition/' + definitionId + "/remove").then(
      // success
      function(response) {
        console.debug('  successful remove workflow bin definition');
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
    this.regenerateBins = function(projectId, type) {
      console.debug('regenerateBins', projectId, type);
      var deferred = $q.defer();

      // regenerate workflow bins
      gpService.increment();
      $http.post(workflowUrl + '/bins?projectId=' + projectId, type).then(
      // success
      function(response) {
        console.debug('  successfully regenerated bins');
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
        console.debug('  assignedWork = ', response.data);
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
    this.findAssignedWorklists = function(projectId, userName, role, pfs) {
      console.debug('findAssignedWorklists', projectId, userName, role, pfs);

      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklist/assigned?projectId=' + projectId + '&userName=' + userName
          + '&role=' + role, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  assignedWorklists = ', response.data);
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
        console.debug('  availableWork = ', response.data);
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
    this.findAvailableWorklists = function(projectId, userName, role, pfs) {
      console.debug('findAvailableWorklists', projectId, userName, role, pfs);

      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklist/available?projectId=' + projectId + '&userName=' + userName
          + '&role=' + role, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  availableWorklists = ', response.data);
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
        console.debug('  checklists = ', response.data);
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
        console.debug('  worklists = ', response.data);
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
        console.debug('  reports = ', response.data);
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

    // get workflow bin definition
    this.getWorkflowBinDefinition = function(projectId, definitionName, type) {
      console.debug('getWorkflowBinDefinition', projectId, definitionName, type)
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(
        workflowUrl + '/definition?projectId=' + projectId + '&name=' + definitionName + '&type='
          + type).then(
      // success
      function(response) {
        console.debug('  definition = ', response.data);
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
      console.debug('getWorkflowBins', projectId, type);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/bin/all?projectId=' + projectId + '&type=' + type).then(
      // success
      function(response) {
        console.debug('  bins = ', response.data);
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
      console.debug('getWorklist', projectId, worklistId);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/worklist/' + worklistId + '?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  worklist = ', worklist);
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
      console.debug('getChecklist', projectId, checklistId);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/checklist/' + checklistId + '?projectId=' + projectId).then(
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

    // get log for project and refset/translation
    this.getLog = function(projectId, checklistId, worklistId, lines) {
      console.debug('getLog', projectId, checklistId, worklistId, lines);
      var deferred = $q.defer();
      var llines = lines ? lines : 1000;
      // Assign user to project
      gpService.increment();
      $http.get(
        workflowUrl + '/log?projectId=' + projectId
          + (checklistId ? '&checklistId=' + checklistId : '')
          + (worklistId ? '&worklistId=' + worklistId : '') + '&lines=' + llines, {
          transformResponse : [ function(response) {
            // Data response is plain text at this point
            // So just return it, or do your parsing here
            return response;
          } ]
        }).then(
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
      console.debug('getWorkflowConfigs', projectId);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/config/all?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  configs = ', response.data);
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
      console.debug('performWorkflowAction', projectId, worklistId, userName, role, action);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(
        workflowUrl + '/worklist/action?projectId=' + projectId + '&worklistId=' + worklistId
          + '&userName=' + userName + '&userRole=' + role + '&action=' + action).then(
      // success
      function(response) {
        console.debug('  action success', response.data);
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
          + (clusterType != 'default' ? '&clusterType=' + clusterType : '') + '&name=' + name
          + '&description=' + description + '&randomize=' + randomize + '&excludeOnWorklist='
          + excludeOnWorklist + (query != '' && query != null ? '&query=' + query : ''),
        utilService.prepPfs(pfs)).then(
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

    // Create a worklist
    this.createWorklist = function(projectId, workflowBinId, clusterType, pfs) {
      console.debug('createWorklist', projectId, workflowBinId, clusterType, pfs);

      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklist/add?projectId=' + projectId + '&workflowBinId=' + workflowBinId
          + (clusterType != 'default' ? '&clusterType=' + clusterType : ''),
        utilService.prepPfs(pfs)).then(
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

    // remove worklist
    this.removeWorklist = function(projectId, worklistId) {
      console.debug('removeWorklist', projectId, worklistId);

      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/worklist/' + worklistId + "/remove?projectId=" + projectId)
        .then(
        // success
        function(response) {
          console.debug('  successful remove worklist');
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
      console.debug('removechecklist', projectId, checklistId);
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/checklist/' + checklistId + "/remove?projectId=" + projectId)
        .then(
        // success
        function(response) {
          console.debug('  successful remove checklist');
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
      console.debug('findTrackingRecordsForWorklist', projectId, worklistId, pfs);
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.post(workflowUrl + '/worklist/' + worklistId + '/records?projectId=' + projectId, pfs)
        .then(
        // success
        function(response) {
          console.debug('  records = ', response.data);
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
      console.debug('findTrackingRecordsForChecklist', projectId, checklistId, pfs);
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http
        .post(workflowUrl + '/checklist/' + checklistId + '/records?projectId=' + projectId, pfs)
        .then(
        // success
        function(response) {
          console.debug('  records = ', response.data);
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
      console.debug('findTrackingRecordsForWorkflowBin', projectId, binId, pfs);

      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.post(workflowUrl + '/bin/' + binId + '/records?projectId=' + projectId, pfs).then(
      // success
      function(response) {
        console.debug('  records = ', response.data);
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
          console.debug('  successful remove note');
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
          console.debug('  successful remove note');
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

    // regenerate bins
    this.regenerateBins = function(projectId, workflowBinType) {
      console.debug('regenerate bins');
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.get(
        workflowUrl + '/bin/regenerate/all?projectId=' + projectId + '&type=' + workflowBinType)
        .then(
        // success
        function(response) {
          console.debug('  successful regenerate bins');
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

    // clear bins
    this.clearBins = function(projectId, type) {
      console.debug('clear bins', projectId, type);
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.get(workflowUrl + '/bin/clear/all?projectId=' + projectId + '&type=' + type).then(
      // success
      function(response) {
        console.debug('  successful clear bins');
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

    // test query
    this.testQuery = function(projectId, query, queryType) {
      console.debug('testQuery', projectId, query, queryType);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(
        workflowUrl + '/definition/test?projectId=' + projectId + '&query='
          + utilService.prepQuery(query) + '&queryType=' + queryType).then(
      // success
      function(response) {
        console.debug('  successful test query');
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
