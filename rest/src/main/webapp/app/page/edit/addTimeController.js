// Add time controller
var AddTimeModalCtrl = function($scope, $uibModalInstance, contentService, reportService,
  workflowService, project, worklist, projectRole) {
  console.debug('Entered add time modal control');
  $scope.worklist = worklist;
  $scope.project = project;
  $scope.projectRole = projectRole;
  $scope.hours;
  $scope.minutes;
  
  $scope.errors = [];

  $scope.addTime = function() {
    var seconds = (($scope.hours * 60 * 60) + ($scope.minutes * 60));
    if ($scope.projectRole == 'AUTHOR') {
      $scope.worklist.authorTime = seconds;
    } else if ($scope.projectRole == 'REVIEWER') {
      $scope.worklist.reviewerTime = seconds;
    }
    workflowService.updateWorklist($scope.project.id, $scope.worklist).then(
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



  // Dismiss modal
  $scope.cancel = function() {
    $uibModalInstance.close();
  };

};
