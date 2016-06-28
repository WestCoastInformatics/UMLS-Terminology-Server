// Project Service
tsApp
  .service(
    'workflowService',
    [
      '$http',
      '$q',
      '$rootScope',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, gpService, utilService) {
        console.debug('configure workflowService');

        
        // get all workflow paths
        this.getWorkflowPaths = function() {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(workflowUrl + 'paths').then(
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
          $http.post(workflowUrl + 'config/add?projectId=' + projectId, workflowConfig).then(
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
          $http.post(workflowUrl + 'config/update?projectId=' + projectId, workflowConfig).then(
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


        // remove workflow config
        this.removeWorkflowConfig = function(workflowConfig) {
          console.debug();
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http['delete'](workflowUrl + 'config/' + workflowConfig.id + "/remove").then(
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
          $http.post(workflowUrl + 'definition/add?projectId=' + projectId + 
            '&configId=' workflowConfigId, workflowBinDefinition).then(
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
          $http.post(workflowUrl + 'definition/update?projectId=' + projectId, workflowBinDefinition).then(
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
          $http['delete'](workflowUrl + 'definition/' + workflowBinDefinition.id + "/remove").then(
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
          $http.post(workflowUrl + 'clear?projectId=' + projectId, workflowBinType).then(
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
          $http.post(workflowUrl + 'bins?projectId=' + projectId, workflowBinType).then(
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
          $http.post(workflowUrl + 'records/assigned?projectId=' + projectId + '&userName=' + userName,
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
          $http.post(workflowUrl + 'worklists/assigned?projectId=' + projectId + '&userName=' + userName,
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
          $http.post(workflowUrl + 'records/available?projectId=' + projectId + '&userName=' + userName,
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
          $http.post(workflowUrl + 'worklists/available?projectId=' + projectId + '&userName=' + userName,
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
          $http.post(workflowUrl + 'checklists?projectId=' + projectId + '&query=' + query,
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
          $http.get(workflowUrl + 'records?conceptId=' + conceptId).then(
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
        
        
       
        // get tracking records for concept
        this.performWorkflowAction = function(projectId, worklistId,
          userName, role, action) {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(workflowUrl + 'action?projectId=' + projectId + '&worklistId=' + worklistId + '&action=' + 
            action + '&userName=' + userName + '&userRole=' + userRole).then(
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
        // end
      } ]);
