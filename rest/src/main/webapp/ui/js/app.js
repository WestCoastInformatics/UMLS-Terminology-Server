'use strict'

var baseUrl = '';
var securityUrl = baseUrl + 'security/';
var contentUrl = baseUrl + 'content/';
var metadataUrl = baseUrl + 'metadata/';
var historyUrl = baseUrl + 'history/';
var glassPane = 0;

var tsApp = angular.module('tsApp', [ 'ui.bootstrap', 'ui.tree' ]).config(
  function() {

  })

tsApp.run(function($http) {
  // nothing yet -- may want to put metadata retrieval here
})

tsApp.filter('highlight', function($sce) {
  return function(text, phrase) {
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span class="highlighted">$1</span>')

    return $sce.trustAsHtml(text)
  }
})

tsApp
  .controller(
    'tsIndexCtrl',
    [
      '$scope',
      '$http',
      '$q',
      function($scope, $http, $q) {

        // the default viewed terminology, if available
        var defaultTerminology = 'UMLS';

        $scope.$watch('component', function() {
          // // console.debug("Component changed to ",
          // $scope.component);
        });

        // the currently viewed terminology (set by default or user)
        $scope.terminology = null;
        $scope.metadata = null;

        // query base variables
        $scope.componentQuery = null;
        $scope.autocompleteUrl = null; // set on terminology change

        // query boolean variables for return types
        // add others here and update findComponents() method
        $scope.queryForList = true // whether to query for list, default
        $scope.queryForTree = false; // whether to query for tree

        // the displayed component
        $scope.component = null;
        $scope.componentType = null; // the type, e.g. CONCEPT
        $scope.componentTypePrefix = null; // the type previx, e.g. cui

        // basic scope variables
        $scope.userName = null;
        $scope.authToken = null;
        $scope.error = "";
        $scope.glassPane = 0;

        // labels
        $scope.atomsLabel = "Atoms";
        $scope.hierarchiesLabel = "Hierarchies";
        $scope.attributesLabel = "Attributes";
        $scope.definitionsLabel = "Definitions";
        $scope.subsetsLabel = "Subsets";
        $scope.relationshipsLabel = "Relationships";

        // full variable arrays
        $scope.searchResults = null;
        $scope.searchResultsTree = null;

        $scope.handleError = function(data, status, headers, config) {
          $scope.error = data.replace(/"/g, '');
        }

        $scope.clearError = function() {
          $scope.error = null;
        }

        $scope.setTerminology = function(terminology) {
          $scope.terminology = terminology;
          if (!$scope.terminology.metathesaurus) {
            $scope.showObsolete = false;
          }
        }

        /**
         * Watch selected terminology and perform necessary operations
         */
        $scope.$watch('terminology', function() {

          // clear the terminology-specific variables
          $scope.autoCompleteUrl = null;

          // if no terminology specified, stop
          if ($scope.terminology == null) {
            return;
          }

          // set the autocomplete url, with pattern:
          // /type/{terminology}/{version}/autocomplete/{searchTerm}
          $scope.autocompleteUrl = contentUrl
            + getTypePrefix($scope.terminology.organizingClassType) + '/'
            + $scope.terminology.terminology + '/' + $scope.terminology.version
            + "/autocomplete/";

          $scope.glassPane++;

          $http(
            {
              url : metadataUrl + 'all/terminology/id/'
                + $scope.terminology.terminology + '/'
                + $scope.terminology.version,
              method : "GET",
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            $scope.setMetadata(data.keyValuePairList);
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });

        })

        $scope.login = function(name, password) {

          if (name == null) {
            alert("You must specify a user name");
            return;
          } else if (password == null) {
            alert("You must specify a password");
            return;
          }

          // login
          $scope.glassPane++;
          // console.debug("Login called - " + securityUrl + 'authenticate/' +
          // name);
          $http({
            url : securityUrl + 'authenticate/' + name,
            dataType : "text",
            data : password,
            method : "POST",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {
            console.log(name + " = " + data);

            $scope.clearError();

            $scope.userName = name;
            $scope.authToken = data;
            $scope.password = "";

            // set request header
            // authorization
            $http.defaults.headers.common.Authorization = $scope.authToken;

            // retrieve available
            // terminologies
            $scope.getTerminologies();
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.logout = function() {
          console.log("logout - " + $scope.authToken.replace(/"/g, ""));
          if ($scope.authToken == null) {
            alert("You are not currently logged in");
            return;
          }
          $scope.glassPane++;
          // logout
          $http({
            url : securityUrl + 'logout/' + $scope.authToken.replace(/"/g, ""),
            method : "GET",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {

            // clear scope variables
            $scope.userName = null;
            $scope.authToken = null;

            // clear http authorization
            // header
            $http.defaults.headers.common.Authorization = null;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.autocomplete = function(searchTerms) {
          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined
            || searchTerms.length < 3) {
            return new Array();
          }

          var deferred = $q.defer();

          // NO GLASS PANE
          $http({
            url : $scope.autocompleteUrl + searchTerms,
            method : "GET",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {
            deferred.resolve(data.string);
          }).error(function(data, status, headers, config) {
            deferred.resolve(null); // hide errors

          });

          return deferred.promise;
        }

        $scope.getTerminology = function(name, version) {

          var deferred = $q.defer();
          setTimeout(function() {
            $scope.glassPane++;
            // login
            $http({
              url : metadataUrl + 'terminology/id/' + name + '/' + version,
              method : "GET",
              headers : {
                "Content-Type" : "text/plain"
              }
            }).success(function(data) {
              $scope.glassPane--;
              deferred.resolve(data);
            }).error(
              function(data, status, headers, config) {
                $scope.glassPane--;
                deferred.reject("Could not retrieve terminology " + name + ", "
                  + version);

              });
          });

          return deferred.promise;
        }

        /**
         * Get all available terminologies and store in scope.terminologies
         */
        $scope.getTerminologies = function() {

          // reset terminologies
          $scope.terminologies = null;
          $scope.glassPane++;

          // login
          $http({
            url : metadataUrl + 'terminology/terminologies',
            method : "GET",
            headers : {
              "Content-Type" : "text/plain"
            }
          })
            .success(
              function(data) {
                $scope.terminologies = new Array();
                // console
                // .debug(
                // "Retrieved terminologies:",
                // data.keyValuePairList);

                // results are in pair list, want full terminologies
                var ct = 0;
                for (var i = 0; i < data.keyValuePairList.length; i++) {
                  var pair = data.keyValuePairList[i].keyValuePair[0];

                  var terminologyObj = {
                    name : pair['key'],
                    version : pair['value']
                  };

                  // call helper function to get the full terminology object
                  var terminologyObj = $scope.getTerminology(pair['key'],
                    pair['value']);

                  terminologyObj
                    .then(
                      function(terminology) {

                        terminology.hidden = (terminology.terminology === 'MTH' || terminology.terminology === 'SRC');

                        // add result to the list of terminologies
                        $scope.terminologies.push(terminology);

                        if (terminology.metathesaurus) {
                          $scope.setTerminology(terminology);
                        }

                        if (++ct == data.keyValuePairList.length
                          && !$scope.terminology) {
                          // If a "metathesaurus" wasn't found, pick the first
                          if ($scope.terminologies[0]) {
                            $scope.setTerminology($scope.terminologies[0]);
                          }
                        }

                      }, function(reason) {
                        // do error message here
                      });

                }

                $scope.glassPane--;

              }).error(function(data, status, headers, config) {
              $scope.handleError(data, status, headers, config);
              $scope.glassPane--;
            });
        }

        /**
         * Function to get a component of the terminology's organizing class
         * type.
         */
        $scope.getComponent = function(terminologyName, terminologyId) {
          // if terminology matches scope terminology
          if (terminologyName === $scope.terminology.terminology) {
            getComponentHelper($scope.terminology, terminologyId,
              getTypePrefix($scope.terminology.organizingClassType));

            // otherwise get the terminology first
          } else {
            var localTerminology = getTerminologyFromName(terminologyName);
            getComponentHelper(localTerminology, terminologyId,
              getTypePrefix(localTerminology.organizingClassType));
          }
        }

        /**
         * Function to get a component based on type parameter
         */
        $scope.getComponentFromType = function(terminologyName, terminologyId,
          type) {
          // console.debug('getComponentFromType', terminologyName,
          // terminologyId, type);
          switch (type) {
          case 'CONCEPT':
            $scope.getConcept(terminologyName, terminologyId);
            break;
          case 'DESCRIPTOR':
            $scope.getDescriptor(terminologyName, terminologyId);
            break;
          case 'CODE':
            $scope.getCode(terminologyName, terminologyId);
            break;
          default:
            $scope.componentError = "Could not retrieve " + type + " for "
              + terminologyName + "/" + terminologyId;
          }
        }

        /**
         * Function to get a concept for a terminology. Does not trigger on
         * terminology class type.
         */
        $scope.getConcept = function(terminologyName, terminologyId) {

          console.debug('getConcept', terminologyName, terminologyId);

          // if terminology matches scope terminology
          if (terminologyName === $scope.terminology.terminology) {
            getComponentHelper($scope.terminology, terminologyId,
              getTypePrefix('CONCEPT'));

            // otherwise get the terminology first
          } else {
            var localTerminology = getTerminologyFromName(terminologyName);
            getComponentHelper(localTerminology, terminologyId,
              getTypePrefix('CONCEPT'));
          }
        }

        /**
         * Function to get a descriptor for a terminology. Does not trigger on
         * terminology class type.
         */
        $scope.getDescriptor = function(terminologyName, terminologyId) {

          console.debug('getDescriptor', terminologyName, terminologyId);

          // if terminology matches scope terminology
          if (terminologyName === $scope.terminology.terminology) {
            getComponentHelper($scope.terminology, terminologyId,
              getTypePrefix('DESCRIPTOR'));
          } else {
            var localTerminology = getTerminologyFromName(terminologyName);
            getComponentHelper(localTerminology, terminologyId,
              getTypePrefix('DESCRIPTOR'));
          }

        }

        /**
         * Function to get a code for a terminology. Does not trigger on
         * terminology class type;
         */
        $scope.getCode = function(terminologyName, terminologyId) {

          console.debug('getCode', terminologyName, terminologyId);

          // if terminology matches scope terminology
          if (terminologyName === $scope.terminology.terminology) {
            getComponentHelper($scope.terminology, terminologyId,
              getTypePrefix('CODE'));
          } else {
            var localTerminology = getTerminologyFromName(terminologyName);
            getComponentHelper(localTerminology, terminologyId,
              getTypePrefix('CODE'));
          }
        }

        /**
         * Helper function called by getConcept, getDescriptor, and getCode -
         * terminologyObj: the full terminology object from getTerminology() -
         * terminologyId: the terminology id of the component - typePrefix: the
         * url prefix denoting object type (cui/dui/code)
         */
        function getComponentHelper(terminologyObj, terminologyId, typePrefix) {

          $scope.componentType = getComponentTypeFromPrefix(typePrefix);
          $scope.componentTypePrefix = typePrefix;

          // console.debug('getComponentHelper', terminologyObj, terminologyId,
          // typePrefix, $scope.componentType);

          // clear existing component and paging
          $scope.component = null;
          $scope.componentError = null;
          clearPaging();

          if (!terminologyObj || !terminologyId || !typePrefix) {
            $scope.componentError = "An unexpected display error occurred. Click a concept or perform a new search to continue";
            return;
          }

          // get single concept
          $scope.glassPane++;
          $http(
            {
              url : contentUrl + typePrefix + "/" + terminologyObj.terminology
                + "/" + terminologyObj.version + "/" + terminologyId,
              method : "GET",

            }).success(
            function(data) {

              $scope.glassPane--;

              // update history
              $scope.addConceptToHistory(data.terminology, data.terminologyId,
                $scope.componentType, data.name);

              if (!data) {
                $scope.componentError = "Could not retrieve "
                  + $scope.componentType + " data for "
                  + terminologyObj.terminology + "/" + terminologyId;

                return;
              }

              // if local terminology matches passed terminology, attempt to set
              // active row
              if ($scope.terminology === terminologyObj)
                setActiveRow(terminologyId);

              // cycle over all atoms for pre-processing
              for (var i = 0; i < data.atom.length; i++) {

                // assign expandable content flag
                data.atom[i].hasContent = atomHasContent(data.atom[i]);

                // console.debug("Atom content", data.atom[i].hasContent,
                // data.atom[i]);

                // push any definitions up to top level
                for (var j = 0; j < data.atom[i].definition.length; j++) {
                  var definition = data.atom[i].definition[j];

                  // set the atom element flag
                  definition.atomElement = true;

                  // add the atom information for tooltip display
                  definition.atomElementStr = data.atom[i].name + " ["
                    + data.atom[i].terminology + "/" + data.atom[i].termType
                    + "]";

                  // add the definition to the top level component
                  data.definition.push(definition);
                }
              }

              // set the component
              $scope.setComponent(data, typePrefix);

            }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        // /////////////////////////////////
        // Tree Display
        // /////////////////////////////////

        $scope.treeCount = null;
        $scope.treeViewed = null;
        $scope.siblingPageSize = 10; // the number of siblings to display in
        // component hierarchy view

        /**
         * Function to get a single (paged) hierarchical tree for the displayed
         * component
         */
        $scope.getSingleTreeForComponent = function(component, typePrefix,
          startIndex) {

          console.debug('getSingleTreeForComponent', component, typePrefix,
            startIndex);

          $scope.glassPane++;

          if (startIndex === undefined) {
            console.debug('start index undefined')
            startIndex = 0;
          }

          var pfs = {
            startIndex : startIndex,
            maxResults : 1,
            sortField : 'ancestorPath',
            queryRestriction : null
          }

          $http(
            {
              url : contentUrl + typePrefix + '/' + component.terminology + '/'
                + component.version + '/' + component.terminologyId + '/trees',
              method : "POST",
              dataType : 'json',
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            $scope.glassPane--;

            $scope.componentTree = data.tree;

            // set the count and position variables
            $scope.treeCount = data.totalCount;
            if (data.count > 0)
              $scope.treeViewed = startIndex;
            else
              $scope.treeViewed = 0;

            // if parent tree cannot be read, clear the component tree
            // (indicates no hierarchy present)
            if ($scope.componentTree.length == 0) {
              $scope.componentTree = null;
              return;
            }

            // get the ancestor path of the bottom element (the component)
            // ASSUMES: unilinear path (e.g. A~B~C~D, no siblings)
            var parentTree = $scope.componentTree[0];
            while (parentTree.child.length > 0) {
              // check if child has no children
              if (parentTree.child[0].child.length == 0)
                break;
              parentTree = parentTree.child[0];
            }

            // replace the parent tree of the lowest level with first page of
            // siblings computed
            $scope.getAndSetTreeChildren(parentTree, 0);

          }).error(function(data, status, headers, config) {
            $scope.glassPane--;
            $scope.handleError(data, status, headers, config);

          });

        }

        /**
         * Helper function to get previous or next tree by offset
         */
        $scope.getTreeByOffset = function(offset) {

          console.debug('getTreeByOffset', $scope.treeViewed, offset,
            $scope.treeCount);
          var treeViewed = $scope.treeViewed + offset;

          // ensure number is in circular index
          if (!treeViewed)
            treeViewed = 0;
          if (treeViewed >= $scope.treeCount)
            treeViewed = treeViewed - $scope.treeCount;
          if (treeViewed < 0)
            treeViewed = treeViewed + $scope.treeCount;

          $scope.getSingleTreeForComponent($scope.component,
            $scope.componentTypePrefix, treeViewed);

        }

        /** Fake "enum" for clarity. Could use freeze, but meh */
        var TreeNodeExpansionState = {
          'Undefined' : -1,
          'Unloaded' : 0,
          'ExpandableFromNode' : 1,
          'ExpandableFromList' : 2,
          'Loaded' : 3
        };

        /**
         * Helper function to determine what icon to show for a tree node Case
         * 1: Has children, none loaded -> use right chevron Case 2: Has
         * children, incompletely loaded (below pagesize) -> expandable (plus)
         * Case 3: Has children, completely loaded (or above pagesize) -> not
         * expandable (right/down))
         */
        $scope.getTreeNodeExpansionState = function(tree) {

          console.debug('getTreeNodeExpansionState', tree);

          if (!tree)
            return null;

          // case 1: no children loaded, but children exist
          if (tree.childCt > 0 && tree.child.length == 0) {
            return TreeNodeExpansionState.Unloaded;
          }

          // case 2: some children loaded, not by user, expandable from node
          else if (tree.child.length < tree.childCt
            && tree.child.length < $scope.siblingPageSize) {
            return TreeNodeExpansionState.ExpandableFromNode;
          }

          // case 3: some children loaded by user, expandable from list
          else if (tree.child.length < tree.childCt
            && tree.child.length >= $scope.siblingPageSize) {
            return TreeNodeExpansionState.ExpandableFromList;
          }

          // case 4: all children loaded
          else if (tree.child.length == tree.childCt) {
            return TreeNodeExpansionState.Loaded;
          }

          else
            return TreeNodeExpansionState.Undefined;

        }

        $scope.getTreeNodeIcon = function(tree, collapsed) {

          console.debug('getTreeNodeIcon', tree, collapsed);

          // if childCt is zero, return leaf
          if (tree.childCt == 0)
            return 'glyphicon-leaf';

          // otherwise, switch on expansion state
          switch ($scope.getTreeNodeExpansionState(tree)) {
          case TreeNodeExpansionState.Unloaded:
            return 'glyphicon-chevron-right';
          case TreeNodeExpansionState.ExpandableFromNode:
            return 'glyphicon-plus';
          case TreeNodeExpansionState.ExpandableFromList:
          case TreeNodeExpansionState.Loaded:
            if (collapsed)
              return 'glyphicon-chevron-right';
            else
              return 'glyphicon-chevron-down';
          default:
            return 'glyphicon-question-sign';
          }
        }

        /**
         * Helper function to determine whether siblings are hidden on a
         * user-expanded list
         */
        $scope.hasHiddenSiblings = function(tree) {

          // TODO Identify strange bug causing non-tree objects to be passed in
          if (!tree || !tree.child)
            return;

          switch ($scope.getTreeNodeExpansionState(tree)) {
          case TreeNodeExpansionState.ExpandableFromList:
            return true;
          default:
            return false;
          }
        }

        /**
         * Function to determine whether to toggle children and/or retrieve
         * children if necessary
         */
        $scope.getTreeChildren = function(tree, treeHandleScope) {

          console.debug('toggleChildren', tree);

          switch ($scope.getTreeNodeExpansionState(tree)) {

          // if fully loaded or expandable from list, simply toggle
          case TreeNodeExpansionState.Loaded:
          case TreeNodeExpansionState.ExpandableFromList:
            console.debug("expanded by user or fully loaded, toggling")
            treeHandleScope.toggle();
            return;

          default:
            console.debug("node expansion, retrieving from beginning");
            $scope.getAndSetTreeChildren(tree, 0); // type prefix auto set

          }
        }

        /** Get a tree node's children */
        $scope.getAndSetTreeChildren = function(tree, startIndex) {

          if (!tree) {
            console.error("Can't set tree children without tree!")
            return;
          }

          // set default for startIndex if not specified
          if (!startIndex)
            startIndex = 0;

          // get the type prefix for displayed component
          var typePrefix = getTypePrefix($scope.componentType);

          console.debug("getAndSetTreeChildren", tree, typePrefix);

          // NOTE: Currently hard-coded to only return siblingPageSize items
          var pfs = getPfs();
          pfs.startIndex = startIndex;
          pfs.maxResults = $scope.siblingPageSize;

          $scope.glassPane++;

          // @Path("/cui/{terminology}/{version}/{terminologyId}/trees/children")
          $http(
            {
              url : contentUrl + typePrefix + '/' + tree.terminology + '/'
                + tree.version + '/' + tree.terminologyId + '/trees/children',
              method : "POST",
              dataType : 'json',
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            })
            .success(
              function(data) {
                $scope.glassPane--;

                console.debug('children received: ', data)

                // construct ancestor path (for sake of completeness, not filled
                // in on server-side)
                var ancestorPath = tree.ancestorPath + '~' + tree.terminologyId;

                // cycle over children, and construct tree nodes
                for (var i = 0; i < data.tree.length; i++) {

                  // check that child is not already present (don't override
                  // present data)
                  var childPresent = false;
                  for (var j = 0; j < tree.child.length; j++) {
                    if (tree.child[j].terminologyId === data.tree[i].terminologyId) {
                      childPresent = true;
                      break;
                    }
                  }

                  // if not present, add
                  if (!childPresent) {
                    tree.child.push(data.tree[i]);
                  }
                }

                tree.childrenRetrieved = true; // currently unused

              }).error(function(data, status, headers, config) {
              $scope.glassPane--;
              $scope.handleError(data, status, headers, config);

            });

        }

        /**
         * Clear the search box and perform any additional operations required
         */
        $scope.clearQuery = function() {
          $scope.suggestions = null;
          $scope.componentQuery = null;
        }

        // ///////////////////////////////////////////
        // Search Results View
        // ///////////////////////////////////////////

        /**
         * Functions to set the results list view
         */
        $scope.setTreeView = function() {
          $scope.queryForTree = true;
          $scope.queryForList = false;

          $scope.findComponentsAsTree($scope.componentQuery);
        }

        $scope.setListView = function() {
          $scope.queryForList = true;
          $scope.queryForTree = false;

          $scope.findComponentsAsList($scope.componentQuery);
        }

        /**
         * Find concepts based on current search type e.g. list or tree based on
         * booleans
         */
        $scope.findComponents = function(queryStr, page) {

          if ($scope.queryForList)
            $scope.findComponentsAsList(queryStr, page);
          if ($scope.queryForTree)
            $scope.findComponentsAsTree(queryStr, page);

        }

        /**
         * Find concepts based on terminology and queryStr Expected return type
         * is List Does not currently use any p/f/s settings NOTE: Always uses
         * the selected terminology
         */
        $scope.findComponentsAsList = function(queryStr, page) {

          console.debug('find concepts', queryStr);

          if (!page)
            page = 1;

          // ensure query string has minimum length
          if (queryStr == null || queryStr.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }

          clearPaging();

          // force the search box to sync with query string
          $scope.componentQuery = queryStr;

          // TODO Enable paging
          var pfs = {
            startIndex : (page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : null,
            queryRestriction : null
          }

          // find concepts
          $scope.glassPane++;
          $http(
            {
              url : contentUrl
                + getTypePrefix($scope.terminology.organizingClassType) + "/"
                + $scope.terminology.terminology + "/"
                + $scope.terminology.version + "/query/"
                + encodeURIComponent(queryStr),
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(
            function(data) {
              // console.debug("Retrieved concepts:", data);
              $scope.searchResults = data.searchResult;
              $scope.searchResults.totalCount = data.totalCount;

              // select the first component if results returned
              if ($scope.searchResults.length != 0)
                $scope.getComponent($scope.terminology.terminology,
                  $scope.searchResults[0].terminologyId);

              $scope.glassPane--;

            }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        /**
         * Helper function to browse terminology
         */
        $scope.browseHierarchy = function(page) {
          $scope.queryForTree = true;
          $scope.queryForList = false
          $scope.browsingHierarchy = true;

          if (!page)
            page = 1;

          // construct the pfs
          var pfs = {
            startIndex : (page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : null,
            queryRestriction : null
          }

          // find concepts
          $scope.glassPane++;
          $http(
            {
              url : contentUrl
                + getTypePrefix($scope.terminology.organizingClassType) + "/"
                + $scope.terminology.terminology + "/"
                + $scope.terminology.version + "/trees/roots",
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved component trees:", data);

            // for ease and consistency of use of the ui tree directive
            // force the single tree into a ui-tree data structure with count
            // variables
            $scope.searchResultsTree = [];
            $scope.searchResultsTree.push(data); // treeList array of size 1
            $scope.searchResultsTree.totalCount = data.totalCount;
            $scope.searchResultsTree.count = data.count;

            console.debug($scope.searchResultsTree);

            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        /**
         * Performs query Find concepts based on terminology and queryStr
         * Expected return type is List Does not currently use any p/f/s
         * settings NOTE: Always uses the selected terminology
         */
        $scope.findComponentsAsTree = function(queryStr, page) {
          console.debug('findComponentsTree', queryStr);

          if (!page)
            page = 1;

          // ensure query string has minimum length
          if (!queryStr || queryStr.length < 1) {
            alert("You must use at least three characters to search");
            return;
          }

          clearPaging();

          // force the search box to sync with query string
          $scope.componentQuery = queryStr;

          // construct the pfs
          var pfs = {
            startIndex : (page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : null,
            queryRestriction : null
          } // 'terminologyId:' + queryStr }

          // find concepts
          $scope.glassPane++;
          $http(
            {
              url : contentUrl
                + getTypePrefix($scope.terminology.organizingClassType) + "/"
                + $scope.terminology.terminology + "/"
                + $scope.terminology.version + "/trees/query/"
                + encodeURIComponent(queryStr),
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved component trees:", data);

            // for ease and consistency of use of the ui tree directive
            // force the single tree into a ui-tree structure with count
            // variables
            $scope.searchResultsTree = [];
            $scope.searchResultsTree.push(data); // treeList array of size 1
            $scope.searchResultsTree.totalCount = data.totalCount;
            $scope.searchResultsTree.count = data.count;

            console.debug($scope.searchResultsTree);

            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        /**
         * Sets the component and performs any operations required
         */
        $scope.setComponent = function(component, typePrefix) {

          // set the component
          $scope.component = component;

          // get the initial tree
          $scope.getSingleTreeForComponent(component, typePrefix, 0);

          // apply the initial paging
          applyPaging();
        }

        // /////////////////////////////
        // Show/Hide List Elements
        // /////////////////////////////

        // variables for showing/hiding elements based on boolean fields
        $scope.showSuppressible = true;
        $scope.showObsolete = true;
        $scope.showAtomElement = true;
        $scope.showInferred = true;

        /**
         * Determine if an item has boolean fields set to true in its child
         * arrays
         */
        $scope.hasBooleanFieldTrue = function(object, fieldToCheck) {

          // check for proper arguments
          if (object == null || object == undefined)
            return false;

          // cycle over all properties
          for ( var prop in object) {
            var value = object[prop];

            // if null or undefined, skip
            if (value == null || value == undefined) {
              // do nothing
            }

            // if an array, check the array's objects
            else if (Array.isArray(value) == true) {
              for (var i = 0; i < value.length; i++) {
                if (value[i][fieldToCheck] == true) {
                  return true;
                }
              }
            }

            // if not an array, check the object itself
            else if (value.hasOwnProperty(fieldToCheck)
              && value[fieldToCheck] == true) {
              return true;
            }

          }

          // default is false
          return false;
        }

        /**
         * Helper function to determine whether an item should be shown based on
         * obsolete/suppressed
         */
        $scope.showItem = function(item) {

          // trigger on suppressible (model data)
          if ($scope.showSuppressible == false && item.suppressible == true)
            return false;

          // trigger on obsolete (model data)
          if ($scope.showObsolete == false && item.obsolete == true)
            return false;

          // trigger on applied showAtomElement flag
          if ($scope.showAtomElement == false && item.atomElement == true)
            return false;

          // trigger on inferred flag
          if ($scope.terminology.descriptionLogicTerminology
            && item.hasOwnProperty('stated') && $scope.showInferred
            && item.stated)
            return false;
          if ($scope.terminology.descriptionLogicTerminology
            && item.hasOwnProperty('inferred') && !$scope.showInferred
            && item.inferred)
            return false;

          return true;
        }

        /** Function to toggle obsolete flag and apply paging */
        $scope.toggleObsolete = function() {
          if ($scope.showObsolete == null || $scope.showObsolete == undefined) {
            $scope.showObsolete = false;
          } else {
            $scope.showObsolete = !$scope.showObsolete;
          }

          applyPaging();

        }

        /** Function to toggle suppressible flag and apply paging */
        $scope.toggleSuppressible = function() {
          if ($scope.showSuppressible == null
            || $scope.showSuppressible == undefined) {
            $scope.showSuppressible = false;
          } else {
            $scope.showSuppressible = !$scope.showSuppressible;
          }

          applyPaging();
        }

        /** Function to toggle atom element flag and apply paging */
        $scope.toggleAtomElement = function() {
          if ($scope.showAtomElement == null
            || $scope.showAtomElement == undefined) {
            $scope.showAtomElement = false;
          } else {
            $scope.showAtomElement = !$scope.showAtomElement;
          }

          applyPaging();
        }

        /** Function to toggle inferred flag and apply paging */
        $scope.toggleInferred = function() {
          if ($scope.showInferred == null || $scope.showInferred == undefined) {
            $scope.showInferred = false;
          } else {
            $scope.showInferred = !$scope.showInferred;
          }
          applyPaging();
        }

        // /////////////////////////////
        // Expand/Collapse functions
        // /////////////////////////////
        $scope.toggleItemCollapse = function(item) {
          item.expanded = !item.expanded;
        }

        // Return true/false whether an atom has expandable content
        function atomHasContent(atom) {
          // console.debug('atomHasContent', atom);
          if (!atom)
            return false;
          if (atom.attribute.length > 0)
            return true;
          if (atom.definition.length > 0)
            return true;
          if (atom.relationship.length > 0)
            return true;
          return false;
        }

        // Returns the css class for an item's collapsible control
        $scope.getCollapseIcon = function(item) {

          // console.debug('getCollapseIcon', item.hasContent, item.expanded,
          // item);

          // if no expandable content detected, return blank glyphicon (see
          // tsMobile.css)
          if (!item.hasContent)
            return 'glyphicon glyphicon-plus glyphicon-none';

          // return plus/minus based on current expanded status
          if (item.expanded)
            return 'glyphicon glyphicon-minus';
          else
            return 'glyphicon glyphicon-plus';
        }

        // /////////////////////////////
        // Misc helper functions
        // /////////////////////////////

        /** Set selected item to active row (for formatting purposes */
        function setActiveRow(terminologyId) {
          if (!$scope.searchResults || $scope.searchResults.length == 0)
            return;
          for (var i = 0; i < $scope.searchResults.length; i++) {
            if ($scope.searchResults[i].terminologyId === terminologyId) {
              $scope.searchResults[i].active = true;
            } else {
              $scope.searchResults[i].active = false;
            }
          }
        }

        /** Construct a default PFS object */
        function getPfs(page) {
          if (!page)
            page = 1;
          return {
            startIndex : (page - 1) * $scope.pageSize,
            maxResults : $scope.pageSize,
            sortField : null,
            queryRestriction : null
          };
        }

        /** Helper function to get the proper html prefix based on class type */
        function getTypePrefix(classType) {

          switch (classType) {
          case 'CONCEPT':
            return 'cui';
          case 'DESCRIPTOR':
            return 'dui';
          case 'CODE':
            return 'code';
          default:
            return 'prefixErrorDetected';
          }

        }

        /** Helper function to get the component type from the url prefix */
        function getComponentTypeFromPrefix(prefix) {
          switch (prefix) {
          case 'cui':
            return 'CONCEPT';
          case 'dui':
            return 'DESCRIPTOR';
          case 'code':
            return 'CODE';
          default:
            return 'UNKNOWN COMPONENT';
          }
        }

        /** Helper function to get properties for ng-repeat */
        function convertObjectToJsonArray() {
          var newArray = new Array();
          for ( var prop in object) {
            var obj = {
              key : prop,
              value : object[prop]
            };
            newArray.push(obj);
          }
        }

        /** Get the organizing class type from a terminology name */
        $scope.getOrganizingClassType = function(terminologyName) {

          if (!terminologyName)
            return null;

          var terminology = getTerminologyFromName(terminologyName);
          if (!terminology) {
            return "ClassTypeUnknown";
          }
          return terminology.organizingClassType;
        }

        /**
         * Function to filter viewable terminologies for picklist
         */
        $scope.getViewableTerminologies = function() {
          var viewableTerminologies = new Array();
          if (!$scope.terminologies) {
            return viewableTerminologies;
          }
          for (var i = 0; i < $scope.terminologies.length; i++) {
            // exclude MTH and SRC
            if ($scope.terminologies[i].terminology != 'MTH'
              && $scope.terminologies[i].terminology != 'SRC')
              viewableTerminologies.push($scope.terminologies[i])
          }
          return viewableTerminologies;
        }

        /**
         * Helper function to get full terminology object given terminology name
         */
        function getTerminologyFromName(terminologyName) {
          // check for full terminology object by comparing to selected
          // terminology
          if (terminologyName != $scope.terminology.terminology) {

            // cycle over available terminologies for match
            for (var i = 0; i < $scope.terminologies.length; i++) {
              if ($scope.terminologies[i].terminology === terminologyName) {
                return $scope.terminologies[i];
              }
            }
          } else {
            return $scope.terminology;
          }
        }

        // ////////////////////////////////////
        // Metadata Helper Functions
        // ////////////////////////////////////

        var relationshipTypes = [];
        var attributeNames = [];
        var termTypes = [];
        var generalEntries = [];
        var markerSets = [];

        // on metadata changes
        $scope.setMetadata = function(terminology) {

          // reset arrays
          relationshipTypes = [];
          attributeNames = [];
          termTypes = [];
          generalEntries = [];
          markerSets = [];
          $scope.metadata = terminology;

          if (terminology == null)
            return;
          
          for (var i = 0; i < $scope.metadata.length; i++) {
            // extract relationship types for convenience
            if ($scope.metadata[i].name === 'Relationship_Types') {
              relationshipTypes = $scope.metadata[i].keyValuePair;
            }
            if ($scope.metadata[i].name === 'Attribute_Names') {
              attributeNames = $scope.metadata[i].keyValuePair;
            }
            if ($scope.metadata[i].name === 'Term_Types') {
            	termTypes = $scope.metadata[i].keyValuePair;
            }
            if ($scope.metadata[i].name === 'Marker_Sets') {
            	markerSets = $scope.metadata[i].keyValuePair;
            }
            if ($scope.metadata[i].name === 'General_Metadata_Entries') {
              generalEntries = $scope.metadata[i].keyValuePair;

              for (var j = 0; j < generalEntries.length; j++) {
                if (generalEntries[j].key === "Atoms_Label") {
                  $scope.atomsLabel = generalEntries[j].value;
                }
                if (generalEntries[j].key === "Hierarchies_Label") {
                  $scope.hierarchiesLabel = generalEntries[j].value;
                }
                if (generalEntries[j].key === "Definitions_Label") {
                  $scope.definitionsLabel = generalEntries[j].value;
                }
                if (generalEntries[j].key === "Attributes_Label") {
                  $scope.attributesLabel = generalEntries[j].value;
                }
                if (generalEntries[j].key === "Subsets_Label") {
                  $scope.subsetsLabel = generalEntries[j].value;
                }
                if (generalEntries[j].key === "Relationships_Label") {
                  $scope.relationshipsLabel = generalEntries[j].value;
                }
              }

            }
          }
        }

        // get relationship type name from its abbreviation
        $scope.getRelationshipTypeName = function(abbr) {
          for (var i = 0; i < relationshipTypes.length; i++) {
            if (relationshipTypes[i].key === abbr) {
              return relationshipTypes[i].value;
            }
          }
          return null
        }

        // get attribute name name from its abbreviation
        $scope.getAttributeNameName = function(abbr) {
          for (var i = 0; i < attributeNames.length; i++) {
            if (attributeNames[i].key === abbr) {
              return attributeNames[i].value;
            }
          }
          return null
        }

        // get term type name from its abbreviation
        $scope.getTermTypeName = function(abbr) {
          for (var i = 0; i < termTypes.length; i++) {
            if (termTypes[i].key === abbr) {
              return termTypes[i].value;
            }
          }
          return null
        }

        // get general entry name from its abbreviation
        $scope.getGeneralEntryValue = function(abbr) {
            for (var i = 0; i < generalEntries.length; i++) {
              if (generalEntries[i].key === abbr) {
                return generalEntries[i].value;
              }
            }
            return null
          }

        $scope.getMarkerSetName = function(abbr) {
            for (var i = 0; i < markerSets.length; i++) {
              if (markerSets[i].key === abbr) {
                return markerSets[i].value;
              }
            }
            return null
          }

        $scope.getMarkerSetsValue = function(tree) {
        	if (tree.markerSets == undefined) {
        		console.debug("Undefined marker sets.");
        		return;
        	}
        	if (tree.markerSets.length == 1) {
        		return "Ancestor of content in:<br>&#x2022;&nbsp;" + 
        		$scope.getMarkerSetName(tree.markerSets[0]);
        		
        	}
        	var retVal = "Ancestor of content in:<br>";
            for (var i = 0; i < tree.markerSets.length; i++) {
            	if (i > 0) {
            		retval += "<br>";
            	}
            	retVal += "&#x2022;&nbsp;" + $scope.getMarkerSetName(tree.markerSets[i]);
            }
        }
        
        
        // ////////////////////////////////////
        // Navigation History
        // ////////////////////////////////////

        // concept navigation variables
        $scope.componentHistory = [];
        $scope.componentHistoryIndex = -1; // index is the actual array index
        // (e.g. 0:n-1)

        // add a terminology/terminologyId pair to the history stack
        $scope.addConceptToHistory = function(terminology, terminologyId, type,
          name) {

          console.debug("Adding concept to history", terminology,
            terminologyId, type, name);

          // if history exists
          if ($scope.componentHistoryIndex != -1) {

            // if this component currently viewed, do not add
            if ($scope.componentHistory[$scope.componentHistoryIndex].terminology === terminology
              && $scope.componentHistory[$scope.componentHistoryIndex].terminologyId === terminologyId)
              return;
          }

          // add item and set index to last
          $scope.componentHistory.push({
            'terminology' : terminology,
            'terminologyId' : terminologyId,
            'type' : type,
            'name' : name,
            'index' : $scope.componentHistory.length
          });
          $scope.componentHistoryIndex = $scope.componentHistory.length - 1;
        }

        // local history variables for dorp down list
        $scope.localHistory = null;
        $scope.localHistoryPageSize = 10; // NOTE: must be even number!
        $scope.localHistoryPreviousCt = 0;
        $scope.localHistoryNextCt = 0;

        // get the local history for the currently viewed concept
        $scope.$watch('componentHistoryIndex', function() {
          console.debug('componentHistoryIndex changed');

          setComponentLocalHistory($scope.componentHistoryIndex);
        });

        /**
         * Function to set the local history for drop down list based on an
         * index For cases where history > page size, returns array [index -
         * pageSize / 2 + 1 : index + pageSize]
         */
        function setComponentLocalHistory(index) {

          console.debug('getting local history', index,
            $scope.componentHistory.length, $scope.localHistoryPageSize);

          // if not a full page of history, simply set to component history and
          // stop
          if ($scope.componentHistory.length <= $scope.localHistoryPageSize) {
            $scope.localHistory = $scope.componentHistory;
            return;
          }

          // get upper bound
          var upperBound = Math.min(index + $scope.localHistoryPageSize / 2,
            $scope.componentHistory.length);
          var lowerBound = Math
            .max(upperBound - $scope.localHistoryPageSize, 0);

          // resize upper bound to ensure full page (for cases near beginning of
          // history)
          upperBound = lowerBound + $scope.localHistoryPageSize;

          // calculate unshown element numbers
          $scope.localHistoryNextCt = $scope.componentHistory.length
            - upperBound;
          $scope.localHistoryPreviousCt = lowerBound;

          console.debug('indices', lowerBound, upperBound, 'remaining',
            $scope.localHistoryPreviousCt, $scope.localHistoryNextCt);

          // return the local history
          $scope.localHistory = $scope.componentHistory.slice(lowerBound,
            upperBound);
        }
        ;

        $scope.getComponentFromHistory = function(index) {

          // if currently viewed do nothing
          if (index === $scope.componentHistoryIndex)
            return;

          // set the index and get the component from history information
          $scope.componentHistoryIndex = index;
          $scope
            .getComponentFromType(
              $scope.componentHistory[$scope.componentHistoryIndex].terminology,
              $scope.componentHistory[$scope.componentHistoryIndex].terminologyId,
              $scope.componentHistory[$scope.componentHistoryIndex].type);
        }

        $scope.getComponentStr = function(component) {
          if (!component)
            return null;

          return component.terminology + "/" + component.terminologyId + " "
            + component.type + ": " + component.name;
        }

        // UNTESTED
        $scope.viewHistoryInTable = function() {
          var searchResults = [];

          for (var i = 0; i < $scope.componentHistory.length; i++) {
            var comp = $scope.componentHistory[i];
            var searchResult = {
              'terminology' : comp['terminology'],
              'version' : comp['version'],
              'name' : comp['name']
            }
            searchResults.push(searchResult);
          }

          $scope.searchResults = searchResults;
          $scope.pagedSearchResults = $scope.getPagedArray(
            $scope.searchResults, 1, false, null);
        }

        // UNTESTED
        $scope.clearHistory = function() {
          $scope.componentHistory = [];
          $scope.componentHistoryIndex = -1;

          // set currently viewed item as first history item
          $scope.addConceptToHistory($scope.component.terminology,
            $scope.component.terminologyId, $scope.componentType,
            $scope.component.name);
        }
        // //////////////////////////////////
        // Pagination
        // //////////////////////////////////

        // paged variable lists
        // NOTE: Each list must have a totalCount variable
        // either from ResultList object or calculated
        $scope.pagedSearchResults = null;
        $scope.pagedAttributes = null;
        $scope.pagedMembers = null;
        $scope.pagedSemanticTypes = null;
        $scope.pagedDescriptions = null;
        $scope.pagedRelationships = null;
        $scope.pagedAtoms = null;

        // variable page numbers
        $scope.searchResultsPage = 1;
        $scope.semanticTypesPage = 1;
        $scope.definitionsPage = 1;
        $scope.relationshipsPage = 1;
        $scope.atomsPage = 1;

        // variable filter variables
        $scope.semanticTypesFilter = null;
        $scope.descriptionsFilter = null;
        $scope.relationshipsFilter = null;
        $scope.atomsFilter = null;
        $scope.attributesFilter = null;
        $scope.membersFilter = null;

        // default page size
        $scope.pageSize = 10;
        $scope.relsPageSize = 10;
        $scope.treePageSize = 5;

        // reset all paginator pages
        function clearPaging() {
          $scope.searchResultsPage = 1;
          $scope.semanticTypesPage = 1;
          $scope.definitionsPage = 1;
          $scope.relationshipsPage = 1;
          $scope.atomsPage = 1;
          $scope.attributesPage = 1;
          $scope.membersPage = 1;

          $scope.semanticTypesFilter = null;
          $scope.descriptionsFilter = null;
          $scope.relationshipsFilter = null;
          $scope.atomsFilter = null;
          $scope.attributesFilter = null;
          $scope.membersFilter = null;

        }

        // apply paging to all elements
        function applyPaging() {

          // call each get function without paging (use current paging info)
          $scope.getPagedAtoms();
          $scope.getPagedRelationships();
          $scope.getPagedDefinitions();
          $scope.getPagedAttributes();
          $scope.getPagedMembers();
          $scope.getPagedSemanticTypes();

        }

        // ///////////////////////////////////////////////////////////////
        // Server-side Paging
        // Functions for arrays paged by server-side find methods
        // ///////////////////////////////////////////////////////////////
        $scope.getPagedRelationships = function(page, query) {

          if (!page)
            page = 1;

          // hack for wildcard searching, may impair other lucene functionality
          if (query) {
            // append wildcard to end of query string if not present and not
            // quoted
            if (query.indexOf("*") == -1 && query.indexOf("\"") == -1) {
              query = query + "*";
            }
          }
          if (!query)
            query = "~BLANK~";

          var typePrefix = getTypePrefix($scope.componentType);
          var pfs = getPfs(page);

          // Show only inferred rels for now
          // construct query restriction if needed
          // TODO Change these to use pfs object parameters
          var qr = '';
          if ($scope.showSuppressible == false) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if ($scope.showObsolete == false) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }
          if ($scope.showInferred == true) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'inferred:true';
          }
          if ($scope.showInferred == false) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'stated:true';
          }
          pfs['queryRestriction'] = qr;
          pfs['sortField'] = 'relationshipType';

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if ($scope.terminology.descriptionLogicTerminology) {
            console.debug('Read all relationships');
            pfs['startIndex'] = -1;
            $scope.relsPageSize = 100000000;
          } else {
            $scope.relsPageSize = $scope.pageSize;
          }

          $scope.glassPane++;
          $http(
            {
              url : contentUrl + typePrefix + "/"
                + $scope.component.terminology + "/" + $scope.component.version
                + "/" + $scope.component.terminologyId
                + "/relationships/query/" + query,
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {

            // if description logic terminology, sort relationships also by
            // group
            if ($scope.terminology.descriptionLogicTerminology) {
              data.relationship.sort(function(a, b) {
                if (a.relationshipType < b.relationshipType)
                  return -1;
                if (a.relationshipType > b.relationshipType)
                  return 1;
                if (a.group < b.group)
                  return -1;
                if (a.group > b.group)
                  return 1;
                return 0;
              });
            }

            $scope.pagedRelationships = data.relationship;
            $scope.pagedRelationships.totalCount = data.totalCount;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        // //////////////////////////////////////////////////////////////
        // Client-side Paging
        // Functions for arrays retrieved in full, then paged by js.
        // //////////////////////////////////////////////////////////////
        $scope.getPagedAtoms = function(page, query) {

          // set the page if supplied, otherwise use the current value
          if (page)
            $scope.atomsPage = page;
          if (!query)
            query = null;

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedAtoms = $scope.getPagedArray($scope.component.atom,
            $scope.atomsPage, true, query);
        }

        $scope.getPagedDefinitions = function(page, query) {

          console.debug('paged definitions', page, $scope.definitionsPage);

          // set the page if supplied, otherwise use the current value
          if (page)
            $scope.definitionsPage = page;
          if (!query)
            query = null;

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedDefinitions = $scope.getPagedArray(
            $scope.component.definition, $scope.definitionsPage, true, query,
            'value', false);
        }

        $scope.getPagedAttributes = function(page, query) {

          // set the page if supplied, otherwise use the current value
          if (page)
            $scope.attributesPage = page;
          if (!query)
            query = null;

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedAttributes = $scope.getPagedArray(
            $scope.component.attribute, $scope.attributesPage, true, query,
            'name', false);

        }

        $scope.getPagedMembers = function(page, query) {

          // set the page if supplied, otherwise use the current value
          if (page)
            $scope.membersPage = page;
          if (!query)
            query = null;

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedMembers = $scope.getPagedArray($scope.component.member,
            $scope.membersPage, true, query, 'name', false);
        }

        $scope.getPagedSemanticTypes = function(page, query) {

          // set the page if supplied, otherwise use the current value
          if (page)
            $scope.semanticTypesPage = page;

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedSemanticTypes = $scope.getPagedArray(
            $scope.component.semanticType, $scope.semanticTypesPage, true,
            null, 'semanticType', false);
        }

        /**
         * Get a paged array with show/hide flags (ENABLED) and filtered by
         * query string (NOT ENABLED)
         */
        $scope.getPagedArray = function(array, page, applyFlags, filterStr,
          sortField, ascending) {

          console.debug('getPagedArray', page, applyFlags, filterStr);

          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined
            || Array.isArray(array) == false)
            return newArray;

          // apply page 1 if not supplied
          if (!page)
            page = 1;

          newArray = array;

          // apply sort if specified
          if (sortField) {
            // if ascending specified, use that value, otherwise use false
            newArray.sort($scope.sort_by(sortField, ascending ? ascending
              : false))
          }

          // apply flags
          if (applyFlags) {
            newArray = getArrayByFlags(newArray);
          }

          // apply filter
          if (filterStr) {
            console.debug('filter detected', filterStr, newArray);
            newArray = getArrayByFilter(newArray, filterStr);
          }

          // get the page indices
          var fromIndex = (page - 1) * $scope.pageSize;
          var toIndex = Math.min(fromIndex + $scope.pageSize, array.length);

          // slice the array
          var results = newArray.slice(fromIndex, toIndex);

          // add the total count before slicing
          results.totalCount = newArray.length;

          // console.debug(" results", results.totalCount, fromIndex, toIndex,
          // results);

          return results;
        }

        /** function for sorting an array by (string) field and direction */
        $scope.sort_by = function(field, reverse) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field]
          };

          // convert reverse to integer (1 = ascending, -1 = descending)
          reverse = !reverse ? 1 : -1;

          return function(a, b) {
            return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
          }
        }

        /** Filter array by show/hide flags */
        function getArrayByFlags(array) {

          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined
            || Array.isArray(array) == false)
            return newArray;

          // apply show/hide flags via showItem() function
          for (var i = 0; i < array.length; i++) {
            if ($scope.showItem(array[i]) == true) {
              newArray.push(array[i]);
            }
          }

          return newArray;
        }

        /** Get array by filter text matching terminologyId or name */
        function getArrayByFilter(array, filter) {
          var newArray = [];

          console.debug('getArrayByFilter', array, filter);
          for ( var object in array) {

            if (objectContainsFilterText(array[object], filter)) {
              console.debug('pushing object');
              newArray.push(array[object]);
            }
          }
          return newArray;
        }

        /** Returns true if any field on object contains filter text */
        function objectContainsFilterText(object, filter) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            var value = object[prop];

            console.debug('checking', value.toString().toLowerCase(), filter
              .toLowerCase());

            // check property for string, note this will cover child elements
            // TODO May want to make this more restrictive?
            if (value
              && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        }

      } ]);
