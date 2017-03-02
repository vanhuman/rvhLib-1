/*
DiskPlayer: to play sound files from disk
By Robert van Heumen 2017
Depends on the FreeAfter quark.

Arguments (the first one is mandatory, the rest optional)
server: usually 's'
soundFilePath: the full path to the soundfile to play
out: the output channel
group: the synth group
loop: 0=no looping, 1=looping
att: attack of the playback synth's envelope
rel: release of the playback synth's envelope
lev: level of playback (0-1)
bufFramePower: the buffer allocated has size 2**bufFramePower (default buffer size is about 11 sec so provide smaller number for shorter samples to loop)
startPos: start position into the soundfile in seconds
doneAction: set to 2 to have the buffer freed automatically when finished playing a non-looping soundfile
autoplay: when true the playback starts immediately on instantiation

NOTE: doneAction should not be used, and loop=1 has some issues for shorter files. This has to do with a bug with cueSoundFile: https://github.com/supercollider/supercollider/issues/2474
NOTE: in connection with the remark above, .remove should not be called on created DiskPlayers, as it stalls starting a new DiskPlayer with the error message: File '/Users/Robert/---data---/Audio/random samples/oguz004a.wav' could not be opened: Error. Bad format field in SF_INFO struct when openning a RAW file for read.

*/

DiskPlayer {

	var server, soundFilePath, out, group, loop, att, rel, lev, bufFramePower, startPos, doneAction, autoplay;
	var soundSynth, buf, numChan, soundFileFound = 0, soundFileShort = "";

	*new {
		arg server, soundFilePath, out = 0, group = nil, loop = 0, att = 0.1, rel = 0.1, lev = 1, bufFramePower = 19, startPos = 0, doneAction = 0, autoplay = false;
		^super.newCopyArgs(server, soundFilePath, out, group, loop, att, rel, lev, bufFramePower, startPos, doneAction, autoplay).init;
	}

	init { // initialize
		var soundFileLength, soundFile;

		// define SynthDefs
		[1,2,4].do { |i|
			SynthDef("diskPlayer"++i, {
				arg buf, gate, out, loop, att, rel, lev, mono = 0;
				var sig, env;
				if( i < 4,
					{sig = DiskIn.ar(i, buf, loop)},
					{sig = VDiskIn.ar(i, buf, 1, loop)}
				);
				env = EnvGen.kr(Env.adsr(att,0,1,rel), gate, doneAction: 2);
				FreeSelfWhenDone.kr(sig);
				if(i==2, { sig = ( mono * [Mix.ar(sig),0*sig[1]] ) + ( (1-mono) * sig ) });
				Out.ar(out, lev * env * sig);
			}).send(server);
		};

		// if a buffer is already allocated, free it / this means that init is called a second time, for some reason
		this.remove(now:1);

		// allocate buffer for (V)DiskIn
		if(soundFilePath.notNil, {
			soundFileShort = subStr(soundFilePath,
				soundFilePath.findBackwards("/",offset: max(0,(soundFilePath.size - 30))), soundFilePath.size);
			soundFile = SoundFile.new;
			if(soundFile.openRead(soundFilePath), { // file found
				soundFileFound = 1;
				numChan = soundFile.numChannels;
				soundFileLength = soundFile.numFrames / server.sampleRate;
				buf = Buffer.alloc(server, 2**bufFramePower, numChan);
				"\nDiskPlayer sample file information:".postln;
				("\tBuffer" + buf.bufnum + "size" + (2**bufFramePower) + "allocated").postln;
				("\tSample numChan:" + numChan + "length:" + soundFileLength).postln;
				soundFile.close;
				},{ // file not found
					soundFileFound = 0;
					("\nDiskPlayer ERROR: soundfile:" + soundFileShort + "not found.").postln;
			});
			},{ // no path provided
				"\nDiskPlayer: no soundfile path argument: only SynthDefs send to server.".postln
		});

		// start playback if told so
		if(autoplay, { { this.play }.defer(0.01) });
	}

	set { // setter for parameters
		arg param, value;
		if(soundSynth.notNil, {soundSynth.set(param,value)});
	}

	start { // start playback
		arg mono = 0;

		// if no buffer allocated, run init first / should not happen actually
		if(buf.isNil, {this.init});

		// play!
		if((soundFileFound==1) && (soundSynth.isNil), {
			("\nDiskPlayer: playing sound file" + soundFileShort + if(mono==1,{"(mono)"},{""})+ if(loop==0,{"(one shot)"},{"(looped)"})).postln;
			buf.close;
			buf.cueSoundFile(soundFilePath, startPos * server.sampleRate);
			("DiskPlayer: output to channel" + switch(numChan, 1, {out}, 2, {[out,out+1].asString}, 4, {(out..out+3).asString}) ).postln;
			soundSynth = Synth("diskPlayer"++numChan,
				[\buf, buf, \gate, 1, \out, out, \loop, loop, \att, att, \rel, rel, \lev, lev, \mono, mono],
				target: group)
			.freeAction_({
				"freeAction called".postln;
				soundSynth = nil;
				if(doneAction == 2, { this.remove });
			});
		});
	}

	play { this.start } // just an alias for start

	stop { // stop playback
		if(soundSynth.notNil, {
			("\nDiskPlayer: stopping sound file" + soundFileShort).postln;
			soundSynth.set(\gate,0);
			soundSynth = nil;
		});
	}

	freeBuf { // free buffer // this seems to cause issues with cueSoundFile (see above)
		if(buf.notNil, {
			("\nDiskPlayer: buffer" + buf.bufnum + "for" + soundFileShort + "freed").postln;
			buf.close; buf.free; buf = nil;
		});
	}

	remove { // stop playback and free buffer, after release time
		arg now = 0;
		this.stop;
		if(now == 0, { {this.freeBuf}.defer(rel) }, { this.freeBuf });
	}


}