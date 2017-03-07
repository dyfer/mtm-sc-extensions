RotaryView : ValueView {

	// variables to be use by this class which don't need getters
	var innerRadiusRatio, outerRadiusRatio, boarderPx, <boarderPad;
	var stValue, stInput, >clickMode;

	// create variables with getters which you want
	// the drawing layers to access
	var <direction, <orientation, <bipolar, <startAngle, <sweepLength;
	var <prCenterAngle, <centerNorm, <centerValue;
	var <bnds, <cen, <maxRadius, <innerRadius, <outerRadius, <wedgeWidth;  // units: pixels, set in drawFunc
	var <dirFlag; 				// cw=1, ccw=-1
	var <prStartAngle;		// start angle used internally, reference 0 to the RIGHT, as used in addAnnularWedge
	var <prSweepLength; 	// sweep length used internally, = sweepLength * dirFlag
	var <levelSweepLength;
	var <majTicks, <minTicks, majTickVals, minTickVals;

	// drawing layers. Add getters to get/set individual properties by '.p'
	var <range, <level, <text, <ticks, <handle;



	*new {
		|parent, bounds, spec, initVal, startAngle=0, sweepLength=2pi, innerRadius=0, outerRadius=1|
		^super.new(parent, bounds, spec, initVal).init(startAngle, sweepLength, innerRadius, outerRadius);
	}


	init {
		|argStartAngle, argSweepLength, argInnerRadiusRatio, argOuterRadiusRatio|

		// REQUIRED: in subclass init, initialize drawing layers

		// initialize layer classes and save them to vars
		#range, level, text, ticks, handle = [
			RotaryRangeLayer, RotaryLevelLayer, RotaryTextLayer,
			RotaryTickLayer, RotaryHandleLayer
		].collect({
			|class|
			class.new(this, class.properties)
		});

		layers = [range, level, text, ticks, handle];

		innerRadiusRatio = argInnerRadiusRatio;
		outerRadiusRatio = argOuterRadiusRatio;
		startAngle = argStartAngle;		// reference 0 is UP
		sweepLength = argSweepLength;
		direction = \cw;
		dirFlag = 1;
		orientation = \vertical;
		wrap = false;
		clickMode = \relative;			// or \absolute
		boarderPad = 1;
		boarderPx = boarderPad;

		// intialize pixel unit variables
		maxRadius = this.bounds.width/2;
		outerRadius = maxRadius*outerRadiusRatio;
		innerRadius = maxRadius*innerRadiusRatio;
		wedgeWidth = outerRadius-innerRadius;

		bipolar = false;
		centerValue = spec.minval+spec.range.half;
		centerNorm = spec.unmap(centerValue);

		// valuePerPixel = spec.range / 200; // for interaction: movement range in pixels to cover full spec range
		// valuePerRadian = spec.range / sweepLength;

		majTicks = [];
		minTicks = [];
		majTickVals = [];
		minTickVals = [];

		this.defineMouseActions;
		this.direction_(direction);  // this initializes prStarAngle and prSweepLength
	}

	drawFunc {
		^{|v|
			// "global" instance vars, accessed by ValueViewLayers
			bnds = v.bounds;
			cen  = bnds.center;
			maxRadius = min(cen.x, cen.y) - boarderPx;
			innerRadius = maxRadius * innerRadiusRatio;
			wedgeWidth = outerRadius - innerRadius;
			levelSweepLength = if (bipolar,{input - centerNorm},{input}) * prSweepLength;
			this.drawInThisOrder;
		}
	}

	drawInThisOrder {
		if (range.p.show and: range.p.fill) {range.fill};
		if (level.p.show and: level.p.fill) {level.fill};
		if (ticks.p.show) {ticks.fill; ticks.stroke};
		if (range.p.show and: range.p.stroke) {range.stroke};
		if (level.p.show and: level.p.stroke) {level.stroke};
		if (handle.p.show) {handle.fill; handle.stroke};
		if (text.p.show) {text.fill; text.stroke};
	}

	defineMouseActions {

		// assign action variables: down/move
		mouseDownAction = {
			|v, x, y|
			// mouseDownPnt = x@y; // set for moveAction
			stValue = value;
			stInput = input;
			if (clickMode=='absolute') {this.respondToAbsoluteClick};
		};

		mouseMoveAction  = {
			|v, x, y|
			switch (orientation,
				\vertical, {this.respondToLinearMove(mouseDownPnt.y-y)},
				\horizontal, {this.respondToLinearMove(x-mouseDownPnt.x)},
				\circular, {this.respondToCircularMove(x@y)}
			);
		};
	}

	respondToLinearMove {|dPx|
		if (dPx != 0) {
			this.valueAction_(stValue + (dPx * valuePerPixel))
		};
		// this.refresh;
	}

	// radial change, relative to center
	respondToCircularMove {|mMovePnt|
	/*	var stPos, endPos, stRad, endRad, dRad, delta;
		stPos = (mouseDownPnt - cen);
		stRad = atan2(stPos.y,stPos.x);
		endPos = (mMovePnt - cen);
		endRad = atan2(endPos.y, endPos.x);
		delta = endRad - stRad;
		// dRad = delta.fold(0, pi) * dirFlag * delta.sign;
		dRad = delta.fold(0, pi) * dirFlag * delta.isPositive.if({1},{-1});
		postf("st: %, end: %, delta: %, delta_mapped: %\n",stRad, endRad, delta, dRad);
		if (dRad !=0) {
			this.input_(stInput + (dRad/sweepLength));			// triggers refresh
			this.doAction;
		};
		// allow continuous updating of relative start point
		mouseDownPnt = mMovePnt;
		stValue = value;
		stInput = input;*/
		var pos, rad, radRel;
		pos = (mMovePnt - cen);
		rad = atan2(pos.y,pos.x);								// radian position, relative 0 at 3 o'clock
		radRel = rad + 0.5pi * dirFlag;							//  " relative 0 at 12 o'clock, clockwise
		radRel = (radRel - (startAngle*dirFlag)).wrap(0, 2pi);	//  " relative to start position
		if (radRel.inRange(0, sweepLength)) {
			this.inputAction_(radRel/sweepLength); // triggers refresh
			stValue = value;
			stInput = input;
		};
	}

	respondToAbsoluteClick {
		var pos, rad, radRel;
		pos = (mouseDownPnt - cen);
		rad = atan2(pos.y,pos.x);								// radian position, relative 0 at 3 o'clock
		radRel = rad + 0.5pi * dirFlag;							//  " relative 0 at 12 o'clock, clockwise
		radRel = (radRel - (startAngle*dirFlag)).wrap(0, 2pi);	//  " relative to start position
		if (radRel.inRange(0, sweepLength)) {
			this.inputAction_(radRel/sweepLength); // triggers refresh
			stValue = value;
			stInput = input;
		};
	}

	/* Orientation and Movement */

	direction_ {|dir=\cw|
		direction = dir;
		dirFlag = switch (direction, \cw, {1}, \ccw, {-1});
		this.startAngle_(startAngle);
		this.sweepLength_(sweepLength); // updates prSweepLength
		this.refresh;
	}

	startAngle_ {|radians=0|
		startAngle = radians;
		prStartAngle = -0.5pi + startAngle;						// start angle always relative to 0 is up, cw
		this.setPrCenter;
		this.ticksAtValues_(majTickVals, minTickVals, false);	// refresh the list of maj/minTicks positions
	}

	setPrCenter {
		prCenterAngle = -0.5pi + startAngle + (centerNorm*sweepLength*dirFlag);
		this.refresh;
	}

	centerValue_ {|value|
		centerValue = spec.constrain(value);
		centerNorm = spec.unmap(centerValue);
		this.setPrCenter;
	}

	sweepLength_ {|radians=2pi|
		sweepLength = radians;
		prSweepLength = sweepLength * dirFlag;
		// valuePerRadian = spec.range / sweepLength;
		this.setPrCenter;
		this.ticksAtValues_(majTickVals, minTickVals, false); // refresh the list of maj/minTicks positions
		// this.refresh;
	}

	orientation_ {|vertHorizOrCirc = \vertical|
		orientation = vertHorizOrCirc;
	}

	innerRadiusRatio_ {|ratio|
		innerRadiusRatio = ratio;
		this.refresh
	}

	outerRadiusRatio_ {|ratio|
		outerRadiusRatio = ratio;
		this.refresh
	}

	bipolar_ {|bool|
		bipolar = bool;
		this.refresh;
	}

