// Content controller
tsApp.directive('relationshipsDeep', [
  'utilService', 'contentService',
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
        }

        scope.paging.sortField = 'group';
        scope.paging.sortAscending = true;
        scope.paging.showInferred = true;

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
          var sortFields = [];
          if (scope.paging.sortField === 'group') {
            sortFields = [ 'group', 'relationshipType' ]
          } else {
            sortFields = [ scope.paging.sortField, 'group' ];
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
          contentService.findDeepRelationships(scope.component.object.terminologyId,
            scope.component.object.terminology, scope.component.object.version,
            scope.paging.page, parameters).then(function(data) {

            scope.pagedData.data = data.relationships;
            scope.pagedData.totalCount = data.totalCount;

          });
        }
        ;

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
