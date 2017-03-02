+KeyboardWindow {
	newWindow {
		var nWhiteKeys = ((nKeys / 12) * 7).ceil;
		var scale;
		//var blackKeysStructure = [0,1,1,0,1,1,1];
		var range = 1; // set to 127 to get MIDI range
		~octave = 0;

		window = Window( "keys channel " ++ channel, bounds );
		window.view.background_( Color.white );
		window.alwaysOnTop_(true); // RVH
		window.front;

		scale = KeyboardWindow.scale;
		userView = UserView( window, window.view.bounds ).resize_( 5 );

		userView.mouseDownAction_({|v,x,y|
			var theKey = ( (x / bounds.width) * nWhiteKeys ) + ( (startOctave + 2) * 7 );
			var velo;
			if( ( y < ( bounds.height * 0.66 ) ) )
			{  case	{ (theKey.frac < 0.25) &&
				{ blackKeysStructure.wrapAt( theKey.floor) == 1 }  }
			{ theKey = theKey.floor - 0.5 }
			{ (theKey.frac > 0.75) &&
				{ blackKeysStructure.wrapAt( theKey.floor + 1) == 1 }  }
			{ theKey = theKey.floor + 0.5 }
			{ true }
			{ theKey = theKey.floor };
			}
			{ theKey = theKey.floor; };

			if( theKey.frac == 0.5 )
			{ velo = (( y / ( bounds.height * 0.66 ) ) * range); }
			{ velo = (( y / bounds.height ) * range); };

			if( this.pressKey( theKey, velo ) )
			{ downAction.value( channel,
				theKey.floor.degreeToKey( scale, 12 ) +
				(theKey.frac * 2),
				velo ); }
			{ upAction.value( channel,
				theKey.floor.degreeToKey( scale, 12 ) +
				(theKey.frac * 2),
				velo );
			};
			lastVelo = velo;
		});

		userView.mouseUpAction_({|v,x,y|
			var theKey;
			var velo;
			if( hold.not )
			{ theKey = ( (x / bounds.width) * nWhiteKeys )  + ( (startOctave + 2) * 7 );
				if( ( y < ( bounds.height * 0.66 ) ) )
				{  case	{ (theKey.frac < 0.25) &&
					{ blackKeysStructure.wrapAt( theKey.floor) == 1 }  }
				{ theKey = theKey.floor - 0.5 }
				{ (theKey.frac > 0.75) &&
					{ blackKeysStructure.wrapAt( theKey.floor + 1) == 1 }  }
				{ theKey = theKey.floor + 0.5 }
				{ true }
				{ theKey = theKey.floor };
				}
				{ theKey = theKey.floor; };

				if( theKey.frac == 0.5 )
				{ velo = (( y / ( bounds.height * 0.66 ) ) * range); }
				{ velo = (( y / bounds.height ) * range); };

				if( this.unPressKey( theKey, lastVelo.copy ) )
				{ upAction.value( channel,
					theKey.floor.degreeToKey( scale, 12 ) +
					(theKey.frac * 2),
					velo );  };
			}
		});

		// RVH
		userView.keyDownAction_({
			arg view, char, modifiers, unicode, keycode;
			// [modifiers,keycode].postln;
			if(modifiers.isCmd, {
				case
				{ (keycode == 18) } {"Octave set to -2".postln; ~octave = -2; }
				{keycode == 19} {"Octave set to -1".postln; ~octave = -1; }
				{keycode == 20} {"Octave set to 0".postln; ~octave = 0; }
				{keycode == 21} {"Octave set to 1".postln; ~octave = 1; }
				{keycode == 23} {"Octave set to 2".postln; ~octave = 2; }
			},{
				case
				{(keycode==18)  && modifiers.isShift.not} {"Keyboard chan set to 1".postln; channel = 0; window.name_("keys channel 1")}
				{(keycode==19)  && modifiers.isShift.not} {"Keyboard chan set to 2".postln; channel = 1; window.name_("keys channel 2")}
				{(keycode==20)  && modifiers.isShift.not} {"Keyboard chan set to 3".postln; channel = 2; window.name_("keys channel 3")}
				{(keycode==21)  && modifiers.isShift.not} {"Keyboard chan set to 4".postln; channel = 3; window.name_("keys channel 4")}
				{(keycode==23)  && modifiers.isShift.not} {"Keyboard chan set to 5".postln; channel = 4; window.name_("keys channel 5")}
				{(keycode==22)  && modifiers.isShift.not} {"Keyboard chan set to 6".postln; channel = 5; window.name_("keys channel 6")}
				{(keycode==26)  && modifiers.isShift.not} {"Keyboard chan set to 7".postln; channel = 6; window.name_("keys channel 7")}
				{(keycode==28)  && modifiers.isShift.not} {"Keyboard chan set to 8".postln; channel = 7; window.name_("keys channel 8")}
				{(keycode==18)  && modifiers.isShift} {"Keyboard chan set to 9".postln; channel = 8; window.name_("keys channel 9")}
				{(keycode==19)  && modifiers.isShift} {"Keyboard chan set to 10".postln; channel = 9; window.name_("keys channel 10")}
				{(keycode==20)  && modifiers.isShift} {"Keyboard chan set to 11".postln; channel = 10; window.name_("keys channel 11")}
				{(keycode==21)  && modifiers.isShift} {"Keyboard chan set to 12".postln; channel = 11; window.name_("keys channel 12")}
				{(keycode==23)  && modifiers.isShift} {"Keyboard chan set to 13".postln; channel = 12; window.name_("keys channel 13")}
				{(keycode==22)  && modifiers.isShift} {"Keyboard chan set to 14".postln; channel = 13; window.name_("keys channel 14")}
				{(keycode==26)  && modifiers.isShift} {"Keyboard chan set to 15".postln; channel = 14; window.name_("keys channel 15")}
				{(keycode==28)  && modifiers.isShift} {"Keyboard chan set to 16".postln; channel = 15; window.name_("keys channel 16")}
				{keycode == 6} {this.pressNote(48,127);}
				{keycode == 1} {this.pressNote(49,127);}
				{keycode == 7} {this.pressNote(50,127);}
				{keycode == 2} {this.pressNote(51,127);}
				{keycode == 8} {this.pressNote(52,127);}
				{keycode == 9} {this.pressNote(53,127);}
				{keycode == 5} {this.pressNote(54,127);}
				{keycode == 11} {this.pressNote(55,127);}
				{keycode == 4} {this.pressNote(56,127);}
				{keycode == 45} {this.pressNote(57,127);}
				{keycode == 38} {this.pressNote(58,127);}
				{keycode == 46} {this.pressNote(59,127);}
				;
			});
		});

		userView.drawFunc = { | theWindow |
			bounds = theWindow.bounds;
			//userView.bounds = bounds.copy.top_(0).left_(0);
			Pen.color = Color.black;

			nWhiteKeys.do( { |i|
				var position, keyWidth;
				keyWidth = bounds.width / nWhiteKeys;
				position = keyWidth * i;
				if( activeKeys.includes( i + ( (startOctave + 2) * 7 )  ) )
				{ Pen.width = keyWidth;
					Pen.color = Color.gray(0.66).blend(Color.red, (notesDict.at(
						i + ( (startOctave + 2) * 7 ) ) ? 0.5) / range ); // RVH added range
					Pen.moveTo( (keyWidth * (i + 0.5))@0 );
					Pen.lineTo( (keyWidth * (i + 0.5))@bounds.height );
					Pen.stroke;  };

				Pen.color = Color.black;
				Pen.width = 1;
				Pen.moveTo( position@0 );
				Pen.lineTo( position@bounds.height );
				Pen.stroke;
				if( blackKeysStructure.wrapAt(i) == 1 )
				{ 	if( activeKeys.includes( (i - 0.5) +
					( (startOctave + 2) * 7 ) ) )
				{ Pen.color = Color.gray(0.33).blend( Color.red,
					(notesDict.at(
						(i - 0.5) +
						( (startOctave + 2) * 7 ) ) ? 0.5) / range ); }; // RVH added range
				Pen.width = keyWidth * 0.5;
				Pen.moveTo( position@0 );
				Pen.lineTo( position@(bounds.height * 0.66) );
				Pen.stroke; };
				if( i%7 == 0) { ["C" ++ (startOctave + (i / 7))].wrapAt(i)
					.drawAtPoint( (keyWidth * (i + 0.2))@(bounds.height - 14),
						color: Color.gray ) };
			} );

		};

		window.onClose_( { allWindows.remove( this ) } );

		^this;
	}

	// RVH
	close {window.close()}

}
