+ Env {
	*rcurve { arg nodes = 200, levelsBounds = #[0.1, 1], timesBounds = #[0.01, 0.2];

		var levels = Array.fill(nodes, { rrand(levelsBounds[0], levelsBounds[1]) });
		var times = Array.fill(nodes - 1, { rrand(timesBounds[0], timesBounds[1]) });
		^this.new(levels ++ levels[0], times ++ times[0]).circle
	}

	*rcurve2 { arg nodes = 200, levelsBounds = #[0.1, 1],
		shortTimesBounds = #[0.001, 0.01], longTimesBounds = #[0.2, 0.5], balance = #[0.9, 0.1];

		var levels = Array.fill(nodes, { rrand(levelsBounds[0], levelsBounds[1]) });
		var times = Array.fill(nodes - 1, {
			wchoose([
				rrand(shortTimesBounds[0], shortTimesBounds[1]),
				rrand(longTimesBounds[0], longTimesBounds[1])
			], balance)
		});
		^this.new(levels ++ levels[0], times ++ times[0]).circle
	}
}