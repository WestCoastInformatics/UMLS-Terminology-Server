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
      'securityService',
      'metadataService',
      'projectService',
      'configureService',
      function($scope, $http, $location, $uibModal, gpService, utilService, tabService,
        securityService, metadataService, projectService, configureService) {
        console.debug('configure AdminCtrl');

        tabService.setShowing(true);

        // Clear error
        utilService.clearError();

        // Handle resetting tabs on 'back' and 'reload' events button
        tabService.setSelectedTabByLabel('Admin');

        //
        // Scope Variables
        //
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        
        // If logged in as guest, redirect
        if (securityService.isGuestUser()) {
          $location.path('/');
          return;
        }

        $scope.project = null;
        $scope.projectRoles = [];

        // Model variables
        $scope.projects = null;
        $scope.candiateProjects = null;
        $scope.users = null;
        $scope.assignedUsers = null;
        $scope.unassignedUsers = null;

        // Metadata for refsets, projects, etc.
        $scope.metadata = metadataService.getModel();

        $scope.userPreferences = {
          feedbackEmail : $scope.user.userPreferences.feedbackEmail
        };
        $scope.feedbackEmailChanged = false;

        // Paging variables
        $scope.pageSize = 10;
        $scope.paging = {};
        $scope.paging['project'] = {
          page : 1,
          filter : '',
          sortField : 'lastModified',
          ascending : null
        };
        $scope.paging['candidateProject'] = {
          page : 1,
          filter : '',
          sortField : 'lastModified',
          ascending : null
        };
        $scope.paging['user'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['assignedUser'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['candidateUser'] = {
          page : 1,
          filter : '',
          sortField : 'userName',
          ascending : null
        };
        $scope.paging['lang'] = {
          page : 1,
          filter : '',
          typeFilter : '',
          sortField : 'refsetId',
          ascending : true
        };

        // Get $scope.projects
        $scope.getProjects = function() {

          var pfs = {
            startIndex : ($scope.paging['project'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['project'].sortField,
            ascending : $scope.paging['project'].ascending == null ? true
              : $scope.paging['project'].ascending,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMINISTRATOR'
          };
          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMINISTRATOR') {
            pfs.queryRestriction = null;
          }
          projectService.findProjects($scope.paging['project'].filter, pfs).then(function(data) {
            $scope.projects = data.projects;
            $scope.projects.totalCount = data.totalCount;

          });

        };

        // Get $scope.candidateProjects
        // one of these projects can be selected for user and role
        // assignment
        $scope.getCandidateProjects = function() {

          var pfs = {
            startIndex : ($scope.paging['candidateProject'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['candidateProject'].sortField,
            ascending : $scope.paging['candidateProject'].ascending == null ? true
              : $scope.paging['candidateProject'].ascending,
            queryRestriction : 'userRoleMap:' + $scope.user.userName + 'ADMINISTRATOR'
          };
          // clear queryRestriction for application admins
          if ($scope.user.applicationRole == 'ADMINISTRATOR') {
            pfs.queryRestriction = null;
          }

          projectService.findProjects($scope.paging['candidateProject'].filter, pfs).then(
            function(data) {
              $scope.candidateProjects = data.projects;
              $scope.candidateProjects.totalCount = data.totalCount;
            });

        };

        // Get $scope.users
        $scope.getUsers = function() {

          var pfs = {
            startIndex : ($scope.paging['user'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['user'].sortField,
            ascending : $scope.paging['user'].ascending,
            ascending : $scope.paging['user'].ascending == null ? true
              : $scope.paging['user'].ascending,
            queryRestriction : null
          };

          securityService.findUsersAsList($scope.paging['user'].filter, pfs).then(function(data) {
            $scope.users = data.users;
            $scope.users.totalCount = data.totalCount;
          });

        };

        // Get $scope.unassignedUsers
        // this is the list of users that are not yet
        // assigned to the selected project
        $scope.getUnassignedUsers = function() {
          var pfs = {
            startIndex : ($scope.paging['candidateUser'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['candidateUser'].sortField,
            ascending : $scope.paging['candidateUser'].ascending == null ? true
              : $scope.paging['candidateUser'].ascending,
            queryRestriction : '(applicationRole:USER OR applicationRole:ADMINISTRATOR)'
          };

          projectService.findUnassignedUsersForProject($scope.project.id,
            $scope.paging['candidateUser'].filter, pfs).then(function(data) {
            $scope.unassignedUsers = data.users;
            $scope.unassignedUsers.totalCount = data.totalCount;
          });
        };

        // Get $scope.assignedUsers
        // this is the list of users that are already
        // assigned to the selected project
        $scope.getAssignedUsers = function() {

          var pfs = {
            startIndex : ($scope.paging['assignedUser'].page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : $scope.paging['assignedUser'].sortField,
            ascending : $scope.paging['assignedUser'].ascending == null ? true
              : $scope.paging['assignedUser'].ascending,
            queryRestriction : null
          };
          projectService.findAssignedUsersForProject($scope.project.id,
            $scope.paging['assignedUser'].filter, pfs).then(function(data) {
            $scope.assignedUsers = data.users;
            $scope.assignedUsers.totalCount = data.totalCount;
          });

        };

        // Get $scope.applicationRoles
        $scope.getApplicationRoles = function() {
          securityService.getApplicationRoles().then(function(data) {
            $scope.applicationRoles = data.strings;
          });
        };

        // Get $scope.projectRoles
        $scope.getProjectRoles = function() {
          projectService.getProjectRoles().then(function(data) {
            $scope.projectRoles = data.strings;
          });
        };

        // Get $scope.metadata.terminologies (unless already set)
        $scope.getTerminologies = function() {
          if (!$scope.metadata.terminologies) {
            metadataService.initTerminologies().then(function(data) {
              $scope.metadata.terminologies = data.terminologies;
            });
          }
        };

        // Sets the selected project
        $scope.setProject = function(project) {
          if (!project) {
            return;
          }
          // Don't re-select
          if ($scope.project && project.id == $scope.project.id) {
            return;
          }

          $scope.project = project;
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
          projectService.removeProject(project).then(
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
          securityService.removeUser(user).then(function() {
            // Refresh users
            $scope.getUsers();
            if ($scope.project != null) {
              $scope.getUnassignedUsers();
              $scope.getAssignedUsers();
            }
          });

        };

        // update a specific user preference
        $scope.saveUserPreference = function(item, value) {
          if (item == 'feedbackEmail') {
            $scope.user.userPreferences.feedbackEmail = value;
            $scope.feedbackEmailChanged = false;
          }

          $scope.saveUserPreferences();
        };

        // Save the user preferences
        $scope.saveUserPreferences = function() {
          securityService.updateUserPreferences($scope.user.userPreferences).then(
          // Success
          function(data) {
            $scope.user.userPreferences = data;
          });
        };

        // indicate that a user preference value has changed
        $scope.setChanged = function(item) {
          if (item == 'moduleId') {
            $scope.moduleIdChanged = true;
          } else if (item == 'namespace') {
            $scope.namespaceChanged = true;
          } else if (item == 'organization') {
            $scope.organizationChanged = true;
          } else if (item == 'exclusionClause') {
            $scope.exclusionClauseChanged = true;
          } else if (item == 'feedbackEmail') {
            $scope.feedbackEmailChanged = true;
          }
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
          } else if (table === 'candidateUser') {
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
            $scope.project = data;
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
            $scope.project = data;
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
            $scope.validationChecks = data.keyValuePairs;
          });
        };

        //
        // MODALS
        //

        // Add project modal
        $scope.openAddProjectModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            backdrop : 'static',
            controller : AddProjectModalCtrl,
            resolve : {
              metadata : function() {
                return $scope.metadata;
              },
              user : function() {
                return $scope.user;
              },
              validationChecks : function() {
                return $scope.validationChecks;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(project) {
            projectService.fireProjectChanged(project);
            $scope.getProjects();
            $scope.getCandidateProjects();

          });
        };

        // Add project controller
        var AddProjectModalCtrl = function($scope, $uibModalInstance, metadata, user,
          validationChecks) {

          $scope.action = 'Add';
          $scope.project = {
            terminology : metadata.terminologies[0].terminology,
            feedbackEmail : user.userPreferences.feedbackEmail
          };
          $scope.clause = {
            value : null
          };
          $scope.terminologies = metadata.terminologies;
          $scope.metadata = metadata;
          $scope.user = user;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.errors = [];

          // Wire default validation check 'on' by default
          for (var i = 0; i < $scope.validationChecks.length; i++) {
            if ($scope.validationChecks[i].value.startsWith('Default')) {
              $scope.selectedChecks.push($scope.validationChecks[i].value);
            } else {
              $scope.availableChecks.push($scope.validationChecks[i].value);
            }
          }

          // move a check from unselected to selected
          $scope.selectValidationCheck = function(check) {
            $scope.selectedChecks.push(check);
            var index = $scope.availableChecks.indexOf(check);
            $scope.availableChecks.splice(index, 1);
          };

          // move a check from selected to unselected
          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          };

          // Function to filter viewable terminologies for picklist
          $scope.getViewableTerminologies = function() {
            var viewableTerminologies = new Array();
            if (!$scope.metadata.terminologies) {
              return viewableTerminologies;
            }
            for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
              // exclude MTH and SRC
              if ($scope.metadata.terminologies[i].terminology != 'MTH'
                && $scope.metadata.terminologies[i].terminology != 'SRC')
                viewableTerminologies.push($scope.metadata.terminologies[i]);
            }
            return viewableTerminologies;
          };

          // Add the project
          $scope.submitProject = function(project) {
            if (!project || !project.name || !project.description || !project.terminology) {
              window.alert('The name, description, and terminology fields cannot be blank. ');
              return;
            }
            // Connect validation checks
            project.validationChecks = [];
            for (var i = 0; i < $scope.validationChecks.length; i++) {
              if ($scope.selectedChecks.indexOf($scope.validationChecks[i].value) != -1) {
                project.validationChecks.push($scope.validationChecks[i].key);
              }
            }

            // Add project - this will validate the expression
            projectService.addProject(project).then(
              // Success
              function(data) {
                // if not an admin, add user as a project admin
                if ($scope.user.applicationRole != 'ADMINISTRATOR') {
                  var projectId = data.id;
                  projectService
                    .assignUserToProject(data.id, $scope.user.userName, 'ADMINISTRATOR').then(
                      function(data) {
                        // Update 'anyrole'
                        projectService.getUserHasAnyRole();

                        // Set the "last project" setting to this project
                        $scope.user.userPreferences.lastProjectId = projectId;
                        securityService.updateUserPreferences($scope.user.userPreferences);
                        $uibModalInstance.close(data);
                      },
                      // Error
                      function(data) {
                        $scope.errors[0] = data;
                        utilService.clearError();
                      });
                } else {
                  $uibModalInstance.close(data);
                }
              },
              // Error
              function(data) {
                $scope.errors[0] = data;
                utilService.clearError();
              });
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // modal for editing a project - only application admins can do
        // this
        $scope.openEditProjectModal = function(lproject) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editProject.html',
            backdrop : 'static',
            controller : EditProjectModalCtrl,
            resolve : {
              project : function() {
                return lproject;
              },
              metadata : function() {
                return $scope.metadata;
              },
              validationChecks : function() {
                return $scope.validationChecks;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function() {
            $scope.getCandidateProjects();
          });
        };

        var EditProjectModalCtrl = function($scope, $uibModalInstance, project, metadata,
          validationChecks) {

          $scope.action = 'Edit';
          $scope.clause = {
            value : project.exclusionClause
          };
          $scope.project = project;
          $scope.metadata = metadata;
          $scope.terminologies = metadata.terminologies;
          $scope.validationChecks = validationChecks;
          $scope.availableChecks = [];
          $scope.selectedChecks = [];
          $scope.errors = [];

          for (var i = 0; i < $scope.validationChecks.length; i++) {
            if (project.validationChecks.indexOf($scope.validationChecks[i].key) > -1) {
              $scope.selectedChecks.push($scope.validationChecks[i].value);
            } else {
              $scope.availableChecks.push($scope.validationChecks[i].value);
            }
          }

          $scope.selectValidationCheck = function(check) {
            $scope.selectedChecks.push(check);
            var index = $scope.availableChecks.indexOf(check);
            $scope.availableChecks.splice(index, 1);
          };

          $scope.removeValidationCheck = function(check) {
            $scope.availableChecks.push(check);
            var index = $scope.selectedChecks.indexOf(check);
            $scope.selectedChecks.splice(index, 1);
          };

          // Function to filter viewable terminologies for picklist
          $scope.getViewableTerminologies = function() {
            var viewableTerminologies = new Array();
            if (!$scope.metadata.terminologies) {
              return viewableTerminologies;
            }
            for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
              // exclude MTH and SRC
              if ($scope.metadata.terminologies[i].terminology != 'MTH'
                && $scope.metadata.terminologies[i].terminology != 'SRC')
                viewableTerminologies.push($scope.metadata.terminologies[i]);
            }
            return viewableTerminologies;
          };

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

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // Add user modal
        $scope.openAddUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            backdrop : 'static',
            controller : AddUserModalCtrl,
            resolve : {
              user : function() {
                return luser;
              },
              applicationRoles : function() {
                return $scope.applicationRoles;
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

        // Add user controller
        var AddUserModalCtrl = function($scope, $uibModalInstance, user, applicationRoles) {
          $scope.action = 'Add';
          $scope.user = user;
          $scope.applicationRoles = applicationRoles;
          $scope.errors = [];

          $scope.submitUser = function(user) {
            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert('The name, user name, and application role fields cannot be blank. ');
              return;
            }
            securityService.addUser(user).then(
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

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

        };

        // modal for editing a user - only application admins can do
        // this
        $scope.openEditUserModal = function(luser) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/admin/editUser.html',
            backdrop : 'static',
            controller : EditUserModalCtrl,
            resolve : {
              user : function() {
                return luser;
              },
              applicationRoles : function() {
                return $scope.applicationRoles;
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

        var EditUserModalCtrl = function($scope, $uibModalInstance, user, applicationRoles) {

          $scope.action = 'Edit';
          $scope.user = user;
          // copy data structure so it will be fresh each time modal is opened
          $scope.applicationRoles = JSON.parse(JSON.stringify(applicationRoles));
          $scope.errors = [];

          // those without application admin roles, can't give themselves admin
          // roles
          if (user.applicationRole != 'ADMINISTRATOR') {
            var index = $scope.applicationRoles.indexOf('ADMINISTRATOR');
            $scope.applicationRoles.splice(index, 1);
          }

          $scope.submitUser = function(user) {

            if (!user || !user.name || !user.userName || !user.applicationRole) {
              window.alert('The name, user name, and application role fields cannot be blank. ');
              return;
            }

            securityService.updateUser(user).then(
            // Success
            function(data) {
              $uibModalInstance.close();
            },
            // Error
            function(data) {
              $scope.error[0] = data;
              utilService.clearError();
            });
          };

          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };

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
          $scope.getTerminologies();
          $scope.getValidationChecks();

          // Handle users with user preferences
          if ($scope.user.userPreferences) {
            $scope.configureTab();
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
