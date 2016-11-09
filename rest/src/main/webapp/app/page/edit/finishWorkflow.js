// Finish workflow modal controller
tsApp.controller('FinishWorkflowModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'workflowService',
  'utilService',
  'selected',
  'lists',
  'user',
  'worklist',
  'stamp',
  function($scope, $uibModalInstance, workflowService, utilService, selected, lists, user,
    worklist, stamp) {
    console.debug('Entered finish worklist modal control');

    // Scope
    $scope.worklist = worklist;
    $scope.project = selected.project;
    $scope.projectRole = worklist.reviewers.length == 0 ? 'AUTHOR' : 'REVIEWER';
    $scope.user = user;
    $scope.hours = 0;
    $scope.minutes = 0;
    $scope.errors = [];
    $scope.action = stamp ? "Stamp" : "Finish";

    // Finish
    $scope.finish = function() {

      $scope.errors = [];
      if ($scope.hours > 23) {
        $scope.errors.push('Invalid number of hours, > 24')
        return;
      }
      if ($scope.minutes > 59) {
        $scope.errors.push('Invalid number of minutes, > 59')
      }

      if (!$scope.hours) {
        $scope.hours = 0;
      }

      if (!$scope.minutes) {
        $scope.minutes = 0;
      }

      var seconds = (($scope.hours * 60 * 60) + ($scope.minutes * 60));
      console.debug('seconds', seconds);
      if ($scope.projectRole == 'AUTHOR') {
        $scope.worklist.authorTime = seconds;
        console.debug('authorTime', $scope.worklist.authorTime);
      } else if ($scope.projectRole == 'REVIEWER') {
        $scope.worklist.reviewerTime = seconds;
        console.debug('reviewerTime', $scope.worklist.reviewerTime);
      }
      // update hours/minutes
      workflowService.updateWorklist($scope.project.id, $scope.worklist).then(

      // Success
      function(data) {
        console.debug('xxx=', data);
        // finish workflow
        $scope.finishWorkflow(worklist);
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    };

    // finish worklist
    $scope.finishWorkflow = function(worklist) {
      var action = 'FINISH';
      if (stamp) {
        action = 'APPROVE';
      }
      workflowService.performWorkflowAction($scope.project.id, worklist.id, $scope.user.userName,
        $scope.projectRole, action).then(
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
      $uibModalInstance.dismiss();
    };

    // end
  } ]);
