// Route
tsApp.config(function config($routeProvider) {
  $routeProvider.when('/source', {
    controller : 'SourceDataCtrl',
    templateUrl : 'app/page/source/source.html'
  });
});

// Controller
tsApp
  .controller(
    'SourceDataCtrl',
    function($scope, $http, $q, $interval, NgTableParams, sourceDataService, utilService,
      securityService, gpService, FileUploader, tabService) {
      console.debug('configure SourceDataCtrl');

      // Handle resetting tabs on "back" button
      if (tabService.selectedTab.label != 'Source') {
        tabService.setSelectedTabByLabel('Source');
      }

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
      $scope.tpSourceDataFiles = null;
      $scope.hasZippedFiles = false;
      $scope.showFullPaths = false;

      //
      // Source data retrieval helper functions
      //

      // /////////////////////////////
      // ngTable refresh functions
      // /////////////////////////////

      // Instantiates new table params from sourceDatas array
      function refreshTables() {
        console.debug('Refreshing table with values', sourceDatas);
        $scope.tpSourceDatas = new NgTableParams({}, {
          dataset : sourceDatas,
          counts : []
        // hides page sizes
        });
        $scope.tpSourceDataFiles = new NgTableParams({}, {
          dataset : $scope.currentSourceData ? $scope.currentSourceData.sourceDataFiles : [],
          counts : []
        // hides page sizes
        });
      }
      ;

      // view the source data and retrieve current source data file list
      $scope.viewSourceData = function(sourceData) {
        console.debug('Viewing sourceData', sourceData);

        // set to null initially for currentSourceData watch condition
        $scope.isSourceDataModified = null;
        $scope.currentSourceData = sourceData;

        sourceDataService.findSourceDataFiles("").then(
        // Success
        function(response) {
          sourceDataFiles = response.sourceDataFiles;
          refreshTables();
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
          version : null
        };
        sourceDatas.splice(0, 0, sourceData);
        $scope.currentSourceData = sourceData;
        refreshTables();
      };

      // Save source data
      $scope.saveSourceData = function(sourceData) {
        if (!sourceData.name) {
          window.alert('The source data name cannot be empty');
          return;
        }
        sourceDataService.updateSourceData(sourceData).then(function(response) {
          // update the source data and current source data if response returned
          // (add call)
          if (response) {
            sourceData = response;
            $scope.currentSourceData = response;
          }
          $scope.isSourceDataModified = false;
        })
      };

      // Cancel source data modifications
      $scope.cancelSourceDataModifications = function() {
        if (!$scope.isSourceDataModified || window.confirm('Discard changes?')) {
          $scope.currentSourceData = null;
          refreshTables();
        }
      };

      // Remove source data
      $scope.removeSourceData = function(sourceData) {

        // if local only, simply remove from the list
        if (!sourceData.id) {
          for (var i = 0; i < sourceDatas.length; i++) {
            if (sourceData === sourceDatas[i]) {
              sourceDatas.splice(i, 1);
              break;
            }
          }
        }

        // otherwise ask user for confirmation and delete
        else if (confirm('This will delete any uploaded files for this configuration. Are you sure?')) {
          sourceDataService.removeSourceData(sourceData).then(
          // Success
          function(response) {
            if ($scope.currentSourceData && $scope.currentSourceData.id === sourceData.id) {
              $scope.currentSourceData = null;
            }
            // retrieve source datas, also refreshes tables
            retrieveSourceDatas();
          });
        }

      };

      // Add source data to source data list
      $scope.addSourceDataFileToSourceData = function(sourceData, file) {
        if (!sourceData.sourceDataFiles || !Array.isArray(sourceDataFiles)) {
          sourceData.sourceDataFiles = [ file ];
        } else {
          sourceData.sourceDataFiles.push(file);
        }
        refreshTables();
      };

      // Remove source data from list
      $scope.removeSourceDataFileFromSourceData = function(file) {
        sourceDataService.removeSourceDataFile(file.id).then(function() {
          // re-retrieve the source datas

        });
      };

      $scope.getFilePath = function(file) {

        var id = $scope.currentSourceData.id;
        return file.path.substring(file.path.indexOf(id) + id.toString().length + 1);
      };

      // Refreshes source data list from server and instantiates new table
      // params
      function retrieveSourceDatas() {
        var deferred = $q.defer();
        sourceDataService.findSourceData("").then(
        // Success
        function(response) {
          sourceDatas = response.sourceDatas;

          // find and refresh the currently viewed source data
          if ($scope.currentSourceData) {
            angular.forEach(sourceDatas, function(sourceData) {
              if ($scope.currentSourceData.id === sourceData.id) {
                $scope.currentSourceData = sourceData;
              }
            });
          }

          // check for polling requirements
          angular.forEach(sourceDatas, function(sourceData) {
            if (sourceData.status === 'LOADING' || sourceData.status === 'REMOVING') {
              $scope.startPolling(sourceData);
            }
            ;
          });

          refreshTables();
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
          sourceData.status = 'LOADING';
          // TODO Reenable polling
          // $scope.startPolling(sourceData);
        });

      };

      $scope.startSourceDataPolling = function(sourceData) {
        console.log('Starting status polling for ' + sourceData.name);

        // TODO Ensure Brian notices my rebellion with polling interval of
        // 1.001s!
        $scope.loadingPolls[sourceData.id] = $interval(function() {

          var startStatus = sourceData.status;

          // get the source data by id
          sourceDataService.getSourceData(sourceData.id).then(
            function(polledSourceData) {
              // if cannot retrieve or no longer loading, cancel polling
              if (!polledSourceData || polledSourceData.status !== startStatus) {
                console.log('Status change detected for ' + sourceData.name + ': '
                  + polledSourceData.status + ' (previously ' + startStatus + ')');
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
        for ( var key in $scope.loadingPolls) {
          if ($scope.loadingPolls.hasOwnProperty(key)) {
            $interval.cancel($scope.loadingPolls[key]);
          }
        }
      });

      $scope.processStatusChange = function(sourceData) {
        switch (sourceData.status) {
        case 'LOADING_COMPLETE':

          break;
        case 'LOADING_FAILED':
          break;
        case 'REMOVAL_COMPLETE':
          break;
        case 'REMOVAL_FAILED':
          break;
        }
      }

      //
      // Angular File Upload controls
      //

      function isZipFile(item) {
        return item.file.name.match(/.*\.zip/g) !== null;
      }

      // Specify the angular-file-uploader
      var uploader = $scope.uploader = new FileUploader({
        url : fileUrl + 'upload'
      });

      // FILTERS

      uploader.filters = [];

      // CALLBACKS
      uploader.onWhenAddingFileFailed = function(item /* {File|FileLikeObject} */, filter, options) {
        // console.info('onWhenAddingFileFailed', item, filter, options);
      };
      uploader.onAfterAddingFile = function(fileItem) {
        // console.info('onAfterAddingFile', fileItem);
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
      };
      uploader.onBeforeUploadItem = function(item) {

        // dynamically set the upload url with the unzip flag
        item.url = fileUrl + '/upload/' + $scope.currentSourceData.id + '?unzip='
          + (item.unzip ? 'true' : 'false');

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
        // console.info('onProgressAll', progress);
      };
      uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', uploader, fileItem, response, status, headers);
        uploader.queue = uploader.queue.filter(function(item) {
          return !item.isSuccess;
        })
      };
      uploader.onErrorItem = function(fileItem, response, status, headers) {
        // console.info('onErrorItem', fileItem, response, status, headers);
        utilService
          .handleError({
            data : response ? response
              : "Folders cannot be uploaded; only single files or zip files containing no folders may be uploaded.",
            status : status,
            headers : headers
          }); // shoehorn into tsApp expected error format
      };

      uploader.onCancelItem = function(fileItem, response, status, headers) {
        // console.info('onCancelItem', fileItem, response, status, headers);
      };

      uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
      };
      uploader.onCompleteAll = function() {
        console.info('onCompleteAll', uploader);
        retrieveSourceDatas();
      };

      //
      // Initialize if USER
      //
      if (securityService.isUser()) {
        retrieveSourceDatas();
        getSourceDataHandlers();
      }

    });