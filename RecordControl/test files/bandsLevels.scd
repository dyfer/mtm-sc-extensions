///add plotter after DC offset
// link plotting ranges
// convert sampling rate in the SC files - using SND or other tools
// rate conversion playback
// scaler before -
//adjust lag on the fly



//open file later with gui, or provide path as an argument (see below)
~playCtrl = PlayControlView1();

// specify file path
~dataFile = "/Volumes/data/Dokumenty/2015-2016/Art&Brain research/recording/2016.08.04/00_160804_142639_kr_5minBaseline.WAV"
// eeg data file
// ~dataFile = "~/Files/Projects/Active/9e2/EEG_Spatial_Processing/OpenBCI-RAW-Dan_Adam_Test_1.txt_filter_None_allCh.wav";
// ~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/00_OpenBCI-RAW-2016-08-04_14-38-37_5minBaseline.txt_filter_None_allCh.wav";
// ~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/01_OpenBCI-RAW-2016-08-04_14-48-41_5minPinkNoise.txt_filter_None_allCh.wav";
~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/03_OpenBCI-RAW-2016-08-04_14-57-00_fieldRecording.txt_filter_None_allCh.wav"

//open player with the path argument
~playCtrl = PlayControlView1(~dataFile);
~playCtrl.player.playBus; //outbus of the player

