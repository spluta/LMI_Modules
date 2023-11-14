SnareSwitch_Mod : Module_Mod {
 	var mainSnare;

 	*initClass {
 		StartUp.add {
 			SynthDef("snareSwitch_mod", {arg inBus, outBus, tvFreq1=60, tvStabVol = 1, crankTV=2, t_trig, mainGate=1, snareGate=0, vol=0, gate = 1;
 				var in, freqs, snare, snare2, env, mainEnv, snareEnv, tvEnv, tvStabSig, tvSig;

 				mainEnv = EnvGen.kr(Env.asr(0.01, 1, 0.01), mainGate);
 				snareEnv = EnvGen.kr(Env.asr(0.01, 1, 0.01), snareGate);

				//only necessary if the analog TVs require a signal
				tvEnv = EnvGen.kr(Env.asr(0.01, 1, 0.01), 1-max(snareGate, mainGate));

				in = In.ar(inBus, 2)*mainEnv;

				tvStabSig = tvEnv*SinOsc.ar(tvFreq1)*tvStabVol;

				freqs = TRand.kr(50, 90, t_trig!2);

 				snare = (SinOsc.ar(freqs[0], 0, 0.4)+SinOsc.ar(freqs[1], 0, 0.4))*snareEnv;

				snare2 = (SinOsc.ar(freqs[0]*2, 0, 0.4)+SinOsc.ar(freqs[1]*2, 0, 0.4))*snareEnv;

 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);

				tvSig = (snare2*env*2).clip(-0.7,0.7)+tvStabSig+Mix(in);

				Out.ar(outBus, in*env); //input signal to output
 				Out.ar(outBus+2, snare*env*vol); //output to snare
				Out.ar(outBus+3, tvSig); //output to TVs
 			}).writeDefFile;
 		}
 	}

 	init {
 		this.makeWindow("SnareSwitch", Rect(318, 645, 300, 180));

 		this.initControlsAndSynths(8);

 		this.makeMixerToSynthBus(2);

 		synths = List.new;
 		synths.add(Synth("snareSwitch_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

 		mainSnare = 0;

 		controls.add(Button.new()
 			.states_([["snare", Color.black, Color.red], ["snare", Color.red, Color.black]])
 			.action_{|v|
 				controls[1].value = 1;
 				v.value = 0;
 				synths[0].set(\t_trig, 1, \snareGate, 1, \mainGate, 0);
 				mainSnare = 1;
 			}
 		);
 		this.addAssignButton(0, \onOff);

 		controls.add(Button.new()
 			.states_([["pass", Color.black, Color.red], ["pass", Color.red, Color.black]])
 			.action_{|v|
 				controls[0].value = 1;
 				v.value = 0;
 				synths[0].set(\snareGate, 0, \mainGate, 1);
 				mainSnare = 0;
 			}
 		);
 		this.addAssignButton(1, \onOff);

 		controls.add(Button.new()
 			.states_([["mute", Color.black, Color.red], ["mute", Color.red, Color.black]])
 			.action_{|v|
 				controls[3].value = 1;
 				v.value = 0;
 				synths[0].set(\snareGate, 0, \mainGate, 0);
 			};
 		);
 		this.addAssignButton(2, \onOff);

 		controls.add(Button.new()
 			.states_([["unmute", Color.black, Color.red], ["unmute", Color.red, Color.black]])
 			.action_{|v|
 				controls[2].value = 1;
 				v.value = 0;
 				if(mainSnare == 0,{
 					synths[0].set(\t_trig, 1, \snareGate, 0, \mainGate, 1);
 					},{
 						synths[0].set(\t_trig, 1, \snareGate, 1, \mainGate, 0);
 				});
 			};
 		);
 		this.addAssignButton(3, \onOff);

 		controls.add(QtEZSlider("snareVol", ControlSpec(0.0,1.0,\amp),
 			{|v|
 				synths[0].set(\vol, v.value);
 		}, 0, false, \horz));

		controls.add(QtEZSlider("tv1Freq", ControlSpec(40,80),
 			{|v|
 				synths[0].set(\tvFreq1, v.value);
 		}, 60, false, \horz));

		controls.add(QtEZSlider("tvStabVol", ControlSpec(0,2),
 			{|v|
 				synths[0].set(\tvStabVol, v.value);
 		}, 0.5, false,\horz));

		controls.add(QtEZSlider("crankTV", ControlSpec(1,8),
 			{|v|
 				synths[0].set(\crankTV, v.value);
 		}, 1, false, \horz));

		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0], controls[1]),
					HLayout(assignButtons[0], assignButtons[1]),
					HLayout(controls[2], controls[3]),
					HLayout(assignButtons[2], assignButtons[3]),
					controls[4],
					controls[5],
					controls[6],
					controls[7]
				)
			)
		);

 	}
 }

DustAmpMod_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("dustAmpMod_mod", {arg inBus, outBus, pulseRate0 = 1, pulseRate1 = 1, onBypass=0, gate=1, pauseGate=1;
				var env, out, impulse, dust, mod, pauseEnv;

				impulse = Dust.kr(pulseRate0);
				//dust = Dust.kr(pulseRate1);

				mod = Lag.kr(Select.kr(onBypass, [1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.02);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 2)*env*mod*pauseEnv);
			}).writeDefFile;
		}
	}

	init {

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("dustAmpMod_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		controls.add(Button()
			.states_([["dust", Color.blue, Color.black],["dust", Color.black, Color.blue]])
			.action_({arg butt;
				controls[1].value_(0);
				synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \onBypass, 1);
			})
		);

		controls.add(Button()
			.states_([["bypass", Color.blue, Color.black],["bypass", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\pulseRate0, 0, \pulseRate1, 0, \onBypass, 0);
				controls[0].value_(0);
			})
		);

		this.addAssignButton(0,\onOff);
		this.addAssignButton(1,\onOff);

		controls.add(QtEZRanger("speed", ControlSpec(0.25, 30, 'linear'),
			{arg val;
				pulseRate = val.value;
				if(impulseOn&&dustOn,{
					synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \pulseRate1, rrand(pulseRate[0], pulseRate[1]));
				},{
					if(impulseOn,{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2);
					},{
						if(dustOn,{
							synths[0].set(\pulseRate1, rrand(pulseRate[0], pulseRate[1])*2);
						})
					})
				})
			}, [4, 7], true, \horz));

		controls[1].valueAction_(1);

		this.makeWindow("DustAmpMod", Rect(0, 0, 200, 40));

		win.layout_(VLayout(
			HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15)),
			HLayout(assignButtons[0], assignButtons[1]),
			controls[2]
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		//win.front;
	}
}
