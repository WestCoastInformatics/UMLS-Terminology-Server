// Content controller
tsApp
  .controller(
    'ContentCtrl',
    [
      '$rootScope',
      '$scope',
      '$routeParams',
      '$uibModal',
      '$location',
      '$q',
      '$anchorScroll',
      '$sce',
      'gpService',
      'utilService',
      'tabService',
      'configureService',
      'securityService',
      'projectService',
      'metadataService',
      'contentService',
      'appConfig',
      function($rootScope, $scope, $routeParams, $uibModal, $location, $q, $anchorScroll, $sce,
        gpService, utilService, tabService, configureService, securityService, projectService,
        metadataService, contentService, appConfig) {
        console.debug('configure ContentCtrl');

        // Set up tabs and controller
        if ($routeParams.mode) {
          console.debug('  ' + $routeParams.mode + '  mode deletected, hide tabs');
          tabService.setShowing(false);
          utilService.setHeaderFooterShowing(false);
        } else {
          console.debug('  no-params mode detected, show tabs');
          tabService.setShowing(true);
          utilService.setHeaderFooterShowing(true);
        }

        utilService.clearError();
        $scope.user = securityService.getUser();
        projectService.getUserHasAnyRole();
        tabService.setSelectedTabByLabel('Content');

        // pass app configuration constants to scope (for email link)
        $scope.appConfig = appConfig;

        // History
        $scope.history = contentService.getHistory();

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.user = securityService.getUser();
        $scope.isGuestUser = securityService.isGuestUser;
        $scope.callbacks = contentService.getCallbacks();
        console.debug('callbacks', $scope.callbacks);
        // Scope vars
        $scope.mode = $routeParams.mode ? $routeParams.mode : 'full';
        $scope.selected = {
          metadata : metadataService.getModel(),
          component : null,
          project : null
        };
        $scope.lists = {
          projects : [],
          terminologies : []
        };

        // Search parameters
        $scope.searchParams = contentService.getSearchParams();
        $scope.searchResults = {};
        $scope.searchResultsCollapsed = true;
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

        // Stuff for scoring
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

        // on route changes, save search params and last viewed component
        $scope.$on('$routeChangeStart', function() {
          contentService.setLastSearchParams($scope.searchParams);
          contentService.setLastComponent($scope.selected.component);
        });

        //
        // General
        //

        // Sets the terminololgy
        $scope.setTerminology = function(terminology) {
          // Set shared model (may already be set)
          metadataService.setTerminology(terminology);

          // set the autocomplete url, with pattern:
          // /type/{terminology}/{version}/autocomplete/{searchTerm}
          $scope.autocompleteUrl = $scope.selected.metadata.terminology.organizingClassType
            .toLowerCase()
            + '/'
            + $scope.selected.metadata.terminology.terminology
            + '/'
            + $scope.selected.metadata.terminology.version + "/autocomplete/";

          // Load all metadata for this terminology, store it in the metadata
          // service and return deferred promise
          var deferred = $q.defer();
          metadataService.getAllMetadata(terminology.terminology, terminology.version).then(
          // Success
          function(data) {

            // Set the shared model in the metadata service
            metadataService.setModel(data);

            // if metathesaurus, ensure list view set
            if (terminology.metathesaurus) {
              $scope.setListView();
            }
            // if a query is specified, research
            if ($scope.searchParams.query || $scope.searchParams.advancedMode) {
              $scope.findComponents(false, true);
            }

            // Do not update user prefs when in popout mode
            if (!$routeParams.terminology && $scope.user && $scope.user.userPreferences) {
              $scope.user.userPreferences.lastTerminology = terminology.terminology;
              securityService.updateUserPreferences($scope.user.userPreferences);
            }
            deferred.resolve();
          }, function() {
            deferred.reject();
          });

          return deferred.promise;
        };

        // Retrieve all projects
        $scope.getProjects = function() {

          projectService.getProjectsForUser($scope.user).then(
          // Success
          function(data) {
            $scope.lists.projects = data.projects;
            $scope.selected.project = data.project;
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
          $scope.searchResults = [];
          $scope.searchParams.page = 1;
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
        $scope.getComponent = function(component) {
          contentService.getComponent(component).then(
          // Success
          function(response) {

            // If we still don't know the terminology (because of a link in),
            // look it up first

            $scope.selected.component = response;
            $scope.checkFavoriteStatus();
            $scope.addComponentHistory();

          });
        };

        // Find components for a programmatic query
        $scope.findComponentsForQuery = function(queryStr) {
          $scope.searchParams.page = 1;
          $scope.searchParams.query = queryStr;
          $scope.findComponents(true);
        };

        $scope.performNewSearch = function(suppressWarnings) {
          $scope.searchParams.page = 1;
          $scope.searchResultsCollapsed = false;
          $scope.findComponents(true, suppressWarnings);
        };

        // Find concepts based on current search
        // - loadFirst indicates whether to auto-load result[0]
        $scope.findComponents = function(loadFirst, suppressWarnings) {
          findComponents(loadFirst, suppressWarnings);
        }
        function findComponents(loadFirst, suppressWarnings) {
          $scope.searchOrBrowse = "SEARCH";
          if ($scope.queryForList) {
            $scope.findComponentsAsList(loadFirst, suppressWarnings);
          }
          if ($scope.queryForTree) {
            $scope.findComponentsAsTree(loadFirst, suppressWarnings);
          }
          $location.hash('top');
          $anchorScroll();

        }
        ;

        // Perform search and populate list view
        $scope.findComponentsAsList = function(loadFirst, suppressWarnings) {
          $scope.queryForTree = false;
          $scope.queryForList = true;

          // prerequisite checking
          var hasQuery = $scope.searchParams && $scope.searchParams.query
            && $scope.searchParams.query.length > 0;
          var hasExpr = $scope.searchParams && $scope.searchParams.advancedMode
            && $scope.searchParams.expression && $scope.searchParams.expression.value
            && $scope.searchParams.expression.value.length > 0;
          var hasNotes = $scope.searchParams && $scope.searchParams.advancedMode
            && $scope.searchParams.userNote;

          // ensure query/expression string has appropriate length
          if (!hasQuery && !hasExpr && !hasNotes) {
            if (!suppressWarnings) {
              alert("You must use at least one character to search"
                + ($scope.searchParams.advancedMode ? ($scope.selected.metadata.terminology.descriptionLogicTerminology ? ", supply an expression,"
                  : "")
                  + " or search user notes"
                  : ""));

              // added to prevent weird bug causing page to scroll down a few
              // lines
              $location.hash('top');
            }
            return;
          }

          contentService.findComponentsAsList($scope.searchParams.query,
            $scope.selected.metadata.terminology.organizingClassType,
            $scope.selected.metadata.terminology.terminology,
            $scope.selected.metadata.terminology.version, $scope.searchParams).then(function(data) {
            $scope.searchResults = data;

            if (loadFirst && $scope.searchResults.results.length > 0) {
              // pass the search result (as component)
              $scope.getComponent($scope.searchResults.results[0]);
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
            $scope.selected.metadata.terminology.organizingClassType,
            $scope.selected.metadata.terminology.terminology,
            $scope.selected.metadata.terminology.version, $scope.searchParams).then(function(data) {

            // for ease and consistency of use of the ui tree
            // directive force the single tree into a ui-tree structure
            // with count variables
            $scope.searchResults.tree = [];
            $scope.searchResults.tree.push(data); // treeList
            // array of size 1
            $scope.searchResults.tree.totalCount = data.totalCount;
            // Load first functionality is not obvious here
            // so leave it alone for now.

          });
        };

        // set the top level component from a tree node
        // TODO Consider changing nodeTerminologyId to terminologyId, adding
        // type to allow component universality
        $scope.getComponentFromTree = function(type, nodeScope) {
          console.debug('getComponentFromTree', type, nodeScope);
          var tree = nodeScope.$modelValue;
          $scope.getComponent({
            id : tree.nodeId,
            type : type,
            terminologyId : tree.nodeTerminologyId,
            terminology : tree.terminology,
            version : tree.version
          });
        };

        // Load hierarchy into tree view
        $scope.browseHierarchy = function() {
          $scope.searchOrBrowse = "BROWSE";
          $scope.queryForTree = true;
          $scope.queryForList = false;
          $scope.browsingHierarchy = true;
          $scope.searchResults.page = 1
          $scope.searchParams.query = null;

          contentService.getTreeRoots($scope.selected.metadata.terminology.organizingClassType,
            $scope.selected.metadata.terminology.terminology,
            $scope.selected.metadata.terminology.version).then(function(data) {
            // for ease and consistency of use of the ui tree
            // directive
            // force the single tree into a ui-tree data
            // structure with count
            // variables
            $scope.queryForTree = true;
            $scope.searchResults.tree = [];
            $scope.searchResults.tree.push(data);
            // treeList array of size 1
            $scope.searchResults.tree.totalCount = data.totalCount;
            if (data.objects) {
              $scope.searchResults.tree.objects.length = data.objects.length
            }
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
        // METADATA related functions
        //

        // Find a terminology version
        $scope.getTerminologyVersion = function(terminology) {
          for (var i = 0; i < $scope.lists.terminologies.length; i++) {
            if (terminology === $scope.lists.terminologies[i].terminology) {
              return $scope.lists.terminologies[i].version;
            }
          }
        };
        // Function to filter viewable terminologies for picklist
        $scope.getViewableTerminologies = function() {
          var viewableTerminologies = new Array();
          if (!$scope.lists.terminologies) {
            return null;
          }
          for (var i = 0; i < $scope.lists.terminologies.length; i++) {
            // exclude MTH and SRC
            if ($scope.lists.terminologies[i].terminology != 'MTH'
              && $scope.lists.terminologies[i].terminology != 'SRC')
              viewableTerminologies.push($scope.lists.terminologies[i]);
          }
          return viewableTerminologies;
        };

        // 
        // HISTORY related functions
        //

        // Local history variables for the display.
        function setHistoryPage() {
          console.debug('setHistoryPage: ', $scope.history);

          // convenience variables
          var hps = $scope.historyPageSize;
          var ct = $scope.history.components.length;
          var index = $scope.history.index;

          // get the from and to indices
          var fromIndex = Math.max(index - (2 * hps - Math.min(ct - index, hps)), 0);
          var toIndex = Math.min(fromIndex + 2 * hps, ct);

          // slice the components
          var components = $scope.history.components.slice(fromIndex, toIndex);

          // assign indices for retrieval convenience
          for (var i = 0; i < components.length; i++) {
            components[i].index = fromIndex + i;
          }

          // set the scope variable
          $scope.historyPage = {
            fromIndex : fromIndex,
            toIndex : toIndex,
            components : components
          };
        }

        // Retrieve a component from the history list
        $scope.getComponentFromHistory = function(index) {

          // if currently viewed do nothing
          if (index === $scope.history.index) {
            return;
          }

          contentService.getComponentFromHistory(index).then(function(data) {
            $scope.selected.component = data;
            $scope.checkFavoriteStatus();
            setHistoryPage();
          });
        };

        // Get a string representation fo the component
        $scope.getComponentHistoryStr = function(component) {
          if (!component)
            return null;

          return component.terminology + "/" + component.terminologyId + " " + component.type
            + ": " + component.name;
        };

        // Function to set the local history for drop down list based on
        // an index For cases where history > page size, returns array
        // [index - pageSize / 2 + 1 : index + pageSize]
        $scope.addComponentHistory = function(index) {
          contentService.addComponentToHistory($scope.selected.component);
          setHistoryPage();

        };

        // Pop out content window
        $scope.popout = function() {
          contentService.popout($scope.selected.component);
        };

        //
        // Expression handling
        //

        // Set expression
        $scope.setExpression = function() {
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
        };

        // get the defined expressions and set to the first option
        $scope.configureExpressions = function() {
          $scope.expressions = contentService.getExpressions();
          $scope.searchParams.expression = $scope.expressions[0];
        };

        $scope.selectComponent = function(key) {

          window.alert('wire this to finder modal which should be its own component');

          // modalInstance.result.then(function(component) {
          // $scope.searchParams.expression.fields[key] =
          // component.terminologyId + ' | '
          // + component.name + ' |';
          // $scope.setExpression();
          // }, function() {
          // // do nothing
          // });
        };

        // remove atom
        $scope.removeAtom = function(atom) {
          metaEditingService.removeAtom($scope.selected.project.id, $scope.selected.activityId,
            $scope.selected.component, atom.id, true);
        }

        //
        // MODALS
        //

        // Add atom modal
        $scope.openAddAtomModal = function(latom) {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/atoms/editAtom.html',
            backdrop : 'static',
            controller : 'AtomModalCtrl',
            resolve : {
              atom : function() {
                return null;
              },
              action : function() {
                return 'Add';
              },
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(user) {
            $scope.getPagedAtoms();
          });
        };

        // Open notes modal, from either wrapper or component
        $scope.viewNotes = function(wrapper) {

          var modalInstance = $uibModal.open({
            animation : $scope.animationsEnabled,
            templateUrl : 'app/util/component-note-modal/componentNoteModal.html',
            controller : 'componentNoteModalCtrl',
            scope : $rootScope,
            size : 'lg',
            resolve : {
              component : function() {
                return $scope.selected.component;

              }
            }
          });

          // on close or cancel, re-retrieve the concept for updated notes
          modalInstance.result.then(function() {
            // re-retrieve the concept
            $scope.getComponent($scope.selected.component);
          }, function() {
            // re-retrieve the concept
            $scope.getComponent($scope.selected.component);
          });
        };

        //
        // Favorites
        //

        // Check favorite status
        $scope.checkFavoriteStatus = function() {
          $scope.isFavorite = $scope.selected.component ? securityService
            .isUserFavorite($scope.selected.component) : false;
        };

        // Toggle favorite
        $scope.toggleFavorite = function(component) {
          if (securityService.isUserFavorite(component)) {
            securityService.removeUserFavorite(component).then(function() {
              $scope.isFavorite = false;
              websocketService.fireFavoriteChange();
            });
          } else {
            securityService.addUserFavorite(component).then(function() {
              $scope.isFavorite = true;
              websocketService.fireFavoriteChange();
            });
          }
        };

        //
        // Callbacks Function Objects
        //
        $scope.configureCallbacks = function() {

          //
          // Local scope functions pertaining to component retrieval
          //
          $scope.callbacks = {
            getComponent : $scope.getComponent,
            getComponentFromTree : $scope.getComponentFromTree,
            findComponentsForQuery : $scope.findComponentsForQuery,
            checkFavoriteStatus : $scope.checkFavoriteStatus
          };

          //
          // Component report callbacks
          //

          // pass metadata callbacks for tooltip and general display
          utilService.extendCallbacks($scope.callbacks, metadataService.getCallbacks());

          // add content callbacks for special content retrieval (relationships,
          // mappings, etc.)
          utilService.extendCallbacks($scope.callbacks, contentService.getCallbacks());

        };

        // Wait for "terminologies" to load
        $scope.initMetadata = function() {

          metadataService.getTerminologies().then(
            // Success
            function(data) {
              $scope.lists.terminologies = data.terminologies;

              // Load all terminologies upon controller load (unless already
              // loaded)
              if ($scope.lists.terminologies) {

                // if route parameters are specified, set the terminology and
                // retrieve the specified concept
                var terminologies = [];
                if ($routeParams.terminology
                  && (($routeParams.version && $routeParams.terminologyId) || $routeParams.id)) {

                  terminologies = $scope.lists.terminologies.filter(function(item) {
                    return item.terminology == $routeParams.terminology
                      && (!$routeParams.version || item.version == $routeParams.version);
                  });
                  if (terminologies && terminologies.length == 1) {
                    // set the terminology
                    $scope.setTerminology(terminologies[0]).then(function(data) {

                      // get the component
                      $scope.getComponent($routeParams);
                    });
                  } else if (terminologies && terminologies.length > 1) {
                    utilService.setError('Too many matching terminologies found');
                  } else {
                    utilService.setError('Terminology specified in URL not found');
                  }

                }

                // otherwise, specify the default terminology
                else {

                  var found = false;
                  if ($scope.user.userPreferences && $scope.user.userPreferences.lastTerminology) {
                    for (var i = 0; i < $scope.lists.terminologies.length; i++) {
                      var terminology = $scope.lists.terminologies[i];
                      // set from user prefs
                      if (terminology.terminology === $scope.user.userPreferences.lastTerminology) {
                        $scope.setTerminology(terminology);
                        found = true;
                        break;
                      }
                    }
                  }

                  // otherwise look for metathesaurus
                  if (!found) {
                    for (var i = 0; i < $scope.lists.terminologies.length; i++) {
                      var terminology = $scope.lists.terminologies[i];
                      // Determine whether to set as default
                      if (terminology.metathesaurus) {
                        $scope.setTerminology(terminology);
                        found = true;
                        break;
                      }
                    }
                  }

                  // If nothing set, pick the first one
                  if (!found) {

                    if (!$scope.lists.terminologies) {
                      window.alert('No terminologies found, database may not be properly loaded.');
                    } else {
                      $scope.setTerminology($scope.lists.terminologies[0]);
                    }
                  }
                }
              }

            });

        }

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          // configure tab
          securityService.saveTab($scope.user.userPreferences, '/content');

          $scope.configureExpressions();
          $scope.configureCallbacks();
          $scope.getProjects();

          //
          // Check for values preserved in content service (after route changes)
          //
          if (contentService.getLastSearchParams()) {
            $scope.searchParams = contentService.getLastSearchParams();
            $scope.findComponents(false, true);
          }

          if (contentService.getLastComponent()) {
            $scope.selected.component = contentService.getLastComponent();
            $scope.checkFavoriteStatus();
          }

          // Initialize metadata
          $scope.initMetadata();

        };

        //
        // Initialization: Check
        // (1) that application is configured, and
        // (2) that the license has been accepted (if required)
        //
        configureService.isConfigured().then(
        // Success
        function(isConfigured) {
          if (!isConfigured) {
            $location.path('/configure');
          } else {
            securityService.checkLicense().then(
            // Success
            function() {
              console.debug('License valid, initializing');
              $scope.initialize();
            },
            // Error
            function() {
              console.debug('Invalid license');
              utilService.setError('You must accept the license before viewing that content');
              $location.path('/license');
            });
          }
        });

      }

    ]);