OSCFunc.trace(false)
//---------
// start ~playCtrl first, load file
// then run code below
//--------
(
var dataBandAnalysis, dcRemoveSynth, synths, note, bus;
var win, lfKnob, hfKnob, lagScaleKnob, scaleKnob, chNum, plotButt, outbusNumButt;
var bandData, respCommand;
var rawEegBus;
var numCh, numBands;
var outBuses, outDCbuses, notes, notesDC, plotters, controlPlotters;
var responder;
var cleanup;
var minMaxArr;
var resetPlotterRanges;
var layout;
var startPlayerAutomatically;
var linkPlotRanges;

//setup
startPlayerAutomatically = false; //if true, PlayControlView1(~dataFile) will be executed automatically
linkPlotRanges = true; //if true, ranges will be linked

if(startPlayerAutomatically.not, {
	rawEegBus = ~playCtrl.player.playBus; //set bus here if not starting player automatically
});
//~outBuses //all signals are sent to control buses; array of arrays of 4 bands
//~outBuses[0][0] is first channel, first band (delta)
//~outBuses[0][2] is first channel, third band (alpha)
//~outBuses[1][2] is second channel, third band (alpha)

//NOTE: plot bounds are remembered over time; to reset, press "r" on the keyboard with the plot window active

bandData = [ //[lf, hf, nameSymbol]
	[0.5, 4, \delta],
	[4, 8, \theta],
	[8, 13, \alpha],
	[13, 30, \beta]	
];
layout = \vertical; //\vertical or \horizontal
//-----------------------

numBands = bandData.size;
// numCh = rawEegBus.numChannels;
// outBuses = numBands.collect({IdentityDictionary.new});

respCommand = ("/analysisReply" ++ 1024.rand.asString).asSymbol;
"respCommand: ".post; respCommand.postln;
Server.default.waitForBoot({
	if(startPlayerAutomatically, {
		~playCtrl = PlayControlView1(~dataFile); //temp auto
		"waiting".postln;
		1.wait;
	});
	
	rawEegBus = ~playCtrl.player.playBus; //setup bus here, if different
	numCh = rawEegBus.numChannels;

	controlPlotters = Array.newClear(numCh);
	
	// data frequency analysis for a single band
	synths = CtkProtoNotes(
		SynthDef(\dataBandAnalysis, { //uses control bus as input; leakDC added
			arg inbus = 0, outbus=0, loFreq = 8.0, hiFreq = 13.0, lagScale=4.0, scale=2.0, updateRate = 30, id = 0, bandNumber = 0, lagTime = 0; //note, lagScale is set to higher number by default
			var centerFreq, numSamps, lagTimeVar, lagTimeChosen;
			var in, mod, modSig, out, avgFilt;
			centerFreq = (loFreq + hiFreq)/2;
			numSamps = (Server.default.sampleRate / Server.default.options.blockSize) / centerFreq;
			lagTimeVar = centerFreq.reciprocal;
			in = In.kr(inbus, 1);
			in = LeakDC.kr(in);
			mod = SinOsc.kr(centerFreq, [pi/2, 0]);
			modSig = [in * mod[0], in * mod[1]];
			avgFilt = RunningSum.kr(modSig, numSamps) / numSamps;
			out = (avgFilt[0].squared + avgFilt[1].squared).sqrt;
			lagTimeChosen = (lagTimeVar * lagScale * BinaryOpUGen('==', lagTime, 0)) + (lagTime * BinaryOpUGen('>', lagTime, 0));
			out = Lag3.kr(out, lagTimeChosen);
			out = out * scale;
			SendReply.kr(Impulse.kr(updateRate), respCommand, [bandNumber, out], id);
			Out.kr(outbus, out);
		}),
		SynthDef(\dcRemove, { arg inbus = 0, outbus=0; //note, lagScale is set to higher number by default
			var in;
			in = In.kr(inbus, 1);
			in = LeakDC.kr(in);
			Out.kr(outbus, in);
		}),
	);
	~dataBandAnalysis = synths[\dataBandAnalysis]; //for other use

	
	outBuses = numCh.collect({
		numBands.collect({
			Bus.control(Server.default, 1);
		});
	});

	outDCbuses = numCh.collect({
		Bus.control(Server.default, 1);
	});

	~outBuses = outBuses;
	~outDCbuses = outDCbuses;

	Server.default.sync;

	win = Window.new("dataBandAnalysis (press r to reset bounds)").front;
	win.onClose_({cleanup.()}); //free on close
	~win = win;
	
	layout.switch(
		\vertical, {
			win.bounds_(600@Window.screenBounds.height);
			win.layout_(VLayout());
		},
		\horizontal, {
			win.bounds_(Window.screenBounds.width@200);
			win.layout_(HLayout());
		}
	);
	win.layout.spacing = 0;

	~ov = List.new;
// ControlPlotter
// "here".postln;
	//create plotters
	plotters = numCh.collect({|inc|
		var thisOne, horizView, plotter, otherWin;
		horizView = View(win).layout_(HLayout());
		thisOne = Plotter.new("", Rect(0,0,100, 50), parent: horizView).value_(0!numBands).findSpecs_(true).plotMode_(\levels).domainSpecs_(ControlSpec(0, 4, \lin, 1, 0, "band"));
		thisOne.refresh;
		thisOne.interactionView.maxWidth_(160);
		plotter = ControlPlotter(outDCbuses[inc].index, 1, 200, 30, \linear).start;
		controlPlotters[inc] = plotter;
		otherWin = plotter.mon.plotter.parent.view;
		// plotter.dump;
		// horizView.layout.add(plotter.mon.plotter.parent.view);
		plotter.mon.plotter.makeWindow(horizView);
		horizView.onClose = otherWin.onClose;
		otherWin.onClose = nil;
		otherWin.close;
		// ~cp[0].mon.plotter
		thisOne;
	});

	~pl = plotters;
	~cp = controlPlotters;

	//store min max for minmax over time
	resetPlotterRanges = {
		var cpMax = 0, cpMin = 0;
		// "here".postln;
		minMaxArr = numCh.collect({|chInc|
			bandData.collect({
				[0, 0] //min, max
			});
		});
		// ~mm = minMaxArr;
		
		controlPlotters.do({|thisCP|
			thisCP !? {
				var dataArr, thisMin, thisMax;
				dataArr = thisCP.mon.monitor.data.flat;
				thisMin = dataArr.minItem;
				thisMax = dataArr.maxItem;
				if(thisMin < cpMin, {
					cpMin = thisMin;
				});
				if(thisMax > cpMax, {
					cpMax = thisMax
				});
			}
		});
		controlPlotters.do({|thisCP|
			thisCP !? {
				thisCP.bounds_(cpMin, cpMax)
			}
		});
	};

	~reset = resetPlotterRanges;

	win.view.keyDownAction_({|view, char, modifiers, unicode, keycode|
		unicode.postln;
		unicode.switch(
			114, {resetPlotterRanges.()}, //r
			82, {resetPlotterRanges.()}, //R, just in case
		);
				
	});

	resetPlotterRanges.(); //to init array!
	{resetPlotterRanges.()}.defer(1); //again for plotters

	//create responder
	responder = OSCFunc({|msg|
		var id, bandNumber, val, minMax;
		id = msg[2];
		bandNumber = msg[3];
		val = msg[4];
		{plotters[id].value[bandNumber] = val}.defer;
		// "here1".postln;
		if(linkPlotRanges, { //use first channel when linked
			if(val < minMaxArr[0][bandNumber][0], {minMaxArr[id][bandNumber][0] = val});
			if(val > minMaxArr[0][bandNumber][1], {minMaxArr[id][bandNumber][1] = val});
		}, {
			if(val < minMaxArr[id][bandNumber][0], {minMaxArr[id][bandNumber][0] = val});
			if(val > minMaxArr[id][bandNumber][1], {minMaxArr[id][bandNumber][1] = val});
		});
		if(bandNumber == 0, {
			// "here2".postln;
			{
				// plotters[id].calcSpecs(true, minMaxArr[id][bandNumber][0], minMaxArr[id][bandNumber][1]); //uses history of minmax
				if(linkPlotRanges, {
					plotters[id].calcSpecs(true, minMaxArr[0].flop[0].minItem, minMaxArr[0].flop[1].maxItem); //first channel when connected
				}, {
					plotters[id].calcSpecs(true, minMaxArr[id].flop[0].minItem, minMaxArr[id].flop[1].maxItem); //uses history of minmax
				});
				plotters[id].refresh;
			}.defer;
		});
	}, respCommand);


	notes =  numCh.collect({|chInc|
		bandData.collect({|args, bandInc|
			synths[\dataBandAnalysis].note.inbus_(rawEegBus.index + chInc).loFreq_(args[0]).hiFreq_(args[1]).outbus_(outBuses[chInc][bandInc]).id_(chInc).bandNumber_(bandInc).play;
		})
	});

	notesDC = numCh.collect({|chInc|
		// bandData.collect({|args, bandInc|
			synths[\dcRemove].note.inbus_(rawEegBus.index + chInc).outbus_(outDCbuses[chInc]).play;
		// })
	});

	cleanup = {
		responder.free;
		note.free;
		notes.flat.do(_.free);
		outBuses.flat.do(_.free);
		outDCbuses.flat.do(_.free);
		controlPlotters.do(_.free);
		bus.free;
	};

	// bus = Bus.control(Server.default, 1);

	// note = dataBandAnalysis.note.inbus_(rawEegBus).outbus_(bus).play;

	// lfKnob = EZKnob(win, 60@80, "loFreq", [0.1, 40].asSpec, {|knob|
	// 	note !? {note.loFreq_(knob.value)};
	// }).value_(8);
	// hfKnob = EZKnob(win, 60@80, "hiFreq", [0.1, 40].asSpec, {|knob|
	// 	note !? {note.hiFreq_(knob.value)};
	// }).value_(12);
	// lagScaleKnob = EZKnob(win, 60@80, "lagScale", [0, 8].asSpec, {|knob|
	// 	note !? {note.hiFreq_(knob.value)};
	// }).value_(1);
	// scaleKnob = EZKnob(win, 60@80, "scale", [0, 8].asSpec, {|knob|
	// 	note !? {note.loFreq_(knob.value)};
	// }).value_(2);
	// note.inbus.postln;
	// note.outbus.postln;

	// win.layout_(
	// 	HLayout(
	// 		VLayout(
	// 			StaticText().string_("which input channel\n(from the player bus,\nstart at 0):"),
	// 			NumberBox().action_({|num|note.inbus_(~playCtrl.player.playBus.index + num.value.asInteger)}).clipLo_(0).step_(1),
	// 			nil
	// 		),
	// 		VLayout(
	// 			lfKnob.labelView,
	// 			lfKnob.knobView,
	// 			lfKnob.numberView
	// 		),
	// 		VLayout(
	// 			hfKnob.labelView,
	// 			hfKnob.knobView,
	// 			hfKnob.numberView
	// 		),
	// 		VLayout(
	// 			lagScaleKnob.labelView,
	// 			lagScaleKnob.knobView,
	// 			lagScaleKnob.numberView
	// 		),
	// 		VLayout(
	// 			scaleKnob.labelView,
	// 			scaleKnob.knobView,
	// 			scaleKnob.numberView
	// 		),
	// 		VLayout(
	// 			Button().states_([["Plot input and output"]]).action_({
	// 				ControlPlotter([note.inbus, note.outbus.index], plotLength: 400, refresh_rate: 25, overlay:false, plotMode: \linear).start
	// 			}),
	// 			Button().states_([["Plot output only"]]).action_({
	// 				ControlPlotter(note.outbus.index, plotLength: 200, refresh_rate: 25, overlay:false, plotMode: \linear).start
	// 			}),
	// 			Button().states_([["Post output bus index"]]).action_({
	// 				"Output bus index: ".post; bus.index.postln;
	// 			}),
	// 		)
	// 	)
	// );
})
);



