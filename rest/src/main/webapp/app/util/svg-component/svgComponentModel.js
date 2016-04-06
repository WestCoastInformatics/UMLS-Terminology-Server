/**
 * Directive to append a image file generated via SVG to a parent div Example:
 * <div svg-component-model component="component"></div>
 */
'use strict';
// jshint ignore: start
angular.module('tsApp').directive(
  'svgComponentModel',
  function() {
    return {
      restrict : 'A',
      transclude : false,
      replace : true,
      scope : {
        component : '=',
      },
      templateUrl : 'app/util/svg-component/svgComponentModel.html',

      link : function(scope, element, attrs) {
        var idSequence = 0;
        scope.$watch('component', function(newVal, oldVal) {
          setTimeout(function() {
            drawComponentDiagram(scope.component, element, {}, {});
          }, 100);
        }, true);

        // Draw the diagram
        function drawComponentDiagram(component, div, options, snfComponent) {
          console.debug("draw component diagram=", component, div, options, snfComponent);

          var svgIsaModel = new Array();
          var svgAttrModel = new Array();

          if (component.relationships) {
            angular.forEach(component.relationships, function(i, field) {
              if (field.active) {
                // TODO: make this configurable, handled by loader
                if (field.type == 'CHD') {
                  svgIsaModel.push(field);
                } else {
                  svgAttrModel.push(field);
                }
              }
            });
          }

          var parentDiv = div;

          parentDiv.svg({
            settings : {
              // TODO: figure this out
              height : '1250px',
              width : '2500px',
              id : 'svg-' + component.componentId
            }
          });
          var svg = parentDiv.svg('get');
          loadDefs(svg);
          var x = 10;
          var y = 10;
          var maxX = 10;
          var sctClass = "";
          if (!component.fullyDefined == "PRIMITIVE") {
            sctClass = "sct-primitive-component";
          } else {
            sctClass = "sct-defined-component";
          }
          var rect1 = drawBox(svg, x, y, component.name, component.terminologyId, sctClass);
          x = x + 90;
          y = y + rect1.getBBox().height + 40;
          var circle1;
          if (!component.fullyDefined == "PRIMITIVE") {
            circle1 = drawSubsumedByNode(svg, x, y);
          } else {
            circle1 = drawEquivalentNode(svg, x, y);
          }
          connectElements(svg, rect1, circle1, 'bottom-50', 'left');
          x = x + 55;
          var circle2 = drawConjunctionNode(svg, x, y);
          connectElements(svg, circle1, circle2, 'right', 'left', 'LineMarker');
          x = x + 40;
          y = y - 18;
          maxX = ((maxX < x) ? x : maxX);
          // load stated parents
          sctClass = "sct-defined-component";
          angular.forEach(svgIsaModel, function(i, relationship) {
            // May need to deal with this
            if (!relationship.to.fullyDefined == "PRIMITIVE") {
              sctClass = "sct-primitive-component";
            } else {
              sctClass = "sct-defined-component";
            }
            var rectParent = drawBox(svg, x, y, relationship.to.name,
              relationship.to.terminologyId, sctClass);
            // $("#" + rectParent.id).css({"top":
            // (rectParent.outerHeight()/2) + "px"});
            connectElements(svg, circle2, rectParent, 'center', 'left', 'ClearTriangle');
            y = y + rectParent.getBBox().height + 25;
            maxX = ((maxX < x + rectParent.getBBox().width + 50) ? x + rectParent.getBBox().width
              + 50 : maxX);
          });

          // load ungrouped attributes
          var maxRoleNumber = 0;
          angular.forEach(svgAttrModel,
            function(i, relationship) {
              if (!relationship.to.fullyDefined == "PRIMITIVE") {
                sctClass = "sct-primitive-component";
              } else {
                sctClass = "sct-defined-component";
              }
              if (relationship.group == 0) {
                if (relationship.nest) {
                  var rectAttr = drawBox(svg, x, y, relationship.type.name,
                    relationship.type.componentId, "sct-attribute");
                  connectElements(svg, circle2, rectAttr, 'center', 'left');
                  x = x + rectAttr.getBBox().width + 25;
                  y = y + rectAttr.getBBox().height / 2;
                  var circle3 = drawConjunctionNode(svg, x, y);
                  connectElements(svg, rectAttr, circle3, 'right', 'left', 'LineMarker');
                  y = y - rectAttr.getBBox().height / 2;
                  x = x - 100;
                  var rectTarget = drawBox(svg, x + rectAttr.getBBox().width + 50, y,
                    relationship.target.name, relationship.target.componentId, sctClass);
                  x = x + 100;
                  connectElements(svg, circle3, rectTarget, 'right', 'left');
                  y = y + rectTarget.getBBox().height + 25;
                  maxX = ((maxX < x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width
                    + 50) ? x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width + 50
                    : maxX);
                } else {
                  var rectAttr = drawBox(svg, x, y, relationship.type.name,
                    relationship.type.componentId, "sct-attribute");
                  connectElements(svg, circle2, rectAttr, 'center', 'left');
                  var rectTarget = drawBox(svg, x + rectAttr.getBBox().width + 50, y,
                    relationship.target.name, relationship.target.componentId, sctClass);
                  connectElements(svg, rectAttr, rectTarget, 'right', 'left');
                  y = y + rectTarget.getBBox().height + 25;
                  maxX = ((maxX < x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width
                    + 50) ? x + rectAttr.getBBox().width + 50 + rectTarget.getBBox().width + 50
                    : maxX);
                }
              } else {
                if (relationship.groupId > maxRoleNumber) {
                  maxRoleNumber = relationship.groupId;
                }
              }
              // TODO: can probably remove "nest"
              if (relationship.nest) {
                if (relationship.nest[0].target.definitionStatus == "PRIMITIVE") {
                  sctClass = "sct-primitive-component";
                } else {
                  sctClass = "sct-defined-component";
                }
                y = y + 25;
                x = x + 50;
                var rectAttrNest = drawBox(svg, x, y, relationship.nest[0].type.name,
                  relationship.nest[0].type.componentId, "sct-attribute");
                connectElements(svg, circle3, rectAttrNest, 'center', 'left');
                var rectTargetNest = drawBox(svg, x + rectAttrNest.getBBox().width + 50, y,
                  relationship.nest[0].target.name, relationship.nest[0].target.componentId,
                  sctClass);
                connectElements(svg, rectAttrNest, rectTargetNest, 'right', 'left');
                y = y + rectTarget.getBBox().height + 25;
                maxX = ((maxX < x + rectAttrNest.getBBox().width + 50
                  + rectTargetNest.getBBox().width + 50) ? x + rectAttrNest.getBBox().width + 50
                  + rectTargetNest.getBBox().width + 50 : maxX);
              }
            });
          y = y + 15;
          for (var i = 1; i <= maxRoleNumber; i++) {
            var groupNode = drawAttributeGroupNode(svg, x, y);
            connectElements(svg, circle2, groupNode, 'center', 'left');
            var conjunctionNode = drawConjunctionNode(svg, x + 55, y);
            connectElements(svg, groupNode, conjunctionNode, 'right', 'left');
            angular.forEach(svgAttrModel, function(m, relationship) {
              if (relationship.groupId == i) {
                if (relationship.target.definitionStatus == "PRIMITIVE") {
                  sctClass = "sct-primitive-component";
                } else {
                  sctClass = "sct-defined-component";
                }
                var rectRole = drawBox(svg, x + 85, y - 18, relationship.type.name,
                  relationship.type.componentId, "sct-attribute");
                connectElements(svg, conjunctionNode, rectRole, 'center', 'left');
                var rectRole2 = drawBox(svg, x + 85 + rectRole.getBBox().width + 30, y - 18,
                  relationship.target.name, relationship.target.componentId, sctClass);
                connectElements(svg, rectRole, rectRole2, 'right', 'left');
                y = y + rectRole2.getBBox().height + 25;
                maxX = ((maxX < x + 85 + rectRole.getBBox().width + 30 + rectRole2.getBBox().width
                  + 50) ? x + 85 + rectRole.getBBox().width + 30 + rectRole2.getBBox().width + 50
                  : maxX);
              }
            });
          }

          // TODO: no need to convert to PNG (that was for resizing)

          // var svgCode = '<?xml version="1.0" encoding="UTF-8"
          // standalone="no"?>'
          // + parentDiv.html();
          // svgCode = svgCode.substr(0, svgCode.indexOf("svg") + 4)
          // + ' xmlns:dc="http://purl.org/dc/elements/1.1/"
          // xmlns:cc="http://web.resource.org/cc/"
          // xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
          // xmlns:svg="http://www.w3.org/2000/svg"
          // xmlns="http://www.w3.org/2000/svg" '
          // + svgCode.substr(svgCode.indexOf("svg") + 4)
          // svgCode = svgCode.replace('width="1000px" height="2000px"',
          // 'width="' + maxX
          // + '" height="' + y + '"');
          // // var b64 = Base64.encode(svgCode);
          //
          // convertToPng(svgCode, component.componentId);
        }

        // Draw a box
        function drawBox(svg, x, y, label, compId, cssClass) {
          // console.log("In svg: " + label + " " + compId + " " + cssClass);
          // x,y coordinates of the top-left corner
          var testText = "Test";
          if (label && compId) {
            if (label.length > compId.toString().length) {
              testText = label;
            } else {
              testText = compId.toString();
            }
          } else if (label) {
            testText = label;
          } else if (compId) {
            testText = compId.toString();
          }
          var fontFamily = '"Helvetica Neue",Helvetica,Arial,sans-serif';
          // var fontFamily = 'sans-serif';
          var tempText = svg.text(x, y, testText, {
            fontFamily : fontFamily,
            fontSize : '12',
            fill : 'black'
          });
          var textHeight = tempText.getBBox().height;
          var textWidth = tempText.getBBox().width;
          textWidth = Math.round(textWidth * 1.2);
          svg.remove(tempText);

          var rect = null;
          var widthPadding = 20;
          var heightpadding = 25;

          if (!compId || !label) {
            heightpadding = 15;
          }

          if (cssClass == "sct-primitive-component") {
            rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
              id : 'rect' + idSequence,
              fill : '#99ccff',
              stroke : '#333',
              strokeWidth : 2
            });
          } else if (cssClass == "sct-defined-component") {
            rect = svg.rect(x - 2, y - 2, textWidth + widthPadding + 4, textHeight + heightpadding
              + 4, {
              fill : 'white',
              stroke : '#333',
              strokeWidth : 1
            });
            var innerRect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
              id : 'rect' + idSequence,
              fill : '#ccccff',
              stroke : '#333',
              strokeWidth : 1
            });
          } else if (cssClass == "sct-attribute") {
            rect = svg.rect(x - 2, y - 2, textWidth + widthPadding + 4, textHeight + heightpadding
              + 4, 18, 18, {
              fill : 'white',
              stroke : '#333',
              strokeWidth : 1
            });
            var innerRect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding,
              18, 18, {
                id : 'rect' + idSequence,
                fill : '#ffffcc',
                stroke : '#333',
                strokeWidth : 1
              });
          } else if (cssClass == "sct-slot") {
            rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
              id : 'rect' + idSequence,
              fill : '#99ccff',
              stroke : '#333',
              strokeWidth : 2
            });
          } else {
            rect = svg.rect(x, y, textWidth + widthPadding, textHeight + heightpadding, {
              id : 'rect' + idSequence,
              fill : 'white',
              stroke : 'black',
              strokeWidth : 1
            });
          }

          if (compId && label) {
            svg.text(x + 10, y + 16, compId.toString(), {
              fontFamily : fontFamily,
              fontSize : '10',
              fill : 'black'
            });
            svg.text(x + 10, y + 31, label, {
              fontFamily : fontFamily,
              fontSize : '12',
              fill : 'black'
            });
          } else if (label) {
            svg.text(x + 10, y + 18, label, {
              fontFamily : fontFamily,
              fontSize : '12',
              fill : 'black'
            });
          } else if (compId) {
            svg.text(x + 10, y + 18, compId.toString(), {
              fontFamily : fontFamily,
              fontSize : '12',
              fill : 'black'
            });
          }

          idSequence++;

          // Probably not needed
          // $('rect').click(function(evt) {
          // });

          return rect;
        }

        // Connect elements
        function connectElements(svg, fig1, fig2, side1, side2, endMarker) {
          var rect1cx = fig1.getBBox().x;
          var rect1cy = fig1.getBBox().y;
          var rect1cw = fig1.getBBox().width;
          var rect1ch = fig1.getBBox().height;

          var rect2cx = fig2.getBBox().x;
          var rect2cy = fig2.getBBox().y;
          var rect2cw = fig2.getBBox().width;
          var rect2ch = fig2.getBBox().height;

          var markerCompensantion1 = 15;
          var markerCompensantion2 = 15;

          switch (side1) {
          case 'top':
            var originY = rect1cy;
            var originX = rect1cx + (rect1cw / 2);
            break;
          case 'bottom':
            var originY = rect1cy + rect1ch;
            var originX = rect1cx + (rect1cw / 2);
            break;
          case 'left':
            var originX = rect1cx - markerCompensantion1;
            var originY = rect1cy + (rect1ch / 2);
            break;
          case 'right':
            var originX = rect1cx + rect1cw;
            var originY = rect1cy + (rect1ch / 2);
            break;
          case 'bottom-50':
            var originY = rect1cy + rect1ch;
            var originX = rect1cx + 40;
            break;
          default:
            var originX = rect1cx + (rect1cw / 2);
            var originY = rect1cy + (rect1ch / 2);
            break;
          }

          switch (side2) {
          case 'top':
            var destinationY = rect2cy;
            var destinationX = rect2cx + (rect2cw / 2);
            break;
          case 'bottom':
            var destinationY = rect2cy + rect2ch;
            var destinationX = rect2cx + (rect2cw / 2);
            break;
          case 'left':
            var destinationX = rect2cx - markerCompensantion2;
            var destinationY = rect2cy + (rect2ch / 2);
            break;
          case 'right':
            var destinationX = rect2cx + rect2cw;
            var destinationY = rect2cy + (rect2ch / 2);
            break;
          case 'bottom-50':
            var destinationY = rect2cy + rect2ch;
            var destinationX = rect2cx + 50;
            break;
          default:
            var destinationX = rect2cx + (rect2cw / 2);
            var destinationY = rect2cy + (rect2ch / 2);
            break;
          }

          if (endMarker == null) {
            endMarker = "BlackTriangle";
          }

          var polyline1 = svg.polyline([ [ originX, originY ], [ originX, destinationY ],
            [ destinationX, destinationY ] ], {
            id : 'poly1',
            fill : 'none',
            stroke : 'black',
            strokeWidth : 2,
            'marker-end' : 'url(#' + endMarker + ')'
          });

        }

        // Load SVG defintions
        function loadDefs(svg) {
          var defs = svg.defs();
          var blackTriangle = svg.marker(defs, 'BlackTriangle', 0, 0, 20, 20, {
            viewBox : '0 0 22 20',
            refX : '0',
            refY : '10',
            markerUnits : 'strokeWidth',
            markerWidth : '8',
            markerHeight : '6',
            fill : 'black',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.path(blackTriangle, 'M 0 0 L 20 10 L 0 20 z');

          var clearTriangle = svg.marker(defs, 'ClearTriangle', 0, 0, 20, 20, {
            viewBox : '0 0 22 20',
            refX : '0',
            refY : '10',
            markerUnits : 'strokeWidth',
            markerWidth : '8',
            markerHeight : '8',
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.path(clearTriangle, 'M 0 0 L 20 10 L 0 20 z');

          var lineMarker = svg.marker(defs, 'LineMarker', 0, 0, 20, 20, {
            viewBox : '0 0 22 20',
            refX : '0',
            refY : '10',
            markerUnits : 'strokeWidth',
            markerWidth : '8',
            markerHeight : '8',
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.path(lineMarker, 'M 0 10 L 20 10');
        }

        // Draw an attribute group
        function drawAttributeGroupNode(svg, x, y) {
          var circle = svg.circle(x, y, 20, {
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          return circle;
        }

        // Draw a conjunction node
        function drawConjunctionNode(svg, x, y) {
          var circle = svg.circle(x, y, 10, {
            fill : 'black',
            stroke : 'black',
            strokeWidth : 2
          });
          return circle;
        }

        // Draw an equivalent node
        function drawEquivalentNode(svg, x, y) {
          var g = svg.group();
          svg.circle(g, x, y, 20, {
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y - 5, x + 7, y - 5, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y, x + 7, y, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y + 5, x + 7, y + 5, {
            stroke : 'black',
            strokeWidth : 2
          });
          return g;
        }

        // Draw a subsumed by node
        function drawSubsumedByNode(svg, x, y) {
          var g = svg.group();
          svg.circle(g, x, y, 20, {
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y - 8, x + 7, y - 8, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y + 3, x + 7, y + 3, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 6, y - 8, x - 6, y + 3, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y + 7, x + 7, y + 7, {
            stroke : 'black',
            strokeWidth : 2
          });
          return g;
        }

        // Draw a subsumes node
        function drawSubsumesNode(svg, x, y) {
          var g = svg.group();
          svg.circle(g, x, y, 20, {
            fill : 'white',
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y - 8, x + 7, y - 8, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y + 3, x + 7, y + 3, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x + 6, y - 8, x + 6, y + 3, {
            stroke : 'black',
            strokeWidth : 2
          });
          svg.line(g, x - 7, y + 7, x + 7, y + 7, {
            stroke : 'black',
            strokeWidth : 2
          });
          return g;
        }

      }
    };
  });