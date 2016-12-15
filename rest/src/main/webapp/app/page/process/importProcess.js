// Import modal controller
tsApp.controller('ImportProcessModalCtrl', [ '$scope', '$uibModalInstance', 'utilService',
  'processService', 'selected', 'lists',
  function($scope, $uibModalInstance, utilService, processService, selected, lists) {
    console.debug('Entered import process modal control', selected, lists);

    $scope.selected = selected;
    $scope.lists = lists;
    $scope.comments = [];
    $scope.warnings = [];
    $scope.errors = [];

    // Handle import
    $scope.importProcess = function(file) {
      $scope.errors = [];
      processService.importProcess($scope.selected.project.id, file).then(
      // Success - close dialog
      function(data) {
        $uibModalInstance.close(data);
      },
      // Failure - show error
      function(data) {
        handleError($scope.errors, data);
      });
    }

    // Dismiss modal
    $scope.cancel = function() {
      // dismiss the dialog
      $uibModalInstance.dismiss('cancel');
    };

    $scope.close = function() {
      // close the dialog and reload refsets
      $uibModalInstance.close();
    };

    // end

  } ]);
