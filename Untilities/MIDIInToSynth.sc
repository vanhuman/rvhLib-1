MIDIInToSynth {

	var synth;

	*new {
		arg synth;
		^super.newCopyArgs(synth).init;
	}

	init {
		var notes = Array.newClear(128);

		MIDIIn.connectAll;

		MIDIdef.noteOn(\noteOn, {
			arg val, num;
			if(notes[num].isNil, {
				notes[num] = Synth(synth, [\freq,num.midicps]);
			});
		}).fix;

		MIDIdef.noteOff(\noteOff, {
			arg val, num;
			if(notes[num].notNil, {
				notes[num].set(\gate,0);
				notes[num] = nil;
			});
		}).fix;
	}
}