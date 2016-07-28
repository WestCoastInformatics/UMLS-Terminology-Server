// Project Service
var projectUrl = 'project';
tsApp
  .service(
    'projectService',
    [
      '$http',
      '$q',
      '$rootScope',
      'gpService',
      'utilService',
      function($http, $q, $rootScope, gpService, utilService) {
        console.debug('configure projectService');

        // Declare the model
        var userProjectsInfo = {
          anyrole : null
        };

        // broadcasts a new project id
        this.fireProjectChanged = function(project) {
          $rootScope.$broadcast('refset:projectChanged', project);
        };

        // Gets the user projects info
        this.getUserProjectsInfo = function() {
          return userProjectsInfo;
        };

        // get all projects
        this.getProjects = function() {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(projectUrl + '/all').then(
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

        // get project
        this.getProject = function(projectId) {
          var deferred = $q.defer();

          // Get projects
          gpService.increment();
          $http.get(projectUrl + '/' + projectId).then(
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
        // add project
        this.addProject = function(project) {
          console.debug('addProject');
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http.put(projectUrl + '/add', project).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // update project
        this.updateProject = function(project) {
          console.debug();
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http.post(projectUrl + '/update', project).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // remove project
        this.removeProject = function(project) {
          console.debug();
          var deferred = $q.defer();

          // Add project
          gpService.increment();
          $http['delete'](projectUrl + '/remove/' + project.id).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // Finds projects as a list
        this.findProjects = function(query, pfs) {

          console.debug('findProjects', query, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(projectUrl + '/all?query=' + utilService.prepQuery(query),
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

        // Finds users on given project
        this.findAssignedUsersForProject = function(projectId, query, pfs) {

          console.debug('findAssignedUsersForProject', projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.post(
            projectUrl + '/' + projectId + '/users?query=' + utilService.prepQuery(query),
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

        // Finds users NOT on given project
        this.findUnassignedUsersForProject = function(projectId, query, pfs) {

          console.debug('findUnassignedUsersForProject', projectId, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.post(
            projectUrl + '/users/' + projectId + '/unassigned?query='
              + utilService.prepQuery(query), utilService.prepPfs(pfs)).then(
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

        // assign user to project
        this.assignUserToProject = function(projectId, userName, projectRole) {
          console.debug('assignUserToProject');
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();
          $http.get(
            projectUrl + '/assign?projectId=' + projectId + '&userName=' + userName + '&role='
              + projectRole).then(
          // success
          function(response) {
            console.debug('  project = ', response.data);
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

        // unassign user from project
        this.unassignUserFromProject = function(projectId, userName) {
          console.debug('unassignUserFromProject');
          var deferred = $q.defer();

          // Unassign user from project
          gpService.increment();
          $http.get(projectUrl + '/unassign?projectId=' + projectId + '&userName=' + userName)
            .then(
            // success
            function(response) {
              console.debug('  project = ', response.data);
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

        // get project roles
        this.getProjectRoles = function() {
          console.debug('getProjectRoles');
          var deferred = $q.defer();

          // Get project roles
          gpService.increment();
          $http.get(projectUrl + '/roles').then(
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

        // does user have any role on any project
        this.getUserHasAnyRole = function() {
          console.debug('getUserHasAnyRole');
          var deferred = $q.defer();

          // Get project roles
          gpService.increment();
          $http.get(projectUrl + '/user/anyrole').then(
          // success
          function(response) {
            userProjectsInfo.anyrole = (response.data != 'false');
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            gpService.decrement();
            utilService.handleError(response);
            deferred.reject(response.data);
          });
          return deferred.promise;
        };

        this.getSourceDataLog = function(terminology, version, activity, lines) {
          console.debug('getSourceDataLog', terminology, version, activity, lines);
          var deferred = $q.defer();

          if (!terminology && !version && !activity) {
            console
              .error('Must specify all of terminology, version, and activity (LOADING/REMOVING) to retrieve log entries');
            deferred.reject(null);
          }

          else {

            $http.get(
              projectUrl + '/log?terminology=' + sourceData.terminology + '&version='
                + sourceData.version + (lines ? '&lines=' + lines : '')).then(function(response) {
              deferred.resolve(response.data);
            }, function(error) {
              utilService.handleError(error);
              gpService.decrement();
              deferred.reject('Error retrieving source data log entries');
            });
          }
          return deferred.promise();
        };

        // get log for project and refset/translation
        this.getLog = function(projectId, objectId) {
          console.debug('getLog');
          var deferred = $q.defer();

          // Assign user to project
          gpService.increment();
          $http.get(
            projectUrl + '/log?projectId=' + projectId + (objectId ? '&objectId=' + objectId : '')
              + '&lines=1000').then(
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

        // get all validation check names
        this.getValidationCheckNames = function() {
          console.debug('getValidationCheckNames');
          var deferred = $q.defer();

          gpService.increment();
          $http.get(projectUrl + '/checks').then(
          // success
          function(response) {
            console.debug('  validation checks = ', response.data);
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
