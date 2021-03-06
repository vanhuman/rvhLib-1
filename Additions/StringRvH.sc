+ String {
	subStr { arg start=0, end;
		/*
		Returns a sub-string from start index to end index (0-based).
		Negative start indexes wrap around from the end.
		End indexes greater than string length and nil end indexes are forced to string length.
		Negative end indexes count backwards from the end of the string.
		If end index is greater than start index, the result is reversed
		"a string".subStr( -2 ).postln; // returns the last 2 chars
		"a string".subStr( 0, -1 ).postln; // snips the last char
		"a string".subStr( 1 ).postln; // snips the first char
		"a string".subStr( 2, 4 ).postln; // return chars from the middle
		"a string".subStr( 4, 2 ).postln; // return chars from the middle, backwards
		*/
		var len;
		len = this.size - 1;
		(start < 0).if({start = this.size + start});
		end = end ? len;
		(end > len).if({ end = len });
		(end < 0).if({ end = len + end });
		^( start <= end ).if({this.copyRange( start, end )},{ this.subStr( end, start ).reverse })
	}

	subStrNoWrap { arg start=0, end;
		/*
		Returns a sub-string from start index to end index (0-based).
		Negative start indexes wrap around from the end.
		End indexes greater than string length and nil end indexes are forced to string length.
		Negative end indexes count backwards from the end of the string.
		If end index is greater than start index, the result is reversed
		"a string".subStr( -2 ).postln; // returns the last 2 chars
		"a string".subStr( 0, -1 ).postln; // snips the last char
		"a string".subStr( 1 ).postln; // snips the first char
		"a string".subStr( 2, 4 ).postln; // return chars from the middle
		"a string".subStr( 4, 2 ).postln; // return chars from the middle, backwards
		*/
		var len;
		len = this.size - 1;
		(start < 0).if({start = 0});
		end = end ? len;
		(end > len).if({ end = len });
		// (end < 0).if({ end = len + end });
		^( start <= end ).if({this.copyRange( start, end )},{ "" })
	}
}