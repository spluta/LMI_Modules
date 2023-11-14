//created after moving the EQ from the main out to before the main mixer

EQ_Mod : Module_Mod {

	*initClass {
		StartUp.add {


			SynthDef("EQ_mod", {arg inBus, outBus, lowDB = 0, midDB = 0, hiDB = 0, vol=1, gate=1, pauseGate = 1;
				var in, env, pauseEnv;

				in = In.ar(inBus,2);

				in = BLowShelf.ar(in, 80, 1, Lag.kr(lowDB));
				in = MidEQ.ar(in, 2500, 1, Lag.kr(midDB));
				in = BHiShelf.ar(in, 12500, 1, Lag.kr(hiDB));

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				Out.ar(outBus, in*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("EQ", Rect(150, 100, 160, 115));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("EQ_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));

		controls.add(QtEZSlider("Low", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\lowDB, slider.value)}
		}, 0, true));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider("Mid", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\midDB, slider.value)}
		}, 0, true));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider("Hi", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\hiDB, slider.value)}
		}, 0, true));
		this.addAssignButton(2,\continuous);

		controls.add(Button().maxWidth_(40)
			.states_([["reset", Color.blue, Color.black]],[["reset", Color.blue, Color.black]])
			.action_{
				3.do{|i| controls[i].valueAction_(0)};
				this.sendGUIVals;
			}
		);
		this.addAssignButton(3,\onOff);

		win.layout_(
			VLayout(
				HLayout(controls[0], controls[1],controls[2],controls[3]),
				HLayout(assignButtons[0].maxWidth_(40), assignButtons[1].maxWidth_(40), assignButtons[2].maxWidth_(40), assignButtons[3].maxWidth_(40))
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}
}
