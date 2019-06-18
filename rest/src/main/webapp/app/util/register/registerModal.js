tsApp
		.controller(
				'RegisterModalCtrl',
				[
						'$rootScope',
						'$scope',
						'$http',
						'securityService',
						'$uibModalInstance',
						'appConfig',
						function($rootScope, $scope, $http, securityService,
								$uibModalInstance, appConfig) {

							$scope.appConfig = appConfig;
							$scope.errors = [];
							$scope.appName = appConfig['deploy.title'];
							
							// update to user
							$scope.register = function() {
								$scope.errors = [];
								if ($scope.reg == null 
										|| $scope.reg.user == null
										|| $scope.reg.user.name == null
										|| $scope.reg.user.name.trim() === "") {
									$scope.errors.push("Name is required.");
								}
								
								if ($scope.reg == null 
										|| $scope.reg.user == null
										|| $scope.reg.user.email == null 
										|| $scope.reg.user.email.trim() === "") {
									$scope.errors.push("Email is required.");
								}
								else if (!validateEmail($scope.reg.user.email)) {
									$scope.errors.push("Email must be valid.");
								}

								if ($scope.errors.length === 0 ) {									
									$scope.userName = securityService.getUser().userName;
									securityService.getUserByName($scope.userName).then(
											function(data) {
												var user = data;
												// update Name and email
												user.name = $scope.reg.user.name;
												user.email = $scope.reg.user.email;

												securityService.updateUser(user).then(
												// success
												function(data) {
													// if everything OK, close the form
													$uibModalInstance.close(data);
												}, function(data) {
													$scope.errors[0] = data;
													utilService.clearError();
													$uibModalInstance.close(data);
												});
											});
								}
							};
							
							function validateEmail(email) {
								var regex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
								email = email.trim();
								return !(email === "" || !regex.test(email))
							}
} ]);