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
        var defaultTerminology = 'UMLS';

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
          $scope.autocompleteUrl = contentUrl + getUrlPrefix($scope.terminology.organizingClassType) + '/autocomplete/' + $scope.terminology.terminology + '/' + $scope.terminology.terminologyVersion;
       
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
        
        $scope.autocomplete = function(searchTerms) {
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
            	  
            	  terminology.hidden = (terminology.terminology === 'MTH' || terminology.terminology === 'SRC');
           	  
            	  // add result to the list of terminologies
            	  $scope.terminologies.push(terminology);
            	  
            	  if (terminology.terminology === defaultTerminology) {
                      $scope.setTerminology(terminology);
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
         * Helper function to get full terminology object given terminology name
         */
        function getTerminologyFromName(terminologyName) {
        	// check for full terminology object by comparing to selected terminology
        	if (terminologyName != $scope.terminology.terminology) {
        		
        		// cycle over available terminologies for match
        		for (var i = 0; i < $scope.terminologies.length; i++) {
        			if ($scope.terminologies[i].terminology === terminologyName) {
        				return $scope.terminologies[i];
        			}
        		}
        	} else {
        		return $scope.terminology;
        	}
        	
        	
        }
        
        /**
         * Function to get a component of the terminology's organizing class type.
         * TODO:  Get rid of this and have conceptTErminologyIds retrieved by getConcept
         */
        $scope.getComponent = function(terminologyName, terminologyId) {
        	
        	console.debug('getComponent', terminologyName, terminologyId);
        	
        	// if terminology matches scope terminology
        	if (terminologyName === $scope.terminology.terminology) {
        		getComponentHelper($scope.terminology, terminologyId, getUrlPrefix($scope.terminology.organizingClassType));
        	
        	// otherwise get the terminology first
        	} else {
        		var localTerminology = getTerminologyFromName(terminologyName);
        		getComponentHelper(localTerminology, terminologyId, getUrlPrefix(localTerminology.organizingClassType));
        	}
        }

        /** 
         * Function to get a concept for a terminology.  Does not trigger on terminology class type.
         */
        $scope.getConcept = function(terminologyName, terminologyId) {
        	
        	console.debug('getConcept', terminologyName, terminologyId);
        	
        	// if terminology matches scope terminology
        	if (terminologyName === $scope.terminology.terminology) {
        		getComponentHelper($scope.terminology, terminologyId, getUrlPrefix('CONCEPT'));
        	
        	// otherwise get the terminology first
        	} else {
        		var localTerminology = getTerminologyFromName(terminologyName);
        		getComponentHelper(localTerminology, terminologyId, getUrlPrefix('CONCEPT'));
        	}
        }
        
        /**
         * Function to get a descriptor for a terminology.  Does not trigger on terminology class type.
         */
        $scope.getDescriptor = function(terminologyName, terminologyId) {
        	
        	console.debug('getDescriptor', terminologyName, terminologyId);
        	
        	// if terminology matches scope terminology
        	if (terminologyName === $scope.terminology.terminology) {
        		getComponentHelper($scope.terminology, terminologyId, getUrlPrefix('DESCRIPTOR'));
        	} else {
        		var localTerminology = getTerminologyFromName(terminologyName);
        		getComponentHelper(localTerminology, terminologyId, getUrlPrefix('DESCRIPTOR'));
        	}
        	
        }
        
        /**
         * Function to get a code for a terminology.  Does not trigger on terminology class type;
         */
        $scope.getCode = function(terminologyName, terminologyId) {
        	
        	console.debug('getCode', terminologyName, terminologyId);
        	
        	// if terminology matches scope terminology
        	if (terminologyName === $scope.terminology.terminology) {
        		getComponentHelper($scope.terminology, terminologyId, getUrlPrefix('CODE'));
        	} else {
        		var localTerminology = getTerminologyFromName(terminologyName);
        		getComponentHelper(localTerminology, terminologyId, getUrlPrefix('CODE'));
        	}
        }
        
        /**
         * Helper function called by getConcept, getDescriptor, and getCode
         * - terminologyObj:  the full terminology object from getTerminology()
         * - terminologyId:   the terminology id of the component
         * - typePrefix:      the url prefix denoting object type (cui/dui/code)
         * 
         * TODO:  Add definitions from all atoms to the top level component 
         */
        function getComponentHelper(terminologyObj, terminologyId, typePrefix) {
        	
        	console.debug('getComponentHelper', terminologyObj, terminologyId, typePrefix);
        	
        	// clear existing component and paging
        	$scope.component = null;
        	$scope.componentError = null;
        	clearPaging();
        	
        	if (!terminologyObj || !terminologyId || !typePrefix) {
        		$scope.componentError = "An unexpected display error occurred.<p>Click a concept or perform a new search to continue";
        		return;
        	}
        	
            // get single concept
            $scope.glassPane++;
            $http(
            {
              url : contentUrl + typePrefix + "/" + terminologyObj.terminology + "/" + terminologyObj.terminologyVersion + "/"
                + terminologyId,
              method : "GET",

            }).success(function(data) {
            	
            	// update history
                $scope.addConceptToHistory(data.terminology, data.terminologyId);
            	
	            if (!data) {
	            	$scope.componentError = "Could not retrieve " + typePrefix + " data for " + terminologyObj.terminology + "/" + terminologyId;
	            	$scope.glassPane--;
	            	return;
	            }
	            
	            // if local terminology matches passed terminology, attempt to set active row
	            if ($scope.terminology === terminologyObj)
	            	setActiveRow(terminologyId);
	            
	            // cycle over all atoms looking for definitions
	            for (var i = 0; i < data.atom.length; i++) {
	            	for (var j = 0; j < data.atom[i].definition.length; j++) {
	            		var definition = data.atom[i].definition[j];
	            		
	            		// set the atom element flag
	            		definition.atomElement = true;
	            		
	            		// add the atom information for tooltip display
	            		// TODO: This is kind of clunky/hackish, consider further
	            		// Format:  name [terminology/termType]			
	            		definition.atomElementStr = data.atom[i].name + " [" + data.atom[i].terminology + "/" + data.atom[i].termType + "]";
	            		
	            		// add the definition to the top level component
	            		data.definition.push(definition);
	            	}
	            }
	            
	            // set the component
	            $scope.setComponent(data);

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
         * NOTE: Always uses the selected terminology
         */
        $scope.findConcepts = function(queryStr) {
        	
        	console.debug('find concepts', queryStr);

          // ensure query string has minimum length
          if (queryStr == null || queryStr.length < 3) {
            alert("You must use at least three characters to search");
            return;
          }
    
          // clear concept, history, suggestions, and paging data
          $scope.suggestions = null;
          $scope.component = null;
          $scope.componentError = null;
          $scope.conceptHistory = [];
          $scope.conceptHistoryIndex = -1;
          clearPaging();
          
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
              url : contentUrl + getUrlPrefix($scope.terminology.organizingClassType) + "/" + $scope.terminology.terminology + "/"
                + $scope.terminology.terminologyVersion + "/query/" + queryStr,
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
        
        ///////////////////////////////
        // Show/Hide List Elements
        ///////////////////////////////
        
        // variables for showing/hiding elements based on boolean fields
        $scope.showSuppressible = true;
        $scope.showObsolete = true;
        $scope.showAtomElement = true;

        
        /** Determine if an item has boolean fields set to true
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
         */
        $scope.showItem = function(item) {
        
        	// trigger on suppressible (model data)
        	if ($scope.showSuppressible == false && item.suppressible == true)
    			return false;
    		
        	// trigger on obsolete (model data)
    		if ($scope.showObsolete == false && item.obsolete == true)
    			return false;
    		
    		// trigger on applied showAtomElement flag
    		if ($scope.showAtomElement == false && item.atomElement == true)
    			return false;
    		
    		return true;
    	}
        
        /** Function to toggle obsolete flag and apply paging */
        $scope.toggleObsolete= function() {
        	if ($scope.showObsolete == null || $scope.showObsolete == undefined) {
        		$scope.showObsolete = false;
        	} else {
        		$scope.showObsolete = !$scope.showObsolete;
        	}
        	
        	applyPaging();
        	
        }
        
        /** Function to toggle suppressible flag and apply paging */
        $scope.toggleSuppressible= function() {
        	if ($scope.showSuppressible == null || $scope.showSuppressible == undefined) {
        		$scope.showSuppressible = false;
        	} else {
        		$scope.showSuppressible = !$scope.showSuppressible;
        	}
        	
        	applyPaging();
        }
        
        /** Function to toggle atom element flag and apply paging */    
        $scope.toggleAtomElement= function() {
        	if ($scope.showAtomElement == null || $scope.showAtomElement == undefined) {
        		$scope.showAtomElement = false;
        	} else {
        		$scope.showAtomElement = !$scope.showAtomElement;
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
        
        /**  Helper function to get the proper html prefix based on class type  */
        function getUrlPrefix(classType) {
        	
        	switch(classType) {
        	case 'CONCEPT':
        		return 'cui';
        	case 'DESCRIPTOR':
        		return 'dui';
        	case 'CODE':
        		return 'code';
        	default:
        		return 'prefixErrorDetected';
        	}

        }
           
        /** Helper function to get properties for ng-repeat */
        function convertObjectToJsonArray() {
        	var newArray = new Array();
        	for (var prop in object) {
        		var obj = { key:prop, value:object[prop]};
        		newArray.push(obj);
        	}
        }
        
        //////////////////////////////////////
        // Navigation History
        //////////////////////////////////////
        
        // concept navigation variables
        $scope.conceptHistory = [];
        $scope.conceptHistoryIndex = -1;  // index is the actual array index (e.g. 0:n-1)
        
        // add a terminology/terminologyId pair to the history stack
        $scope.addConceptToHistory = function(terminology, terminologyId) {
        	
        	console.debug("Adding concept to history", terminology, terminologyId);
        	
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
        
        // get and display the previous concept
        $scope.getPreviousConcept = function() {
        	
        	// decrement the counter and get the concept
        	$scope.conceptHistoryIndex--;
        	$scope.getConcept(
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminology, 
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminologyId)
        	
        }
        
        // get a string representing the previous concept
        $scope.getPreviousConceptStr = function() {
        	
        	if(!$scope.conceptHistory[$scope.conceptHistoryIndex-1])
        		return null;
        	
        	return $scope.conceptHistory[$scope.conceptHistoryIndex-1].terminology + "/" + $scope.conceptHistory[$scope.conceptHistoryIndex-1].terminologyId;
        }
        
        // get and display the next concept
        $scope.getNextConcept = function() {
        	
        	// increment the counter and get the concept
        	$scope.conceptHistoryIndex++;
        	$scope.getConcept(
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminology,
        			$scope.conceptHistory[$scope.conceptHistoryIndex].terminologyId)
        	
        }
        
        // get a string representing the next concept
        $scope.getNextConceptStr = function() {
        	
        	if(!$scope.conceptHistory[$scope.conceptHistoryIndex+1])
        		return null;
        	
        	return $scope.conceptHistory[$scope.conceptHistoryIndex+1].terminology + "/" + $scope.conceptHistory[$scope.conceptHistoryIndex+1].terminologyId;
        }
        
        ////////////////////////////////////
        // Pagination
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
        
        // reset all paginator pages
        function clearPaging() {
        	$scope.searchResultsPage = 1;
            $scope.semanticTypesPage = 1;
            $scope.descriptionsPage = 1;
            $scope.relationshipsPage = 1;
            $scope.atomsPage = 1;

            // TODO Add others
        }
        
        // apply paging to all elements
        function applyPaging() {
        	
        	// call each get function without paging (use current paging info)
        	$scope.getPagedAtoms();
        	$scope.getPagedRelationships();
        	$scope.getPagedDefinitions();
        	
        	// TODO add others
        }
        
        /**
         * Functions to page individual elements
         */ 
        $scope.getPagedAtoms = function(page) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.atomsPage = page;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedAtoms = $scope.getPagedArray($scope.component.atom, $scope.atomsPage, true, null);
        }
        
        $scope.getPagedRelationships = function(page) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.relationshipsPage = page;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedRelationships = $scope.getPagedArray($scope.component.relationship, $scope.relationshipsPage, true, null);
        }
        
        $scope.getPagedDefinitions = function(page) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.definitionsPage = page;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedDefinitions = $scope.getPagedArray($scope.component.definition, $scope.definitionsPage, true, null);
        }
        
        /**
         * Get a paged array with show/hide flags (ENABLED) and filtered by query string (NOT ENABLED)
         */
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
        	if (applyFlags) {
        		newArray = getArrayByFlags(newArray);
        	}
        	
        	// apply filter
        	if (filterStr) {
        		newArray = getArrayByFilter(filterStr);
        	}
        	   	
        	// get the page indices
        	var fromIndex = (page-1)*$scope.pageSize;
        	var toIndex = Math.min(fromIndex + $scope.pageSize, array.length);
        	
        	// slice the array
        	var results = newArray.slice(fromIndex, toIndex);
        	
        	// add the total count before slicing
        	results.totalCt = newArray.length;
        	
        	//console.debug("  results", results.totalCt, fromIndex, toIndex, results);

        	return results;
        }
        
        /** Filter array by show/hide flags */
        function getArrayByFlags(array) {
        	
        	var newArray = new Array();
        	
        	// if array blank or not an array, return blank list
        	if (array == null || array == undefined || Array.isArray(array) == false)
        		return newArray;
        	
        	// apply show/hide flags via showItem() function
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
        	
        	// TODO
        	
        	return newArray;
        	
        	
        }
    
       

      } ]);
