/*
This class expects as argument the value MUL, EXT or IAC1. If ~midiOutDevice is set before, the argument can me omitted. The class will set ~midiOut to the requested MIDI interface; if not connected it will resort to IAC1.
If a second argument is passed, the midiOutPort, this class will try to findByName the device with [midiOutDevice, midiOutPort]. In this case this device has to be connected, otherwise it will throw an error.
*/
MIDIout {
	var iac1ID = 0, iac1i, mulID = 0, lpkID = 0, extID = 0, iac2ID = 0;

	*new
	{
		arg midiOutDevice, midiOutPort;

		^super.new.init(midiOutDevice, midiOutPort);
	}

	init
	{
		arg midiOutDevice, midiOutPort;

		if(midiOutDevice.notNil, {~midiOutDevice = midiOutDevice});

		"### Initializing MIDI".postln;
		MIDIIn.connectAll;

		MIDIClient.sources.size.do({|i|
			case
			// MOTU Ultralite -> MUL
			{
				(MIDIClient.sources[i].device == "UltraLite mk3 Hybrid") && (MIDIClient.sources[i].name == "MIDI Port")
			}
			{
				mulID = MIDIClient.sources[i].uid;
				// organize MIDI output
				if(~midiOutDevice == "MUL", {
					~midiOut = MIDIOut(i);
					~midiOut.latency_(0.01);
					("MIDI out to MUL with ID=" ++ mulID).postln;
				});
			}
			// M-Audio MidiSport, Teensy MIDI, Ploytec MIDI Cable, iCON iControl V1.01 -> EXT
			{
				((MIDIClient.sources[i].device == "MIDISPORT 2x2") && (MIDIClient.sources[i].name == "Port A")) ||
				(MIDIClient.sources[i].device == "Teensy MIDI") ||
				((MIDIClient.sources[i].device == "iCON iControl V1.01") && (MIDIClient.sources[i].name == "Port 1")) ||
				(MIDIClient.sources[i].device == "Ploytec MIDI Cable")
			}
			{
				extID = MIDIClient.sources[i].uid;
				// organize MIDI output
				if(~midiOutDevice == "EXT", {
					~midiOut = MIDIOut(i);
					~midiOut.latency_(0.01);
					("MIDI out to EXT (MIDISPORT, Teensy, Ploytec or iCON) with ID=" ++ extID).postln;
				});
			}
			// IAC1 -> IAC1
			{
				(MIDIClient.sources[i].device == "IAC Driver") && (MIDIClient.sources[i].name == "IAC Bus 1")
			}
			{
				iac1ID = MIDIClient.sources[i].uid;
				iac1i = i;
				// organize MIDI output
				if(~midiOutDevice == "IAC1", {
					~midiOut = MIDIOut(i);
					~midiOut.latency_(0.01);
					("MIDI out to IAC1 with ID=" ++ iac1ID).postln;
				});
			}
			// IAC2 -> IAC2
			{
				(MIDIClient.sources[i].device == "IAC Driver") && (MIDIClient.sources[i].name == "IAC Bus 2")
			}
			{
				iac2ID = MIDIClient.sources[i].uid;
				// organize MIDI output
				if(~midiOutDevice == "IAC2", {
					~midiOut = MIDIOut(i);
					~midiOut.latency_(0.01);
					("MIDI out to IAC2 with ID=" ++ iac2ID).postln;
				});
			}
			// LPK, MPK, microKEY -> LPK
			{
				(MIDIClient.sources[i].device == "LPK25") ||
				(MIDIClient.sources[i].device == "MPKmini2") ||
				((MIDIClient.sources[i].device == "microKEY") && (MIDIClient.sources[i].name == "KEYBOARD"))
			}
			{
				lpkID = MIDIClient.sources[i].uid
			}
			;
		});

		// if no MIDI out yet search by argument as name
		if(~midiOut.isNil && midiOutPort.notNil, {
			~midiOut = MIDIOut.newByName(~midiOutDevice,midiOutPort);
			("MIDI out to" + ~midiOutDevice + "(" ++ midiOutPort ++ ")").postln;
		});

		// if MIDI out should be initialized but midiOut is still nil, use IAC1
		if(~midiOut.isNil, {
			~midiOut = MIDIOut(iac1i);
			~midiOut.latency_(0.01);
			("MIDI out to IAC1 (since" + ~midiOutDevice + "is not available)").postln;
			~midiOutDevice = "IAC1";
		});

		"### Done initializing MIDI".postln;
	}

	getIDs {
		^[iac1ID, iac2ID, mulID, lpkID, extID]
	}
}