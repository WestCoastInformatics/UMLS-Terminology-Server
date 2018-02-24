// Content Service
var contentUrl = 'content';
tsApp
  .service(
    'contentService',
    [
      '$http',
      '$q',
      '$window',
      'gpService',
      'utilService',
      'tabService',
      'metadataService',
      function($http, $q, $window, gpService, utilService, tabService, metadataService) {

        var metadata = metadataService.getModel();

        // Shared data model for history
        var history = {
          // the components stored
          components : [],
          // the index of the currently viewed component
          index : -1,
          pageSize : 5
        };

        // Shared data model for search params
        var searchParams = {
          page : 1,
          pageSize : 10,
          query : null,
          expression : null,
          advancedMode : false,
          semanticType : null,
          termType : null,
          matchTerminology : null,
          language : null,
          showExtension : false,
          userNote : null,
          expression : {
            name : null,
            value : null,
            fields : [],
            isDisabled : false,
            isRawInput : false

          }
        };

        // prior search results
        var lastSearchParams = null;
        var lastComponent = null;

        // Get new copy of default search parameters
        this.getSearchParams = function() {
          return angular.copy(searchParams);
        };

        // get the expressions
        var expressions = [
          {
            name : 'Raw Expression',
            isRawInput : true,
            value : null
          },
          {
            name : '------',
            isDisabled : true
          },
          {
            name : 'Ancestor of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '> ' + this.fields['Concept'];
            }
          },
          {
            name : 'Descendant of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '< ' + this.fields['Concept'];
            }
          },
          {
            name : 'Member of',
            fields : {
              'Concept' : ''
            },
            compute : function() {
              this.value = '^ ' + this.fields['Concept'];
            }
          },
          {
            name : 'Has Attribute',
            fields : {
              'Focus Concept' : '',
              'Attribute' : '',
              'Target' : '',
            },
            compute : function() {
              this.value = this.fields['Focus Concept'] + ': ' + this.fields['Attribute'] + "= "
                + this.fields['Target'];
            }
          }

        ];

        // Returns the expression types
        this.getExpressions = function() {
          return expressions;
        };

        // Get autocomplete results
        this.autocomplete = function(searchTerms, autocompleteUrl) {

          // Setup deferred
          var deferred = $q.defer();

          // if invalid search terms or no url, return empty array
          if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3
            || !autocompleteUrl) {
            deferred.resolve(new Array());
          } else {

            // NO GLASS PANE
            // Make GET call
            $http.get(contentUrl + '/' + autocompleteUrl + encodeURIComponent(searchTerms)).then(
            // success
            function(response) {
              deferred.resolve(response.data.strings);
            },
            // error
            function(response) {
              utilService.handleError(response);
              deferred.resolve(response.data);
            });
          }

          return deferred.promise;
        };

        // Get the component from a component
        // where component is at minimum { id: ..., type: ..., terminology: ...,
        // version:
        // ..., terminologyId: ...}
        // Search results and components can be passed directly
        this.getComponent = function(component, projectId) {
          console.debug('getComponent', component, projectId);

          var deferred = $q.defer();

          // check prereqs
          if (!(component.type && component.id)
            && !(component.type && component.terminologyId && component.terminology && component.version)) {
            console.debug("component",component);
            utilService.setError('Component object not fully specified');
            deferred.reject('Component object not fully specified');
            return;
          }

          // Make GET call
          gpService.increment();

          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the

          // compose the URL, if the version is specified use terminologyId,
          // otherwise use "id"
          var url = component.version ? contentUrl + '/' + component.type.toLowerCase() + "/"
            + component.terminology + "/" + component.version + "/" + component.terminologyId
            : contentUrl + '/' + component.type.toLowerCase() + "/" + component.id;

          $http.get(url + (projectId ? '?projectId=' + projectId : '')).then(
            // success
            function(response) {
              var data = response.data;
              if (!data) {
                deferred
                  .reject('Could not retrieve ' + component.type + ' data for '
                    + component.terminologyId + '/' + component.terminology + '/'
                    + component.version);
              } else {

                // Set the type of the returned component
                data.type = component.type;

                // cycle over all atoms for pre-processing
                if (component.type != 'ATOM') {
                  for (var i = 0; i < data.atoms.length; i++) {

                    // push any definitions up to top level
                    for (var j = 0; j < data.atoms[i].definitions.length; j++) {
                      var definition = data.atoms[i].definitions[j];

                      // set the atom element flag
                      definition.atomElement = true;

                      // add the atom information for tooltip
                      // display
                      definition.atomElementStr = data.atoms[i].name + " ["
                        + data.atoms[i].terminology + "/" + data.atoms[i].termType + "]";

                      // add the definition to the top level
                      // component
                      data.definitions.push(definition);
                    }
                  }
                }

              }

              gpService.decrement();
              deferred.resolve(data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };
        
        // Get the inverse of a relationship type
        this.getInverseRelationshipType = function(relationshipType, terminology, version) {
                console.debug('getInverseRelationshipType', relationshipType, terminology, version);

                var deferred = $q.defer();
                gpService.increment();
                $http.get(
                  contentUrl + '/inverseRelationshipType/' + terminology + '/' + version + '/' + relationshipType, {
                      transformResponse : [ function(response) {
                          // Data response is plain text at this point
                          // So just return it, or do your parsing here
                          return response;
                        } ]
                      }).then(
                    // Success
                    function(response) {
                      console.debug('  inverse relationship type = ' + response.data);
                      gpService.decrement();
                      deferred.resolve(response.data);
                    },
                    // Error
                    function(response) {
                      utilService.handleError(response);
                      gpService.decrement();
                      deferred.reject(response.data);
                    });
                    return deferred.promise;
        };

        //
        // Search history for preservation across route navigation
        // NOTE: Currently only set in contentController.js
        //

        // Sets last search params
        this.setLastSearchParams = function(searchParams) {
          this.searchParams = searchParams;
        };

        // Gets last search params
        this.getLastSearchParams = function() {
          return this.searchParams;
        };

        // Sets last component
        this.setLastComponent = function(component) {
          this.lastComponent = component;
        };

        // Gets last component
        this.getLastComponent = function() {
          return this.lastComponent;
        };

        // add a component history entry
        this.addComponentToHistory = function(component) {
          var terminologyId = component.terminologyId;
          var terminology = component.terminology;
          var version = component.version;
          var type = component.type;
          var name = component.name;

          // if history exists
          if (history && history.index != -1) {

            var curObj = history.components[history.index];

            if (!curObj) {
              utilService.setError('Error accessing history at index: ' + history.index);
            }

            // if this component currently viewed, do not add
            if (curObj.terminology === terminology && curObj.version === version
              && curObj.terminologyId === terminologyId)
              return;
          }

          // add item and set index to last
          history.components.push({
            'version' : version,
            'terminology' : terminology,
            'terminologyId' : terminologyId,
            'type' : type,
            'name' : name,
            'index' : history.length
          });
          history.index = history.components.length - 1;
        };

        // Accessor for the history object
        this.getHistory = function() {
          return history;
        };

        // Clears history
        this.clearHistory = function() {
          history = {
            components : [],
            index : -1
          };
        };

        // Retrieve a component from history based on the index
        this.getComponentFromHistory = function(index) {
          console.debug('getComponentFromHistory', index);
          var deferred = $q.defer();

          if (index < 0 || index > history.components.length) {
            deferred.reject('Invalid history index: ' + index);
          } else {

            // extract component object
            var component = history.components[index];

            // set the index and get the component from history
            // information
            this.getComponent(component).then(function(data) {
              // set the index and return
              history.index = index;
              deferred.resolve(data);
            });
          }
          return deferred.promise;
        };

        // Helper function for determining if an atom has content
        this.atomHasContent = function(atom) {
          if (!atom)
            return false;
          if (atom.attributes.length > 0)
            return true;
          if (atom.definitions.length > 0)
            return true;
          if (atom.relationships.length > 0)
            return true;
          return false;
        }

        // Gets the tree for the specified component
        this.getTree = function(component, startIndex) {
          console.debug('getTree', component, startIndex);

          if (startIndex === undefined) {
            startIndex = 0;
          }
          // set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : 1,
            sortField : 'ancestorPath',
            queryRestriction : null
          };

          // Make post call
          var url = null;
          if (component.type === 'ATOM') {
            url = contentUrl + '/atom/' + component.id + '/trees'
          } else {
            url = contentUrl + '/' + component.type.toLowerCase() + '/' + component.terminology
              + '/' + component.version + '/' + component.terminologyId + '/trees'
          }
          gpService.increment();
          $http.post(url, pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // Get child trees for the tree (and start index)
        this.getChildTrees = function(tree, type, startIndex) {
          console.debug('getChildTrees', tree, type, startIndex);

          // Set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : 10,
            sortField : 'nodeName',
            queryRestriction : null
          };

          gpService.increment();

          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          var url = null;
          if (type === 'ATOM') {
            url = contentUrl + '/atom/' + tree.nodeId + '/trees/children'
          } else {
            url = contentUrl + '/' + type.toLowerCase() + '/' + tree.terminology + '/'
              + tree.version + '/' + tree.nodeTerminologyId + '/trees/children'
          }
          $http.post(url, pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;

        };

        // Gets the tree roots for the specified params
        this.getTreeRoots = function(type, terminology, version) {
          console.debug('getTreeRoots', type, terminology, version);

          // Setup deferred
          var deferred = $q.defer();

          // PFS - large page size to hopefully read them all except in
          // degenerate cases
          var pageSize = 30;
          var pfs = {
            startIndex : 0,
            maxResults : pageSize,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // Make POST call
          gpService.increment();

          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version
              + "/trees/roots", pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        this.getConceptsForQuery = function(queryStr, terminology, version, projectId, pfs) {
          console.debug('getConcepts', queryStr, terminology, version, pfs);

          var deferred = $q.defer();
          gpService.increment();
          $http.post(
            contentUrl + '/concept/' + terminology + '/' + version + '/get?query='
              + encodeURIComponent(utilService.cleanQuery(queryStr))
              + (projectId ? '&projectId=' + projectId : ''), pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });
          return deferred.promise;
        }

        // Finds components as a list
        this.findComponentsAsList = function(queryStr, type, terminology, version, searchParams) {
          console.debug('findComponentsAsList', queryStr, type, terminology, version, searchParams);

          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (searchParams.page - 1) * searchParams.pageSize,
            maxResults : searchParams.pageSize,
            sortField : searchParams.sortField,
            ascending : searchParams.sortAscending,
            expression : searchParams && searchParams.expression ? searchParams.expression.value
              : null,
            queryRestriction : "(suppressible:false^20.0 OR suppressible:true) AND (atoms.suppressible:false^20.0 OR atoms.suppressible:true)"
          };

          // check parameters for advanced mode
          if (searchParams && searchParams.advancedMode) {
            if (searchParams.semanticType) {
              pfs.queryRestriction += " AND semanticTypes.semanticType:\""
                + searchParams.semanticType + "\"";
            }

            if (searchParams.matchTerminology) {
              pfs.queryRestriction += " AND atoms.terminology:\"" + searchParams.matchTerminology
                + "\"";
            }
            if (searchParams.termType) {
              pfs.queryRestriction += " AND atoms.termType:\"" + searchParams.termType + "\"";
            }
            if (searchParams.language) {
              pfs.queryRestriction += " AND atoms.language:\"" + searchParams.language + "\"";
            }
            if (searchParams.userNote) {
              pfs.queryRestriction += " AND notes.note:\"" + searchParams.userNote + "\"";
            }
          }

          // Add anonymous condition for concepts
          if (type.toLowerCase() == "concept") {
            pfs.queryRestriction += " AND anonymous:false";
          }

          // Make POST call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version + "?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // Finds components as a tree
        this.findComponentsAsTree = function(queryStr, type, terminology, version, searchParams) {
          console.debug('findComponentsAsTree', queryStr, type, terminology, version, searchParams);

          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (searchParams.page - 1) * searchParams.pageSize,
            maxResults : searchParams.pageSize,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // check parameters for advanced mode
          if (searchParams.advancedMode) {

            if (searchParams.semanticType) {
              pfs.queryRestriction = "ancestorPath:" + semanticType.replace("~", "\\~") + "*";
            }/*
                           * if (searchParams.semanticType) { pfs.queryRestriction += " AND
                           * semanticTypes.semanticType:\"" + searchParams.semanticType + "\""; }
                           */

            if (searchParams.matchTerminology) {
              pfs.queryRestriction += " AND atoms.terminology:\"" + searchParams.matchTerminology
                + "\"";
            }
            if (searchParams.termType) {
              pfs.queryRestriction += " AND atoms.termType:\"" + searchParams.termType + "\"";
            }
            if (searchParams.language) {
              pfs.queryRestriction += " AND atoms.language:\"" + searchParams.language + "\"";
            }
          }

          // Make POST call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + "/" + terminology + "/" + version
              + "/trees?query=" + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // Find relationships for query
        this.findRelationshipsForQuery = function(component, query, pfs) {
          console.debug('find relationships', component, query, pfs);

          var type = component.type;
          var terminology = component.terminology;
          var version = component.version;
          var terminologyId = component.terminologyId;

          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + '/' + type.toLowerCase() + '/' + terminology + '/' + version + '/'
              + terminologyId + '/relationships?query='
              + (query != '' && query != null ? '&query=' + query : ''), utilService.prepPfs(pfs))
            .then(
            // success
            function(response) {
              console.debug('  rels = ', response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            },
            // error
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };

        // Handle paging of relationships (requires content service
        // call).
        this.findRelationships = function(component, paging) {
          console.debug('findRelationships', component, paging);
          var deferred = $q.defer();

          if (paging) {

            var pfs = {
              startIndex : (paging.page - 1) * paging.pageSize,
              maxResults : paging.pageSize,
              sortFields : paging.sortFields ? paging.sortFields : [ 'group', 'relationshipType' ],
              ascending : paging.sortAscending,
              queryRestriction : null
            };
          }

          // Show only inferred rels for now
          // construct query restriction if needed
          var qr = '';
          if (!paging.showSuppressible) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if (!paging.showObsolete) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }
          if (paging.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'inferred:true';
          }
          if (!paging.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'stated:true';
          }
          pfs.queryRestriction = qr;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if (metadata.terminology && metadata.terminology.descriptionLogicTerminology) {
            pfs.startIndex = -1;
            pfs.maxResults = 1000000;
          } else {
            pfs.maxResults = paging.pageSize;
          }

          var query = paging.text;

          // Add wildcard to allow better matching from basic search
          // NOTE: searching for "a"* is interpreted by lucene as a
          // leading wildcard search (i.e. "a" *)
          if (query && !query.endsWith('*') && !query.endsWith('"')) {
            query += '*';
          }

          // perform the call
          // NOTE: Must lower case the type (e.g. CONCEPT -> concept) for the
          // path
          gpService.increment();
          $http.post(
            contentUrl + '/' + component.type.toLowerCase() + "/" + component.terminology + "/"
              + component.version + "/" + component.terminologyId + "/relationships?query="
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // Handle paging of relationships (requires content service
        // call).
        this.findDeepRelationships = function(component, inverseFlag, includeConceptRels,
          preferredOnly, includeSelfReferential, paging) {
          console.debug('findDeepRelationships', component, inverseFlag, includeConceptRels,
            preferredOnly, includeSelfReferential, paging);

          var deferred = $q.defer();

          if (component.type.toLowerCase() !== 'concept') {
            utilService.handleError({
              data : 'Deep relationships cannot be retrieved for type ' + component.type
            });
            deferred.reject();
          }

          var sortField = paging.sortField;
          var sortFields = null;
          if (paging.sortFields) {
            sortField = null;
            sortFields = paging.sortFields;
          } else if (!sortField) {
            sortFields = [ 'group', 'relationshipType' ];
          }

          if (paging) {

            var pfs = {
              startIndex : (paging.page - 1) * paging.pageSize,
              maxResults : paging.pageSize,
              ascending : paging.sortAscending,

              // NOTE: Deep relationships do not support query restrictions,
              // instead using
              // text filter as only query parameter
              queryRestriction : null
            };
          }

          if (pfs.sortFields) {
            pfs.sortFields = sortFields;
          } else {
            pfs.sortField = sortField;
          }
          if (!paging.showSuppressible) {
            pfs.queryRestriction = "suppressible:false";
          }

          // set filter/query; unlike relationships, does not require * for
          // filtering
          var query = paging.filter

          // do not use glass pane, produces additional user lag on initial
          // concept load
          // i.e. retrieve concept, THEN get deep relationships
          // gpService.increment();
          $http
            .post(
              contentUrl
                + '/'
                + component.type.toLowerCase()
                + "/"
                + component.terminology
                + "/"
                + component.version
                + "/"
                + component.terminologyId
                + "/relationships/deep?query="
                + encodeURIComponent(utilService.cleanQuery(query))
                + (inverseFlag != null && inverseFlag != '' ? '&inverseFlag=' + inverseFlag : '')
                + (includeConceptRels != null && includeConceptRels != '' ? '&includeConceptRels='
                  + includeConceptRels : '')
                + (preferredOnly != null && preferredOnly != '' ? '&preferredOnly=' + preferredOnly
                  : '')
                + (includeSelfReferential != null && includeSelfReferential != '' ? '&includeSelfReferential='
                  + includeSelfReferential
                  : ''), pfs).then(function(response) {
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              // gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };

        // Handle paging of tree positions (requires content service
        // call).
        this.findDeepTreePositions = function(component, paging) {
          console.debug('findDeepTreePositions', component, paging);

          var deferred = $q.defer();

          if (component.type.toLowerCase() !== 'concept') {
            utilService.handleError({
              data : 'Deep tree positions cannot be retrieved for type ' + component.type
            });
            deferred.reject();
          }

          if (paging) {

            var pfs = {
              startIndex : (paging.page - 1) * paging.pageSize,
              maxResults : paging.pageSize,
              sortFields : paging.sortFields ? paging.sortFields : [ 'terminology' ],
              ascending : paging.sortAscending,

              // NOTE: Deep relationships do not support query restrictions,
              // instead using
              // text filter as only query parameter
              queryRestriction : null
            };
          }

          // set filter/query; unlike relationships, does not require * for
          // filtering
          var query = paging.text;

          // do not use glass pane, produces additional user lag on initial
          // concept load
          // i.e. retrieve concept, THEN get deep tree positions
          // gpService.increment();
          $http.post(
            contentUrl + '/' + component.type.toLowerCase() + "/" + component.terminology + "/"
              + component.version + "/" + component.terminologyId + "/treePositions/deep?query="
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            // gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        };

        // function for testing whether a query is a valid expression
        this.isExpressionConstraintLanguage = function(terminology, version, query) {
          console.debug('isExpressionConstraintLanguage', terminology, version, query);
          var deferred = $q.defer();
          if (!query || query.length == 0) {
            deferred.reject('Cannot check empty query for expressions');
            utilService.setError('Cannot check empty query for expressions');
          }
          gpService.increment();
          $http.get(
            contentUrl + '/ecl/isExpression/' + encodeURIComponent(utilService.cleanQuery(query)))
            .then(function(response) {
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });
        };

        // Add a component note
        this.addComponentNote = function(component, note) {
          console.debug('addComponentNote', component, note);
          var deferred = $q.defer();
          if (!component || !note) {
            deferred.reject('Concept id and note must be specified');
          } else {

            gpService.increment();
            $http.post(
              contentUrl + '/' + component.type.toLowerCase() + '/' + component.id + '/note', note)
              .then(function(response) {
                gpService.decrement();
                deferred.resolve(response.data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                // return the original concept without additional annotation
                deferred.reject();
              });

            return deferred.promise;
          }
        };

        // Remove component note
        this.removeComponentNote = function(component, noteId) {
          console.debug('removeComponentNote', component, noteId);
          var deferred = $q.defer();
          if (!component || !noteId) {
            deferred.reject('Component component (minimum type) and note id must be specified');
          } else {

            gpService.increment();
            $http['delete'](contentUrl + '/' + component.type.toLowerCase() + '/note/' + noteId)
              .then(function(response) {
                console.debug('  successful remove note');
                gpService.decrement();
                deferred.resolve(response.data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                // return the original concept without additional annotation
                deferred.reject();
              });

            return deferred.promise;
          }
        };

        // Get the user favorites
        // NOTE: This uses the paging structure in utilService.getPaging
        // parallel to uses in component report elements (atoms,
        // relationships...)
        // instead of the getSearchParams structure for standard queries
        this.getUserFavorites = function(paging) {
          console.debug('get user favorites', paging);
          var deferred = $q.defer();
          if (!paging) {
            deferred.reject('Paging must be specified');
          } else {

            var pfs = {
              startIndex : (paging.page - 1) * paging.pageSize,
              maxResults : paging.pageSize,
              sortField : paging.sortField ? paging.sortField : 'lastModified',
              queryRestriction : paging.filter,
              ascending : paging.sortAscending
            };

            gpService.increment();
            $http.post(contentUrl + '/favorites', pfs).then(
            // Success
            function(response) {
              console.debug('  user favorites = ', response.data);

              gpService.decrement();
              deferred.resolve(response.data);
            },
            // Failure
            function(response) {
              utilService.handleError(response);
              gpService.decrement();
              // return the original concept without additional annotation
              deferred.reject();
            });
          }
          return deferred.promise;
        };

        // Get components with notes
        this.getComponentsWithNotesForUser = function(query, paging) {
          console.debug('get components with notes', query, paging);
          var deferred = $q.defer();

          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField ? paging.sortField : 'name',
            ascending : paging.sortAscending
          };

          if (query && !query.endsWith("*")) {
            query += "*";
          }

          gpService.increment();
          $http.post(
            contentUrl + '/component/notes?query='
              + encodeURIComponent(utilService.cleanQuery(query)), pfs).then(function(response) {
            gpService.decrement();
            deferred.resolve(response.data);
          }, function(response) {
            utilService.handleError(response);
            gpService.decrement();
            // return the original concept without additional annotation
            deferred.reject();
          });

          return deferred.promise;
        };

        /**
         * Callbacks functions needed by directives NOTE: getComponent and
         * getComponentForTree deliberately excluded as each view should
         * interact with the content service directly for history and other
         * considerations
         */
        this.getCallbacks = function() {
          return {
            findRelationships : this.findRelationships,
            findDeepRelationships : this.findDeepRelationships
          };
        };

        // end

        // function for getting concept
        this.getConcept = function(conceptId, projectId) {
          return this.getComponent({
            id : conceptId,
            type : 'CONCEPT'
          }, projectId);
        };

        // function for getting atom
        this.getAtom = function(atomId, projectId) {
          return this.getComponent({
            id : atomId,
            type : 'ATOM'
          }, projectId);
        };

        // get mapsets
        this.getMapSets = function(terminology, version) {
          console.debug('getMapSets', terminology, version);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.get(contentUrl + '/mapset/all/' + terminology + '/' + version).then(
          // success
          function(response) {
            console.debug('  mapsets =', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        }

        // Find mappings
        this.findMappings = function(component, pfs) {
          console.debug('findMappings', component, pfs);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.post(
            contentUrl + '/' + component.type.toLowerCase() + '/' + component.terminologyId + '/'
              + component.terminology + '/' + component.version + '/mappings', pfs).then(
          // success
          function(response) {
            console.debug('  mappings =', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        }

        // validate concept
        this.validateConcept = function(projectId, concept, checkId) {
          console.debug('validateConcept', projectId, concept, checkId);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.post(
            contentUrl + '/validate/concept?projectId=' + projectId
              + (checkId ? 'checkId=' + checkId : ''), concept).then(
          // success
          function(response) {
            console.debug('  validation results =', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        }
        
        // validate concept
        this.validateConcepts = function(projectId, conceptIds, checkId) {
          console.debug('validateConcepts', projectId, conceptIds, checkId);
          // Setup deferred
          var deferred = $q.defer();

          // Make POST call
          gpService.increment();

          $http.post(
            contentUrl + '/validate/concepts?projectId=' + projectId
              + (checkId ? '&checkId=' + checkId : ''), conceptIds).then(
          // success
          function(response) {
            console.debug('  validation results =', response.data);
            gpService.decrement();
            deferred.resolve(response.data);
          },
          // error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
            deferred.reject(response.data);
          });

          return deferred.promise;
        }

        // Popout component into new window
        this.popout = function(component) {
          var currentUrl = window.location.href;
          var baseUrl = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
          // TODO; don't hardcode this - maybe "simple" should be a parameter
          var newUrl = baseUrl + '/content/simple/' + component.type + '/' + component.terminology
            + '/' + component.version + '/' + component.terminologyId;
          var title = 'Component-' + component.terminology + '/' + component.version + ', '
            + component.terminologyId;
          var newWindow = $window.open(newUrl, title, 'width=950,height=600,scrollbars=yes');
          newWindow.document.title = title;
          newWindow.focus();

        };

        // Gets the tree for the specified component
        this.exportTerminologySimple = function(terminology, version) {
          console.debug('exportTerminologySimple', terminology, version);

          // Make post call
          gpService.increment();
          $http.get(
            contentUrl + '/terminology/export/simple?terminology=' + terminology + '&version='
              + version).then(
          // Success
          function(response) {
            var blob = new Blob([ response.data ], {
              type : ''
            });

            // fake a file URL and download it
            var fileURL = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = fileURL;
            a.target = '_blank';
            a.download = terminology + '_' + version + '.txt';
            document.body.appendChild(a);
            gpService.decrement();
            a.click();
            window.URL.revokeObjectURL(fileURL);

          },
          // Error
          function(response) {
            utilService.handleError(response);
            gpService.decrement();
          });

        };
        // end
      } ]);
