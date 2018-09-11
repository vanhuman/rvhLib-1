// wslib 2011
// quick way of monitoring all incoming osc messages
// exclude can be an array of Symbols with extra messages to exclude (i.e. not post)


OSCMonitor {

	classvar <>exclude, <>showAddr;

	*value { |time = 0, addr, port, msg = ([])|
		var oscMsg, serverInfo = "";
		if( port.size != 0 ) { msg = port };
		if( ([ '/status.reply', '/localhostOutLevels', '/localhostInLevels', '/n_go', '/n_end' ]
				++ exclude.asCollection ).includes( msg[0] ).not ) {
//			[ time.asSMPTEString, addr, msg ].postln;
			oscMsg = msg.asString;
			if( showAddr==1 ) { serverInfo = "\n\tIP:" + addr.ip + "Port:" + addr.port + "Time:" + time.asTimeString };
			("OSC IN:" + oscMsg ++ serverInfo).postln;
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