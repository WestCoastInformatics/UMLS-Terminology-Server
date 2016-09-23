// Report Service
var reportUrl = 'report';
tsApp.service('reportService', [
  '$http',
  '$q',
  '$window',
  'gpService',
  'utilService',
  function($http, $q, $window, gpService, utilService) {
    console.debug('configure reportService');

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
      var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#')+1);
      var newUrl = baseUrl + '/content/report/' + component.type + '/' + component.terminology
        + '/' + component.id;
      var title = 'Report-' + component.terminology + '/' + component.version + ', '
        + component.terminologyId;
      var newWindow = $window.open(newUrl, title, 'width=500, height=600');
      newWindow.document.title = title;
      newWindow.focus();

    };

    // end
  } ]);
