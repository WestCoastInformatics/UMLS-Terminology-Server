// Report Service
var reportUrl = 'report';
tsApp.service('reportService', [
  '$http',
  '$q',
  '$window',
  'gpService',
  'utilService',
  function($http, $q, $window, gpService, utilService) {

    // get concept report
    this.getComponentReport = function(projectId, component) {
      var deferred = $q.defer();

      // Get projects
      // gpService.increment();
      $http.get(
        reportUrl + '/' + component.type.toLowerCase() + '/' + component.id
          + (projectId ? '?projectId=' + projectId : ''), {
          headers : {
            'Content-type' : 'text/plain'
          }
        }).then(
      // success
      function(response) {
        // gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        // gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };

    // Popout report into new window
    this.popout = function(component) {
      var currentUrl = window.location.href;
      var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
      var newUrl = baseUrl + '/content/report/' + component.type + '/' + component.terminology
        + '/' + component.id;
      var title = 'Report-' + component.terminology + '/' + component.version + ', '
        + component.terminologyId;
      var newWindow = $window.open(newUrl, title, 'width=500, height=600');
      newWindow.document.title = title;
      newWindow.focus();

    };

    // find report definitions
    this.findReportDefinitions = function(projectId) {
      console.debug('findReportDefinitions');
      var deferred = $q.defer();

      // Assign user to project
      gpService.increment();
      $http.get(
        projectUrl + '/definitions?projectId=' + projectId).then(
      // success
      function(response) {
        console.debug('  report definitions = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });
      return deferred.promise;
    };
    
    // Finds reports as a list
    this.findReportsByName = function(projectId, name, pfs) {

      console.debug('findReports', projectId, name, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(reportUrl + '/find?projectId=' + projectId + '&query=' + utilService.prepQuery('name:"' + name + '"'),
        utilService.prepPfs(pfs)).then(
      // success
      function(response) {
        console.debug('  reports = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      return deferred.promise;
    };

    // Generates a report
    this.generateReport = function(projectId, name, query, queryType, resultType) {

      console.debug('generateReport', projectId, query, pfs);
      // Setup deferred
      var deferred = $q.defer();

      // Make POST call
      gpService.increment();
      $http.post(reportUrl + '/generate/' + projectId + '?name=' + name + '&query=' + 
          utilService.prepQuery(query) + '&queryType=' + queryType + '&resultType=' + resultType).then(
      // success
      function(response) {
        console.debug('  report = ', response.data);
        gpService.decrement();
        deferred.resolve(response.data);
      },
      // error
      function(response) {
        utilService.handleError(response);
        gpService.decrement();
        deferred.reject(response.data);
      });

      return deferred.promise;
    };

    
    // end
  } ]);
