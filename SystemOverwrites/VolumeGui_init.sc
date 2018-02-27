
+ VolumeGui{
	init{ | win, bounds |
		var count = 0;
		var slider, box, simpleController;
		var spec = [model.min, model.max, \db].asSpec;
		bounds = bounds ?? {Rect((Window.screenBounds.width - 85), 150, 80, 300)};
		~volWindow = win ?? {GUI.window.new("Volume", bounds).front.background_(Color.white).alwaysOnTop_(true).onClose_({~volWindow = nil})};
		slider = SmoothSlider(~volWindow, Rect(15,10,50,280))
		.knobColor_(Color.grey(0.3))
		.knobSize_(0.1)
		.thumbSize_(15)
		.value_(spec.unmap(model.volume))
		.canFocus_(false)
		.hilightColor_(Color.green(0.9).alpha_(0.5))
		.string_(round(model.volume,0.1) + "db")
		.stringColor_(Color.white)
		.align_(\center)
		.stringAlignToKnob_(true)
		.font_(Font("Helvetica",10))
		.action_({
			var value = spec.map(slider.value);
			slider.string = round(value,0.1) + "db";
			model.volume_(value)
		})
		.mouseUpAction_({
			arg arg1,arg2,arg3,arg4,arg5;
			if(arg4 == 1048576, {"reset".postln; slider.valueAction = spec.unmap(0); });
		});
	}
	// init { | win, bounds |
	// 	var slider, box, simpleController;
	// 	var spec = [model.min, model.max, \db].asSpec;
	// 	bounds = bounds ?? { Rect(100, 100, 80, 330) };
	// 	window = win ?? { Window.new("Volume", bounds).front };
	// 	box = NumberBox(window, Rect(10, 10, 60, 30))
	// 	.value_(model.volume);
	//
	// 	slider = Slider(window, Rect(10, 40, 60, 280))
	// 	.value_(spec.unmap(model.volume));
	//
	// 	slider.action_({ | item |
	// 		model.volume_(spec.map(item.value));
	// 	});
	// 	box.action_({ | item |
	// 		model.volume_(item.value);
	// 	});
	// 	window.onClose_({
	// 		simpleController.remove;
	// 	});
	//
	// 	simpleController = SimpleController(model)
	// 	.put(\amp, {|changer, what, volume|
	// 		box.value_(volume.round(0.01));
	// 		slider.value_(spec.unmap(volume));
	// 	})
	// 	.put(\ampRange, {|changer, what, min, max|
	// 		spec = [min, max, \db].asSpec.debug;
	// 		slider.value_(spec.unmap(model.volume));
	// 	})
	// }

}