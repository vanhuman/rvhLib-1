RGB2CMYK {
	*new {
		arg rgb = [0,0,0];
		^super.new.init(rgb);
	}

	init {
		arg rgb;
		var red = rgb[0], green = rgb[1], blue = rgb[2];
		var black = min(0.999, 1 - max(red,green,blue));
		var cyan = (1- red - black) / (1 - black);
		var magenta =  (1 - green - black) / (1 - black);
		var yellow = (1 - blue - black) / (1 - black);
		var cmyk = [cyan,magenta,yellow,black];
		("Converted RGB" + rgb + "to CMYK" + cmyk).postln;
		^cmyk
	}
}














