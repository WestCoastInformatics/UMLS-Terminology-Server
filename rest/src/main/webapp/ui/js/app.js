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
    	  
    	  $scope.test = ["1", "2", "3"];
    	  
    	// the default viewed terminology, if available
        var defaultTerminology = 'SNOMEDCT_US';

        $scope.$watch('component', function() {
          // // console.debug("Component changed to ",
          // $scope.component);
        });
        
        // the currently viewed terminology (set by default or user)
        $scope.terminology = null;
        
      
        
        // query autocomplete variables
        $scope.conceptQuery = null;
        $scope.autocompleteUrl = null; // set on terminology change
        
        // the displayed component
        $scope.component = null;

        // whether to show suppressible/obsolete component elements
        $scope.showSuppressible = true;
        $scope.showObsolete = true;
        
        // basic scope variables
        $scope.userName = null;
        $scope.authToken = null;
        $scope.error = "";
        $scope.glassPane = 0;
        
        // full variable arrays
        $scope.searchResults = null;
        
        $scope.handleError = function(data, status, headers, config) {
          $scope.error = data.replace(/"/g, '');
        }

        $scope.clearError = function() {
          $scope.error = null;
        }

        $scope.setTerminology = function(terminology) {
          $scope.terminology = terminology;
        }
      
        /**
         * Watch selected terminology and perform necessary operations
         */
        $scope.$watch('terminology', function() {
          
          // clear the terminology-specific variables
          $scope.autoCompleteUrl = null;
          $scope.metadata = null;
          
          // clear the search result list and current component
          $scope.component = null;
          $scope.componentError = null;
          $scope.searchResults = null;
          
          // reset the history
          $scope.conceptHistory = [];
          $scope.conceptHistoryIndex = -1;

          // if no terminology specified, stop
          if ($scope.terminology == null) {
            // console.debug("Returning")
            return;
          }
          
          // set the autocomplete url
          $scope.autocompleteUrl = contentUrl + getTypePrefix($scope.terminology) + '/autocomplete/' + $scope.terminology.terminology + '/' + $scope.terminology.terminologyVersion;
       
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
        	if (searchTerms == null || searchTerms == undefined || searchTerms.length < 2) {
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

        /**
         * Get all available terminologies and store in scope.terminologies
         */
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

            // results are in pair list, want full terminologies
            for (var i = 0; i < data.keyValuePairList.length; i++) {
              var pair = data.keyValuePairList[i].keyValuePair[0];

              var terminologyObj = {
                name : pair['key'],
                version : pair['value']
              };
             
              // call helper function to get the full terminology object
              var terminologyObj = $scope.getTerminology(pair['key'], pair['value']);
            
              terminologyObj.then(function(terminology) {
            	  
            	  // add result to the list of terminologies
            	  if (terminology.terminology != 'MTH' && terminology.terminology != 'SRC') {
            		  $scope.terminologies.push(terminology);
            	  
            	  if (terminology.terminology === defaultTerminology) {
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

        /** 
         * Function to get a concept based on terminology.  Two modes:
         * (1) Full terminology object passed
         * (2) Only terminology name passed, must be in list of available terminologies
         */
        $scope.getConcept = function(terminology, terminologyId) {
        	
        	// clear existing component
        	$scope.component = null;
        	$scope.componentError = null;
        	
        	var localTerminology = null;
        	
        	// check for full terminology object by comparing to selected terminology
        	if (terminology != $scope.terminology) {
        		
        		// cycle over available terminologies for match
        		for (var i = 0; i < $scope.terminologies.length; i++) {
        			if ($scope.terminologies[i].terminology === terminology) {
        				localTerminology = $scope.terminologies[i];
        			}
        		}
        	}
        	
        	if (!localTerminology) {
        		$scope.componentError = "Requested terminology " + terminology + " not found";
        		return;
        	}
        	
            // get single concept
            $scope.glassPane++;
            $http(
            {
              url : contentUrl + getTypePrefix(localTerminology.terminology, terminologyId) + "/" + localTerminology.terminology + "/" + localTerminology.terminologyVersion + "/"
                + terminologyId,
              method : "GET",

            }).success(function(data) {
            	
            if (!data) {
            	$scope.componentError = "Could not retrieve data for " + terminology + "/" + terminologyId;
            	return;
            }
            
            // if local terminology matches passed terminology, attempt to set active row
            if (terminology === localTerminology)
            	setActiveRow(terminologyId);
            
            // set the component
            $scope.setComponent(data);
           
            // update history
            $scope.addConceptToHistory(data.terminology, data.terminologyId);
               
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }
        
        /**
         * Clear the search box and perform any additional operations required
         */
        $scope.clearQuery = function() {
        	$scope.suggestions = null;
        	$scope.conceptQuery = null;
        }

        /**
         * Find concepts based on terminology and queryStr
         * Does not currently use any p/f/s settings
         */
        $scope.findConcepts = function(terminology, queryStr) {

          // ensure query string has minimum length
          if (queryStr == null || queryStr.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }
          
          // console.debug("Finding concepts for ", terminology, queryStr);

          // clear concept and suggestions
          $scope.suggestions = null;
          $scope.component = null;
          $scope.componentError = null;
          $scope.conceptHistory = [];
          $scope.conceptHistoryIndex = -1;
          
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

        /**
         * Sets the component and performs any operations required
         */
        $scope.setComponent = function(component, componentType) {
          // set the component
          $scope.component = component;
          
          // apply the initial paging
          applyPaging();
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
        	
        	applyPaging();
        	
        }
        
        $scope.toggleSuppressible= function() {
        	if ($scope.showSuppressible == null || $scope.showSuppressible == undefined) {
        		$scope.showSuppressible = false;
        	} else {
        		$scope.showSuppressible = !$scope.showSuppressible;
        	}
        	
        	applyPaging();
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
        function getTypePrefix(terminology, terminologyId) {
          console.debug('getTypePrefix', terminology, terminologyId);
          switch (terminology) {
          case 'SNOMEDCT_US':
        	  return 'cui';
          case 'UMLS':
        	  if (terminologyId.indexOf("D") == 0)
        		  return 'dui';
        	  if (terminologyId.indexOf("C") == 0)
        		  return 'cui';
        	  return 'code';
          case 'MSH':
        	  return 'dui';
          default:
              return 'cui';
          }
        }
        
        //////////////////////////////////////
        // History Functions
        //////////////////////////////////////
        
        // concept navigation variables
        $scope.conceptHistory = [];
        $scope.conceptHistoryIndex = -1;  // index is the actual array index (e.g. 0:n-1)
        
        $scope.addConceptToHistory = function(terminology, terminologyId) {
        	
        	// if history exists
        	if ($scope.conceptHistoryIndex != -1) {
        		
        		// if this concept is the current historical concept, do not add
        		if ($scope.conceptHistory[$scope.conceptHistoryIndex].terminology === terminology 
        				&& $scope.conceptHistory[$scope.conceptHistoryIndex].terminologyId === terminologyId)
        			return;
        	}
        	
        	// delete further future history if in middle of history
        	if ($scope.conceptHistoryIndex != $scope.conceptHistory.length -1) {
        		
        		// slice from beginning to current item
        		$scope.conceptHistory = $scope.conceptHistory.slice(0, $scope.conceptHistoryIndex + 1);
        	}
        	
        	// add item and increment index
        	$scope.conceptHistory.push({'terminology':terminology, 'terminologyId':terminologyId});
        	$scope.conceptHistoryIndex++;
        }
        
        $scope.getPreviousConcept = function() {
        	
        	// decrement the counter and get the concept
        	$scope.conceptHistoryIndex--;
        	$scope.getConcept(
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminology, 
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminologyId)
        	
        }
        
        $scope.getPreviousConceptStr = function() {
        	return $scope.conceptHistory[$scope.conceptHistoryIndex-1].terminology + "/" + $scope.conceptHistory[$scope.conceptHistoryIndex-1].terminologyId;
        }
        
        $scope.getNextConcept = function() {
        	
        	// increment the counter and get the concept
        	$scope.conceptHistoryIndex++;
        	$scope.getConcept(
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminology,
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminologyId)
        	
        }
        
        ////////////////////////////////////
        // Pagination functions
        ////////////////////////////////////
       
        // paged variable lists
        $scope.pagedSearchResults = null;	
        $scope.pagedSemanticTypes = null;
        $scope.pagedDescriptions = null;
        $scope.pagedRelationships = null;
        $scope.pagedAtoms = null;
        
        // variable page numbers
        $scope.searchResultsPage = 1;
        $scope.semanticTypesPage = 1;
        $scope.descriptionsPage = 1;
        $scope.relationshipsPage = 1;
        $scope.atomsPage = 1;
        
        // default page size
        $scope.pageSize = 10;
        
        function applyPaging() {

        	if ($scope.component.hasOwnProperty('atom'))
        		$scope.pagedAtoms = $scope.getPagedArray($scope.component.atom, $scope.atomPage, true, false);
        	
        	if ($scope.component.hasOwnProperty('relationship'))
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
        
        /** Helper function to get properties for ng-repeat */
        function convertObjectToJsonArray() {
        	var newArray = new Array();
        	for (var prop in object) {
        		var obj = { key:prop, value:object[prop]};
        		newArray.push(obj);
        	}
        }
       

      } ]);
