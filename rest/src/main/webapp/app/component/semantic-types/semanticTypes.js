// Semantic types
tsApp.directive('semanticTypes', [ 'utilService', function(utilService) {
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
    link : function(scope, element, attrs) {

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.semanticTypes, scope.paging);
      }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallback = {
        getPagedList : getPagedList
      };

      // watch the component
      scope.$watch('component', function() {
        if (scope.component) {
          // Clear paging
          scope.paging = utilService.getPaging();
          scope.pageCallback = {
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
