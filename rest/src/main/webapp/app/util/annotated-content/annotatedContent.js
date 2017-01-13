// Notes
tsApp.directive('annotatedContent', [
  '$rootScope',
  'utilService',
  'contentService',
  'securityService',
  '$uibModal',
  function($rootScope, utilService, contentService, securityService, $uibModal) {
    return {
      restrict : 'A',
      scope : {
        // NOTE: metadata used for non-matching terminology display in html only
        metadata : '=',
        component : '=',
        callbacks : '='
      },
      templateUrl : 'app/util/annotated-content/annotatedContent.html',
      link : function(scope, element, attrs) {

        // TODO Check into case sensitivity of filter for terminology ids

        console.debug('entered note components directive');

        // instantiate paging and paging callbacks function
        scope.pagedData = [];
        $scope.pageSizes = utilService.getPageSizes();
        scope.paging = utilService.getPaging();
        console.debug(scope.paging);
        scope.pageCallbacks = {
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
          key : 'Terminology',
          value : 'terminology'
        }, {
          key : 'Terminology Id',
          value : 'terminologyId'
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

        scope.openAnnotatedContent = function(content) {
          scope.callbacks.getComponent(content);
        };

        scope.removeAnnotatedContent = function(content) {
          securityService.removeUserAnnotatedContent(content.type, content.terminology,
            content.version, content.terminologyId, content.value).then(function(response) {
            getPagedList();
            scope.callbacks.checkAnnotatedContentStatus();

          });
        };

        // watch for broadcast content update notification
        scope.$on('termServer::noteChange', function(event, data) {

          console.debug('annotatedContent: received noteChange', event, data, scope.pagedData);
          getPagedList();
        });

        // Open notes modal, from either wrapper or component
        scope.viewNotes = function(content) {

          var modalInstance = $uibModal.open({
            animation : scope.animationsEnabled,
            templateUrl : 'app/util/component-note-modal/componentNoteModal.html',
            controller : 'componentNoteModalCtrl',
            scope : $rootScope,
            size : 'lg',
            resolve : {
              component : function() {
                return content;

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
