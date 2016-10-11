
//open file later with gui, or provide path as an argument (see below)
~playCtrl = PlayControlView1();

// specify file path
// ~dataFile = "/Volumes/data/Dokumenty/2015-2016/Art&Brain research/recording/2016.08.04/00_160804_142639_kr_5minBaseline.WAV"
// eeg data file
// ~dataFile = "~/Files/Projects/Active/9e2/EEG_Spatial_Processing/OpenBCI-RAW-Dan_Adam_Test_1.txt_filter_None_allCh.wav";
// ~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/00_OpenBCI-RAW-2016-08-04_14-38-37_5minBaseline.txt_filter_None_allCh.wav";
// ~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/01_OpenBCI-RAW-2016-08-04_14-48-41_5minPinkNoise.txt_filter_None_allCh.wav";
~dataFile = "~/Files/Projects/Active/9e2/Data/2016.08.04/03_OpenBCI-RAW-2016-08-04_14-57-00_fieldRecording.txt_filter_None_allCh.wav"

//open player with the path argument
~playCtrl = PlayControlView1(~dataFile);
~playCtrl.player.playBus; //outbus of the player


//---------
// start ~playCtrl first, load file
// then run code below
//--------
(
var dataBandAnalysis, note, bus;
var win, lfKnob, hfKnob, lagScaleKnob, scaleKnob, chNum, plotButt, outbusNumButt;

// data frequency analysis for a single band
dataBandAnalysis = CtkSynthDef(\dataBandAnalysis, { //uses control bus as input; leakDC added
	arg inbus = 0, outbus=0, loFreq = 8.0, hiFreq = 13.0, lagScale=1.0, scale=2.0;
	var centerFreq, numSamps, lagTime;
	var in, mod, modSig, out, avgFilt;
	centerFreq = (loFreq + hiFreq)/2;
	numSamps = (Server.default.sampleRate / Server.default.options.blockSize) / centerFreq;
	lagTime = centerFreq.reciprocal;
	in = In.kr(inbus, 1);
	in = LeakDC.kr(in);
	mod = SinOsc.kr(centerFreq, [pi/2, 0]);
	modSig = [in * mod[0], in * mod[1]];
	avgFilt = RunningSum.kr(modSig, numSamps) / numSamps;
	out = (avgFilt[0].squared + avgFilt[1].squared).sqrt;
	out = Lag3.kr(out, lagTime * lagScale);
	out = out * scale;
	Out.kr(outbus, out);
});
~dataBandAnalysis = dataBandAnalysis; //for other uses

bus = Bus.control(Server.default, 1);
win = Window.new("dataBandAnalysis", 600@200).front;
win.onClose_({bus.free; note.free}); //free on close

note = dataBandAnalysis.note.inbus_(~playCtrl.player.playBus.index).outbus_(bus).play;

lfKnob = EZKnob(win, 60@80, "loFreq", [0.1, 40].asSpec, {|knob|
	note !? {note.loFreq_(knob.value)};
}).value_(8);
hfKnob = EZKnob(win, 60@80, "hiFreq", [0.1, 40].asSpec, {|knob|
	note !? {note.hiFreq_(knob.value)};
}).value_(12);
lagScaleKnob = EZKnob(win, 60@80, "lagScale", [0, 8].asSpec, {|knob|
	note !? {note.hiFreq_(knob.value)};
}).value_(1);
scaleKnob = EZKnob(win, 60@80, "scale", [0, 8].asSpec, {|knob|
	note !? {note.loFreq_(knob.value)};
}).value_(2);
note.inbus.postln;
note.outbus.postln;

win.layout_(
	HLayout(
		VLayout(
			StaticText().string_("which input channel\n(from the player bus,\nstart at 0):"),
			NumberBox().action_({|num|note.inbus_(~playCtrl.player.playBus.index + num.value.asInteger)}).clipLo_(0).step_(1),
			nil
		),
		VLayout(
			lfKnob.labelView,
			lfKnob.knobView,
			lfKnob.numberView
		),
		VLayout(
			hfKnob.labelView,
			hfKnob.knobView,
			hfKnob.numberView
		),
		VLayout(
			lagScaleKnob.labelView,
			lagScaleKnob.knobView,
			lagScaleKnob.numberView
		),
		VLayout(
			scaleKnob.labelView,
			scaleKnob.knobView,
			scaleKnob.numberView
		),
		VLayout(
			Button().states_([["Plot input and output"]]).action_({
				ControlPlotter([note.inbus, note.outbus.index], plotLength: 400, refresh_rate: 25, overlay:false, plotMode: \linear).start
			}),
			Button().states_([["Plot output only"]]).action_({
				ControlPlotter(note.outbus.index, plotLength: 200, refresh_rate: 25, overlay:false, plotMode: \linear).start
			}),
			Button().states_([["Post output bus index"]]).action_({
				"Output bus index: ".post; bus.index.postln;
			}),
		)
	)
);
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

