+ServerMeterView{
	*new{ |aserver,parent,leftUp,numIns,numOuts,numGainedChans|
		^super.new.init(aserver,parent,leftUp,numIns,numOuts,numGainedChans)
	}

	init { arg aserver, parent, leftUp, anumIns,anumOuts, numGainedChans;
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

		viewWidth= this.class.getWidth(anumIns+numGainedChans,anumOuts);

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
			Pen.stringCenteredIn("    0", Rect(0, 0, meterWidth, 12));
			Pen.stringCenteredIn("-12", Rect(0, (height-70)/4, meterWidth, 12));
			Pen.stringCenteredIn("-24", Rect(0, (height-64)/2, meterWidth, 12));
			Pen.stringCenteredIn("-48", Rect(0, height-60, meterWidth, 12));
		});

		(numIns > 0).if({
			// ins
			StaticText(view, Rect(30, 5, 250, 15))
				.font_(Font.sansSerif(10))
				.string_("Inputs");
			inmeters = Array.fill( numIns+numGainedChans, { arg i;
				var comp;
				// RvH; specific channel names for MOTU
				var chan;
				if((motuAvail == true) && ((i==10) || (i==11)),
					{chan = "R" ++ (i-9).asString},
					{chan = (i+1).asString});
				if(i>=numIns, {chan = 50 + (i - numIns)});
				// RvH END
				comp = CompositeView(innerView, Rect(0,0,meterWidth,height-35)).resize_(5);
				StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font.sansSerif(9))
				// .string_(i.asString); // RvH
					.string_(chan);
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
			StaticText(view, Rect(10 + if(numIns > 0 , ((numIns + 2 + numGainedChans) * (meterWidth + gapWidth)), 0), 5, 200, 15))
				.font_(Font.sansSerif(10))
				.string_("Outputs");
			outmeters = Array.fill( numOuts, { arg i;
				var comp;
				// RvH: specific channel names for MOTU
				var chan;
				if(motuAvail == true,
					{if(i<2, {chan = "M" ++ (i+1).asString}, {chan = (i-1).asString});},
					{chan = (i+1).asString});
				// RvH END
				comp = CompositeView(innerView, Rect(0,0,meterWidth,height-35));
				StaticText(comp, Rect(0, height-50, meterWidth, 15))
					.font_(Font.sansSerif(9))
				// .string_(i.asString); //RvH
					.string_(chan);
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
		this.start;
	}

	startResponderInBus{  // RvH: responder for inBus gain routing to channel 50
		arg numGainedChans;
		~inresp50 = OSCFunc({|msg|
			// msg.postln;
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

	stop{
		serverMeterViews[server].remove(this);
		if(serverMeterViews[server].size == 0 and: (serverCleanupFuncs.notNil)) {
			serverCleanupFuncs[server].value;
			serverCleanupFuncs.removeAt(server);
		};

		(numIns > 0).if({ inresp.free; });
		(numOuts > 0).if({ outresp.free; });
		~inresp50.free; // RvH

		ServerBoot.remove(startResponderFunc, server)
	}
}


+ServerMeter{

	*new{ |server, numIns, numOuts, xPos = 5, yPos = 305, numGainedChans = 4|

		var meterView;

		numIns = numIns ?? { server.options.numInputBusChannels };
		numOuts = numOuts ?? { server.options.numOutputBusChannels };

		~wMeter = Window.new(server.name ++ " levels (dBFS)",
							Rect(xPos, yPos, ServerMeterView.getWidth(numIns+numGainedChans,numOuts), 200), // RvH
							false).background_(Color.white); // RvH
		~wMeterAvail = 1; // RvH
		~wMeter.view.background_(Color.white); // RvH
		~wMeter.onClose = {~wMeterAvail = nil}; // RvH

		meterView = ServerMeterView(server, ~wMeter, 0@0, numIns, numOuts, numGainedChans);
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