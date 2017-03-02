/*
Robert van Heumen 2014/2015
MIDI player to embed in a window or run independently
arguments (all optional):
	win: the window to embed the player in - if empty a window will be created
	marginX, marginY: the offsett for the player in the window
	font: the font of the text, format [font, size]
	color: the color of some interface elements
	midiFilePath: the MIDI file to be played
	midiOutID: the number of the MIDI output device
	midiChan: the MIDI channel to be played to 0-15
	useTrackChan: boolean indicating whether the channel numbers inside the MIDI file should be used

Cases regarding MIDI channels:
1. No MIDI channel information available in the MIDI file: midiChan will be used if provided, else channel 1 will be used.
2. MIDI channel information is included in the MIDI file:
	a. if useTrackChan = true then the included MIDI channel will be used
	b. if useTrackChan = false then midiChan will be used if provided, else channel 1 will be used.

*/

MIDIplayer {

	var midiPlayer, midiFile;

	*new {
		arg win, marginX = 20, marginY = 20, font = ["Helvetica",12], color = Color.green, midiFilePath, midiOutID = 0, midiChan = 0, useTrackChan = true;
		^super.new.initMIDIplayer(win, marginX, marginY, font, color, midiFilePath, midiOutID, midiChan, useTrackChan);
	}

	initMIDIplayer {
		arg win, marginX, marginY, font, color, midiFilePath, midiOutID, midiChan, useTrackChan;

		var localWin = 0, screenHeight = Window.screenBounds.height;
		var midiOut, midiFileSelected = 0, midiPorts;
		var dMidiOut, bSelectFile, tFile, bPlayFile, bMonitor, bInfo, bAllNotesOff, cLoop, lLoop, dMidiChan, lMidiChan;
		var midiChannels = (1..16);

		if(MIDIClient.initialized == false, {MIDIClient.init});
		midiPorts = MIDIClient.destinations.collect({ |x| (x.device++":"+x.name) });
		// if(midiOutID.isNil, {midiOutID = 0});

		// ------------------ GUI --------------------

		if(win.isNil, { // create window
			win = (Window("MIDI Player", Rect(20,screenHeight - 200,970+(2*marginX),60))
				.background_(Color.grey(0.9))
				.alpha_(0.9)
			);
			localWin = 1;
		});
		win.onClose_({
			win = nil;
			this.stop;
		});

		bSelectFile = (Button(win, Rect(marginX+0,marginY+0,50,20))
			.states_([["Select", Color.black, Color.white]])
			.canFocus_(false)
			.font_(Font(font[0],font[1]))
			.action_({
				Dialog.openPanel({
					arg path;
					midiFile = SimpleMIDIFile.read(path);
					tFile.string = "..."++subStr(path, path.size - 30);
					midiFileSelected = 1;
					midiFile.timeMode = \seconds;
				},
				{"Selection cancelled".postln},
				false
				);
			})
		);

		tFile = (StaticText(win, Rect(marginX+60,marginY+0,200,20))
			.canFocus_(false)
			.font_(Font(font[0],font[1]-2))
			.string_("")
		);

		bInfo = (Button(win,Rect(marginX+270,marginY+0,20,20))
			.states_([["i", Color.black, Color.white]])
			.canFocus_(false)
			.action_({ this.info })
		);

		bPlayFile = (Button(win, Rect(marginX+300,marginY+0,50,20))
			.states_([["Play", Color.black, Color.white],["Stop",Color.black, color]])
			.font_(Font(font[0],font[1]))
			.canFocus_(false)
			.action_({
				if(midiFileSelected == 1, {
					if(bPlayFile.value == 1,{
						"Playing MIDI file...".postln;
						midiPlayer = (midiFile.p2(amp:1, useTrackChan: useTrackChan) <> (type: \midi, midiout: midiOut, chan: midiChan)).play;
						{ // when MIDI file is done playing restart if loop==1
							if(bPlayFile.value == 1, {
								if(cLoop.value == 1, {bPlayFile.valueAction_(0); bPlayFile.valueAction_(1)});
							});
						}.defer(midiFile.length);
					},
					{ midiPlayer.stop })
				},{
					"No MIDI file is selected!".postln;
					bPlayFile.value = 0;
				});
			})
		);

		lLoop = StaticText(win, Rect(marginX+360,marginY,35,20)).canFocus_(false).string_("Loop: ");

		cLoop = CheckBox(win, Rect(marginX+400,marginY+2,16,16)).canFocus_(false).background_(Color.white);

		lMidiChan = StaticText(win, Rect(marginX+420,marginY,35,20)).canFocus_(false).string_("Chan:");

		dMidiChan = (PopUpMenu(win, Rect(marginX+460,marginY+0,40,20))
			.font_(Font(font[0],font[1]))
			.canFocus_(false)
			.items_(midiChannels)
			.action_({ midiChan = dMidiChan.value })
		);

		dMidiOut = (PopUpMenu(win, Rect(marginX+505,marginY+0,250,20))
			.font_(Font(font[0],font[1]))
			.canFocus_(false)
			.items_(midiPorts)
			.action_({ midiOut = MIDIOut(dMidiOut.value).latency_(0.01) })
		);

		bMonitor = (Button(win, Rect(marginX+760,marginY+0,100,20))
			.font_(Font(font[0],font[1]))
			.canFocus_(false)
			.states_([["MIDI monitor", Color.black, Color.white]])
			.action_({ "osascript -e 'tell application \"Midi Monitor\" to activate'".unixCmd })
		);

		bAllNotesOff = (Button(win,Rect(marginX+865,marginY+0,100,20))
			.states_([["All notes off", Color.black, Color.white]])
			.canFocus_(false)
			.font_(Font(font[0],font[1]))
			.action_({ midiOut.control(0,123,127) })
		);

		// ------------------ Initialize --------------------

		midiOutID = max(0,min(midiPorts.size-1,midiOutID)); // force to be in the range of MIDI ports
		dMidiOut.valueAction = midiOutID;

		midiChan = max(0,min(15,midiChan)); // force to be in the range 0-15
		dMidiChan.valueAction = midiChan;

		if(midiFilePath.notNil, {
			tFile.string = "..."++subStr(midiFilePath, midiFilePath.size - 30);
			midiFile = SimpleMIDIFile.read(midiFilePath);
			midiFileSelected = 1;
			midiFile.timeMode = \seconds;
		}, {
			tFile.string = "--------------- select MIDI file ---------------"
		});

		if(localWin == 1, { win.front });
	}

	stop { midiPlayer.stop }

	info {
		if(midiFile.notNil, {
			midiFile.plot;
			("Lenght:" + midiFile.length).postln;
			" ".postln;
			midiFile.info;
			"\nMIDI Events:".postln;
			midiFile.midiEvents.size.do({arg i;
				midiFile.midiEvents[i].postln;
			});
			"\nMeta Events:".postln;
			midiFile.metaEvents.size.do({arg i;
				midiFile.metaEvents[i].postln;
			});
		});
	}

}