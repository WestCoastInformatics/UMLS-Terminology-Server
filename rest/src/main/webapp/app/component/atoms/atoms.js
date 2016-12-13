// Atoms directive
tsApp.directive('atoms', [ 'utilService', 'contentService', function(utilService, contentService) {
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/atoms/atoms.html',
    link : function(scope, element, attrs) {

      scope.expanded = {};
      scope.showing = true;
      scope.getPagedList = function() {
        getPagedList();
      }

      // Paging function
      function getPagedList() {
        scope.pagedData = utilService.getPagedArray(scope.selected.component.atoms.filter(
        // handle hidden flag
        function(item) {
          return scope.paging.showHidden || (!item.obsolete && !item.suppressible);
        }), scope.paging);

        console.debug('paged atoms', scope.pagedData);
      }

      // instantiate paging and paging callbacks function
      scope.pagedData = [];
      scope.paging = utilService.getPaging();
      scope.pageCallbacks = {
        getPagedList : getPagedList
      };

      // watch the component
      scope.$watch('component', function() {
        if (scope.selected.component) {
          // reset paging
          // commented out - interferes with Show All/Show Paged
          // scope.paging = utilService.getPaging();
          scope.pageCallbacks = {
            getPagedList : getPagedList
          };
          // get data
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

      // toggle an items collapsed state
      scope.toggleItemCollapse = function(item) {
        scope.expanded[item.id] = !scope.expanded[item.id];
      };

      // get the collapsed state icon
      scope.getCollapseIcon = function(item) {

        // if no expandable content detected, return blank glyphicon
        // (see
        // tsApp.css)
        if (!contentService.atomHasContent(item))
          return 'glyphicon glyphicon-plus glyphicon-plus nocontent';

        // return plus/minus based on current expanded status
        if (scope.expanded[item.id])
          return 'noul glyphicon glyphicon-minus';
        else
          return 'noul glyphicon glyphicon-plus';
      };

      // Determine if the atom has a prject"new atom termgroup"
      scope.isNewAtomTermgroup = function(atom) {
        console.debug('xxx', atom, scope.selected);
        if (!scope.selected.project) {
          return false;
        }
        for (var i = 0; i < scope.selected.project.newAtomTermgroups.length; i++) {
          var tg = scope.selected.project.newAtomTermgroups[i];
          if (atom.terminology + '/' + atom.termType == tg) {
            return true;
          }
        }
        return false;
      }

      // Edit atom modal
      $scope.openEditAtomModal = function(latom) {

        var modalInstance = $uibModal.open({
          templateUrl : 'app/page/edit/atoms/editAtom.html',
          backdrop : 'static',
          controller : 'AtomModalCtrl',
          resolve : {
            atom : function() {
              return latom;
            },
            action : function() {
              return 'Edit';
            },
            selected : function() {
              return $scope.selected;
            },
            lists : function() {
              return $scope.lists;
            }
          }
        });

        modalInstance.result.then(
        // Success
        function(user) {
          $scope.getPagedAtoms();
        });
      };

      // end
    }
  };
} ]);
