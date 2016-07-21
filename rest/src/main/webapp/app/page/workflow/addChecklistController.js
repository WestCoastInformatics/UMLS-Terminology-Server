    // Create checklist controller
    var CreateChecklistModalCtrl = function($scope, $uibModalInstance, workflowService, clusterType, projectId, bin) {

      $scope.bin = bin;
      $scope.clusterType = clusterType

      $scope.errors = [];


      // Create the checklist
      $scope.createChecklist = function(checklist) {
        if (!checklist || !checklist.name) {
          window.alert('The name field cannot be blank. ');
          return;
        }

        // Create checklist 
        workflowService.createChecklist(projectId, bin.id, $scope.clusterType, checklist.name, 
          checklist.randomize, checklist.excludeOnWorklist, checklist.query == undefined ? 
            "" : checklist.query, checklist.pfs).then(
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