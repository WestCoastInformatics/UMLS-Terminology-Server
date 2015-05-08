'use strict'

var baseUrl = '';
var securityUrl = baseUrl + 'security/';
var contentUrl = baseUrl + 'content/';
var metadataUrl = baseUrl + 'metadata/';
var historyUrl = baseUrl + 'history/';
var glassPane = 0;

var tsApp = angular.module('tsApp', [ 'ui.bootstrap' ]).config(function() {

})

tsApp.run(function($http) {

})

// currently unused
tsApp.directive('conceptReport', function() {
	 return {
		    replace : false,
		    restrict : 'AE',
		    templateUrl : 'ui/partials/conceptReport.html',
		    scope : {
		      concept : '=', // two-way binding in anticipation of editing
		      terminology : '@', // isolate scope as terminology will not change
		      showSuppressible : '=',  // two way binding for showing Suppressible control
		      showObsolete : '=', // two way binding for showing Obsolete control
		      changeConcept : '&' // method link for following concept links
		   
		    },
		    link : function(scope, element, attrs) {
		    	
		    	scope.showItem = function(item) {
		    		
		    		if (scope.showSuppressible == false && item.suppressible == true)
		    			return false;
		    		
		    		if (scope.showObsolete == false && item.obsolete == true)
		    			return false;
		    		
		    		return true;
		    	}
		    
		    }
	 }

});

