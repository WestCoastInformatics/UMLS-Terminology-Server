// Route
tsApp.config(function config($routeProvider) {
  $routeProvider.when('/source', {
    controller : 'SourceDataCtrl',
    templateUrl : 'app/page/source/source.html'
  });
});

// Controller
tsApp.controller('SourceDataCtrl', function($scope, $http, $q, $timeout, NgTableParams,
  sourceDataService, utilService, securityService, gpService) {
  console.debug('configure SourceDataCtrl');

  // /////////////////////
  // Local variables
  // /////////////////////

  var sourceDatas = [];
  var sourceDataFiles = [];
  var currentSourceData = null;

  // //////////////////////
  // Scope variables
  // //////////////////////

  $scope.loaders = null;
  $scope.tpSourceDatas = null;

  // /////////////////////////////
  // ngTable refresh functions
  // /////////////////////////////

  // Instantiates new table params from sourceDatas array
  function refreshSourceDataTable() {
    console.debug('Refreshing table with values', sourceDatas);
    $scope.tpSourceDatas = new NgTableParams({}, {
      dataset : sourceDatas
    });
  }

  // Instantiates new table params from source data files and current source
  // data viewed
  function refreshFileTables() {

    // extract current file ids for convenience
    var currentFileIds = $scope.currentSourceData.sourceDataFiles.map(function(item) {
      return item.id;
    });

    // available: any items not in current file ids (not attached to viewed
    // sourceData)
    $scope.tpAvailable = new NgTableParams({}, {
      dataset : sourceDataFiles.filter(function(item) {
        return currentFileIds.indexOf(item.id) === -1;
      })
    });

    // attached: any items in current file ids (attached to viewed
    // sourceData)
    $scope.tpAttached = new NgTableParams({}, {
      dataset : sourceDataFiles.filter(function(item) {
        return currentFileIds.indexOf(item.id) !== -1;
      })
    });
  }

  // view the source data and retrieve current source data file list
  $scope.viewSourceData = function(sourceData) {
    console.debug('Viewing sourceData', sourceData);
    $scope.currentSourceData = sourceData;

    // hackish way to set the isModified flag
    $timeout(function() {
      $scope.isSourceDataModified = false;
    }, 250);

    sourceDataService.findSourceDataFiles("").then(
    // Successs
    function(response) {
      sourceDataFiles = response.sourceDataFiles;
      refreshFileTables();
    });
  };

  // watch for changes to current source data to enable save/cancel buttons
  $scope.$watch('currentSourceData', function() {
    $scope.isSourceDataModified = true;
  }, true);

  // Create new source data JSON object
  $scope.createSourceData = function() {
    var sourceData = {
      name : null,
      description : null,
      lastModifiedBy : securityService.getUser().userName,
      sourceDataFiles : [],
      loader : null,
    };
    sourceDatas.splice(0, 0, sourceData);
    refreshSourceDataTable();
  };

  // Save source data
  $scope.saveSourceData = function(sourceData) {
    if (!sourceData.name) {
      window.alert('The source data name cannot be empty');
      return;
    }
    sourceDataService.updateSourceData(sourceData);
  };

  // Cancel source data modifications
  $scope.cancelSourceDataModifications = function() {
    if (!$scope.isSourceDataModified || window.confirm('Discard changes?')) {
      $scope.currentSourceData = null;
    }
  };

  // Remove source data
  $scope.removeSourceData = function(sourceData) {
    sourceDataService.removeSourceData(sourceData).then(
    // Success
    function(response) {
      refreshSourceDatas();
    });

  };

  // Add source data to source data list
  $scope.addSourceDataFileToSourceData = function(sourceData, file) {
    if (!sourceData.sourceDataFiles || !Array.isArray(sourceDataFiles)) {
      sourceData.sourceDataFiles = [ file ];
    } else {
      sourceData.sourceDataFiles.push(file);
    }
    refreshFileTables();
  };

  // Remove source data from list
  $scope.removeSourceDataFileFromSourceData = function(sourceData, file) {
    $scope.currentSourceData.sourceDataFiles = $scope.currentSourceData.sourceDataFiles
      .filter(function(item) {
        return item.id !== file.id;
      });
    refreshFileTables();
  };

  // Refreshes source data list from server and instantiates new table
  // params
  function refreshSourceDatas() {
    sourceDataService.findSourceData("").then(
    // Success
    function(response) {
      sourceDatas = response.sourceDatas;
      refreshSourceDataTable();
    });
  }

  // Gets $scope.loaders
  function getLoaders() {
    sourceDataService.getLoaders().then(
    // Success
    function(names) {
      $scope.loaders = names;
    });
  }

  //
  // Initialize
  //
  refreshSourceDatas();
  getLoaders();

});