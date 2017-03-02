
NetAddrMon : NetAddr {

	sendMsg { arg ... args;
		super.sendMsg( *args );
		if(~oscOutMonitor == 1, {("OSC OUT --" + super.hostname + "--" + super.port + "--" + args).postln});
	}
} 