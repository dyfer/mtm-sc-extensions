~path = "/Volumes/data/Dokumenty/2015-2016/Art&Brain research/recording/2016.08.04/00_160804_142639_kr_5minBaseline.WAV"
v = PlayControlView1(~path);
~player = PlayControl(~path)
//DONE plotter - simple
//time display - lookup violin phase
//Juan's synth with controls - separate; playback only; also plotter
//add processing with arbitrary synth graph - take function as arg
10.12.asTimeString
//copy ploter from Mike's code...
//add controls for plottwing parameters... maybe update in realtime instead of offline? should be fairly fast... (well, 1/64 of time, minute updated in a second...)
//maybe use lang side for max/min/avg values, then use synth processing for scaling... or in language...?
//also easy vertical zoom...
//maybe keep track of normalization values? would be fancy... maybe for later
//look into Juan's code
SoundFileView
v = PlayControlView1();
v = PlayControlView1(~path);
v.playButt
v.player.plot
v.soundFileView.soundFileView.scroll(0.8)
v.soundFileView.soundFileView.scrollPos * v.soundFileView.numFrames
v.soundFileView.soundFileView.viewFrames + (v.soundFileView.soundFileView.scrollPos * v.soundFileView.numFrames)
v.soundFileView.timeCursorPosition
v.soundFileView.yZoom = 200
v.soundFileView.yZoom = 100
v.soundFileView.yZoom = 9
v.soundFileView.yZoom = 0.8
v.soundFileView.heightChannels_(1)
v.soundFileView.set(0, (1100 * 8).collect({rrand(0.00005, 0.00009)}).flat);//manually update
v.soundFileView.set(0, (1100 * 8).collect({rrand(0, 1.2)}).flat);//manually update
v.soundFileView.set(0, (1100 * 8).collect({0.00099}).flat);//manually updatedata
v.soundFileView.soundFileView.peakColor //Color(0.94901960784314, 0.69803921568627)
v.soundFileView.soundFileView.rmsColor
v.soundFileView.soundFileView.peakColor = v.soundFileView.soundFileView.rmsColor
v.soundFileView.soundFileView.peakColor = Color(0.94901960784314, 0.69803921568627)
v.soundFileView.soundFileView.waveColors = [Color.rand]
v.soundFileView.soundFileView.sampleRate
v.soundFileView.soundFileView.blockSize
SoundFileView
v.loadFile(~path)
v.player.stop
v.player.resetPos_(10000)
v.soundFileView.alloc(4000, 1, 400)
v.soundFileView.view.bounds
v.soundFileView.yZoom = 20
v.soundFileView.heightChannels(2)
SoundFile
s.plotTree
~player.buffer.sampleRate

w = Window.new("ControlRate player for EEG signals", Window.screenBounds).front
w.bounds
w.layout = VLayout();
~sfv = SoundFileView(w, w.view.bounds.width @ (w.view.bounds.height)).alloc(750 * 100, 1, 10);
~sfv.gridOn_(0)
~sfv.set(0, (1000rrand(-1.0, 1) ! 7000 ! 8).flat);//manually update data
~sfv.set(0, (1000.collect({|inc| inc / 1000})).flat);//manually update data
~sfv.set(2, [1]);//manually update data

v = SoundFileView.new

setWaveValue: {|thisOne, sample, val|
				{soundFileView.set(sample, val)}.defer;
},
~sfv.numCh
~sfv.numFrames
~player.buffer.numFrames
~player.buffer.numChannels
~win = Window.new.front;
~sfv = SoundFileView(~win, ~win.bounds.width @ ~win.bounds.height).alloc(~player.buffer.numFrames, ~player.buffer.numChannels, ~player.buffer.sampleRate / s.options.blockSize); //sr = control rate
~sfv = SoundFileView(~win, ~win.bounds.width @ ~win.bounds.height).alloc(~player.buffer.numFrames, ~player.buffer.numChannels, ~player.buffer.sampleRate); //sr is audio sr
~player.buffer.loadToFloatArray(action: {|arr|
	a = arr;
	{"setting".postln;~sfv.set(0, arr.normalize)}.defer;
})
~player.play
~player.stop
~player.rate_(1)
~sfv.dump
a.maxItem
a.minItem
c = a.normalize
c.maxItem
a.plot
~player.buffer.plot
~sfv.gridOn = false

