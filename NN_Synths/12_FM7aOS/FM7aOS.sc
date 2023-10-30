FM7aOS_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("FM7aOS_NNMod",{
				var ctls, mods, chans, out, initMod, envs, vol, onOffSwitch;
				var mlpVals = In.kr(\dataInBus.kr, 35);

				var inc = 0;
				var freq1 = mlpVals[inc].linexp(0,1,1,4000).lag(0.05);
				var freq2 = mlpVals[inc=inc+1].linexp(0,1,1,4000).lag(0.05);
				var freq3 = mlpVals[inc=inc+1].linexp(0,1,1,4000).lag(0.05);
				var freq4 = mlpVals[inc=inc+1].linexp(0,1,1,4000).lag(0.05);
				var mod1 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod2 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod3 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod4 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod5 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod6 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod7 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod8 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod9 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod10 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod11 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var mod12 = mlpVals[inc=inc+1].linexp(0,1,1,3000).lag(0.05);
				var osc1 = mlpVals[inc=inc+1].linlin(0,1,1,4).round.lag(0.05);
				var osc2 = mlpVals[inc=inc+1].linlin(0,1,1,4).round.lag(0.05);
				var osc3 = mlpVals[inc=inc+1].linlin(0,1,1,4).round.lag(0.05);
				var osc4 = mlpVals[inc=inc+1].linlin(0,1,1,4).round.lag(0.05);
				var filtFreq = mlpVals[inc=inc+1].linexp(0,1,20,20000).lag(0.05);
				var filtRes = mlpVals[inc=inc+1].linlin(0,1,0,0.95).lag(0.05);

				ctls = [
					// freq, phase, amp
					freq1,
					freq2,
					freq3,
					freq4
				];

				//if(version == 1){initMod = 250}{initMod=0.25};
				mods = [
					[0, mod1, mod2, mod3],
					[mod4, 0, mod5, mod6],
					[mod7,mod8, 0, mod9],
					[mod10, mod11, mod12, 0]
				];

				out = FM7aOS.ar(ctls, mods, [osc1,osc2,osc3,osc4], 4)[0].softclip;

				out = MoogVCF2.ar(out, filtFreq, filtRes);

				//in every synth
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, out.dup*envs);
				//ScopeOut2.ar(sound, \scopeBuf.kr(0));

			})
			//.load(ModularServers.servers[\lmi1].server);
			.writeDefFile;
		}
	}

	init {

		this.makeWindow("FM7aOS", Rect(0, 0, 200, 40));

		nnVals = [[\freq1],
			[\freq2],
			[\freq3],
			[\freq4],
			[\mod1],
			[\mod2],
			[\mod3],
			[\mod4],
			[\mod5],
			[\mod6],
			[\mod7],
			[\mod8],
			[\mod9],
			[\mod10],
			[\mod11],
			[\mod12],
			[\osc1],
			[\osc2],
			[\osc3],
			[\osc4],
			[\filtFreq],[\filtRes]
		];


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}