// Semantic types
tsApp.directive('semanticTypes', [ function() {
  console.debug('configure semanticTypes directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/semantic-types/semanticTypes.html',
    controller : [
      '$scope',
      'utilService',
      function($scope, utilService) {

        function getPagedList() {
          $scope.pagedData = utilService.getPagedArray($scope.component.semanticTypes,
            $scope.paging);
        }

        $scope.showing = true;

        // instantiate paging and paging callbacks function
        $scope.pagedData = [];
        $scope.paging = utilService.getPaging();
        $scope.pageCallbacks = {
          getPagedList : getPagedList
        };

        // watch the component
        $scope.$watch('component', function() {
          if ($scope.component) {
            // Clear paging
            $scope.paging = utilService.getPaging();
            $scope.pageCallbacks = {
              getPagedList : getPagedList
            };
            // Get data
            getPagedList();
          }
        }, true);

        // watch show hidden flag
        $scope.$watch('showHidden', function(newValue, oldValue) {
          $scope.paging.showHidden = $scope.showHidden;

          // if value changed, get paged list
          if (newValue != oldValue) {
            getPagedList();
          }
        });

        // end controller
      } ]
  };
} ]);
