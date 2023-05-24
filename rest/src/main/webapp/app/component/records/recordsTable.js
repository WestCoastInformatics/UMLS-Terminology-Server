// Records table
// e.g. <div records-table selected="..." lists="..." user="user" paging="paging" />
tsApp.directive('recordsTable', [ function() {
  console.debug('configure recordsTable directive');
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      user : '=',
      paging : '=',
      type : '='
    },
    templateUrl : 'app/component/records/recordsTable.html',
    controller : [ '$scope', 'utilService', 'metadataService', 'contentService',
      function($scope, utilService, metadataService, contentService) {
        // Clear component on record list change
        $scope.$watch('lists.records', function() {
          $scope.selected.component = null;
        });

        // Callbacks for report
        $scope.callbacks = {};
        utilService.extendCallbacks($scope.callbacks, metadataService.getCallbacks());
        utilService.extendCallbacks($scope.callbacks, contentService.getCallbacks());

        // Selects a concept (setting $scope.selected.component)
        $scope.selectConcept = function(component) {

          contentService.getConcept(component.id, $scope.selected.project.id).then(
          // Success
          function(data) {
            // Set the component for display
            $scope.selected.component = data;

          });
        };

        // end

      } ]
  };
} ]);
