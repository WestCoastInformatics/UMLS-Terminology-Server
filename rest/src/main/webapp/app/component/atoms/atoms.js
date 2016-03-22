// Content controller
tsApp.directive('atoms', [ 'utilService', function(utilService) {
  console.debug('configure atoms directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '=?'
    },
    templateUrl : 'app/component/atoms/atoms.html',
    link : function(scope, element, attrs) {

      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.component.object.atoms, scope.paging);
        console.debug('ATOMS DATA', scope.paging, scope.pagedData);
      }

      // instantiate paging and paging callback function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallback = {
        getPagedList : getPagedList
      };

      console.debug('atom callbacks', scope.callbacks);

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

      // toggle an items collapsed state
      scope.toggleItemCollapse = function(item) {
        item.expanded = !item.expanded;
      };

      // get the collapsed state icon
      scope.getCollapseIcon = function(item) {

        // if no expandable content detected, return blank glyphicon
        // (see
        // tsApp.css)
        if (!item.hasContent)
          return 'glyphicon glyphicon-plus glyphicon-none';

        // return plus/minus based on current expanded status
        if (item.expanded)
          return 'glyphicon glyphicon-minus';
        else
          return 'glyphicon glyphicon-plus';
      };

    }
  };
} ]);
