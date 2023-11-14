// RingMod2_Mod : ProtoType_Mod {
//
// *initClass {
// StartUp.add {
// SynthDef(\ringmod2_mod,{arg inBus, controlsBus, outBus;
// var ctlIn = In.kr(controlsBus, 6);
// var freq0= ctlIn[0].linexp(0,1,20, 250).lag;
// var freq0= ctlIn[1].linexp(0,1,150, 2000).lag;
// var vol = ctlIn[5].lincurve(0,1,0, 1).lag;
//
// var noise, sig;
// var noiseFreq=ctlIn[2].linexp(0,1,200, 20000).lag;
// var noiseMul =ctlIn[3].linlin(0,1,1, 2000).lag;
// var onOff = (ctlIn[1]+ctlIn[4]).clip(0,1).lag;
//
// noise = LFNoise1.ar(noiseFreq)*noiseMul;
// sig = SinOsc.ar(([freq, freq]+LFNoise2.ar([0.1,0.12], 3)+noise).clip(40, 20000));
//
// sig = sig*In.ar(inBus,2)*4;
//
// sig = (T312AX7.ar(sig)).softclip;
//
// Out.ar(outBus, sig*vol*onOff);
//
// }).writeDefFile;
// }
// }
//
// loadExtra {}
//
// init {
// numControls = 7;
// textList = Array.with("freq", "onOff", "noiseFreq", "noiseMul", "onOff", "vol");
// withTextView = false;
// this.init2;
// }
//
// init3 {
// synths.add(Synth("ringmod2_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
// }
// }