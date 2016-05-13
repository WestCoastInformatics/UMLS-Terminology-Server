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
      function($scope, $location, securityService, utilService, appConfig) {
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
            console.debug('rerouting to content');
            $location.path('/content');
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
        // check scroll height initially to catch short fragments
        checkScrollHeight();

      } ]);