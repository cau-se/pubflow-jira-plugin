
(function(){

///////////////////////////////////////////////////////////////////
// GRAPH FLOW MODEL (an extended ForceDirected version)          //
///////////////////////////////////////////////////////////////////

/*
 * File: Layouts.GraphFlow.js
 *
*/

/*
 * Class: Layouts.GraphFlow
 * 
 * Implements a Force Directed Layout.
 * 
 * Implemented By:
 * 
 * <GraphFlow>
 * 
 * Credits:
 * 
 * Marcus Cobden <http://marcuscobden.co.uk>
 *
 */
$jit.Layouts.GraphFlow = new Class({
  getOptions: function(random) {
    var s = this.canvas.getSize();
    var w = s.width, h = s.height;
    //count nodes
    var count = 0;
    this.graph.eachNode(function(n) { 
      count++;
    });
    var k2 = w * h / count, k = Math.sqrt(k2);
    var l = this.config.levelDistance;
    
    return {
      width: w,
      height: h,
      tstart: w * 0.1,
      nodef: function(x) { return k2 / (x || 1); },
      edgef: function(x) { return /* x * x / k; */ k * (x - l); }
    };
  }
});

/*
 * File: GraphFlow.js
 */

/*
   Class: GraphFlow
      
   A visualization that lays graphs using a Force-Directed layout algorithm. (for now)
   
   Inspired by:
  
   Force-Directed Drawing Algorithms (Stephen G. Kobourov) <http://www.cs.brown.edu/~rt/gdhandbook/chapters/force-directed.pdf>
   
  Implements:
  
  All <Loader> methods
  
   Constructor Options:
   
   Inherits options from
   
   - <Options.Canvas>
   - <Options.Controller>
   - <Options.Node>
   - <Options.Edge>
   - <Options.Label>
   - <Options.Events>
   - <Options.Tips>
   - <Options.NodeStyles>
   - <Options.Navigation>
   
   Additionally, there are two parameters
   
   levelDistance - (number) Default's *50*. The natural length desired for the edges.
   iterations - (number) Default's *50*. The number of iterations for the spring layout simulation. Depending on the browser's speed you could set this to a more 'interesting' number, like *200*. 
     
   Instance Properties:

   canvas - Access a <Canvas> instance.
   graph - Access a <Graph> instance.
   op - Access a <GraphFlow.Op> instance.
   fx - Access a <GraphFlow.Plot> instance.
   labels - Access a <GraphFlow.Label> interface implementation.

*/

var Extras = $jit.getExtras();
var Loader = $jit.getLoader();

$jit.GraphFlow = new Class( {

  Implements: [ Loader, Extras, $jit.Layouts.GraphFlow ],
  
  initialize: function(controller) {
	
    var $GraphFlow = $jit.GraphFlow;
    var config = {
      iterations: 1,
      levelDistance: 0
    };

    this.controller = this.config = $.merge(Options("Canvas", "Node", "Edge",
        "Fx", "Tips", "NodeStyles", "Events", "Navigation", "Controller", "Label"), config, controller);

    var canvasConfig = this.config;
    if(canvasConfig.useCanvas) {
      this.canvas = canvasConfig.useCanvas;
      this.config.labelContainer = this.canvas.id + '-label';
    } else {
      if(canvasConfig.background) {
        canvasConfig.background = $.merge({
          type: 'Circles'
        }, canvasConfig.background);
      }
      this.canvas = new Canvas(this, canvasConfig);
      this.config.labelContainer = (typeof canvasConfig.injectInto == 'string'? canvasConfig.injectInto : canvasConfig.injectInto.id) + '-label';
    }

    this.graphOptions = {
      'complex': true,
      'Node': {
        'selected': false,
        'exist': true,
        'drawn': true
      }
    };
 
    
    this.graph = new Graph(this.graphOptions, this.config.Node,
        this.config.Edge);
    this.labels = new $GraphFlow.Label[canvasConfig.Label.type](this);
    this.fx = new $GraphFlow.Plot(this, $GraphFlow);
    this.op = new $GraphFlow.Op(this);
    this.json = null;
    this.busy = false;
    
    this.animationFix = {
    	'duration' : 0,
    	'busy' : false,
    	'map' : undefined
    };
    
    //overwrite faulty click function
    this.Classes.Events.prototype.onMouseDown = function(e, win, event) {
        var evt = $.event.get(e, win);
        
        if(this.hovered){
      	  this.pressed = this.hovered;
        }else{
      	  this.pressed = event.getNode() || (this.config.enableForEdges && event.getEdge());
        }
        this.config.onDragStart(this.pressed, event, evt);
      };
      
    // extend Interpolator for new values
    var that = this;
    var iMap = this.fx.Interpolator.map;
    
    this.fx.Interpolator.canvas = 
    	function(c, delta, doZoom, doTranslate){
    		var scale;
    		
    		// scale
    		if(doZoom){
    			var oldScale = c.scaleOffsetOld;
    			var newScale = c.scaleOffsetEnd;
    			scale = c.scaleOffsetX;
    			
    			newScale = that.fx.Interpolator.compute(oldScale, newScale, delta) / scale;
    			c.scale(newScale, newScale);
    		}
    		
    		// translate
    		if(doTranslate){
	    		scale = c.scaleOffsetX;
		    	var fromX = c.translateOffsetX / scale,
					fromY = c.translateOffsetY / scale;
				var toX = c.translateOffsetXEnd,
					toY = c.translateOffsetYEnd;
			  
				var newX = that.fx.Interpolator.compute(fromX, toX, delta),
					newY = that.fx.Interpolator.compute(fromY, toY, delta);
				
				c.translate(newX - fromX, newY - fromY);
    		}
    	};
    	
    iMap.fillColor = "color"; 
    // "number", "array-number"
    
    // initialize extras
    this.initializeExtras();
  },
  
  /* 
    Method: refresh 
    
    Computes positions and plots the tree.
  */
  refresh: function() {
    //this.compute();
	this.canvas.clear();
    this.fx.plot();
  },

  reposition: function() {
    //this.compute('end');
  },

/*
  Method: computeIncremental
  
  Performs the Force Directed algorithm incrementally.
  
  Description:
  
  ForceDirected algorithms can perform many computations and lead to JavaScript taking too much time to complete. 
  This method splits the algorithm into smaller parts allowing the user to track the evolution of the algorithm and 
  avoiding browser messages such as "This script is taking too long to complete".
  
  Parameters:
  
  opt - (object) The object properties are described below
  
  iter - (number) Default's *20*. Split the algorithm into pieces of _iter_ iterations. For example, if the _iterations_ configuration property 
  of your <ForceDirected> class is 100, then you could set _iter_ to 20 to split the main algorithm into 5 smaller pieces.
  
  property - (string) Default's *end*. Whether to update starting, current or ending node positions. Possible values are 'end', 'start', 'current'. 
  You can also set an array of these properties. If you'd like to keep the current node positions but to perform these 
  computations for final animation positions then you can just choose 'end'.
  
  onStep - (function) A callback function called when each "small part" of the algorithm completed. This function gets as first formal 
  parameter a percentage value.
  
  onComplete - A callback function called when the algorithm completed.
  
  Example:
  
  In this example I calculate the end positions and then animate the graph to those positions
  
  (start code js)
  var fg = new $jit.GraphFlow(...);
  fg.computeIncremental({
    iter: 20,
    property: 'end',
    onStep: function(perc) {
      Log.write("loading " + perc + "%");
    },
    onComplete: function() {
      Log.write("done");
      fg.animate();
    }
  });
  (end code)
  
  In this example I calculate all positions and (re)plot the graph
  
  (start code js)
  var fg = new GraphFlow(...);
  fg.computeIncremental({
    iter: 20,
    property: ['end', 'start', 'current'],
    onStep: function(perc) {
      Log.write("loading " + perc + "%");
    },
    onComplete: function() {
      Log.write("done");
      fg.plot();
    }
  });
  (end code)
  
  */
  /*
  computeIncremental: function(opt) {
    opt = $.merge( {
      iter: 1, // 20
      property: 'end',
      onStep: $.empty,
      onComplete: $.empty
    }, opt || {});

    this.config.onBeforeCompute(this.graph.getNode(this.root));
    this.compute(opt.property, opt);
  },*/

  /*
    Method: plot
   
    Plots the GraphFlow graph. This is a shortcut to *fx.plot*.
   */
  plot: function() {
	this.canvas.clear();
	this.fx.plot();
  },
  
  /*
     Method: animate
    
     Animates the graph from the current positions to the 'end' node positions.
  */
  animate: function(opt) {
    this.fx.animate($.merge( {
      modes: [ 'linear' ]
    }, opt || {}));
  } 
  
});

$jit.GraphFlow.$extend = true;

(function(GraphFlow) {
	
  /*
     Class: GraphFlow.Op
     
     Custom extension of <Graph.Op>.

     Extends:

     All <Graph.Op> methods
     
     See also:
     
     <Graph.Op>

  */
  GraphFlow.Op = new Class( {

    Implements: $jit.Graph.Op

  });

  /*
    Class: GraphFlow.Plot
    
    Custom extension of <Graph.Plot>.
  
    Extends:
  
    All <Graph.Plot> methods
    
    See also:
    
    <Graph.Plot>
  
  */
  GraphFlow.Plot = new Class( {
    Implements: $jit.Graph.Plot,
    
    /*
    Animates a <GraphFlow>, using the plotGraphFlow function instead of plot.
	 */
	animate: function(opt, versor) {
	    opt = $.merge(this.viz.config, opt || {});
	    
	    var that = this,
	        viz = this.viz,
	        animFix = viz.animationFix,
	        graph  = viz.graph,
	        interp = this.Interpolator,
	        animation =  opt.type === 'nodefx'? this.nodeFxAnimation : this.animation;
	    
	    // merge animation properties
	    if(animFix.busy){
	    	animFix.map = $.merge(animFix.map, this.prepare(opt.modes));
			
	    }else{
	    	animFix.map = this.prepare(opt.modes);
	    	animFix.busy = true;
	    }
	    
	    //prepare graph values
		var m = animFix.map;
		var doZoom = false;
		var doTrans = false;
		var moveCanvas = !!m.canvas;
		
		// prepare canvas move parameters
		if(moveCanvas){
			for(var c in m.canvas){
				c = m.canvas[c];
				if(c == "zoom"){
					doZoom = true;
				}else if(c == "translate"){
					doTrans = true;
				}
			}
		}
		
		var iterNodes = !!Object.keys(m).length;
		delete m.canvas;
		
	    //animate
	    animation.setOptions($.merge(opt, {
	      $animating: false,
	      compute: function(delta) {
	    	  
	    	if(iterNodes){
		        graph.eachNode(function(node) { 
		          for(var p in m) {
		            interp[p](node, m[p], delta, versor);
		          }
		        });
	    	}
	        viz.canvas.clear();
	        if(moveCanvas){
	    		interp.canvas(viz.canvas, delta, doZoom, doTrans);
	    	}
	        
	        that.plot(opt, this.$animating, delta);
	        this.$animating = true;
	      },
	      link: 'cancel',
	      complete: function() {
	    	viz.canvas.clear();
	        that.plot(opt);
	        opt.onComplete();
	        opt.onAfterCompute();
	        animFix.busy = false;
	      }       
	    })).start();
	  },
	
	/*
	  
	   Method: plot
	
	   Plots a <GraphFlow>. Paints edges above nodes and ignores drawing undefined nodes.
	
	   Parameters:
	
	   opt - (optional) Plotting options. Most of them are described in <Options.Fx>.
	
	   Example:
	
	   (start code js)
	   var viz = new $jit.Viz(options);
	   viz.fx.plot(); 
	   (end code)
	
	*/
	plot: function(opt, animating)  {
	  var viz = this.viz, 
	  aGraph = viz.graph, 
	  canvas = viz.canvas, 
	  id = viz.root, 
	  that = this, 
	  ctx = canvas.getCtx(), 
	  min = Math.min,
	  opt = opt || this.viz.controller;
	  //opt.clearCanvas && canvas.clear();
	  
	  var root = aGraph.getNode(id);
	  if(!root) return;
	  var T = !!root.visited;
	  
	  var edges = [];
	  
	  aGraph.eachNode(function(node) {
	    var nodeAlpha = node.getData('alpha');
	    node.eachAdjacency(function(adj) {
	       // memorize all edges, because they will be drawn above everything else
	       if(!!adj.nodeTo.visited === T && node.drawn && adj.nodeTo.drawn) {
	    	   edges.push(adj);
	       }
	    });
	    ctx.save();
	    if(node.drawn) {
	      !animating && opt.onBeforePlotNode(node);
	      that.plotNode(node, canvas, animating);
	      !animating && opt.onAfterPlotNode(node);
	    }
	    ctx.restore();
	    node.visited = !T;
	  });
	  var paintEdge = function(adj) {
	      var nodeTo = adj.nodeTo;
	        !animating && opt.onBeforePlotLine(adj);
	        ctx.save();
	        ctx.globalAlpha = min(nodeTo.getData('alpha'), 
	        				      adj.getData('alpha'));
	        that.plotLine(adj, canvas, animating);
	        ctx.restore();
	        !animating && opt.onAfterPlotLine(adj);
	    };
	  
	  // paint all edges above nodes
	  for(var e in edges){
		  paintEdge(edges[e]);
	  }
	}

  });

  /*
    Class: GraphFlow.Label
    
    Custom extension of <Graph.Label>. 
    Contains custom <Graph.Label.SVG>, <Graph.Label.HTML> and <Graph.Label.Native> extensions.
  
    Extends:
  
    All <Graph.Label> methods and subclasses.
  
    See also:
  
    <Graph.Label>, <Graph.Label.Native>, <Graph.Label.HTML>, <Graph.Label.SVG>.
  
  */
  GraphFlow.Label = {};

  /*
     GraphFlow.Label.Native
     
     Custom extension of <Graph.Label.Native>.

     Extends:

     All <Graph.Label.Native> methods

     See also:

     <Graph.Label.Native>

  */
  GraphFlow.Label.Native = new Class( {
    Implements: $jit.Graph.Label.Native
  });

  /*
    GraphFlow.Label.SVG
    
    Custom extension of <Graph.Label.SVG>.
  
    Extends:
  
    All <Graph.Label.SVG> methods
  
    See also:
  
    <Graph.Label.SVG>
  
  */
  GraphFlow.Label.SVG = new Class( {
    Implements: $jit.Graph.Label.SVG,

    initialize: function(viz) {
      this.viz = viz;
    },

    /* 
       placeLabel

       Overrides abstract method placeLabel in <Graph.Label>.

       Parameters:

       tag - A DOM label element.
       node - A <Graph.Node>.
       controller - A configuration/controller object passed to the visualization.
      
     */
    placeLabel: function(tag, node, controller) {
      var pos = node.pos.getc(true), 
          canvas = this.viz.canvas,
          ox = canvas.translateOffsetX,
          oy = canvas.translateOffsetY,
          sx = canvas.scaleOffsetX,
          sy = canvas.scaleOffsetY,
          radius = canvas.getSize();
      var labelPos = {
        x: Math.round(pos.x * sx + ox + radius.width / 2),
        y: Math.round(pos.y * sy + oy + radius.height / 2)
      };
      tag.setAttribute('x', labelPos.x);
      tag.setAttribute('y', labelPos.y);

      controller.onPlaceLabel(tag, node);
    }
  });

  /*
     GraphFlow.Label.HTML
     
     Custom extension of <Graph.Label.HTML>.

     Extends:

     All <Graph.Label.HTML> methods.

     See also:

     <Graph.Label.HTML>

  */
  GraphFlow.Label.HTML = new Class( {
    Implements: $jit.Graph.Label.HTML,

    initialize: function(viz) {
      this.viz = viz;
    },
    /* 
       placeLabel

       Overrides abstract method placeLabel in <Graph.Plot>.

       Parameters:

       tag - A DOM label element.
       node - A <Graph.Node>.
       controller - A configuration/controller object passed to the visualization.
      
     */
    placeLabel: function(tag, node, controller) {
      var pos = node.pos.getc(true), 
          canvas = this.viz.canvas,
          ox = canvas.translateOffsetX,
          oy = canvas.translateOffsetY,
          sx = canvas.scaleOffsetX,
          sy = canvas.scaleOffsetY,
          radius = canvas.getSize();
      var labelPos = {
        x: Math.round(pos.x * sx + ox + radius.width / 2),
        y: Math.round(pos.y * sy + oy + radius.height / 2)
      };
      var style = tag.style;
      style.left = labelPos.x + 'px';
      style.top = labelPos.y + 'px';
      style.display = this.fitsInCanvas(labelPos, canvas) ? '' : 'none';

      controller.onPlaceLabel(tag, node);
    }
  });

  
  GraphFlow.NodeHelper = {
		  
		  	/*
			 * Attempts to draw an image. Does nothing instead if no image
			 * exists.
			 */
			'drawImage' : function(ctx, img, x, y, width, height){
			  	 if (img && img != "emptyImage"){
			  		 ctx.drawImage(img, x, y, width, height);
			  	 }
			},
		  
		  /*
		  Object: NodeHelper.roundRect
		  */
		  'roundRect': {
		    /*
		    Method: render
		    
		    Renders a rounded rectangle into the canvas.
		    
		    Parameters:
		    
		    type - (string) Possible options are 'fill' or 'stroke'.
		    pos - (object) An *x*, *y* object with the position of the center of the rectangle.
		    width - (number) The width of the rectangle.
		    height - (number) The height of the rectangle.
			roundness - the radius of the rounded corners.
		    canvas - (object) A <Canvas> instance.
		    
		    Example:
		    (start code js)
		    NodeHelper.rectangle.render('fill', { x: 10, y: 30 }, 30, 40, viz.canvas);
		    (end code)
		    */
		    'render': function(type, pos, width, height, roundness, canvas){
				var hWidth = width/2;
					hHeight= height/2;
					ctx = canvas.getCtx(),
					rWidth = hWidth - roundness,
					rHeight = hHeight - roundness;
					x = pos.x - rWidth,
					y = pos.y - rHeight;
				ctx.save();
			  
				var pif = Math.PI/2,
					pit = 3 * pif;
				ctx.beginPath();
				// upper left
				ctx.arc(x , y , roundness, Math.PI, pit, false);
				x = pos.x + rWidth,
				ctx.lineTo(x, pos.y - hHeight);

				// upper right
				ctx.arc(x , y , roundness, pit, 0, false);
				y = pos.y + rHeight,
				ctx.lineTo(pos.x + hWidth, y);
				
				// lower right
				ctx.arc(x , y , roundness,0, pif, false);
				x = pos.x - rWidth,
				ctx.lineTo(x, pos.y + hHeight);

				// lower left
				ctx.arc(x , y , roundness, pif, Math.PI, false);
				ctx.closePath();

				ctx[type]();
				ctx.restore();
				
		    },
		    /*
		    Method: contains
		    
		    Returns *true* if *pos* is contained in the area of the shape. Returns *false* otherwise.
		    
		    Parameters:
		    
		    npos - (object) An *x*, *y* object with the <Graph.Node> position.
		    pos - (object) An *x*, *y* object with the position to check.
		    width - (number) The width of the rendered rectangle.
		    height - (number) The height of the rendered rectangle.
		    
		    Example:
		    (start code js)
		    NodeHelper.rectangle.contains({ x: 10, y: 30 }, { x: 15, y: 35 }, 30, 40);
		    (end code)
		    */
		    'contains': function(npos, pos, width, height){
		      return Math.abs(pos.x - npos.x) <= width / 2
		          && Math.abs(pos.y - npos.y) <= height / 2;
		    }
		  }
  };
  
  
  /*
    Class: GraphFlow.Plot.NodeTypes

    This class contains a list of <Graph.Node> built-in types. 
    Node types implemented are 'none', 'circle', 'triangle', 'rectangle', 'star', 'ellipse' and 'square'.

    You can add your custom node types, customizing your visualization to the extreme.

    Example:

    (start code js)
      GraphFlow.Plot.NodeTypes.implement({
        'mySpecialType': {
          'render': function(node, canvas) {
            //print your custom node to canvas
          },
          //optional
          'contains': function(node, pos) {
            //return true if pos is inside the node or false otherwise
          }
        }
      });
    (end code)

  */
  GraphFlow.Plot.NodeTypes = new Class({
    'none': {
      'render': $.empty
    },
	
	/**
		The nodeFamily is a grey rectangle with a dark stroke. Width, height, and fillColor of the rectangle must
		be saved in the nod.data properties.
		*/
	'nodeFamily': {
	    	  'render': function(node, canvas){
					
					//Log.write("render "+Math.random());
	    	        var data = node.data,
	    	        	pos = node.pos.getc(true), 
						width = data.$width,
						height = data.$height,
						dim = data.$dim,
						rounded = dim/4,
						strokeWidth = 2,
						fillColor = data.$fillColor,
						posX = pos.x ,
						posY = pos.y;
					var	ctx = canvas.getCtx();
					// draw Stroke ( in the color, specified in node properties)
					GraphFlow.NodeHelper.roundRect.render('fill', {x: posX, y: posY}, width+4, height+4, rounded, canvas);	
						
					// fill box
					ctx.fillStyle = fillColor;
					GraphFlow.NodeHelper.roundRect.render('fill', {x: posX, y: posY}, width, height, rounded, canvas);
	    	       	//this.nodeHelper.roundRect.render('fill', {x: posX, y: posY+rounded*2}, width, height-4*rounded, rounded, canvas);
					
					
					var tX = posX;
					var img = data.$icon;
					
					if(img){
						tX += dim;
					}
					
					var maxLabelWidth = 0;
					
					// paint name label
					var name = node.name;
					
					if(canvas.showLabels && name){
						
						var midX, midY, labelWidth, labelHeight;
						var labelSize = 0.83 * dim;
						ctx.fillStyle = data.$color;
						ctx.font = labelSize + data.$font;
						midY = pos.y - ((name.length - 2) * dim / 2);
						
						for(var t = 0, l = name.length; t < l; t++){
							labelWidth = ctx.measureText(name[t]).width/2;
							maxLabelWidth = Math.max(labelWidth, maxLabelWidth);
							midX = tX - labelWidth;
							
							ctx.fillText(name[t], midX, midY);
							midY += dim;
						}
					}
					
					// draw icon
					
					var ix = posX - maxLabelWidth - dim*1.3,
						iy = posY - dim ;
					GraphFlow.NodeHelper.drawImage(ctx,img, ix, iy, dim*2, dim*2);
					
	    	      },
				  
	    	      'contains': function(node, pos){
	    	        var npos = node.pos.getc(true), 
						dim = node.data.$dim/2,
	    	            width = node.data.$width/2 -dim,
						height = node.data.$height/2 -dim,
						checkPosX = pos.x - npos.x,
						checkPosY = pos.y - npos.y;
					var notInCloseButton = (checkPosX < width-dim || checkPosY > dim-height);
					var inBounds = Math.abs(checkPosX) <= width && Math.abs(checkPosY) <= height;
					
					//console.log("("+Math.round(Math.random()*9)+ ")" +node.id+".contains: "+ (notInCloseButton && inBounds));
	    	        return notInCloseButton && inBounds;
	        }
	},
	
	/**
		The crossBox is a red square with a white cross in its middle. Its size is exactly the node.dim.
		The node.pos marks the position of the upper-right corner of the box.
	*/
	'crossBox': {
	    	  'render': function(node, canvas){
					if(!node.data.$visible){
						return;
					}
					
	    	        var size = node.data.$dim;
	    	        if(canvas.showLabels){
						var	pos = node.pos.getc(true),
							hSize = size/2,
							bx = pos.x - hSize,
							by = pos.y - hSize/2,
		    	            ctx = canvas.getCtx();
						
							ctx.font = 0.83 * size +"px Verdana";
							ctx.fillText("x", bx, by); 
	    	        }
					
	    	      },
	    	      'contains': function(node, pos){
					if(!node.data.$visible){
						return false;
					}
					
	    	        var npos = node.pos.getc(true), 
	    	            size = node.data.$dim/2,
						xN = npos.x - size/2,
						yN = npos.y - size;
					return Math.abs(pos.x - xN) <= size/2 && Math.abs(pos.y - yN) <= size;
	        }
	},
	
	/**
		The port is a 2x1 square. The node.pos marks the position of the top-middle of the rectangle.
	*/
	'repositoryPort': {
	    	  'render': function(node, canvas){
					
	    	        var pos = node.pos.getc(true), 
	    	            size = node.data.$dim,
						bx = pos.x,
						by = pos.y,
	    	            ctx = canvas.getCtx();
					
					// draw close-button area
					this.nodeHelper.rectangle.render('fill', {x: bx, y: by+size/4}, size, size/2, canvas);
					
					if(canvas.showLabels){
						var bSize = size * 0.4;
						bx -= size/6;
						by += bSize;
						
						var symbol = node.data.$symbol;
						if(!symbol){ symbol = "R";}
						
						ctx.fillStyle = node.data.$fillColor;
						ctx.font = bSize +"px Arial Black";
						ctx.fillText(symbol, bx, by);
	    	  		}
					
	    	      },
	    	      'contains': function(node, pos){
				  
	    	        var npos = node.pos.getc(true), 
	    	            size = node.data.$dim / 2;
	    	        return Math.abs(pos.x - npos.x) <= size && Math.abs(pos.y - npos.y-size) <= size;
	        }
	},
	
	/**
		The port is a 2x1 square. The node.pos marks the position of the top-middle of the rectangle.
	*/
	'inputPort': {
	    	  'render': function(node, canvas){
					
	    	        var pos = node.pos.getc(true), 
	    	            size = node.data.$dim,
						bx = pos.x,
						by = pos.y,
	    	            ctx = canvas.getCtx();
					
					// draw rectangle
					this.nodeHelper.rectangle.render('fill', {x: bx, y: by+size/4}, size, size/2, canvas);
					
					
					ctx.fillStyle = node.data.$fillColor;
					var symbol = node.data.$symbol;
					
					if(symbol && canvas.showLabels){
						
						// draw symbol
						var bSize = size * 0.4;
						bx -= size/6;
						by += bSize;
						ctx.font = bSize +"px Arial Black";
						ctx.fillText(symbol, bx, by);
					}
					else{
						
						// draw arrow
						by += size/4;
						var	octo = size/8,
							xp = bx - size/2 + octo;
						ctx.beginPath();
						ctx.moveTo(xp, by - octo);
						ctx.lineTo(bx , by);
						ctx.lineTo(xp, by + octo);
						ctx.closePath();
						ctx.fill();
					}
					
	    	      },
	    	      'contains': function(node, pos){
				  
	    	        var npos = node.pos.getc(true), 
	    	            size = node.data.$dim / 2;
	    	        return Math.abs(pos.x - npos.x) <= size && Math.abs(pos.y - npos.y-size) <= size;
	        }
	},
	/**
		The port is a 2x1 square. The node.pos marks the position of the top-middle of the rectangle.
	*/
	'outputPort': {
	    	  'render': function(node, canvas){
	    	        var pos = node.pos.getc(true), 
	    	            size = node.data.$dim,
						bx = pos.x,
						by = pos.y,
	    	            ctx = canvas.getCtx();
					
					// draw rectangle
					this.nodeHelper.rectangle.render('fill', {x: bx, y: by+size/4}, size, size/2, canvas);
					
					ctx.fillStyle = node.data.$fillColor;
					var symbol = node.data.$symbol;
					
					if(symbol && canvas.showLabels){
						// draw symbol
						
						var bSize = size * 0.4;
						bx -= size/6;
						by += bSize;
						
						
						ctx.font = bSize +"px Arial Black";
						ctx.fillText(symbol, bx, by);
					}
					else{
						// draw arrow
						by += size/4;
						var	octo = size/8;
						ctx.beginPath();
						ctx.moveTo(bx, by - octo);
						ctx.lineTo(bx + size/2 - octo , by);
						ctx.lineTo(bx, by + octo);
						ctx.closePath();
						ctx.fill();
					}
										
	    	      },
	    	      'contains': function(node, pos){
				  
	    	        var npos = node.pos.getc(true), 
	    	            size = node.data.$dim / 2;
	    	        return Math.abs(pos.x - npos.x) <= size && Math.abs(pos.y - npos.y-size) <= size;
	        }
	},
	
	'grid': {
			'render': function(grid, canvas){
				if(!grid.data.$alpha){
					return;
				}
				var scale = 1/canvas.scaleOffsetX,
					centerX = - canvas.translateOffsetX*scale,
					centerY = - canvas.translateOffsetY*scale,
					size = grid.data.$dim,
					width = grid.data.$width*scale*2,
					height = grid.data.$height*scale*2,
					left = centerX - width,
					up = centerY - height,
					right = centerX + width,
					bottom = centerY + height,
					ctx = canvas.getCtx();
				
				ctx.lineWidth = scale;
				ctx.beginPath();
				// draw vertical lines
				for(var x = left - (left % size), l = centerX + width; x < l; x += size){
					ctx.moveTo(x, up);
					ctx.lineTo(x, bottom);
					
				}
				
				// draw horizontal lines
				for(var y = up - (up % size), l = centerY + height; y < l; y += size){
					ctx.moveTo(left, y);
					ctx.lineTo(right, y);
				}
				
				ctx.stroke();
			}
		}
  });
	
  
  /*
   * GraphFlow.EdgeHelper
   * 
   * provides some functions for drawing edges and checking if the mouse is within an edge
   */
  GraphFlow.EdgeHelper = {
    /*
     * Checks if pos is anywhere near an edge that may contain bend points
     */
	'contains' : function(adj, pos) {	
    	  
    	if(adj.data.$isMouseEdge){
          	return false;
          }
    	  
        var dim = adj.getData('dim'),
        	data = adj.data,
			inverse = data.$direction[0] != adj.nodeFrom.id,
			posFrom = adj.nodeFrom.pos.getc(true),
			posTo = adj.nodeTo.pos.getc(true),
			offF = data.$offsetFrom,
			offT = data.$offsetTo;
        
		var from = {"x": posFrom.x, "y" : posFrom.y};
		var to = {"x": posTo.x, "y" : posTo.y};
		
		// swap if the arrow points in inverse order
		if(inverse){
			var temp = to;
			to = from;
			from = temp;
		}
		
		// add offset
		from.x += offF.x;
		from.y += offF.y;
		to.x += offT.x;
		to.y += offT.y;
		
		// check if mouse is between bendpoints
		var bend = adj.data.$bendPoints,
			toBend;
		
		if(bend){
			for(var b = 0, l = bend.length; b < l; b++){
				toBend = bend[b];
				//if (EdgeHelper.line.contains(from, toBend, pos, dim)){
				if (GraphFlow.EdgeHelper.isOverEdge(from, toBend, pos, dim)){
					return true;
				}
				from = bend[b];
			}
			
		}
		//return EdgeHelper.line.contains(from, to, pos, dim);
		return GraphFlow.EdgeHelper.isOverEdge(from, to, pos, dim);
      },	  
		  
	'isOverEdge' : function(edgeFrom, edgeTo, checkPos, threshold) {
		var cx = checkPos.x,
			cy = checkPos.y,
			efx = edgeFrom.x,
			efy = edgeFrom.y,
			etx = edgeTo.x,
			ety = edgeTo.y;
	  
	  
		// is checkPos within line bounds ?
		{
			var temp,
				minX = efx,
				maxX = etx;
			if(minX > maxX){
				temp = maxX;
				maxX = minX;
				minX = temp;
			}
			
			temp = cx;
			if(temp < minX - threshold || temp > maxX + threshold){
				return false;
			}
			
			var minY = efy,
				maxY = ety;
			if(minY > maxY){
				temp = maxY;
				maxY = minY;
				minY = temp;
			}
		
			temp = cy;
			if(temp < minY - threshold || temp > maxY + threshold){
				return false;
			}
		}
	
		// calculate normal vector of edge
		var nx = ety - efy, 
			ny = efx - etx,
			nAbs = Math.sqrt(nx * nx + ny * ny);
		
		if(nAbs != 0){
			nx /= nAbs;
			ny /= nAbs;
		}
		
		// use Hesse-Normal-Form for calculating the distance between edge and mouse pos
		var d = (cx - efx)*nx + (cy - efy)*ny;

		// is mouse pos close to line ?
		return Math.abs(d) <= threshold ;
    },
    
    /*
     * Draws a straight edge with an arrow head
     * 
     * parameters:
     * canvas - the canvas object onto which the edge is drawn
     * from - the starting point of the edge
     * to - the end point of the edge
     * lineType - 0 : normal line
     * 			  1 : dashed line
     * 			  2 : dotted line
     * 			  3 : dashedDotted line
     * 
     * (optional)
     * headLength - the length of the arrow head
     * headWidth - the width of the arrow head
     */
    'drawEdge' : function(canvas, from, to, lineType, dim, headLength, headWidth){
    	
    	var ctx = canvas.getCtx();
    	ctx.beginPath();
    	
    	if(!lineType || lineType == 0){
	        ctx.moveTo(from.x, from.y);
	        ctx.lineTo(to.x, to.y);
    	}
    	else if(lineType == 1){
    		GraphFlow.EdgeHelper.dashedLine(ctx, from, to, dim);
    	}
    	else if(lineType == 2){
    		GraphFlow.EdgeHelper.dottedLine(ctx, from, to, dim);
    	}
    	else if(lineType == 3){
    		GraphFlow.EdgeHelper.dashedDottedLine(ctx, from, to, dim);
    	}
    	
    	ctx.stroke();
    	
        if(!headLength || !headWidth){
        	return;
        }
    	
        var vect = new Complex(to.x - from.x, to.y - from.y);
        vect.$scale(headLength / vect.norm());
        
        var intermediatePoint = new Complex(to.x - vect.x, to.y - vect.y);
        var normal = new Complex(-vect.y, vect.x);
            normal.$scale(headWidth / (2 * normal.norm()));
            
        var v1 = intermediatePoint.add(normal), 
            v2 = intermediatePoint.$add(normal.$scale(-1));
        
        ctx.beginPath();
        ctx.moveTo(v1.x, v1.y);
        ctx.lineTo(v2.x, v2.y);
        ctx.lineTo(to.x, to.y);
        ctx.closePath();
        ctx.fill();
    },
    
    /*
     * Draws a simple dashed line between two points
     * 
     * parameters:
     * ctx - the canvas context object
     * from - a {x,y} object marking the start position of the line
     * to - a {x,y} object marking the end position of the line
     * dim - the length of each dash
     */
    'dashedLine' : function(ctx, from, to, dim){
		var vx = to.x - from.x,
			vy = to.y - from.y,
			abs = Math.sqrt(vx * vx + vy * vy),
			maxSegments = 2 * abs / dim;
		
		var dx = from.x,
			dy = from.y;
		
		// set starting point of line
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(dx, dy);
		
		// only draw dashed if we have enough length
		if(maxSegments > 1){
			vx *= dim / abs;
			vy *= dim / abs;
			
			var vx2 = vx / 2,
				vy2 = vy / 2;
			
			dx += vx;
			dy += vy;
			
			// draw segments
			var segments = 2;
			while(segments < maxSegments){
				
				ctx.lineTo (dx, dy);
				dx += vx2;
				dy += vy2;
				
				ctx.moveTo(dx, dy);
				dx += vx;
				dy += vy;
				
				segments+= 3;
			}
			// always draw the last section
			ctx.moveTo(to.x - vx, to.y - vy);
		}
		ctx.lineTo (to.x, to.y);
		ctx.stroke();
		ctx.restore();
	},
	
	 /*
     * Draws a simple dotted line between two points
     * 
     * parameters:
     * ctx - the canvas context object
     * from - a {x,y} object marking the start position of the line
     * to - a {x,y} object marking the end position of the line
     * dim - the distance between the dots
     */
	'dottedLine' : function(ctx, from, to, dim){
		var vx = to.x - from.x,
			vy = to.y - from.y,
			abs = Math.sqrt(vx * vx + vy * vy),
			maxSegments = (abs / dim) - 1;
		
		var dx = from.x,
			dy = from.y;
		
		var xDot = vx / abs,
			yDot = vy / abs;
			
		vx *= dim / abs;
		vy *= dim / abs;
		
		dx += vx;
		dy += vy;
		
		ctx.save();
		ctx.beginPath();
		
		// draw segments
		var segments = 0;
		while(segments < maxSegments){
			ctx.moveTo(dx, dy);
			ctx.lineTo (dx + xDot, dy + yDot);
			
			dx += vx;
			dy += vy;
			segments++;
		}
		ctx.stroke();
		ctx.restore();
	},
	
	 /*
     * Draws a line between two points, alternating between a dot and
     * a dash
     * 
     * parameters:
     * ctx - the canvas context object
     * from - a {x,y} object marking the start position of the line
     * to - a {x,y} object marking the end position of the line
     * dim - the length of each dash
     */
	'dashedDottedLine' : function(ctx, from, to, dim){
		var vx = to.x - from.x,
			vy = to.y - from.y,
			abs = Math.sqrt(vx * vx + vy * vy),
			maxSegments = (abs / ( 2* dim)) - 1;
		
		var dx = from.x,
			dy = from.y;
		
		// set starting point of line
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(dx, dy);
		
		// only draw dashed if we have enough length
		if(maxSegments > 1){
			var xDot = vx / abs,
				yDot = vy / abs;
				
			vx *= dim / abs;
			vy *= dim / abs;
			
			var vx2 = vx / 2,
				vy2 = vy / 2;
			
			dx += vx;
			dy += vy;
			
			// draw segments
			var segments = 0;
			while(segments < maxSegments){
				
				ctx.lineTo (dx, dy);
				dx += vx2;
				dy += vy2;
				
				ctx.moveTo(dx, dy);
				ctx.lineTo(dx + xDot, dy + yDot);
				dx += vx2;
				dy += vy2;
				
				ctx.moveTo(dx, dy);
				dx += vx;
				dy += vy;
				
				segments++;
			}
			// always draw the last section
			ctx.moveTo(to.x - vx, to.y - vy);
		}
		ctx.lineTo (to.x, to.y);
		ctx.stroke();
		ctx.restore();
	}
  }
  
  /*
    Class: GraphFlow.Plot.EdgeTypes
  
    This class contains a list of <Graph.Adjacence> built-in types. 
    Edge types implemented are 'none', and 'flowarrow'.
  
    You can add your custom edge types, customizing your visualization to the extreme.
  
    Example:
  
    (start code js)
      GraphFlow.Plot.EdgeTypes.implement({
        'mySpecialType': {
          'render': function(adj, canvas) {
            //print your custom edge to canvas
          },
          //optional
          'contains': function(adj, pos) {
            //return true if pos is inside the arc or false otherwise
          }
        }
      });
    (end code)
  
  */
  GraphFlow.Plot.EdgeTypes = new Class({
	  
    'none': $.empty,

	'flowArrow': {
      'render': function(adj, canvas) {
        var data = adj.data,
        	dim = adj.Edge.dim,
            inverse = data.$direction[0] != adj.nodeFrom.id,
			posFrom = adj.nodeFrom.pos.getc(true),
			posTo = adj.nodeTo.pos.getc(true),
			offF = data.$offsetFrom,
			offT = data.$offsetTo;

		var from = {"x": posFrom.x, "y" : posFrom.y};
		var to = {"x": posTo.x, "y" : posTo.y};
		
		// swap points if the edge direction is "wrong"
		if(inverse){
			var temp = to;
			to = from;
			from = temp;
		}
		
		// add offset
		from.x += offF.x;
		from.y += offF.y;
		to.x += offT.x;
		to.y += offT.y;
		
		// draw potential bend points
		var bend = data.$bendPoints,
			toBend;
		if(bend){
			for(var b = 0, l = bend.length; b < l; b++){
				toBend = bend[b];
				this.edgeHelper.line.render(from, toBend, canvas);
				from = bend[b];
			}
		}
		
		// draw the last line
		GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 0, dim, dim, dim /2);
		
		// add the label
		var label = adj.data.$label;
		
		if(canvas.showLabels && label){
			var tX = (from.x + to.x) / 2;
			
			ctx.font = (1.23 * dim)+"px Arial";
			var midX,
				midY =  (from.y + to.y -dim) / 2; 
			
			for(var t = label.length-1; t >= 0; t--){
				midX = tX - ctx.measureText(label[t]).width/2;
				
				ctx.fillText(label[t], midX, midY);
				midY -= dim;
			}
		}
		
		/*
		var label = adj.data.$label;
		if(label != undefined && canvas.showLabels){
			ctx.font = (1.23 * dim)+"px Arial";
				
			var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
				midY = (from.y + to.y -dim) / 2;
			
			ctx.fillText(label, midX, midY);
		}*/
		
      },
      'contains': GraphFlow.EdgeHelper.contains
    }
  });

})($jit.GraphFlow);

})();