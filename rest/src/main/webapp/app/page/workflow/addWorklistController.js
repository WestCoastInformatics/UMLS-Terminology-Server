    // Create worklist controller
    var CreateWorklistModalCtrl = function($scope, $uibModalInstance, workflowService, utilService, 
      user, projectId, binId, clusterType) {

      $scope.worklist = {};

      $scope.user = user;
      $scope.errors = [];


      // Create the worklist
      $scope.createWorklist = function(clusterCt, skipClusterCt) {
        
        var pfs = {
          maxResults : clusterCt,
          startIndex : skipClusterCt
        };
        // Create worklist 
        workflowService.createWorklist(projectId, binId, clusterType,
          pfs).then(
          // Success
          function(data) {
            $uibModalInstance.close();
            workflowService.fireWorklistChanged(data);
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