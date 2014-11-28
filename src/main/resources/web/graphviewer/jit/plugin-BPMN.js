

/**
 *	An object containing functions and variables used for drawing circles.
 */
var CircleHelper = {
		'fullCirc' : Math.PI * 2,
		'halfCirc' : Math.PI,
		'quarterCirc' : Math.PI / 2,
		'threeFourthsCirc' : Math.PI * 1.5,
	
		'dashedStep' : Math.PI / 4.5,
		'dashedSpace' : Math.PI / 3,
		
		/**
		 * Draws a dashed circle with 6 dashes. Mainly used for Events.
		 * 
		 *	Parameters:
		 *   ctx - the canvas.context object
		 *   x - horizontal position of the circle center
		 *   y - vertical position of the circle center
		 *   radius - the radius of the circle
		 *   displaced - if true, the starting angle is displaced a bit
		 */
		'drawDashedCircle' : function(ctx, x, y, radius, displaced){
			var step = CircleHelper.dashedStep;
			var angleStep = CircleHelper.dashedSpace;
			var startingAngle = (displaced) ? angleStep/2 : 0;
			
			for(var i = 0; i < 6; i++){
				ctx.beginPath();
				ctx.arc(x, y, radius, startingAngle, startingAngle + step);
				startingAngle += angleStep;
				ctx.stroke();
			}
		}
}

/**
 * An object that provides helper-functions for drawing BPMN symbols that appear on Events, Gateways, Tasks, etc.
 */
