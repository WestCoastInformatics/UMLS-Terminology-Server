// Assign worklist controller
tsApp.controller('AssignWorklistModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  'worklist',
  'action',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user,
    worklist, action) {
    console.debug("configure AssignWorklistModalCtrl", worklist, action);

    // Scope vars
    $scope.worklist = worklist;
    $scope.action = action;
    $scope.project = selected.project;
    $scope.users = [];
    $scope.user = user;
    $scope.note = null;
    $scope.errors = [];

    // Handle team based projects
    if ($scope.project.teamBased && $scope.worklist.team) {
      for (var i = 0; i < lists.users.length; i++) {
        if (lists.users[i].team == $scope.worklist.team) {
          $scope.users.push(lists.users[i]);
        }
      }
    } else {
      $scope.users = lists.users;
    }

    // Assign (or reassign)
    $scope.assignWorklist = function() {
      if (!$scope.user) {
        $scope.errors[0] = 'The user must be selected. ';
        return;
      }

      if (action == 'ASSIGN') {
        workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
          $scope.selected.projectRole, 'ASSIGN').then(
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
          }

          // If user has a team, update worklist
          securityService.getUserByName($scope.user).then(

          // Success
          function(data) {
            $scope.user = data;
            if ($scope.user.team) {
              worklist.team = $scope.user.team;
            }
            $uibModalInstance.close(worklist);
          },
          // Error
          function(data) {
            utilService.handleDialogError(errors, data);
          });

        },
        // Error
        function(data) {
          $uibModalInstance.close();
        });
      }

    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);