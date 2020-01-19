+ Dialog {
	*openPanelAndLoad { arg buffer, path;
		var function = {
			arg path;
			var file = SoundFile.new();
			var channels;
			if (file.openRead(path)) {
				channels = if (file.numChannels == 1, { [0,0] }, { [0,1] });
				buffer.readChannel(path, channels: channels);
				("Loaded:" + path.subStr(path.findBackwards("/") + 1)).postln;
				file.close;
			};
		};
		^(this.openPanel(okFunc: function, path: path));
	}
}