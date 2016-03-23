// Content controller
tsApp.directive('pager', [ function() {
  console.debug('configure pager directive');
  return {
    restrict : 'A',
    scope : {
      paging : '=',
      count : '=',
      callback : '='
    },
    templateUrl : 'app/util/pager/pager.html',
  }
} ]);
