// Landingcontroller
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
              console.debug('rerouting to first available tab');
              tabService.setSelectedTabForIndex(0);
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

        // If no login page, set guest user
        if (appConfig.loginEnabled !== 'true') {
          securityService.setGuestUser();
        }

      } ]);