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
	// nothing yet -- may want to put metadata retrieval here
})

tsApp
  .controller(
    'tsIndexCtrl',
    [
      '$scope',
      '$http',
      '$q',
      function($scope, $http, $q) {

        $scope.$watch('component', function() {
          // // console.debug("Component changed to ",
          // $scope.component);
        });
        
        // the viewed terminology
        $scope.terminology = null;
        
        // autocomplete settings
        $scope.conceptQuery = null;
        $scope.autocompleteUrl = null; // set on terminology change

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
        
        // pagination variables
        $scope.pagedSearchResults = null;	
        $scope.pagedSemanticTypes = null;
        $scope.pagedDescriptions = null;
        $scope.pagedRelationships = null;
        $scope.pagedAtoms = null;
        $scope.pageSize = 10;
        
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
          
          // console.debug("terminology changed: ", $scope.terminology);

          if ($scope.terminology == null) {
            // console.debug("Returning")
            return;
          }
          
          // set the autocomplete url
          $scope.autocompleteUrl = contentUrl + getTypePrefix($scope.terminology) + '/autocomplete/' + $scope.terminology.terminology + '/' + $scope.terminology.terminologyVersion;
          
          // console.debug("Retrieving metadata");

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
          // console.debug("Login called - " + securityUrl + 'authenticate/' + name);
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
        	  
        	  $scope.clearError();
        	  
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
        
        $scope.autocomplete = function(terminology, searchTerms) {
        	// if invalid search terms, return empty array
        	if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
        		return new Array();
        	}
        	
        	var deferred = $q.defer();
        	
	    	// NO GLASS PANE
	    	$http({
	             url : $scope.autocompleteUrl,
	             method : "POST",
	             data: searchTerms,
	             headers : {
	               "Content-Type" : "text/plain"
	             }
	        }).success(function(data) {
	           	deferred.resolve(data.string);
	        }).error(function(data, status, headers, config) {
	            deferred.resolve(null); // hide errors
	              
	        });
	    	
	    	return deferred.promise;
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
              // console.debug("Retrievig terminology" + pair['key'] + ", " + pair['value']);
              terminologyObj.then(function(terminology) {
            	  // console.debug("  Retrieved", terminology.terminology);
            	  
            	  // add result to the list of terminologies
            	  if (terminology.terminology != 'MTH' && terminology.terminology != 'SRC') {
            		  $scope.terminologies.push(terminology);
            	  
            	  if (terminology.terminology === 'SNOMEDCT_US') {
                      // console.debug('SNOMEDCT found');
                      $scope.setTerminology(terminology);
                  }
            	  // console.debug("Current terminologies", $scope.terminologies);
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

            // console.debug("Retrieved concept:", $scope.concept);

            setActiveRow(terminologyId);
            $scope.setComponent($scope.concept, 'Concept');
            
           // $scope.getParentAndChildConcepts($scope.concept);

            
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }
        
        $scope.clearQuery = function() {
        	$scope.suggestions = null;
        	$scope.conceptQuery = null;
        }

        $scope.findConcepts = function(terminology, queryStr) {

          // ensure query string has minimum length
          if (queryStr == null || queryStr.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }
          
          // console.debug("Finding concepts for ", terminology, queryStr);

          // clear concept and suggestions
          $scope.suggestions = null;
          $scope.concept = null;
          
          // force the search box to sync with query string
          $scope.conceptQuery = queryStr;

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
            // console.debug("Retrieved concepts:", data);
            $scope.searchResults = data.searchResult;
            $scope.pagedSearchResults = $scope.getPagedArray($scope.searchResults, 1, false, null);
           
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.setComponent = function(component, componentType) {
          // // console.debug("Setting component",
          // componentType, component);
          $scope.component = component;
          $scope.componentType = componentType;
          
          // apply the initial paging
          applyInitialPaging();
        }
        
        //////////////////////////////////////////
        // Suppressible/Obsolete Functions
        /////////////////////////////////////////
        
        // default: show all
        $scope.showObsolete = true;
        $scope.showSuppressible = true;
        
        /** Determine if an item has obsolete elements
         *  in its child arrays
         */
        $scope.hasBooleanFieldTrue = function(object, fieldToCheck) {
        	
        	// check for proper arguments
        	if (object == null || object == undefined)
        		return false;
        	
        	// cycle over all properties
        	for (var prop in object) {
        		var value = object[prop];
        		 		
        		// if null or undefined, skip
        		if (value == null || value == undefined) { 
        			// do nothing
        		}
        			
        		
        		// if an array, check the array's objects
        		else if (Array.isArray(value) == true) {
        			for (var i = 0; i < value.length; i++) {
        				if (value[i][fieldToCheck] == true) {
        					return true;
        				}
        			}
        		}
        		
        		// if not an array, check the object itself
        		else if (value.hasOwnProperty(fieldToCheck) && value[fieldToCheck] == true) {
        			return true;
        		}
        			
        	}
        	
        	// default is false
        	return false;
        }
        
        /**
         * Helper function to determine whether an item
         * should be shown based on obsolete/suppressed
         * 
         * Array:  The containing array, with showSuppressible/showObsolete flag set
         * e.g. component.atom
         * 
         * Item:  The item in the containing array being evaluated
         */
        $scope.showItem = function(item) {
        
        	if ($scope.showSuppressible == false && item.suppressible == true)
    			return false;
    		
    		if ($scope.showObsolete == false && item.obsolete == true)
    			return false;
    		
    		return true;
    	}
        
        /** Functions to flip (and/or initialize) a toggle variable */
        $scope.toggleObsolete= function() {
        	if ($scope.showObsolete == null || $scope.showObsolete == undefined) {
        		$scope.showObsolete = false;
        	} else {
        		$scope.showObsolete = !$scope.showObsolete;
        	}
        	
        	applyInitialPaging();
        	
        }
        
        $scope.toggleSuppressible= function() {
        	if ($scope.showSuppressible == null || $scope.showSuppressible == undefined) {
        		$scope.showSuppressible = false;
        	} else {
        		$scope.showSuppressible = !$scope.showSuppressible;
        	}
        	
        	applyInitialPaging();
        }
        
     
        ///////////////////////////////
        // Misc helper functions
        ///////////////////////////////
        
        
        /** Set selected item to active row (for formatting purposes */
        function setActiveRow(terminologyId) {
          for (var i = 0; i < $scope.searchResults.length; i++) {
            if ($scope.searchResults[i].terminologyId === terminologyId) {
              $scope.searchResults[i].rowClass = "active";
            } else {
              $scope.searchResults[i].rowClass = "";
            }
          }
        }

        /** Construct a default PFS object */
        function getPfs() {
          return {
            startIndex : -1,
            maxResults : -1,
            sortField : null,
            queryRestriction : null
          };
        }
        
        /** Get the Type Prefix for HTML calls, e.g. for /code, /cui, /dui */
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
        
        ////////////////////////////////////
        // Pagination functions
        ////////////////////////////////////
        
        function applyInitialPaging() {

        	$scope.pagedAtoms = $scope.getPagedArray($scope.component.atom, $scope.atomPage, true, false);
        	$scope.pagedRelationships = $scope.getPagedArray($scope.component.relationship, $scope.relationshipPage, true, false);
        	// TODO Add others
        }
        
        
        $scope.getPagedArray = function(array, page, applyFlags, filterStr) {
        	
        	var newArray = new Array();
        	
        	// if array blank or not an array, return blank list
        	if (array == null || array == undefined || Array.isArray(array) == false)
        		return newArray;
        	
        	// apply page 1 if not supplied
        	if (!page)
        		page = 1;
        	
        	newArray = array;
        	
        	// apply flags
        	if (applyFlags == true) {
        		newArray = getArrayByFlags(newArray);
        	}
        	
        	// apply filter
        	if (filterStr) {
        		newArray = getArrayByFilter(filterStr);
        	}

        	// slice the flagged/filtered results
        	var fromIndex = (page-1)*$scope.pageSize;
        	var toIndex = Math.min(fromIndex + $scope.pageSize, array.length);
        	
        	console.debug("  results", fromIndex, toIndex, array.slice(fromIndex, toIndex));

        	return newArray.slice(fromIndex, toIndex);
        }
        
        /** Get array with suppressed/obsolete flags applied */
        function getArrayByFlags(array) {
        	
        	var newArray = new Array();
        	
        	// if array blank or not an array, return blank list
        	if (array == null || array == undefined || Array.isArray(array) == false)
        		return newArray;
        	
        	// apply obsolete and suppressible flags
        	for (var i = 0; i < array.length; i++) {
        		if ($scope.showItem(array[i]) == true) {
        			newArray.push(array[i]);
        		}
        	}
        	
        	return newArray;
        }
        
        /** Get array by filter text matching terminologyId or name */
        function getArrayByFilterText(array, filter) {
        	var newArray = array;
        	
        	return newArray;
        	
        	// TODO
        }
        
       

      } ]);
