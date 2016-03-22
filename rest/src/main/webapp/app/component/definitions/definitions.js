// Content controller
tsApp.directive('definitions', [ 'utilService', function(utilService) {
  console.debug('configure definitions directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '=?'
    },
    templateUrl : 'app/component/definitions/definitions.html',
    link : function(scope, element, attrs) {

      console.debug('definitions', scope.showHidden, scope.component.object.definitions, scope.metadata);

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.object.definitions, scope.paging);
       }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.callbacks = {
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
