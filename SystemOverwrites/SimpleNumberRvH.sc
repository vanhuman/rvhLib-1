+ SimpleNumber  {
	asTimeStringHM { arg precision=0.1;
		var hours,mins,secs;
		mins = this.div(60);
		if(mins >= 60,{ hours = mins.div(60).asString ++ ":";
			mins = mins%60;
			if(mins < 10 ,{ mins = "0"++ mins.asString; },{ mins = mins.asString; });
			},{
				hours = "";
				//RVH
				if(mins<10, {mins = "0"++mins.asString});
				mins = mins.asString;
		});
		secs = (this%60).trunc(precision);
		if(secs<10,{ secs = "0"++secs.asString; },{ secs=secs.asString; });
		if (precision == 1, { secs = secs.subStr(0,1); });
		^(hours ++ mins ++ ":" ++ secs);
	}
	midicps { arg tuning = 440, note = 69; // combination of cps and corresponding midi note number
		^((this + (tuning.cpsmidi - note)).midicps);
	}
}