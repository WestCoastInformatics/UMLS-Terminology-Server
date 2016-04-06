// Content controller
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

      console.debug('labels', scope.showHidden, scope.component.object.labels, scope.metadata);

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.object.labels, scope.paging);
       }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallback = {
        getPagedList : getPagedList
      }

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
