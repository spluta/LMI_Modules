FB100B_Mod : Module_Mod {
	var data, nButtons, numControls, textList;

	*initClass {
		StartUp.add {
			SynthDef(\fb100B_mod,{
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

				Out.ar(\outBus.kr(0), snd*\vol.kr(0.2)*\env.kr(1, 0.001)/2);
			}).writeDefFile;
		}
	}

	loadExtra {
	}

	init {
		numControls = 8;
		nButtons = NButtons(8,4);
		textList = List.fill(numControls, {|i| "playButton"++i});
		//textList.add("clearButton")

		data = Object.readArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/FB100B/data1");

		this.initControlsAndSynths(numControls+2);

		numControls.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
				{arg val;
					var synthNum = nButtons.buttonChange(i,val);
					if(val == 1){
						synths[0].set(*data[synthNum.asSymbol]);
						synths[0].set(\env, 1);
					}{
						if (synthNum>0){
							synths[0].set(*data[synthNum.asSymbol])
						}{
							synths[0].set(\env, 0);
						}
					}
				},
				true));
		};

		controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "vol", {
			arg val;

			synths[0].set(\vol, val.value)
		}));

		this.makeWindow2;

		synths.add(Synth(\fb100B_mod, [\outBus, outBus, \env, 0, \locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4], group));
	}

	makeWindow2 {
		var temp;

		temp = this.class.asString;
		this.makeWindow(temp.copyRange(0, temp.size-5));

		win.layout_(
			VLayout(
				VLayout(*controls.copyRange(0,controls.size).collect({arg item; item}))
		));
		win.layout.spacing_(1).margins_(1!4);
		win.view.resizeTo(10*17,numControls+15*17);

		win.front;
	}
}
