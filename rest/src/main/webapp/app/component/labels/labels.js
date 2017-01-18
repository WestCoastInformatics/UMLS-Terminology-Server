// Labels
tsApp.directive('labels', [ 'utilService', function(utilService) {
  console.debug('configure labels directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/labels/labels.html',
    link : function(scope, element, attrs) {

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.labels.filter(
        // handle hidden flag
        function(item) {
          return scope.paging.showHidden || (!item.obsolete && !item.suppressible);
        }), scope.paging);
      }

      // instantiate paging and paging callbacks function
      scope.pagedData = [];
      $scope.pageSizes = utilService.getPageSizes();
      scope.paging = utilService.getPaging();
      scope.pageCallbacks = {
        getPagedList : getPagedList
      };

      // watch the component
      scope.$watch('component', function() {
        if (scope.component) {
          // Clear paging
          scope.paging = utilService.getPaging();
          scope.pageCallbacks = {
            getPagedList : getPagedList
          };
          // Get data
          getPagedList();
        }
      }, true);

      // watch show hidden flag
      scope.$watch('showHidden', function(newValue, oldValue) {
        scope.paging.showHidden = scope.showHidden;

        // if value changed, get paged list
        if (newValue != oldValue) {
          getPagedList();
        }
      });

    }
  };
} ]);
