MyScreens : Object {

	classvar result;

	*resolution {
		(result == nil).if{ this.refresh };
		^result;
	}


	*numScreens {
		^this.resolution.size;
	}


	*refresh {
		 var systemProfiler;
		 systemProfiler = "system_profiler SPDisplaysDataType | grep Resolution".unixCmdGetStdOut;
		 result = systemProfiler
			  .findRegexp("(?<!@ )[0-9]{3,}")
			  .collect({|item| item[1].asInteger})
			  .clump(2);
		 ^result;
	}

	*getSuggestedRatio {
		arg presentationScreen = 1, maxNbrX = 8;
		var nbrX, nbrY, screenWidth, screenHeight;
		var res = MyScreens.resolution;

		("Screens:" + res + "(laptop always the first)").postln;

		// if more than one screen take the second, else the only one
		if(res.size > presentationScreen, {res = res[presentationScreen]}, {res = res[0]});
		("\nResolution presentation screen:"+(if(presentationScreen==0,{(res/2).asString + "(double precision so divided by 2)"},{res}))).postln;

		screenWidth = res[0]; screenHeight = res[1];
		// compensate for double precision
		if(presentationScreen == 0, { screenWidth = (screenWidth / 2).floor.asInteger; screenHeight = (screenHeight / 2).floor.asInteger; });
		nbrX = screenWidth / gcd(screenWidth,screenHeight);
		nbrY = screenHeight / gcd(screenWidth,screenHeight);

		("Optimal nbrX & nbrY:" + [nbrX, nbrY]).postln;

		if(nbrX > maxNbrX, {
			nbrY = ((maxNbrX / nbrX) * nbrY).round(1);
			nbrX = maxNbrX;
			("Rescaled nbrX & nbrY:" + [nbrX, nbrY]).postln;
		});

	}
}