~sfv.yZoom = 1

~sfv.dump

b = a.size.collect({rrand(-1, 1.0)})
b.plot

~sfv.peakColor
~sfv.background = Color(1, 1, 1)

~sfv.bounds = 600@500

~sfv.set(0, b)



~sfv.sampleRate
~sfv.waveColors = [];
~sfv.peakColor = Color.black

~sfv.rmsColor = Color.black

~sfv.numFrames


~sfv.elasticMode = false

~sfv.blockSize

~nChToDisplay = 4;

w = Window.new("ControlRate player for EEG signals", Window.screenBounds).front
w.bounds
w.layout = VLayout();
~sfv = SoundFileView(w, w.view.bounds.width @ (w.view.bounds.height)).alloc(750 * 100, 1, 750);
~sfv.set(0, (1000rrand(-1.0, 1) ! 7000 ! 8).flat);//manually update data
~sfv.set(0, (1000.collect({|inc| inc / 1000})).flat);//manually update data
~sfv.set(2, [1]);//manually update data
100.do({|inc|~sfv.set(inc, [rrand(-0.5, 0.5)])});//manually update data
100.do({|inc|~sfv.set(inc, [0.5])});//manually update data
~sfv.set(0, (100.collect({|inc| inc / 100})).flat);//manually update data
~sfv.gridOn_(0)
~sfv.yZoom_(1)
w.view.onResize = {|...args| args.postln;} //this is triggered on resize, use to resice the waveform!
v = View.new(w, Rect(0, 0, w.bounds.width, w.bounds.height - 100));
v = ScrollView.new(w, Rect(0, 0, w.bounds.width, w.bounds.height - 100));
v.background = Color.rand(0.2)
v.bounds
v.remove
u = View.new(w).minHeight_(120);
u.background = Color.rand(0.2)
w.layout = HLayout();

~sfv = SoundFileView(v, v.bounds.width @ (v.bounds.height * 3)).alloc(44100 * 100, 8, 750); //sr = control rate
v.onResize = {|view| ~sfv.bounds = Rect(0,0,view.bounds.width, view.bounds.height * (~player.buffer.numChannels / ~nChToDisplay))}

~sfv.gridOn = false

~sfv.dump
s.boot
p = PlayControl("/Volumes/data/Dokumenty/2015-2016/Art&Brain research/recording/2016.08.04/00_160804_142639_kr_5minBaseline.WAV", false, s)
s.options.blockSize
OpenDialog.new
v.yZoom = 400
v.yZoom = 0.2
SoundFile
p.buffer.numFrames
b.numFrames
n.background_(Color.rand)
n.layout.insert(t[0])
t[0]

t[0].background = Color.rand
u.(8)
n.bounds.top
n.bounds = Rect(0, 0, n.bounds.width, 800);
w.bounds.height * (8/4)
v.timeCursorPosition

//sfv tests

w = Window.new("soundfile test", Rect(200, 300, 740, 100)).front;
a = SoundFileViewLabels.new(w, Rect(20,20, 700, 60));
a.bounds
a.enclosingMasterView.bounds
a.enclosingScrollView.bounds
w.view.bounds;
w.layout

