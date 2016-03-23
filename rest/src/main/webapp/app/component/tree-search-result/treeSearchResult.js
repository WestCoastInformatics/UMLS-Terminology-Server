// Content controller
tsApp.directive('treeSearchResult', [
  '$q',
  'contentService',
  'metadataService',
  'utilService',
  function($q, contentService, metadataService, utilService) {
    console.debug('configure trees directive');
    return {
      restrict : 'A',
      scope : {
        // set search results if viewing trees for search
        searchResults : '=',

        // pass parameters for styling (e.g. extension highlighting)
        parameters : '=',

        // callback functions from parent scope
        callbacks : '='
      },
      templateUrl : 'app/component/tree-search-result/treeSearchResult.html',
      link : function(scope, element, attrs) {

        console.debug('treeSearchResult', scope.searchResults, scope.parameters, scope.callbacks);
        ;

        // page sizes
        scope.pageSizeSibling = 10;

        scope.isDerivedLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isDerivedLabelSet(tree);
        }

        scope.getDerivedLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getDerivedLabelSetsValue(tree);
        }

        scope.isLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isLabelSet(tree);
        }

        scope.getLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getLabelSetsValue(tree);
        }

        // retrieves the children for a node (from DOM)
        scope.getTreeChildrenFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          scope.getTreeChildren(tree).then(function(children) {
            console.debug('adding children', children);
            tree.children = tree.children.concat(children);
          });
        }

        // retrieves children for a node (not from DOM)
        scope.getTreeChildren = function(tree) {

          var deferred = $q.defer();

          if (!tree) {
            console.error('getChildren called with null node');
            deferred.resolve([]);
          }

          // get the next page of children based on start index of current children length
          contentService.getChildTrees(tree, tree.children.length).then(function(data) {
            console.debug('retrieved children', data);
            deferred.resolve(data.trees);
          }, function(error) {
            console.error('Unexpected error retrieving children');
            deferred.resolve([]);
          });

          return deferred.promise;
        }

        // toggles a node (from DOM)
        scope.toggleTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;

          console.debug('toggling tree', tree, nodeScope.collapsed);

          // if not expanded, simply expand
          if (nodeScope.collapsed) {
            nodeScope.toggle();
          }

          // otherwise if a full page of siblings not already loaded, get first page
          else if (tree.children.length != tree.childCt
            && tree.children.length < scope.pageSizeSibling) {
            console.debug('getting children')
            scope.getTreeChildren(tree).then(function(children) {
              console.debug('adding children', children);
              tree.children = tree.children.concat(children);
            });
          }

          // otherwise, collapse
          else {
            console.debug('collapsing');
            nodeScope.toggle();
          }
        }

        // returns the display icon for a node (from DOM)
        scope.getTreeNodeIcon = function(nodeScope) {
          var tree = nodeScope.$modelValue;

          // NOTE: This is redundant, leaf icon is set directly in html
          if (tree.childCt == 0) {
            return 'glyphicon-leaf';
          }

          // if formally collapsed or less than sibling page size retrieved children, return plus sign
          else if (tree.children.length != tree.childCt
            && tree.children.length < scope.pageSizeSibling) {
            return 'glyphicon-plus';
          }

          // if collapsed or unloaded
          else if (nodeScope.collapsed || (tree.childCt > 0 && tree.children.length == 0)) {
            return 'glyphicon-chevron-right'
          }

          // otherwise, return minus sign
          else if (!nodeScope.collapsed) {
            return 'glyphicon-chevron-down';
          }

          // if no matches, return a ? because something is seriously wrong
          else {
            return 'glyphicon-question-sign';
          }

        };

        // Label functions
        scope.isDerivedLabelSet = metadataService.isDerivedLabelSet;
        scope.isLabelSet = metadataService.isLabelSet;
        scope.getDerivedLabelSetsValue = function() {
          return $sce.trustAsHtml('<div style="text-align:left;">'
            + metadataService.getDerivedLabelSetsValue() + '</div>');
        };
        scope.getLabelSetsValue = function() {
          return $sce.trustAsHtml('<div style="text-align:left;">'
            + metadataService.getLabelSetsValue + '</div>');
        };

      }
    };
  } ]);
