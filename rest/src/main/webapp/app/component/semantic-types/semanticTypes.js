// Semantic types
tsApp.directive('semanticTypes', [ function() {
  console.debug('configure semanticTypes directive');
  return {
    restrict : 'A',
    scope : {
      selected : '=',
      lists : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/semantic-types/semanticTypes.html',
    controller : [
      '$scope',
      '$uibModal',
      'utilService',
      'contentService',
      'editService',
      'appConfig',
      function($scope, $uibModal, utilService, contentService, editService, appConfig) {

        function getPagedList() {
          $scope.pagedData = utilService.getPagedArray($scope.selected.component.semanticTypes,
            $scope.paging);
        }

        $scope.showing = true;

        // instantiate paging and paging callbacks function
        $scope.pagedData = [];
        $scope.pageSizes = utilService.getPageSizes();
        $scope.paging = utilService.getPaging();
        $scope.pageCallbacks = {
          getPagedList : getPagedList
        };

        // watch the component
        $scope.$watch('selected.component', function() {
          if ($scope.selected.component) {
            // Clear paging
            $scope.paging = utilService.getPaging();
            $scope.pageCallbacks = {
              getPagedList : getPagedList
            };
            // Get data
            getPagedList();
          }
        }, true);

        // watch show hidden flag
        $scope.$watch('showHidden', function(newValue, oldValue) {
          $scope.paging.showHidden = $scope.showHidden;

          // if value changed, get paged list
          if (newValue != oldValue) {
            getPagedList();
          }
        });

        //
        // MODALS
        //
        // Add atom modal
        $scope.openAddSemanticTypesModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/content/addSemanticType.html',
            backdrop : 'static',
            controller : 'SimpleSemanticTypeModalCtrl',
            resolve : {
           
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
            $scope.callbacks.getComponent($scope.selected.component);
          });
        };
        
        $scope.removeSemanticType = function(sty) {
          editService.removeSemanticType($scope.selected.project.id, $scope.selected.component.id, sty.id).then(function() {
            $scope.callbacks.getComponent($scope.selected.component);
          })
        }
       

        // end controller
      } ]
  };
} ]);
