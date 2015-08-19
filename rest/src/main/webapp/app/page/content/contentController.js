// Content controller
tsApp
  .controller(
    'ContentCtrl',
    [
      '$scope',
      '$http',
      '$modal',
      '$location',
      '$anchorScroll',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'metadataService',
      'contentService',
      function($scope, $http, $modal, $location, $anchorScroll, gpService,
        utilService, tabService, securityService, metadataService,
        contentService) {
        console.debug('configure ContentCtrl');
        
        // Handle resetting tabs on "back" button
        if (tabService.selectedTab.label != 'Content') {
          tabService.setSelectedTabByLabel('Content');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        $scope.metadata = metadataService.getModel();
        $scope.user = securityService.getUser();
        $scope.component = contentService.getModel();
        $scope.pageSizes = contentService.getPageSizes();

        // Search parameters
        $scope.searchParams = contentService.getSearchParams();
        $scope.searchResults = contentService.getSearchResults();

        // set on terminology change
        $scope.autocompleteUrl = null;

        // Track search results and what type we are querying for
        $scope.queryForList = true // whether to query for list, default
        $scope.queryForTree = false; // whether to query for tree

        // Variables for iterating through trees in report
        $scope.treeCount = null;
        $scope.treeViewed = null;
        $scope.componentTree = null;

        //
        // Watch expressions
        //

        // Watch for changes in metadata.terminology
        $scope
          .$watch(
            'metadata.terminology',
            function() {
              // clear the terminology-specific variables
              $scope.autoCompleteUrl = null;

              // if no terminology specified, stop
              if ($scope.metadata.terminology == null) {
                return;
              }

              // set the autocomplete url, with pattern:
              // /type/{terminology}/{version}/autocomplete/{searchTerm}
              $scope.autocompleteUrl = contentUrl
                + contentService
                  .getPrefixForType($scope.metadata.terminology.organizingClassType)
                + '/' + $scope.metadata.terminology.terminology + '/'
                + $scope.metadata.terminology.version + "/autocomplete/";

              // metadataService.setTerminology($scope.metadata.terminology);

            });

        //
        // General
        //

        // Sets the terminololgy
        $scope.setTerminology = function(terminology) {
          metadataService.setTerminology(terminology);
        }

        // Autocomplete function
        $scope.autocomplete = function(searchTerms) {
          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined
            || searchTerms.length < 3) {
            return new Array();
          }
          return contentService.autocomplete(searchTerms,
            $scope.autocompleteUrl);
        }

        // 
        // Supporting trees
        // 

        // Function to get a single (paged) hierarchical tree for the displayed
        $scope.getTree = function(startIndex) {
          // Call content service to retrieve the tree
          contentService.getTree($scope.component.object.terminologyId,
            $scope.component.object.terminology,
            $scope.component.object.version, startIndex).then(function(data) {

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
            $scope.getAndSetChildTrees(parentTree, 0);

          });

        }

        // Helper function to get previous or next tree by offset
        $scope.getTreeByOffset = function(offset) {

          var treeViewed = $scope.treeViewed + offset;

          // ensure number is in circular index
          if (!treeViewed)
            treeViewed = 0;
          if (treeViewed >= $scope.treeCount)
            treeViewed = treeViewed - $scope.treeCount;
          if (treeViewed < 0)
            treeViewed = treeViewed + $scope.treeCount;

          $scope.getTree(treeViewed);

        }

        /** Fake "enum" for clarity. Could use freeze, but meh */
        var TreeNodeExpansionState = {
          'Undefined' : -1,
          'Unloaded' : 0,
          'ExpandableFromNode' : 1,
          'ExpandableFromList' : 2,
          'Loaded' : 3
        };

        // Helper function to determine what icon to show for a tree node Case
        // 1: Has children, none loaded -> use right chevron Case 2: Has
        // children, incompletely loaded (below pagesize) -> expandable (plus)
        // Case 3: Has children, completely loaded (or above pagesize) -> not
        // expandable (right/down))
        $scope.getTreeNodeExpansionState = function(tree) {

          if (!tree) {
            return null;
          }

          // case 1: no children loaded, but children exist
          if (tree.childCt > 0 && tree.child.length == 0) {
            return TreeNodeExpansionState.Unloaded;
          }

          // case 2: some children loaded, not by user, expandable from node
          else if (tree.child.length < tree.childCt
            && tree.child.length < $scope.pageSizes.sibling) {
            return TreeNodeExpansionState.ExpandableFromNode;
          }

          // case 3: some children loaded by user, expandable from list
          else if (tree.child.length < tree.childCt
            && tree.child.length >= $scope.pageSizes.sibling) {
            return TreeNodeExpansionState.ExpandableFromList;
          }

          // case 4: all children loaded
          else if (tree.child.length == tree.childCt) {
            return TreeNodeExpansionState.Loaded;
          }

          else {
            return TreeNodeExpansionState.Undefined;
          }

        }

        // Determine the icon to show (plus, right, down, or blank)
        $scope.getTreeNodeIcon = function(tree, collapsed) {

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

        // Helper function to determine whether siblings are hidden on a
        // user-expanded list
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

        // Helper function to determine whether to toggle children and/or
        // retrieve children if necessary
        $scope.getChildTrees = function(tree, treeHandleScope) {

          switch ($scope.getTreeNodeExpansionState(tree)) {

          // if fully loaded or expandable from list, simply toggle
          case TreeNodeExpansionState.Loaded:
          case TreeNodeExpansionState.ExpandableFromList:
            treeHandleScope.toggle();
            return;

          default:
            $scope.getAndSetChildTrees(tree, 0); // type prefix auto set

          }
        }

        // Get a tree node's children and add to the parent
        $scope.getAndSetChildTrees = function(tree, startIndex) {
          if (!tree) {
            return;
          }

          // set default for startIndex if not specified
          if (!startIndex)
            startIndex = 0;

          // Get child trees
          contentService
            .getChildTrees(tree, startIndex)
            .then(
              function(data) {
                // construct ancestor path (for sake of completeness, not filled
                // in on server-side)
                var ancestorPath = tree.ancestorPath + '~'
                  + tree.nodeTerminologyId;

                // cycle over children, and construct tree nodes
                for (var i = 0; i < data.tree.length; i++) {

                  // check that child is not already present (don't override
                  // present data)
                  var childPresent = false;
                  for (var j = 0; j < tree.child.length; j++) {
                    if (tree.child[j].nodeTerminologyId === data.tree[i].nodeTerminologyId) {
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

              });

        }

        // 
        // Search functions
        // 

        // Clear the search box and perform any additional operations required
        $scope.clearQuery = function() {
          $scope.searchParams.query = null;
          $scope.searchResults.list = [];
          $scope.searchResults.tree = [];
        }

        // Perform a search for the tree view
        $scope.setTreeView = function() {
          $scope.queryForTree = true;
          $scope.queryForList = false;
          if ($scope.searchParams.query) {
            $scope.searchParams.page = 1;
            $scope.findComponentsAsTree($scope.searchParams.query);
          }
        }

        // Perform a search for the list view
        $scope.setListView = function() {
          $scope.queryForList = true;
          $scope.queryForTree = false;
          if ($scope.searchParams.query) {
            $scope.searchParams.page = 1;
            $scope.findComponentsAsList($scope.searchParams.query);
          }
        }

        // Get a component and set the local component data model
        // e.g. this is called when a user clicks on a search result
        $scope.getComponent = function(terminologyId, terminology, version) {
          contentService
            .getComponent(terminologyId, terminology, version)
            .then(
              function() {
                $scope.setActiveRow($scope.component.object.terminologyId);
                $scope.getTree(0);
                $scope
                  .setComponentLocalHistory($scope.component.historyIndex);
                applyPaging();
              });
        }

        // Get a component and set the local component data model
        // e.g. this is called when a user clicks on a link in a report
        $scope.getComponentFromType = function(terminologyId, terminology,
          version, type) {
          contentService
            .getComponentFromType(terminologyId, terminology, version, type)
            .then(
              function() {
                $scope.setActiveRow($scope.component.object.terminologyId);
                $scope
                  .setComponentLocalHistory($scope.component.historyIndex);
                $scope.getTree(0);
                applyPaging();
              });
        }

        // Find components for a programmatic query
        $scope.findComponentsForQuery = function(queryStr) {
          $scope.searchParams.page = 1;
          $scope.searchParams.query = queryStr;
          $scope.findComponents(true);
        }

        // Find concepts based on current search
        // - loadFirst indicates whether to auto-load result[0]
        $scope.findComponents = function(loadFirst) {
          if ($scope.queryForList)
            $scope.findComponentsAsList(loadFirst);
          if ($scope.queryForTree)
            $scope.findComponentsAsTree(loadFirst);

          $location.hash('top');
          $anchorScroll();

        }

        // Perform search and populate list view
        $scope.findComponentsAsList = function(loadFirst) {
          $scope.queryForTree = false;
          $scope.queryForList = true;

          // ensure query string has minimum length
          if ($scope.searchParams.query == null
            || $scope.searchParams.query.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }

          contentService
            .findComponentsAsList($scope.searchParams.query,
              $scope.metadata.terminology.terminology,
              $scope.metadata.terminology.version, $scope.searchParams.page)
            .then(
              function(data) {
                $scope.searchResults.list = data.searchResult;
                $scope.searchResults.list.totalCount = data.totalCount;

                if (loadFirst && $scope.searchResults.list.length > 0) {
                  contentService
                    .getComponent($scope.searchResults.list[0].terminologyId,
                      $scope.metadata.terminology.terminology,
                      $scope.metadata.terminology.version)
                    .then(
                      function(data) {
                        $scope
                          .setActiveRow($scope.component.object.terminologyId);
                        $scope
                          .setComponentLocalHistory($scope.component.historyIndex);
                        $scope.getTree(0);
                        applyPaging();
                      });
                }
              });
        }

        // Perform search and populate tree view
        // - loadFirst is currently not used here
        $scope.findComponentsAsTree = function(loadFirst) {
          $scope.queryForTree = true;
          $scope.queryForList = false;

          // ensure query string has minimum length
          if (!$scope.searchParams.query
            || $scope.searchParams.query.length < 1) {
            alert("You must use at least three characters to search");
            return;
          }

          contentService.findComponentsAsTree($scope.searchParams.query,
            $scope.metadata.terminology.terminology,
            $scope.metadata.terminology.version, $scope.searchParams.page)
            .then(function(data) {

              // for ease and consistency of use of the ui tree directive
              // force the single tree into a ui-tree structure with count
              // variables
              $scope.searchResults.tree = [];
              $scope.searchResults.tree.push(data); // treeList array of size 1
              $scope.searchResults.tree.totalCount = data.totalCount;
              $scope.searchResults.tree.count = data.count;

              // Load first functionality is not obvious here
              // so leave it alone for now.

            });
        }

        // Load hierarchy into tree view
        $scope.browseHierarchy = function() {
          $scope.queryForTree = true;
          $scope.queryForList = false
          $scope.browsingHierarchy = true;
          $scope.searchParams.page = 1;
          $scope.searchParams.query = null;

          contentService.getTreeRoots($scope.metadata.terminology.terminology,
            $scope.metadata.terminology.version, $scope.searchParams.page)
            .then(function(data) {
              // for ease and consistency of use of the ui tree directive
              // force the single tree into a ui-tree data structure with count
              // variables
              $scope.searchResults.tree = [];
              $scope.searchResults.tree.push(data); // treeList array of size 1
              $scope.searchResults.tree.totalCount = data.totalCount;
              $scope.searchResults.tree.count = data.count;

              // TODO: if there is no hierarchy, display a message indicating
            });
        }

        // 
        // Show/Hide List Elements
        // 

        // variables for showing/hiding elements based on boolean fields
        $scope.showSuppressible = true;
        $scope.showObsolete = true;
        $scope.showAtomElement = true;
        $scope.showInferred = true;
        $scope.showExtension = false;

        // Helper function to determine if an item has boolean fields set to
        // true in its child arrays
        $scope.hasBooleanFieldTrue = function(item, fieldToCheck) {

          // check for proper arguments
          if (item == null || item == undefined)
            return false;

          // cycle over all properties
          for ( var prop in item) {
            var value = item[prop];

            // if null or undefined, skip
            if (value == null || value == undefined) {
              // do nothing
            }

            // if an array, check the array's items
            else if (Array.isArray(value)) {
              for (var i = 0; i < value.length; i++) {
                if (value[i][fieldToCheck]) {
                  return true;
                }
              }
            }

            // if not an array, check the item itself
            else if (value.hasOwnProperty(fieldToCheck) && value[fieldToCheck]) {
              return true;
            }

          }

          // default is false
          return false;
        }

        // Helper function to determine whether an item should be shown based on
        // obsolete/suppressible
        $scope.showItem = function(item) {

          // trigger on suppressible (model data)
          if (!$scope.showSuppressible && item.suppressible) {
            return false;
          }

          // trigger on obsolete (model data)
          if (!$scope.showObsolete && item.obsolete) {
            return false;
          }

          // trigger on applied showAtomElement flag
          if (!$scope.showAtomElement && item.atomElement) {
            return false;
          }

          // trigger on inferred flag
          if ($scope.metadata.terminology.descriptionLogicTerminology
            && item.hasOwnProperty('stated') && $scope.showInferred
            && item.stated) {
            return false;
          }
          if ($scope.metadata.terminology.descriptionLogicTerminology
            && item.hasOwnProperty('inferred') && !$scope.showInferred
            && item.inferred) {
            return false;
          }

          return true;
        }

        // Function to toggle obsolete flag and apply paging
        $scope.toggleObsolete = function() {
          if ($scope.showObsolete == null || $scope.showObsolete == undefined) {
            $scope.showObsolete = false;
          } else {
            $scope.showObsolete = !$scope.showObsolete;
          }
          applyPaging();
        }

        // Function to toggle suppressible flag and apply paging
        $scope.toggleSuppressible = function() {
          if ($scope.showSuppressible == null
            || $scope.showSuppressible == undefined) {
            $scope.showSuppressible = false;
          } else {
            $scope.showSuppressible = !$scope.showSuppressible;
          }

          applyPaging();
        }

        // Function to toggle atom element flag and apply paging
        $scope.toggleAtomElement = function() {
          if ($scope.showAtomElement == null
            || $scope.showAtomElement == undefined) {
            $scope.showAtomElement = false;
          } else {
            $scope.showAtomElement = !$scope.showAtomElement;
          }

          applyPaging();
        }

        // Function to toggle inferred flag and apply paging
        $scope.toggleInferred = function() {
          if ($scope.showInferred == null || $scope.showInferred == undefined) {
            $scope.showInferred = false;
          } else {
            $scope.showInferred = !$scope.showInferred;
          }
          applyPaging();
        }

        // Function to toggle showing of extension info
        $scope.toggleExtension = function() {
          if ($scope.showExtension == null || $scope.showExtension == undefined) {
            $scope.showExtension = false;
          } else {
            $scope.showExtension = !$scope.showExtension;
          }
        }

        // 
        // Expand/Collapse functions
        // 

        // Toggle collapse state
        $scope.toggleItemCollapse = function(item) {
          item.expanded = !item.expanded;
        }

        // Returns the css class for an item's collapsible control
        $scope.getCollapseIcon = function(item) {

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

        //
        // Paging functions
        //

        // paged variable lists
        // NOTE: Each list must have a totalCount variable
        // either from ResultList object or calculated
        $scope.pagedAttributes = null;
        $scope.pagedMembers = null;
        $scope.pagedSemanticTypes = null;
        $scope.pagedDescriptions = null;
        $scope.pagedRelationships = null;
        $scope.pagedAtoms = null;

        // variable page numbers
        $scope.atomPaging = {
          page : 1,
          filter : ""
        };

        $scope.styPaging = {
          page : 1,
          filter : ""
        }

        $scope.defPaging = {
          page : 1,
          filter : ""
        }

        $scope.attributePaging = {
          page : 1,
          filter : ""
        }

        $scope.memberPaging = {
          page : 1,
          filter : ""
        }

        $scope.relPaging = {
          page : 1,
          filter : ""
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

        // Handle paging of relationships (requires content service call).
        $scope.getPagedRelationships = function() {

          var filters = {
            showSuppressible : $scope.showSuppressible,
            showObsolete : $scope.showObsolete,
            showInferred : $scope.showInferred,
            text : $scope.relPaging.filter
          };

          // Request from service
          contentService.findRelationships(
            $scope.component.object.terminologyId,
            $scope.component.object.terminology,
            $scope.component.object.version, $scope.relPaging.page, filters)
            .then(function(data) {

              // if description logic terminology, sort relationships also by
              // group
              if ($scope.metadata.terminology.descriptionLogicTerminology) {
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

            });
        }

        // Get paged atoms (assume all are loaded)
        $scope.getPagedAtoms = function() {
          $scope.pagedAtoms = $scope.getPagedArray(
            $scope.component.object.atom, $scope.atomPaging.page, true,
            $scope.atomPaging.filter);
        }

        // Get paged definitions (assume all are loaded)
        $scope.getPagedDefinitions = function() {

          // get the paged array, with flags and filter (TODO: Support
          // filtering)
          $scope.pagedDefinitions = $scope.getPagedArray(
            $scope.component.object.definition, $scope.defPaging.page, true,
            $scope.defPaging.filter, 'value', false);
        }

        // Get paged attributes (assume all are loaded)
        $scope.getPagedAttributes = function() {

          // get the paged array, with flags and filter
          $scope.pagedAttributes = $scope.getPagedArray(
            $scope.component.object.attribute, $scope.attributePaging.page,
            true, $scope.attributePaging.filter, 'name', false);

        }

        // Get paged members (assume all are loaded)
        $scope.getPagedMembers = function() {

          // get the paged array, with flags and filter
          $scope.pagedMembers = $scope.getPagedArray(
            $scope.component.object.member, $scope.memberPaging.page, true,
            $scope.memberPaging.filter, 'name', false);
        }

        // Get paged STYs (assume all are loaded)
        $scope.getPagedSemanticTypes = function() {

          // get the paged array, with flags and filter
          $scope.pagedSemanticTypes = $scope.getPagedArray(
            $scope.component.object.semanticType, $scope.styPaging.page, true,
            $scope.styPaging.filter, 'semanticType', false);
        }

        // Helper to get a paged array with show/hide flags (ENABLED) and
        // filtered by
        // / query string (NOT ENABLED)
        $scope.getPagedArray = function(array, page, applyFlags, filterStr,
          sortField, ascending) {

          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array))
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
            newArray = getArrayByFilter(newArray, filterStr);
          }

          // get the page indices
          var fromIndex = (page - 1) * $scope.pageSizes.general;
          var toIndex = Math.min(fromIndex + $scope.pageSizes.general,
            array.length);

          // slice the array
          var results = newArray.slice(fromIndex, toIndex);

          // add the total count before slicing
          results.totalCount = newArray.length;

          return results;
        }

        // function for sorting an array by (string) field and direction
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

        // Filter array by show/hide flags
        function getArrayByFlags(array) {

          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array))
            return newArray;

          // apply show/hide flags via showItem() function
          for (var i = 0; i < array.length; i++) {
            if ($scope.showItem(array[i])) {
              newArray.push(array[i]);
            }
          }

          return newArray;
        }

        // Get array by filter text matching terminologyId or name
        function getArrayByFilter(array, filter) {
          var newArray = [];

          for ( var object in array) {

            if (objectContainsFilterText(array[object], filter)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        }

        // Returns true if any field on object contains filter text
        function objectContainsFilterText(object, filter) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            var value = object[prop];
            // check property for string, note this will cover child elements
            // TODO May want to make this more restrictive?
            if (value
              && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        }

        // 
        // Misc helper functions
        // 

        // Helper function to select an item in the list view
        $scope.setActiveRow = function(terminologyId) {
          if (!$scope.searchResults.list
            || $scope.searchResults.list.length == 0)
            return;
          for (var i = 0; i < $scope.searchResults.list.length; i++) {
            if ($scope.searchResults.list[i].terminologyId === terminologyId) {
              $scope.searchResults.list[i].active = true;
            } else {
              $scope.searchResults.list[i].active = false;
            }
          }
        }

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
        }
        // Function to filter viewable terminologies for picklist
        $scope.getViewableTerminologies = function() {
          var viewableTerminologies = new Array();
          if (!$scope.metadata.terminologies) {
            return viewableTerminologies;
          }
          for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
            // exclude MTH and SRC
            if ($scope.metadata.terminologies[i].terminology != 'MTH'
              && $scope.metadata.terminologies[i].terminology != 'SRC')
              viewableTerminologies.push($scope.metadata.terminologies[i])
          }
          return viewableTerminologies;
        }

        // get relationship type name from its abbreviation
        $scope.getRelationshipTypeName = function(abbr) {
          return metadataService.getRelationshipTypeName(abbr);
        }

        // get attribute name name from its abbreviation
        $scope.getAttributeNameName = function(abbr) {
          return metadataService.getAttributeNameName(abbr);
        }

        // get term type name from its abbreviation
        $scope.getTermTypeName = function(abbr) {
          return metadataService.getTermTypeName(abbr);
        }

        // get general entry name from its abbreviation
        $scope.getGeneralEntryValue = function(abbr) {
          return metadataService.getGeneralEntryValue(abbr);
        }

        // Gets the label set name
        $scope.getLabelSetName = function(abbr) {
          return metadataService.getLabelSetName(abbr);
        }

        // Label functions
        $scope.isDerivedLabelSet = metadataService.isDerivedLabelSet;
        $scope.isLabelSet = metadataService.isLabelSet;
        $scope.getDerivedLabelSetsValue = metadataService.getDerivedLabelSetsValue;
        $scope.getLabelSetsValue = metadataService.getLabelSetsValue;
        $scope.countLabels = metadataService.countLabels;

        // Load all terminologies upon controller load (unless already loaded)
        if (!$scope.metadata.terminologies) {
          metadataService
            .initTerminologies()
            .then(
              // success
              function(data) {

                var found = false;
                for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                  var terminology = $scope.metadata.terminologies[i];
                  // Determine whether to set as default
                  if (terminology.metathesaurus) {
                    metadataService.setTerminology(terminology);
                    found = true;
                    break;
                  }

                  // For icd server
                  // TODO: unhardcode this
                  if (terminology.terminology === "ICD10CM") {
                    metadataService.setTerminology(terminology);
                    found = true;
                    break;
                  }
                }

                // If nothing set, pick the first one
                if (!found) {
                  if (!$scope.metadata.terminologies) {
                    window
                      .alert('No terminologies found, database may not be properly loaded.');
                  } else {
                    metadataService
                      .setTerminology($scope.metadata.terminologies[0]);
                  }
                }
              });
        }

        // 
        // HISTORY related functions
        //

        // Local history variables for the display.
        $scope.localHistory = null;
        $scope.localHistoryPageSize = $scope.pageSizes.general; // NOTE: must be
        // even number!
        $scope.localHistoryPreviousCt = 0;
        $scope.localHistoryNextCt = 0;

        // Retrieve a component from the history list
        $scope.getComponentFromHistory = function(index) {
          // if currently viewed do nothing
          if (index === $scope.component.historyIndex)
            return;

          contentService.getComponentFromHistory(index).then(function(data) {
            // manage local history
            $scope.setComponentLocalHistory(index);
            applyPaging();
          });
        }

        // Get a string representation fo the component
        $scope.getComponentStr = function(component) {
          if (!component)
            return null;

          return component.terminology + "/" + component.terminologyId + " "
            + component.type + ": " + component.name;
        }

        // Function to set the local history for drop down list based on an
        // index For cases where history > page size, returns array [index -
        // pageSize / 2 + 1 : index + pageSize]
        $scope.setComponentLocalHistory = function(index) {
          // if not a full page of history, simply set to component history and
          // stop
          if ($scope.component.history.length <= $scope.localHistoryPageSize) {
            $scope.localHistory = $scope.component.history;
            return;
          }

          // get upper bound
          var upperBound = Math.min(index + $scope.localHistoryPageSize / 2,
            $scope.component.history.length);
          var lowerBound = Math
            .max(upperBound - $scope.localHistoryPageSize, 0);

          // resize upper bound to ensure full page (for cases near beginning of
          // history)
          upperBound = lowerBound + $scope.localHistoryPageSize;

          // calculate unshown element numbers
          $scope.localHistoryNextCt = $scope.component.history.length
            - upperBound;
          $scope.localHistoryPreviousCt = lowerBound;

          // return the local history
          $scope.localHistory = $scope.component.history.slice(lowerBound,
            upperBound);
        }

        // when navigating back, apply paging if there is a component
        if ($scope.component.object) {
          applyPaging();
        }
      }

    ]);
