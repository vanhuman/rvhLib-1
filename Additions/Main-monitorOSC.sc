// wslib 2011
// quick way of monitoring all incoming osc messages
// exclude can be an array of Symbols with extra messages to exclude (i.e. not post)


OSCMonitor {

	classvar <>exclude, <>showAddr;

	*value { |time = 0, addr, port, msg = ([])|
		var oscMsg;
		if( port.size != 0 ) { msg = port };
		if( ([ '/status.reply', '/localhostOutLevels', '/localhostInLevels', '/n_go', '/n_end' ]
				++ exclude.asCollection ).includes( msg[0] ).not ) {
//			[ time.asSMPTEString, addr, msg ].postln;
			if(showAddr==1,
				{oscMsg = addr.ip + "--" + addr.port + "--" + msg.asString},
				{oscMsg = msg.asString});
			("OSC IN --" + oscMsg + time.asTimeString).postln;
		};
	}

	*valueArray { arg args; ^this.value(*args) }
}

+ Main {
	oscInMonitor { |bool = true, excl, addr = 0|
		if( bool == true ) {
			OSCMonitor.exclude = excl;
			OSCMonitor.showAddr = addr;
			recvOSCfunc = recvOSCfunc.removeFunc( OSCMonitor ).addFunc( OSCMonitor );
		} {
			recvOSCfunc = recvOSCfunc.removeFunc( OSCMonitor );
		};
	}
}