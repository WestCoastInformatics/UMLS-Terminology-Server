// Util service
tsApp
  .service(
    'utilService',
    [
      '$location',
      '$anchorScroll',
      '$cookies',
      'appConfig',
      function($location, $anchorScroll, $cookies, appConfig) {
        console.debug('configure utilService');

        this.showHeaderFooter = true;

        // declare the error
        this.error = {
          message : null,
          longMessage : null,
          expand : false
        };

        this.success = {
          message : null,
          longMessage : null,
          expand : false
        };

        // tinymce options
        this.tinymceOptions = {
          menubar : false,
          statusbar : false,
          plugins : 'autolink autoresize link image charmap searchreplace lists paste',
          toolbar : 'undo redo | styleselect lists | bold italic underline strikethrough | charmap link image',
          forced_root_block : ''
        };

        // Prep query
        this.prepQuery = function(query) {
          if (!query) {
            return '';
          }

          // Add a * to the filter if set and doesn't contain a : ( or "
          if (query.indexOf("(") == -1 && query.indexOf(":") == -1 && query.indexOf("\"") == -1) {
            var query2 = query.concat('*');
            return encodeURIComponent(query2);
          }
          return encodeURIComponent(query);
        };

        // Prep pfs filter
        this.prepPfs = function(pfs) {
          if (!pfs) {
            return {};
          }

          // Add a * to the filter if set and doesn't contain a :
          if (pfs.queryRestriction && pfs.queryRestriction.indexOf(":") == -1
            && pfs.queryRestriction.indexOf("\"") == -1) {
            var pfs2 = angular.copy(pfs);
            pfs2.queryRestriction += "*";
            return pfs2;
          }
          return pfs;
        };

        // Sets the error
        this.setError = function(message) {
          console.error(message);
          this.error.message = message;
          $location.hash('top');
          $anchorScroll();
        };

        // Clears the error
        this.clearError = function() {
          this.error.message = null;
          this.error.longMessage = null;
          this.error.expand = false;
        };

        // Sets the success
        this.setSuccess = function(message) {
          this.success.message = message;
        };

        // Clears the success
        this.clearSuccess = function() {
          this.success.message = null;
          this.success.longMessage = null;
          this.success.expand = false;
        };

        this.handleSuccess = function(message) {
          if (message && message.legth > 100) {
            this.success.message = 'Successful process reported, click the icon to view full message';
            this.success.longMessage = message;
          } else {
            this.success.message = message;
          }

          // scroll to top of page
          $location.hash('top');
          $anchorScroll();
        };

        // Handle error message
        this.handleError = function(response) {
          if (response.data && response.data.length > 100) {
            this.error.message = "Unexpected error, click the icon to view attached full error";
            this.error.longMessage = response.data;
            console.error(this.error.longMessage);
          } else {
            this.error.message = response.data;
            console.error(this.error.message);
          }
          // handle no message
          if (!this.error.message) {
            // Print the stack trace so we know where the error came from
            e = new Error();
            console.log("ERROR", e.stack);

            console.error(this.error.message);
            this.error.message = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (this.error.message && this.error.message.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            if (appConfig['deploy.login.enabled'] === 'true') {
              $location.path('/login');
            } else {
              $location.path('/');
            }
          } else {
            // scroll to top of page
            $location.hash('top');
            $anchorScroll();
          }
        };

        // Dialog error handler
        this.handleDialogError = function(errors, error) {
          // handle long error
          if (error && error.length > 100) {
            errors[0] = "Unexpected error, click the icon to view attached full error";
            errors[1] = error;
          } else {
            errors[0] = error;
          }
          // handle no message
          if (!error) {
            errors[0] = "Unexpected server side error.";
          }
          // If authtoken expired, relogin
          if (error && error.indexOf('AuthToken') != -1) {
            // Reroute back to login page with 'auth token has
            // expired' message
            $location.path('/');
          }
          // otherwise clear the top-level error
          else {
            this.clearError();
          }
        };

        // Set a flag indicating whether header/footer are to be showing
        this.setHeaderFooterShowing = function(showHeaderFooter) {
          this.showHeaderFooter = showHeaderFooter;
        };

        // Indicates whether header/footer are showing at all
        this.isHeaderFooterShowing = function() {
          return this.showHeaderFooter;
        };

        this.composeUrl = function(extension) {
          var currentUrl = $location.absUrl();
          var baseUrl = currentUrl.substring(0, currentUrl.indexOf('#') + 1);
          var newUrl = baseUrl + extension;
          return newUrl;
        }

        // Convert seconds to hour/min string
        this.toTime = function(secs) {
          if (secs) {
            var date = new Date(null);
            date.setSeconds(secs);
            return date.toISOString().substr(11, 8);
          }

          // if (d == 0)
          // return "";
          // var h = Math.floor(d / 3600);
          // var m = Math.floor(d % 3600 / 60);
          // return ((h + ":" + (m < 10 ? "0" : "") ) + m);
        }

        // Convert date to a string
        this.toDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          var hour = '' + date.getHours();
          if (hour.length == 1) {
            hour = '0' + hour;
          }
          var minute = '' + date.getMinutes();
          if (minute.length == 1) {
            minute = '0' + minute;
          }
          var second = '' + date.getSeconds();
          if (second.length == 1) {
            second = '0' + second;
          }
          return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
        };

        // Convert date to a short string
        this.toShortDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + '-' + month + '-' + day;
        };

        // Convert date to a simple string
        this.toSimpleDate = function(lastModified) {
          var date = new Date(lastModified);
          var year = '' + date.getFullYear();
          var month = '' + (date.getMonth() + 1);
          if (month.length == 1) {
            month = '0' + month;
          }
          var day = '' + date.getDate();
          if (day.length == 1) {
            day = '0' + day;
          }
          return year + month + day;
        };

        // Uniq an array of simple data types
        this.uniq = function uniq(a) {
          var seen = {};
          return a.filter(function(item) {
            return seen.hasOwnProperty(item) ? false : (seen[item] = true);
          });
        };

        // Table sorting mechanism
        this.setSortField = function(table, field, paging) {
          paging[table].sortField = field;
          // reset page number too
          paging[table].page = 1;
          // handles null case also
          if (!paging[table].sortAscending) {
            paging[table].sortAscending = true;
          } else {
            paging[table].sortAscending = false;
          }
          // reset the paging for the correct table
          for ( var key in paging) {
            if (paging.hasOwnProperty(key)) {
              if (key == table)
                paging[key].page = 1;
            }
          }
        };

        // Return up or down sort chars if sorted
        this.getSortIndicator = function(table, field, paging) {
          if (paging[table].sortAscending == null) {
            return '';
          }
          if (paging[table].sortField == field && paging[table].sortAscending) {
            return '▴';
          }
          if (paging[table].sortField == field && !paging[table].sortAscending) {
            return '▾';
          }
        };

        // Helper function to get a standard paging object
        // overwritten as needed
        // Example of filterFields
        // paging.filterFields.terminologyId = 1;
        // paging.filterFields.expandedForm = 1;
        //
        this.getPaging = function() {
          return {
            page : 1,
            pageSize : 10,
            filter : '',
            filterFields : null,
            sortField : null,
            sortAscending : true,
            sortOptions : []
          };
        };

        // Helper to get a paged array with show/hide flags
        // and filtered by query string
        // use when all data is already loaded
        this.getPagedArray = function(array, paging) {
          var newArray = new Array();

          // if array blank or not an array, return blank list
          if (array == null || array == undefined || !Array.isArray(array)) {
            return newArray;
          }

          newArray = array;
          // apply suppressible/obsolete

          // apply sort if specified
          if (paging.sortField) {
            // if ascending specified, use that value, otherwise use false
            newArray.sort(this.sortBy(paging.sortField, paging.sortAscending));
          }

          // apply filter
          if (paging.filter) {
            newArray = this.getArrayByFilter(newArray, paging.filter, paging.filterFields);
          }

          // get the page indices (if supplied)
          var results;
          if (paging.pageSize != -1) {
            var fromIndex = (paging.page - 1) * paging.pageSize;
            var toIndex = Math.min(fromIndex + paging.pageSize, array.length);

            // slice the array
            results = newArray.slice(fromIndex, toIndex);
          } else {
            results = newArray;
          }

          return {
            data : results,
            totalCount : newArray.length
          };
        };

        // function for sorting an array by (string) field and direction
        this.sortBy = function(field, reverse) {

          var fields = field.split(',');

          // key: function to return field value from object
          var keys = {};
          for (var i = 0; i < fields.length; i++) {
            var f = fields[i];
            keys[f] = function(x) {
              return x[f];
            };
          }
          // convert reverse to integer (1 = ascending, -1 =
          // descending)
          reverse = !reverse ? 1 : -1;

          return function(a, b) {
            for (var i = 0; i < fields.length; i++) {
              var key = fields[i];
              a = keys[key](a);
              b = keys[key](b);
              if (a == b) {
                continue;
              }
              return reverse * ((a > b) - (b > a));
            }
            return 0;
          };
        };

        // Get array by filter text matching terminologyId or name
        this.getArrayByFilter = function(array, filter, fields) {
          var newArray = [];
          for ( var object in array) {

            if (this.objectContainsFilterText(array[object], filter, fields)) {
              newArray.push(array[object]);
            }
          }
          return newArray;
        };

        // Returns true if any field on object contains filter text
        this.objectContainsFilterText = function(object, filter, fields) {

          if (!filter || !object)
            return false;

          for ( var prop in object) {
            // skip if fields are defined but not specified in prop
            if (fields && !fields[prop]) {
              continue;
            }
            var value = object[prop];
            // check property for string, note this will cover child elements
            if (value && value.toString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
              return true;
            }
          }

          return false;
        };

        // Finds the object in a list by the field
        this.findBy = function(list, obj, field) {

          // key: function to return field value from object
          var key = function(x) {
            return x[field];
          };

          for (var i = 0; i < list.length; i++) {
            if (key(list[i]) == key(obj)) {
              return list[i];
            }
          }
          return null;
        };

        // Get words of a string
        this.getWords = function(str) {
          // Same as in tinymce options
          return str.match(/[^\s,\.]+/g);
        };

        // Single and multiple-word ordered phrases
        this.getPhrases = function(str) {
          var words = str.match(/[^\s,\.]+/g);
          var phrases = [];

          for (var i = 0; i < words.length; i++) {
            for (var j = i + 1; j <= words.length; j++) {
              var phrase = words.slice(i, j).join(' ');
              // a phrase have at least 5 chars and no start/end words that are
              // purely punctuation
              if (phrase.length > 5 && words[i].match(/.*[A-Za-z0-9].*/)
                && words[j - 1].match(/.*[A-Za-z0-9].*/)) {
                phrases.push(phrase.toLowerCase());
              }
            }
          }
          return phrases;
        };

        // Utility for cleaning a query
        this.cleanQuery = function(queryStr) {
          if (queryStr == null) {
            return "";
          }
          var cleanQuery = queryStr;
          // Replace all slash characters
          cleanQuery = queryStr.replace(new RegExp('[/\\\\]', 'g'), ' ');
          // Remove brackets if not using a fielded query
          if (queryStr.indexOf(':') == -1) {
            cleanQuery = queryStr.replace(new RegExp('[^a-zA-Z0-9:\\.\\-\'\\*"]', 'g'), ' ');
          }
          return cleanQuery;
        };

        //
        // Extends a callbacks object, checking for name clashes
        //
        this.extendCallbacks = function(callbacks, itemsToAdd) {
          if (!callbacks) {
            callbacks = {};
          }
          for ( var key in itemsToAdd) {
            if (callbacks.hasOwnProperty(key)) {
              utilService
                .setError('Error constructing callbacks, name clash for ' + key, callbacks);
              return;
            }
            callbacks[key] = itemsToAdd[key];
          }
        };

      } ]);

// Glass pane service
tsApp.service('gpService', function() {
  console.debug('configure gpService');
  // declare the glass pane counter
  this.glassPane = {
    counter : 0
  };

  this.isGlassPaneSet = function() {
    return this.glassPane.counter;
  };

  this.isGlassPaneNegative = function() {
    return this.glassPane.counter < 0;
  };

  // Increments glass pane counter
  this.increment = function(message) {
    this.glassPane.counter++;
  };

  // Decrements glass pane counter
  this.decrement = function() {
    this.glassPane.counter--;
  };

});
