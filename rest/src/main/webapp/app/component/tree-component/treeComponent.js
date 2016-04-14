// Content controller
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
        component : '=',
        callbacks : '=?'
      },
      templateUrl : 'app/component/tree-component/treeComponent.html',
      link : function(scope, element, attrs) {

        // total trees for this component
        scope.treeCount = null;

        // the currently viewed tree index
        scope.treeViewed = null;

        // the currently viewed tree data
        scope.componentTree = null;

        // the cutoff length for siblings
        scope.pageSizeSibling = 10;

        // retrieves the specified tree position by index (top-level,
        // scope-indifferent)
        // displayed
        scope.getTree = function(startIndex) {
          // Call content service to retrieve the tree
          contentService.getTree(scope.component.object.terminologyId,
            scope.component.object.terminology, scope.component.object.version, startIndex).then(
            function(data) {

              scope.componentTree = data.trees;

              // set the count and position variables
              scope.treeCount = data.totalCount;
              if (data.count > 0)
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
                parentTree.children = parentTree.children.concat(children.filter(function(child) {
                  // do not re-add the already-shown component for this tree
                  return scope.component.object.terminologyId !== child.nodeTerminologyId;
                }));
              });

            });

        };

        // on load, get the first tree
        scope.getTree(0);

        scope.isDerivedLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isDerivedLabelSet(tree);
        };

        scope.getDerivedLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getDerivedLabelSetsValue(tree);
        };

        scope.isLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.isLabelSet(tree);
        };

        scope.getLabelSetsValueFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return scope.getLabelSetsValue(tree);
        };

        // retrieves the children for a node (from DOM)
        scope.getTreeChildrenFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          scope.getTreeChildren(tree).then(function(children) {
            console.debug('adding children', children);
            tree.children = tree.children.concat(children);
          });
        };

        // retrieves children for a node (not from DOM)
        scope.getTreeChildren = function(tree) {

          var deferred = $q.defer();

          if (!tree) {
            console.error('getChildren called with null node');
            deferred.resolve([]);
          }

          // get the next page of children based on start index of current
          // children length
          contentService.getChildTrees(tree, tree.children.length).then(function(data) {
            console.debug('retrieved children', data);
            deferred.resolve(data.trees);
          }, function(error) {
            console.error('Unexpected error retrieving children');
            deferred.resolve([]);
          });

          return deferred.promise;
        };

        // toggles a node (from DOM)
        scope.toggleTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;

          console.debug('toggling tree', tree, nodeScope.collapsed);

          // if not expanded, simply expand
          if (nodeScope.collapsed) {
            nodeScope.toggle();
          }

          // otherwise if a full page of siblings not already loaded, get first
          // page
          else if (tree.children.length != tree.childCt
            && tree.children.length < scope.pageSizeSibling) {
            console.debug('getting children');
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
