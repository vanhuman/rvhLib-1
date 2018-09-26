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
		~midiSourcesAll.do { arg currentSource, index;
			var currentDevices = currentSource[\device];
			currentSource.name.do { arg currentName;
				MIDIClient.sources.do { arg source, sourceIndex;
					if (
						// check if the source-device is in the current device array
						currentDevices.detectIndex({arg item; item == source.device}).notNil
						&&
						// check if the source-name is the source-name
						currentName == source.name
					) {
						if (currentSource[\uid].isNil) {
							currentSource[\uid] = List();
							"".postln;
						};
						currentSource[\uid].add(source.uid);
							deviceID = ~midiSourcesAll.keys[index];
							devicePort = currentSource[\uid].size - 1;
						("Device available:" + deviceID + "on port" + devicePort).postln;
						("\tSource:" + source).postln;
						("\tUse as srcID filter in MIDIdefs: ~midiSourcesAll[\\" ++ deviceID++ "].uid[" ++ devicePort ++ "]").postln;
					};
				};
			};
		};
		^~midiSourcesAll
	}
}
