// Actions/Errors controller
tsApp.controller('ActionErrorsCtrl', [ '$scope', '$uibModalInstance', 'utilService', 'errors',
  'warnings', 'action', 'concept',
  function($scope, $uibModalInstance, utilService, errors, warnings, action, concept) {

    // Scope variables
    $scope.action = action;
    $scope.concept = concept;
    $scope.warnings = warnings;
    $scope.errors = errors;

    // Override warnings
    $scope.overrideWarnings = function() {
      $uibModalInstance.close(true);
    }

    // Dismiss modal
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

  } ]);