// Administration controller
tsApp
  .controller(
    'AdminCtrl',
    [
      '$scope',
      '$http',
      '$location',
      '$uibModal',
      'gpService',
      'utilService',
      'tabService',
      'configureService',
      'securityService',
      'metadataService',
      'projectService',
      function($scope, $http, $location, $uibModal, gpService, utilService, tabService,
        configureService, securityService, metadataService, projectService) {
        console.debug('configure AdminCtrl');

        // Set up tabs and controller
        tabService.setShowing(true);
        utilService.clearError();
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        tabService.setSelectedTabByLabel('Admin');

        // If logged in as guest, redirect
        if (securityService.isGuestUser()) {
          $location.path('/');
          return;
        }

        // Scope variables
        $scope.selected = {
          project : null,
          terminology : null,
          metadata : null
        }
        $scope.lists = {
          projects : [],
          candidateProjects : [],
          users : [],
          assignedusers : [],
          unassignedUsers : [],
          projectRoles : [],
          applicationRoles : [],
          validationChecks : [],
          terminologies : []
        }

        // Accordion Groups
        $scope.groups = [ {
          title : "Projects",
          open : false
        }, {
          title : "Users",
          open : false
        }, {
          title : "User Preferences",
          open : false
        } ];
        
        // Track user preferences changes
        $scope.changed = {
          feedbackEmail : false
        }

        // Paging variables
        $scope.paging = {};
        $scope.paging['project'] = utilService.getPaging();
        $scope.paging['project'].sortField = 'lastModified';
        $scope.paging['project'].callbacks = {
          getPagedList : getProjects
        };
        $scope.paging['candidateProject'] = utilService.getPaging();
        $scope.paging['candidateProject'].sortField = 'lastModified';
        $scope.paging['candidateProject'].callbacks = {
          getPagedList : getCandidateProjects
        };
        $scope.paging['user'] = utilService.getPaging();
        $scope.paging['user'].sortField = 'userName';
        $scope.paging['user'].callbacks = {
          getPagedList : getUsers
        };
        $scope.paging['assignedUser'] = utilService.getPaging();
        $scope.paging['assignedUser'].sortField = 'userName';
        $scope.paging['assignedUser'].callbacks = {
          getPagedList : getAssignedUsers
        };
        $scope.paging['unassignedUser'] = utilService.getPaging();
        $scope.paging['unassignedUser'].sortField = 'userName';
        $scope.paging['unassignedUser'].callbacks = {
          getPagedList : getUnassignedUsers
        };

        // Get $scope.lists.projects
        $scope.getProjects = function() {
          getProjects();
        }
        function getProjects() {
          var paging = $scope.paging['project'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };
          
          var query = '';
          projectService.findProjects(query, pfs).then(function(data) {
            $scope.lists.projects = data.projects;
            $scope.lists.projects.totalCount = data.totalCount;

          });
        }

        // Get $scope.lists.candidateProjects
        // one of these projects can be selected for user and role
        // assignment
        $scope.getCandidateProjects = function() {
          getCandidateProjects();
        }
        function getCandidateProjects() {
          var paging = $scope.paging['candidateProject'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          var query = 'userRoleMap:' + $scope.user.userName + 'ADMINISTRATOR';
          // no restrictions for application admin
          if ($scope.user.applicationRole == 'ADMINISTRATOR') {
            query = '';
          }

          projectService.findProjects(query, pfs).then(function(data) {
            $scope.lists.candidateProjects = data.projects;
            $scope.lists.candidateProjects.totalCount = data.totalCount;
          });

        }

        // Get $scope.lists.users
        $scope.getUsers = function() {
          getUsers();
        }
        function getUsers() {
          var paging = $scope.paging['user'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          securityService.findUsersAsList('', pfs).then(function(data) {
            $scope.lists.users = data.users;
            $scope.lists.users.totalCount = data.totalCount;
          });

        }

        // Get $scope.lists.assignedUsers
        // this is the list of users that are already
        // assigned to the selected project
        $scope.getAssignedUsers = function() {
          getAssignedUsers();
        }
        function getAssignedUsers() {
          var paging = $scope.paging['assignedUser'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };
          projectService.findAssignedUsersForProject($scope.selected.project.id, '', pfs).then(
            function(data) {
              $scope.lists.assignedUsers = data.users;
              $scope.lists.assignedUsers.totalCount = data.totalCount;
            });

        }

        // Get $scope.lists.unassignedUsers
        // this is the list of users that are not yet
        // assigned to the selected project
        $scope.getUnassignedUsers = function() {
          getUnassignedUsers();
        }
        function getUnassignedUsers() {
          var paging = $scope.paging['unassignedUser'];
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };
          var query = '(applicationRole:USER OR applicationRole:ADMINISTRATOR)';
          projectService.findUnassignedUsersForProject($scope.selected.project.id, query, pfs)
            .then(function(data) {
              $scope.lists.unassignedUsers = data.users;
              $scope.lists.unassignedUsers.totalCount = data.totalCount;
            });
        }

        // Get $scope.lists.applicationRoles
        $scope.getApplicationRoles = function() {
          securityService.getApplicationRoles().then(function(data) {
            $scope.lists.applicationRoles = data.strings;
          });
        };

        // Get $scope.lists.projectRoles
        $scope.getProjectRoles = function() {
          projectService.getProjectRoles().then(function(data) {
            $scope.lists.projectRoles = data.strings;
          });
        };
        
        $scope.hasPermissions = function(action) {
          return securityService.hasPermissions(action);
        }

        // Sets the selected project
        $scope.setProject = function(project) {
          if (!project) {
            return;
          }
          // Don't re-select
          if ($scope.selected.project && project.id == $scope.selected.project.id) {
            return;
          }

          $scope.selected.project = project;
          $scope.getUnassignedUsers();
          $scope.getAssignedUsers();

          resetPaging();

        };

        // Removes a project
        $scope.removeProject = function(project) {
          // check for users
          if (project.userRoleMap != null && project.userRoleMap != undefined
            && Object.keys(project.userRoleMap).length > 0) {
            if (!confirm('The project has users assigned to it.  Are you sure you want to remove the project ('
              + project.name + ') and unassign all of its users?')) {
              return;
            }
          }
          // Otherwise, remove project
          projectService.removeProject(project.id).then(
          // Success
          function() {
            // Refresh projects
            $scope.getProjects();
            $scope.getCandidateProjects();
          });

        };

        // Removes a user
        $scope.removeUser = function(user) {
          if (user.projectRoleMap && Object.keys(user.projectRoleMap).length > 0) {
            window.alert('You can not delete a user that is assigned to a project -'
              + 'Remove this user from all projects before deleting it');
            return;
          }
          securityService.removeUser(user.id).then(function() {
            // Refresh users
            $scope.getUsers();
            if ($scope.selected.project != null) {
              $scope.getUnassignedUsers();
              $scope.getAssignedUsers();
            }
          });

        };

        // update a specific user preference
        $scope.saveUserPreference = function(item) {
          $scope.changed[item] = false;
          $scope.saveUserPreferences();
        };

        // Save the user preferences
        $scope.saveUserPreferences = function() {
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        // indicate that a user preference value has changed
        $scope.setChanged = function(item) {
          $scope.changed[item] = true;
        };

        // sort mechanism
        $scope.setSortField = function(table, field) {
          utilService.setSortField(table, field, $scope.paging);

          // retrieve the correct table
          if (table === 'candidateProject') {
            $scope.getCandidateProjects();
          } else if (table === 'project') {
            $scope.getProjects();
          } else if (table === 'user') {
            $scope.getUsers();
          } else if (table === 'assignedUser') {
            $scope.getAssignedUsers();
          } else if (table === 'unassignedUser') {
            $scope.getUnassignedUsers();
          }
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        // assign user to project
        $scope.assignUserToProject = function(projectId, userName, projectRole) {
          if (projectId == null || projectId == undefined) {
            window.alert('Select a project before assigning a user! ');
            return;
          }
          // call service
          projectService.assignUserToProject(projectId, userName, projectRole).then(function(data) {
            // Update 'anyrole'
            projectService.getUserHasAnyRole();
            $scope.getProjects();
            $scope.selected.project = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          });
        };

        // remove user from project
        $scope.unassignUserFromProject = function(projectId, userName) {
          projectService.unassignUserFromProject(projectId, userName).then(function(data) {
            // Update 'anyrole' in case user removed themselves from the project
            projectService.getUserHasAnyRole();
            $scope.getProjects();
            $scope.selected.project = data;
            $scope.getAssignedUsers();
            $scope.getUnassignedUsers();
          });
        };

        // reset paging for all tables to page 1
        var resetPaging = function() {
          for ( var key in $scope.paging) {
            if ($scope.paging.hasOwnProperty(key)) {
              $scope.paging[key].page = 1;
            }
          }
          $scope.getAssignedUsers();
          $scope.getUnassignedUsers();
        };

        $scope.getValidationChecks = function() {
          projectService.getValidationCheckNames().then(
          // Success
          function(data) {
            $scope.lists.validationChecks = data.keyValuePairs;
          });
        };

        // force exception
        $scope.forceException = function(flag) {
          projectService.forceException(flag);
        }

        // Reload server config
        $scope.reloadConfig = function() {
          projectService.reloadConfigProperties().then(
          // Success
          function() {
            // reload page
            $location.path('/admin');
          });
        }

        $scope.resetUserPreferences = function(user) {
          securityService.resetUserPreferences(user);
        }
        
        
        $scope.saveAccordionStatus = function() {
          console.debug('saveAccordionStatus', $scope.groups);
          $scope.user.userPreferences.properties['adminGroups'] = JSON
            .stringify($scope.groups);
          securityService.updateUserPreferences($scope.user.userPreferences);
        }

        //
        // MODALS
        //

        // Add project modal
        $scope.openAddProjectModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            backdrop : 'static',
            controller : 'EditProjectModalCtrl',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              project : function() {
                return null;
              },
              validationChecks : function() {
                return $scope.lists.validationChecks;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(project) {
            // Update and reload projects
            $scope.getProjects();
            $scope.getCandidateProjects();

          });
        };

        // modal for editing a project - only application admins can do
        // this
        $scope.openEditProjectModal = function(lproject) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            backdrop : 'static',
            controller : 'EditProjectModalCtrl',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              project : function() {
                return lproject;
              },
              validationChecks : function() {
                return $scope.lists.validationChecks;
              },
              action : function() {
                return 'Edit';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            // Update and reload projects
            $scope.getProjects();
            $scope.getCandidateProjects();
          });
        };

        // Edit project modal controller
        var EditProjectModalCtrl = function($scope, $uibModalInstance, project, selected,
          validationChecks) {

          // Scope variables
          $scope.action = 'Edit';
          $scope.project = project;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.errors = [];
          $scope.selected = selected;

          // Attach validation checks
          for (var i = 0; i < $scope.validationChecks.length; i++) {
            if (project.validationChecks.indexOf($scope.validationChecks[i].key) > -1) {
              $scope.selectedChecks.push($scope.validationChecks[i].value);
            } else {
              $scope.availableChecks.push($scope.validationChecks[i].value);
            }
          }

          // Handle selecting a validation check
          $scope.selectValidationCheck = function(check) {
            $scope.selectedChecks.push(check);
            var index = $scope.availableChecks.indexOf(check);
            $scope.availableChecks.splice(index, 1);
          };

          // Handle removing a validation check
          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          };

          // Save the project
          $scope.submitProject = function(project) {
            if (!project || !project.name || !project.description || !project.terminology) {
              window.alert('The name, description, and terminology fields cannot be blank. ');
              return;
            }

            project.validationChecks = [];
            for (var i = 0; i < $scope.validationChecks.length; i++) {
              if ($scope.selectedChecks.indexOf($scope.validationChecks[i].value) != -1) {
                project.validationChecks.push($scope.validationChecks[i].key);
              }
            }

            // Update project - this will validate the expression
            projectService.updateProject(project).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.errors[0] = data;
              utilService.clearError();
            });
          };

          // dismiss the dialog
          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // Add user modal
        $scope.openAddUserModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            backdrop : 'static',
            controller : 'EditUserModalCtrl',
            resolve : {
              user : function() {
                return null;
              },
              applicationRoles : function() {
                return $scope.lists.applicationRoles;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(user) {
            $scope.getUnassignedUsers();
            $scope.getAssignedUsers();
          });
        };

        // modal for editing a user - only application admins can do
        // this
        $scope.openEditUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            backdrop : 'static',
            controller : 'EditUserModalCtrl',
            resolve : {
              user : function() {
                return luser;
              },
              applicationRoles : function() {
                return $scope.lists.applicationRoles;
              },
              action : function() {
                return 'Edit';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.getUnassignedUsers();
            $scope.getAssignedUsers();
          });
        };

        // Configure the tab
        $scope.configureTab = function() {
          $scope.user.userPreferences.lastTab = '/admin';
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        //
        // Initialize
        //
        $scope.initialize = function() {
          projectService.getUserHasAnyRole();
          $scope.getProjects();
          $scope.getUsers();
          $scope.getCandidateProjects();
          $scope.getApplicationRoles();
          $scope.getProjectRoles();
          $scope.getValidationChecks();

          // Get all terminologies
          metadataService.getTerminologies().then(
          // Success
          function(data) {
            $scope.lists.terminologies = data.terminologies;
          });

          // Handle users with user preferences
          if ($scope.user.userPreferences) {
            $scope.configureTab();
          }
          
          if ($scope.user.userPreferences.properties['adminGroups']) {
            var savedAdminGroups = JSON
              .parse($scope.user.userPreferences.properties['adminGroups']);
            angular.copy(savedAdminGroups, $scope.groups);
          }
        };

        //
        // Initialization: Check that application is configured
        //
        configureService.isConfigured().then(function(isConfigured) {
          if (!isConfigured) {
            $location.path('/configure');
          } else {
            $scope.initialize();
          }
        });

        // end

      }

    ]);
