// Content controller
tsApp.directive('semanticTypes', [
  'utilService',
  function(utilService) {
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
          scope.pagedData = utilService.getPagedArray(scope.component.object.semanticTypes,
            scope.paging);
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
