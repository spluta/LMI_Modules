Mute_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef("mute_mod", {arg inBus, controlsBus, outBus, gate=1, pauseGate=1;
				var pauseEnv, env;
				var ctlIn = In.kr(controlsBus, 3);

				var ramp = ctlIn[2].lincurve(0,1,0.001, 0.25);
				var mute0= ctlIn[0].lag(ramp);
				var mute1= Select.kr(mute0, [ctlIn[1].lag(0.001), 1-ctlIn[1].lag(0.001)]);

				var mute = Select.kr(mute0, [mute1, mute0*mute1]);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 2)*env*mute*pauseEnv);
			}).writeDefFile;
		}
	}

	loadExtra {}

	init {
		numControls = 3;
		textList = Array.with("mute0", "mute1", "ramp");
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("mute_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}
