(
~midiSourceDefault = \IAC;
~midiSourcesAll = OEM(
	\IAC, (
		device: ["IAC Driver"],
		name: ["IAC Bus 1", "IAC Bus 2", "IAC Bus 3"],
	),
	\ICON, (
		device: ["iCON iControl V1.01"],
		name: ["Port 1"],
	),
	\LPK, (
		device: ["MPKmini2"],
		name: ["MPKmini2"],
	),
	\MSP, (
		device: ["MIDISPORT 2x2", "MIDISPORT 4x4 Anniv"],
		name: [ "Port A",  "Port B",  "Port C",  "Port D"],
	),
	\MUL, (
		device: ["UltraLite mk3 Hybrid", "UltraLite mk3 Hybrid #2", "828mk2", "UltraLite"],
		name: ["MIDI Port"],
	),
);
)