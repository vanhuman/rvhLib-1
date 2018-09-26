/*
This class expects an argument midiOutSource. If a global variable ~midiOutSource is set before, the argument can me omitted. Both the midiOutSource and ~midiOutSource should have one of these two formats:
1. An array of [device ID, name index] pairs, f.e. [  [\IAC, 2],  [\MUL, 0],  [\MSP, 1]  ];
2. A single device ID, f.e. \IAC;
The device ID's should be formatted as one of the devices defined in <Document.dir>/BaseLib/GlobalBaseVars.scd.

The class will set a ~midiOut global array of the same length as ~midiOutSource with the MIDIOut objects. It also returns the same array. Use f.e. as ~midiOut[0].noteOn(..).

If one of the sources in ~midiOutSource is not available, it will be assigned the default source ~midiSourceDefault as defined in <Document.dir>/BaseLib/GlobalBaseVars.scd.
*/
SetMIDIOutMultiple {
	*new {
		arg midiOutSource;
		^super.new.init(midiOutSource);
	}
	init {
		arg midiOutSource;
		var defaultDevice, defaultName;
		(this.class.filenameSymbol.asString.dirname ++ "/midiSourceDefinitions.scd").load();

		// if passed as argument, the global variable is overridden
		// ~midiOutDevice is the old way, should be not an array, just a device ID
		~midiOutSource = midiOutSource ? ~midiOutSource ? ~midiOutDevice;

		// in case ~midiOutSource is not an array, it is just a device ID, so format correctly
		if (~midiOutSource.isArray.not) {
			var source = ~midiSourcesAll[~midiOutSource];
			if (source.notNil) {
				~midiOutSource = [ [~midiOutSource, source.name[0]] ];
			};
		};

		// prepare ~midiOut
		~midiOut = Array.newClear(~midiOutSource.size);

		if (MIDIClient.initialized.not) {
			"\n### Initializing MIDI...".postln;
			MIDIIn.connectAll;
			"### Done initializing MIDI".postln;
		};

		"\n### Setting MIDI Output devices...".postln;
		("\nSetting up devices:" + ~midiOutSource).postln;
		~midiOutSource.do({ arg currentSource, index;
			var sourceDevice, sourceName;
			var currentDevice = currentSource[0];
			var currentNameIndex = if (currentSource[1] < ~midiSourcesAll[currentDevice][\name].size) { currentSource[1] } { 0 };
			var currentName = ~midiSourcesAll[currentDevice][\name][currentNameIndex];
			MIDIClient.sources.do({ arg source, sourceIndex;
				// check the current source
				if (
					// check if the source-device is in the current device array
					~midiSourcesAll[currentDevice][\device].detectIndex({arg item; item == source.device}).notNil
					&&
					// check if the source-name is valid for this device (not really necessary, just for completeness and documentation)
					~midiSourcesAll[currentDevice][\name].detectIndex({arg item; item == source.name}).notNil
					&&
					// check if the source-name is the current name
					currentName == source.name
				) {
					("Connecting device" + currentSource + "...").postln;
					("\tSource:" + source).postln;
					("\tUse as MIDIOut device by calling f.e.: ~midiOut[" ++ index ++ "].noteOn(0, 60, 127)").postln;
					sourceDevice = source.device;
					sourceName = source.name;
				};
				// set the default
				if (
					defaultDevice.isNil
					&&
					~midiSourcesAll[~midiSourceDefault][\device].detectIndex({arg item; item == source.device}).notNil
					&&
					source.name == ~midiSourcesAll[~midiSourceDefault][\name][0]
				) {
					("Connecting default device" + ~midiSourceDefault + "...").postln;
					("\tSource:" + source).postln;
					defaultDevice = source.device;
					defaultName = source.name;
				};
			});
			if(sourceDevice.notNil) {
				~midiOut[index] = MIDIOut.newByName(sourceDevice, sourceName);
			};
		});

		// set default if device not found
		~midiOut.size.do { arg index;
			if (~midiOut[index].isNil) {
				("Device" + ~midiOutSource[index] + "not available. Using default device" + ~midiSourceDefault + "instead.").postln;
				~midiOut[index] = MIDIOut.newByName(defaultDevice, defaultName);
			};
		};
		// if only one device, remove the array layer
		if (~midiOut.size === 1) {
			~midiOut = ~midiOut[0];
		};
		^~midiOut
	}
}