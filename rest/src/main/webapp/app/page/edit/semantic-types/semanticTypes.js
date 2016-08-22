// Semantic types controller

tsApp.controller('SemanticTypesCtrl', [
  '$scope',
  '$http',
  '$location',
  '$routeParams',
  '$window',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'utilService',
  'metadataService',
  'metaEditingService',
  '$uibModal',
  function($scope, $http, $location, $routeParams, $window, gpService, utilService, tabService,
    securityService, utilService, metadataService, metaEditingService, $uibModal) {

    console.debug("configure SemanticTypesCtrl");

    // remove tabs, header and footer
    tabService.setShowing(false);
    utilService.setHeaderFooterShowing(false);

    // preserve parent scope reference
    $scope.parentWindow = window.opener.$windowScope;
    window.$windowScope = $scope;
    $scope.selected = $scope.parentWindow.selected;

    // Paging variables
    $scope.visibleSize = 4;
    $scope.pageSize = 5;
    $scope.paging = {};
    $scope.paging['stys'] = {
      page : 1,
      filter : '',
      typeFilter : '',
      filterFields : {'expandedForm' : 1, 'typeId' : 1, 'treeNumber' : 1},
      sortField : null,
      ascending : true,
      pageSize : $scope.pageSize
    };

    // add semantic type
    $scope.addSemanticTypeToConcept = function(semanticType) {
      metaEditingService.addSemanticType($scope.selected.project.id, null, $scope.selected.concept,
        semanticType).then(
      // Success
      function(data) {
        // TODO: need to refresh concept
        $scope.getPagedStys();
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // remove semantic type
    $scope.removeSemanticTypeFromConcept = function(semanticType) {
      metaEditingService.removeSemanticType($scope.selected.project.id, null,
        $scope.selected.concept, semanticType.id, true).then(
      // Success
      function(data) {
        // $scope.parentWindow.selectConcept($scope.selected.concept);
        // $scope.parentWindow.getWorklists();
        $scope.refresh();
        $scope.getPagedStys();
      },
      // Error
      function(data) {
        utilService.handleDialogError($scope.errors, data);
      });
    }

    // Get paged stys (assume all are loaded)
    $scope.getPagedStys = function() {
      // first only display stys that aren't already on concept
      $scope.stysForDisplay = [];
      console.debug('$scope.fullStys', $scope.fullStys.length);
      console.debug('$scope.selected.concept.semanticTypes', $scope.selected.concept.semanticTypes.length);
      for (var i = 0; i < $scope.fullStys.length; i++) {
        var found = false;
        for (var j = 0; j < $scope.selected.concept.semanticTypes.length; j++) {
          if ($scope.selected.concept.semanticTypes[j].semanticType == $scope.fullStys[i].expandedForm) {
            found = true;
          }
        }
        if (!found) {
          $scope.stysForDisplay.push($scope.fullStys[i]);
        }
      }
      console.debug('$scope.stysForDisplay', $scope.stysForDisplay.length);
      // page from the stys that are available to add
      $scope.pagedStys = utilService.getPagedArray($scope.stysForDisplay, $scope.paging['stys']);
    };

    // approve concept
    $scope.approveConcept = function() {
      $scope.parentWindow.approveConcept($scope.selected.concept);
    }

    // approve next
    $scope.approveNext = function() {
      $scope.parentWindow.approveNext();
    }

    // refresh
    $scope.refresh = function() {
      $scope.$apply();
    }

    // notify edit controller when semantic type window closes
    $window.onbeforeunload = function(evt) {
      $scope.parentWindow.removeWindow('semanticType');
    }

    // Table sorting mechanism
    $scope.setSortField = function(table, field, object) {
      utilService.setSortField(table, field, $scope.paging);

      // retrieve the correct table
      if (table === 'stys') {
        $scope.getPagedStys();
      }
    };

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      return utilService.getSortIndicator(table, field, $scope.paging);
    };
    
    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {
      metadataService.getSemanticTypes($scope.selected.project.terminology, 'latest').then(
        function(data) {
          $scope.fullStys = data.types;
          $scope.getPagedStys();
        },
        // Error
        function(data) {
          utilService.handleDialogError($scope.errors, data);
        });
    }

    // Call initialize
    $scope.initialize();

  } ]);