RingMod2_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\ringmod2_mod,{arg inBus, controlsBus, outBus;
				var ctlIn = In.kr(controlsBus, 9);

				var freq0= ctlIn[0].linexp(0,1,20, 500).lag;
				var freq1= ctlIn[1].linexp(0,1,350, 2000).lag;
				var freq2= ctlIn[2].linexp(0,1,1500, 10000).lag;
				var freq3 = TExpRand.kr(20, 500, ctlIn[3]);
				var freq4 = TExpRand.kr(350, 2000, ctlIn[4]);
				var freq5 = TExpRand.kr(1500, 10000, ctlIn[5]);

				var vol = ctlIn[8].lincurve(0,1,0, 1).lag;

				var noise, sig;
				var noiseFreq=ctlIn[6].linexp(0,1,20, 10000).lag;
				var noiseMul =ctlIn[7].linlin(0,1,1, 500).lag;
				var onOff = 1;//(ctlIn[1]).clip(0,1).lag;

				var freq = MostChange.kr(freq0,MostChange.kr(freq1,freq2));
				freq = MostChange.kr(freq,MostChange.kr(freq5,MostChange.kr(freq3,freq4)));

				noise = LFNoise1.ar(noiseFreq)*noiseMul;
				sig = SinOsc.ar(([freq, freq]+LFNoise2.ar([0.1,0.12], 0.1)+noise).clip(40, 20000));

				sig = (sig*In.ar(inBus,2)*4).softclip;

				Out.ar(outBus, sig*vol*onOff);
			}).writeDefFile;

		}
	}

	loadExtra {
	}

	init {
		numControls = 9;
		textList = Array.fill(6,{|i| "freq"++i.asString}).addAll([]);
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("ringmod2_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}