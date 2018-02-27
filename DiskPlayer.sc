/*
DiskPlayer Class to play sound files from disk. Number of channels can be 1 to 4 and is automatically detected.
Developed by Robert van Heumen 2017 http://west28.nl/
Depends on the FreeAfter quark.
Note: synthDefs are only added if not already present in the global SynthDescLib. So if you've made changes, recompile the library. (Due to a bug with cueSoundFile: https://github.com/supercollider/supercollider/issues/2474).

Arguments (the first two are mandatory, the rest optional)
server: usually 's'
path: the full path to the soundfile to play
out: the output channel (default 0)
group: the synth group (default nil)
loop: 0=no looping, 1=looping (default 0)
att: attack of the playback synth's envelope (default 0.1)
rel: release of the playback synth's envelope (default 0.1)
lev: level of playback (0-1) (default 1)
bufPwr: the buffer allocated has size 2**bufPwr (default 19; then the buffer size is about 11 sec so provide smaller number for shorter samples to loop)
startPos: start position into the soundfile in seconds (default 0)
doneAction: set to 2 to have the buffer freed automatically when finished playing a non-looping soundfile (default 0)
autoPlay: when true the playback starts immediately on instantiation (default false)
verbose: when true lots of message will be outputted while using DiskPlayer (default true)
mono: only for stereo files: whether playback should be mono (value 1) or stereo (value 0)
altOut: extra output channel, for example for recording internally with MOTU / specify level as arg for .play and .start (default is 1!)

Use:
a = DiskPlayer.new(s,"/Users/Robert/---data---/Audio/random samples/oguz004a.wav", loop: 1, bufPwr: 17, startPos: 0.5, autoPlay: true, doneAction: 2);
a.stop;
a.start;
a.remove;

*/

DiskPlayer {

	var server, path, out, group, loop, att, rel, lev, rate, ff, bufPwr, startPos, doneAction, autoPlay, verbose, mono, altOut;
	var synth, buf, numChan, sRate, file, numFrames, fileFound = 0, pathShort = "", task;

	*new {
		arg server, path, out = 0, group = nil, loop = 0, att = 0.1, rel = 0.1, lev = 1, rate = 1, ff = 20000,
			bufPwr = 19, startPos = 0, doneAction = 0, autoPlay = false, verbose = true, mono = 0, altOut = 8;
		^super.newCopyArgs(server, path, out, group, loop, att, rel, lev, rate, ff,
			bufPwr, startPos, doneAction, autoPlay, verbose, mono, altOut).init;
	}

	init {
		sRate = server.sampleRate;

		// only add synthDefs if not already there
		// this has to do with a bug with cueSoundFile: https://github.com/supercollider/supercollider/issues/2474
		if(SynthDescLib.global.synthDescs.keys.asArray.indexOf(\diskPlayer4).isNil, { this.sendSynthDefs() });

		// allocate buffer for VDiskIn
		if(path.notNil, {
			pathShort = subStr(path, path.findBackwards("/",offset: max(0,(path.size - 30))) + 1, path.size);
			file = SoundFile.new;
			if(file.openRead(path), { // file found

				// set variables and allocate buffer
				fileFound = 1;
				numChan = file.numChannels;
				numFrames = file.numFrames;
				buf = Buffer.alloc(server, 2**bufPwr, numChan);
				if(verbose, {
					"\nDiskPlayer samplefile:".postln;
					("\tBuffer" + buf.bufnum + "size" + (2**bufPwr) + "allocated").postln;
					("\tSample numChan:" + numChan + "length:" + (numFrames/sRate).round(0.01) ++ "sec").postln;
				});
				file.close;

				// start playback if told so
				if(autoPlay, { { this.play }.defer(0.01) });

				},{ // file not found
					fileFound = 0;
				("\nDiskPlayer ERROR: soundfile:" + pathShort + "not found.").postln;
			});
			},{ // no path provided
				"\nDiskPlayer: no soundfile path argument".postln
		});
	}
	sendSynthDefs {
		[1,2,3,4].do { |i|
			SynthDef(\diskPlayer++i, {
				arg buf, gate, out, loop, att, rel, lev, rate, ff, mono, altOutLev = 0;
				var sig, env;
				sig = VDiskIn.ar(i, buf, rate, loop);
				env = EnvGen.kr(Env.adsr(att,0,1,rel), gate, doneAction: 2);
				FreeSelfWhenDone.kr(sig);
				sig = RLPF.ar(sig, ff, 0.5);
				sig = lev * env * sig;
				if(i==2, { sig = ( mono * [Mix.ar(sig),0*sig[1]] ) + ( (1-mono) * sig ) });
				Out.ar(altOut, sig * altOutLev);
				Out.ar(out, sig);
			}).add;
		};
		if(verbose, { "\nDiskPlayer: SynthDefs sent".postln });
	}
	// setter for parameters
	set {
		arg param, value;
		if(synth.notNil, {synth.set(param,value)});
	}
	// start playback
	start {
		arg altOutLev = 1;
		// if no buffer allocated, run init first
		if(buf.isNil, { this.init });
		// play!
		if((fileFound==1) && (synth.isNil), {
			buf.close;
			buf.cueSoundFile(path, startPos * sRate);
			synth = Synth(\diskPlayer++numChan,
				[\buf, buf, \gate, 1, \out, out, \loop, loop, \att, att, \rel, rel, \lev, lev, \rate, rate, \ff, ff, \mono, mono, \altOutLev, altOutLev],
				target: group)
			.freeAction_({
				synth = nil;
				if(doneAction == 2, { this.remove });
			});
			if(verbose, {
				"\nDiskPlayer playing:".postln;
				("\t" + pathShort ++ if(mono==1,{" (mono)"},{""})+ if(loop==0,{"(one shot)"},{"(looped)"})).postln;
				("\toutput to channel" + switch(numChan, 1, {out}, 2, {[out,out+1].asString}, 4, {(out..out+3).asString}) ).postln;
				("\talt output to channel" + switch(numChan, 1, {altOut}, 2, {[altOut,altOut+1].asString}, 4, {(altOut..altOut+3).asString}) ).postln;
			});
		});
	}
	// start playback
	play {
		arg altOutLev = 1;
		this.start(altOutLev);
	}
	// start playback quantized / alternative output on by default
	playTask {
		arg quant, altOutLev = 1;
		task = Task.new({ this.start(altOutLev) }).play(quant: quant);
	}
	// stop playback
	stop {
		if(synth.notNil, {
			if(verbose, { ("\nDiskPlayer: stopping" + pathShort).postln });
			synth.set(\gate,0);
			synth = nil;
			if(task.notNil, { task.stop });
		});
	}
	// free buffer // this seems to cause issues with cueSoundFile (see above)
	freeBuf {
		if(buf.notNil, {
			if(verbose, { ("\nDiskPlayer: buffer for" + pathShort + "freed").postln });
			buf.close; buf.free; buf = nil;
		});
	}
	// stop playback and free buffer, after release time
	remove {
		arg now = 0;
		this.stop;
		if(now == 0, { {this.freeBuf}.defer(rel) }, { this.freeBuf });
	}
}