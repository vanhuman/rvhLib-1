/*
This class will extend the global variable ~midiSourcesAll as defined in <Document.dir>/BaseLib/GlobalBaseVars.scd with uid properties, so the devices can be polled for incoming MIDI. The global variable ~midiSourcesAll will also be returned. Use as srcID filter in MIDIdefs, f.e.: ~midiSourcesAll[\IAC].uid[2].
*/

GetMIDIInIds {
	*new {
		^super.new.init();
	}
	init {
		var deviceID, devicePort;
		(this.class.filenameSymbol.asString.dirname ++ "/midiSourceDefinitions.scd").load();

		if (MIDIClient.initialized.not) {
			"\n### Initializing MIDI...".postln;
			MIDIIn.connectAll;
			"\n### Done initializing MIDI".postln;
		};

		"\n### Getting MIDI Input uid's...".postln;
		~midiSourcesAll.do { arg currentSource, sourceIndex;
			var currentDevices = currentSource[\device];
			// create nil array for uid's
			currentSource[\uid] = Array.newClear(currentSource.name.size);
			currentSource.name.do { arg currentName, nameIndex;
				MIDIClient.sources.do { arg source;
					if (
						// check if the source-device is in the current device array
						currentDevices.detectIndex({arg item; item == source.device}).notNil
						&&
						// check if the source-name is the source-name
						currentName == source.name
					) {
						if (nameIndex == 0) {
							"".postln;
						};
						currentSource[\uid][nameIndex] = source.uid;
						deviceID = ~midiSourcesAll.keys[sourceIndex];
						devicePort = nameIndex;
						("Device available:" + deviceID + "on port" + devicePort).postln;
						("\tSource:" + source + "with uid:" + source.uid).postln;
						("\tUse as srcID filter in MIDIdefs: ~midiSourcesAll[\\" ++ deviceID++ "].uid[" ++ devicePort ++ "]").postln;
					};
				};
			};
		};
		^~midiSourcesAll
	}
}