var SymbolHelper = {
	
	/**
	 * The color, used to fill up symbols
	 */
	'symbolColor' : "#FFFFFF",
	
	/**
	 * Draws a symbol on the canvas.
	 * 
	 *	Parameters:
	 *   symbol - the name of the symbol
	 *   ctx - the canvas context
	 *   x - the upper left corner of the symbol
	 *   y - the upper left corner of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   negative - if true, the symbol will be drawn filled out
	 */
	'drawSymbol' : function (nodeType, symbol, ctx, x, y, width, height, negative){
		
		var fun = SymbolHelper[symbol];
		if(fun){
			
			// fix some weird dimensions
			switch(nodeType){
				case "bpmn_gateway" :
					negative = true;
					break;
				case "bpmn_eventgateway" :
				case "bpmn_startevent" :
				case "bpmn_intermediateevent" :
				case "bpmn_endevent" :
					if(symbol == "message"){
						height = width * 0.75;
					}
					break;
			}
			
			fun(ctx, x, y, width, height, negative);
		}
	},
	
	'drawLabel' : function(label, dim, ctx, color, font, x, y){
		
		ctx.font = (0.83 * dim) + font;
		ctx.fillStyle = color;
		
		var midX, labelWidth, labelHeight;
		for(var t = 0, l = label.length; t < l; t++){
			labelWidth = ctx.measureText(label[t]).width/2;
			midX = x - labelWidth;
			
			ctx.fillText(label[t], midX, y);
			y += dim;
		}
	},
	
	/**
	 * Helper Function that draws the Escalation-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'escalation'  : function (ctx, x, y, width, height, black){

		var yTop =  y - height / 2,
			yBot =  y + (height * 0.3),
			hw = width * 0.4;
		
		ctx.save();
		ctx.beginPath();
		
		ctx.moveTo(x, y);
		ctx.lineTo(x + hw, yBot);
		ctx.lineTo(x , yTop);
		ctx.lineTo(x - hw, yBot);
		
		ctx.closePath();
		
		if(black){
			ctx.fill();
		}
		else{
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Error-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - middle of the symbol
	 *   y - middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'error'  : function (ctx, x, y, width, height, black){
		
		var hh = height * 0.4,
			yTop = y - hh,
			yBot = y + hh,
			w1 = width * 0.15,
			w2 = width / 2
			xLeft = x - w1,
			xRight = x + w1;
		
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(x - w2, yBot);
		ctx.lineTo(xLeft, yTop);
		ctx.lineTo(xRight, y);
		ctx.lineTo(x + w2, yTop);
		ctx.lineTo(xRight, yBot);
		ctx.lineTo(xLeft, y);
		ctx.closePath();
		/*
		height *= 0.8;
		y -= height / 2;
		
		var w = width / 4,
			x1 = x + w,
			x2 = x1 + w
			yh = y + height,
			yh2 = y + height / 2;
		
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(x, yh);
		ctx.lineTo(x1, y);
		ctx.lineTo(x2, yh2);
		ctx.lineTo(x2 + w, y);
		ctx.lineTo(x2, yh);
		ctx.lineTo(x1, yh2);
		ctx.closePath();
		*/
		if(black){
			ctx.fill();
		}
		else{ 
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Compensation-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'compensation'  : function (ctx, x, y, width, height, black){
		
		var hh = height * 0.4,
			hw = width / 2;
		
		x -= hw / 5;
		
		var xLeft = x - hw,
			xRight = x + hw,
			yBot = y + hh,
			yTop = y - hh;
		
		ctx.save();
		ctx.beginPath();
		
		ctx.moveTo(xLeft, y);
		ctx.lineTo(x, yTop);
		ctx.lineTo(x, yBot)
		ctx.closePath();
		
		ctx.moveTo(x, y);
		ctx.lineTo(xRight, yTop);
		ctx.lineTo(xRight, yBot);
		ctx.closePath();
		
		if(black){
			ctx.fill();
		}
		else{ 
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Abort-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'abort'  : function (ctx, x, y, width, height, black){
		
		var origLW = ctx.lineWidth;
		
		var w = width / ((black) ? 4 : 3),
			hh = height /2,
			hw = width / 2;
		
		// draws a cross
		var crossFunc = function(){
			ctx.lineWidth = w;
			ctx.beginPath();
			
			ctx.moveTo(x - hw, y - hh)
			ctx.lineTo(x + hw, y + hh);
			ctx.closePath();
			
			ctx.moveTo(x - hw, y + hh)
			ctx.lineTo(x + hw, y - hh);
			ctx.closePath();
			
			ctx.stroke();
		};
		
		// draw black plus
		ctx.save();
		crossFunc();
		
		// draw filling
		if(!black){
			ctx.strokeStyle = SymbolHelper.symbolColor;
			w -= 2 * origLW;
			// the following line would be  more correct:
			// origLW = Math.sqrt(origLW * origLW * 2);
			hh -= origLW;
			hw -= origLW;
			crossFunc();
		}
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Conditional-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'conditional'  : function (ctx, x, y, width, height, black){

		var hw = width * 0.35,
			hh = height / 2;
		
		var xLeft = x - hw,
			xRight = x + hw,
			yTop = y - hh,
			yBot = y + hh;
		
		// draw outer path
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(xLeft, yTop);
		ctx.lineTo(xRight, yTop);
		ctx.lineTo(xRight, yBot);
		ctx.lineTo(xLeft, yBot);
		ctx.closePath();
		
		ctx.fillStyle = SymbolHelper.symbolColor;
		ctx.fill();
		
		// draw inner lines
		hh *= 0.8;
		yTop = y - hh;
		yBot = y + hh;
		
		ctx.moveTo(xLeft, yTop);
		ctx.lineTo(xRight, yTop);
		ctx.moveTo(xLeft, yBot);
		ctx.lineTo(xRight, yBot);
		
		hh = height * 0.15;
		yTop = y - hh;
		yBot = y + hh;
		
		ctx.moveTo(xLeft, yTop);
		ctx.lineTo(xRight, yTop);
		ctx.moveTo(xLeft, yBot);
		ctx.lineTo(xRight, yBot);
		
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Parallel-Gateway/Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'parallel' : function (ctx, x, y, width, height, black){

		var origLW = ctx.lineWidth;
		
		var w = width / ((black) ? 4 : 3),
			hh = height /2,
			hw = width / 2;
		
		// draws a plus
		var plusFunc = function(){
			ctx.lineWidth = w;
			ctx.beginPath();
			
			ctx.moveTo(x, y - hh)
			ctx.lineTo(x, y + hh);
			ctx.closePath();
			
			ctx.moveTo(x - hw, y)
			ctx.lineTo(x + hw, y);
			ctx.closePath();
			
			ctx.stroke();
		};
		
		// draw black plus
		ctx.save();
		plusFunc();
		
		// draw filling
		if(!black){
			ctx.strokeStyle = SymbolHelper.symbolColor;
			w -= 2 * origLW;
			hh -= origLW;
			hw -= origLW;
			plusFunc();
		}
		ctx.restore();
	},
	
	'parallel multiple' : function(ctx, x, y, width, height, black){
		SymbolHelper.parallel(ctx, x, y, width, height, black);
	},
	
	/**
	 * Helper Function that draws the Complex-Gateway Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   dim - the width and height of the symbol
	 *   height - a placeholder for using the drawSymbol function correctly
	 */
	'complex'  : function (ctx, x, y, width, height){
		
		width *= 1.25;
		height *= 1.25;
		
		var circW = width * 0.1458,
			circH = height * 0.1458;
		
		var w = width / 6,
			hh = height /2,
			hw = width / 2;
		
		ctx.save();
		ctx.lineWidth = w;
		ctx.beginPath();
		
		// draw plus
		ctx.moveTo(x, y - hh)
		ctx.lineTo(x, y + hh);
		ctx.closePath();
		
		ctx.moveTo(x - hw, y)
		ctx.lineTo(x + hw, y);
		ctx.closePath();
		
		// draw cross
		hh -= circH;
		hw -= circW;
		
		ctx.moveTo(x - hw, y - hh)
		ctx.lineTo(x + hw, y + hh);
		ctx.closePath();
		
		ctx.moveTo(x - hw, y + hh)
		ctx.lineTo(x + hw, y - hh);
		ctx.closePath();
		
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Exclusive-Gateway Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'exclusive'  : function (ctx, x, y, width, height, black){
		SymbolHelper.abort(ctx, x, y, width * 0.75, height, black);
	},
	
	/**
	 * Helper Function that draws the Inclusive-Gateway Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the upper left corner of the symbol
	 *   y - the upper left corner of the symbol
	 *   dim - the width and height of the symbol
	 */
	'inclusive'  : function (ctx, x, y, dim){
		var lw = ctx.lineWidth;//dim / 8;
		
		var r = dim / 2 - lw / 2,
			l = r / 2,
			s = l / 4;
		
		// draw plus
		ctx.save();
		ctx.beginPath();
		ctx.arc(x, y, r, 0, CircleHelper.fullCirc, true);
		
		var stroke = ctx.fillStyle;
		ctx.fillStyle = SymbolHelper.symbolColor;
		ctx.fill();
		
		//ctx.lineWidth = lw;
		ctx.fillStyle = stroke;
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Timer-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   dim - the width and height of the symbol
	 */
	'timer'  : function (ctx, x, y, dim){
		
		var r = dim / 2,
			l = r / 2,
			s = l / 4;
		
		// draw circle
		ctx.save();
		ctx.beginPath();
		ctx.arc(x, y, r, 0, CircleHelper.fullCirc, true);
		
		var stroke = ctx.fillStyle;
		ctx.fillStyle = SymbolHelper.symbolColor;
		ctx.fill();
		ctx.fillStyle = stroke;
		
		y -= r;
		var y2 = y + dim;
		ctx.moveTo(x, y);
		ctx.lineTo(x, y2);
		x -= r;
		var x2 = x + dim;
		ctx.moveTo(x, y + r);
		ctx.lineTo(x2, y + r);
		
		ctx.moveTo(x + l, y + s);
		ctx.lineTo(x2 - l, y2 - s);
		ctx.moveTo(x + s, y + l);
		ctx.lineTo(x2 - s, y2 - l);
		
		ctx.moveTo(x + l, y2 - s);
		ctx.lineTo(x2 - l, y + s);
		ctx.moveTo(x + s, y2 - l);
		ctx.lineTo(x2 - s, y + l);
		ctx.stroke();
		
		ctx.beginPath();
		x += r;
		y += r;
		
		ctx.arc(x, y, r * 0.8, 0, CircleHelper.fullCirc, true);
		ctx.fillStyle = SymbolHelper.symbolColor;
		ctx.fill();
		ctx.fillStyle = stroke;
		
		ctx.beginPath();
		ctx.moveTo(x + r * 0.7, y);
		ctx.lineTo(x, y);
		ctx.lineTo(x, y - r * 0.7);
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Terminate-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   dim - the width and height of the symbol
	 */
	'terminate'  : function (ctx, x, y, dim){
		var r = dim * 0.6;
		
		// draw circle
		ctx.save();
		ctx.beginPath();
		ctx.arc(x, y, r, 0, CircleHelper.fullCirc, true);
		ctx.fill();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Signal-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'signal'  : function (ctx, x, y, width, height, black){
		
		var wh = width / 2;
		height *= 0.8;
		y += (height * 3) / 8;
		
		// draw triangle
		ctx.save();
		ctx.beginPath();
		
		ctx.moveTo(x - wh, y);
		ctx.lineTo(x + wh, y);
		ctx.lineTo(x, y - height);
		
		ctx.closePath();
		
		// fill triangle
		if(black){
			ctx.fill();
		}else{
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Multiple-Event Symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the middle of the symbol
	 *   y - the middle of the symbol
	 *   dim - the width and height of the symbol
	 *   height - a placeholder for using the drawSymbol function correctly
	 *   black - if true, fills the symbol with stroke color
	 */
	'multiple'  : function (ctx, x, y, dim, height, black){
		x -= dim / 2;
		y -= height / 2;
		
		var d = dim / 2,
			h = dim / 3
			w = h / 2;
		
		var yh = y + h,
			xdim = x + dim;
		
		// draw polygon
		ctx.save();
		ctx.beginPath();
		
		ctx.moveTo(x + d, y);
		ctx.lineTo(xdim, yh);
		y += dim - w / 2;
		ctx.lineTo(xdim - w, y);
		ctx.lineTo(x + w, y);
		ctx.lineTo(x, yh);
		ctx.closePath();
		
		if(black){
			ctx.fill();
		}
		else{
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws an envelope symbol.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the upper left corner of the symbol
	 *   y - the upper left corner of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'message'  : function (ctx, x, y, width, height, black){
		x -= width / 2;
		y -= height / 2;
		
		ctx.save();
		ctx.beginPath();
		ctx.moveTo(x, y);
		
		// fill symbol
		if(black){
			var xw = x + width,
				xw2 = x + width/2,
				yh = y + height;
				
			ctx.lineTo(xw, y);
			ctx.lineTo(xw2, y + height / 2);
			ctx.closePath();
			
			var l = ctx.lineWidth;
			
			ctx.moveTo(x, yh);
			ctx.lineTo(xw, yh);
			y += l;
			ctx.lineTo(xw , y);
			ctx.lineTo(xw2, y + height / 2);
			ctx.lineTo(x, y);
			ctx.closePath();
			
			ctx.fillStyle = ctx.strokeStyle;
			ctx.fill();
		}
		else{
			var stroke = ctx.strokeStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.strokeStyle = stroke;
			ctx.fillRect(x, y, width, height);
			ctx.strokeRect(x, y, width, height);
			
			// draw fold
			ctx.fillStyle = stroke;
			ctx.lineTo(x + width / 2, y + height / 2);
			ctx.lineTo(x + width, y);
			ctx.stroke();
		}		
		
		ctx.restore();
		
	},
	
	/**
	 * Helper Function that draws the an arrow.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the upper left corner of the symbol
	 *   y - the upper left corner of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'arrow' : function(ctx, x, y, width, height, black){
		
		var w = (5 * width) / 8,
			h = height / 3;
	
		var xw = x + w,
			yh = y + h,
			yh2 = y + h + h;
		
		ctx.save();
		ctx.beginPath();
		
		ctx.moveTo(x, yh2);
		ctx.lineTo(x, yh);
		ctx.lineTo(xw, yh);
		ctx.lineTo(xw, y);
		ctx.lineTo(x + width, y + height / 2)
		ctx.lineTo(xw, y + height);
		ctx.lineTo(xw, yh2);
		ctx.closePath();
		
		if(black){
			ctx.fill();
		}
		else{ 
			var stroke = ctx.fillStyle;
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.fillStyle = stroke;
		}
		ctx.stroke();
		
		ctx.restore();
	},
	
	/**
	 * Helper Function that draws the Link-Event.
	 *
	 *	Parameters:
	 *   ctx - the canvas context
	 *   x - the upper left corner of the symbol
	 *   y - the upper left corner of the symbol
	 *   width - the width of the symbol
	 *   height - the height of the symbol
	 *   black - if true, fills the symbol with stroke color
	 */
	'link' : function(ctx, x, y, width, height, black){
		x -= width / 2;
		y -= height / 2;
		SymbolHelper.arrow(ctx, x, y, width, height, black);
	},
	
	'task' : {
		
		/**
		 * Draws a task symbol on the canvas.
		 *
		 *	Parameters:
		 *   symbol - the name of the symbol
		 *   ctx - the canvas context
		 *   x - the upper left corner of the symbol
		 *   y - the upper left corner of the symbol
		 *   dim - the width of the symbol
		 */
		'drawSymbol' : function (symbol, ctx, x, y, dim){
			
			var fun = SymbolHelper.task[symbol];
			if(fun){
				fun(ctx, x, y, dim);
			}
		},
		
		/**
		 * Helper Function that draws the Sub-Process plus symbol at the bottom of
		 * an activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'subprocess'  : function (ctx, x, y, dim){
			var hw = dim / 2,
				hh = dim / 2;
			
			var xLeft = x - hw,
				xRight = x + hw,
				yTop = y - dim,
				yBot = y ;
			
			
			// draw outer path
			ctx.save();
			ctx.beginPath();
			
			ctx.moveTo(xLeft, yTop);
			ctx.lineTo(xRight, yTop);
			ctx.lineTo(xRight, yBot);
			ctx.lineTo(xLeft, yBot);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			
			// draw inner plus
			y -= hh;
			hw = dim * 0.3,
			hh = dim * 0.3;
			
			ctx.moveTo(x - hw, y);
			ctx.lineTo(x + hw, y);
			ctx.moveTo(x, y - hh);
			ctx.lineTo(x, y + hh);
			
			ctx.lineWidth = dim / 10;
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the Sub-Process minus symbol at the bottom of
		 * an activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'subprocess_hidden'  : function (ctx, x, y, dim){
			var hw = dim / 2,
				hh = dim / 2;
			
			var xLeft = x - hw,
				xRight = x + hw,
				yTop = y - dim,
				yBot = y ;
			
			
			// draw outer path
			ctx.save();
			ctx.beginPath();
			
			ctx.moveTo(xLeft, yTop);
			ctx.lineTo(xRight, yTop);
			ctx.lineTo(xRight, yBot);
			ctx.lineTo(xLeft, yBot);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			
			// draw inner plus
			y -= hh;
			hw = dim * 0.3,
			hh = dim * 0.3;
			
			ctx.moveTo(x - hw, y);
			ctx.lineTo(x + hw, y);
			
			ctx.lineWidth = dim / 10;
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the Parallel-Execution Symbol.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the upper left corner of the symbol
		 *   y - the upper left corner of the symbol
		 *   dim - the width of the symbol
		 */
		'parallel'  : function (ctx, x, y, dim){
			
			var w = dim / 4;
			
			var yTop = y - dim,
				yBot = y,
				dx = x - w * 1.5;
			
			ctx.save();
			ctx.beginPath();
			ctx.lineWidth = w;
			w += w / 2;
			
			ctx.moveTo(dx, yTop);
			ctx.lineTo(dx, yBot);
			dx += w;
			ctx.moveTo(dx, yTop);
			ctx.lineTo(dx, yBot);
			dx += w;
			ctx.moveTo(dx, yTop);
			ctx.lineTo(dx, yBot);
			
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the standard loop symbol at the bottom of an 
		 * activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'standard'  : function (ctx, x, y, dim){

			var radius = dim / 2,
				lw = radius / 5;
			y -= radius
			
			// draw circle
			var qc =  CircleHelper.quarterCirc,
				qch =  qc / 4;
			ctx.save();
			ctx.lineWidth = lw;
			ctx.beginPath();
			
			ctx.arc(x, y, radius, qc - qch, qc + qch, true)
			ctx.stroke();
			
			// draw arrow head
			ctx.beginPath();
			var al = radius * 0.5,
				dx = x - radius * 0.55 - al + lw,
				dy = y + radius * 0.9 + lw / 2;
			ctx.moveTo(dx, dy);
			dx += al;
			ctx.lineTo(dx, dy);
			dy -= al;
			ctx.lineTo(dx, dy);
			
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the sequential multi execution symbol 
		 * at the bottom of an activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   width - the width of the symbol
		 */
		'sequential'  : function (ctx, x, y, dim){
			var w = dim / 4;
			
			var xLeft = x - dim / 2,
				xRight = x + dim / 2;
				dy = y - w / 2;
			
			ctx.save();
			ctx.beginPath();
			ctx.lineWidth = w;
			w += w/2;
			
			ctx.moveTo(xLeft, dy);
			ctx.lineTo(xRight, dy);
			dy -= w;
			ctx.moveTo(xLeft, dy);
			ctx.lineTo(xRight, dy);
			dy -= w;
			ctx.moveTo(xLeft, dy);
			ctx.lineTo(xRight, dy);
			
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the ad-hoc symbol at the bottom of an activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'ad-hoc'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				qw = hw / 2;
			
			var lw = dim / 5;
			var dy = y - hw;
			
			ctx.save();
			ctx.beginPath();
			ctx.lineWidth = lw;
			lw /= 2;
			
			ctx.moveTo(x - hw, dy + lw);
			ctx.quadraticCurveTo(x - qw, dy - qw, x, dy);
			ctx.quadraticCurveTo(x + qw, dy + qw, x + hw, dy - lw);
			
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the compensation symbol at the bottom of an activity.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'compensation'  : function (ctx, x, y, dim){
			ctx.save();
			ctx.lineWidth = dim / 10;
			SymbolHelper.compensation(ctx, x, y - dim / 2, dim, dim * 0.7, false);
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the user symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'user'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				qw = hw / 2,
				lw = dim / 10;
			
			var xMid = x + hw,
				xRight = x + dim,
				yBot = y + dim,
				dyBot = yBot - qw
				yMid = y + hw;
			
			// draw body
			ctx.save();
			ctx.beginPath();
			ctx.moveTo(x, yBot);
			ctx.lineTo(xRight, yBot);
			ctx.lineTo(xRight, dyBot);
			ctx.quadraticCurveTo(xRight, yMid, xMid, yMid - lw );
			ctx.quadraticCurveTo(x, yMid, x, dyBot);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			
			// draw arms
			var dx = x + qw;
			ctx.moveTo(dx, yBot);
			ctx.lineTo(dx, dyBot);
			
			dx += hw;
			ctx.moveTo(dx, yBot);
			ctx.lineTo(dx, dyBot);
			
			ctx.lineWidth = lw;
			ctx.stroke();
			
			// draw head
			ctx.beginPath();
			ctx.arc(xMid, yMid - hw * 0.4, qw, 0, CircleHelper.fullCirc);
			ctx.fill();
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the manual symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'manual'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				sStep = hw / 10,
				mStep = sStep + sStep;
			
			var xMid = x + hw,
				xRight = x + dim,
				yMid = y + hw;
			
			var sleeveRight = x + hw * 0.3,
				sleeveTop = yMid - mStep - sStep,
				sleeveBot = yMid + hw / 2;
			
			// draw sleeve
			ctx.save();
			ctx.beginPath();
			
			ctx.moveTo(x, sleeveTop);
			ctx.lineTo(x, sleeveBot);
			ctx.lineTo(sleeveRight, sleeveBot);
			ctx.lineTo(sleeveRight, sleeveTop);
			ctx.closePath();
			
			// draw hand
			var top = sleeveTop - mStep,
				left = xMid + mStep,
				right = left;
			ctx.moveTo(sleeveRight, sleeveTop)
			ctx.quadraticCurveTo(sleeveRight + sStep, top, sleeveRight + sStep + mStep, top);
			ctx.quadraticCurveTo(right, top, right, sleeveTop - sStep);
			
			top += mStep;
			right = xRight - mStep;
			ctx.quadraticCurveTo(xMid + mStep, top, xMid, top);
			ctx.lineTo(xMid - mStep - sStep, top);
			ctx.lineTo(right - mStep, top);
			ctx.quadraticCurveTo(right, top, right, top + sStep);
			
			top += mStep;
			ctx.quadraticCurveTo(right, top, right - mStep, top);
			ctx.lineTo(left, top);
			ctx.lineTo(right, top);
			ctx.quadraticCurveTo(xRight, top, xRight, top + sStep);
			
			top += mStep;
			ctx.quadraticCurveTo(xRight, top, right, top);
			ctx.lineTo(left, top);
			right += sStep;
			ctx.lineTo(right - mStep, top);
			ctx.quadraticCurveTo(right, top, right, top + sStep);
			
			top += mStep;
			ctx.quadraticCurveTo(right, top, right - mStep, top);
			ctx.lineTo(xMid + sStep, top);
			
			right -= (mStep + sStep);
			ctx.lineTo(right - mStep, top);
			ctx.quadraticCurveTo(right, top, right, top + sStep);
			
			ctx.quadraticCurveTo(right, sleeveBot, right - mStep, sleeveBot);
			ctx.lineTo(sleeveRight, sleeveBot);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			ctx.lineWidth = sStep * 1.5;
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the service symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 *   black - if true, fills the symbol with stroke color
		 */
		'service'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				step = hw * 0.3;
			
			x += hw;
			y += hw;
			
			var cogWidth = dim / 10,
				cogRadius = dim / 5,
				cogDist = cogRadius,
				cogX = x + step - cogDist,
				cogY = y + step - cogDist,
				sStep = cogRadius / 2;
			
			// draw small cog
			{
				ctx.save();
				ctx.beginPath();
				ctx.arc(cogX, cogY, cogRadius, CircleHelper.halfCirc - CircleHelper.quarterCirc / 3, 
						CircleHelper.threeFourthsCirc + CircleHelper.quarterCirc / 3);
				
				var xLeft = x - cogRadius - cogDist,
					yTop = y - cogRadius - cogDist;
				
				// draw orthogonal lines
				ctx.moveTo(xLeft, cogY);
				ctx.lineTo(xLeft + step, cogY);
				ctx.moveTo(cogX, yTop);
				ctx.lineTo(cogX, yTop + step);
		
				// draw skewed lines
				xLeft += sStep,
				yTop += sStep,
				
				ctx.moveTo(xLeft, yTop);
				ctx.lineTo(xLeft + sStep, yTop + sStep);
				
				ctx.lineWidth = cogWidth;
				ctx.stroke();
			}
			
			// draw big cog
			{
				cogX += cogDist;
				cogY += cogDist;
				
				ctx.beginPath();
				ctx.arc(cogX, cogY, cogRadius, 0, CircleHelper.fullCirc);
				
				ctx.fillStyle = SymbolHelper.symbolColor;
				ctx.fill();
				
				var xLeft = x - cogRadius,
					xRight = x + hw,
					yTop = y - cogRadius,
					yBot = y + hw;
				
				// draw orthogonal lines
				ctx.moveTo(xLeft, cogY);
				ctx.lineTo(xLeft + step, cogY);
				ctx.moveTo(xRight, cogY);
				ctx.lineTo(xRight - step, cogY);
				ctx.moveTo(cogX, yTop);
				ctx.lineTo(cogX, yTop + step);
				ctx.moveTo(cogX, yBot);
				ctx.lineTo(cogX, yBot - step);
		
				// draw skewed lines
				xLeft += sStep,
				xRight -= sStep,
				yTop += sStep,
				yBot -= sStep;
				
				ctx.moveTo(xLeft, yTop);
				ctx.lineTo(xLeft + sStep, yTop + sStep);
				ctx.moveTo(xRight, yTop);
				ctx.lineTo(xRight - sStep,  yTop + sStep);
				ctx.moveTo(xLeft, yBot);
				ctx.lineTo(xLeft + sStep, yBot - sStep);
				ctx.moveTo(xRight, yBot);
				ctx.lineTo(xRight - sStep, yBot - sStep);
				
				ctx.stroke();
				ctx.restore();
			}
		},
		
		/**
		 * Helper Function that draws the script symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'script'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				step = hw * 0.2,
				sStep = hw / 10,
				mStep = hw / 5;
			
			var xLeft = x + step,
				xRight = x + dim - step,
				dxLeft = xLeft + mStep,
				dxRight = xRight - mStep,
				yBot = y + dim,
				dyTop = y + hw / 2,
				dyBot = yBot - hw / 2,
				yMid = y + hw;
			
			// draw outer path
			ctx.save();
			ctx.beginPath();
			
			ctx.moveTo(xRight, y);
			ctx.quadraticCurveTo(dxRight, y + mStep , dxRight, dyTop);
			ctx.quadraticCurveTo(dxRight, yMid - mStep , dxRight + sStep, yMid);
			
			ctx.quadraticCurveTo(xRight, yMid + mStep , xRight, dyBot);
			ctx.quadraticCurveTo(xRight, yBot - mStep , dxRight, yBot);
			ctx.lineTo(xLeft, yBot);
			
			ctx.quadraticCurveTo(dxLeft, yBot - mStep , dxLeft, dyBot);
			ctx.quadraticCurveTo(dxLeft, yMid + mStep , xLeft + sStep, yMid);
			
			ctx.quadraticCurveTo(xLeft, yMid - mStep , xLeft, dyTop);
			ctx.quadraticCurveTo(xLeft, y + mStep , dxLeft, y);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			
			// draw inner lines
			dxRight -= mStep;
			ctx.moveTo(dxLeft, dyTop);
			ctx.lineTo(dxRight, dyTop);
			
			dxLeft += sStep;
			dxRight += sStep;
			ctx.moveTo(dxLeft, yMid);
			ctx.lineTo(dxRight, yMid);
			
			dxLeft += sStep;
			dxRight += sStep;
			ctx.moveTo(dxLeft, dyBot);
			ctx.lineTo(dxRight, dyBot);
			
			ctx.lineWidth = mStep;
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the business rule symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'business rule'  : function (ctx, x, y, dim){

			var hw = dim / 2,
				step = hw * 0.3;
			
			var yBot = y + dim - step,
				yTop = y + step ;
			var xRight = x + dim;
			
			// draw outer path
			ctx.save();
			ctx.beginPath();
			ctx.moveTo(x, yTop);
			ctx.lineTo(xRight, yTop);
			ctx.lineTo(xRight, yBot);
			ctx.lineTo(x, yBot);
			ctx.closePath();
			
			ctx.fillStyle = SymbolHelper.symbolColor;
			ctx.fill();
			
			// draw inner lines
			var qw = hw / 2;
			yBot -= qw;
			yTop = y + hw - step;
			ctx.moveTo(x, yTop );
			ctx.lineTo(xRight, yTop);
			ctx.moveTo(x, yBot);
			ctx.lineTo(xRight, yBot);
			
			x += qw;
			ctx.moveTo(x, yTop);
			ctx.lineTo(x, yBot + qw);
			
			ctx.lineWidth = dim / 10;
			ctx.stroke();
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the send symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'send'  : function (ctx, x, y, dim){
			var hw = dim / 2,
				height = dim * 0.6;
			
			ctx.save();
			ctx.lineWidth = dim / 10;
			SymbolHelper.message(ctx, x + hw, y + hw, dim, height, true);
			ctx.restore();
		},
		
		/**
		 * Helper Function that draws the send symbol at the top left of a task.
		 *
		 *	Parameters:
		 *   ctx - the canvas context
		 *   x - the middle of the symbol
		 *   y - the middle of the symbol
		 *   dim - the width of the symbol
		 */
		'receive'  : function (ctx, x, y, dim){
			var hw = dim / 2,
				height = dim * 0.6;
			ctx.save();
			ctx.lineWidth = dim / 10;
			SymbolHelper.message(ctx, x + hw, y + hw, dim, height, false);
			ctx.restore();
		}
	}
	
}

/**
 *	Parameters:
 *  nodeID - the unique identifier of the node of which the descendants
 *				 are requested
 *
 *	Return:
 *  all descendants of a single node as an array
 */
function getDescendants(nodeID){
	var subTree = [];
	
	// recursive search function
	var addSubTree = function(from){
		// remove cycles and recurring nodes
		for(var p in subTree){
			if(subTree[p].id == from){
				return;
			}
		}
		var n = graph.getNode(from);
		subTree.push(n);
		
		var adja = n.adjacencies;
		if(adja){
			for(var a = 0, l = adja.length; a < l; a++){
				addSubTree(adja[a].nodeTo);
			}
		}
	}; 
	
	// start recursion with root node
	addSubTree(nodeID);
	
	return subTree;
}


/**
 *	After a GraphFlow() object has been constructed, calling this function
 * adds a .BPMN object to the graph. This object provides functions for
 * drawing BPMN elements.
 *
 * Parameters:
 *	 graph - the GraphFlow object that will receive BPMN functionality
 */
function importBPMN(graph){
	
	/**
	 * Calculates edge offset, by connecting the edge left or right of the node. 
	 * The right side is reserved as output, while the left side is reserved as an input.
	 */
	var baseOffset = function(sourcePort, targetPort, isSource, edgeData){
		var offset = {'x':0, 'y':0};
		var bendPoints = edgeData.$bendPoints;
		var bpLast = (bendPoints) ? bendPoints[(isSource) ? 0 : bendPoints.length-1] : false;
		
		var sx = sourcePort.pos.x,
			sy = sourcePort.pos.y;
		
		var tx = targetPort.pos.x,
			ty = targetPort.pos.y;
		
		var height, width;
		
		// connect up, down, or east
		if(isSource){
			height = sourcePort.data.$height / 2;
			width = sourcePort.data.$width / 2;
			
			// force orthogonal edges
			offset.y = (bpLast) ? (bpLast.y - sourcePort.pos.y) : 0;
			
		}else{
			height = targetPort.data.$height /2;
			width = - targetPort.data.$width / 2;
			
			// force orthogonal edges
			offset.y = ((bpLast) ? bpLast.y : sy) - ty;
		}
		
		if(offset.y > height){
			offset.y = height;
		}
		else if(offset.y < -height){
			offset.y = -height;
		}
		
		// the x offset depends on the shape of the type
		var type = (isSource) ? sourcePort.data.$type : targetPort.data.$type;
		switch(type){
			case "bpmn_startevent" :
			case "bpmn_intermediateevent" :
			case "bpmn_endevent" :
				var ox = offset.y;
				ox = width * width - ox * ox;
				ox = (ox <= 0) ? 0 : Math.sqrt(ox);
				width = (isSource) ? ox : -ox;
				break;
				
			case 'bpmn_gateway' :
				var ox = offset.y;
				ox = (ox < 0) ? ox : -ox;
				width += (isSource) ? ox : -ox;
				break;	
				
			case 'bpmn_annotation': 
				if(isSource){
					sourcePort.data.$leftOriented = (sx < tx);
				}else{
					targetPort.data.$leftOriented = (sx > tx);
				}
				width = 0;
				break;
				
			case 'bpmn_dataobject' :
			case 'bpmn_datainput' :	
			case 'bpmn_dataoutput' :	
				break;
		}
		offset.x = width;
		return offset;
	};
	
	
	var edgeOffsetFun = function(){return {'x':0, 'y':0}};
	// define colors and icons
	registerCustomNode('bpmn_hideButton', 			'#DDDDDD', '#000000', 'Lucida Console', false, edgeOffsetFun);
	registerCustomNode('bpmn_startevent', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_intermediateevent', '#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_endevent', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_gateway', 				'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_eventgateway', 		'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_group', 				'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_activity', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_task', 					'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_subprocess', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_dataobject', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_datainput', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_dataoutput', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_annotation', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	registerCustomNode('bpmn_pool', 					'#DDDDDD', '#000000', 'Lucida Console', false, edgeOffsetFun);
	registerCustomNode('bpmn_lane', 					'#DDDDDD', '#000000', 'Lucida Console', false, edgeOffsetFun);
	registerCustomNode('bpmn_datastore', 			'#DDDDDD', '#000000', 'Lucida Console', false, baseOffset);
	
	graph.setNodeIcon("Custom", "http://yviekowski.files.wordpress.com/2011/05/wurst-index-klein.gif");
	
	// define Click Listener for edge creation
	graph.addListener("onClick", function(node, info, e){
		if(node){
			var t = node.data.$type;
			
			// EDGE CLICK
			if(node.nodeFrom){
				// do nothing. Let the flowEditor.js handle edge detach
				return;
			}
			
			// NODE CLICK
			if(t == "bpmn_hideButton" || t == "bpmn_pool" || t == "bpmn_lane"){
				if(graph.isEdgeDragged()){
					graph.connectEdge(false);
				}
			}
			else{
				var edgeType = 'bpmn_edge_sequence';
				
				// make dotted line to DATA elements
				if(node.data.$type.indexOf("bpmn_data") == 0){
					edgeType = "bpmn_edge_data";
					
				// make dashed line to annotations
				}else if(node.data.$type == "bpmn_annotation"){
					edgeType = "bpmn_edge_annotation";
				}
				// draw edge
				graph.connectEdge(node.id, false, null, {'$type' : edgeType});
			}
		}
	});
	
	/**
	 * Transforms sequence edges if they are connected to certain elements
	 */
	graph.addListener("onCreateEdge", function(sourceNode, targetNode, sourcePort, targetPort, data){
		var annoType = "bpmn_annotation";
		var dataType = "bpmn_data";
		
		// make dashed line to annotations
		if(sourcePort.data.$type == annoType || 
				targetPort.data.$type == annoType){
			data.$type = "bpmn_edge_annotation";
			return;
		}
		// make dotted line to DATA elements
		else if(sourcePort.data.$type.indexOf(dataType) == 0 || 
				targetPort.data.$type.indexOf(dataType) == 0){
			data.$type = "bpmn_edge_data";
			return;
		}
	});
	
	/**
	 * Manages hide buttons and resizing of the parent.
	 */
	graph.addAddChildListener(function(child, parent){
		var pData = parent.data,
			children = pData.$children,
			cData = child.data;
			
		// bring hideButton to front
		if(pData.$type == "bpmn_subprocess"){
			for(var c in children){
				c = children[c];
				if(c.data.$type == "bpmn_hideButton"){
					graph.setNodeToTop(c, false);
					break;
				}
			}
		}		
		// resize parent
		graph.BPMN.fitToChildren(parent);
	});
	
	/**
	 * Realign HideButtons.
	 */
	graph.addListener("onLoadGraph", function(){
		graph.iterateAllNodes(function(node){
			if(node.data.$type == "bpmn_hideButton"){
				graph.BPMN.refreshHideButton(node);
			}
		});
	});
	
	/**
	 *	Adds functionality to HideButtons.
	 */
	graph.addListener("onDragStart", function(node, info, e){
		if(node){
			if(node.data.$type == "bpmn_hideButton"){
				toggleSubProcessHidden(node);
			}
		}
	});
	
	/**
	 *	Resize a pool if one of its lanes are deleted.
	 */
	graph.addListener("onRemoveNode", function(lane){
		if(lane.data.$type != "bpmn_lane"){
			return;
		}
		
		var done = false;
		graph.iterateAllNodes(function(node){
			if(done || node.data.$type != "bpmn_pool"){
				return;
			}
			var lanes = node.data.$children;
			for(var l in lanes){
				if(lanes[l] == lane){
					lane.data.$markedForDeletion = true;
					done = true;
					BPMN.resizePool(node);
					return;
				}
			}
		});
	});
	
	/**
	 * 
	 */
	var poolLayoutList = new Array();
	
	/**
	 * Memorize pool positions to restore them after layouting.
	 */
	graph.addListener("onLoadPositionsStart", function(){
		poolLayoutList = new Array();
		var data, type;
		
		graph.iterateAllNodes(function(node){
			data = node.data;
			type = data.$type;
			
			if(type != "bpmn_pool"){
				return;
			}
			
			// make list of pools, ordered by decreasing y-pos
			var y = node.pos.y;
			
			var i = 0;
			while(true){
				if(i == poolLayoutList.length){
					poolLayoutList.push(node);
					break;
				}
				if(y > poolLayoutList[i].pos.y){
					poolLayoutList.splice(i, 0, node);
					break;
				}
				i++;
			}
		});
	});
	
	
	/**
	 * Resize Lanes and Pools after layouting the graph and reposition
	 * pools.
	 */
	graph.addListener("onLoadPositionsComplete", function(){
		
		var data, type, dim, width, label;
		var midY = 0, poolSpace = 0;
		
		graph.iterateAllNodes(function(node){
			
			data = node.data;
			type = data.$type;
			switch(type){
				case "bpmn_hideButton":
					BPMN.refreshHideButton(node);
					return;
			
				case "bpmn_pool":
					BPMN.resizePool(node);
					
					midY += node.pos.y;
					poolSpace += data.$height;
					
				case "bpmn_lane":
					dim = data.$dim;
					width = dim / 2;
					label = node.name;
					
					for(var l in label){
						width += dim;
					}
					data.$width = width;
					return; // break
			}
		});
		
		// reposition pools
		var poolCount = poolLayoutList.length;
		
		if(poolCount > 0){
			var dim = poolLayoutList[0].data.$dim;
			var midX =  poolLayoutList[0].pos.x;
			var node;
			var height;
			
			poolSpace += (poolCount - 1) * dim;
			midY = midY / poolCount - poolSpace / 2;
			
			for(var i = poolCount - 1; i >= 0; i--){
				node = poolLayoutList[i];
				height = node.data.$height;
				
				graph.moveNode(node, midX, midY + height / 2, false, true);
				midY += height + dim;
			}
		}
		
		graph.plot();
	});
	
	addBPMNConstructors(graph);
	
	/**
	 * Hides or shows the children of a subprocess.
	 *
	 * Parameters:
	 *	hideButton - the hideButton node
	 */
	function toggleSubProcessHidden(hideButton){
		var hData = hideButton.data,
			p = hData.$parent,
			pData = p.data;
			
		// hide all that is hierarchically lower than the node
		var hideFunc = function(node){
			var data = node.data;
			if(node != hideButton){
				var adja = node.adjacencies;
				for(var a in adja){
					adja[a].data.$unselectable = true;
				}
				data.$unselectable = true;
				data.$alpha = 0;
				var children = data.$children;
				for(var ch in children){
					hideFunc(children[ch]);
				}
			}
		};
		
		// show all that is hierarchically lower than the node
		// and is not a hidden subProcess
		var showFunc = function(node){
			var data = node.data;
			
			if(node != hideButton){
				var adja = node.adjacencies;
					for(var a in adja){
						delete adja[a].data.$unselectable;
					}
					delete node.data.$unselectable;
					node.data.$alpha = 1;
				var children = data.$children;
				// test if node is a hidden subProcess
				if(data.$type == "bpmn_subprocess" && data.$showHeight != undefined){
					for(var ch in children){
						ch = children[ch];
						if(ch.data.$type == "bpmn_hideButton"){
							delete ch.data.$unselectable;
							ch.data.$alpha = 1;
							break;
						}
					}
				}else{
					for(var ch in children){
						showFunc(children[ch]);
					}
				}
			}
		};
		
		var state = !hData.$hidden;
		var newHeight;
		var pChildren = pData.$children;
		if(state){
			// hide
			for(var c in pChildren){
				hideFunc(pChildren[c]);
			}
			pData.$showHeight = pData.$height;
			pData.$tempLayoutHeight = true;
			newHeight = pData.$dim * (3 + p.name.length / 2);
		}else{
			//show
			for(var c in pChildren){
				showFunc(pChildren[c]);
			}
			newHeight = pData.$showHeight;
			delete pData.$showHeight;
			delete pData.$tempLayoutHeight;
		}
		
		hData.$hidden = state;
		graph.setNodeData(p.id, {"$height" : newHeight});
		
		BPMN.refreshHideButton(hideButton);
	};
	
	/**
	 *	Hides hideButtons during loadPositions()
	 */
	graph.addListener("onLoadPositionsStart", function(){
		graph.iterateAllNodes(function(node){
			if(node.data.$type == "bpmn_hideButton"){
				graph.setNodeData(node.id, {"$alpha" : 0});
			}
		});
	});
	
	/**
	 *	Prevents hidden subprocesses from resizing on loadPositions();
	 *	also makes hideButtons reappear after loadPositions().
	 */
	graph.addListener("onLoadPositionsComplete", function(){
		var data;
		graph.iterateAllNodes(function(node){
			data = node.data;
			if(data.$type == "bpmn_hideButton" && !data.$unselectable){
				data.$alpha = 1;
			}
			// adjust the hidden height property
			else if(data.$type == "bpmn_subprocess" && data.$showHeight != undefined){
				data.$showHeight = data.$tempLayoutHeight;
				data.$tempLayoutHeight = true;
			}
		});
		BPMN.refreshAllHideButtons();
	 });
}

/**
 * Creates individual constructors for BMPN nodes.
 *
 *	Parameters:
 *   graph - the GraphFlow() object which is to be extended
 */
function addBPMNConstructors(graph){
	
	var dim = graph.getSizeModifier();
	var BPMN = new Object();
	
	
	BPMN.extendedLanes = false;
	BPMN.extendedPools = false;
	
	/**
	 * Resizes and repositions a parent in order to include all of
	 *	its children.
	 *
	 * Parameters:
	 *	 parent - the parent node that is to be resized
	 */
	BPMN.fitToChildren = function(parent){
		var pData = parent.data;
		var children = pData.$children;
		if(!children){
			return;
		}
	
		// get outer bounds of children
		var cX, cY, cWidth, cHeight;
		var leftMost = Number.POSITIVE_INFINITY;
		var rightMost = Number.NEGATIVE_INFINITY;
		var topMost = Number.POSITIVE_INFINITY;
		var bottomMost = Number.NEGATIVE_INFINITY;
		
		var child;
		for(var c = 0, l = children.length; c < l; c++){
			child = children[c];
			cX = child.pos.x;
			cY = child.pos.y;
			cWidth = child.data.$width / 2;
			cHeight = child.data.$height / 2;
			leftMost = Math.min(leftMost, cX - cWidth);
			rightMost = Math.max(rightMost, cX + cWidth);
			topMost = Math.min(topMost, cY - cHeight);
			bottomMost = Math.max(bottomMost, cY + cHeight);
		}
		
		// resize horizontally
		cWidth = dim + 2 * Math.max(parent.pos.x - leftMost, rightMost - parent.pos.x);
		var pWidth = pData.$width;
		if(pWidth < cWidth){
			//parent.pos.x = leftMost + cWidth / 2;
			pData.$width = cWidth;
		}
		// resize vertically
		cHeight = dim + 2 * Math.max(parent.pos.y - topMost, bottomMost - parent.pos.y);
		var pHeight = pData.$height;
		if(pHeight < cHeight){
			//parent.pos.y = topMost + cHeight / 2;
			pData.$height = cHeight;
		}
		graph.plot();
	}
	
	/**
	 *	Realigns all subprocess hide buttons.
	 */
	BPMN.refreshAllHideButtons = function(){
		graph.iterateAllNodes(function(node){
			var data = node.data;
			if(data.$type == "bpmn_hideButton"){
				BPMN.refreshHideButton(node);
			}
		});
	};
	
	/**
	 *	Realigns a single sub-process hide button to the
	 *	middle bottom of the sub-process.
	 *
	 *	Parameters:
	 *		hideButton - the hide button
	 */
	BPMN.refreshHideButton = function(hideButton){
		var p = hideButton.data.$parent;
		var dim = hideButton.data.$dim;
		var x = p.pos.x + ((p.data.$bottomSymbols.length - 1) * ( 5 * dim / 8));
		var y = p.pos.y + (p.data.$height - dim) / 2;
		graph.moveNode(hideButton, x, y);
	};
	
	/**
	 * WORK IN PROGESS
	 *	Parses a .bpmn-file, creating a graph from its content.
	 *
	 *	Parameters:
	 *   bpmnString - the complete content of a .bpmn-file
	 *   idPrefix - a unique prefix for all nodeIDs so that no duplicate IDs are created
	 */
	BPMN.parse = function(bpmnString, idPrefix){
		// get content within <process> brackets
		var processRegex = /[\s\S]*<(?:bpmn2\:)?process\s[\s\S]*?>([\s\S]*?)<\/(?:bpmn2\:)?process>/gi;
		var result = processRegex.exec(bpmnString);
		
		if(result){
			var processString = result[1];
			var eleRegex = /\s*<(\/?)(?:bpmn2\:)?(?:script\>([\s\S]*?)\<\/(?:bpmn2\:)?script|(incoming|outgoing)|([^\s>]*)([\s\S]*?)(\/)?)>/gi;
			var objArray = new Array();
			
			// extract objects from string
			while(result = eleRegex.exec(processString)){
				// define an integer for better performance:
				// type	-	0 : opening tag
				//			1 : closing tag
				//			2 : opening and closing tag
				var name = result[4];
				
				var isScript = result[2];
				if(isScript){
					name = "script";
				}
				
				if(name){
					var startsWithSlash = result[1];
					var endsWithSlash = result[6];
					
					var resObj = {'type' : (startsWithSlash) ? 1 : (isScript || endsWithSlash) ? 2 : 0};
					if(resObj.type != 1){
						resObj.name = name;
						resObj.attributes = (isScript) ? 'script="' + isScript + '"' :result[5];
					}
					objArray.push(resObj);
				}
			}
			BPMN.makeBPMNGraph(objArray, idPrefix);
			return true;
		}
		graph.callListener("onError", ["Error in function parseBPMN(): Not a valid BPMN string!"]);
		return false;
	};
	
	/**
	 * Helper function that is called by parseBPMN(). Constructs a graph out of the
	 * Array that was constructed by the parseBPMN() function.
	 *
	 *	Parameters:
	 *   objArray - an array of objects containing bpmn data
	 *   idPrefix - a unique prefix for all nodeIDs so that no duplicate IDs are created
	 */
	BPMN.makeBPMNGraph = function (objArray, idPrefix){
		
		var parentStack = new Array();
		var lastParent;
		var edgeStack = new Array();
		
		var idCounter = 0;
		
		var bpmnPrefix = "BPMN_";
		var attrRegex = /\s*([\S]*?)\s?=\s?\"([\s\S]*?)\"/gi;
		var isEdgeRegex = /(association)|\w*(Flow)$/i;
		var node, result;
		
		for(var obj in objArray){
			obj = objArray[obj];
			
			// closing tag
			if(obj.type == 1){
				parentStack.pop();
				lastParent = parentStack[parentStack.length - 1];
			}
			// opening tag
			else{
				// write attributes to data
				var data = {};
				var attr = obj.attributes;
				while(result = attrRegex.exec(attr)){
					data[bpmnPrefix + result[1]] = result[2];
				}
				
				// use a name if it was specified
				var name = data.BPMN_name;
				
				// is the object a script
				/*if(obj.name == "script"){
					console.log("script:"+lastParent);
					lastParent.data[bpmnPrefix+"script"] =
						data[bpmnPrefix+"script"];
				}
				
				// is the object an edge?
				else*/ 
				data.$tooltip = obj.name;
				var objInfo = {'name' : name, 'data' : data};
				
				if(isEdgeRegex.test(obj.name)){
					edgeStack.push(objInfo);
				}
				// is the object something else
				else{
					// tweak some data
					name = (name) ? (name +"\n\<"+ obj.name+"\>") : ("\<"+ obj.name+"\>");
					var id = idPrefix + (idCounter++);
					
					// add node
					objInfo.xmlTagName = obj.name;
					
					node = BPMN.addObject(objInfo, 0, 0, id);
					
					if(!node){
						id--;
						// if node is of unknown type, add it as property to the parent
						console.log("nest("+obj.name+"):"+lastParent);
						
						BPMN.addPropertyToNode(lastParent, {'xmlTagName' : obj.name, 'data' : data});
					}
				}
				
				// add node to parent stack if it is an opening tag
				if(node && obj.type == 0){
					parentStack.push(node);
					lastParent = node;
				}
			}
		}
		
		// connect edges
		var srcDate = bpmnPrefix + "sourceRef";
		var tarDate = bpmnPrefix + "targetRef";
		var idDate = bpmnPrefix + "id";
		
		while(edgeStack.length != 0){
			var edge = edgeStack.pop();
			// get BPMN_id from edge
			var sourceBID = edge.data[srcDate];
			var targetBID = edge.data[tarDate];
			
			// get node that matches the BPMN_ids
			var source = getNodesByDate(idDate, sourceBID);
			var target = getNodesByDate(idDate, targetBID);
			
			// do these nodes exist? then connect their ports
			if(source[0] && target[0]){
				edge.data.$type = 'bpmn_edge_sequence';
				graph.addEdge(source[0].id, target[0].id, edge.name, edge.data);
			}
		}
	};
	
	/**
	 * Adds an (unknown) xml property to the parent.
	 */
	BPMN.addPropertyToNode = function(node, propertyObject){
		if(node){
			var isEventRegex = /bpmn_(.*)Event$/i;
			
			// if the parent is an event, this may be its symbol definition
			if(isEventRegex.test(node.data.$type)){
				var eventTypeRegex = /(.*)EventDefinition$/i;
				var result;
				
				if(result = eventTypeRegex.exec(propertyObject.xmlTagName)){
					node.data.$symbol = result[1];
					return;
				}
			}
			
			var nest = node.data.nestedObjects;
			if(!nest){
				// make room for nested objects
				nest = node.data.nestedObjects = new Array();
			}
			nest.push(propertyObject);
		}
	};
	
	/**
	 *	Creates a single BPMN-Element and adds it to the graph.
	 *
	 *	Parameters:
	 *	 objInfo - an object that contains xml-data of the element
	 *	 x - the x-position where the object is created
	 *	 y - the y-position where the object is created
	 *	 id - a unique identifier of the element
	 */
	BPMN.addObject = function(objInfo, x, y, id){
		var xmlTagName = objInfo.xmlTagName.toLowerCase;
		var data = objInfo.data;
		var label = objInfo.name;
			
		getType:{
			// EVENT
			var eventRegex = /(start|intermediate|end).*(Event)$/i;
			var result = eventRegex.exec(xmlTagName);
			if(result){
				var symbol = (data.BPMN_parallelMultiple == "true") 
								? "parallel" 
								: "multiple";
				BPMN.addEvent(x, y, id, result[1], symbol);
				break getType;
			}
			// GATEWAY
			var gatewayRegex = /(.*)(Gateway)$/i;
			var result = gatewayRegex.exec(xmlTagName);
			if(result){
				var symbol = result[1];
				BPMN.addGateway(x, y, id, xmlTagName, symbol);
				break getType;
			}
			// ACTIVITY
			var activityRegex = /(.*)(Task|Process|Activity)$/i;
			result = activityRegex.exec(xmlTagName);
			if(result){
				var type = result[1];
				BPMN.addActivity(x, y, id, label);
				break getType;
			}
			// DATA_OBJECT
			var dataObjectRegex = /(Data)(.*)$/i;
			result = dataObjectRegex.exec(xmlTagName);
			if(result){
				var type = result[2];
				if(type == "Input" || type == "Output" || type == "Object" || type =="Store"){
					BPMN.addDataObject(x, y, id, "testType", type);
				}else{
					return false;
				}
				break getType;
			}
			// ANNOTATION
			var annotationRegex = /(Annotation)$/i;
			result = annotationRegex.exec(xmlTagName);
			if(result){
				var type = result[1];
				BPMN.addAnnotation(x, y, id, type);
				break getType;
			}
			
			if(xmlTagName == "extensionElements"){
				BPMN.addActivity(x, y, id, xmlTagName);
				break getType;
			}
			// UNKNOWN
			return false;
		}
		graph.setNodeData(id, data);
		return graph.getNode(id);
	};
	
	/**
	 *	Creates a BPMN-Element and adds it to the graph.
	 * This function should be used instead of "addObject()" when
	 * new elements are created.
	 *
	 *	Parameters:
	 *	 elementType - the intern nodeType of the element
	 *	 label - the displayed label of the element
	 *	 x - the x-position where the object is created
	 *	 y - the y-position where the object is created
	 *	 data - additional data, added to the element
	 */
	BPMN.addElement = function(elementType, label, x, y, data){
		
		// convert String to array for linebreaks
		if(label && label != ""){
			label = label.split("\n");
		}else{
			label = [elementType];
		}
	
		// define basic data
		var eData = {
			"$dim" : 2 * dim,
			"$font" : "px Lucida Console",
			"$width" : 4 * dim,
			"$height" : 4 * dim,
			"$movable" : true
		};
		
		// define element specific data
		var font = (1.66 * dim) + eData.$font;
		switch(elementType){
			case 'bpmn_endevent':
				eData.$negative = true;
				break;
			
			case 'bpmn_group':
			case 'bpmn_subprocess':
				eData.$children = new Array();
			case 'bpmn_activity':
			case 'bpmn_task':
				// adjust height to match linebreaks in task name
				eData.$height += dim + label.length * eData.$dim;
				// calculate width of longest text line
				for(var l in label){
					eData.$width = Math.max(eData.$width, graph.getTextWidth(label[l], font));
				}
				eData.$width += eData.$dim;
				break;
			case 'bpmn_dataobject':
			case 'bpmn_datainput':
			case 'bpmn_dataoutput':
				eData.$width = 2.5 * dim;
				eData.$height = 3 * dim;
				eData.$dim = dim;
				break;
			case 'bpmn_annotation' :
				eData.$height = (label.length + 1) * dim;
				// calculate width of longest text line
				for(var l in label){
					eData.$width = Math.max(eData.$width, graph.getTextWidth(label[l], font));
				}
				eData.$width += dim;
				eData.$dim = dim;
				break;
				
			case 'bpmn_lane' :
				eData.$leftOffset = 0;
			case 'bpmn_pool' :
				eData.$width = dim;
				for(var l in label){
					//eData.$noLayout = true;
					eData.$height = Math.max(eData.$height, graph.getTextWidth(label[l], font) + 2 * dim);
					eData.$minHeight = eData.$height;
					eData.$width += 2 * dim;
				}
				eData.$children = new Array();
				break;
		}
		
		// apply additional data
		for(var d in data){
			eData[d] = data[d];
		}
		
		// create element
		var newEle = graph.addCustomNode(x, y, label, elementType, eData, false);
	
		// add HideButton for subProcesses
		if(elementType == "bpmn_subprocess"){
			var hideButtonData = {
				"$dim" : dim,
				"$width" : dim,
				"$height" : dim,
				"$hidden" : false,
				"$parent" : newEle,
				"$noLayout" : true,
				"$movable" : false
			};
			var hideButton = graph.addCustomNode(x, y, false, "bpmn_hideButton", hideButtonData, true);
			newEle.data.$children.push(hideButton);
			BPMN.refreshHideButton(hideButton);
		}
		
		return newEle;
	};
	
	/**
	 * Adds a bpmn_lane object as a child to a bpmn_pool object,
	 * dropping it near the specified location and resizing the pool.
	 * 
	 *	Parameters:
	 *   lane - the 'bpmn_lane' node
	 *   pool - the 'bpmn_lane' node
	 *   yPos - the y-position where the lane should be inserted
	 */
	BPMN.addLaneToPool = function(lane, pool, yPos){
		var laneArray = pool.data.$children;
		
		// check if lane exists already
		for(var l in laneArray){
			if(laneArray[l] == lane){
				return;
			}
		}
		
		
		// determine children array index of new lane
		var cIndex = 0;
		
		for(var l in laneArray){
			l = laneArray[l];
			if(yPos > l.pos.y){
				cIndex++;
			}
		}
		
		// add lane to pool and resize it
		lane.data.$leftOffset = pool.data.$width;
		laneArray.splice(cIndex, 0, lane);
		BPMN.resizePool(pool);
	};
	
	/**
	 * Resizes a pool element, rearranging all lanes.
	 *
	 *	Parameters:
	 *	  pool - a 'bpmn_pool' node
	 */
	BPMN.resizePool = function(pool){
		var laneArray = pool.data.$children;
		// calculate height of all lanes combined
		var combinedLaneHeight = 0;
		for(var l in laneArray){
			l = laneArray[l];
			if(l.data.$markedForDeletion){
				continue;
			}
			combinedLaneHeight += l.data.$height;
		}
		// rearrange lanes
		var lanePos = pool.pos.y - combinedLaneHeight / 2;
		var halfHeight;
		for(var l in laneArray){
			l = laneArray[l];
			if(l.data.$markedForDeletion){
				continue;
			}
			halfHeight = l.data.$height / 2;
			lanePos += halfHeight;
			graph.moveNode(l, l.pos.x, lanePos, false, true);
			lanePos += halfHeight;
		}

		// resize pool
		pool.data.$height = Math.max(combinedLaneHeight, pool.data.$minHeight);
		//graph.setNodeData(pool.id, {'$height' : Math.max(combinedLaneHeight, pool.data.$minHeight)});
		graph.plot();
	};
	
	/**
	 * Performs an hierarchical layout algorithm on the graph, resizing its components if necessary.
	 */
	BPMN.autoLayout = function(){
		
		var layerObject = {};
		var layerCount = 0;
		
		var incLayer = function(node){
			
			// make entry in layerObject
			if(layerObject[node.id] == undefined){
				layerObject[node.id] = 0;
			}else{
				layerObject[node.id]++;
			}
			// increment layer of children
			var lc = node.data.$children;
			for(var c in lc){
				incLayer(lc[c]);
			}
			
			layerCount = Math.max(layerCount, layerObject[node.id]);
		}
		
		graph.iterateAllNodes(function(node){
			incLayer(node);
		});
		
		// array of layers and groups
		var groups = new Array(layerCount + 1);
		for(var i = layerCount; i >= 0; i--){
			groups[i] = {"parents" : new Array(), "children" : new Array()};
		}
		groups[0].children.push(new Array());
		
		// sort groups of children by their respective layer
		var children;
		graph.iterateAllNodes(function(node){
			children = node.data.$children;
			
			if(layerObject[node.id] == 0){
				groups[0].children[0].push(node);
			}
			
			if(children && children.length > 0){
				
				var group = new Array(children.length);
				for(var c in children){
					group[c] = children[c];
				}
				
				var layer = layerObject[children[0].id];
				groups[layer].parents.push(node);
				groups[layer].children.push(group);
			}
		});
		
		return groups;
	};
	
	graph.BPMN = BPMN;
}

/**
 *	This object contains draw and mouse collision functions for
 * all supported BPMN elements. These functions are used in 
 * plotting and mouse-over routines.
 *
 */
var BPMN_Objects = {
	'bpmn_hideButton' : {
		'render' : function(node, canvas) {
			var pos = node.pos,
				data = node.data,
				dim = data.$dim,
				hidden = data.$hidden;
				
			var ctx = canvas.getCtx();
			ctx.fillStyle = data.$fillColor;
			ctx.strokeStyle = data.$color;
			
			if(hidden){
				SymbolHelper.task.drawSymbol("subprocess", ctx, pos.x, pos.y, dim);
			}
			else{
				SymbolHelper.task.drawSymbol("subprocess_hidden", ctx, pos.x, pos.y, dim);
			}
		},
		
		'contains' : function(node, pos){
    	  if(node.data.$unselectable){
    		  return false;
    	  }
	      var npos = node.pos.getc(true), 
	          dim = node.data.$dim;
	      return Math.abs(pos.x - npos.x) < dim && Math.abs(pos.y - npos.y) < dim;
        }
	},

	'bpmn_startevent' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				radius =  data.$dim,
				lineWidth = radius/10;  
			
			var ctx = canvas.getCtx();
			
			// fill circle
			ctx.save();
			ctx.beginPath();
			ctx.fillStyle = data.$fillColor;
			ctx.arc(x, y, radius, 0, CircleHelper.fullCirc, true);
			ctx.fill(); 
			
			//draw stroke(s)
			ctx.beginPath();
			ctx.fillStyle = data.$color;
			ctx.lineWidth = lineWidth;
			
			if(data.$nonInterrupting){
				CircleHelper.drawDashedCircle(ctx, x, y, radius, false);
			}
			else{
				ctx.arc(x, y, radius, 0, CircleHelper.fullCirc, true);
				ctx.stroke();
			}
			
			// draw label
			var label = node.name;
			if(canvas.showLabels && data.$showLabel && label){
				SymbolHelper.drawLabel(label, radius, ctx, data.$color, data.$font, x, y + 1.8 * radius);
			}
			
			// draw icon
			var img = data.$icon;
			radius *= 1.2;
			if(img){
				$jit.GraphFlow.NodeHelper.drawImage(ctx, img, x-radius/2, y-radius/2, radius, radius);
		    }else if(data.$symbol){
		    	SymbolHelper.drawSymbol(data.$type, data.$symbol, ctx, x, y, radius, radius, data.$negative);
		    }
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  if(node.data.$unselectable){
    		  return false;
    	  }
	      var npos = node.pos.getc(true), 
	          radius = node.data.$dim;
	      return Math.abs(pos.x - npos.x) < radius && Math.abs(pos.y - npos.y) < radius;
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_intermediateevent' : {
		'render' : function(node, canvas) {  
			var pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				radius =  node.data.$dim,
				lineWidth = radius/10;  
			
			var ctx = canvas.getCtx();
			ctx.save();
			
			// draw regular event
			BPMN_Objects.bpmn_startevent.render(node, canvas);
			
			// draw inner circle
			ctx.fillStyle = node.data.$color;
			ctx.lineWidth = lineWidth;
			ctx.beginPath();
			
			if(node.data.$nonInterrupting){
				CircleHelper.drawDashedCircle(ctx, x, y, radius*0.8, true);
			}
			else{
				ctx.arc(x, y, radius*0.8, 0, CircleHelper.fullCirc, true);
			}
			
			ctx.stroke();
			ctx.beginPath();
			ctx.restore();
	  },
      
	  'contains' : function(node, pos){
		  return BPMN_Objects.bpmn_startevent.contains(node, pos);
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_endevent' : {
		'render' : function(node, canvas) {  
			var pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				radius =  node.data.$dim,
				lineWidth = radius/5;  
			
			var ctx = canvas.getCtx();
			
			// draw regular event
			BPMN_Objects.bpmn_startevent.render(node, canvas);
			
			// draw thick line
			ctx.save();
			ctx.fillStyle = node.data.$color;
			ctx.lineWidth = lineWidth;
			
			ctx.beginPath();
			ctx.arc(x, y, radius, 0, CircleHelper.fullCirc, true);
			ctx.stroke();
			
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_startevent.contains(node, pos);
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_gateway' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				dim =  data.$dim; 
			
			var ctx = canvas.getCtx();
			
			// fill rhombus
			ctx.beginPath();
			ctx.fillStyle = data.$fillColor;
			ctx.moveTo(x, y - dim);
			ctx.lineTo(x + dim, y);
			ctx.lineTo(x, y + dim);
			ctx.lineTo(x - dim, y);
			ctx.closePath();
			ctx.fill(); 
			
			// stroke path
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw icon
		    var img = data.$icon;
		    if(img){
		    	$jit.GraphFlow.NodeHelper.drawImage(ctx, img, x-dim/2, y-dim/2, dim, dim);
		    }else if(data.$symbol){
		    	SymbolHelper.drawSymbol(data.$type, data.$symbol, ctx, x, y, dim, dim, true);
		    }
		    
		    // draw label
			var label = node.name;
			if(canvas.showLabels && data.$showLabel && label){
				SymbolHelper.drawLabel(label, dim, ctx, data.$color, data.$font, x, y + 1.7 * dim);
			}
	  },
      
      'contains' : function(node, pos){
    	  if(node.data.$unselectable){
    		  return false;
    	  }
	      var npos = node.pos.getc(true), 
	          radius = node.data.$dim;
	      return Math.abs(pos.x - npos.x) + Math.abs(pos.y - npos.y) < radius;
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_eventgateway' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				eventType = data.$eventType,
				dim =  data.$dim; 
			
			var ctx = canvas.getCtx();
			
			// fill rhombus
			ctx.beginPath();
			ctx.fillStyle = data.$fillColor;
			ctx.moveTo(x, y - dim);
			ctx.lineTo(x + dim, y);
			ctx.lineTo(x, y + dim);
			ctx.lineTo(x - dim, y);
			ctx.closePath();
			ctx.fill(); 
			
			// stroke path
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw event
			if(!eventType){
				eventType = "bpmn_startevent";
			}
			data.$dim = dim / 2;
			var showLabel = data.$showLabel;
			data.$showLabel = false;
			BPMN_Objects[eventType].render(node, canvas);
			data.$dim = dim;
			data.$showLabel = showLabel;
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_gateway.contains(node, pos);
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_task' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				roundness = dim/4,
				label = node.name; 
			
			// paint rounded rectangle
			var ctx = canvas.getCtx(),
				hWidth = data.$width/2,
				hHeight= data.$height/2,
				rWidth = hWidth - roundness,
				rHeight = hHeight - roundness,
				x = pos.x - rWidth,
				y = pos.y - rHeight;
				
			var pif = CircleHelper.quarterCirc,
				pit = 3 * pif;
			
			ctx.save();
			ctx.beginPath();
			
			// upper left
			ctx.arc(x , y , roundness, CircleHelper.halfCirc, pit, false);
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
			ctx.arc(x , y , roundness, pif, CircleHelper.halfCirc, false);
			ctx.closePath();
			
			if(data.$isTransaction){
				ctx.save();
				var lw = ctx.lineWidth;
				ctx.lineWidth *= 5;
				ctx.strokeStyle = data.$color;
				ctx.stroke();
				ctx.lineWidth = 3 * lw;
				ctx.strokeStyle = data.$fillColor;
				ctx.stroke();
				ctx.lineWidth = lw;
				ctx.restore();
			}
			
			ctx.fillStyle = data.$fillColor;
			ctx.fill(); 
			ctx.strokeStyle = data.$color;
			
			// draw dotted path
			if(data.$isEventSubProcess){
				ctx.save();
				ctx.beginPath();
				
				var rc = roundness * 0.3,
					rc2 = roundness - rc;
				var lw = ctx.lineWidth,
					hlw = lw / 2;
				
				var from = {'x' : pos.x - hWidth + roundness, 'y' : pos.y - hHeight};
				var to = {'x' : pos.x + hWidth - roundness, 'y' : from.y};
				
				// top arcs
				ctx.moveTo(from.x - rc2 - hlw, from.y + rc);
				ctx.lineTo(from.x - rc2 + hlw, from.y + rc);
				ctx.moveTo(to.x + rc2 + hlw, from.y + rc);
				ctx.lineTo(to.x + rc2 - hlw, from.y + rc);
				ctx.stroke();
				
				// top
				$jit.GraphFlow.EdgeHelper.dottedLine(ctx, from, to, roundness);
				
				// bottom
				var top = from.y + roundness;
				from.y += hHeight + hHeight;
				to.y = from.y;
				$jit.GraphFlow.EdgeHelper.dottedLine(ctx, from, to, roundness);
				
				// bottom arcs
				ctx.moveTo(from.x - rc2 - hlw, from.y - rc);
				ctx.lineTo(from.x - rc2 + hlw, from.y - rc);
				ctx.moveTo(to.x + rc2 + hlw, from.y - rc);
				ctx.lineTo(to.x + rc2 - hlw, from.y - rc);
				ctx.stroke();
				
				// left
				var right = to.x + roundness;
				from.x -= roundness;
				from.y = top;
				to.x = from.x;
				to.y -= roundness;
				$jit.GraphFlow.EdgeHelper.dottedLine(ctx, from, to, roundness);
				
				// right
				to.x = right;
				from.x = right;
				$jit.GraphFlow.EdgeHelper.dottedLine(ctx, from, to, roundness);
				ctx.restore();
			}
			else{
				ctx.stroke();
			}
			
			// draw top symbol
			var symbol = data.$symbol;
			
			if(symbol){
				x = pos.x - hWidth + roundness;
				y = pos.y - hHeight + roundness;
				SymbolHelper.task.drawSymbol(symbol, ctx, x, y, dim);
			}
			
			
			// paint label
			if(canvas.showLabels && data.$showLabel && label){
				var midY = pos.y + ((data.$children) ? dim - hHeight : 0.415 * dim);
				SymbolHelper.drawLabel(label, dim, ctx, data.$color, data.$font, pos.x, midY);
			}
			
			// draw bottom symbols
			var bSymbols = data.$bottomSymbols;
			if(bSymbols && bSymbols.length > 0){
				ctx.fillStyle = data.$fillColor;
				ctx.strokeStyle = data.$color;
				var symDim = dim / 2;
				var gap = symDim / 4;
				x = pos.x - ((bSymbols.length - 1) * (symDim + gap) / 2);
				y = pos.y + hHeight - roundness;
				
				for(var s in bSymbols){
					SymbolHelper.task.drawSymbol(bSymbols[s], ctx, x, y, symDim);
					x += symDim + gap;
				}
			}
			
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  if(node.data.$unselectable){
    		return false;
    	  }
    	  var npos = node.pos.getc(true), 
    	  	  width = node.data.$width / 2,
    	  	  height = node.data.$height / 2;
    	  return Math.abs(pos.x - npos.x) < width && Math.abs(pos.y - npos.y) < height;
      }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_activity' : {
		'render' : function(node, canvas) {
			// make stroke thicker
			var ctx = canvas.getCtx();
			ctx.save();
			ctx.lineWidth *= 2;
			
			// draw task
			BPMN_Objects.bpmn_task.render(node, canvas);
			ctx.restore();
		},
		
		'contains' : function(node, pos){
	    	  return BPMN_Objects.bpmn_task.contains(node, pos);
	    }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_subprocess' : {
		'render' : function(node, canvas) {
			// draw task
			BPMN_Objects.bpmn_task.render(node, canvas);
		},
		
		'contains' : function(node, pos){
	    	  return BPMN_Objects.bpmn_task.contains(node, pos);
	    }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_group' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				roundness = dim/4,
				label = node.name; 
			
			// paint rounded rectangle
			var ctx = canvas.getCtx(),
				hWidth = data.$width/2,
				hHeight= data.$height/2,
				rWidth = hWidth - roundness,
				rHeight = hHeight - roundness,
				x = pos.x - rWidth,
				y = pos.y - rHeight;
				
			var pif = CircleHelper.quarterCirc,
				pit = 3 * pif;
			
			ctx.save();
			ctx.strokeStyle = data.$color;
			
			// draw dashed/dotted path
			
			ctx.beginPath();
			
			var rc = roundness * 0.3,
				rc2 = roundness - rc;
			var lw = ctx.lineWidth,
				hlw = lw / 2;
			
			var from = {'x' : pos.x - hWidth + roundness, 'y' : pos.y - hHeight};
			var to = {'x' : pos.x + hWidth - roundness, 'y' : from.y};
			
			// top arcs
			ctx.moveTo(from.x - rc2 - hlw, from.y + rc);
			ctx.lineTo(from.x - rc2 + hlw, from.y + rc);
			ctx.moveTo(to.x + rc2 + hlw, from.y + rc);
			ctx.lineTo(to.x + rc2 - hlw, from.y + rc);
			ctx.stroke();
			
			// top
			$jit.GraphFlow.EdgeHelper.dashedDottedLine(ctx, from, to, roundness);
			
			// bottom
			var top = from.y + roundness;
			from.y += hHeight + hHeight;
			to.y = from.y;
			$jit.GraphFlow.EdgeHelper.dashedDottedLine(ctx, from, to, roundness);
			
			// bottom arcs
			ctx.moveTo(from.x - rc2 - hlw, from.y - rc);
			ctx.lineTo(from.x - rc2 + hlw, from.y - rc);
			ctx.moveTo(to.x + rc2 + hlw, from.y - rc);
			ctx.lineTo(to.x + rc2 - hlw, from.y - rc);
			ctx.stroke();
			
			// left
			var right = to.x + roundness;
			from.x -= roundness;
			from.y = top;
			to.x = from.x;
			to.y -= roundness;
			$jit.GraphFlow.EdgeHelper.dashedDottedLine(ctx, from, to, roundness);
			
			// right
			to.x = right;
			from.x = right;
			$jit.GraphFlow.EdgeHelper.dashedDottedLine(ctx, from, to, roundness);
			
			// paint label
			var midX, midY, labelWidth, labelHeight;
			var labelSize = 0.83 * dim;
			
			if(canvas.showLabels && data.$showLabel && label){
				ctx.fillStyle = data.$color;
				ctx.font = labelSize + data.$font;
				midY = pos.y - hHeight + dim;
				
				for(var t = 0, l = label.length; t < l; t++){
					labelWidth = ctx.measureText(label[t]).width/2;
					midX = pos.x - labelWidth;
					
					ctx.fillText(label[t], midX, midY);
					midY += dim;
				}
			}
			
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_task.contains(node, pos);
        }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_dataobject' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				label = node.name; 
			
			// draw file
			var ctx = canvas.getCtx(),
				width = data.$width,
				height = data.$height,
				fold = height / 3,
				x = pos.x,
				y = pos.y;
				
			ctx.beginPath();
			y = y - height / 2;
			x = x - width / 2;
			
			ctx.moveTo(x, y);
			
			x += width
			ctx.lineTo(x - fold, y);
			ctx.lineTo(x, y + fold);
			y += height;
			ctx.lineTo(x, y);
			ctx.lineTo(x - width, y);
			ctx.closePath();
			
			ctx.fillStyle = data.$fillColor;
			ctx.fill(); 
			
			// stroke path
			y -= height - fold;
			ctx.moveTo(x, y);
			x -= fold;
			ctx.lineTo(x, y);
			ctx.lineTo(x, y - fold);
			
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw collection symbol
			var isCollection = data.$isCollection;
			if(isCollection){
				ctx.beginPath();
				var dy = pos.y +  height / 2 - dim * 0.25;
				SymbolHelper.task.parallel(ctx, pos.x, dy, dim);
			}
			
			
			// paint label
			var midX, midY, labelWidth, labelHeight;
			var labelSize = 0.83 * dim;
			
			if(canvas.showLabels && data.$showLabel && label){
				ctx.fillStyle = data.$color;
				ctx.font = labelSize + data.$font;
				midY = pos.y + height / 2 + dim;
				
				for(var t = 0, l = label.length; t < l; t++){
					labelWidth = ctx.measureText(label[t]).width/2;
					midX = pos.x - labelWidth;
					
					ctx.fillText(label[t], midX, midY);
					midY += dim;
				}
			}
	  },
      
      'contains' : function(node, pos){
    	  if(node.data.$unselectable){
    		  return false;
    	  }
    	  var npos = node.pos.getc(true), 
    	  	  width = node.data.$width / 2,
    	  	  height = node.data.$height / 2;
    	  return Math.abs(pos.x - npos.x) < width && Math.abs(pos.y - npos.y) < height;
        }
	},
	
	'bpmn_datainput' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim; 
			
			// draw basic dataObject
			BPMN_Objects.bpmn_dataobject.render(node, canvas);
			
			// draw file
			var ctx = canvas.getCtx(),
				width = data.$width,
				height = data.$height;
			
			ctx.save();
			
			var dim3 = dim / 3;
			var dx = pos.x - width / 2 + dim3,
				dy = pos.y;
			
			// draw input label
			/*if(canvas.showLabels){
				var labelSize = width / 3.8;
				ctx.font = labelSize + "px Verdana";
				ctx.fillText('Input', dx, dy + labelSize / 2);
			}*/
			
			// draw input arrow
			dy -= height / 2 - dim3;
			ctx.lineWidth /= 2;
			SymbolHelper.arrow(ctx, dx, dy, dim * 0.9, dim * 0.7, false);
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_dataobject.contains(node, pos);
        }
	},
	
	'bpmn_dataoutput' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim; 
			
			// draw basic dataObject
			BPMN_Objects.bpmn_dataobject.render(node, canvas);
			
			// draw file
			var ctx = canvas.getCtx(),
				width = data.$width,
				height = data.$height;
			
			ctx.save();
					
			
			var dim3 = dim / 3;
			var dx = pos.x - width / 2 + dim3,
				dy = pos.y;
			// draw output label
			/*
			if(canvas.showLabels){
				var labelSize = width / 3.5;
				ctx.font = labelSize + "px Verdana";
				ctx.fillText('Out-', dx, dy + labelSize / 2);
				ctx.fillText('put', dx + labelSize/4, dy + 1.5 * labelSize);
			}*/
			
			// draw output arrow
			dy -= height / 2 - dim3;
			ctx.lineWidth /= 2;
			SymbolHelper.arrow(ctx, dx, dy, dim * 0.9, dim * 0.7, true);
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_dataobject.contains(node, pos);
        }
	},
		
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_datastore' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				width = data.$width,
				height = data.$height,
				dim =  data.$dim;  
			
			var ctx = canvas.getCtx();
			
			var smal = height * 0.1;
			var mid = height * 0.15;
			
			var left = x - width / 2;
			var right = left + width;
			var top = y - height / 2;
			var bot = top + height;
			
			var bot2 = bot - smal;
			var top2 = top + smal;
			
			// fill circle
			ctx.save();
			ctx.beginPath();
			ctx.moveTo(left, top2);
			ctx.quadraticCurveTo(left, top, x, top);
			ctx.quadraticCurveTo(right, top, right, top2);
			ctx.lineTo(right, bot2);
			ctx.quadraticCurveTo(right, bot, x, bot);
			ctx.quadraticCurveTo(left, bot, left, bot2);
			ctx.closePath();
			
			// fill container, stroke outlines
			ctx.fillStyle = data.$fillColor;
			ctx.fill(); 
			
			var lineWidth = dim/10;
			ctx.lineWidth = lineWidth;
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw inner lines
			ctx.beginPath();
			for(var i = 0; i < 3; i++){
				ctx.moveTo(left, top2);
				ctx.quadraticCurveTo(left, top2 + smal, x, top2 + smal);
				ctx.quadraticCurveTo(right, top2 + smal, right, top2);
				top2 += mid;
			}
			ctx.stroke();
			
			// paint label
			var label = node.name;
			if(canvas.showLabels && data.$showLabel && label){
				SymbolHelper.drawLabel(label, dim, ctx, data.$color, data.$font, x, bot + dim);
			}
			
			ctx.restore();
	  },
      
      'contains' : function(node, pos){
    	  return BPMN_Objects.bpmn_dataobject.contains(node, pos);
      }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_annotation' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				label = node.name; 
			
			var ctx = canvas.getCtx(),
				width = data.$width,
				height= data.$height / 2,
				x = pos.x,
				y = pos.y,
				left = data.$leftOriented,
				dir;
			
			if(left){
				dir = -dim;
			}else{
				dir = dim;
			}
			
			// paint bracket
			ctx.beginPath();
			ctx.fillStyle = data.$color;
			
			y += height;
			ctx.moveTo(x + dir, y)
			ctx.lineTo(x, y);
			y -= 2 * height;
			ctx.lineTo(x, y);
			ctx.lineTo(x + dir, y);
			ctx.stroke();
			 
			// paint label
			var midX, midY;
			var labelSize = 0.83 * dim;
			
			if(canvas.showLabels && data.$showLabel && label){
				ctx.font = labelSize + data.$font;
				midY = y + 1.25 * dim;
				midX = x + dir/2;
				
				if(left){
					var labelWidth;
					for(var t = 0, l = label.length; t < l; t++){
						labelWidth = ctx.measureText(label[t]).width;
						ctx.fillText(label[t], midX - labelWidth, midY);
						midY += dim;
					}
				}
				else{
					for(var t = 0, l = label.length; t < l; t++){
						ctx.fillText(label[t], midX, midY);
						midY += dim;
					}
				}
			}
		},
      
	    'contains' : function(node, pos){
			if(node.data.$unselectable){
			  return false;
			}
			var npos = node.pos.getc(true), 
			  	width = node.data.$width /2,
			  	height = node.data.$height / 2;
			if(node.data.$leftOriented){
				  return Math.abs(pos.x - npos.x + width) < width && Math.abs(pos.y - npos.y) < height;
			}
			else{
				  return Math.abs(pos.x - npos.x - width) < width && Math.abs(pos.y - npos.y) < height;
			}
	    }
	},
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_pool' : {
		'render': function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				poolName = node.name;
			
			// canvas values
			var bounds = graph.getScreenBounds(),
				left = bounds.left,
				right = bounds.right;
			
			var ctx = canvas.getCtx(),
				width = data.$width,
				height = data.$height,
				x = pos.x,
				y = pos.y - height / 2,
				bottom = y + height;
			
			ctx.beginPath();
			
			// fill pool
			ctx.fillStyle = data.$fillColor;
			ctx.fillRect(left, y, right - left, height);
			
			// draw borders
			ctx.fillStyle = data.$color;
			
			ctx.moveTo(left, y);
			ctx.lineTo(right, y);
						
			// draw bottom line
			ctx.moveTo(left, bottom);
			ctx.lineTo(right, bottom);
			ctx.stroke();
			
			// draw pool label fill
			ctx.fillRect(left, bottom - height, width, height);
			
			// draw label
			if(canvas.showLabels && data.$showLabel && poolName){
				var labelWidth;
				ctx.save();
				ctx.rotate(-CircleHelper.quarterCirc);
				midX = left + dim;
				
				// pool name
				ctx.fillStyle = data.$fillColor;
				ctx.font = 0.83 * dim + data.$font;
				
				for(var t = 0, l = poolName.length; t < l; t++){
					labelWidth = ctx.measureText(poolName[t]).width / 2;
					midY = -pos.y - labelWidth;
					
					ctx.fillText(poolName[t], midY, midX);
					midX += dim;
				}
				midX += dim / 4;
				ctx.restore();
			}
		},
			
		'contains' : function(node, pos){
			if(node.data.$unselectable){
	    		  return false;
	    	}
			var bounds = graph.getScreenBounds(),
				height = node.data.$height,
				top = node.pos.y - height / 2;
			return pos.y >= top && pos.y <= top + height && (BPMN.extendedPools || pos.x <= bounds.left + node.data.$width);
		}
	},
	
////////////////////////////////////////////////////////////////////////////////////////////
	
	'bpmn_lane' : {
		'render': function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				label = node.name;
			
			// canvas values
			var bounds = graph.getScreenBounds(),
				left = bounds.left + data.$leftOffset,
				right = bounds.right;
			
			var ctx = canvas.getCtx(),
				width = data.$width,
				height = data.$height,
				top = pos.y - height / 2,
				bottom = top + height;
			
			ctx.save();
			
			// draw lane
			ctx.beginPath();
			ctx.moveTo(right, top);
			ctx.lineTo(left, top);
			ctx.lineTo(left, bottom);
			ctx.lineTo(right, bottom);
			
			ctx.strokeStyle = data.$color;
			ctx.stroke();
			
			// draw label
			if(canvas.showLabels && data.$showLabel && label){
				var labelWidth;
				
				ctx.fillStyle = data.$color;
				ctx.font = 0.83 * dim + data.$font;
				ctx.rotate(-CircleHelper.quarterCirc);
				midX = left + dim;
				
				for(var t = 0, l = label.length; t < l; t++){
					labelWidth = ctx.measureText(label[t]).width / 2;
					midY = -pos.y - labelWidth;
					
					ctx.fillText(label[t], midY, midX);
					midX += dim;
				}
				ctx.restore();
			}
		},
		'contains' : function(node, pos){
			if(BPMN.extendedPools){
				return false;
			}
			
			var data = node.data;
			if(data.$unselectable){
	    		  return false;
	    	}
			var bounds = graph.getScreenBounds(),
				height = data.$height,
				lleft = bounds.left + data.$leftOffset
				ltop = node.pos.y - height / 2;
			
			return pos.y >= ltop && pos.y <= ltop + height && pos.x > lleft && (BPMN.extendedLanes || pos.x <= lleft + data.$width);
		}
	}
};

/**
 * This object contains draw and mouse collision functions for 
 * all supported BPMN edge types. These functions are used in 
 * plotting and mouse-over routines.
 */
var BPMN_Edges ={
	'bpmn_edge_sequence' : {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints,
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
			
			var ctx = canvas.getCtx();
			// draw potential bend points
			var	toBend;
			if(bend){
				ctx.beginPath();
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					ctx.moveTo(from.x, from.y);
					ctx.lineTo(toBend.x, toBend.y);
					from = bend[b];
				}
				ctx.stroke();
			}
			
			// draw final line
			$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 0, dim, dim, dim);
			
			// add the label
			var label = adj.data.$label;
			if(label && canvas.showLabels){
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	'bpmn_edge_conditional': {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints,
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
			var ctx = canvas.getCtx();
			
			var	toBend;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					ctx.moveTo(from.x, from.y);
					ctx.lineTo(toBend.x, toBend.y);
					from = bend[b];
				}
			}
			
			// calculate some stuff for drawing the rhombus
			var vect = {'x' : 0, 'y' : 0}, 
				normal = {'x' : 0, 'y' : 0}, 
				iPoint = {'x' : 0, 'y' : 0};
			
			if(bend && bend.length > 0){
				vect.x = bend[0].x - from.x;
				vect.y = bend[0].y - from.y;
			}else{
				vect.x = to.x - from.x;
				vect.y = to.y - from.y;
			}
			abs = Math.sqrt(vect.x * vect.x + vect.y * vect.y);
			vect.x *= dim / abs;
			vect.y *= dim / abs;
			normal.x = -vect.y / 2;
			normal.y =  vect.x / 2;
			iPoint.x = from.x + vect.x;
			iPoint.y = from.y + vect.y;
			
			// draw rhombus
			ctx.beginPath();
			ctx.moveTo(from.x, from.y);
			ctx.lineTo(iPoint.x + normal.x, iPoint.y + normal.y);
			from.x = iPoint.x + vect.x;
			from.y = iPoint.y + vect.y;
			ctx.lineTo(from.x, from.y);
			ctx.lineTo(iPoint.x - normal.x, iPoint.y - normal.y);
			ctx.closePath();
			ctx.stroke();
			
			// draw final line
			$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 0, dim, dim, dim);
		
			// add the label
			var label = adj.data.$label;
			if(label != undefined && canvas.showLabels){
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	'bpmn_edge_standard': {
		'render': function(adj, canvas) {
			
			// draw sequence flow edge
			BPMN_Edges.bpmn_edge_sequence.render(adj, canvas);
			
			var inverse = adj.data.$direction[0] != adj.nodeFrom.id,
				dim = adj.Edge.dim,
				bend = adj.data.$bendPoints,
			 	posFrom = adj.nodeFrom.pos.getc(true),
			 	posTo = adj.nodeTo.pos.getc(true),
			 	from = {"x": posFrom.x, "y" : posFrom.y},
			 	to = {"x": posTo.x, "y" : posTo.y};
			
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
			
			// calculate some stuff
			var vect = {'x' : 0, 'y' : 0}, 
				normal = {'x' : 0, 'y' : 0}, 
				iPoint = {'x' : 0, 'y' : 0};
			
			if(bend && bend.length > 0){
				vect.x = bend[0].x - from.x;
				vect.y = bend[0].y - from.y;
			}else{
				vect.x = to.x - from.x;
				vect.y = to.y - from.y;
			}
			abs = Math.sqrt(vect.x * vect.x + vect.y * vect.y);
			vect.x *= dim / abs;
			vect.y *= dim / abs;
			normal.x = -vect.y / 2;
			normal.y =  vect.x / 2;
			iPoint.x = from.x + vect.x;
			iPoint.y = from.y + vect.y;
			
			// draw 
			normal.x -= vect.x / 2;
			normal.y -= vect.y / 2;
			
			var ctx = canvas.getCtx();
			ctx.moveTo(iPoint.x + normal.x, iPoint.y + normal.y);
			ctx.lineTo(iPoint.x - normal.x, iPoint.y - normal.y);
			ctx.stroke();
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	'bpmn_edge_data': {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints,
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
			var	toBend;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, toBend, 2, dim);
					from = bend[b];
				}
			}
			
			// draw final line
			$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 2, dim, dim, dim);
		
			// add the label
			var label = adj.data.$label;
			if(label != undefined && canvas.showLabels){
				var ctx = canvas.getCtx();
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	'bpmn_edge_annotation': {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints,
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
			var	toBend;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, toBend, 1, dim);
					from = bend[b];
				}
			}
			
			// draw final line
			$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 1, dim);
		
			// add the label
			var label = adj.data.$label;
			if(label != undefined && canvas.showLabels){
				var ctx = canvas.getCtx();
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	'bpmn_edge_message': {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints,
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
			var ctx = canvas.getCtx();
			var	toBend;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					ctx.moveTo(from.x, from.y);
					ctx.lineTo(toBend.x, toBend.y);
					from = bend[b];
				}
			}
			
			// draw final line
			$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 0, dim, dim, dim);
		
			// add the label
			var label = adj.data.$label;
			if(label != undefined && canvas.showLabels){
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		'contains': $jit.GraphFlow.EdgeHelper.contains
	},
	
	'bpmn_edge' : {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
	            inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				condition = data.$condition,
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
			
			var ctx = canvas.getCtx();
			ctx.beginPath();
			ctx.fillStyle = data.$color;

			ctx.moveTo(from.x, from.y);
			
			// calculate points for the arrowhead and symbols
			
			var bend = adj.data.$bendPoints;
			var vect = {'x' : 0, 'y' : 0}, 
				normal = {'x' : 0, 'y' : 0}, 
				iPoint = {'x' : 0, 'y' : 0};
	        
	        // calculate some stuff for drawing the rhombus
			if(bend && bend.length > 0){
				vect.x = bend[0].x - from.x;
				vect.y = bend[0].y - from.y;
			}else{
				vect.x = to.x - from.x;
				vect.y = to.y - from.y;
			}
			abs = Math.sqrt(vect.x * vect.x + vect.y * vect.y);
			vect.x *= dim / abs;
			vect.y *= dim / abs;
			normal.x = -vect.y / 2;
			normal.y =  vect.x / 2;
			iPoint.x = from.x + vect.x;
			iPoint.y = from.y + vect.y;
			
			
			switch(condition){
				case 1: // conditional flow
					
					ctx.lineTo(iPoint.x + normal.x, iPoint.y + normal.y);
					from.x = iPoint.x + vect.x;
					from.y = iPoint.y + vect.y;
					ctx.lineTo(from.x, from.y);
					ctx.lineTo(iPoint.x - normal.x, iPoint.y - normal.y);
					ctx.closePath();
					ctx.stroke();
					ctx.beginPath();
					break;
				case 2: // standard flow
					normal.x -= vect.x / 2;
					normal.y -= vect.y / 2;
					ctx.moveTo(iPoint.x + normal.x, iPoint.y + normal.y);
					ctx.lineTo(iPoint.x - normal.x, iPoint.y - normal.y);
					break;
				default: // sequence flow
			}
			
			// draw potential bend points
			var	toBend;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					ctx.moveTo(from.x, from.y);
					ctx.lineTo(toBend.x, toBend.y);
					from = bend[b];
				}
			}
			
			// draw arrow head
			vect.x = to.x - from.x;
			vect.y = to.y - from.y;
			abs = Math.sqrt(vect.x * vect.x + vect.y * vect.y);
			vect.x *= dim / abs;
			vect.y *= dim / abs;
			normal.x = -vect.y / 2;
			normal.y =  vect.x / 2;
			iPoint.x = to.x - vect.x;
			iPoint.y = to.y - vect.y;
			
			var x1 = iPoint.x + normal.x,
	        y1 = iPoint.y + normal.y,
	        x2 = iPoint.x - normal.x,
	        y2 = iPoint.y - normal.y;
			
	        ctx.moveTo(from.x, from.y);
	        ctx.lineTo(to.x, to.y);
	        ctx.stroke();
	        
	        ctx.beginPath();
	        ctx.moveTo(x1, y1);
	        ctx.lineTo(x2, y2);
	        ctx.lineTo(to.x, to.y);
	        ctx.closePath();
	        ctx.fill();
			ctx.stroke();
			
			
			
			// add the label
			var label = adj.data.$label;
			if(label != undefined && canvas.showLabels){
				ctx.font = (1.23 * dim)+"px Arial";
					
				var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
					midY = (from.y + to.y - dim) / 2;
			}
		},
		
		'contains': $jit.GraphFlow.EdgeHelper.contains
	}
};

// register defined objects for the GraphFlow
$jit.GraphFlow.Plot.NodeTypes.implement(BPMN_Objects);
$jit.GraphFlow.Plot.EdgeTypes.implement(BPMN_Edges);

        
