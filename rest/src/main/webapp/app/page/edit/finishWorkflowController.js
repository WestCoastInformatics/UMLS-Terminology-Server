// Finish worklist modal controller
tsApp.controller('FinishWorklistModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'workflowService',
  'selected',
  'lists',
  'user',
  'worklist',
  function($scope, $uibModalInstance, workflowService, selected, lists, user, worklist) {
    console.debug('Entered finish worklist modal control');

    // Scope
    $scope.worklist = worklist;
    $scope.project = selected.project;
    $scope.projectRole = selected.projectRole;
    $scope.hours;
    $scope.minutes;
    $scope.errors = [];

    // Finish
    $scope.finish = function() {
      var seconds = (($scope.hours * 60 * 60) + ($scope.minutes * 60));
      if ($scope.projectRole == 'AUTHOR') {
        $scope.worklist.authorTime = seconds;
      } else if ($scope.projectRole == 'REVIEWER') {
        $scope.worklist.reviewerTime = seconds;
      }
      // update hours/minutes
      workflowService.updateWorklist($scope.project.id, $scope.worklist).then(

      // Success
      function(data) {
        // finish workflow
        $scope.finishWorkflow(worklist);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });
    };

    // finish worklist
    $scope.finishWorkflow = function(worklist) {
      workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
        $scope.user.userName, $scope.selected.projectRole, 'FINISH').then(
      // Success
      function(data) {
        $uibModalInstance.close();
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.close();
    };

    // end
  } ]);
