SoundFileViewLabels {
	var <soundFileView;
	var <enclosingMasterView;
	var <enclosingScrollView;
	var <channelLabelView;
	var <gridLabelView;
	var <channelLabelWidth = 30;
	var <gridLabelHeight = 0;
	var <numChannels;
	var <numChannelsToDisplay;
	// var soundfile; //for SoundFileView
	var <labelStrings;
	var <mouseMoveAction;
	*new { arg parent, bounds;
		^super.new.init(parent, bounds);
		
	}

	init {|parent, bounds|
		var widthDiminisher = -10; //negativem for preventing horizontal scroll
		//workaround for placing without bounds?
		bounds = bounds ?? parent.bounds;
		enclosingMasterView = View(parent, bounds).minWidth_(channelLabelWidth * 2).minHeight_(gridLabelHeight + 10);
		enclosingScrollView = ScrollView(enclosingMasterView, Rect(0, gridLabelHeight, bounds.width, bounds.height - gridLabelHeight));
		enclosingScrollView.hasHorizontalScroller = false; //don't show horizontal scroll
		// w = waveView;
		enclosingScrollView.background = Color.hsv(0.5, 0, 0.8); //just to see where it is
		gridLabelView = View(enclosingMasterView, Rect(channelLabelWidth, 0, bounds.width - channelLabelWidth + widthDiminisher, gridLabelHeight));
		gridLabelView.background = Color.rand;
		soundFileView = SoundFileView(enclosingScrollView, Rect(channelLabelWidth, gridLabelHeight, bounds.width - channelLabelWidth + widthDiminisher, bounds.height - gridLabelHeight));
		soundFileView.mouseMoveAction_({mouseMoveAction !? {mouseMoveAction.(this)}});
		soundFileView.gridOn_(false);//because it's buggy as of 08.2016
		channelLabelView = View.new(enclosingScrollView, Rect(0, 0, channelLabelWidth, bounds.height - gridLabelHeight));
		channelLabelView.layout = VLayout.new;
		enclosingMasterView.onResize = {|view|
			var allChannelsHeight;
			// "view.bounds.width: ".post; view.bounds.width.postln;
			numChannels !? {
				numChannelsToDisplay = numChannelsToDisplay ?? numChannels;
				allChannelsHeight = ( view.bounds.height - gridLabelHeight) * (numChannels / numChannelsToDisplay);
			};
			allChannelsHeight = allChannelsHeight ?? ( view.bounds.height - gridLabelHeight); //before data is present
			enclosingScrollView.bounds = Rect(0, gridLabelHeight, view.bounds.width, view.bounds.height - gridLabelHeight);
			// soundFileView !? {
			// "Rect(channelLabelWidth, 0, view.bounds.width - channelLabelWidth, allChannelsHeight): ".post; Rect(channelLabelWidth, 0, view.bounds.width - channelLabelWidth, allChannelsHeight).postln;
			soundFileView.bounds = Rect(channelLabelWidth, 0, view.bounds.width - channelLabelWidth, allChannelsHeight);
			// "soundFileView.bounds: ".post; soundFileView.bounds.postln;
			// }
			channelLabelView.bounds = Rect(0, 0, channelLabelWidth, allChannelsHeight);
			gridLabelView.bounds = Rect(channelLabelWidth, 0, view.bounds.width - channelLabelWidth, gridLabelHeight);
			//redraw grid here - for later
			//also redraw on mouse move...
			//also trigger soundFileView action here?
			soundFileView.doAction;
		};

	}

	// height { //waveform height in pixels; will not scale; not implemented for now
	// }

	heightChannels_ {|numCh|
		var allChannelsHeight;
		numChannelsToDisplay = numCh ?? numChannelsToDisplay;
		numChannelsToDisplay = numChannelsToDisplay ?? numChannels;
		//resize here
		allChannelsHeight = ( enclosingMasterView.bounds.height - gridLabelHeight) * (numChannels / numChannelsToDisplay);
		soundFileView.bounds = Rect(channelLabelWidth, 0, enclosingMasterView.bounds.width - channelLabelWidth, allChannelsHeight);
		channelLabelView.bounds = Rect(0, 0, channelLabelWidth, allChannelsHeight);
	}

	bounds_ {|argBounds| //this changes size anyway....
		enclosingMasterView.bounds = argBounds;
	}

	parent {^enclosingMasterView.parent}
	bounds {^enclosingMasterView.bounds}
	view {^enclosingMasterView}

	updateNumChannelsPath {|path|
		SoundFile.use(path, {|file| this.updateNumChannels(file.numChannels)});
	}

	updateNumChannelsSoundfile { //assumes soundfile is avaliable
		// SoundFile.use(path, {|file| this.updateNumChannels(file.numChannels)});
		soundFileView.soundfile.notNil.if({
			this.updateNumChannels(soundFileView.soundfile.numChannels)
		}, {
			"soundFileView.soundfile is nil".warn;
		});
	}

	soundfile_ {|thisSF|
		soundFileView.soundfile_(thisSF);
	}

	soundfile {
		^soundFileView.soundfile;
	}

	updateNumChannels {|argNumCh| //called after setting changes to the number of channels
		numChannels = argNumCh;
		channelLabelView.children.do({|thisOne| thisOne.remove}); //remove existing labels
		labelStrings = numChannels.collect({|inc| StaticText(channelLabelView).string_((inc + 1).asString)}); //in the future allow for different channel labels? also set adjust font sizes...
	}

	updateGrid { //on every resize, as well as SFV manipulation, to be implemented
	}

	//methods copied from SoundFileView...
	
	load { arg filename, startFrame, frames, block, doneAction;
		soundFileView.load(filename, startFrame, frames, block, {this.updateNumChannelsPath(filename); doneAction.(soundFileView)}); //what should be passed to doneAction?
	}

	alloc { arg frames, channels=1, samplerate=44100;
		soundFileView.alloc(frames, channels, samplerate);
		this.updateNumChannels(channels);
	}

	data_ { arg data;
		soundFileView.data_(data);
	}

	setData { arg data, block, startFrame=0, channels=1, samplerate=44100;
		soundFileView.setData(data, block, startFrame, channels, samplerate);
		this.updateNumChannels(channels);
	}

	set { arg offset=0, data;
		soundFileView.set(offset, data);
	}

	readFile { arg aSoundFile, startFrame, frames, block, closeFile, doneAction;
		soundFileView.readFile( aSoundFile, startFrame, frames, block, closeFile, {this.updateNumChannelsSoundfile; doneAction.()});
	}

	read { arg startFrame, frames, block, closeFile, doneAction;
		soundFileView.read( startFrame, frames, block, closeFile, {this.updateNumChannelsSoundfile; doneAction.(soundFileView)});
	}

	readFileWithTask { arg soundFile, startFrame, frames, block, doneAction, showProgress;
		soundFileView.readFileWithTask( startFrame, frames, block, {this.updateNumChannelsSoundfile; doneAction.(soundFileView)}, showProgress);
	}

	readWithTask { arg startFrame, frames, block, doneAction, showProgress;
		// this.read( startFrame, frames, block, nil, doneAction );
		soundFileView.readWithTask( startFrame, frames, block, {this.updateNumChannelsSoundfile; doneAction.(soundFileView)}, showProgress);
	}

	// drawsWaveForm { ^this.getProperty( \drawsWaveform ); }

	// drawsWaveForm_ { arg boolean; this.setProperty( \drawsWaveform, boolean ); }

	// waveColors { ^this.getProperty( \waveColors ) }
	// waveColors_ { arg colors; this.setProperty( \waveColors, colors ) }

	//// Info

	startFrame { ^soundFileView.startFrame}

	numFrames { ^soundFileView.numFrames}

	scrollPos { ^soundFileView.scrollPos} // a fraction of the full scrolling range

	viewFrames { ^soundFileView.viewFrames }

	// readProgress { ^this.getProperty( \readProgress ); }

	//// Navigation

	// zoom { arg factor; this.invokeMethod( \zoomBy, factor.asFloat ); }

	// zoomToFrac { arg fraction; this.invokeMethod( \zoomTo, fraction.asFloat ); }

	// zoomAllOut { this.invokeMethod( \zoomAllOut ); }

	// zoomSelection { arg selection;
	// 	if( selection.isNil ) { selection = this.currentSelection };
	// 	this.invokeMethod( \zoomSelection, selection );
	// }

	scrollTo { arg fraction; // a fraction of the visible range
		^soundFileView.scrollTo(fraction);
	}

	scroll { arg fraction; // a fraction of the visible range
		^soundFileView.scroll(fraction);
	}

	scrollToStart { this.invokeMethod( \scrollToStart ); }

	scrollToEnd { this.invokeMethod( \scrollToEnd ); }

	xZoom {^soundFileView.xZoom}

	xZoom_ { arg seconds; soundFileView.xZoom_(seconds)}

	yZoom {^soundFileView.yZoom}

	yZoom_ { arg factor; soundFileView.yZoom_(factor) }

	//// Selections

	// selections { ^this.getProperty( \selections ); }

	// currentSelection { ^this.getProperty( \currentSelection ); }

	// currentSelection_ { arg index; this.setProperty( \currentSelection, index ); }

	// selection { arg index; ^this.invokeMethod( \selection, index, true ); }

	// setSelection { arg index, selection;
	// 	this.invokeMethod( \setSelection, [index, selection] );
	// }

	// selectionStart { arg index;
	// 	var sel = this.selection( index );
	// 	^sel.at(0);
	// }

	// setSelectionStart { arg index, frame;
	// 	var sel = this.selection( index );
	// 	sel.put( 0, frame );
	// 	this.setSelection( index, sel );
	// }

	// selectionSize { arg index;
	// 	var sel = this.selection( index );
	// 	^sel.at(1);
	// }

	// setSelectionSize { arg index, frames;
	// 	var sel = this.selection( index );
	// 	sel.put( 1, frames );
	// 	this.setSelection( index, sel );
	// }

	// selectAll { arg index; this.setSelection( index, [0, this.numFrames] ); }

	// selectNone { arg index; this.setSelection( index, [0, 0] ); }


	// setEditableSelectionStart { arg index, editable; ^this.nonimpl("setEditableSelectionStart"); }

	// setEditableSelectionSize { arg index, editable; ^this.nonimpl("setEditableSelectionSize"); }

	// setSelectionColor { arg index, color; this.invokeMethod( \setSelectionColor, [index,color] ); }


	// selectionStartTime { arg index; ^this.nonimpl("selectionStartTime"); }

	// selectionDuration { arg index; ^this.nonimpl("selectionDuration"); }


	// readSelection { arg block, closeFile; ^this.nonimpl("readSelection"); }

	// readSelectionWithTask { ^this.nonimpl("readSelectionWithTask"); }

	// cursor

	timeCursorOn { ^soundFileView.timeCursorOn }
	timeCursorOn_ { arg flag; soundFileView.timeCursorOn_(flag) }

	timeCursorEditable { ^soundFileView.timeCursorEditable }
	timeCursorEditable_ { arg flag; soundFileView.timeCursorEditable_(flag) }

	timeCursorPosition { ^soundFileView.timeCursorPosition }
	timeCursorPosition_ { arg frame; soundFileView.timeCursorPosition_(frame) }

	// grid

	gridOn { ^soundFileView.gridOn }
	gridOn_ { arg flag; soundFileView.gridOn_(flag) }

	gridResolution { ^soundFileView.gridResolution }
	gridResolution_ { arg seconds; soundFileView.gridResolution_(seconds) }

	gridOffset { ^soundFileView.gridOffset }
	gridOffset_ { arg seconds; soundFileView.gridOffset_(seconds) }

	// colors

	// peakColor { ^this.getProperty(\peakColor) }
	// peakColor_ { arg color; this.setProperty(\peakColor, color) }

	// rmsColor { ^this.getProperty(\rmsColor) }
	// rmsColor_ { arg color; this.setProperty(\rmsColor, color) }

	// timeCursorColor { ^this.getProperty( \cursorColor ); }
	// timeCursorColor_ { arg color; this.setProperty( \cursorColor, color ) }

	// gridColor { ^this.getProperty( \gridColor ) }
	// gridColor_ { arg color; this.setProperty( \gridColor, color ) }

	// actions

	// metaAction_ { arg action;
	// 	this.manageFunctionConnection( metaAction, action, 'metaAction()' );
	// 	metaAction = action
	// }
	action_ {|actionArg|
		soundFileView.action_({actionArg.(this)});
	}
	action {soundFileView.action}
	mouseMoveAction_ {|actionArg|
		mouseMoveAction = actionArg;
	}
	// mouseMoveAction {^mouseMoveAction}
}
