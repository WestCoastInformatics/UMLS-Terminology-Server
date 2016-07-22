// Content controller
tsApp.directive('relationshipsDeep', [
  'utilService',
  'contentService',
  function(utilService, contentService) {
    console.debug('configure deep relationships directive');
    return {
      restrict : 'A',
      scope : {
        component : '=',
        metadata : '=',
        showHidden : '=',
        callbacks : '='
      },
      templateUrl : 'app/component/relationships-deep/relationshipsDeep.html',
      link : function(scope, element, attrs) {

        // instantiate paging and paging callback function
        scope.pagedData = [];
        scope.paging = utilService.getPaging();
        scope.pageCallback = {
          getPagedList : getPagedList
        };

        scope.paging.sortField = 'group';
        scope.paging.sortAscending = true;

        // Default is Group/Type, where in getpagedData
        // relationshipType is automatically appended as a multi-
        // sort search
        scope.paging.sortOptions = [ {
          key : 'Group, Type',
          value : 'group'
        }, {
          key : 'Type',
          value : 'relationshipType'
        }, {
          key : 'Additional Type',
          value : 'additionalRelationshipType'
        }, {
          key : 'Name',
          value : 'to.name'
        } ];

        function getPagedList() {

          // compute the sort order
          // always sort intelligently around relationship type and additional
          // relationship type
          var sortFields = new Array();
          if (scope.paging.sortField === 'group') {
            sortFields = [ 'group', 'relationshipType', 'additionalRelationshipType' ];
          } else if (scope.paging.sortField === 'relationshipType') {
            sortFields = [ 'relationshipType', 'additionalRelationshipType', 'group' ];
          } else if (scope.paging.sortFied === 'additionalRelationshipType') {
            sortFields = [ 'additionalRelationshipType', 'relationshipType', 'group' ];
          } else {
            sortFields = [ scope.paging.sortField, 'group', 'relationshipType',
              'additionalRelationshipType' ];
          }

          var parameters = {
            showSuppressible : scope.showHidden,
            showObsolete : scope.showHidden,
            text : scope.paging.filter,
            sortFields : sortFields,
            sortAscending : scope.paging.sortAscending
          };
          
          var wrapper = {
            id : scope.component.id,
            type : scope.metadata.terminology.organizingClassType,
            terminology : scope.component.terminology,
            version : scope.component.version,
            terminologyId: scope.component.terminologyId
          };

          // Request from service
          contentService.findDeepRelationships(wrapper, scope.paging.page,
            parameters).then(function(data) {

            scope.pagedData.data = data.relationships;
            scope.pagedData.totalCount = data.totalCount;

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
