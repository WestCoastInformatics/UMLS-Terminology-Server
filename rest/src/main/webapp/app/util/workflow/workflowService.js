// Workflow Service
var workflowUrl = 'workflow';
tsApp.service('workflowService', [
  '$http',
  '$q',
  'Upload',
  'gpService',
  'utilService',
  function($http, $q, Upload, gpService, utilService) {

    this.getRecordTypes = function() {
      return [ 'N', 'R' ];
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

    // get workflow epoch
    this.getWorkflowEpoch = function(projectId) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/epoch?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  epoch = ', response.data);
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

    // add workflow epoch
    this.addWorkflowEpoch = function(projectId, epoch) {
      console.debug('addWorkflowEpoch');
      var deferred = $q.defer();

      // Add workflow config
      gpService.increment();
      $http.put(workflowUrl + '/epoch?projectId=' + projectId, epoch).then(
      // success
      function(response) {
        console.debug('  epoch = ', response.data);
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
      $http.put(workflowUrl + '/config?projectId=' + projectId, config).then(
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
      $http.post(workflowUrl + '/config?projectId=' + projectId, config).then(
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

    // export workflow
    this.exportWorkflow = function(projectId, workflowId) {
      console.debug('exportProcess', projectId, workflowId);
      gpService.increment();
      $http.post(
        workflowUrl + '/config/export?projectId=' + projectId + '&workflowId=' + workflowId, '')
        .then(
        // Success
        function(response) {
          var blob = new Blob([ JSON.stringify(response.data, null, 2) ], {
            type : ''
          });

          // fake a file URL and download it
          var fileURL = URL.createObjectURL(blob);
          var a = document.createElement('a');
          a.href = fileURL;
          a.target = '_blank';
          a.download = 'workflow.' + workflowId + '.txt';
          document.body.appendChild(a);
          gpService.decrement();
          a.click();

        },
        // Error
        function(response) {
          utilService.handleError(response);
          gpService.decrement();
        });
    };

    // Import workflow
    this.importWorkflow = function(projectId, file) {
      console.debug('importWorkflow', projectId);
      var deferred = $q.defer();
      gpService.increment();
      Upload.upload({
        url : workflowUrl + '/config/import?projectId=' + projectId,
        data : {
          file : file
        }
      }).then(
      // Success
      function(response) {
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      },
      // event
      function(evt) {
        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
        console.debug('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
      });
      return deferred.promise;
    };

    // update worklist
    this.updateWorklist = function(projectId, worklist) {
      console.debug('updateWorklist', projectId, worklist);
      var deferred = $q.defer();

      // Update worklist
      gpService.increment();
      $http.post(workflowUrl + '/worklist?projectId=' + projectId, worklist).then(
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
      $http['delete'](workflowUrl + '/config/' + configId + "?projectId=" + projectId).then(
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

    // remove workflow epoch
    this.removeWorkflowEpoch = function(projectId, epochId) {
      console.debug('removeWorkflowEpoch', projectId, epochId);
      var deferred = $q.defer();

      // Add project
      gpService.increment();
      $http['delete'](workflowUrl + '/epoch/' + epochId + "?projectId=" + projectId).then(
      // success
      function(response) {
        console.debug('  successful remove workflow epoch');
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
      console.debug('addWorkflowBinDefinition', projectId, workflowBinDefinition, positionAfterId);
      var deferred = $q.defer();

      // Add workflow bin Definition
      gpService.increment();
      $http.put(
        workflowUrl + '/definition?projectId=' + projectId
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
      $http.post(workflowUrl + '/definition?projectId=' + projectId, workflowBinDefinition).then(
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
      $http['delete'](workflowUrl + '/definition/' + definitionId + '?projectId=' + projectId)
        .then(
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

    // Find done work
    this.findDoneWork = function(projectId, userName, pfs) {
      console.debug('findDoneWork', projectId, userName, pfs);

      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(workflowUrl + '/records/done?projectId=' + projectId + '&userName=' + userName,
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  doneWork = ', response.data);
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

    // Find done worklists
    this.findDoneWorklists = function(projectId, userName, role, pfs) {
      console.debug('findDoneWorklists', projectId, userName, role, pfs);

      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        workflowUrl + '/worklist/done?projectId=' + projectId + '&userName=' + userName + '&role='
          + role, utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  doneWorklists = ', response.data);
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
      $http.post(
        workflowUrl + '/checklist/find?projectId=' + projectId
          + (query ? '&query=' + utilService.prepQuery(query) : ''), utilService.prepPfs(pfs))
        .then(
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
      $http.post(
        workflowUrl + '/worklist/find?projectId=' + projectId
          + (query ? '&query=' + utilService.prepQuery(query) : ''), utilService.prepPfs(pfs))
        .then(
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

    // get all workflow epochs
    this.getWorkflowEpochs = function(projectId) {
      console.debug('getWorkflowEpochs', projectId);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(workflowUrl + '/epoch/all?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  epochs = ', response.data);
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
        workflowUrl + '/checklist?projectId=' + projectId + '&workflowBinId=' + workflowBinId
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

      // Make PUT call
      gpService.increment();
      $http.put(
        workflowUrl + '/worklist?projectId=' + projectId + '&workflowBinId=' + workflowBinId
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
      $http['delete'](workflowUrl + '/worklist/' + worklistId + "?projectId=" + projectId).then(
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
      $http['delete'](workflowUrl + '/checklist/' + checklistId + "?projectId=" + projectId).then(
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
          //utilService.handleError(response);
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
          //utilService.handleError(response);
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
      $http.put(workflowUrl + '/checklist/' + listId + '/note?projectId=' + projectId, note, {
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
      $http['delete'](workflowUrl + '/checklist/note/' + noteId + '?projectId=' + projectId).then(
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
      $http.put(workflowUrl + '/worklist/' + listId + '/note?projectId=' + projectId, note, {
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
      $http['delete'](workflowUrl + '/worklist/note/' + noteId + '?projectId=' + projectId).then(
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

    // regenerate bin
    this.regenerateBin = function(projectId, id, name, workflowBinType) {
      console.debug('regenerate bin', projectId, id, name, workflowBinType);
      var deferred = $q.defer();

      // find tracking records
      gpService.increment('Regenerating bin...');
      var url = workflowUrl + '/bin/' + id + '/regenerate?projectId=' + projectId + '&type='
        + workflowBinType;
      if (!id) {
        url = workflowUrl + '/definition/regenerate?projectId=' + projectId + '&type='
          + workflowBinType + '&name=' + name;
      }
      $http.post(url, '').then(
      // success
      function(response) {
        console.debug('  successful regenerate bin');
        gpService.decrement('Regenerating bin...');
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement('Regenerating bin...');
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // regenerate bins
    this.regenerateBins = function(projectId, workflowBinType) {
      console.debug('regenerate bins');
      var deferred = $q.defer();

      // find tracking records
      gpService.increment('Regenerating bins...');
      $http
        .post(
          workflowUrl + '/bin/regenerate/all?projectId=' + projectId + '&type=' + workflowBinType,
          '').then(
        // success
        function(response) {
          console.debug('  successful regenerate bins');
          gpService.decrement('Regenerating bins...');
          deferred.resolve(response.data);
        },
        // error
        function(response) {
          utilService.handleError(response);
          gpService.decrement('Regenerating bins...');
          deferred.reject(response.data);
        });
      return deferred.promise;
    };

    // clear bin
    this.clearBin = function(projectId, id) {
      console.debug('clear bin', projectId, id);
      var deferred = $q.defer();

      // find tracking records
      gpService.increment();
      $http.post(workflowUrl + '/bin/' + id + '/clear?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  successful clear bin');
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
      $http.post(workflowUrl + '/bin/clear/all?projectId=' + projectId + '&type=' + type, '').then(
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
    this.testQuery = function(projectId, query, queryType, queryStyle) {
      console.debug('testQuery', projectId, query, queryType, queryStyle);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(
        workflowUrl + '/query/test?projectId=' + projectId + '&query='
          + encodeURIComponent(query) + '&queryType=' + queryType + '&queryStyle=' + queryStyle
        ).then(
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

    // import checklist
    this.importChecklist = function(projectId, name, file) {
      console.debug('import checklist', projectId, name);
      var deferred = $q.defer();
      gpService.increment();
      Upload.upload({
        url : workflowUrl + '/checklist/import?projectId=' + projectId + '&name=' + name,
        data : {
          file : file
        }
      }).then(
      // Success
      function(response) {
        console.debug('  checklist imported =', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      },
      // event
      function(evt) {
        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
        console.debug('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
      });
      return deferred.promise;
    };

    // compute checklist
    this.computeChecklist = function(projectId, query, queryType, name, pfs) {
      console.debug('computeChecklist', projectId, query, queryType, name);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.post(
        workflowUrl + '/checklist/compute?projectId=' + projectId + '&query='
          + encodeURIComponent(query) + '&queryType=' + queryType + '&name=' + name, pfs).then(
      // success
      function(response) {
        console.debug('  checklist computed =', response.data);
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

    // Export checklist
    this.exportChecklist = function(projectId, id, name) {
      console.debug('exportChecklist', projectId, id, name);
      gpService.increment();
      $http.get(workflowUrl + '/checklist/' + id + '/export?projectId=' + projectId).then(
      // Success
      function(response) {
        var blob = new Blob([ response.data ], {
          type : ''
        });

        // fake a file URL and download it
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = '_blank';
        // File name based on checklist name
        a.download = name + '.xls';
        document.body.appendChild(a);
        gpService.decrement();
        a.click();
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Export worklist
    this.exportWorklist = function(projectId, id, name) {
      console.debug('exportWorklist', projectId, id, name);
      gpService.increment();
      $http.get(workflowUrl + '/worklist/' + id + '/export?projectId=' + projectId).then(
      // Success
      function(response) {
        var blob = new Blob([ response.data ], {
          type : ''
        });

        // fake a file URL and download it
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = '_blank';
        // File name based on worklist name
        a.download = name + '.xls';
        document.body.appendChild(a);
        gpService.decrement();
        a.click();
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Generate concept reports
    this.generateConceptReport = function(projectId, worklistId) {
      console.debug('generateConceptReport', projectId, worklistId);
      var deferred = $q.defer();
      // NO glass pane, this could take a while
      $http.get(
        workflowUrl + '/worklist/' + worklistId + '/report/generate?projectId=' + projectId
          + '&sendEmail=true', {
          transformResponse : [ function(response) {
            // Data response is plain text at this point
            // So just return it, or do your parsing here
            return response;
          } ]
        }).then(
      // Success
      function(response) {
        console.debug('  report = ' + response.data);
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // Find generated reports
    this.findGeneratedConceptReports = function(projectId, worklistName, pfs) {
      console.debug('findGeneratedConceptReports', projectId, worklistName, pfs);
      var deferred = $q.defer();

      gpService.increment();
      $http.post(
        workflowUrl + '/report?projectId=' + projectId
          + (worklistName ? '&query=' + encodeURIComponent(worklistName) : ''),
        utilService.prepPfs(pfs)).then(
      // Success
      function(response) {
        console.debug('  reports = ' + response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // Get generated concept report
    this.getGeneratedConceptReport = function(projectId, fileName) {
      console.debug('getGeneratedConceptReport', projectId, fileName);

      gpService.increment();
      $http.get(
        workflowUrl + '/report/' + encodeURIComponent(fileName) + '/?projectId=' + projectId).then(
      // Success
      function(response) {
        var blob = new Blob([ response.data ], {
          type : ''
        });

        // fake a file URL and download it
        var fileURL = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = fileURL;
        a.target = '_blank';
        // File name based on worklist name
        a.download = fileName;
        document.body.appendChild(a);
        gpService.decrement();
        a.click();
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
      });
    };

    // Remove generated concept report
    this.removeGeneratedConceptReport = function(projectId, fileName) {
      console.debug('removeGeneratedConceptReport', projectId, fileName);
      var deferred = $q.defer();

      gpService.increment();
      $http['delete'](
        workflowUrl + '/report/' + encodeURIComponent(fileName) + '?projectId=' + projectId).then(
      // Success
      function(response) {
        console.debug('  report succcessfully removed');
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // Error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // stamp worklist
    this.stampWorklist = function(projectId, worklist, approve, overrideWarnings) {
      console.debug('stamp worklist');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(
        workflowUrl
          + '/worklist/'
          + worklist.id
          + '/stamp?projectId='
          + projectId
          + (worklist.name ? "&activityId=" + worklist.name : "")
          + (approve != null && approve != '' ? '&approve=' + approve : '')
          + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
            + overrideWarnings : ''), null).then(
      // success
      function(response) {
        console.debug('  validation = Successful stamp');
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

    // stamp checklist
    this.stampChecklist = function(projectId, checklist, approve, overrideWarnings) {
      console.debug('stamp checklist');
      var deferred = $q.defer();

      gpService.increment();
      $http.post(
        workflowUrl
          + '/checklist/'
          + checklist.id
          + '/stamp?projectId='
          + projectId
          + (checklist.name ? "&activityId=" + checklist.name : "")
          + (approve != null && approve != '' ? '&approve=' + approve : '')
          + (overrideWarnings != null && overrideWarnings != '' ? '&overrideWarnings='
            + overrideWarnings : ''), null).then(
      // success
      function(response) {
        console.debug('  validation = Successful stamp');
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

    // recompute concept status
    this.recomputeConceptStatus = function(projectId, updateFlag) {
      console.debug('recompute concept status', updateFlag);
      var deferred = $q.defer();

      gpService.increment('Recomputing concept status...');
      $http.post(
        workflowUrl + '/status/compute?projectId=' + projectId
          + (updateFlag ? '&update=' + updateFlag : ''), null).then(
      // success
      function(response) {
        console.debug('  validation = Successful concept status recompute');
        gpService.decrement('Recomputing concept status...');
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement('Recomputing concept status...');
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    // end
  } ]);
