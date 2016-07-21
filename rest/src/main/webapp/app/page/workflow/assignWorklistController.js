// Assign worklist controller
var AssignWorklistModalCtrl = function($scope, $uibModalInstance, $sce, workflowService, worklist, action,
  currentUser, project) {
  console.debug('Entered assign worklist modal control', worklist.id, action, currentUser, project.id);
  $scope.worklist = worklist;
  $scope.action = action;
  $scope.project = project;
  $scope.assignedUsers = Object.keys(project.userRoleMap);
  $scope.user = currentUser;
  $scope.note;
  $scope.errors = [];

  // Sort users by name and role restricts
  /*var sortedUsers = $scope.assignedUsers.sort(utilService.sort_by('name'));
  for (var i = 0; i < sortedUsers.length; i++) {
    if ($scope.role == 'AUTHOR'
      || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
      || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
      $scope.assignedUsers.push(sortedUsers[i]);
    }
  }*/

  // Assign (or reassign)
  $scope.assignWorklist = function() {
    if (!$scope.user) {
      $scope.errors[0] = 'The user must be selected. ';
      return;
    }

    if (action == 'ASSIGN') {
      workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user,
        $scope.project.userRoleMap[$scope.user], 'ASSIGN').then(
      // Success
      function(data) {

        // Add a note as well
        if ($scope.note) {
          workflowService.addWorklistNote(worklist.id, $scope.note).then(
          // Success
          function(data) {
            $uibModalInstance.close(worklist);
          },
          // Error
          function(data) {
            handleError($scope.errors, data);
          });
        }
        // close dialog if no note
        else {
          $uibModalInstance.close(worklist);
        }

      },
      // Error
      function(data) {
        //handleError($scope.errors, data);
        $uibModalInstance.close();
      });
    }

  }

  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.dismiss('cancel');
  };

};