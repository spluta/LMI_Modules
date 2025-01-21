FB100B_Mod : Module_Mod {
	var data, nButtons, numControls, textList, dataSynth, dataBuf, dataBus, dataGroup, playGroup;

	*initClass {
		StartUp.add {
			SynthDef(\fb100B2_mod,{
				var snd, num, trig0, trig1, trig, locIn, verb, env, vol;
				var controls = In.kr(\dataBus.kr(0), 57);
				
				var locInChoose = controls[0];
				var bitRand1 = controls[1..4];
				var bitRand2 = controls[5..8];
				var hiShelfFreq = controls[9];
				var hiShelfDB = controls[10];
				var resonzFreq = controls[11..14];
				var bwr = controls[15..18];
				var integratorVal = controls[19];
				var sndMult = controls[20..35];
				var sndRange = controls[36..51];
				var clip  = controls[52];
				var timeRoot = controls[53..56];

				var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				num = 4;

				snd = Impulse.ar(0)!num;

				locIn = LocalIn.ar(num)*0.1;

				locIn = SelectX.ar(locInChoose,
					[locIn, BitCrusher.ar(locIn, bitRand1, bitRand2)]
				);

				locIn = BHiShelf.ar(locIn, hiShelfFreq, 1, hiShelfDB);

				snd = snd + locIn.sum;

				snd = Resonz.ar(snd,resonzFreq, bwr);
				snd = Integrator.ar(snd, integratorVal);

				snd = snd * LFNoise1.kr(sndMult).range(sndRange);
				snd = [(snd*[1,0,0,0]).sum, (snd*[0,1,0,0]).sum, (snd*[0,0,1,0]).sum, (snd*[0,0,0,1]).sum];

				snd = LeakDC.ar(snd);

				LocalOut.ar(DelayC.ar(snd.clip2(clip).(LPF.ar(_, SampleRate.ir/2)),512/SampleRate.ir,((timeRoot**8)*512/SampleRate.ir)));

				snd = T312AX7.ar(snd);
				snd = snd.softclip;
				snd = snd.select{|item, i| i<2}+Pan2.ar(snd[2], -0.5)+Pan2.ar(snd[3], 0.5);

				snd = snd.(LPF.ar(_, 20000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

				Out.ar(\outBus.kr(0), snd*\vol.kr(0.2)*\env.kr(1, 0.001)/2*envs);
			}).writeDefFile;
		}
	}

	loadExtra {
	}

	init {
		numControls = 8;
		nButtons = NButtons(8,4);
		textList = List.fill(numControls, {|i| "playButton"++i});
		dataBus = Bus.control(group.server, 57);
		dataBus.setn([1, 25, 2, 10, 20, 711, 35948, 10978, 10536, 1457, -11, 2.1064851926035, 0.00032819650152758, 1232.8821319597, 1020.2633017861, 0.011751627922058, 0.15087965726852, 0.094749226570129, 0.21772589683533, 0.98975873231888, 0.032193231463432, 0.0044004176855087, 0.029635562777519, 0.076149999141693, 0.037872407793999, 0.016901365637779, 0.083858563780785, 0.027987971901894, 0.021130641698837, 0.0016615217924118, 0.022890596747398, 0.023032901883125, 0.075504350662231, 0.029924197435379, 0.092113111376762, 0.053760142445564, -460, -425, 323, -401, 182, -366, -11, -436, 213, -240, -284, -476, 342, 173, -415, -322, 982, 0.9121161699295, 0.58438122272491, 0.79945504665375, 0.55237007141113]);

		dataGroup = Group.head(group);
		playGroup = Group.tail(group);

		data = FluidDataSet.new(group.server);
		dataBuf = Buffer.alloc(group.server, 57);
		data.read("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/FB100B/dataSet.json", {
			data.getPoint("1", dataBuf);
		});
		
		dataSynth = {
			Out.kr(dataBus, FluidBufToKr.kr(dataBuf))
		}.play(dataGroup);
		
		this.initControlsAndSynths(numControls+2);

		numControls.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
				{arg val;
					var synthNum = nButtons.buttonChange(i,val);
					if(val == 1){
						//synths[0].set(*data[synthNum.asSymbol]);
						//dataSynth.set(\synthNum, synthNum);
						data.getPoint(synthNum.asString, dataBuf);
						synths[0].set(\env, 1);
					}{
						if (synthNum>0){
							data.getPoint(synthNum.asString, dataBuf);
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

		synths.add(Synth(\fb100B2_mod, [\outBus, outBus, \env, 0, \dataBus, dataBus], playGroup));
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

	killMeSpecial {
		dataSynth.free;
	}
}



// FB100B_Mod : Module_Mod {
// 	var data, nButtons, numControls, textList;

// 	*initClass {
// 		StartUp.add {
// 			SynthDef(\fb100B_mod,{
// 				var snd, num, trig0, trig1, trig, locIn, verb, env, controls, vol;
// 				num = 4;
// 				snd = Impulse.ar(0)!num;

// 				locIn = LocalIn.ar(num)*0.1;

// 				locIn = SelectX.ar(\locInChoose.kr(0),
// 					[locIn, BitCrusher.ar(locIn, \bitRand1.kr(1!num), \bitRand2.kr(299!num))]
// 				);

// 				locIn = BHiShelf.ar(locIn, \hiShelfFreq.kr(1000), 1, \hiShelfDB.kr(0));

// 				snd = snd + locIn.sum;

// 				snd = Resonz.ar(snd,\resonzFreq.kr(2000!num), \bwr.kr(0.2!num));
// 				snd = Integrator.ar(snd, \integratorVal.kr(0.98));

// 				snd = snd * LFNoise1.kr(\sndMult.kr(0.05!16)).range(\sndRange.kr(0!16));
// 				snd = [(snd*[1,0,0,0]).sum, (snd*[0,1,0,0]).sum, (snd*[0,0,1,0]).sum, (snd*[0,0,0,1]).sum];

// 				snd = LeakDC.ar(snd);

// 				LocalOut.ar(DelayC.ar(snd.clip2(\clip.kr(200)).(LPF.ar(_, SampleRate.ir/2)),512/SampleRate.ir,((\timeRoot.kr(0.5!4)**8)*512/SampleRate.ir)));

// 				snd = T312AX7.ar(snd);
// 				snd = snd.softclip;
// 				//snd = Clipper8.ar(snd);
// 				snd = snd.select{|item, i| i<2}+Pan2.ar(snd[2], -0.5)+Pan2.ar(snd[3], 0.5);

// 				snd = snd.(LPF.ar(_, 20000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

// 				Out.ar(\outBus.kr(0), snd*\vol.kr(0.2)*\env.kr(1, 0.001)/2);
// 			}).writeDefFile;
// 		}
// 	}

// 	loadExtra {
// 	}

// 	init {
// 		numControls = 8;
// 		nButtons = NButtons(8,4);
// 		textList = List.fill(numControls, {|i| "playButton"++i});
// 		//textList.add("clearButton")

// 		data = Object.readArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/FB100B/data1");

// 		this.initControlsAndSynths(numControls+2);

// 		numControls.do{arg func, i;
// 			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
// 				{arg val;
// 					var synthNum = nButtons.buttonChange(i,val);
// 					if(val == 1){
// 						synths[0].set(*data[synthNum.asSymbol]);
// 						synths[0].set(\env, 1);
// 					}{
// 						if (synthNum>0){
// 							synths[0].set(*data[synthNum.asSymbol])
// 						}{
// 							synths[0].set(\env, 0);
// 						}
// 					}
// 				},
// 				true));
// 		};

// 		controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "vol", {
// 			arg val;

// 			synths[0].set(\vol, val.value)
// 		}));

// 		this.makeWindow2;

// 		synths.add(Synth(\fb100B_mod, [\outBus, outBus, \env, 0, \locInChoose, [0,1].wchoose([0.6,0.4]), \bitRand1, {rrand(1,32)}!4, \bitRand2, {rrand(200,40000)}!4, \hiShelfFreq, rrand(1000, 10000), \hiShelfDB, rrand(-40, 0), \resonzFreq, {exprand(0.0001, 10000)}!4, \bwr, {rrand(0.01,0.3)}!4, \integratorVal, rrand(0.97, 0.99), \sndMult, {rrand(0.001, 0.1)}!16, \sndRange, {rrand(-500,500)}!16, \clip, rrand(100, 1000), \timeRoot, {rrand(0,1.0)}!4], group));
// 	}

// 	makeWindow2 {
// 		var temp;

// 		temp = this.class.asString;
// 		this.makeWindow(temp.copyRange(0, temp.size-5));

// 		win.layout_(
// 			VLayout(
// 				VLayout(*controls.copyRange(0,controls.size).collect({arg item; item}))
// 		));
// 		win.layout.spacing_(1).margins_(1!4);
// 		win.view.resizeTo(10*17,numControls+15*17);

// 		win.front;
// 	}
// }
