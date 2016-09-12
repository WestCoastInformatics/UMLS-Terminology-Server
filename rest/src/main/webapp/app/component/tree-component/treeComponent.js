// Tree
tsApp.directive('treeComponent', [
  '$q',
  'contentService',
  'utilService',
  function($q, contentService, utilService) {
    console.debug('configure trees directive');
    return {
      restrict : 'A',
      scope : {
        // set component if viewing trees for component
        metadata : '=',
        component : '=',
        callbacks : '=?'
      },
      templateUrl : 'app/component/tree-component/treeComponent.html',
      link : function(scope, element, attrs) {

        console.debug('callbacks', scope.callbacks);

        // watch for component change
        scope.$watch('component', function() {
          if (scope.component) {
            scope.getTree(0);
          }
        })

        // total trees for this component
        scope.treeCount = null;

        // the currently viewed tree index
        scope.treeViewed = null;

        // the currently viewed tree data
        scope.componentTree = null;

        // the cutoff length for siblings
        scope.pageSizeSibling = 10;

        function concatSiblings(tree, siblings) {

          var existingIds = tree.map(function(item) {
            return item.nodeTerminologyId;
          });

          newSiblings = tree.concat(siblings.filter(function(sibling) {
            return existingIds.indexOf(sibling.nodeTerminologyId) == -1;
          }));

          newSiblings.sort(function(a, b) {
            if (a.nodeTerminologyId === scope.component.terminologyId) {
              return -1;
            }
            if (a.nodeName < b.nodeName) {
              return -1;
            } else {
              return 1;
            }

          });

          return newSiblings;
        }

        // retrieves the specified tree position by index (top-level,
        // scope-indifferent)
        // displayed
        scope.getTree = function(startIndex) {
          console.debug('getting tree', startIndex);
          // Call content service to retrieve the tree
          contentService.getTree(scope.component, startIndex).then(function(data) {

            scope.componentTree = data.trees;

            // set the count and position variables
            scope.treeCount = data.totalCount;
            if (data.trees && data.trees.length > 0)
              scope.treeViewed = startIndex;
            else
              scope.treeViewed = 0;

            // if parent tree cannot be read, clear the component
            // tree
            // (indicates no hierarchy present)
            if (scope.componentTree.length == 0) {
              scope.componentTree = null;
              return;
            }

            // get the ancestor path of the bottom element (the
            // component)
            // ASSUMES: unilinear path (e.g. A~B~C~D, no siblings)
            var parentTree = scope.componentTree[0];
            while (parentTree.children.length > 0) {
              // check if child has no children
              if (parentTree.children[0].children.length == 0)
                break;
              parentTree = parentTree.children[0];
            }

            // replace the parent tree of the lowest level with
            // first page of siblings computed
            scope.getTreeChildren(parentTree, 0).then(function(children) {
              parentTree.children = concatSiblings(parentTree.children, children);
            });

          });

        };

        scope.isDerivedLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isDerivedLabelSet(tree);
        };

        scope.getDerivedLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getDerivedLabelSetsValue(tree, scope.metadata);
        };

        scope.isLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isLabelSet(tree);
        };

        scope.getLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getLabelSetsValue(tree, scope.metadata);
        };

        // retrieves the children for a node (from DOM)
        scope.getTreeChildrenFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          scope.getTreeChildren(tree).then(function(children) {
            tree.children = concatSiblings(tree.children, children);
          });
        };

        // retrieves children for a node (not from DOM)
        scope.getTreeChildren = function(tree) {

          var deferred = $q.defer();

          if (!tree) {
            utilSevice.setError('getChildren called with null node');
            deferred.resolve([]);
          }

          // get the next page of children based on start index of current
          // children length
          // NOTE: Offset by 1 to incorporate the (possibly) already loaded item
          contentService.getChildTrees(tree, scope.component.type, tree.children.length - 1).then(
            function(data) {
              deferred.resolve(data.trees);
            }, function(error) {
              utilService.setError('Unexpected error retrieving children');
              deferred.resolve([]);
            });

          return deferred.promise;
        };

        // toggles a node (from DOM)
        scope.toggleTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;

          // if not expanded, simply expand
          if (nodeScope.collapsed) {
            nodeScope.toggle();
          }

          // otherwise if a full page of siblings not already loaded, get first
          // page
          else if (tree.children.length != tree.childCt
            && tree.children.length < scope.pageSizeSibling) {
            scope.getTreeChildren(tree).then(function(children) {
              tree.children = concatSiblings(tree.children, children);
            });
          }

          // otherwise, collapse
          else {
            nodeScope.toggle();
          }
        };

        // returns the display icon for a node (from DOM)
        scope.getTreeNodeIcon = function(nodeScope) {
          var tree = nodeScope.$modelValue;

          // NOTE: This is redundant, leaf icon is set directly in html
          if (tree.childCt == 0) {
            return 'glyphicon-leaf';
          }

          // if collapsed or unloaded
          else if (nodeScope.collapsed || (tree.childCt > 0 && tree.children.length == 0)) {
            return 'glyphicon-chevron-right';
          }

          // if formally collapsed or less than sibling page size retrieved
          // children, return plus sign
          else if (tree.children.length != tree.childCt
            && tree.children.length < scope.pageSizeSibling) {
            return 'glyphicon-plus';
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

        // get tree by specified offset (circular index)
        scope.getTreeByOffset = function(offset) {

          var treeViewed = scope.treeViewed + offset;

          if (!treeViewed)
            treeViewed = 0;
          if (treeViewed >= scope.treeCount)
            treeViewed = treeViewed - scope.treeCount;
          if (treeViewed < 0)
            treeViewed = treeViewed + scope.treeCount;

          scope.getTree(treeViewed);
        };

      }
    };
  } ]);
