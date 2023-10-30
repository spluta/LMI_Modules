MarielOrWeDontNeedLight_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\marielorwedontneedlight_mod,{arg inBus, controlsBus, outBus;
				//sine waves for the end of marie's piece

				var controls = In.kr(controlsBus, 10);


				var dist = controls[1].linlin(0,1,0.001,3);


				var sound = (LFTri.ar([60.5.midicps/4-dist, 60.5.midicps/4+dist], 0, 5)).tanh.softclip.sum.dup*controls[0].lincurve(0,1,0,0.25,4);

				sound = LPF.ar(sound, controls[2].linlin(0,1,100, 500));

				Out.ar(outBus, sound);
			}).writeDefFile;

		}
	}

	loadExtra {
	}

	init {
		numControls = 2;
		textList = Array.fill(numControls, {"text"});
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("marielorwedontneedlight_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}