// currently unused
tsApp
  .controller(
    'tsIndexCtrl',
    [
      '$scope',
      '$http',
      '$q',
      function($scope, $http, $q) {

        $scope.$watch('component', function() {
          // console.debug("Component changed to ",
          // $scope.component);
        });
        
        // the viewed terminology
        $scope.terminology = null;

        // initialize the search results
        $scope.searchResult = [];
        $scope.resultsPage = 1;
        
        // the displayed component
        $scope.component = null;
        $scope.componentType = null;

        // whether to show suppressible/obsolete component elements
        $scope.showSuppressible = true;
        $scope.showObsolete = true;
        
        // basic scope variables
        $scope.userName = null;
        $scope.authToken = null;
        $scope.error = "";
        $scope.glassPane = 0;

        $scope.handleError = function(data, status, headers, config) {
          $scope.error = data.replace(/"/g, '');
        }

        $scope.clearError = function() {
          $scope.error = null;
        }

        $scope.setTerminology = function(terminology) {
          $scope.terminology = terminology;
        }
      
        $scope.$watch('terminology', function() {
          
          console.debug("terminology changed: ", $scope.terminology);

          if ($scope.terminology == null) {
            console.debug("Returning")
            return;
          }
          
          console.debug("Retrieving metadata");

          $scope.glassPane++;
          $http(
            {
              url : metadataUrl + 'all/terminology/id/' + $scope.terminology.terminology
                + '/' + $scope.terminology.terminologyVersion,
              method : "GET",
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            $scope.metadata = data.keyValuePairList;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        })

        $scope.login = function(name, password) {
        

          if (name == null) {
            alert("You must specify a user name");
            return;
          } else if (password == null) {
            alert("You must specify a password");
            return;
          }

          // login
          $scope.glassPane++;
          console.debug("Login called - " + securityUrl + 'authenticate/' + name);
          $http({
            url : securityUrl + 'authenticate/' + name,
            dataType : "text",
            data : password,
            method : "POST",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {
        	  console.log(name + " = " + data);
            $scope.userName = name;
            $scope.authToken = data;
            $scope.password = "";

            // set request header
            // authorization
            $http.defaults.headers.common.Authorization = $scope.authToken;

            // retrieve available
            // terminologies
            $scope.getTerminologies();
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.logout = function() {
          console.log("logout - " + $scope.authToken.replace(/"/g, ""));
          if ($scope.authToken == null) {
            alert("You are not currently logged in");
            return;
          }
          $scope.glassPane++;
          // logout
          $http({
            url : securityUrl + 'logout/' + $scope.authToken.replace(/"/g, ""),
            method : "GET",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {

            // clear scope variables
            $scope.userName = null;
            $scope.authToken = null;

            // clear http authorization
            // header
            $http.defaults.headers.common.Authorization = null;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }
        
        $scope.getTerminology = function(name, version) {
        	
        	var deferred = $q.defer();
        	 setTimeout(function() {
        		 $scope.glassPane++;
        	// login
            $http({
              url : metadataUrl + 'terminology/id/' + name + '/' + version,
              method : "GET",
              headers : {
                "Content-Type" : "text/plain"
              }
            }).success(function(data) {
            	$scope.glassPane--;
            	deferred.resolve(data);
            }).error(function(data, status, headers, config) {
            	 $scope.glassPane--;
                deferred.reject("Could not retrieve terminology " + name + ", " + version);
               
              });
        	 });
        	
        	 return deferred.promise;
        }

        $scope.getTerminologies = function() {

          // reset terminologies
          $scope.terminologies = null;
          $scope.glassPane++;

          // login
          $http({
            url : metadataUrl + 'terminology/terminologies',
            method : "GET",
            headers : {
              "Content-Type" : "text/plain"
            }
          }).success(function(data) {
            $scope.terminologies = new Array();
            // console
            // .debug(
            // "Retrieved terminologies:",
            // data.keyValuePairList);

            // construct objects from
            // returned data structure
            for (var i = 0; i < data.keyValuePairList.length; i++) {
              var pair = data.keyValuePairList[i].keyValuePair[0];

              var terminologyObj = {
                name : pair['key'],
                version : pair['value']
              };
              
             
              var terminologyObj = $scope.getTerminology(pair['key'], pair['value']);
              console.debug("Retrievig terminology" + pair['key'] + ", " + pair['value']);
              terminologyObj.then(function(terminology) {
            	  console.debug("  Retrieved", terminology.terminology);
            	  
            	  // add result to the list of terminologies
            	  if (terminology.terminology != 'MTH' && terminology.terminology != 'SRC') {
            		  $scope.terminologies.push(terminology);
            	  
            	  if (terminology.terminology === 'SNOMEDCT-US') {
                      console.debug('SNOMEDCT found');
                      $scope.setTerminology(terminology);
                  }
            	  console.debug("Current terminologies", $scope.terminologies);
            	  }
              } , function(reason) {
            	  // do error message here
              });
              
             
            }

            // select SNOMEDCT terminology if present
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.getConcept = function(terminology, terminologyId) {
          // get single concept
            $scope.glassPane++;
          $http(
            {
              url : contentUrl + getTypePrefix(terminology) + "/" + terminology.terminology + "/" + terminology.terminologyVersion + "/"
                + terminologyId,
              method : "GET",

            }).success(function(data) {
            $scope.concept = data;

            console.debug("Retrieved concept:", $scope.concept);

            setActiveRow(terminologyId);
            $scope.setComponent($scope.concept, 'Concept');
            
           // $scope.getParentAndChildConcepts($scope.concept);

            
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.findConcepts = function(terminology, queryStr) {

          // ensure query string has minimum length
          if (queryStr == null || queryStr.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }

          $scope.concept = null;

          var pfs = {
            startIndex : -1,
            maxResults : -1,
            sortField : null,
            queryRestriction : null
          } // 'terminologyId:' + queryStr }

          // find concepts
          $scope.glassPane++;
          $http(
            {
              url : contentUrl + getTypePrefix(terminology) + "/" + terminology.terminology + "/"
                + terminology.terminologyVersion + "/query/" + queryStr,
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved concepts:", data);
            $scope.searchResults = data.searchResult;
           
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.setComponent = function(component, componentType) {
          // console.debug("Setting component",
          // componentType, component);
          $scope.component = component;
          $scope.componentType = componentType;
        }
        
        $scope.showItem = function(item) {
        
        	if ($scope.showSuppressible == false && item.suppressible == true)
    			return false;
    		
    		if ($scope.showObsolete == false && item.obsolete == true)
    			return false;
    		
    		return true;
    	}
        
        $scope.toggleSuppressible = function() {
        	if ($scope.showSuppressible == null || $scope.showSuppressible == undefined) {
        		$scope.showSuppressible = false;
        	} else {
        		$scope.showSuppressible = !$scope.showSuppressible;
        	}
        }
        
        $scope.toggleObsolete = function() {
        	if ($scope.showObsolete == null || $scope.showObsolete == undefined) {
        		$scope.showObsolete = false;
        	} else {
        		$scope.showObsolete = !$scope.showObsolete;
        	}
        }

        function setActiveRow(terminologyId) {
          for (var i = 0; i < $scope.searchResults.length; i++) {
            if ($scope.searchResults[i].terminologyId === terminologyId) {
              $scope.searchResults[i].rowClass = "active";
            } else {
              $scope.searchResults[i].rowClass = "";
            }
          }
        }

        function getPfs() {
          return {
            startIndex : -1,
            maxResults : -1,
            sortField : null,
            queryRestriction : null
          };
        }
        
        function getTypePrefix(terminology) {
          switch (terminology.organizingClassType) {
          case 'CODE':
        	  return 'code';
          case 'CONCEPT':
        	  return 'cui';
          case 'DESCRIPTOR':
        	  return 'dui';
          default:
              return null;
          }
        }

      } ]);
