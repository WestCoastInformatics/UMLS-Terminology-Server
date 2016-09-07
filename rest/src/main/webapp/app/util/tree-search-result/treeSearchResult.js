// Content controller
tsApp.directive('treeSearchResult', [
  '$q',
  '$sce',
  'contentService',
  'metadataService',
  'utilService',
  function($q, $sce, contentService, metadataService, utilService) {
    console.debug('configure trees directive');
    return {
      restrict : 'A',
      scope : {
        
        // metadata
        metadata : '=',
        
        // set search results if viewing trees for search
        searchResults : '=',

        // pass parameters for styling (e.g. extension highlighting)
        parameters : '=',

        // callback functions from parent scope
        callbacks : '='
      },
      templateUrl : 'app/util/tree-search-result/treeSearchResult.html',
      link : function(scope, element, attrs) {

        // page sizes
        scope.pageSizeSibling = 10;

        // computed tooltip html for derived labels
        // NOTE: Must not be null or empty string, or uib-tooltip-html
        // will not properly register the first mouseover event
        scope.labelTooltipHtml = "&nbsp;";

        function concatSiblings(tree, siblings) {

          var existingIds = tree.map(function(item) {
            return item.nodeTerminologyId;
          });

          newSiblings = tree.concat(siblings.filter(function(sibling) {
            return existingIds.indexOf(sibling.nodeTerminologyId) == -1;
          }));

          newSiblings.sort(function(a, b) {

            if (a.nodeName < b.nodeName) {
              return -1;
            } else {
              return 1;
            }
            
          });

          return newSiblings;
        }

        //
        // Extension highlighting: derived label sets
        //

        // function called on mouseovers to set computed tooltip html
        // workaround used to get around $sce.trustAsHtml infinite digest loops
        scope.getDerivedLabelSetsValueFromTree = function(nodeScope) {
          scope.labelTooltipHtml = $sce.trustAsHtml('<div style="text-align:left;">'
            + metadataService.getDerivedLabelSetsValue(nodeScope.$modelValue, scope.metadata) + '</div>');

        };

        scope.isDerivedLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return metadataService.isDerivedLabelSet(tree);
        };

        //
        // Extension highlighting: label sets
        //

        // function called on mouseovers to set computed tooltip html
        // workaround used to get around $sce.trustAsHtml infinite digest loops
        scope.getLabelSetsValueFromTree = function(nodeScope) {
          scope.labelTooltipHtml = $sce.trustAsHtml('<div style="text-align:left;">'
            + metadataService.getLabelSetsValue(nodeScope.$modelValue, scope.metadata) + '</div>');
        };

        scope.isLabelSetFromTree = function(nodeScope) {
          var tree = nodeScope.$modelValue;
          return metadataService.isLabelSet(tree);
        };

        //
        // Tree Operations
        //

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
            utilService.setError('getChildren called with null node');
            deferred.resolve([]);
          }

          // get the next page of children based on start index of current
          // children length
          // NOTE: Offset by 1 to incorporate the (possibly) already loaded item

          contentService.getChildTrees(tree, scope.metadata.terminology.organizingClassType, tree.children.length - 1).then(function(data) {
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

      }
    };
  } ]);
