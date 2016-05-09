// Content controller
tsApp.directive('relationships', [
  'utilService',
  'contentService',
  function(utilService, contentService) {
    console.debug('configure relationships directive');
    return {
      restrict : 'A',
      scope : {
        component : '=',
        metadata : '=',
        showHidden : '=',
        callbacks : '='
      },
      templateUrl : 'app/component/relationships/relationships.html',
      link : function(scope, element, attrs) {

        // instantiate paging and paging callback function
        scope.pagedData = [];
        scope.paging = utilService.getPaging();
        scope.pageCallback = {
          getPagedList : getPagedList
        };

        scope.paging.sortField = 'group';
        scope.paging.sortAscending = true;
        scope.paging.showInferred = false;

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
          value : 'toName'
        } ];

        function getPagedList() {

          // compute the sort order
          // if group sort specified, sort additionally by relationship type
          // otherwise, sort by specified field and additionally by group
          // compute the sort order
          // always sort intelligently around relationship type and additional
          // relationship type
          var sortFields = new Array();
          if (scope.paging.sortField === 'group') {
            sortFields = [ 'group', 'relationshipType', 'additionalRelationshipType', 'toName' ];
          } else if (scope.paging.sortField === 'relationshipType') {
            sortFields = [ 'relationshipType', 'additionalRelationshipType', 'group', 'toName' ];
          } else if (scope.paging.sortFied === 'additionalRelationshipType') {
            sortFields = [ 'additionalRelationshipType', 'relationshipType', 'group', 'toName' ];
          } else {
            sortFields = [ scope.paging.sortField, 'group', 'relationshipType',
              'additionalRelationshipType', 'toName' ];
          }

          var parameters = {
            showSuppressible : scope.showHidden,
            showObsolete : scope.showHidden,
            showInferred : scope.paging.showInferred,
            text : scope.paging.filter,
            sortFields : sortFields,
            sortAscending : scope.paging.sortAscending
          };

          // Request from service
          contentService.findRelationships(scope.component.object.terminologyId,
            scope.component.object.terminology, scope.component.object.version, scope.paging.page,
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
        
        // Function to toggle inferred flag and apply paging
        scope.toggleInferred = function() {
          if (scope.paging.showInferred == null || scope.paging.showInferred == undefined) {
            scope.paging.showInferred = false;
          } else {
            scope.paging.showInferred = !scope.paging.showInferred;
          }
          getPagedList();
        };

      }
    };
  } ]);
