// License acceptance controller
tsApp
  .controller(
    'LicenseCtrl',
    [
      '$scope',
      '$location',
      'securityService',
      'utilService',
      'appConfig',
      'tabService',
      function($scope, $location, securityService, utilService, appConfig, tabService) {
        console.debug('configure LicenseCtrl');

        // NOTE: Do NOT clear error here (to preserve license error messages)

        tabService.setShowing(false);

        $scope.licenseChecked = false;

        // check license
        securityService.checkLicense().then(
        // license accepted
        function(response) {
          console.debug('license cookie', response);
          $scope.acceptLicense();
        },
        // license not accepted
        function() {
          console.debug('license not found');
          $scope.licenseChecked = true;
        });

        // function to launch application
        $scope.acceptLicense = function() {
          securityService.acceptLicense().then(function(response) {
            var user = securityService.getUser();
            if (user && user.userPreferences && user.userPreferences.lastTab) {
              $location.path(user.userPreferences.lastTab)
            } else {
              if (tabService.tabs.length == 0) {
                handleError('No tabs configured')
              }
              $location.path(tabService.getFirstViewableTab().link);
            }
          });

        };

        // force user to scroll to bottom before accepting
        $scope.disableAcceptButton = true;
        document.getElementsByName("licenseAgreement")[0].addEventListener("scroll",
          checkScrollHeight, false);
        function checkScrollHeight() {
          var agreementTextElement = document.getElementsByName("licenseAgreement")[0];
          if ((agreementTextElement.scrollTop + agreementTextElement.offsetHeight) >= agreementTextElement.scrollHeight) {
            $scope.disableAcceptButton = false;
          }
        }

        // Initialize

        // check scroll height initially to catch short fragments
        checkScrollHeight();
      } ]);