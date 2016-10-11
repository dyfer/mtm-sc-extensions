PlayControlView1 {
	// copyArgs
	var <path, <server;
	
	var <player;

	var <win, <waveView, <channelLabelView, <controlMainView, <playControlView, <displayControlView, <soundFileView;
	var processWave, loadFile, displayWaveform, free, updateDisplay, setDisplayYZoomFromArray, setDisplayYZoom, updateNChToDisplay; //functions
	// var filePath; //controlrate file to read
	var numChannels, numFrames, sampleRate;
	var tempInPath, tempOutPath, tempFileReady, duration, fs_Hz, prefix;
	var loadButt, playButt, stopButt, resetButt, playPosTxt;
	var nChToDisplay = 1; //should be updated anyway
	var player, displayBufferArray; //do I even need the display buffer here?
	var dispLF, dispHF, dispDCremove = true, dispYZoom;
	var progress, text, progressPercent, progressLabel, progressText; //for progress display; progress 0-1
	// var server;
	var <livePlotter, plotting=false, prevPlotWinBounds, prevPlotBounds, overlayOutputPlot = false; //stolen from Mike
	var plotBut, timeLabel, segmentLabel;
	var yZoomNumber, numChNumber;

	*new {|path, server|
		^super.newCopyArgs(path, server).init
	}

	init {
		server = server ?? Server.default;

		//set vals
		// dispDCremove = true;
		// filePath; //you can autoload file here
		// server = s; //should be booted!
		//-------

		//init
		prefix = "".resolveRelative;
		tempOutPath = prefix ++ "tmpout.wav".postln;

		this.prepareWindow;

		path !? {this.loadFile}
	}

	update {| who, what ... args |
		// what.postln;
		// args.postln;
		// if( who == player, {	// we're only paying attention to one thing, but just in case we check to see what it is
		switch ( what )
		{ \playhead }
		{
			{
				soundFileView.timeCursorPosition_(player.playhead);
				//time display here as well
				timeLabel.string_((player.playhead / (player.buffer.sampleRate / Server.default.options.blockSize)).asTimeString);
				// player.playhead.postln;
				// soundFileView.viewFrames.postln;
				// "(soundFileView.scrollPos * numFrames * 0.9): ".post; (soundFileView.scrollPos * numFrames * 0.9).postln;
				if(player.playhead > (soundFileView.viewFrames + (soundFileView.scrollPos * soundFileView.numFrames * 0.9)), {
					soundFileView.scroll(0.8)
				});	
			}.defer;
		}		
		{ \play }
		{
			// args.postln;
			playButt.value_(args[0].asInteger);
			//button control = play/stop
			//player.isPlaying;
		}
		{ \rate }
		{
			// ......
		}
		{
			\progress
		}
		{
			progress = args[0];
			text = args[1] ?? text;
			{
				var newColor;
				progressLabel.string_(text.asString);
				progressPercent.string_((progress * 100).asInteger.asString ++ "%");
				if(progress < 1, {
					newColor = Color.hsv(0.33, 0.5, 1, 1);
				}, {
					newColor = Color.new(0, 0, 0, 0);
					// {
					// 	if(progress == 1, {
					// 		progressLabel.string_(""); //clear string
					// 	})
					// }.defer(2);
				});
				if(progressLabel.background != newColor, {
					progressLabel.background = newColor;
				})
			}.defer;
		}
		;
		// });
	}

	play {
		player.play;
	}

	stop {
		player.stop;
	}

	reset {//how?
		player.reset; //is this working right? yes, I think
	}

	resetPos_ {|frame|
		player.resetPos_(frame);
	}

	prepareWindow {
		win = Window.new("ControlRate player for EEG signals", Window.screenBounds).front;
		win.layout = VLayout();
		win.onClose = {this.free};//free on close
		// waveView = View(win);
		// w = waveView;
		// waveView.background = Color.hsv(0.5, 0, 0.8); //just to see where it is
		soundFileView = SoundFileViewLabels(win);
		soundFileView.gridOn = false;
		soundFileView.timeCursorOn = true;
		soundFileView.action = {|sfv|
			// sfv.timeCursorPosition.postln;
			player !? {
				player.resetPos_(sfv.timeCursorPosition);
				player.reset;
				
			};
		};
		soundFileView.mouseMoveAction = {|sfv| //move to separate method, call after allocating... should be maybe moved to onZoomChange of SFVLabels?
			{
				var str;
				str = ((soundFileView.scrollPos * numFrames) / (player.buffer.sampleRate / Server.default.options.blockSize)).asTimeString
				++ " - " ++
				(((soundFileView.scrollPos * numFrames) + soundFileView.numFrames) / (player.buffer.sampleRate / Server.default.options.blockSize)).asTimeString;
				// str.postln;
				segmentLabel.string_(str);
			}.defer;
		};
		//basic keyboard control
		soundFileView.view.keyDownAction_({|doc, char, mod, unicode, keycode, key|
			player !? {
				if(unicode == 32, {//space
					if(player.isPlaying, {
						player.stop;
					}, {
						player.play;
					});
				})
			}
		});
		// soundFileView.soundFileView.metaAction = {|sfv|
		// 	sfv.postln;
		// };
		// waveView.onResize = {|view|
		// 	soundFileView !? {
		// 		soundFileView.bounds = Rect(soundFileView.bounds.left, soundFileView.bounds.top, view.bounds.width, view.bounds.height * (player.buffer.numChannels / nChToDisplay));
		// 	}
		// };
		controlMainView = View.new(win).fixedHeight_(120);
		controlMainView.layout = HLayout();
		playControlView = View(controlMainView);
		displayControlView = View(controlMainView);
		playControlView.layout = HLayout(
			VLayout(//load
				HLayout(//playcontrols
					playButt = Button().states_([["Stopped"],["Playing"]]).action_({|butt|
						if(butt.value.asBoolean, {
							this.play;
						},{
							this.stop;
						});
					}),
					// stopButt = Button().states_([["Stop"]]).action_({this.stop}),
					resetButt = Button().states_([["Reset"]]).action_({this.resetPos_(0); this.reset;}),

				),
				HLayout(
					loadButt = Button().states_([["Load file"]]).action_({Dialog.openPanel({|path|this.loadFile(path)}, {"loading cancelled".postln}, false)}).maxWidth_(120),
					progressLabel = StaticText().string_("status").fixedWidth_(200),
					progressPercent = StaticText(),
					nil,
				)
			),
			VLayout(//time display, also selection!
				HLayout(
					plotBut = Button().states_([["Plot Signal"],["Close Plot"]]).action_({
						|but|
						but.value.asBoolean.if(
							{this.plot},
							{livePlotter !? {livePlotter.mon.plotter.parent.close} }
						)
					}),
					nil,
					StaticText().string_("Current time: "),
					timeLabel = StaticText().fixedWidth_(220),
					nil,
					StaticText().string_("Current window: "),
					segmentLabel = StaticText().fixedWidth_(220),
				),
				HLayout(
					VLayout(
						HLayout(
							StaticText().string_("Waveform vertical zoom: ").minWidth_(200),
							yZoomNumber = NumberBox().action_({|num|
								soundFileView.yZoom_(num.value)
							}).clipLo_(0.0).maxDecimals_(4).maxWidth_(40),
							nil
						),
						HLayout(
							StaticText().string_("# of channels vertically: ").minWidth_(200),
							numChNumber = NumberBox().action_({|num|
								soundFileView.heightChannels_(num.value.asInteger)
							}).clipLo_(1).step_(1).maxDecimals_(3).maxWidth_(40),
							nil
						),
					),
					StaticText().string_("Controls: right click + drag to scroll,\nright click + shift + drag to zoom"),
					nil,
				)
			)
		);
	}

	allocSFV {|numFramesArg, numChannelsArg, sampleRateArg|
		soundFileView.alloc(numFramesArg, numChannelsArg, sampleRateArg);
		yZoomNumber.value_(soundFileView.yZoom);
		numChNumber.value_(soundFileView.numChannels).clipHi_(soundFileView.numChannels);
	}
	
	updateDisplay {
		this.processWave(actionWhenDone: {|buf|
			var displayNeedsAllocating;
			if((soundFileView.numChannels == buf.numChannels) && (soundFileView.numFrames == buf.numFrames), {
				//note, SoundFileViewLabels (used here) reports proper number of channels; SoundFileView does not!
				displayNeedsAllocating = false
			}, {
				displayNeedsAllocating = true;				
			});
			// displayBuffer.free;
			// displayBuffer = buf;
			buf.loadToFloatArray(action: {|arr|
				displayBufferArray = arr; 
				numChannels = buf.numChannels;
				// nChToDisplay = numChannels;
				numFrames = buf.numFrames; //this might be different than the original?
				sampleRate = buf.sampleRate;
				// "here".postln;
				{
					if(displayNeedsAllocating, {
						this.allocSFV(numFrames, numChannels, sampleRate/Server.default.options.blockSize); //note, sampleRate might need adjusting...
					});
					soundFileView.set(0, displayBufferArray);
					// this.setDisplayYZoomFromArray(displayBufferArray); //we're normalizing anyway...?
					// buf.free; //we don't need buffer anymore
					// v = soundFileView; //temp
					// b = buf; //temp for checking
					//free buffer here?
				}.defer; //needs defering?
			});
		});
	}
	
	// setDisplayYZoomFromArray {|array|
	// 	var min, max, scaler;
	// 	min = array.minItem;
	// 	max = array.maxItem;
	// 	scaler = min.max(max).reciprocal;
	// 	setDisplayYZoom.(scaler);
	// }

	// setDisplayYZoom {|val|
	// 	dispYZoom = val;
	// 	soundFileView.yZoom = dispYZoom;
	// }

	
	loadFile {|argPath|
		path = argPath ?? path;
		path.notNil.if({
			player = PlayControl(path, false, server, {
				this.updateDisplay;
			});
			player.addDependant(this);
			this.update(player, \progress, 0, "loading");
		}, {
			"no valid path provided".warn;
		});
	}
	
	free {
		// displayBuffer.free;
		player.removeDependant(this);
		player.free;
	}

	processWave {arg loFreq = dispLF, hiFreq = dispHF, removeDC = dispDCremove, fadeTime = 0, channelToExtract, pathIn = path, pathOut = tempOutPath, normalizeOutput = true, argserver = server, actionWhenDone = {}; //action when done is passed a buffer with processed file
		var numCh, sf, options, score, processedOutputPath, oscFilePath, numChOut, synthdef;
		this.update(player, \progress, 0, "preparing display");
		oscFilePath = prefix ++ "csvToWav";
		"pathIn: ".post; pathIn.postln;
		sf = SoundFile.openRead(pathIn);
		numCh = sf.numChannels;
		fs_Hz = sf.sampleRate;
		"channelToExtract: ".post; channelToExtract.postln;
		if(channelToExtract.notNil,{
			numChOut = 1;
		}, {
			numChOut = numCh;
		});
		duration = sf.duration;
		sf.close;
		// processedOutputPath = outputPath ++ "_filt" ++ loFreq ++ "_" ++ hiFreq ++ ".wav";
		processedOutputPath = pathOut;
		synthdef = SynthDef(\filtered, {//arg loFreq = 7, hiFreq = 13;
			var sig, filtSig, env, sigChosen;
			sig = SoundIn.ar(numCh.collect({|inc| inc})); //from NRT in
			// sig = SoundIn.ar([0]); //from NRT in??
			// rq = bw/freq
			// freq = (lo+hi)/2
			// bw = hi-lo
			// -> rq = (hi - lo) / ((lo+hi)/2)
			if(channelToExtract.isKindOf(SimpleNumber), {
				sigChosen = sig[channelToExtract];
			}, {
				sigChosen = sig;
			});
			
			if(removeDC, {
				sigChosen = LeakDC.ar(sigChosen);
			});
			if(fadeTime > 0, {	
				env = EnvGen.ar(Env([0, 1, 1, 0], [fadeTime, duration-(2*fadeTime), fadeTime], \sin));
				sigChosen = sigChosen * env;
			});
			if(loFreq.notNil && hiFreq.notNil, {
				sigChosen = BPF.ar(sigChosen, (loFreq+hiFreq)/2, (hiFreq - loFreq) / ((loFreq+hiFreq)/2));
			});
			Out.ar(0, sigChosen);
		});//.load;
		"synth loaded?".postln;
		//is defer needed for the below???
		// {
		score = [
			[0, [\d_recv, synthdef.asBytes]],
			[0, [\s_new, \filtered, 1000, 0, 0]],
			[duration, [\c_set, 0, 0]] // finish
		];
		options = ServerOptions.new.numInputBusChannels_(numCh).numOutputBusChannels_(numChOut).sampleRate_(fs_Hz); // mono output
		this.update(player, \progress, 0.3, "rendering file");
		Score.recordNRT(score, oscFilePath, processedOutputPath, pathIn, fs_Hz, "WAV", "float", options, "", duration, {
			var pathToReadToBuffer, normPath;
			"Writing file done!".postln;
			File.delete(oscFilePath);
			if(normalizeOutput, {
				"normalizing...".postln;
				this.update(player, \progress, 0.6, "normalizing");
				normPath = processedOutputPath ++ "_norm.wav";
				pathToReadToBuffer = normPath;
				SoundFile.normalize(processedOutputPath, normPath, linkChannels: false, threaded: false); //no threading to wait for the result
				"normalizing finished!".postln;
			}, {
				pathToReadToBuffer = processedOutputPath;
			});
			"Reading into buffer".postln;
			"tempOutPath: ".post; tempOutPath.postln;
			this.update(player, \progress, 0.9, "updating display");
			Buffer.read(argserver, pathToReadToBuffer, startFrame: 0, numFrames: -1, action: {|buf|
				"Reading into buffer done".postln;
				{actionWhenDone.(buf); File.delete(tempOutPath); this.update(player, \progress, 1, "done");}.defer;
				
			});
			// tempFileReady = true
		}); // synthesize
		// }.defer(1);
	}

	plot { |overlay|
		overlay !? {overlayOutputPlot = overlay};

		if (livePlotter.isNil) {
			var win;
			livePlotter = ControlPlotter(player.busnum, player.numChannels, 200, 25, \linear, overlayOutputPlot);

			win = livePlotter.mon.plotter.parent;

			prevPlotWinBounds !? { win.bounds_(prevPlotWinBounds) };
			prevPlotBounds !? {livePlotter.bounds_(*prevPlotBounds)};
			overlayOutputPlot.if{livePlotter.plotColors_(player.numChannels.collect{Color.rand(0.3, 0.7)})};
			livePlotter.start;
			plotting = true;

			win.view.onMove_({|v| prevPlotWinBounds= v.findWindow.bounds });
			win.view.onResize_({|v| prevPlotWinBounds= v.findWindow.bounds });
			plotBut.value_(1);

			livePlotter.mon.plotter.parent.onClose_({ |me|
				livePlotter = nil;
				plotting = false;
				plotBut.value_(0);
			})
		} {
			livePlotter.mon.plotter.parent.front;
		};
	}
}