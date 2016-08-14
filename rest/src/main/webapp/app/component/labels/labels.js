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

      console.debug('labels', scope.showHidden, scope.component.labels, scope.metadata);

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.labels, scope.paging);
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
