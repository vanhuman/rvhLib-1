
NetAddrMon : NetAddr {

	sendMsg { arg ... args;
		super.sendMsg( *args );
		if( ~oscOutMonitor.notNil and: { ~oscOutMonitor >= 1 } ){
			("OSC OUT: " + args).postln;
			if (~oscOutMonitor == 1) {
				("\tIP:" + super.hostname + "Port:" + super.port + "Time:" + SystemClock.seconds.asTimeString).postln;
			};
		};
	}
} 