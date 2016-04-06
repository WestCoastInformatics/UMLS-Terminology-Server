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
        scope.pagedData = utilService.getPagedArray(scope.component.object.subsets, scope.paging);
      }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallback = {
        getPagedList : getPagedList
      };

      // watch show hidden flag
      scope.$watch('showHidden', function() {
        if (scope.showHidden != undefined && scope.showHidden != null) {
          scope.paging.showHidden = scope.showHidden;
        } else {
          scope.paging.showHidden = false;
        }
        getPagedList();
      });

    }
  };
} ]);
