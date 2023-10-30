TVFeedback_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			(SynthDef("TVFeedback_NNMod", {
				var in, sig, audioOut, onOffSwitch, envs, tvVol, oops;

				var mlpVals = In.kr(\dataInBus.kr, 55);
				var inc = -1;

				sig = SoundIn.ar([0,1,4,5,6,7], 6.collect{|i| mlpVals[inc=inc+1].lag(0.05)});

				tvVol = mlpVals[inc=inc+1];

				sig = 6.collect{|i|
					BHiShelf.ar(
						MidEQ.ar(
							MidEQ.ar(
								BLowShelf.ar(
									sig[i],
									mlpVals[inc=inc+1].lag(0.05).linexp(0,1,20,80), 1,
									mlpVals[inc=inc+1].lag(0.05).linlin(0,1,-25,25)),
								mlpVals[inc=inc+1].lag(0.05).linexp(0,1,80,2000), 4,
								mlpVals[inc=inc+1].lag(0.05).linlin(0,1,-25,25)),
							mlpVals[inc=inc+1].lag(0.05).linexp(0,1,2000,5000), 4,
							mlpVals[inc=inc+1].lag(0.05).linlin(0,1,-25,25)),
						mlpVals[inc=inc+1].lag(0.05).linexp(0,1,5000,12500), 1,
						mlpVals[inc=inc+1].lag(0.05).linlin(0,1,-25,25)
					);
				};

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
				sig = Normalizer.ar(Mix(sig) + SoundIn.ar(2))*envs;
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				sig = sig*onOffSwitch;

				audioOut = LPF.ar(LPF.ar(IFFT(PV_BrickWall(FFT(LocalBuf(256), sig), -0.5)), 10000), 10000);

				//in every synth
/*				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);
				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);
				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));*/


				Out.ar(\outBus.kr(0), [sig*tvVol*Lag.kr(In.kr(\volBus.kr), 0.1),audioOut*Lag.kr(In.kr(\volBus.kr), 0.1)]);
			//}).load(ModularServers.servers[\lmi1].server))
			}).writeDefFile;)
		}
	}

	init {

		this.makeWindow("TVFeedback", Rect(0, 0, 200, 40));

		nnVals = [ "inGain0", "inGain1", "inGain2", "inGain3", "inGain4", "inGain5", "tvVol", "lowFrq0", "lowDB0", "midFrqA0", "midDBA0", "midFrqB0", "midDBB0", "hiFrq0", "hiDB0", "lowFrq1", "lowDB1", "midFrqA1", "midDBA1", "midFrqB1", "midDBB1", "hiFrq1", "hiDB1", "lowFrq2", "lowDB2", "midFrqA2", "midDBA2", "midFrqB2", "midDBB2", "hiFrq2", "hiDB2", "lowFrq3", "lowDB3", "midFrqA3", "midDBA3", "midFrqB3", "midDBB3", "hiFrq3", "hiDB3", "lowFrq4", "lowDB4", "midFrqA4", "midDBA4", "midFrqB4", "midDBB4", "hiFrq4", "hiDB4", "lowFrq5", "lowDB5", "midFrqA5", "midDBA5", "midFrqB5", "midDBB5", "hiFrq5", "hiDB5" ].collect({|item| [item.asSymbol]});

		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}
