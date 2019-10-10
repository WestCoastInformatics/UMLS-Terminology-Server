// Checklist modal controller
tsApp.controller('SourceIdRangeModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'inversionService',
  'selected',
  'lists',
  'user',
  'vsab',
  'action',
  function($scope, $uibModalInstance, utilService, inversionService, selected, lists, user, vsab,
    action) {
    console.debug("configure SourceIdRangeModalCtrl", vsab, action);

    // Scope vars
    $scope.action = action;
    $scope.vsab = vsab;
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

    $scope.snomedCase = function() {
      if ($scope.vsab.indexOf("SNOMED") != -1) {
        return true;
      } else {
        return false;
      }
    }

    // Submit source id range request
    $scope.submitSourceIdRangeRequest = function() {
      if (!$scope.numberOfIds) {
        window.alert('Requested number of ids must be specified.');
        return;
      }
      inversionService.requestSourceIdRange($scope.selected.project.id, $scope.vsab,
        $scope.numberOfIds, $scope.beginSourceId).then(
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
      inversionService.updateSourceIdRange($scope.selected.project.id, $scope.vsab,
        $scope.numberOfIds, $scope.beginSourceId).then(
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