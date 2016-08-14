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
      paging : '='
    },
    templateUrl : 'app/component/records/recordsTable.html',
    controller : [ '$scope', function($scope) {

      // Clear component on record list change
      $scope.$watch('lists.records', function() {
        $scope.selected.concept = null;
      });

      // Selects a concept (setting $scope.selected.concept)
      $scope.selectConcept = function(concept) {
        // Set the concept for display
        $scope.selected.concept = concept;
      };

      // end

    } ]
  };
} ]);
