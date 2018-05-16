+ServerMeterView {

	*new{ |aserver,parent,leftUp,numIns,numOuts,numGainedChans,numResampleChans|
		^super.new.init(aserver,parent,leftUp,numIns,numOuts,numGainedChans,numResampleChans)
	}

	init { arg aserver, parent, leftUp, anumIns,anumOuts, numGainedChans, numResampleChans;
		var innerView, viewWidth, levelIndic, palette;
		var motuAvail = if(~audioDevice.notNil, {~audioDevice.contains("MOTU")}, {false}); // RvH
		var meterWarning, meterCritical; // RvH

		// RvH
		dBLow = -48;
		meterWarning = -6.linlin(dBLow, 0, 0, 1);
		meterCritical = 0.linlin(dBLow, 0, 0, 1);

		server = aserver;
		height = 200; // RvH -- subtracted 80 from all heights
		numIns = anumIns ?? { server.options.numInputBusChannels };
		numOuts = anumOuts ?? { server.options.numOutputBusChannels };

		viewWidth= this.class.getWidth(anumIns + numGainedChans + numResampleChans,anumOuts);

		leftUp = leftUp ? (0@0);

		view = CompositeView(parent, Rect(leftUp.x,leftUp.y, viewWidth, height) );
		view.onClose_({ this.stop });
		innerView = CompositeView(view, Rect(10,25, viewWidth, height) );
		innerView.addFlowLayout(0@0, gapWidth@gapWidth);

		// dB scale
		UserView(innerView, Rect(0,0,meterWidth,height-5)).drawFunc_({
			try {
				Pen.color = \QPalette.asClass.new.windowText;
			} {
				Pen.color = Color.white;
			};
			Pen.font = Font.sansSerif(8);
			if(~font.notNil, {Pen.font = Font(~font, 8)});
			Pen.stringCenteredIn("    0", Rect(0, 0, meterWidth, 12));
			Pen.stringCenteredIn("-12", Rect(0, (height-70)/4, meterWidth, 12));
			Pen.stringCenteredIn("-24", Rect(0, (height-64)/2, meterWidth, 12));
			Pen.stringCenteredIn("-48", Rect(0, height-60, meterWidth, 12));
		});

		(numIns > 0).if({
			// ins
			// RvH
			if(~font.notNil, {
				StaticText(view, Rect(30, 5, 250, 15))
				.font_(Font(~font, 10))
				.string_("Inputs");
			}, {
				StaticText(view, Rect(30, 5, 250, 15))
				.font_(Font.sansSerif(10))
				.string_("Inputs");
			});
			inmeters = Array.fill( numIns + numGainedChans + numResampleChans, { arg i;
				var comp;
				// RvH; specific channel names for MOTU
				var chan;
				if( (motuAvail == true) and: { (i == 10) || (i == 11) }, { chan = "R" ++ (i-9).asString }, { chan = (i+1).asString });
				if( i >= numIns and: { i< (numIns+numGainedChans) }, { chan = 50 + (i - numIns) } ); // gained channels
				if( i >= (numIns+numGainedChans), { chan = 80 + i - numIns - numGainedChans } ); // resample channels
				// RvH END
				comp = CompositeView(innerView, Rect(0,0,meterWidth,height-35)).resize_(5);
				// RvH
				if(~font.notNil, {
					StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font(~font, 9))
					// .string_(i.asString); // RvH
					.string_(chan);
				}, {
					StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font.sansSerif(9))
					// .string_(i.asString); // RvH
					.string_(chan);
				});
				levelIndic = LevelIndicator( comp, Rect(0,0,meterWidth,height-50) ).warning_(meterWarning).critical_(meterCritical)
				// .style_(1)
					.drawsPeak_(true)
					.numTicks_(5) // RvH
					.numMajorTicks_(0); // RvH
			});
		});

		if((numIns > 0) && (numOuts > 0)){
			// divider
			UserView(innerView, Rect(0,0,meterWidth,height-50)).drawFunc_({
				try {
					Pen.color = \QPalette.asClass.new.windowText;
				} {
					Pen.color = Color.white;
				};
				Pen.line(((meterWidth + gapWidth) * 0.5)@0, ((meterWidth + gapWidth) * 0.5)@(height-50));
				Pen.stroke;
			});
		};

		// outs
		(numOuts > 0).if({
			// RvH
			StaticText(view, Rect(10 + if(numIns > 0 , ((numIns + 2 + numGainedChans + numResampleChans) * (meterWidth + gapWidth)), 0), 5, 200, 15))
				.font_( if(~font.notNil, { Font(~font, 10) }, { Font.sansSerif(10) }) )
				.string_("Outputs");
			outmeters = Array.fill( numOuts, { arg i;
				var comp;
				// RvH: specific channel names for MOTU
				var chan;
				if(motuAvail == true,
					{ if( i < 2, { chan = "M" ++ (i+1).asString }, { chan = (i-1).asString } ) },
					{ chan = (i+1).asString }
				);
				// RvH END
				comp = CompositeView(innerView, Rect(0,0,meterWidth,height-35));
				if(~font.notNil, {
					StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font(~font, 9))
					// .string_(i.asString); //RvH
					.string_(chan);
				}, {
					StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font.sansSerif(9))
					// .string_(i.asString); //RvH
					.string_(chan);
				});
				levelIndic = LevelIndicator( comp, Rect(0,0,meterWidth,height-50) ).warning_(meterWarning).critical_(meterCritical)
					// .style_(1)
					.drawsPeak_(true)
					.numTicks_(5) // RvH
					.numMajorTicks_(0); // RvH
			});
		});

		this.setSynthFunc(inmeters, outmeters);
		startResponderFunc = {this.startResponders};
		this.startResponderInBus(numGainedChans);
		this.startResponderResampleBus(numGainedChans, numResampleChans);
		this.start;
	}

	startResponderInBus {  // RvH: responder for inBus gain routing to channel 50 - 53
		arg numGainedChans;
		~respGainedChans = OSCFunc({|msg|
			{
				numGainedChans.do {arg i;
					var baseIndex = 3 + (2*i);
					var peakLevel = msg.at(baseIndex);
					var rmsValue  = msg.at(baseIndex + 1);
					var meter = inmeters.at(numIns+i);
					if (meter.isClosed.not) {
						meter.peakLevel = peakLevel.ampdb.linlin(dBLow, 0, 0, 1, \min);
						meter.value = rmsValue.ampdb.linlin(dBLow, 0, 0, 1);
					}
				};
			}.defer;
		}, ("/InBus").asSymbol, server.addr).fix;
	}

	startResponderResampleBus {  // RvH: responder for resample busses 80 & 81
		arg numGainedChans, numResampleChans;
		~respResampleChans = OSCFunc({|msg|
			{
				numResampleChans.do {arg i;
					var baseIndex = 3 + (2*i);
					var peakLevel = msg.at(baseIndex);
					var rmsValue  = msg.at(baseIndex + 1);
					var meter = inmeters.at(numIns+numGainedChans+i);
					if (meter.isClosed.not) {
						meter.peakLevel = peakLevel.ampdb.linlin(dBLow, 0, 0, 1, \min);
						meter.value = rmsValue.ampdb.linlin(dBLow, 0, 0, 1);
					}
				};
			}.defer;
		}, ("/ResampleBus").asSymbol, server.addr).fix;
	}

	stop {
		serverMeterViews[server].remove(this);
		if(serverMeterViews[server].size == 0 and: (serverCleanupFuncs.notNil)) {
			serverCleanupFuncs[server].value;
			serverCleanupFuncs.removeAt(server);
		};

		(numIns > 0).if({ inresp.free; });
		(numOuts > 0).if({ outresp.free; });
		~respGainedChans.free; // RvH
		~respResampleChans.free; // RvH

		ServerBoot.remove(startResponderFunc, server)
	}
}


+ServerMeter{

	*new{ |server, numIns, numOuts, xPos = 5, yPos = 305, numResampleChans = 0|

		var meterView;
		var numGainedChans = if(Server.local.options.numInputBusChannels == 2, { 2 }, { 4 });
		numIns = numIns ?? { server.options.numInputBusChannels };
		numOuts = numOuts ?? { server.options.numOutputBusChannels };

		~wMeter = Window.new(server.name ++ " levels (dBFS)",
							Rect(xPos, yPos, ServerMeterView.getWidth(numIns + numGainedChans + numResampleChans, numOuts), 200), // RvH
							false
		).background_(Color.white); // RvH
		~wMeterAvail = 1; // RvH
		~wMeter.view.background_(Color.white); // RvH
		~wMeter.onClose = { ~wMeterAvail = nil }; // RvH

		meterView = ServerMeterView(server, ~wMeter, 0@0, numIns, numOuts, numGainedChans, numResampleChans);
		meterView.view.keyDownAction_({ arg view, char, modifiers;
			if(modifiers & 16515072 == 0) {
				case
				{char === 27.asAscii } { ~wMeter.close };
			};
		});

		~wMeter.front;

		^super.newCopyArgs(~wMeter,meterView)

	}
}