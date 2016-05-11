// Content controller
tsApp.controller('ContentCtrl', [
  '$rootScope',
  '$scope',
  '$routeParams',
  '$http',
  '$uibModal',
  '$location',
  '$q',
  '$anchorScroll',
  '$sce',
  '$uibModal',
  'gpService',
  'utilService',
  'tabService',
  'securityService',
  'metadataService',
  'contentService',
  'configureService',
  'appConfig',
  function($rootScope, $scope, $routeParams, $http, $uibModal, $location, $q, $anchorScroll, $sce,
    $uibModal, gpService, utilService, tabService, securityService, metadataService,
    contentService, configureService, appConfig) {
    console.debug('configure ContentCtrl');

    // tabs are showing
    tabService.setShowing(true);


    // retrieve the user
    $scope.user = securityService.getUser();
    console.debug($scope.user);
    // Clear error
    utilService.clearError();

    // pass app configuration constants to scope (for email link)
    $scope.appConfig = appConfig;

    // Handle resetting tabs on "back" button, but also handles non-standard
    // content modes which may not have tabs
    if (tabService.selectedTab.label != 'Content' && !$routeParams.mode) {
      tabService.setSelectedTabByLabel('Content');
    }

    //
    // Scope Variables
    //

    // Scope variables initialized from services
    // TODO Add this to other controllers where preferences are modified
    $scope.user = securityService.getUser();
    $scope.metadata = metadataService.getModel();
    $scope.component = null;
    $scope.pageSizes = contentService.getPageSizes();

    // Search parameters
    $scope.searchParams = contentService.getSearchParams();
    $scope.searchResults = null;
    $scope.searchOrBrowse = null;
    
    // favorites
    $scope.favoritesSearchParams = contentService.getSearchParams();

    // the expression constructor array
    $scope.expressions = [];
    $scope.selectedExpr = null;

    // set on terminology change
    $scope.autocompleteUrl = null;

    // Track search results and what type we are querying for
    $scope.queryForList = true;
    // whether to query for list, default
    $scope.queryForTree = false;
    // whether to query for tree

    // Variables for iterating through trees in report
    $scope.treeCount = null;
    $scope.treeViewed = null;
    $scope.componentTree = null;

    // component scoring
    $scope.scoreExcellent = 0.7;
    $scope.scoreGood = 0.3;

    $scope.getColorForScore = function(score) {
      if (score > $scope.scoreExcellent) {
        return 'green';
      } else if (score > $scope.scoreGood) {
        return 'yellow';
      } else {
        return 'orange';
      }
    };

    //
    // Watch expressions
    //

    // Watch for changes in metadata.terminologies (indicates application
    // readiness)
    $scope.$watch('metadata.terminology', function() {

      // clear the terminology-specific variables
      $scope.autoCompleteUrl = null;

      // if no terminology specified, stop
      if ($scope.metadata.terminology == null) {
        return;
      }

      // set the autocomplete url, with pattern:
      // /type/{terminology}/{version}/autocomplete/{searchTerm}
      $scope.autocompleteUrl = contentUrl
        + contentService.getPrefixForType($scope.metadata.terminology.organizingClassType) + '/'
        + $scope.metadata.terminology.terminology + '/' + $scope.metadata.terminology.version
        + "/autocomplete/";
    });

    // on route changes, save search params and last viewed component
    $scope.$on('$routeChangeStart', function() {
      contentService.setLastSearchParams($scope.searchParams);
      contentService.setLastComponent($scope.component);
    });

    //
    // General
    //

    // Configure tab and accordion
    $scope.configureTab = function() {
      // skip guest user
      if ($http.defaults.headers.common.Authorization == 'guest') {
        return;
      }
      $scope.user.userPreferences.lastTab = '/content';
      securityService.updateUserPreferences($scope.user.userPreferences);
    };

    // Sets the terminololgy
    $scope.setTerminology = function(terminology) {

      metadataService.setTerminology(terminology).then(function() {
        if (terminology.metathesaurus) {
          $scope.setListView();
        } else {
          if ($scope.searchParams.query) {
            $scope.findComponents(false);
          }
        }
      });
    };

    // Autocomplete function
    $scope.autocomplete = function(searchTerms) {
      // if invalid search terms, return empty array
      if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
        return new Array();
      }
      return contentService.autocomplete(searchTerms, $scope.autocompleteUrl);
    };

    // 
    // Search functions
    // 

    // Clear the search box and perform any additional operations
    // required
    $scope.clearQuery = function() {
      $scope.searchParams.query = null;
      $scope.semanticType = null;
      $scope.termType = null;
      $scope.matchTerminology = null;
      $scope.language = null;
    };

    // Perform a search for the tree view
    $scope.setTreeView = function() {
      $scope.queryForTree = true;
      $scope.queryForList = false;
      if ($scope.searchParams.query) {
        $scope.searchParams.page = 1;
        $scope.findComponentsAsTree($scope.searchParams.query);
      }
    };

    // Perform a search for the list view
    $scope.setListView = function() {
      $scope.queryForList = true;
      $scope.queryForTree = false;
      if ($scope.searchParams.query) {
        $scope.searchParams.page = 1;
        $scope.findComponentsAsList($scope.searchParams.query);
      }
    };

    // Get a component and set the local component data model
    // e.g. this is called when a user clicks on a search result
    $scope.getComponent = function(terminologyId, terminology, version) {
      contentService.getComponent(terminologyId, terminology, version).then(function(response) {

        $scope.component = response;
        // console.debug($scope.component.object);
        $scope.setActiveRow($scope.component.object.terminologyId);
        $scope.addComponentHistory();

      });
    };

    // Get a component and set the local component data model
    // e.g. this is called when a user clicks on a link in a report
    $scope.getComponentFromType = function(terminologyId, terminology, version, type) {
      contentService.getComponentFromType(terminologyId, terminology, version, type).then(
        function() {
          $scope.component = response;
          $scope.setActiveRow($scope.component.object.terminologyId);
          $scope.addComponentHistory($scope.component.historyIndex);
        });
    };

    // Find components for a programmatic query
    $scope.findComponentsForQuery = function(queryStr) {
      $scope.searchParams.page = 1;
      $scope.searchParams.query = queryStr;
      $scope.findComponents(true);
    };

    // Find concepts based on current search
    // - loadFirst indicates whether to auto-load result[0]
    $scope.findComponents = function(loadFirst, suppressWarnings) {
      $scope.searchOrBrowse = "SEARCH";
      if ($scope.queryForList) {
        $scope.findComponentsAsList(loadFirst, suppressWarnings);
      }
      if ($scope.queryForTree) {
        $scope.findComponentsAsTree(loadFirst, suppressWarnings);
      }
      $location.hash('top');
      $anchorScroll();

    };

    // Perform search and populate list view
    $scope.findComponentsAsList = function(loadFirst, suppressWarnings) {
      $scope.queryForTree = false;
      $scope.queryForList = true;

      var hasQuery = $scope.searchParams && $scope.searchParams.query
        && $scope.searchParams.query.length > 0;
      var hasExpr = $scope.searchParams && $scope.searchParams.advancedMode
        && $scope.searchParams.expression && $scope.searchParams.expression.value
        && $scope.searchParams.expression.value.length > 0;

      // ensure query/expression string has appropriate length
      if (!hasQuery && !hasExpr) {
        if (!suppressWarnings) {
          alert("You must use at least one character to search"
            + ($scope.searchParams.advancedMode ? " or supply an expression" : ""));

          // added to prevent weird bug causing page to scroll down a few lines
          $location.hash('top');
        }
        return;
      }
      contentService.findComponentsAsList($scope.searchParams.query,
        $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
        $scope.searchParams.page, $scope.searchParams).then(
        function(data) {
          $scope.searchResults = data;

          if (loadFirst && $scope.searchResults.results.length > 0) {
            $scope.getComponent($scope.searchResults.results[0].terminologyId,
              $scope.metadata.terminology.terminology, $scope.metadata.terminology.version);
          }
        });
    };

    // Perform search and populate tree view
    // - loadFirst is currently not used here
    $scope.findComponentsAsTree = function(loadFirst) {
      $scope.queryForTree = true;
      $scope.queryForList = false;

      // ensure query string has minimum length
      if (!$scope.searchParams.query || $scope.searchParams.query.length < 1) {
        alert("You must use at least one character to search");
        return;
      }

      contentService.findComponentsAsTree($scope.searchParams.query,
        $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
        $scope.searchParams.page, $scope.searchParams).then(function(data) {

        // for ease and consistency of use of the ui tree
        // directive force the single tree into a ui-tree structure
        // with count variables
        $scope.searchResults.tree = [];
        $scope.searchResults.tree.push(data); // treeList
        // array of size 1
        $scope.searchResults.tree.totalCount = data.totalCount;
        $scope.searchResults.tree.count = data.count;
        // Load first functionality is not obvious here
        // so leave it alone for now.

      });
    };

    // set the top level component from a tree node
    $scope.getComponentFromTree = function(nodeScope) {
      var tree = nodeScope.$modelValue;
      $scope.getComponent(tree.nodeTerminologyId, tree.terminology, tree.version);
    };

    // Load hierarchy into tree view
    $scope.browseHierarchy = function() {
      $scope.searchOrBrowse = "BROWSE";
      $scope.queryForTree = true;
      $scope.queryForList = false;
      $scope.browsingHierarchy = true;
      $scope.searchParams.page = 1;
      $scope.searchParams.query = null;

      contentService.getTreeRoots($scope.metadata.terminology.terminology,
        $scope.metadata.terminology.version, $scope.searchParams.page).then(function(data) {
        // for ease and consistency of use of the ui tree
        // directive
        // force the single tree into a ui-tree data
        // structure with count
        // variables
        $scope.searchResults.tree = [];
        $scope.searchResults.tree.push(data);
        // treeList array of size 1
        $scope.searchResults.tree.totalCount = data.totalCount;
        $scope.searchResults.tree.count = data.count;
      });
    };

    $scope.toggleAtomElement = function() {
      if ($scope.showAtomElement == null || $scope.showAtomElement == undefined) {
        $scope.showAtomElement = false;
      } else {
        $scope.showAtomElement = !$scope.showAtomElement;
      }

    };

    // //////////////////////////////////////////
    // Supporting search result trees
    // /////////////////////////////////////////

    // search result tree callbacks
    // NOTE Search Result Tree uses list search parameters
    $scope.srtCallbacks = {
      // set top level component from tree node
      getComponentFromTree : $scope.getComponentFromTree
    };

    // Function to toggle showing of extension info
    $scope.toggleExtension = function() {
      if ($scope.searchParams.showExtension == null
        || $scope.searchParams.showExtension == undefined) {
        $scope.searchParams.showExtension = false;
      } else {
        $scope.searchParams.showExtension = !$scope.searchParams.showExtension;
      }
    };

    // 
    // Misc helper functions
    // 

    // Helper function to select an item in the list view
    $scope.setActiveRow = function(terminologyId) {
      if (!$scope.searchResults.results || $scope.searchResults.results.length == 0)
        return;
      for (var i = 0; i < $scope.searchResults.results.length; i++) {
        if ($scope.searchResults.results[i].terminologyId === terminologyId) {
          $scope.searchResults.results[i].active = true;
        } else {
          $scope.searchResults.results[i].active = false;
        }
      }
    };

    //
    // METADATA related functions
    //

    // Find a terminology version
    $scope.getTerminologyVersion = function(terminology) {
      for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
        if (terminology === $scope.metadata.terminologies[i].terminology) {
          return $scope.metadata.terminologies[i].version;
        }
      }
    };
    // Function to filter viewable terminologies for picklist
    $scope.getViewableTerminologies = function() {
      var viewableTerminologies = new Array();
      if (!$scope.metadata.terminologies) {
        return null;
      }
      for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
        // exclude MTH and SRC
        if ($scope.metadata.terminologies[i].terminology != 'MTH'
          && $scope.metadata.terminologies[i].terminology != 'SRC')
          viewableTerminologies.push($scope.metadata.terminologies[i]);
      }
      return viewableTerminologies;
    };

    // 
    // HISTORY related functions
    //

    // Local history variables for the display.
    $scope.history = contentService.getHistory();
    $scope.historyPage = {};

    function setHistoryPage() {

      console.debug('setHistoryPage: ', $scope.history);

      // convenience variables
      var hps = parseInt($scope.pageSizes.general / 2);
      var ct = $scope.history.components.length;
      var index = $scope.history.index;

      // get the from and to indices
      var fromIndex = Math.max(index - (2 * hps - Math.min(ct - index, hps)), 0);
      var toIndex = Math.min(fromIndex + 2 * hps, ct);

      // slice the components
      var components = $scope.history.components.slice(fromIndex, toIndex);

      console.debug('from/to', fromIndex, toIndex, components);

      // assign indices for retrieval convenience
      for (var i = 0; i < components.length; i++) {
        components[i].index = fromIndex + i;
      }

      // set the scope variable
      $scope.historyPage = {
        fromIndex : fromIndex,
        toIndex : toIndex,
        components : components
      }
    }

    // Retrieve a component from the history list
    $scope.getComponentFromHistory = function(index) {

      // if currently viewed do nothing
      if (index === $scope.history.index) {
        console.debug('getComponentFromHistory: currently viewed, do nothing')
        return;
      }

      console.debug('getComponentFromHistory: ' + index);
      contentService.getComponentFromHistory(index).then(function(data) {
        console.debug('  -> history comp retrieved: ', data);
        $scope.component = data;
        setHistoryPage();
      });
    };

    // Get a string representation fo the component
    $scope.getComponentHistoryStr = function(component) {
      if (!component)
        return null;

      return component.terminology + "/" + component.terminologyId + " " + component.type + ": "
        + component.name;
    };

    // Function to set the local history for drop down list based on
    // an index For cases where history > page size, returns array
    // [index - pageSize / 2 + 1 : index + pageSize]
    $scope.addComponentHistory = function(index) {
      contentService.addComponentToHistory($scope.component.object.terminologyId,
        $scope.component.object.terminology, $scope.component.object.version,
        $scope.component.prefix, $scope.component.object.name);
      setHistoryPage();

    };

    // Pop out content window
    $scope.popout = function() {
      var currentUrl = window.location.href;
      var baseUrl = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
      // TODO; don't hardcode this - maybe "simple" should be a parameter
      var newUrl = baseUrl + '/content/simple/' + $scope.component.object.terminology + '/'
        + $scope.component.object.version + '/' + $scope.component.object.terminologyId;
      var myWindow = window.open(newUrl, $scope.component.object.terminology + '/'
        + $scope.component.object.version + ', ' + $scope.component.object.terminologyId + ', '
        + $scope.component.object.name);
      myWindow.focus();
    };

    //
    // Expression handling
    //

    $scope.setExpression = function() {
      console.debug('Setting expression from ', $scope.searchParams.expression)

      // ensure all fields set to wildcard if not set
      for ( var key in $scope.searchParams.expression.fields) {
        if ($scope.searchParams.expression.fields.hasOwnProperty(key)) {
          if (!$scope.searchParams.expression.fields[key]) {
            $scope.searchParams.expression.fields[key] = '*';
          }
        }
      }

      // call the expression's pattern generator
      $scope.searchParams.expression.compute();

      // replace wildcards with blank values again
      // TODO Very clunky, obviously
      for ( var key in $scope.searchParams.expression.fields) {
        if ($scope.searchParams.expression.fields.hasOwnProperty(key)) {
          if ($scope.searchParams.expression.fields[key] === '*') {
            $scope.searchParams.expression.fields[key] = '';
          }
        }
      }

    };

    // clears the fields, computed value and resets selected expression
    $scope.clearExpression = function() {
      for ( var key in $scope.searchParams.expression.fields) {
        $scope.searchParams.expression.fields[key] = null;
      }
      $scope.searchParams.expression.value = null;
      $scope.searchParams.expression = $scope.expressions[0];
    }

    // get the defined expressions and set to the first option
    $scope.configureExpressions = function() {
      $scope.expressions = contentService.getExpressions();
      $scope.searchParams.expression = $scope.expressions[0];
    };

    $scope.selectComponent = function(key) {

      var modalScope = $rootScope.$new();

      var modalInstance = $uibModal.open({
        animation : $scope.animationsEnabled,
        templateUrl : 'app/util/select-component-modal/selectComponentModal.html',
        controller : 'selectComponentModalCtrl',
        scope : $rootScope,
        size : 'lg',
        resolve : {
          metadata : function() {
            return $scope.metadata;
          }
        }
      });

      modalInstance.result.then(function(component) {
        console.debug('returned with component', component);
        $scope.searchParams.expression.fields[key] = component.object.terminologyId + ' | '
          + component.object.name + ' |';
        $scope.setExpression();
      }, function() {
        // do nothing
      });
    };

    $scope.viewNotes = function(key) {

      var modalScope = $rootScope.$new();

      var modalInstance = $uibModal.open({
        animation : $scope.animationsEnabled,
        templateUrl : 'app/util/annotation-modal/annotationModal.html',
        controller : 'annotationModalCtrl',
        scope : $rootScope,
        size : 'lg',
        resolve : {
          component : function() {
            return $scope.component;
          }
        }
      });

      modalInstance.result.then(function(component) {
        console.debug('returned with component', component);
        $scope.searchParams.expression.fields[key] = component.object.terminologyId + ' | '
          + component.object.name + ' |';
        $scope.setExpression();
      }, function() {
        // do nothing
      });
    };

    $scope.getUserFavorites = function(page) {
      contentService.getUserFavorites(term.terminology, term.version, $scope.favoritesSearchParams).then(
        function(response) {
          $scope.favorites = response;
        })
    }

    // TODO Move this into more formal setComponent function later
    $scope.$watch('component', function() {
      if ($scope.user) {
        $scope.isFavorite = $scope.user.userPreferences.favorites.filter(function(item) {
          return item.terminology === $scope.component.object.terminology
            && item.terminologyId === $scope.component.object.terminologyId
            && item.version === $scope.component.object.version;
        }).length > 0;
      }
    }, true);
    
    $scope.toggleFavorite = function() {
      
    }

    //
    // Initialize
    //

    $scope.initialize = function() {

      $scope.configureTab();
      $scope.configureExpressions();

      // get the first page of unfilitered user favorites
      $scope.getUserFavorites(1, null);

      //
      // Check for values preserved in content service (after route changes)
      //
      if (contentService.getLastSearchParams()) {
        $scope.searchParams = contentService.getLastSearchParams();
        $scope.findComponents(false, true);
      }
      if (contentService.getLastComponent()) {
        $scope.component = contentService.getLastComponent();
      }

      //
      // Component Report Callbacks
      //
      // if in simple mode, disable navigation functionality
      if ($routeParams.mode === 'simple') {
        $scope.componentReportCallbacks = {
          getTerminologyVersion : metadataService.getTerminologyVersion,
          getRelationshipTypeName : metadataService.getRelationshipTypeName,
          getAttributeNameName : metadataService.getAttributeNameName,
          getTermTypeName : metadataService.getTermTypeName,
          getGeneralEntryValue : metadataService.getGeneralEntryValue,
          getLabelSetName : metadataService.getLabelSetName,
          countLabels : metadataService.countLabels

        };
      }

      // otherwise, enable full functionality
      else {
        $scope.componentReportCallbacks = {
          getComponent : $scope.getComponent,
          getComponentFromType : $scope.getComponentFromType,
          getComponentFromTree : $scope.getComponentFromTree,
          getTerminologyVersion : metadataService.getTerminologyVersion,
          getRelationshipTypeName : metadataService.getRelationshipTypeName,
          getAttributeNameName : metadataService.getAttributeNameName,
          getTermTypeName : metadataService.getTermTypeName,
          getGeneralEntryValue : metadataService.getGeneralEntryValue,
          getLabelSetName : metadataService.getLabelSetName,
          countLabels : metadataService.countLabels,
          findComponentsForQuery : $scope.findComponentsForQuery

        };
      }

      // Load all terminologies upon controller load (unless already
      // loaded)
      if (!$scope.metadata.terminologies || !$scope.metadata.terminology) {
        metadataService.initTerminologies().then(
          // success
          function(data) {

            // if route parameters are specified, set the terminology and retrieve
            // the specified concept
            if ($routeParams.terminology && $routeParams.version) {

              var termToSet = null;
              for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                var terminology = $scope.metadata.terminologies[i];
                // Determine whether to set as default
                if (terminology.terminology === $routeParams.terminology
                  && terminology.version === $routeParams.version) {
                  termToSet = terminology;
                  break;
                }
              }

              if (!termToSet) {
                utilService.setError('Terminology specified in URL not found');
              } else {

                // set the terminology
                metadataService.setTerminology(termToSet).then(
                  function() {

                    // get the component
                    $scope.getComponent($routeParams.terminologyId, $routeParams.terminology,
                      $routeParams.version);
                  });
              }
            }

            // otherwise, specify the default terminology
            else {

              var found = false;
              for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                var terminology = $scope.metadata.terminologies[i];
                // Determine whether to set as default
                if (terminology.metathesaurus) {
                  metadataService.setTerminology(terminology);
                  found = true;
                  break;
                }
              }

              // if no metathesaurus found, default to ICD10CM
              // TODO: Used for ICD10 server, unhardcode this
              if (!found) {
                for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                  var terminology = $scope.metadata.terminologies[i];
                  if (terminology.terminology === 'ICD10CM') {
                    metadataService.setTerminology(terminology);
                    found = true;
                    break;
                  }
                }
              }

              // If nothing set, pick the first one
              if (!found) {
                if (!$scope.metadata.terminologies) {
                  window.alert('No terminologies found, database may not be properly loaded.');
                } else {
                  metadataService.setTerminology($scope.metadata.terminologies[0]);
                }
              }
            }
          });
      }
    }

    //
    // Initialization: Check 
    // (1) that application is configured, and
    // (2) that the license has been accepted (if required)
    //
    configureService.isConfigured().then(function(isConfigured) {
      if (!isConfigured) {
        $location.path('/configure');
      } else {
        securityService.checkLicense().then(function() {
          console.debug('License valid, initializing');
          $scope.initialize();
        }, function() {
          console.debug('Invalid license');
          utilService.setError('You must accept the license before viewing that content');
          $location.path('/license');
        })
      }
    });

  }

]);
