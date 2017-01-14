// Semantic types controller

tsApp.controller('ContextsCtrl', [
  '$scope',
  '$window',
  'utilService',
  'tabService',
  'securityService',
  'utilService',
  'contentService',
  function($scope, $window, utilService, tabService, securityService, utilService, contentService) {

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
    $scope.pageSizes = utilService.getPageSizes();
    $scope.paging = {};
    $scope.paging['entries'] = utilService.getPaging();
    $scope.paging['entries'].sortField = 'terminology';
    $scope.paging['entries'].pageSize = 5;
    $scope.paging['entries'].filterList = [];
    $scope.paging['entries'].filterFields = {};
    $scope.paging['entries'].filterFields.terminology = 1;
    $scope.paging['entries'].sortAscending = false;
    $scope.paging['entries'].callbacks = {
      getPagedList : getPagedEntries
    };

    // Watch for component change
    $scope.$watch('selected.component', function() {
      console.debug('xxx1');
      $scope.getPagedEntries();
    });

    // Get the terminology object for the terminology value
    $scope.getTerminology = function(terminology) {
      for (var i = 0; i < $scope.lists.terminologies.length; i++) {
        if ($scope.lists.terminologies[i].terminology == terminology) {
          return $scope.lists.terminologies[i];
        }
      }
    }

    // Get paged entries (assume all are loaded)
    $scope.getPagedEntries = function() {
      console.debug('yyy');
      getPagedEntries();
    }
    function getPagedEntries() {
      console.debug('zzz');
      $scope.entries = [];
      contentService.findDeepTreePositions({
        terminology : $scope.selected.project.terminology,
        version : $scope.selected.project.version,
        terminologyId : $scope.selected.component.terminologyId,
        type : $scope.selected.component.type
      }, $scope.paging['entries']).then(
      // Success
      function(data) {
        $scope.pagedEntries = data.treePositions;
        $scope.pagedEntries.totalCount = data.totalCount;

        if (data.treePositions.length > 0) {
          $scope.selectEntry(null, data.treePositions[0]);
        }
      });
    }

    // refresh
    $scope.refresh = function() {
      console.debug('xxx2');
      $scope.$apply();
    }

    // notify edit controller when semantic type window closes
    $window.onbeforeunload = function(evt) {
      $scope.parentWindowScope.removeWindow('context');
    }
    $scope.$on('$destroy', function() {
      if (!parentClosing) {
        $scope.parentWindowScope.removeWindow('context');
      }
    });

    // on window resize, save dimensions and screen location to user preferences
    $window.onresize = function(evt) {
      clearTimeout(window.resizedFinished);
      window.resizedFinished = setTimeout(function() {
        console.log('Resized finished on context window.');
        $scope.user.userPreferences.properties['contextWidth'] = window.outerWidth;
        $scope.user.userPreferences.properties['contextHeight'] = window.outerHeight;
        $scope.user.userPreferences.properties['contextX'] = window.screenX;
        $scope.user.userPreferences.properties['contextY'] = window.screenY;
        $scope.parentWindowScope.saveWindowSettings('context',
          $scope.user.userPreferences.properties);

      }, 250);
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
      var lcomponent = {
        id : entry.nodeId,
        type : entry.type,
        terminology : entry.nodeTerminology,
        version : entry.nodeVersion,
        terminologyId : entry.nodeTerminologyId
      };
      if (entry.type === 'ATOM') {
        $scope.component = lcomponent;
      } else {
        contentService.getComponent(lcomponent, $scope.selected.project.id).then(function(data) {
          $scope.component = data;
        });
      }
    };

    // indicates if a particular row is selected
    $scope.isRowSelected = function(entry) {
      return $scope.selected.entry == entry;
    }

    //
    // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
    //
    $scope.initialize = function() {
      // n/a
    }

    // Call initialize
    $scope.initialize();

  } ]);