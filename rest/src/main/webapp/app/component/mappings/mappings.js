// Mappings
tsApp.directive('mappings', [ function() {
  console.debug('configure mappingss directive');
  return {
    restrict : 'A',
    scope : {
      component : '=',
      metadata : '=',
      showHidden : '=',
      callbacks : '='
    },
    templateUrl : 'app/component/mappings/mappings.html',
    controller : [
      '$scope',
      'utilService',
      'contentService',
      function($scope, utilService, contentService) {

        // Paging vars
        $scope.pagedData = [];
        $scope.paging = utilService.getPaging();
        $scope.pageCallbacks = {
          getPagedList : getPagedList
        };

        // watch the component
        $scope.$watch('component', function() {
          if ($scope.component) {
            // Clear paging
            $scope.paging = utilService.getPaging();
            $scope.pageCallbacks = {
              getPagedList : getPagedList
            };
            // Get data
            getPagedList();
          }
        }, true);

        // Get paged data
        function getPagedList() {

          var paging = $$scope.paging;
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          // Request from service
          contentService.findMappings($scope.component.type, $scope.component.terminologyId,
            $scope.component.terminology, $scope.component.version, pfs).then(
          // Success
          function(data) {
            $scope.pagedMappings = data.mappings;
            $scope.pagedMappings.totalCount = data.totalCount;
          });
        }

        // end controller
      } ]
  };
} ]);