//-----------------------------------------------------------------
//-----------------------------------------------------------------
//-----------------------------------------------------------------





// read data into a buffer
~dataBuffer = CtkBuffer.playbuf(~dataFile.standardizePath).load(sync: true);

// audio bus for data
~dataBus = CtkAudio(8);

// audio buses for band data
~deltaBus = Bus.control(s, 1);
~thetaBus = Bus.control(s, 1);
~alphaBus = Bus.control(s, 1);
~betaBus = Bus.control(s, 1);


// start data player
~player = ~dataPlayer.note(addAction: \head).buffer_(~dataBuffer).outbus_(~dataBus).scale_(-32.dbamp).play;

// data analysis, using channel 5
w = ~dataBandAnalysis.note(addAction: \tail).loFreq_(0.5).hiFreq_(4).inbus_(~dataBus[2]).outbus_(~deltaBus).play;
x = ~dataBandAnalysis.note(addAction: \tail).loFreq_(4).hiFreq_(8).inbus_(~dataBus[2]).outbus_(~thetaBus).play;
y = ~dataBandAnalysis.note(addAction: \tail).loFreq_(8.0).hiFreq_(13).inbus_(~dataBus[2]).outbus_(~alphaBus).play;
z = ~dataBandAnalysis.note(addAction: \tail).loFreq_(13.0).hiFreq_(30.0).inbus_(~dataBus[2]).outbus_(~betaBus).play;

[w,x,y,z].do{arg me; me.free};

