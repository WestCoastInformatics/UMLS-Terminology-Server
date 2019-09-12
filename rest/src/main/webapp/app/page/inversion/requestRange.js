// Checklist modal controller
tsApp.controller('SourceIdRangeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'inversionService',
  'selected',
  'lists',
  'user',
  'sab',
  'version',
  'action',
  function($scope, $uibModalInstance, utilService, inversionService, selected, lists, user, sab, version, action) {
    console.debug("configure SourceIdRangeModalCtrl", sab, version, action);

    // Scope vars
    $scope.action = action;
    $scope.sab = sab;
    $scope.version = version;
    $scope.selected = selected;
    $scope.numberOfIds;



    $scope.errors = [];
    $scope.warnings = [];

    $scope.submit = function() {
      if ($scope.action == 'Add') {
        $scope.submitSourceIdRangeRequest();
      } else if ($scope.action == 'Update') {
        $scope.updateSourceIdRangeRequest();
      }
    }
    
    // Submit source id range request
    $scope.submitSourceIdRangeRequest = function() {
      if (!$scope.numberOfIds) {
        window.alert('Requested number of ids must be specified.');
        return;
      }
      inversionService.requestSourceIdRange($scope.selected.project.id, $scope.sab, $scope.version, $scope.numberOfIds).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });      
    }

    // Update source id range
    $scope.updateSourceIdRangeRequest = function() {
      if (!$scope.numberOfIds) {
        window.alert('Requested number of ids must be specified.');
        return;
      }
      inversionService.updateSourceIdRange($scope.selected.project.id, $scope.sab, $scope.version, $scope.numberOfIds).then(
      // Success
      function(data) {
        $uibModalInstance.close(data);
      },
      // Error
      function(data) {
        $scope.errors[0] = data;
        utilService.clearError();
      });      
    }

    $scope.close = function() {
      $uibModalInstance.close(null);
    };

    // end
  } ]);