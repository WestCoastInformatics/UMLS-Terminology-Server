// Content Service
tsApp
  .service(
    'contentService',
    [
      '$http',
      '$q',
      'gpService',
      'utilService',
      'metadataService',
      function($http, $q, gpService, utilService, metadataService) {
        console.debug("configure contentService");

        // Initialize
        var metadata = metadataService.getModel();

        // The component and the history list
        var component = {
          object : null,
          type : null,
          prefix : null,
          error : null,
          history : [],
          historyIndex : -1
        };

        // Page size
        var pageSizes = {
          general : 10,
          rels : 10,
          roots : 125,
          trees : 5,
          search : 10,
          sibling : 10
        };

        // Search results
        var searchParams = {
          page : 1,
          query : null,
          advancedMode : false,
          semanticType : null,
          termType : null,
          matchTerminology : null,
          language : null
        };

        // Search results
        var searchResults = {
          list : null,
          tree : null
        };

        // Accessor function for component
        this.getModel = function() {
          return component;
        };

        // Accessor for the page sizes object
        this.getPageSizes = function() {
          return pageSizes;
        };

        // Accessor for search params
        this.getSearchParams = function() {
          return searchParams;
        };

        // Accessor for search results
        this.getSearchResults = function() {
          return searchResults;
        };

        // Autocomplete function
        this.autocomplete = function(searchTerms, autocompleteUrl) {

          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
            return new Array();
          }

          // Setup deferred
          var deferred = $q.defer();

          // NO GLASS PANE
          // Make GET call
          $http.get(autocompleteUrl + encodeURIComponent(searchTerms)).then(
          // success
          function(response) {
            deferred.resolve(response.data.strings);
          },
          // error
          function(response) {
            utilHandler.handleError(response);
            deferred.resolve(response.data);
          });

          return deferred.promise;
        };

        // Helper function to get the proper html prefix based on class
        // type
        this.getPrefixForType = function(classType) {
          switch (classType) {
          case 'CONCEPT':
            return 'cui';
          case 'DESCRIPTOR':
            return 'dui';
          case 'CODE':
            return 'code';
          default:
            return 'prefix error detected';
          }
        };

        // Helper function to get the component type from the url prefix
        this.getTypeForPrefix = function(prefix) {
          switch (prefix) {
          case 'cui':
            return 'CONCEPT';
          case 'dui':
            return 'DESCRIPTOR';
          case 'code':
            return 'CODE';
          default:
            return 'component type error detected';
          }
        };

        // Helper function to get a type prefix for the terminology
        this.getPrefixForTerminologyAndVersion = function(terminology, version) {
          console.debug('getPrefix', terminology, version, metadataService.getTerminology(terminology, version));
          return this
            .getPrefixForType(metadataService.getTerminology(terminology, version).organizingClassType);
        };

        // Get the component by type
        this.getComponentFromType = function(terminologyId, terminology, version, type) {
          switch (type) {
          case 'CONCEPT':
            return this.getConcept(terminologyId, terminology, version);
          case 'DESCRIPTOR':
            return this.getDescriptor(terminologyId, terminology, version);
          case 'CODE':
            return this.getCode(terminologyId, terminology, version);
          default:
            this.componentError = "Could not retrieve " + type + " for " + terminologyId + "/"
              + terminology + "/" + version;
          }
        };

        // Get the component based on id/terminology/version
        // uses the organizing class type for the type prefix
        this.getComponent = function(terminologyId, terminology, version) {
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);
          return this.getComponentHelper(terminologyId, terminology, version, prefix);
        };

        // Get a concept component
        this.getConcept = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('CONCEPT'));
        };

        // Get a descriptor component
        this.getDescriptor = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('DESCRIPTOR'));
        };

        // Get a code component
        this.getCode = function(terminologyId, terminology, version) {
          return this.getComponentHelper(terminologyId, terminology, version, this
            .getPrefixForType('CODE'));
        };

        // Helper function for loading a component and setting the
        // component
        // data fields
        this.getComponentHelper = function(terminologyId, terminology, version, prefix) {
          console.debug("getComponentHelper", terminologyId, terminology, version, prefix);
          var deferred = $q.defer();

          // Here the prefix is passed in because of terminologies
          // like MSH
          // that may have legitimate types that are not the
          // organizing class
          // type

          // Set component type and prefix
          component.prefix = prefix;
          component.type = this.getTypeForPrefix(prefix);

          // clear existing component and paging
          component.object = null;
          component.error = null;

          if (!terminologyId || !terminology || !version) {
            component.error = "An unexpected display error occurred. Click a concept or perform a new search to continue";
            return;
          }

          // Make GET call
          gpService.increment();
          $http
            .get(
              contentUrl + component.prefix + "/" + terminology + "/" + version + "/"
                + terminologyId).then(
              // success
              function(response) {
                var data = response.data;

                if (!data) {
                  component.error = "Could not retrieve " + component.type + " data for "
                    + terminologyId + "/" + terminology + "/" + version;
                } else {

                  // cycle over all atoms for pre-processing
                  for (var i = 0; i < data.atoms.length; i++) {

                    // assign expandable content flag
                    data.atoms[i].hasContent = atomHasContent(data.atoms[i]);

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
                component.object = data;
                console.debug("  component = ", component);

                // Add component to history
                addComponentToHistory(data.terminologyId, data.terminology, data.version,
                  component.type, data.name);

                gpService.decrement();
                deferred.resolve(data);
              }, function(response) {
                utilService.handleError(response);
                gpService.decrement();
                deferred.reject(response.data);
              });
          return deferred.promise;
        };

        // add a component history entry
        function addComponentToHistory(terminologyId, terminology, version, type, name) {

          // if history exists
          if (component.historyIndex != -1) {

            // if this component currently viewed, do not add
            if (component.history[component.historyIndex].terminology === terminology
              && component.history[component.historyIndex].version === version
              && component.history[component.historyIndex].terminologyId === terminologyId)
              return;
          }

          // add item and set index to last
          component.history.push({
            'version' : version,
            'terminology' : terminology,
            'terminologyId' : terminologyId,
            'type' : type,
            'name' : name,
            'index' : component.history.length
          });
          component.historyIndex = component.history.length - 1;
        }

        // Clears history
        this.clearHistory = function() {

          component.history = [];
          component.historyIndex = -1;

          // set currently viewed item as first history item
          addComponentToHistory(component.terminologyId, component.terminology, component.version,
            component.type, component.name);
        };

        // Retrieve a component from history based on the index
        this.getComponentFromHistory = function(index) {
          var deferred = $q.defer();

          // set the index and get the component from history
          // information
          component.historyIndex = index;
          this.getComponentFromType(component.history[component.historyIndex].terminologyId,
            component.history[component.historyIndex].terminology,
            component.history[component.historyIndex].version,
            component.history[component.historyIndex].type).then(function(data) {
            deferred.resolve(data);
          });
          return deferred.promise;
        };

        // Helper function for determining if an atom has content
        function atomHasContent(atom) {
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
        this.getTree = function(terminologyId, terminology, version, startIndex) {
          console.debug("getTree", terminologyId, terminology, version, startIndex);
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

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Make post call
          gpService.increment();
          $http.post(
            contentUrl + prefix + '/' + terminology + '/' + version + '/' + terminologyId
              + '/trees', pfs).then(
          // success
          function(response) {
            console.debug("  output = ", response.data);
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
        this.getChildTrees = function(tree, startIndex) {
          console.debug("getChildTrees", tree, startIndex);
          // Set up deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : startIndex,
            maxResults : pageSizes.general,
            sortField : null,
            queryRestriction : null
          };

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(tree.terminology, tree.version);

          // Make POST call
          // @Path("/cui/{terminology}/{version}/{terminologyId}/trees/children")
          gpService.increment();
          $http.post(
            contentUrl + prefix + '/' + tree.terminology + '/' + tree.version + '/'
              + tree.nodeTerminologyId + '/trees/children', pfs).then(
          // success
          function(response) {
            console.debug("  childTrees = ", response.data);
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
        this.getTreeRoots = function(terminology, version, page) {
          console.debug("getTreeRoots", terminology, version);
          // Setup deferred
          var deferred = $q.defer();

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // PFS
          // construct the pfs
          var pfs = {
            startIndex : (page - 1) * pageSizes.general,
            maxResults : pageSizes.roots,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // Make POST call
          gpService.increment();
          $http.post(contentUrl + prefix + "/" + terminology + "/" + version + "/trees/roots", pfs)
            .then(
            // success
            function(response) {
              console.debug("  roots =", response.data);
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

        // Finds components as a list
        this.findComponentsAsList = function(queryStr, terminology, version, page, searchParams) {
          console.debug("findComponentsAsList", queryStr, terminology, version, page, searchParams);
          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (page - 1) * pageSizes.general,
            maxResults : pageSizes.general,
            sortField : null,
            queryRestriction : "(suppressible:false^20.0 OR suppressible:true) AND (atoms.suppressible:false^20.0 OR atoms.suppressible:true)"
          };

          // check parameters for advanced mode
          if (searchParams.advancedMode) {
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
          }

          // Get prefix
          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Add anonymous condition for concepts
          if (prefix == "cui") {
            pfs.queryRestriction += " AND anonymous:false";
          }

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + this.getPrefixForType(metadata.terminology.organizingClassType) + "/"
              + terminology + "/" + version + "?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
            console.debug("  output = ", response.data);
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
        this.findComponentsAsTree = function(queryStr, terminology, version, page, semanticType) {
          console.debug("findComponentsAsTree", queryStr, terminology, version, page, semanticType);

          // Setup deferred
          var deferred = $q.defer();

          // PFS
          var pfs = {
            startIndex : (page - 1) * pageSizes.trees,
            maxResults : pageSizes.trees,
            sortField : metadata.treeSortField,
            queryRestriction : null
          };

          // check parameters for advanced mode
          if (searchParams.advancedMode) {

            if (semanticType) {
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

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          // Make POST call
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + terminology + "/" + version + "/trees?query="
              + encodeURIComponent(utilService.cleanQuery(queryStr)), pfs).then(
          // success
          function(response) {
            console.debug("  output = ", response.data);
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
        this.findRelationships = function(terminologyId, terminology, version, page, parameters) {
          console.debug("findRelationships", terminologyId, terminology, version, page, parameters);
          var deferred = $q.defer();

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          if (parameters)

            var pfs = {
              startIndex : (page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortFields : parameters.sortFields ? parameters.sortFields : ['group', 'relationshipType'],
              ascending : parameters.sortAscending,
              queryRestriction : null
            // constructed from filters
            };

          // Show only inferred rels for now
          // construct query restriction if needed
          var qr = '';
          if (!parameters.showSuppressible) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if (!parameters.showObsolete) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }
          if (parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'inferred:true';
          }
          if (!parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'stated:true';
          }
          pfs.queryRestriction = qr;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if (metadata.terminology.descriptionLogicTerminology) {
            pfs.startIndex = -1;
            pfs.maxResults = 1000000;
          } else {
            pfs.maxResults = pageSizes.general;
          }

          var query = parameters.text;

          // Add wildcard to allow better matching from basic search
          // NOTE: searching for "a"* is interpreted by lucene as a 
          // leading wildcard search (i.e. "a" *)
          if (query && !query.endsWith('*') && !query.endsWith('"')) {
            query += '*';
          }
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + component.object.terminology + "/"
              + component.object.version + "/" + component.object.terminologyId
              + "/relationships?query=" + encodeURIComponent(utilService.cleanQuery(query)), pfs)
            .then(function(response) {
              console.debug("  relationships =", response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };

        // Handle paging of mappings (requires content service
        // call).
        this.findMappings = function(terminologyId, terminology, version, page, parameters) {
          console.debug("findMappings", terminologyId, terminology, version, page, parameters);
          var deferred = $q.defer();

          var prefix = this.getPrefixForTerminologyAndVersion(terminology, version);

          if (parameters)

            var pfs = {
              startIndex : (page - 1) * pageSizes.general,
              maxResults : pageSizes.general,
              sortField : parameters.sortField ? parameters.sortField : 'fromTerminologyId',
              ascending : parameters.sortAscending,
              queryRestriction : null
            // constructed from filters
            };

          // Show only inferred rels for now
          // construct query restriction if needed
          var qr = '';
          if (!parameters.showSuppressible) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'suppressible:false';
          }
          if (!parameters.showObsolete) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'obsolete:false';
          }
          if (parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'inferred:true';
          }
          if (!parameters.showInferred) {
            qr = qr + (qr.length > 0 ? ' AND ' : '') + 'stated:true';
          }
          pfs.queryRestriction = qr;

          // For description logic sources, simply read all rels.
          // That way we ensure all "groups" are represented.
          if (metadata.terminology.descriptionLogicTerminology) {
            pfs.startIndex = -1;
            pfs.maxResults = 1000000;
          } else {
            pfs.maxResults = pageSizes.general;
          }

          var query = parameters.text;
          gpService.increment();
          $http.post(
            contentUrl + prefix + "/" + component.object.terminology + "/"
              + component.object.version + "/" + component.object.terminologyId
              + "/mappings?query=" + encodeURIComponent(utilService.cleanQuery(query)), pfs)
            .then(function(response) {
              console.debug("  mappings =", response.data);
              gpService.decrement();
              deferred.resolve(response.data);
            }, function(response) {
              utilService.handleError(response);
              gpService.decrement();
              deferred.reject(response.data);
            });

          return deferred.promise;
        };
        // end

      } ]);
