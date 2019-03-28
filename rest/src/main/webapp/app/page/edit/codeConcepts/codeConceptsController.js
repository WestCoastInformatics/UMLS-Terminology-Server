// Semantic types controller

tsApp
  .controller(
    'CodeConceptsCtrl',
    [
      '$scope',
      '$window',
      '$q',
      '$uibModal',
      'tabService',
      'securityService',
      'utilService',
      'metadataService',
      'contentService',
      'metaEditingService',
      'websocketService',
      function($scope, $window, $q, $uibModal, tabService, securityService, utilService,
        metadataService, contentService, metaEditingService, websocketService) {

        console.debug("configure CodeConceptsCtrl");

        // remove tabs, header and footer
        tabService.setShowing(false);
        utilService.setHeaderFooterShowing(false);

        // preserve parent scope reference
        $scope.parentWindowScope = window.opener.$windowScope;
        $scope.parentClosing = false;
        window.$windowScope = $scope;
        $scope.user = $scope.parentWindowScope.user;
        $scope.selected = $scope.parentWindowScope.selected;
        $scope.lists = $scope.parentWindowScope.lists;


        
        // add concept to concept list via the '+' icon
        $scope.addComponent = function(data) {
          for (var i = 0; i < $scope.lists.concepts.length; i++) {
            if ($scope.lists.concepts[i].id == data.id) {
              window.alert('Concept ' + data.id + ' is already on the concept list.');
              return;
            }
          }
          // get full concept
          contentService.getConcept(data.id, $scope.selected.project.id).then(
          // Success
          function(data) {
            $scope.lists.concepts.push(data);
          });
        }
        
        // Reload concept
        $scope.reloadConcept = function() {
          utilService.clearError();
          $scope.parentWindowScope.reloadConcept($scope.selected.component);
        }

        // refresh
        $scope.refresh = function() {
          console.debug("refresh");
          $scope.$apply();
        }
        
        // notify edit controller when semantic type window closes
        $window.onbeforeunload = function(evt) {
          if (!$scope.parentClosing) {
            $scope.parentWindowScope.removeWindow('codeConcepts');
          }
        }
        $scope.$on('$destroy', function() {
          if (!$scope.parentClosing) {
            $scope.parentWindowScope.removeWindow('codeConcepts');
          }
        });

        // on window resize, save dimensions and screen location to user
        // preferences
        $window.onresize = function(evt) {
          clearTimeout(window.resizedFinished);
          window.resizedFinished = setTimeout(function() {
            console.debug('Resized finished.');
            $scope.user.userPreferences.properties['codeConceptsWidth'] = window.outerWidth;
            $scope.user.userPreferences.properties['codeConceptsHeight'] = window.outerHeight;
            $scope.user.userPreferences.properties['codeConceptsX'] = window.screenX;
            $scope.user.userPreferences.properties['codeConceptsY'] = window.screenY;
            $scope.parentWindowScope.saveWindowSettings('codeConcepts',
              $scope.user.userPreferences.properties);
          }, 250);
        }


        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          contentService.getConceptsForQuery("atoms.codeId:" + $scope.selected.linkedAtom.codeId, "NCIMTH", "latest",
          $scope.selected.project.id, null).then(
              function(data) {

            $scope.concepts = data.concepts.sort(utilService.sortBy('id'));  

          });

        }

        $scope.$watch('selected.linkedAtom.codeId', function() {
          $scope.initialize();
        });
        
 
      } ]);