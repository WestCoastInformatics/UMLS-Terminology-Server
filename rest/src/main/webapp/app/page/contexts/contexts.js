// Semantic types controller

tsApp.controller('ContextsCtrl', [
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
  'contentService',
  '$uibModal',
  function($scope, $http, $location, $routeParams, $window, gpService, utilService, tabService,
    securityService, utilService, metadataService, contentService, $uibModal) {

    console.debug("configure ContextsCtrl");

    // remove tabs, header and footer
    tabService.setShowing(false);
    utilService.setHeaderFooterShowing(false);

    // preserve parent scope reference
    $scope.parentWindowScope = window.opener.$windowScope;
    window.$windowScope = $scope;
    $scope.selected = $scope.parentWindowScope.selected;
    $scope.lists = $scope.parentWindowScope.lists;
    $scope.user = $scope.parentWindowScope.user;
    $scope.entries = [];
    $scope.selected.entry = null;
    
    
    
    
    // Paging variables
    $scope.paging = {};
    $scope.paging['entries'] = utilService.getPaging();
    $scope.paging['entries'].sortField = 'terminology';
    $scope.paging['entries'].pageSize = 10;
    $scope.paging['entries'].filterFields = {};
    $scope.paging['entries'].filterFields.terminology = 1;
    $scope.paging['entries'].sortAscending = false;
    $scope.paging['entries'].callback = {
      getPagedList : getPagedEntries
    };

    
    $scope.$watch('selected.concept', function() {
      console.debug('in watch');
      //$scope.getPagedEntries();
    });


    // Get paged entries (assume all are loaded)
    $scope.getPagedEntries = function() {
      getPagedEntries();
    }
    function getPagedEntries() {
      for (var i=0; i<$scope.selected.concept.atoms.length; i++) {
        var entry = {};
        var fullTerminology = metadataService.getCurrentTerminology($scope.selected.concept.atoms[i].terminology)
        entry.type = fullTerminology.organizingClassType;
        entry.terminology = fullTerminology.rootTerminologyAbbreviation;
        entry.terminologyId = null;
        if (entry.type == 'CODE') {
          entry.terminologyId = $scope.selected.concept.atoms[i].codeId;
        } else if (entry.type == 'CONCEPT') {
          entry.terminologyId = $scope.selected.concept.atoms[i].conceptId;
        } else if (entry.type == 'DESCRIPTOR') {
          entry.terminologyId = $scope.selected.concept.atoms[i].descriptorId;
        } else {
          continue;
        }
        entry.version = metadataService.getTerminologyVersion(entry.terminology);
        var found = false;
        for (var j=0; j<$scope.entries.length; j++) {
          if ($scope.entries[j].terminology == entry.terminology &&
              $scope.entries[j].terminologyId == entry.terminologyId &&
              $scope.entries[j].version == entry.version)
            found = true;
        }
        if (!found) {
          $scope.entries.push(entry);
        }
      }
      
      $scope.pagedEntries = utilService.getPagedArray($scope.entries, $scope.paging['entries']);
    };


    // refresh
    $scope.refresh = function() {
      $scope.$apply();
    }

    // notify edit controller when semantic type window closes
    $window.onbeforeunload = function(evt) {
      $scope.parentWindowScope.removeWindow('context');
    }

    // Table sorting mechanism
    $scope.setSortField = function(table, field, object) {
      utilService.setSortField(table, field, $scope.paging);
      $scope.getPagedEntries();
    };

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      return utilService.getSortIndicator(table, field, $scope.paging);
    };
 
    // selects an entry
    $scope.selectEntry = function(event, entry) {
        $scope.selected.entry = entry;
        contentService.getTree(entry, 0).then(function(data) {
          $scope.selected.component = data;
          $scope.component = data;
        });
    };


    // indicates if a particular row is selected
    $scope.isRowSelected = function(entry) {
      return $scope.selected.entry == entry;
    }
    
    
    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {  
      metadataService.initialize().then(function(data) {
        $scope.metadata = metadataService.getModel();
        $scope.getPagedEntries();
      });
    }

    // Call initialize
    $scope.initialize();


  } ]);