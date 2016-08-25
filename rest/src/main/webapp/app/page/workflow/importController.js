// Checklist modal controller
tsApp.controller('ImportExportModalCtrl', [
  '$scope',
  '$uibModalInstance',
  'utilService',
  'workflowService',
  'selected',
  'lists',
  'user',
  function($scope, $uibModalInstance, utilService, workflowService, selected, lists, user) {
    console.debug('Entered import export modal control', worklist, operation, type);

    // Scope vars
    $scope.selected = selected;
    $scope.lists = lists;
    $scope.users = users;
    $scope.warnings = [];
    $scope.errors = [];
    $scope.checklistName = null;
    $scope.query = null;
    $scope.queryType = null;

    // Test query
    $scope.testQuery = function(binDefinition) {
      $scope.errors = [];
      $scope.messages = [];
      workflowService.testQuery($scope.project.id, binDefinition.query, binDefinition.queryType)
        .then(
        // success
        function(data) {
          $scope.messages.push("Query met validation requirements.");
        },
        // Error
        function(data) {
          utilService.handleDialogError(errors, data);
        });
    }

    // Handle import
    $scope.import = function(file) {

      workflowService.importChecklist($scope.selected.project.id, $scope.checklistName, file).then(
      // Success - close dialog
      function(data) {
        $uibModalInstance.close(data);
      },
      // Failure - show error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // Dismiss modal
    $scope.cancel = function() {
      // dismiss the dialog
      $uibModalInstance.dismiss('cancel');
    };

    // end
  } ]);