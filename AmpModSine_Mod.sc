AmpModSine_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\ampmodsine_mod,{arg inBus, controlsBus, outBus;
//eric's amplitude modulate for the end of his modular piece

var in = SoundIn.ar(46);
var lfoSpeed = In.kr(controlsBus).linlin(0,1,10,20);

var vol = In.kr(controlsBus+1);

Out.ar(outBus, in.dup*SinOsc.ar(lfoSpeed).range(0,1)*vol);
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
synths.add(Synth("ampmodsine_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
}
}