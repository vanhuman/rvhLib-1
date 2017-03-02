+ Server  {

	stopRecording {
		if(recordNode.notNil) {
			recordNode.free;
			recordNode = nil;
			"Recording Stopped: %\n".postf(recordBuf.path);
			recordBuf.close({ |buf| buf.freeMsg });
			recordBuf = nil;
		} {
			"Not Recording".warn
		};
	}

	prepareForRecord { arg path;
		// RVH set different defaults
		recSampleFormat = "int24";
		if (path.isNil) {
			if(File.exists(thisProcess.platform.recordingsDir).not) {
				thisProcess.platform.recordingsDir.mkdir
			};

			// temporary kludge to fix Date's brokenness on windows
			if(thisProcess.platform.name == \windows) {
				path = thisProcess.platform.recordingsDir +/+ "SC_" ++ Main.elapsedTime.round(0.01) ++ "." ++ recHeaderFormat;

			} {
				path = thisProcess.platform.recordingsDir +/+ "SC_" ++ Date.localtime.stamp ++ "." ++ recHeaderFormat;
			};
		};
		recordBuf = Buffer.alloc(this, recBufSize ?? { 4 * (this.sampleRate.nextPowerOfTwo) }, recChannels, // RVH added 4 *
			{arg buf; buf.writeMsg(path, recHeaderFormat, recSampleFormat, 0, 0, true);},
			this.options.numBuffers + 1); // prevent buffer conflicts by using reserved bufnum
		recordBuf.path = path;
		SynthDef("server-record", { arg bufnum;
			DiskOut.ar(bufnum, In.ar(0, recChannels))
		}).send(this);
	}

}