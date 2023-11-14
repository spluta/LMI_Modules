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
//
// Mute_Mod : Module_Mod {
// 	var impulseOn, dustOn, pulseRate;
//
// 	*initClass {
// 		StartUp.add {
//
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("Mute", Rect(500, 500, 290, 75));
//
// 		this.initControlsAndSynths(2);
//
// 		this.makeMixerToSynthBus(2);
//
// 		synths.add(Synth("mute_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));
//
// 		impulseOn = false;
// 		dustOn = false;
// 		pulseRate = [11,17];
//
// 		controls.add(Button(win, Rect(5, 5, 280, 20))
// 			.states_([["mute", Color.blue, Color.black],["on", Color.black, Color.blue]])
// 			.action_({arg butt;
// 				"mute".post;
// 				synths[0].set(\mute, (butt.value).abs.postln)
// 			})
// 		);
//
// 		controls.add(Button(win, Rect(5, 5, 280, 20))
// 			.states_([["mute", Color.blue, Color.black],["on", Color.black, Color.blue]])
// 			.action_({arg butt;
// 				"mute".post;
// 				synths[0].set(\mute, (butt.value).abs.postln)
// 			})
// 		);
//
// 		this.addAssignButton(0,\onOff, Rect(5, 25, 280, 20));
//
// 		controls.add(EZSlider(win, Rect(5, 50, 280, 20), "ramp", ControlSpec(0.01, 0.25, 'linear'),
// 			{arg val;
// 				synths[0].set(\ramp, val.value);
// 		}, 0.01, true));
// 	}
// }