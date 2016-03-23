// Content controller
tsApp.directive('attributes', [ 'utilService', function(utilService) {
  console.debug('configure attributes directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '=',
      testCallback: '&?'
     
    },
    templateUrl : 'app/component/attributes/attributes.html',
    link : function(scope, element, attrs) {

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.object.attributes, scope.paging);
      }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallback = {
        getPagedList : getPagedList
      };
      

      console.debug('attribute callbacks', scope.testCallback, scope.callbacks);

      // watch show hidden flag
      scope.$watch('showHidden', function() {
        console.debug('showHidden changed', scope.showHidden)
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
