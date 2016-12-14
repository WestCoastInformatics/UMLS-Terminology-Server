// Terminology controller
tsApp.controller('TerminologyCtrl', [
  '$scope',
  '$http',
  '$location',
  'appConfig',
  'utilService',
  'tabService',
  'configureService',
  'securityService',
  'projectService',
  'metadataService',
  'contentService',
  function($scope, $http, $location, appConfig, utilService, tabService, configureService,
    securityService, projectService, metadataService, contentService) {
    console.debug("configure TerminologyCtrl");

    // Set up tabs and controller
    tabService.setShowing(true);
    utilService.clearError();
    $scope.user = securityService.getUser();
    projectService.getUserHasAnyRole();
    tabService.setSelectedTabByLabel('Terminology');

    // Scope vars
    $scope.appConfig = appConfig;
    $scope.selected = {
      terminology : null,
      rootTerminology : null
    }
    $scope.lists = {
      metadata : metadataService.getModel(),
      terminologies : [],
      precedenceList : []
    }
    // instantiate paging and paging callbacks function
    $scope.pagedTerminologies = [];
    $scope.paging = {};
    $scope.paging['t'] = utilService.getPaging();
    $scope.paging['t'].sortAsending = true;
    $scope.paging['t'].sortField = 'terminology';
    $scope.paging['t'].pageSize = 50;
    $scope.paging['t'].callbacks = {
      getPagedList : getPagedList
    };

    // Paging function
    $scope.getPagedList = function() {
      getPagedList();
    }
    function getPagedList() {
      $scope.pagedTerminologies = utilService.getPagedArray($scope.lists.terminologies,
        $scope.paging['t']).data;
    }

    // Table sorting mechanism
    $scope.setSortField = function(table, field) {
      utilService.setSortField(table, field, $scope.paging);
      $scope.getPagedList();
    };

    // Return up or down sort chars if sorted
    $scope.getSortIndicator = function(table, field) {
      return utilService.getSortIndicator(table, field, $scope.paging);
    };

    // Set $scope.selected.terminology
    $scope.setTerminology = function(terminology) {

      metadataService.setTerminology(terminology);

      if ($scope.selected.terminology && $scope.selected.terminology.id == terminology.id) {
        $scope.selected.terminology = null;
        $scope.selected.rootTerminology = null;
        return;
      }
      $scope.selected.terminology = terminology;
      metadataService.getRootTerminology(terminology.terminology).then(
      // Success
      function(data) {
        $scope.selected.rootTerminology = data;
        $scope.user.userPreferences.lastTerminology = $scope.selected.terminology.terminology;
        securityService.updateUserPreferences($scope.user.userPreferences);
      });
    }

    // Put citation together
    $scope.getCitationValue = function(citation) {
      if (!citation) {
        return 'n/a';
      }
      var str = (citation.author + '; ' + citation.address + '; ' + citation.organization + '; '
        + citation.editor + '; ' + citation.title + '; ' + citation.contentDesignator + '; '
        + citation.mediumDesignator + '; ' + citation.edition + '; ' + citation.placeOfPublication
        + '; ' + citation.publisher + '; ' + citation.dateOfPublication + '; '
        + citation.dateOfRevision + '; ' + citation.location + '; ' + citation.extent + '; '
        + citation.series + '; ' + citation.notes).replace(/null/g, '').replace(/ ;/g, '').replace(
        /^; /g, '').replace(/; $/, '');
      if (!str) {
        return "n/a";
      }
      return str;
    }

    // Put contact together
    $scope.getContactValue = function(contact) {
      if (!contact) {
        return 'n/a';
      }
      var str = (contact.name + '; ' + contact.title + '; ' + contact.organization + '; '
        + contact.address1 + '; ' + contact.address2 + '; ' + contact.city + '; '
        + contact.stateOrProvince + '; ' + contact.country + '; ' + contact.zipCode).replace(
        /null/g, '').replace(/ ;/g, '').replace(/; $/, '').replace(/^; /g, '');
      if (!str) {
        return "n/a";
      }
      return str;
    }

    // navigate to "metadata" tab
    $scope.navigateMetadata = function() {

      metadataService.getAllMetadata($scope.selected.terminology.terminology,
        $scope.selected.terminology.version).then(
      // Success
      function(data) {
        // Set the shared model in the metadata service
        metadataService.setModel(data);
        metadataService.setTerminology($scope.selected.terminology);
        $location.path('/metadata');
      });
    }

    // navigate to "content" tab
    $scope.navigateContent = function() {
      $scope.user.userPreferences.lastTerminology = $scope.selected.terminology.terminology;
      securityService.updateUserPreferences($scope.user.userPreferences);
      $location.path('/content');

    }

    // Export to simple format
    $scope.export = function() {
      contentService.exportTerminologySimple($scope.selected.terminology.terminology,
        $scope.selected.terminology.version);
    }

    //
    // Initialize
    //

    $scope.initialize = function() {

      // configure tab
      securityService.saveTab($scope.user.userPreferences, '/metadata');

      metadataService.getTerminologies().then(
      // Success
      function(data) {
        $scope.lists.terminologies = data.terminologies;
        $scope.getPagedList();
        // Reselect
        if ($scope.user.userPreferences && $scope.user.userPreferences.lastTerminology) {
          for (var i = 0; i < $scope.lists.terminologies.length; i++) {
            var terminology = $scope.lists.terminologies[i];
            // set from user prefs
            if (terminology.terminology === $scope.user.userPreferences.lastTerminology) {
              $scope.setTerminology(terminology);
              found = true;
              break;
            }
          }
        }
      });

    };

    //
    // Initialization: Check that application is configured
    //
    configureService.isConfigured().then(function(isConfigured) {
      if (!isConfigured) {
        $location.path('/configure');
      } else {
        $scope.initialize();
      }
    });

  } ]);