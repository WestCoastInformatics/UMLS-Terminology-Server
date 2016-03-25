// Project Service
tsApp.service('projectService', [
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

    var iconConfig = {};

    // broadcasts a new project id
    this.fireProjectChanged = function(project) {
      $rootScope.$broadcast('refset:projectChanged', project);
    };

    // Gets the user projects info
    this.getUserProjectsInfo = function() {
      return userProjectsInfo;
    };

    this.getIconConfig = function() {
      console.debug('get icon config', iconConfig);
      return iconConfig;
    };

    // get icon config info
    this.prepareIconConfig = function() {
      console.debug('prepareIconConfig');
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(projectUrl + 'icons').then(
      // success
      function(response) {
        console.debug('  icons = ', response.data);
        // Set the map of key=>value
        for (var i = 0; i < response.data.keyValuePairs.length; i++) {
          iconConfig[response.data.keyValuePairs[i].key] = response.data.keyValuePairs[i].value;
        }
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

    // Tests that the key has an icon
    this.hasIcon = function(key) {
      return iconConfig[key] !== undefined;
    };

    // Returns the icon path for the key (moduleId or namespaceId)
    this.getIcon = function(key) {
      return iconConfig[key];
    };

    // get all projects
    this.getProjects = function() {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(projectUrl + 'all').then(
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
      $http.get(projectUrl + projectId).then(
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
      $http.put(projectUrl + 'add', project).then(
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
      $http.post(projectUrl + 'update', project).then(
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
      $http['delete'](projectUrl + 'remove/' + project.id).then(
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
    this.findProjectsAsList = function(query, pfs) {

      console.debug('findProjectsAsList', query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(projectUrl + 'projects?query=' + utilService.prepQuery(query),
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
      $http.post(projectUrl + 'users/' + projectId + '?query=' + utilService.prepQuery(query),
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
        projectUrl + 'users/' + projectId + '/unassigned?query=' + utilService.prepQuery(query),
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

    // assign user to project
    this.assignUserToProject = function(projectId, userName, projectRole) {
      console.debug('assignUserToProject');
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment();
      $http.get(
        projectUrl + 'assign?projectId=' + projectId + '&userName=' + userName + '&role='
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
      $http.get(projectUrl + 'unassign?projectId=' + projectId + '&userName=' + userName).then(
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
      $http.get(projectUrl + 'roles').then(
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
      $http.get(projectUrl + 'user/anyrole').then(
      // success
      function(response) {
        console.debug('  anyrole = ' + response.data);
        userProjectsInfo.anyRole = (response.data != 'false');
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

    this.findConceptsForQuery = function(query, terminology, version, pfs) {

      console.debug('findConceptsForQuery', query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        projectUrl + 'concepts?query=' + utilService.prepQuery(query, true) + '&terminology='
          + terminology + '&version=' + version, utilService.prepPfs(pfs))

      .then(
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

    this.getConceptParents = function(terminologyId, terminology, version, translationId) {

      console.debug('getConceptParents', terminologyId);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.get(
        projectUrl + 'concept/parents?terminologyId=' + terminologyId + '&terminology='
          + terminology + '&version=' + version
          + (translationId != null ? '&translationId=' + translationId : '')).then(
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

    this.getConceptChildren = function(terminologyId, terminology, version, translationId, pfs) {

      console.debug('getConceptChildren', terminologyId);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(
        projectUrl + 'concept/children?terminologyId=' + terminologyId + '&terminology='
          + terminology + '&version=' + version
          + (translationId != null ? '&translationId=' + translationId : ''),
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

    // get concept with descriptions
    this.getFullConcept = function(terminologyId, terminology, version, translationId) {

      console.debug('getFullConcept', terminologyId);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.get(
        projectUrl + 'concept?terminologyId=' + terminologyId + '&terminology=' + terminology
          + '&version=' + version
          + (translationId != null ? '&translationId=' + translationId : '')).then(
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

    // get terminology editions
    this.getTerminologyEditions = function() {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(projectUrl + 'terminology/all').then(
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

    // get terminology versions
    this.getTerminologyVersions = function(terminology) {
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(projectUrl + 'terminology/' + terminology + '/all').then(
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

    // Get standard description types
    this.getStandardDescriptionTypes = function(terminology, version) {
      console.debug('getStandardDescriptionTypes', terminology, version);
      var deferred = $q.defer();

      // Get projects
      gpService.increment();
      $http.get(projectUrl + 'terminology/' + terminology + '/descriptiontypes?version=' + version)
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

    // get log for project and refset/translation
    this.getLog = function(projectId, objectId) {
      console.debug('getLog');
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment();
      $http
        .get(projectUrl + 'log?projectId=' + projectId + '&objectId=' + objectId + '&lines=1000')
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
    
    // assign user to project
    this.getReplacementConcepts = function(conceptId, terminology, version) {
      console.debug('getReplacementConcepts');
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment();
      $http.get(
        projectUrl + 'concept/replacements'
        + '?conceptId=' + conceptId + '&terminology=' + terminology
        + '&version=' + version).then(
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
    // end
  } ]);
