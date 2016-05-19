// Content controller
tsApp.directive('subsets', [ 'utilService', function(utilService) {
  console.debug('configure subsets directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/subsets/subsets.html',
    link : function(scope, element, attrs) {

      function getPagedList() {
        
        scope.pagedData = utilService.getPagedArray(scope.component.members, scope.paging);
        console.debug('subsets', scope.pagedData);
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
