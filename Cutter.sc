Cutter_Mod : Module_Mod {
	var buffer, volBus;

	*initClass {
		StartUp.add {
			SynthDef("cutter2_mod", {arg inBus, outBus, bufnum, pointClkFreeze, latchPoint, lowRate, highRate, volBus, onOff, overlap = 1, gate = 1, pauseGate = 1;
				var in, trateTemp, trate, dur, xPos, clk, pointClk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, env, pauseEnv, vol;

				vol = In.kr(volBus);
				trateTemp = LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate);
				trate = Select.kr(onOff, [trateTemp, 0]);

				dur = (1 / trateTemp)*overlap;

				clk = Impulse.kr(trate);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				in = In.ar(inBus);

				BufWr.ar(in, bufnum, phasor, 1);

				xPos = (phasor/SampleRate.ir);

				pointClk = Select.kr(pointClkFreeze, [clk, 0]);

				point0 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 0));
				point1 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 1));
				point2 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 2));
				point3 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 3));
				point4 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 4));
				point5 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 5));
				point6 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 6));
				point7 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 7));

				point = TWChoose.kr(clk, [point0,point1,point2,point3], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

				point = Select.kr(latchPoint, [point, Latch.kr(point, latchPoint)]);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				Out.ar(outBus, TGrains.ar(2, clk, bufnum, 1.0, point, dur, TRand.kr(-1, 1, clk), 1, 4)*vol*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Cutter", Rect(900, 400, 240, 115));
		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		buffer = Buffer.alloc(group.server, group.server.sampleRate*60, 1, {|buffer|

			synths.add(Synth("cutter2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));

			controls.add(Button.new()
				.states_([["mute", Color.blue, Color.black ],["mute", Color.black, Color.red ]])
				.action_{|v|
					4.do{arg i; controls[i].value = 0};
					v.value = 1;
					synths[0].set(\onOff, 1);
			});
			this.addAssignButton(0, \onOff);

			controls.add(Button.new()
				.states_([["on", Color.blue, Color.black ],["on", Color.black, Color.red ]])
				.action_{|v|
					4.do{arg i; controls[i].value = 0};
					v.value = 1;
					synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 0);
			});
			this.addAssignButton(1, \onOff);

			controls.add(Button.new()
				.states_([["latch", Color.blue, Color.black ],["latch", Color.black, Color.red ]])
				.action_{|v|
					4.do{arg i; controls[i].value = 0};
					v.value = 1;
					synths[0].set(\onOff, 0, \pointClkFreeze, 1, \latchPoint, 0);
			});
			this.addAssignButton(2, \onOff);

			controls.add(Button.new()
				.states_([["latch1", Color.blue, Color.black ],["latch1", Color.black, Color.red ]])
				.action_{|v|
					4.do{arg i; controls[i].value = 0};
					v.value = 1;
					synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 1);
			});

			this.addAssignButton(3, \onOff);

			controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
				{arg val; volBus.set(val.value)}, 1, true, orientation:'horz'));
			this.addAssignButton(4, \continuous,Rect(240, 50, 60, 20));

			controls.add(QtEZRanger("speed", ControlSpec(3,20),
				{arg val; synths[0].set(\lowRate, val.value[0], \highRate, val.value[1])}, [3,16], true, orientation:'horz'));

			controls.add(QtEZSlider("overlap", ControlSpec(1,3),
				{arg val; synths[0].set(\overlap, val.value)}, 1, true, orientation:'horz'));
			this.addAssignButton(6, \continuous);

			//start me in the off position
			controls[0].value = 1;

			win.layout_(
				HLayout(
					VLayout(
						HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15), controls[2].maxHeight_(15), controls[3].maxHeight_(15)),
						HLayout(assignButtons[0], assignButtons[1], assignButtons[2], assignButtons[3]),
						HLayout(controls[4],assignButtons[4]),
						controls[5],
						HLayout(controls[6],assignButtons[6])
					)
				)
			);
			win.layout.spacing = 0;
			win.layout.margins = [0,0,0,0];
		});
	}

	killMeSpecial {
		buffer.free;
		volBus.free;
	}
}
