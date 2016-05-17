// Content controller
tsApp.directive('favorites', [
  'utilService',
  'contentService',
  'securityService',
  '$uibModal',
  function(utilService, contentService, securityService, $uibModal) {
    console.debug('configure favorites directive');
    return {
      restrict : 'A',
      scope : {
        metadata : '=',
        favorites : '=',
        callbacks : '='
      },
      templateUrl : 'app/util/favorites/favorites.html',
      link : function(scope, element, attrs) {

        // instantiate paging and paging callback function
        scope.pagedData = [];
        scope.paging = utilService.getPaging();
        scope.pageCallback = {
          getPagedList : getPagedList
        };

        scope.paging.sortField = 'name';
        scope.paging.sortAscending = true;
        scope.paging.showInferred = false;

        // Default is Group/Type, where in getpagedData
        // relationshipType is automatically appended as a multi-
        // sort search
        scope.paging.sortOptions = [ {
          key : 'Name',
          value : 'name'
        }, {
          key : 'Id',
          value : 'terminologyId'
        }, {
          key : 'Date Added',
          value : 'lastModified'
        } ];

        function getPagedList() {

          console.debug('Favorites: get paged list', scope.paging, scope.favorites);
          
          // do not make call unless metadata exists
          if (!scope.metadata || !scope.metadata.terminology) {
            return;
          }

          // Request from service
          contentService.getUserFavorites(scope.metadata.terminology.terminology,
            scope.metadata.terminology.version, scope.paging).then(function(response) {
            scope.pagedData = response;

          });
        }
        // watch the component
        scope.$watch('metadata', function() {   
            getPagedList();
        }, true);
        
        // watch the favorites
        scope.$watch('favorites', function() {
          getPagedList();
        }, true)

        scope.openFavorite = function(favorite) {
          scope.callbacks.getComponent(favorite.terminologyId, favorite.terminology,
            favorite.version);
        }

        scope.removeFavorite = function(favorite) {
          console.debug(scope.callbacks);
          securityService.removeUserFavorite(favorite.type, favorite.terminology, favorite.version,
            favorite.terminologyId, favorite.value).then(function(response) {
            getPagedList();
            scope.callbacks.checkFavoriteStatus();

          });
        }


      }
    };
  } ]);
