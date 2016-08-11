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
      'securityService',
      function($http, $q, $rootScope, gpService, utilService, securityService) {
        console.debug('configure projectService');

        // Declare the model
        var userProjectsInfo = {
          anyrole : null
        };

        // broadcasts a project change
        this.fireProjectChanged = function(project) {
          $rootScope.$broadcast('termServer::projectChanged', project);
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

          console.debug('findAssignedUsersForProject', projectId, query, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make PUT call
          gpService.increment();
          $http.post(projectUrl + '/' + projectId + '/users?query=' + utilService.prepQuery(query),
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

        // get query types
        this.getQueryTypes = function() {
          console.debug('getQueryTypes');
          var deferred = $q.defer();

          // Get project roles
          gpService.increment();
          $http.get(projectUrl + '/queryTypes').then(
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

        // Get projects where this user has a role
        // Return all projects and the default project choice
        this.getProjectsForUser = function(user) {
          console.debug('getProjectsForUser', user);
          var deferred = $q.defer();
          // Get all projects for this user
          var pfs = {
            startIndex : -1,
            maxResults : 10,
            sortField : 'name',
            queryRestriction : 'userAnyRole:' + user.userName
          };
          this.findProjects('', pfs).then(
          // Success
          function(data) {
            // Determine if user preferences has a last project id
            if (user.userPreferences.lastProjectId) {
              var found = false;
              for (var i = 0; i < data.projects.length; i++) {
                // if there is a matching one, choose it
                if (data.projects[i].id == user.userPreferences.lastProjectId) {
                  data.project = data.projects[i];
                  found = true;
                  break;
                }
              }
              // If not, choose the first one
              if (!found) {
                data.project = data.projects[0];
              }
            }
            // if not, choose the first one
            else {
              data.project = data.projects[0];
            }
            console.debug(' project,projects', data);
            deferred.resolve(data);
          },
          // Error
          function(data) {
            deferred.reject(data);
          });

          return deferred.promise;
        }

        // From the user and project, determine the user's default role
        // and save the lastProjectId and lastRole settings
        this.getRoleForProject = function(user, projectId) {
          console.debug('getRoleForProject', user.userName, projectId);
          var deferred = $q.defer();

          // Only save lastProjectRole if lastProject is the same
          if (user.userPreferences.lastProjectId != projectId) {
            console.debug('  null lastProjectRole, lastProjectId does not match');
            user.userPreferences.lastProjectRole = null;
          }
          user.userPreferences.lastProjectId = projectId;

          // Find role options for project and choose/save initial role
          this.findAssignedUsersForProject(projectId, '', {}).then(
          // Success
          function(data) {
            console.debug('  project assigned users', data.users, projectId);
            // Get assigned users for the selected project
            var assignedUsers = data.users;
            var role = 'AUTHOR';

            for (var i = 0; i < assignedUsers.length; i++) {
              if (assignedUsers[i].userName == user.userName) {
                role = assignedUsers[i].projectRoleMap[projectId];
                // Force the initial choice to be "AUTHOR" instead of
                // "ADMIN" when switching projects
                if (role == 'ADMINISTRATOR' && !user.userPreferences.lastProjectRole) {
                  role = 'AUTHOR';
                }
                if (user.userPreferences.lastProjectRole) {
                  role = user.userPreferences.lastProjectRole;
                }
                break;
              }
            }
            console.debug('  role choice', role);
            securityService.saveProjectIdAndRole(projectId, role);
            deferred.resolve(role);
          },
          // Error
          function(data) {
            deferred.reject(data);
          });
          return deferred.promise;
        }

        // Get role options
        this.getRoleOptions = function(role) {
          if (role == 'ADMINISTRATOR') {
            return [ 'ADMINISTRATOR', 'REVIEWER', 'AUTHOR' ];
          } else if (role == 'REVIEWER') {
            return [ 'REVIEWER', 'AUTHOR' ];
          } else if (role == 'AUTHOR') {
            return [ 'AUTHOR' ];
          }
          console.trace('Unexpected role option - ' + role);
          return [];
        }

        // end
      } ]);
