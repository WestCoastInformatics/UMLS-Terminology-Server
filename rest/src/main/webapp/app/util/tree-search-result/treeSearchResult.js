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
        // set search results if viewing trees for search
        searchResults : '=',

        // pass parameters for styling (e.g. extension highlighting)
        parameters : '=',

        // callback functions from parent scope
        callbacks : '='
      },
      templateUrl : 'app/util/tree-search-result/treeSearchResult.html',
      link : function(scope, element, attrs) {

        console.debug('treeSearchResult', scope.searchResults, scope.parameters, scope.callbacks);

        // page sizes
        scope.pageSizeSibling = 10;

        // computed tooltip html for derived labels
        // NOTE: Must not be null or empty string, or uib-tooltip-html
        // will not properly register the first mouseover event
        scope.labelTooltipHtml = "&nbsp;";

        //
        // Extension highlighting: derived label sets
        //

        // function called on mouseovers to set computed tooltip html
        // workaround used to get around $sce.trustAsHtml infinite digest loops
        scope.getDerivedLabelSetsValueFromTree = function(nodeScope) {
          scope.labelTooltipHtml = $sce.trustAsHtml('<div style="text-align:left;">'
            + metadataService.getDerivedLabelSetsValue(nodeScope.$modelValue) + '</div>');
          console.debug('derived label html', metadataService
            .getDerivedLabelSetsValue(nodeScope.$modelValue));
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
            + metadataService.getLabelSetsValue(nodeScope.$modelValue) + '</div>');
          console.debug('label html', metadataService.getLabelSetsValue(nodeScope.$modelValue));
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

      }
    };
  } ]);
