


(SynthDef(\fb100_mod,{
	var snd, num, trig0, trig1, trig, locIn, verb, env, controls, vol;
	num = 4;
	snd = Impulse.ar(0)!num;

	locIn = LocalIn.ar(num)*0.1;

	locIn = SelectX.ar(\locInChoose.kr(0),
		[locIn, BitCrusher.ar(locIn, \bitRand1.kr(1!num), \bitRand2.kr(299!num))]
	);

	locIn = BHiShelf.ar(locIn, \hiShelfFreq.kr(1000), 1, \hiShelfDB.kr(0));



	snd = snd + locIn.sum;

	snd = Resonz.ar(snd,\resonzFreq.kr(2000!num), \bwr.kr(0.2!num));
	snd = Integrator.ar(snd, \integratorVal.kr(0.98));

	snd = snd * LFNoise1.kr(\sndMult.kr(0.05!16)).range(\sndRange.kr(0!16));
	snd = [(snd*[1,0,0,0]).sum, (snd*[0,1,0,0]).sum, (snd*[0,0,1,0]).sum, (snd*[0,0,0,1]).sum];

	snd = LeakDC.ar(snd);

	LocalOut.ar(DelayC.ar(snd.clip2(\clip.kr(200)).(LPF.ar(_, SampleRate.ir/2)),512/SampleRate.ir,((\timeRoot.kr(0.5!4)**8)*512/SampleRate.ir)));

	snd = T312AX7.ar(snd);
	snd = snd.softclip;
	//snd = Clipper8.ar(snd);
	snd = snd.select{|item, i| i<2}+Pan2.ar(snd[2], -0.5)+Pan2.ar(snd[3], 0.5);

	snd = snd.(LPF.ar(_, 20000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

	Out.ar(\outBus.kr(0), snd*\vol.kr(0.2)*\env.kr(1)/2);
}).load(s);)

	//sndMult = ({ { LFNoise1.kr(TRand.kr(0.001, 0.1, trig)).range(TRand.kr(-500,500, trig)) } ! num } ! num)

//clip = TRand.ar(100,1000,trig)
				//timeRoot = {TRand.ar(0,1.0,trig!num)}





(~msg = [\locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4].postln;
a.do{|item| item.set(*~msg)};)


Server.local.options.blockSize_(1);
(
//var msgs = List.newClear(0);
var msgs = Object.readArchive("/Users/spluta1/Documents/SC/fixFB100/1697468714");
MIDIClient.init;
MIDIIn.connectAll;

msgs.size.postln;

a = Synth(\fb100_mod, [\locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4, \vol, 0.9]).dup;

MIDIFunc.noteOn({|val, num, chan|
	~msg = [\locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4].postln;
a.do{|item| item.set(*~msg)};
}, 60);

MIDIFunc.noteOn({|val, num, chan|
	msgs.add(~msg);
}, 62);

MIDIFunc.noteOn({|val, num, chan|
	msgs.writeArchive(Document.current.dir+/+Date.getDate.rawSeconds.asInteger);

}, 64);

)


//make the archive for nanoPad2
a = Object.readArchive("/Users/spluta1/Documents/SC/fixFB100/1697470624");

b = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 54, 56, 57, 58, 60, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 86, 88, 89, 90, 92, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 108, 112, 113, 114, 116, 120, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 144, 145, 146, 147, 148, 149, 150, 152, 153, 154, 156, 160, 161, 162, 163, 164, 165, 166, 168, 169, 170, 172, 176, 177, 178, 180, 184, 192, 193, 194, 195, 196, 197, 198, 200, 201, 202, 204, 208, 209, 210, 212, 216, 224, 225, 226, 228, 232, 240];

c = Dictionary();
b.do{|item, i| c.put(item.asSymbol, a[i])}
c.writeArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/FB100B/data1")


(
var msgs = List.newClear(0);

var data = Object.readArchive("/Users/spluta1/Documents/SC/fixFB100/1697424490");
var seq = Pseq(data, inf).asStream;


MIDIClient.init;
MIDIIn.connectAll;

a = Synth(\fb100_mod, [\locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4]);

MIDIFunc.noteOn({|val, num, chan|
	~msg = seq.next.postln;
a.do{|item| item.set(*~msg)};
}, 60);

/*MIDIFunc.noteOn({|val, num, chan|
	msgs.add(~msg);
}, 62);

MIDIFunc.noteOn({|val, num, chan|
	msgs.writeArchive(Document.current.dir+/+Date.getDate.rawSeconds.asInteger);

}, 64);*/

)

