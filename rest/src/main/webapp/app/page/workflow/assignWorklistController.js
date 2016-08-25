// Assign worklist controller
tsApp.controller('AssignWorklistModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'securityService',
  'workflowService',
  'selected',
  'lists',
  'user',
  'worklist',
  'action',
  function($scope, $uibModalInstance, utilService, securityService, workflowService, selected,
    lists, user, worklist, action) {
    console.debug("configure AssignWorklistModalCtrl", worklist, action, user);

    // Scope vars
    $scope.selected = selected;
    $scope.worklist = worklist;
    $scope.action = action;
    $scope.project = selected.project;
    $scope.role = worklist.authorAvailable ? 'AUTHOR' : 'REVIEWER';
    $scope.user = [];
    $scope.users = [];
    $scope.note = null;
    $scope.errors = [];
    $scope.tinymceOptions = utilService.tinymceOptions;

    // Handle team based projects
    for (var i = 0; i < lists.users.length; i++) {
      // If using team base, skip users not in team
      if ($scope.project.teamBased && $scope.worklist.team
        && lists.users[i].team != $scope.worklist.team) {
        continue;
      }

      // If assigning to author, all users have a role and so are good
      else if ($scope.role == 'AUTHOR') {
        $scope.users.push(lists.users[i]);
      }

      // If assigning to a reviewer, ensure a role of > author on the project
      else if ($scope.role == 'REVIEWER' && $scope.project.userRoleMap[user.userName]
        && $scope.project.userRoleMap[user.userName] != 'AUTHOR') {
        $scope.users.push(lists.users[i]);
      }
    }

    // Choose the same user object
    for (var i = 0; i < $scope.users.length; i++) {
      if ($scope.users[i].userName == user.userName) {
        $scope.user = $scope.users[i];
        break;
      }
    }

    $scope.users = $scope.users.sort(utilService.sortBy('userName'));

    // Assign (or reassign)
    $scope.assignWorklist = function() {
      if (!$scope.user) {
        $scope.errors[0] = 'The user must be selected. ';
        return;
      }

      if (action == 'ASSIGN') {

        // The role to use depends on the assignability of the worklist.
        // If "new", it's author, otherwise it's reviewer

        workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
          $scope.role, 'ASSIGN').then(
        // Success
        function(data) {

          // Add a note as well
          if ($scope.note) {
            workflowService.addWorklistNote($scope.project.id, worklist.id, $scope.note).then(
            // Success
            function(data) {
              $uibModalInstance.close(worklist);
            },
            // Error
            function(data) {
              utilService.handleDialogError(errors, data);
            });
          } else {
            $uibModalInstance.close(worklist);
          }

        },
        // Error
        function(data) {
          $uibModalInstance.close(data);
        });
      }

    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);