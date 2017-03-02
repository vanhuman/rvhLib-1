ScrollNumberBox {
	var nrbox, scroller, <>action, <>clipLo, <>clipHi; 
	
	*new { arg parent, bounds, step, argClipLo, argClipHi;
		^super.new.initScrollNumberBox(parent, bounds, step, argClipLo, argClipHi);
  	}
	
	initScrollNumberBox {arg parent, bounds, step, argClipLo, argClipHi;
		var value, offset, startFlag;
		startFlag = true;
		step = step ? 1; 
		clipLo = argClipLo ? -inf; 
		clipHi = argClipHi ? inf;
		
		nrbox = SCNumberBox(parent, bounds);
		nrbox.step = step;
		nrbox.action = { arg num;
			num.value = num.value.clip(clipLo, clipHi);
			action.value(num);
		};
				
		scroller = SCTabletView(parent, bounds);
		scroller.background = Color.new(11,11,11,0);
		scroller.canFocus_(false);
		scroller.mouseDownAction = { arg view,x,y;
			// resize the scroller so other boxes in a row of nrboxes can also detect mousedown
			scroller.bounds_( Rect(	nrbox.bounds.left,
								nrbox.bounds.top - 400,
								nrbox.bounds.width,
								800));
			value = nrbox.value;
			nrbox.focus(true);
		};
		
		scroller.action = { arg view, x, y;
			// the mousedown would not take offset from the new (resized) rect so... here
			if(startFlag == true, {offset = y; startFlag = false});
			nrbox.value_( (((offset - y) * step) + value).clip(clipLo, clipHi) ); 
			action.value(nrbox);
		};
		
		scroller.mouseUpAction = { arg view,x,y;
			scroller.bounds_(bounds);
			startFlag = true;
		};
	}		
	
	value_ { arg val;
		nrbox.value_(val)
	}
	
	value {
		^nrbox.value;
	}
	
	valueAction_ { arg val;
		nrbox.valueAction_(val);
	}
	
	boxColor {
		^nrbox.boxColor;
	}

	boxColor_{ arg color;
		nrbox.boxColor_(color);
	}
	
	canFocus_ { arg bool;
		nrbox.canFocus_(bool)
	}

	font_ { arg argFont;
		nrbox.font_(argFont);
	}

	align_ { arg align;
		nrbox.align_(align);
	}
	
	stringColor {
		^nrbox.stringColor;
	}
	
	stringColor_ { arg color;
		nrbox.stringColor_(color);
	}

	object_ { arg obj;
		nrbox.object_(obj);
	}
	
	properties {
		^nrbox.properties;
	}

	visible_ { arg visible;
		nrbox.visible_(visible);
		scroller.visible_(visible);
	}
}