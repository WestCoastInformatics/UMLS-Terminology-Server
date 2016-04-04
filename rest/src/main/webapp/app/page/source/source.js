// Route
tsApp.config(function config($routeProvider) {
  $routeProvider.when('/source', {
    controller : 'SourceDataCtrl',
    templateUrl : 'app/page/source/source.html'
  });
});

// Controller
tsApp.controller('SourceDataCtrl', function($scope, $http, $q, $interval, NgTableParams,
  sourceDataService, utilService, securityService, gpService, FileUploader) {
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
        /*
        pfsParameter = { queryRestiction :$scope.tpAttached.filter = ...
        sortField = $scope.tpAttached.sort = ...
        startIndex = $scope.tpAttached.page * pageSize...*/
      })
    });
  }

  // view the source data and retrieve current source data file list
  $scope.viewSourceData = function(sourceData) {
    console.debug('Viewing sourceData', sourceData);

    // set to null initially for currentSourceData watch condition
    $scope.isSourceDataModified = null;
    $scope.currentSourceData = sourceData;

    sourceDataService.findSourceDataFiles("").then(
    // Successs
    function(response) {
      sourceDataFiles = response.sourceDataFiles;
      refreshFileTables();
    });
  };

  // watch for changes to current source data to enable save/cancel buttons
  $scope.$watch('currentSourceData', function() {
    // if null condition found, newly loaded/unmodified source data
    if ($scope.isSourceDataModified == null) {
      $scope.isSourceDataModified = false;
    } else {
      $scope.isSourceDataModified = true;
    }
  }, true);

  // Create new source data JSON object
  $scope.createSourceData = function() {
    var sourceData = {
      name : null,
      description : null,
      lastModifiedBy : securityService.getUser().userName,
      status : 'NEW',
      statusText : 'New source data package',
      sourceDataFiles : [],
      handler : null,
      terminology : null,
      version : null,
      releaseVersion : null
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
    var deferred = $q.defer();
    sourceDataService.findSourceData("").then(
    // Success
    function(response) {
      sourceDatas = response.sourceDatas;
      refreshSourceDataTable();
      deferred.resolve();
    }, function() {
      deferred.reject();
    });
  }

  // Gets $scope.loaders
  function getSourceDataHandlers() {
    sourceDataService.getSourceDataHandlers().then(
    // Success
    function(sourceDataHandlers) {
      console.debug('source data handlers', sourceDataHandlers);
      $scope.sourceDataHandlers = sourceDataHandlers;

    });
  }

  // currently active polls, array of objects {id, poll}
  $scope.loadingPolls = {};

  $scope.loadFromSourceData = function(sourceData) {
    // ensure that source data is not modified
    if ($scope.isSourceDataModified) {
      window.alert('Save or cancel changes to source data before loading.');
      return;
    }

    // start load and initiate polling
    sourceDataService.loadFromSourceData(sourceData).then(function() {
      // TODO Reenable polling 
      // $scope.startLoadingPolling(sourceData);
    });

  };

  $scope.startLoadingPolling = function(sourceData) {
    console.log('Starting loading polling for ' + sourceData.name);

    // TODO Ensure Brian notices my rebellion with polling interval of 1.001s!
    $scope.loadingPolls[sourceData.id] = $interval(function() {

      // get the source data by id
      sourceDataService.getSourceData(sourceData.id).then(
        function(polledSourceData) {
          // if cannot retrieve or no longer loading, cancel polling
          if (!polledSourceData || polledSourceData.status !== 'LOADING') {
            console.log('Status change detected for ' + sourceData.name + ': '
              + polledSourceData.status);
            $interval.cancel($scope.loadingPolls[sourceData.id]);
            delete $scope.loadingPolls[sourceData.id];
          }
        });

    }, 1001);
  }

  $scope.cancelLoadingPolling = function(sourceData) {
    $interval.cancel($scope.loadingPolls[sourceData.id]);
    delete $scope.loadingPolls[sourceData.id];
  }

  // cancel all polling on reloads or navigation
  $scope.$on("$routeChangeStart", function(event, next, current) {
    for (var key in $scope.loadingPolls) {
      if ($scope.loadingPolls.hasOwnProperty(key)) {
        $interval.cancel($scope.loadingPolls[key]);
      }
    }
  });
  
  //
  // Angular File Upload controls
  // 
  
//VARIABLES
  var uploadedFiles = [];

  // SCOPE VARIABLES

  // flag for whether zipped files are present, sent in uploader event
  // listeners
  // NOTE: Only updated on add events, as angular-file-upload does not have
  // a _remove event
  $scope.hasZippedFiles = false;

  // Get uploaded file details
  function getUploadedFileDetails() {
    sourceDataService.findSourceDataFiles("").then(
    // Success
    function(response) {
      uploadedFiles = response.sourceDataFiles;
      $scope.tpUploaded = new NgTableParams({}, {
        dataset : uploadedFiles
      });
    });
  }

  // Download file
  $scope.downloadFile = function() {
    window.alert('Not yet functional');
  };

  // Download all files
  $scope.downloadAllFiles = function() {
    window.alert('Not yet functional');
  };

  // Remove file from the server
  $scope.removeFile = function(file) {
    sourceDataService.removeSourceDataFile(file.id).then(
    // Success
    function(response) {
      getUploadedFileDetails();
    });
  };

  // Remove all files
  $scope.removeAllFiles = function() {
    if (!window.confirm('Are you sure you want to delete all uploaded files?')) {
      return;
    }

    // declare timeout object used to prevent retrieval calls more than once
    // per half-second
    // used to prevent enormous number of getUploadedFileDetails for large
    // lists
    // while still allowing for visual update of removed items
    var refreshTimeout = null;
    angular.forEach(uploadedFiles, function(file) {
      gpService.increment();
      sourceDataService.removeSourceDataFile(file.id).then(function() {
        gpService.decrement();

        // cancel existing timeout
        if (refreshTimeout) {
          $timeout.cancel(refreshTimeout);
        }

        // set the new timeout
        refreshTimeout = $timeout(function() {
          getUploadedFileDetails();
        }, 500);
      });
    });
  };

  // ///////////////////////
  // Table Parameters
  // ///////////////////////

  /*
   * // declare table parameters $scope.tpUploaded = new ngTableParams({
   * page : 1, count : 10, sorting : { 'name' : 'asc' } }, { filterDelay :
   * 50, total : $scope.tasks ? $scope.tasks.length : 0, // length of data
   * getData : function($defer, params) {
   * 
   * if (!$scope.uploadedFiles || $scope.uploadedFiles.length == 0) {
   * $defer.resolve([]); } else {
   * 
   * var data = params.sorting() ? $filter('orderBy')($scope.uploadedFiles,
   * params.orderBy()) : mydata;
   * 
   * $defer.resolve(data.slice((params.page() - 1) * params.count(),
   * params.page() params.count())); } } });
   */

  function isZipFile(item) {
    return item.file.name.match(/.*\.zip/g) !== null;
  }

  // Specify the angular-file-uploader
  var uploader = $scope.uploader = new FileUploader({
    url : fileUrl + '/upload'
  });

  // FILTERS

  uploader.filters = [];

  // CALLBACKS
  uploader.onWhenAddingFileFailed = function(item /* {File|FileLikeObject} */, filter, options) {
    //console.info('onWhenAddingFileFailed', item, filter, options);
  };
  uploader.onAfterAddingFile = function(fileItem) {
    //console.info('onAfterAddingFile', fileItem);
    fileItem.isZipped = isZipFile(fileItem);
    if (fileItem.isZipped) {
      $scope.hasZippedFiles = true;
    }
  };
  uploader.onAfterAddingAll = function(addedFileItems) {
   // console.info('onAfterAddingAll', addedFileItems);
    angular.forEach(addedFileItems, function(fileItem) {
      fileItem.isZipped = isZipFile(fileItem);
      if (fileItem.isZipped) {
        $scope.hasZippedFiles = true;
      }
    });

    // checkForZippedFiles();
  };
  uploader.onBeforeUploadItem = function(item) {

    // dynamically set the upload url with the unzip flag
    item.url = fileUrl + '/upload?unzip=' + (item.unzip ? 'true' : 'false');

    // manually set the headers on the item's request (does not inherit from
    // $http, apparently)
    item.headers = {
      'Authorization' : 'admin'
    };
  };
  uploader.onProgressItem = function(fileItem, progress) {
   // console.info('onProgressItem', fileItem, progress);
  };
  uploader.onProgressAll = function(progress) {
  //  console.info('onProgressAll', progress);
  };
  uploader.onSuccessItem = function(fileItem, response, status, headers) {
  //  console.info('onSuccessItem', fileItem, response, status, headers);
  };
  uploader.onErrorItem = function(fileItem, response, status, headers) {
  //  console.info('onErrorItem', fileItem, response, status, headers);
    utilService
      .handleError({
        data : response ? response
          : "Folders cannot be uploaded; only single files or zip files containing no folders may be uploaded.",
        status : status,
        headers : headers
      }); // shoehorn into tsApp expected error format
  };
  uploader.onCancelItem = function(fileItem, response, status, headers) {
 //   console.info('onCancelItem', fileItem, response, status, headers);
  };
  uploader.onCompleteItem = function(fileItem, response, status, headers) {
  //  console.info('onCompleteItem', fileItem, response, status, headers);
    getUploadedFileDetails();
  };
  uploader.onCompleteAll = function() {
  //  console.info('onCompleteAll');
  };
  

  //
  // Initialize
  //
  refreshSourceDatas();
  getSourceDataHandlers();

});