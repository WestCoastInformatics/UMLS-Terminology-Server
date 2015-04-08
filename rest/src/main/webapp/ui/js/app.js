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

tsApp.directive('component', function() {

  return {
    replace : false,
    restrict : 'AE',
    templateUrl : 'ui/partials/component.html',
    scope : {
      object : '=', // two-way binding, currently un-used
      type : '@' // isolate scope, string expected
    },
    link : function(scope, element, attrs) {

      // initially collapsible is expanded
      scope.componentExpanded = true;

      // function to determine if string is boolean "true"/"false" value
      // used to distinguish between real boolean values and, e.g. "0",
      // "1"
      scope.isTrueFalse = function(elem) {
        if (elem == null)
          return false;

        return elem.toString() === 'true' || elem.toString() === 'false';
      }

      // function to determine if element is a model component array
      // tests (1) if Array, (2) if Array elements have id field
      scope.isComponentArray = function(elem) {
        if (elem == null || !Array.isArray(elem) || elem[0] == null)
          return false;

        return elem[0].hasOwnProperty('terminologyId');
      }

      // quick function to convert "string" into "String"
      scope.getLabelString = function(key, value) {

        var labelString = "";
        if (value == false) {
          labelString = "Not " + key;
        } else {
          labelString = key.substring(0, 1).toUpperCase() + key.substring(1);
        }

        return labelString;

      }
    }

  }
});

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

        // the displayed component
        $scope.component = null;
        $scope.componentType = null;

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
              url : metadataUrl + 'all/terminology/id/' + $scope.terminology.name
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
              // console
              // .debug(terminologyObj);
              $scope.terminologies.push(terminologyObj);

            }

            // select the first
            // terminology
            $scope.setTerminology($scope.terminologies[0]);
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.handleError(data, status, headers, config);
            $scope.glassPane--;
          });
        }

        $scope.getConcept = function(terminology, version, terminologyId) {
          // get single concept
            $scope.glassPane++;
          $http(
            {
              url : contentUrl + "concept/" + terminology + "/" + version + "/"
                + terminologyId,
              method : "GET",

            }).success(function(data) {
            $scope.concept = data;

            console.debug("Retrieved concept:", $scope.concept);

            setActiveRow(terminologyId);
            $scope.setComponent($scope.concept, 'Concept');
            $scope.getParentAndChildConcepts($scope.concept);

            // default button selected is structure, otherwise don't change
            if ($scope.navSelected == null)
              $scope.navSelected = 'Structure';
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
              url : contentUrl + "concepts/" + terminology.name + "/"
                + terminology.version + "/query/" + queryStr,
              method : "POST",
              dataType : "json",
              data : pfs,
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved concepts:", data);
            $scope.searchResults = data.searchResult;
            console.debug("Retrieved terminologies:", data.keyValuePairList);
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

          // clear all viewed component settings
          $scope.concept.viewed = false;

          for (var i = 0; i < $scope.concept.description.length; i++) {
            // console.debug("Settingn description...")
            $scope.concept.description[i].viewed = false;
            // console.debug(" Set for ",
            // $scope.concept.description[i].id);
            // console.debug("Languages: " +
            // $scope.concept.description[i].language.length);

            for (var j = 0; j < $scope.concept.description[i].language.length; j++) {
              console.debug("Setting for language... ", i, j);
              $scope.concept.description[i].language[j].viewed = false;
              console.debug("  Set for language",
                $scope.concept.description[i].language[j].id);
            }
            // console.debug("Done with description");

          }
          for (var i = 0; i < $scope.concept.relationship.length; i++) {
            $scope.concept.relationship[i].viewed = false;
          }
          component.viewed = true;
        }

        $scope.getParentAndChildConcepts = function(concept) {
            $scope.glassPane++;
          // find concepts
          $http(
            {
              url : contentUrl + "concepts/" + concept.terminology + "/"
                + concept.terminologyVersion + "/" + concept.terminologyId
                + "/parents",
              method : "POST",
              dataType : "json",
              data : getPfs(),
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved parent concepts:", data);
            $scope.parentConcepts = data.concept;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.error = data;
            $scope.glassPane--;
          });

          // find concepts
          $scope.glassPane++;
          $http(
            {
              url : contentUrl + "concepts/" + concept.terminology + "/"
                + concept.terminologyVersion + "/" + concept.terminologyId
                + "/children",
              method : "POST",
              dataType : "json",
              data : getPfs(),
              headers : {
                "Content-Type" : "application/json"
              }
            }).success(function(data) {
            console.debug("Retrieved child concepts:", data);
            $scope.childConcepts = data.concept;
            $scope.glassPane--;

          }).error(function(data, status, headers, config) {
            $scope.error = data;
            $scope.glassPane--;
          });
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

      } ]);
