// Content controller
tsApp.directive('favorites', [
  '$rootScope',
  'utilService',
  'contentService',
  'securityService',
  '$uibModal',
  '$timeout',
  function($rootScope, utilService, contentService, securityService, $uibModal, $timeout) {
    return {
      restrict : 'A',
      scope : {
        // NOTE: metadata used for non-matching terminology display in html only
        metadata : '=',
        component : '=',
        favorites : '=',
        callbacks : '='
      },
      templateUrl : 'app/util/favorites/favorites.html',
      link : function(scope, element, attrs) {

        // instantiate paging and paging callbacks function
        scope.pagedData = null;
        scope.pageSizes = utilService.getPageSizes();
        scope.paging = utilService.getPaging();
        scope.pageCallbacks = {
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
        },

        {
          key : 'Terminology',
          value : 'terminology'
        }, {
          key : 'Terminology Id',
          value : 'terminologyId'
        }, {
          key : 'Timestamp',
          value : 'timestamp'
        } ];

        function getPagedList() {

          // Request from service
          contentService.getUserFavorites(scope.paging).then(
          // Success
          function(response) {
            scope.pagedData = response;

          });
        }

        // watch the favorites for first-load initialization
        scope.$watch('favorites', function() {
          if (scope.pagedData == null) {
            getPagedList();
          }
        }, true);

        // watch for favorite change notifications
        // TODO This can result in duplicate calls where top-level favorites
        // change. Resolve this once websocket functionality is complete.
        scope.$on('termServer::favoriteChange', function(event, data) {
          getPagedList();
        });

        // watch for broadcast favorite update notification
        scope.$on('termServer::noteChange', function(event, data) {

          console.debug('favorites: received noteChange', event, data, scope.pagedData);

          // check if referenced component is in list
          if (data && data.component) {
            for (var i = 0; i < scope.pagedData.results.length; i++) {
              console.debug(' comparing ' + scope.pagedData.results[i].id + ' to '
                + data.component.id);
              if (scope.pagedData.results[i].id === data.component.id) {
                console.debug('  component in viewed list, refreshing');
                getPagedList();
                break;
              }
            }
          } else {
            console.debug(' no data received"0;');
          }
        });

        scope.openFavorite = function(favorite) {
          scope.callbacks.getComponent(favorite);
        };

        scope.removeFavorite = function(favorite) {
        	console.debug('remove favorite', favorite);
          securityService.removeUserFavorite(favorite).then(
          // Success
          function(response) {
            getPagedList();
            scope.callbacks.checkFavoriteStatus();
            websocketService.fireFavoriteChange();
          });
        };

        // Open notes modal, from either wrapper or component
        scope.viewNotes = function(favorite) {

          var modalInstance = $uibModal.open({
            animation : scope.animationsEnabled,
            templateUrl : 'app/util/component-note-modal/componentNoteModal.html',
            controller : 'componentNoteModalCtrl',
            scope : $rootScope,
            size : 'lg',
            resolve : {
              component : function() {
                return favorite;

              }
            }
          });

          modalInstance.result.then(function() {
            // do nothing
          }, function() {
            // do nothing
          });
        };

      }
    };
  } ]);
