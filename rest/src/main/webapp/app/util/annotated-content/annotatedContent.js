// Content controller
tsApp.directive('annotatedContent', [
  '$rootScope',
  'utilService',
  'contentService',
  'securityService',
  '$uibModal',
  function($rootScope, utilService, contentService, securityService, $uibModal) {
    console.debug('configure note components directive');
    return {
      restrict : 'A',
      scope : {
        metadata : '=',
        callbacks : '='
      },
      templateUrl : 'app/util/annotated-content/annotatedContent.html',
      link : function(scope, element, attrs) {

        console.debug('entered note components directive');

        // instantiate paging and paging callback function
        scope.pagedData = [];
        scope.paging = utilService.getPaging();
        console.debug(scope.paging);
        scope.pageCallback = {
          getPagedList : getPagedList
        };

        scope.paging.sortField = 'name';
        scope.paging.sortAscending = true;

        // Default is Group/Type, where in getpagedData
        // relationshipType is automatically appended as a multi-
        // sort search
        scope.paging.sortOptions = [ {
          key : 'Name',
          value : 'name'
        }, {
          key : 'Type',
          value : 'type'
        }, {
          key : 'Terminology Id',
          value : 'terminologyId'
        }, {
          key : 'Terminology',
          value : 'terminology'
        } ];

        function getPagedList() {

          console.debug('get components with notes', scope.paging);

          // Request from service
          contentService.getComponentsWithNotesForUser(scope.paging.filter, scope.paging).then(
            function(response) {
              scope.pagedData = response;

            });
        }
        getPagedList();

        scope.openFavorite = function(favorite) {
          scope.callbacks.getComponent(favorite);
        }

        scope.removeFavorite = function(favorite) {
          securityService.removeUserFavorite(favorite.type, favorite.terminology, favorite.version,
            favorite.terminologyId, favorite.value).then(function(response) {
            getPagedList();
            scope.callbacks.checkFavoriteStatus();

          });
        }

        // watch for broadcast favorite update notification
        scope.$on('termServer::noteChange', function(event, data) {

          console.debug('annotatedContent: received noteChange', event, data, scope.pagedData);

          // check if referenced component is in list
          if (data && data.component) {
            for (var i = 0; i < scope.pagedData.results.length; i++) {
              console.debug(' comparing ' + scope.pagedData.results[i].id + ' to '
                + data.component.id)
              if (scope.pagedData.results[i].id === data.component.id) {
                console.debug('  component in viewed list, refreshing');
                getPagedList();
                break;
              }
            }
          } else {
            console.debug(' no data received"0;')
          }
        });

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

          }, function() {
            // do nothing
          });
        };

      }
    };
  } ]);