~path = "/Volumes/data/Dokumenty/2015-2016/Art&Brain research/recording/2016.08.04/00_160804_142639_kr_5minBaseline.WAV";
w = Window.new("soundfile test", Rect(200, 300, 740, 100)).front;
w.layout = HLayout();
a = SoundFileViewLabels.new(w);
a.load(~path)
a.soundfile
a.heightChannels(2)
a.bounds
a.enclosingMasterView.bounds = a.enclosingMasterView.bounds
a.enclosingMasterView.refresh
a.soundFileView.bounds
a.soundFileView.bounds = Rect(30, 0, 900, 100)
a.channelLabelView.layout = VLayout();
a.channelLabelView.children
b = String.new(a.channelLabelView)
a.numChannels.collect({|inc| StaticText(a.channelLabelView).string_((inc + 1).asString)})
a.labelStrings
//add player controls, using  the control model
//also play stop etc, playhead
//add .update to GUI and .changed to PlayControl
w.bounds.width
w.bounds.height
(
var win, waveView, channelLabelView, controlMainView, playControlView, displayControlView, soundFileView;
var processWave, loadFile, displayWaveform, free, updateDisplay, setDisplayYZoomFromArray, setDisplayYZoom, createSFV, updateNChToDisplay; //functions
var filePath; //controlrate file to read
var numChannels, numFrames, sampleRate;
var tempInPath, tempOutPath, tempFileReady, duration, fs_Hz, prefix;
var loadButt, playButt, stopButt;
var nChToDisplay = 1; //should be updated anyway
var player, displayBufferArray; //do I even need the display buffer here?
var dispLF, dispHF, dispDCremove, dispYZoom;
var server;

//set vals
dispDCremove = true;
filePath; //you can autoload file here
server = s; //should be booted!
//-------

//init
prefix = "".resolveRelative;
tempOutPath = prefix ++ "tmpout.wav".postln;

win = Window.new("ControlRate player for EEG signals", Window.screenBounds).front;
win.layout = VLayout();
win.onClose = {free.()};//free on close
waveView = ScrollView(win);
waveView.hasHorizontalScroller = false;
w = waveView;
waveView.background = Color.hsv(0.5, 0, 0.8); //just to see where it is
waveView.onResize = {|view|
	soundFileView !? {
		soundFileView.bounds = Rect(soundFileView.bounds.left, soundFileView.bounds.top, view.bounds.width, view.bounds.height * (player.buffer.numChannels / nChToDisplay));
	}
};
controlMainView = View.new(win).fixedHeight_(120);
controlMainView.layout = HLayout();
playControlView = View(controlMainView);
displayControlView = View(controlMainView);
playControlView.layout = HLayout(
	VLayout(//load
		loadButt = Button().states_([["Load file"]]).action_({Dialog.openPanel({|path|loadFile.(path)}, {"loading cancelled".postln}, false)});
	),
	VLayout(//playcontrols
		playButt = Button().states_([["Play"]]),
		stopButt = Button().states_([["Stop"]]),
	),
	VLayout(//time display, also selection!
	)
);

loadFile = {|path|
	"loading".postln;
	path.postln;
	filePath = path; //use global var
	player = PlayControl(filePath, makeGui: false, server: server);
	p = player; //temp
	{
		updateDisplay.();
	}.defer(1);
};

createSFV = {
	var labelWidth = 30;
	soundFileView.remove; //just in case
	soundFileView = nil;
	soundFileView = SoundFileView(waveView, Rect(labelWidth, 0, (waveView.bounds.width - labelWidth - 5), (waveView.bounds.height * (numChannels / nChToDisplay)))).alloc(numFrames, numChannels, sampleRate / server.options.blockSize); //using control rate here
	//set some vals
	channelLabelView = View(waveView, Rect(0,0,labelWidth, (waveView.bounds.height * (numChannels / nChToDisplay))));
	channelLabelView.layout_(VLayout(*t = numChannels.collect({|inc| StaticText().string_((inc + 1).asString).font_(Font(size: 16))})));
	n = channelLabelView;
	soundFileView.gridOn = false;
	soundFileView.timeCursorOn = true;
};

updateNChToDisplay = {|val|
	val = val.clip(1, numChannels);
	nChToDisplay = val;
	"nChToDisplay: ".post; nChToDisplay.postln;
	soundFileView !? {soundFileView.bounds = Rect(soundFileView.bounds.left, soundFileView.bounds.top, soundFileView.bounds.width, waveView.bounds.height * (numChannels / nChToDisplay))};
	channelLabelView.bounds = Rect(channelLabelView.bounds.left, channelLabelView.bounds.top, channelLabelView.bounds.width, waveView.bounds.height * (numChannels / nChToDisplay));
	//update channel labels
	//update gui numer here?
};

u = updateNChToDisplay;
	

updateDisplay = {
	processWave.(actionWhenDone: {|buf|
		var displayNeedsAllocating;
		if(soundFileView.isNil, {
			displayNeedsAllocating = true;
		}, {
			if((soundFileView.numChannels == buf.numChannels) && (soundFileView.numFrames == buf.num), {
				//soundFileView.numChannels reports always 1! so the view will always be recreated for now
				displayNeedsAllocating = false
			}, {
				displayNeedsAllocating = true;				
			})
		});
		// displayBuffer.free;
		// displayBuffer = buf;
		buf.loadToFloatArray(action: {|arr|
			displayBufferArray = arr; 
			numChannels = buf.numChannels;
			// nChToDisplay = numChannels;
			numFrames = buf.numFrames; //this might be different than the original?
			sampleRate = buf.sampleRate;
			{
				if(displayNeedsAllocating, {
					createSFV.();
				});
				soundFileView.set(0, displayBufferArray);
				setDisplayYZoomFromArray.(displayBufferArray); //nope, since we're normalizing anyway...
				// buf.free; //we don't need buffer anymore
				v = soundFileView; //temp
				b = buf; //temp for checking
			}.defer; //needs defering?
		});
	});
};

setDisplayYZoomFromArray = {|array|
	var min, max, scaler;
	min = array.minItem;
	max = array.maxItem;
	scaler = min.max(max).reciprocal;
	setDisplayYZoom.(scaler);
};

setDisplayYZoom = {|val|
	dispYZoom = val;
	soundFileView.yZoom = dispYZoom;
};

free = {
	// displayBuffer.free;
	player.free;
};

processWave = {arg loFreq = dispLF, hiFreq = dispHF, removeDC = dispDCremove, fadeTime = 0, channelToExtract, pathIn = filePath, pathOut = tempOutPath, normalizeOutput = true, argserver = server, actionWhenDone = {}; //action when done is passed a buffer with processed file
	var numCh, sf, options, score, processedOutputPath, oscFilePath, numChOut;
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
	SynthDef(\filtered, {//arg loFreq = 7, hiFreq = 13;
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
	}).load;
	"synth loaded?".postln;
	//is defer needed for the below???
	{
	score = [
		[0, [\s_new, \filtered, 1000, 0, 0]],
		[duration, [\c_set, 0, 0]] // finish
	];
	options = ServerOptions.new.numInputBusChannels_(numCh).numOutputBusChannels_(numChOut).sampleRate_(fs_Hz); // mono output
		Score.recordNRT(score, oscFilePath, processedOutputPath, pathIn, fs_Hz, "WAV", "float", options, "", duration, {
			var pathToReadToBuffer, normPath;
			"Writing file done!".postln;
			File.delete(oscFilePath);
			if(normalizeOutput, {
				"normalizing...".postln;
				normPath = processedOutputPath ++ "_norm.wav";
				pathToReadToBuffer = normPath;
				SoundFile.normalize(processedOutputPath, normPath, linkChannels: false, threaded: false); //no threading to wait for the result
				"normalizing finished!".postln;
			}, {
				pathToReadToBuffer = processedOutputPath;
			});
			"Reading into buffer".postln;
			"tempOutPath: ".post; tempOutPath.postln;
			Buffer.read(argserver, pathToReadToBuffer, startFrame: 0, numFrames: -1, action: {|buf|
			"Reading into buffer done".postln;
				{actionWhenDone.(buf)}.defer;
				File.delete(tempOutPath);
			});
			// tempFileReady = true
		}); // synthesize
	}.defer(1);
};

//auto load here
filePath !? {loadFile.(filePath)};

)