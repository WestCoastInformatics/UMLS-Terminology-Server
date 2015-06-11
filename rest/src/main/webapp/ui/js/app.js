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
        $scope.componentType = null;
        
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
          //$scope.component = null;
          $scope.componentError = null;
          //$scope.searchResults = null;
          
          // reset the history
         // $scope.componentHistory = [];
         // $scope.componentHistoryIndex = -1;

          // if no terminology specified, stop
          if ($scope.terminology == null) {
            // console.debug("Returning")
            return;
          }
          
          // set the autocomplete url, with pattern: /type/{terminology}/{version}/autocomplete/{searchTerm}
          $scope.autocompleteUrl = contentUrl + getUrlPrefix($scope.terminology.organizingClassType) + '/' + $scope.terminology.terminology + '/' + $scope.terminology.version + "/autocomplete/";
       
          $scope.glassPane++;
          $http(
            {
              url : metadataUrl + 'all/terminology/id/' + $scope.terminology.terminology
                + '/' + $scope.terminology.version,
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
        	console.debug('autocomplete', searchTerms);
        	// if invalid search terms, return empty array
        	if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
        		return new Array();
        	}
        	
        	var deferred = $q.defer();
        	
	    	// NO GLASS PANE
	    	$http({
	             url : $scope.autocompleteUrl + searchTerms,
	             method : "GET",
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
         * Function to get a component of the terminology's organizing class type.
         */
        $scope.getComponent = function(terminologyName, terminologyId) {
        	
        	//console.debug('getComponent', terminologyName, terminologyId);
        	
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
         * Function to get a component based on type parameter
         */
        $scope.getComponentFromType = function(terminologyName, terminologyId, type) {
        	//console.debug('getComponentFromType', terminologyName, terminologyId, type);
        	switch (type) {
        	case 'CONCEPT': $scope.getConcept(terminologyName, terminologyId); break;
        	case 'DESCRIPTOR': $scope.getDescriptor(terminologyName, terminologyId); break;
        	case 'CODE': $scope.getCode(terminologyName, terminologyId); break;
        	default: $scope.componentError = "Could not retrieve " + type + " for " + terminologyName + "/" + terminologyId;
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
         */
        function getComponentHelper(terminologyObj, terminologyId, typePrefix) {
        	
        	$scope.componentType = getComponentTypeFromPrefix(typePrefix);
        	
        	//console.debug('getComponentHelper', terminologyObj, terminologyId, typePrefix, $scope.componentType);
        	
        	// clear existing component and paging
        	$scope.component = null;
        	$scope.componentError = null;
        	clearPaging();
        	
        	if (!terminologyObj || !terminologyId || !typePrefix) {
        		$scope.componentError = "An unexpected display error occurred. Click a concept or perform a new search to continue";
        		return;
        	}
        	
            // get single concept
            $scope.glassPane++;
            $http(
            {
              url : contentUrl + typePrefix + "/" + terminologyObj.terminology + "/" + terminologyObj.version + "/"
                + terminologyId,
              method : "GET",

            }).success(function(data) {
            	
            	// update history
                $scope.addConceptToHistory(data.terminology, data.terminologyId, $scope.componentType, data.name);
            	
	            if (!data) {
	            	$scope.componentError = "Could not retrieve " + $scope.componentType + " data for " + terminologyObj.terminology + "/" + terminologyId;
	            	$scope.glassPane--;
	            	return;
	            }
	            
	            // if local terminology matches passed terminology, attempt to set active row
	            if ($scope.terminology === terminologyObj)
	            	setActiveRow(terminologyId);
	            
	            // cycle over all atoms for pre-processing
	            for (var i = 0; i < data.atom.length; i++) {
	            	
	            	// assign expandable content flag
	            	data.atom[i].hasContent = atomHasContent(data.atom[i]);
	            	
	            	//console.debug("Atom content", data.atom[i].hasContent, data.atom[i]);
	            	
	            	// push any definitions up to top level
	            	for (var j = 0; j < data.atom[i].definition.length; j++) {
	            		var definition = data.atom[i].definition[j];
	            	           		
	            		// set the atom element flag
	            		definition.atomElement = true;
	            		
	            		// add the atom information for tooltip display		
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
                + $scope.terminology.version + "/query/" + queryStr,
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
            
            // select the first component if results returned
            if ($scope.searchResults.length != 0)
            	$scope.getComponent($scope.terminology.terminology, $scope.searchResults[0].terminologyId);
           
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
        // Expand/Collapse functions
        ///////////////////////////////
        $scope.toggleItemCollapse = function(item) {
        	item.expanded = !item.expanded;
        }
        
        // Return true/false whether an atom has expandable content
        function atomHasContent(atom) {
        	//console.debug('atomHasContent', atom);
        	if (!atom) return false; 	
        	if (atom.attribute.length > 0) return true;
        	if (atom.definition.length > 0) return true;    	
        	if (atom.relationship.length > 0) return true;   	
        	return false;
        }
        
        // Returns the css class for an item's collapsible control
        $scope.getCollapseIcon = function(item) {
        	
        	//console.debug('getCollapseIcon', item.hasContent, item.expanded, item);
        	
        	// if no expandable content detected, return blank glyphicon (see tsMobile.css)
        	if (!item.hasContent) return 'glyphicon glyphicon-plus glyphicon-none';
        	
        	// return plus/minus based on current expanded status
        	if (item.expanded) return 'glyphicon glyphicon-minus';
        	else return 'glyphicon glyphicon-plus';
        }
        
     
        ///////////////////////////////
        // Misc helper functions
        ///////////////////////////////
        
        
        /** Set selected item to active row (for formatting purposes */
        function setActiveRow(terminologyId) {
        	if (!$scope.searchResults || $scope.searchResults.length == 0)
        		return;
          for (var i = 0; i < $scope.searchResults.length; i++) {
            if ($scope.searchResults[i].terminologyId === terminologyId) {
              $scope.searchResults[i].active = true;
            } else {
              $scope.searchResults[i].active = false;
            }
          }
        }

        /** Construct a default PFS object */
        function getPfs(page) {
        	if (!page) page = 1;
            return {
	            startIndex : (page-1)*$scope.pageSize,
	            maxResults : $scope.pageSize,
	            sortField : null,
	            queryRestriction : null
	        };
        }
        
        /**  Helper function to get the proper html prefix based on class type  */
        function getUrlPrefix(classType) {
        	
        	switch(classType) {
        	case 'CONCEPT':	return 'cui';
        	case 'DESCRIPTOR': return 'dui';
        	case 'CODE': return 'code';
        	default: return 'prefixErrorDetected';
        	}

        }
        
        /** Helper function to get the component type from the url prefix */
        function getComponentTypeFromPrefix(prefix) {
        	switch (prefix) {
        	case 'cui': return 'CONCEPT';
        	case 'dui': return 'DESCRIPTOR';
        	case 'code': return 'CODE';
        	default: return 'UNKNOWN COMPONENT';
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
        
        /** Get the organizing class type from a terminology name */
        $scope.getOrganizingClassType = function(terminologyName) {
        	
        	if (!terminologyName)
        		return null;
        	
        	var terminology = getTerminologyFromName(terminologyName);
        	if (!terminology) {
        		return "ClassTypeUnknown";
        	}
        	return terminology.organizingClassType;
        }
        
        /**
         * Function to filter viewable terminologies for picklist
         */
        $scope.getViewableTerminologies = function() {
        	var viewableTerminologies = new Array();
        	if (!$scope.terminologies) {
        		return viewableTerminologies;
        	}
        	for (var i = 0; i < $scope.terminologies.length; i++) {
        		// exclude MTH and SRC
        		if ($scope.terminologies[i].terminology != 'MTH' && $scope.terminologies[i].terminology != 'SRC')
        			viewableTerminologies.push($scope.terminologies[i])
        	}
        	return viewableTerminologies;
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
        
        //////////////////////////////////////
        // Metadata Helper Functions
        //////////////////////////////////////
        
        var relationshipTypes = [];
        var attributeNames = [];
        var termTypes = [];
        
        // on metadata changes
        $scope.$watch('metadata', function() {
        	
        	// reset arrays
        	relationshipTypes = [];
        	attributeNames = [];
        	termTypes = [];
        	
        	if ($scope.metadata) {
        		for (var i = 0; i < $scope.metadata.length; i++) {
        			
        			// extract relationship types for convenience
	        		if ($scope.metadata[i].name === 'Relationship_Types') {
	        			relationshipTypes = $scope.metadata[i].keyValuePair;
	        		}
	        		if ($scope.metadata[i].name === 'Attribute_Names') {
	        			attributeNames = $scope.metadata[i].keyValuePair;
	        		}
	        		if ($scope.metadata[i].name === 'Term_Types') {
	        			termTypes = $scope.metadata[i].keyValuePair;
	        		}
	        	}
        	}        	
        });
        
        // get relationship type name from its abbreviation
        $scope.getRelationshipTypeName = function(abbr) {
        	for (var i = 0; i < relationshipTypes.length; i++) {
        		if (relationshipTypes[i].key === abbr) {
        			return relationshipTypes[i].value;
        		}
        	}
        	return null
        }

        // get attribute name name from its abbreviation
        $scope.getAttributeNameName = function(abbr) {
        	for (var i = 0; i < attributeNames.length; i++) {
        		if (attributeNames[i].key === abbr) {
        			return attributeNames[i].value;
        		}
        	}
        	return null
        }

        // get term type name from its abbreviation
        $scope.getTermTypeName = function(abbr) {
        	for (var i = 0; i < termTypes.length; i++) {
        		if (termTypes[i].key === abbr) {
        			return termTypes[i].value;
        		}
        	}
        	return null
        }

        //////////////////////////////////////
        // Navigation History
        //////////////////////////////////////
        
        // concept navigation variables
        $scope.componentHistory = [];
        $scope.componentHistoryIndex = -1;  // index is the actual array index (e.g. 0:n-1)
        
        // add a terminology/terminologyId pair to the history stack
        $scope.addConceptToHistory = function(terminology, terminologyId, type, name) {
        	
        	console.debug("Adding concept to history", terminology, terminologyId, type, name);
        	
        	// if history exists
        	if ($scope.componentHistoryIndex != -1) {
        		
        		// if this component currently viewed, do not add
        		if ($scope.componentHistory[$scope.componentHistoryIndex].terminology === terminology 
        				&& $scope.componentHistory[$scope.componentHistoryIndex].terminologyId === terminologyId)
        			return;
        	}
        		
        	
        	// add item and set index to last
        	$scope.componentHistory.push({'terminology':terminology, 'terminologyId':terminologyId, 'type':type, 'name':name, 'index':$scope.componentHistory.length});
        	$scope.componentHistoryIndex = $scope.componentHistory.length - 1;
        }
        
        // local history variables for dorp down list
        $scope.localHistory = null;
        $scope.localHistoryPageSize = 10; // NOTE: must be even number!
        $scope.localHistoryPreviousCt = 0;
        $scope.localHistoryNextCt = 0;
        
        // get the local history for the currently viewed concept
        $scope.$watch('componentHistoryIndex', function() {
        	console.debug('componentHistoryIndex changed');
        	
        	setComponentLocalHistory($scope.componentHistoryIndex);
        });
        
        /** 
         * Function to set the local history for drop down list based on an index
         * For cases where history > page size, returns array [index - pageSize / 2 + 1 : index + pageSize]
         */
        function setComponentLocalHistory(index) {
        	
        	console.debug('getting local history', index, $scope.componentHistory.length, $scope.localHistoryPageSize);
        	
        	// if not a full page of history, simply set to component history and stop
        	if ($scope.componentHistory.length <= $scope.localHistoryPageSize) {
        		$scope.localHistory = $scope.componentHistory;
        		return;
        	}
        	
        	// get upper bound
        	var upperBound = Math.min(index + $scope.localHistoryPageSize / 2, $scope.componentHistory.length);
        	var lowerBound = Math.max(upperBound - $scope.localHistoryPageSize, 0);

        	// resize upper bound to ensure full page (for cases near beginning of history)
        	upperBound = lowerBound + $scope.localHistoryPageSize;
        	
        	// calculate unshown element numbers
        	$scope.localHistoryNextCt = $scope.componentHistory.length - upperBound;
        	$scope.localHistoryPreviousCt = lowerBound;
	
        	console.debug('indices', lowerBound, upperBound, 'remaining', $scope.localHistoryPreviousCt, $scope.localHistoryNextCt);
        	
        	// return the local history
        	$scope.localHistory = $scope.componentHistory.slice(lowerBound, upperBound);
        };
        
        $scope.getComponentFromHistory = function(index) {
        	
        	// if currently viewed do nothing
        	if (index === $scope.componentHistoryIndex)
        		return;
        	
        	// set the index and get the component from history information
        	$scope.componentHistoryIndex = index;
        	$scope.getComponentFromType(
        			$scope.componentHistory[$scope.componentHistoryIndex].terminology, 
        			$scope.componentHistory[$scope.componentHistoryIndex].terminologyId,
        			$scope.componentHistory[$scope.componentHistoryIndex].type);
        }

        $scope.getComponentStr = function(component) {
        	if (!component)
        		return null;
        	
        	return component.terminology + "/" + component.terminologyId + " " + component.type + ": " + component.name;
        }
        
        // UNTESTED
        $scope.viewHistoryInTable = function() {
        	var searchResults = [];
        	
        	for (var i = 0; i < $scope.componentHistory.length; i++) {
        		var comp = $scope.componentHistory[i];
        		var searchResult = {'terminology':comp['terminology'], 'version':comp['version'], 'name':comp['name']}
        		searchResults.push(searchResult);
        	}
        	
        	$scope.searchResults = searchResults;
        	$scope.pagedSearchResults = $scope.getPagedArray($scope.searchResults, 1, false, null);
        }
        
        // UNTESTED
        $scope.clearHistory = function() {
        	 $scope.componentHistory = [];
             $scope.componentHistoryIndex = -1;
             
             // set currently viewed item as first history item
             $scope.addConceptToHistory($scope.component.terminology, $scope.component.terminologyId, $scope.componentType, $scope.component.name);
        }
        ////////////////////////////////////
        // Pagination
        ////////////////////////////////////
       
        // paged variable lists
        // NOTE:  Each list must have a totalCount variable
        //        either from ResultList object or calculated
        $scope.pagedSearchResults = null;
        $scope.pagedAttributes = null;
        $scope.pagedSemanticTypes = null;
        $scope.pagedDescriptions = null;
        $scope.pagedRelationships = null;
        $scope.pagedAtoms = null;
        
        // variable page numbers
        $scope.searchResultsPage = 1;
        $scope.semanticTypesPage = 1;
        $scope.definitionsPage = 1;
        $scope.relationshipsPage = 1;
        $scope.atomsPage = 1;
        
        // variable filter variables
        $scope.semanticTypesFilter = null;
        $scope.descriptionsFilter = null;
        $scope.relationshipsFilter = null;
        $scope.atomsFilter = null;
        $scope.attributesFilter = null;
        
        // default page size
        $scope.pageSize = 10;
        
        // reset all paginator pages
        function clearPaging() {
        	$scope.searchResultsPage = 1;
            $scope.semanticTypesPage = 1;
            $scope.definitionsPage = 1;
            $scope.relationshipsPage = 1;
            $scope.atomsPage = 1;
            $scope.attributesPage = 1;

            $scope.semanticTypesFilter = null;
            $scope.descriptionsFilter = null;
            $scope.relationshipsFilter = null;
            $scope.atomsFilter = null;
            $scope.attributesFilter = null;
            
        }
        
        // apply paging to all elements
        function applyPaging() {
        	
        	// call each get function without paging (use current paging info)
        	$scope.getPagedAtoms();
        	$scope.getPagedRelationships();
        	$scope.getPagedDefinitions();
        	$scope.getPagedAttributes();
        	$scope.getPagedSemanticTypes();
        	
        }
        
        /////////////////////////////////////////////////////////////////
        // Server-side Paging
        // Functions for arrays paged by server-side find methods
        /////////////////////////////////////////////////////////////////
        $scope.getPagedRelationships = function(page, query) {
        	
        	if (!page) page = 1;
        	
        	// hack for wildcard searching, may impair other lucene functionality
        	if (query) {
        		// append wildcard to end of query string if not present and not quoted
        		if (query.indexOf("*") == -1 && query.indexOf("\"") == -1) {
        			query = query + "*";
        		}
        	}
        	if (!query) query = "~BLANK~";
        	
        	var typePrefix = getUrlPrefix($scope.componentType);
        	var pfs = getPfs(page);
        	
        	// construct query restriction if needed
        	// TODO Change these to use pfs object parameters
        	var qr = '';
        	if ($scope.showSuppressible == false) {
        		qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
        	}
        	if($scope.showObsolete == false) {
        		qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
        	}
        	pfs['queryRestriction'] = qr;
        	pfs['sortField'] = 'relationshipType';
        	
        	$scope.glassPane++;
            $http(
              {
                url : contentUrl + typePrefix 
                + "/" + $scope.component.terminology
                + "/" + $scope.component.version 
                + "/" + $scope.component.terminologyId 
                + "/relationships/query/" + query,
                method : "POST",
                dataType : "json",
                data : pfs,
                headers : {
                  "Content-Type" : "application/json"
                }
              }).success(function(data) {
            	  
            	 $scope.pagedRelationships = data.relationship;
            	 $scope.pagedRelationships.totalCount = data.totalCount;
            	 $scope.glassPane--;

            }).error(function(data, status, headers, config) {
              $scope.handleError(data, status, headers, config);
              $scope.glassPane--;
            });
        }
        
        ////////////////////////////////////////////////////////////////
        // Client-side Paging
        // Functions for arrays retrieved in full, then paged by js.
        ////////////////////////////////////////////////////////////////
        $scope.getPagedAtoms = function(page, query) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.atomsPage = page;
        	if (!query) query = null;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedAtoms = $scope.getPagedArray($scope.component.atom, $scope.atomsPage, true, query);
        }
        
        $scope.getPagedDefinitions = function(page, query) {
        	
        	console.debug('paged definitions', page, $scope.definitionsPage);
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.definitionsPage = page;
        	if (!query) query = null;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedDefinitions = 
        		$scope.getPagedArray(
        				$scope.component.definition, 
        				$scope.definitionsPage, 
        				true, 
        				query,
        				'value',
        				false);
        }
        
        $scope.getPagedAttributes = function(page, query) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.attributesPage = page;
        	if (!query) query = null;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedAttributes =
        		$scope.getPagedArray(
        				$scope.component.attribute, 
        				$scope.attributesPage, 
        				true, 
        				query,
        				'name',
        				false);
        	
        }
        
        $scope.getPagedSemanticTypes = function(page, query) {
        	
        	// set the page if supplied, otherwise use the current value
        	if (page) $scope.semanticTypesPage = page;
        	
        	// get the paged array, with flags and filter (TODO: Support filtering)
        	$scope.pagedSemanticTypes = 
        		$scope.getPagedArray(
        				$scope.component.semanticType, 
        				$scope.semanticTypesPage, 
        				true, 
        				null,
        				'semanticType',
        				false);
        }
        
        /**
         * Get a paged array with show/hide flags (ENABLED) and filtered by query string (NOT ENABLED)
         */
        $scope.getPagedArray = function(array, page, applyFlags, filterStr, sortField, ascending) {
        	
        	console.debug('getPagedArray', page, applyFlags, filterStr);
        		
        	var newArray = new Array();
        	
        	// if array blank or not an array, return blank list
        	if (array == null || array == undefined || Array.isArray(array) == false)
        		return newArray;
        	
        	// apply page 1 if not supplied
        	if (!page)
        		page = 1;
        	
        	newArray = array;
        	
        	// apply sort if specified
        	if (sortField) {
        		// if ascending specified, use that value, otherwise use false
        		newArray.sort($scope.sort_by(sortField, ascending ? ascending : false))
        	}
        	
        	// apply flags
        	if (applyFlags) {
        		newArray = getArrayByFlags(newArray);
        	}
        	
        	// apply filter
        	if (filterStr) {
        		console.debug('filter detected', filterStr, newArray);
        		newArray = getArrayByFilter(newArray, filterStr);
        	}
        	   	
        	// get the page indices
        	var fromIndex = (page-1)*$scope.pageSize;
        	var toIndex = Math.min(fromIndex + $scope.pageSize, array.length);
        	
        	// slice the array
        	var results = newArray.slice(fromIndex, toIndex);
        	
        	// add the total count before slicing
        	results.totalCount = newArray.length;
        	
        	//console.debug("  results", results.totalCount, fromIndex, toIndex, results);

        	return results;
        }
        
        /** function for sorting an array by (string) field and direction */
        $scope.sort_by = function(field, reverse){

           // key: function to return field value from object
    	   var key = function(x) { return x[field] };

    	   // convert reverse to integer (1 = ascending, -1 = descending)
    	   reverse = !reverse ? 1 : -1;

    	   return function (a, b) {
    	       return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
    	     } 
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
        function getArrayByFilter(array, filter) {
        	var newArray = [];
        	
        	console.debug('getArrayByFilter', array, filter);
        	for (var object in array) {

        		if (objectContainsFilterText(array[object], filter)) {
        			console.debug('pushing object');
        			newArray.push(array[object]);
        		}
        	}
        	return newArray;
        }
        
        /** Returns true if any field on object contains filter text */
        function objectContainsFilterText(object, filter) {
        		
        	if (!filter || !object)
        		return false;
        	
        	for (var prop in object) {
        		var value = object[prop];
        		
        		console.debug('checking', value.toString().toLowerCase(), filter.toLowerCase());
        		
        		
        		// check property for string, note this will cover child elements
        		// TODO May want to make this more restrictive?
        		if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()	) != -1) {
        			return true;
        		}
        	}
        	
        	return false;
        }
    
       

      } ]);
