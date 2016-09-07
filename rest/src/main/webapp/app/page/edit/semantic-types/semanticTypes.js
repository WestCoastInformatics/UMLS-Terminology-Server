// Semantic types controller

tsApp
  .controller(
    'SemanticTypesCtrl',
    [
      '$scope',
      '$window',
      'tabService',
      'utilService',
      'metadataService',
      'metaEditingService',
      '$uibModal',
      function($scope, $window, tabService, utilService, metadataService, metaEditingService,
        $uibModal) {

        console.debug("configure SemanticTypesCtrl");

        // remove tabs, header and footer
        tabService.setShowing(false);
        utilService.setHeaderFooterShowing(false);

        // preserve parent scope reference
        $scope.parentWindowScope = window.opener.$windowScope;
        window.$windowScope = $scope;
        $scope.selected = $scope.parentWindowScope.selected;
        $scope.lists = $scope.parentWindowScope.lists;

        // sty lists
        $scope.fullStys = [];
        $scope.stysForDisplay = [];

        // Paging variables
        $scope.paging = {};
        $scope.paging['stys'] = utilService.getPaging();
        $scope.paging['stys'].sortField = 'typeId';
        $scope.paging['stys'].pageSize = 5;
        $scope.paging['stys'].filterFields = {};
        $scope.paging['stys'].filterFields.expandedForm = 1;
        $scope.paging['stys'].filterFields.typeId = 1;
        $scope.paging['stys'].filterFields.treeNumber = 1;
        $scope.paging['stys'].sortAscending = false;
        $scope.paging['stys'].callback = {
          getPagedList : getPagedStys
        };

        $scope.$watch('selected.concept', function() {
          console.debug('in watch');
          $scope.getPagedStys();
        });

        // add semantic type
        $scope.addSemanticTypeToConcept = function(semanticType) {
          metaEditingService.addSemanticType($scope.selected.project.id, null,
            $scope.selected.concept, semanticType);
        }

        // remove semantic type
        $scope.removeSemanticTypeFromConcept = function(semanticType) {
          metaEditingService.removeSemanticType($scope.selected.project.id, null,
            $scope.selected.concept, semanticType.id, true);
        }

        // Get paged stys (assume all are loaded)
        $scope.getPagedStys = function() {
          getPagedStys();
        }
        function getPagedStys() {
          $scope.stysForDisplay = [];
          // first only display stys that aren't already on concept
          for (var i = 0; i < $scope.fullStys.length; i++) {
            var found = false;
            for (var j = 0; j < $scope.selected.concept.semanticTypes.length; j++) {
              if ($scope.selected.concept.semanticTypes[j].semanticType == $scope.fullStys[i].expandedForm) {
                found = true;
                break;
              }
            }
            if (!found) {
              $scope.stysForDisplay.push($scope.fullStys[i]);
            }
          }
          // page from the stys that are available to add
          $scope.pagedStys = utilService
            .getPagedArray($scope.stysForDisplay, $scope.paging['stys']);
        }
        ;

        // approve concept
        $scope.approveConcept = function() {
          $scope.parentWindowScope.approveConcept($scope.selected.concept);
        }

        // approve next
        $scope.approveNext = function() {
          $scope.parentWindowScope.approveNext();
        }

        // next
        $scope.next = function() {
          $scope.parentWindowScope.next();
        }

        // refresh
        $scope.refresh = function() {
          $scope.$apply();
        }

        // notify edit controller when semantic type window closes
        $window.onbeforeunload = function(evt) {
          $scope.parentWindowScope.removeWindow('semanticType');
        }

        // Table sorting mechanism
        $scope.setSortField = function(table, field, object) {
          utilService.setSortField(table, field, $scope.paging);
          $scope.getPagedStys();
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {

          // Initialize metadata
          metadataService.getSemanticTypes($scope.selected.project.terminology, 'latest').then(
          // Success
          function(data) {
            $scope.fullStys = data.types;
            $scope.getPagedStys();
          });

        }

        // Call initialize
        $scope.initialize();

      } ]);