/* NO LONGER NECESSARY NOW THAT outerRadius can be defined
	boarderPad_ { |px|
		var align, overhang, alignRadius;
		align = handle.align;
		boarderPad = px;
		boarderPx = if (handle.type==\line)
		{boarderPad}
		{
			if (align==\outside)
			{boarderPx + handle.radius}
			{
				if (align.isKindOf(Number))
				{
					overhang = max(0, ((wedgeWidth * align) + (handle.radius)) - wedgeWidth);
					overhang + boarderPad
				}
				{boarderPad}
			}
		};
	}
*/
	/* Ticks */

	// showTicks_ {|bool|
	// 	showTicks = bool;
	// 	this.refresh;
	// }
	//
	// majorTickRatio_ {|ratio = 0.25|
	// 	majorTickRatio = ratio;
	// 	this.refresh;
	// }
	//
	// minorTickRatio_ {|ratio = 0.15|
	// 	minorTickRatio = ratio;
	// 	this.refresh;
	// }
	//
	// // \inside, \outside, \center
	// tickAlign_ {|insideOutSideCenter|
	// 	case
	// 	{
	// 		(insideOutSideCenter == \inside) or:
	// 		(insideOutSideCenter == \outside) or:
	// 		(insideOutSideCenter == \center)
	// 	} {
	// 		tickAlign = insideOutSideCenter;
	// 		this.refresh;
	// 	}
	// 	{ "Rotary:tickAlign_ : Invalid align argument. Must be 'inside', 'outside' or 'center'".warn }
	// }

	// arrays of radian positions, reference from startAngle
	ticksAt_ {|majorRadPositions, minorRadPositions, show=true|
		majTicks = majorRadPositions;
		minTicks = minorRadPositions;
		majTickVals = spec.map(majTicks / sweepLength);
		minTickVals = spec.map(minTicks / sweepLength);
		// show.if{showTicks = true};
		show.if{ticks.show = true};
		this.refresh;
	}

	// ticks at values unmapped by spec
	ticksAtValues_ {|majorVals, minorVals, show=true|
		majTicks = spec.unmap(majorVals)*sweepLength;
		minTicks = spec.unmap(minorVals)*sweepLength;
		majTickVals = majorVals;
		minTickVals = minorVals;
		// show.if{showTicks = true};
		show.if{ticks.show = true};
		this.refresh;
	}

	// ticks values by value hop, unmapped by spec
	ticksEveryVal_ {|valueHop, majorEvery=2|
		var num, ticks, numMaj, majList, minList;
		num = (spec.range / valueHop).floor.asInt;
		ticks = num.collect{|i| spec.unmap(i * valueHop) * sweepLength};
		numMaj = num/majorEvery;
		majList = List(numMaj);
		minList = List(num-numMaj);
		ticks.do{|val, i| if ((i%majorEvery) == 0) {majList.add(val)} {minList.add(val)} };
		this.ticksAt_(majList, minList);
		this.refresh;
	}


	ticksEvery_ {|radienHop, majorEvery=2|
		this.refresh;
	}

	// evenly distribute ticks
	numTicks_ {|num, majorEvery=2, endTick=true|
		var hop, ticks, numMaj, majList, minList;
		hop = if (endTick) {sweepLength / (num-1)} {sweepLength / num};
		// drawNum = if (sweepLength==2pi) {num-1} {num}; // don't draw overlaying ticks in the case of full circle
		ticks = num.asInt.collect{|i| i * hop};
		numMaj = num/majorEvery;
		majList = List(numMaj);
		minList = List(num-numMaj);
		ticks.do{|val, i| if ((i%majorEvery) == 0) {majList.add(val)} {minList.add(val)} };
		this.ticksAt_(majList, minList);
	}
}
