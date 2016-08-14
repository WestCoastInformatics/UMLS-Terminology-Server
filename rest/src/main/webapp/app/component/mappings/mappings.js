// Mappings
tsApp.directive('mappings', [
  'utilService',
  'contentService',
  function(utilService, contentService) {
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
      link : function(scope, element, attrs) {

        // instantiate paging and paging callback function
        scope.pagedData = [];
        scope.paging = utilService.getPaging();
        scope.pageCallback = {
          getPagedList : getPagedList
        };

        function getPagedList() {

          var parameters = {
            showSuppressible : scope.showHidden,
            showObsolete : scope.showHidden,
            showInferred : scope.paging.showInferred,
            text : scope.paging.filter,
            sortFields : null,
            sortAscending : true
          };

          // Request from service
          contentService.findMappings(scope.component.terminologyId,
            scope.component.terminology, scope.component.version, scope.paging.page,
            parameters).then(function(data) {

            scope.pagedMappings = data.mapping;
            scope.pagedMappings.totalCount = data.totalCount;

          });
        }

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
