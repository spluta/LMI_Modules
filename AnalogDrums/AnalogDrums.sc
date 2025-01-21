AnalogDrums_Mod : Module_Mod {
	var data, nButtons, numControls, textList, dataSynth, dataBuf, delta, offTime, delayedFunc, audioBuf, dataLoaded;

	*initClass {
		StartUp.add {
			SynthDef(\analogDrums_mod,{
				var data = FluidBufToKr.kr(\dataBuf.kr(0), 0, 5);
				var kind = data[0];
				var start = Select.kr(data[0], [data[3], data[3], data[1]]);
				var end = Select.kr(data[0], [data[4], data[4], data[1]+TRand.kr(500,3000,\t_trig.tr)]);
				var jumpTo = data[1];
				var rate = Select.kr(data[0], [DC.kr(1), DC.kr(-1), DC.kr(1)]);
				var sound = BufRd.ar(2, \audioBuf.kr(0), Phasor.ar(\t_trig.tr, BufRateScale.kr(\audioBuf.kr)*rate*\env.kr(0), start, end, jumpTo), loop:1);
				var env2 = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
				Out.ar(\outBus.kr(0), sound*\env.kr*env2*\vol.kr(0));
			}).writeDefFile;
		}
	}

	loadExtra {
	}

	init {
		dataLoaded = false;
		delta = 0;
    	offTime = Main.elapsedTime;
		path = PathName(this.class.filenameSymbol.asString).pathOnly;
		numControls = 8;
		nButtons = NButtons(numControls,4);
		textList = List.fill(numControls, {|i| "playButton"++(i+1)});

		data = FluidDataSet.new(group.server);
		dataBuf = Buffer.alloc(group.server, 5);
		
		this.initControlsAndSynths(numControls+3);

		synths.add(nil);

		numControls.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
				{arg val;
					var synthNum = nButtons.buttonChange(i,val);
					synthNum.postln;
					if(dataLoaded){
						"doit".postln;
						if(val == 1){
							synths[0].set(\env, 1, \t_trig, 1);
							data.getPoint(synthNum.asString, dataBuf);
							
						}{
							if (synthNum!=0){
								delayedFunc = {
									synths[0].set(\env, 1, \t_trig, 1);
									data.getPoint(synthNum.asString, dataBuf);
								};
								SystemClock.sched(0.01,{delayedFunc.value; nil});
							}{
								delta = (Main.elapsedTime - offTime);
								if(delta<0.01){
									delayedFunc = {};
								};
							};
							offTime = Main.elapsedTime;
						}
					}
				},
				true));
		};

		controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "mute", {
			arg val;
			if(val.value == 1){
				synths[0].set(\env, 0);
			};
		}));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 9, "vol", {
			arg val;

			synths[0].set(\vol, val.value)
		}));

		controls.add(TextObject(this, "path", {
			arg path;

			synths[0].set(\gate, 0);

			path = PathName(path).pathOnly;
			
			controls[10].value_(path);
			
			//PathName("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/AnalogDrums/euro_drums_1/audioBuf.*").isFile;

			if (PathName(path++"dataSet.json").isFile and: PathName(path++"audioBuf.wav").isFile) {
				data.read(path++"dataSet.json", {
					data.getPoint("1", dataBuf);
				});
				audioBuf.free;
				audioBuf = Buffer.read(group.server, path++"audioBuf.wav", action:{|buf|
					synths[0]=Synth(\analogDrums_mod, [\outBus, outBus, \rate, 0, \audioBuf, buf, \dataBuf, dataBuf], group);
				});
				dataLoaded = true;
			} {
				"error: path does not contain dataset.json and audioBuf.json".postln;
				dataLoaded = false;

			}

		}));

		this.makeWindow2;

		// audioBuf = Buffer.read(group.server, "/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/AnalogDrums/audioBuf.wav", action:{
		// 	synths.add(Synth(\analogDrums_mod, [\outBus, outBus, \rate, 0, \audioBuf, audioBuf, \dataBuf, dataBuf], group));
		// });
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



// AnalogDrums_Mod : Module_Mod {
// 	var data, nButtons, numControls, textList, dataSynth, dataBuf, delta, offTime, delayedFunc, audioBuf;

// 	*initClass {
// 		StartUp.add {
// 			SynthDef(\analogDrums_mod,{
// 				var data = FluidBufToKr.kr(\dataBuf.kr(0), 0, 5);
// 				//var env = Lag.kr(\rate.kr(0).abs>0, 0.01);
// 				var kind = data[0];
// 				var start = Select.kr(data[0], [data[3], data[3], data[1]]);
// 				var end = Select.kr(data[0], [data[4], data[4], data[1]+TRand.kr(500,3000,\t_trig.tr)]);
// 				var jumpTo = data[1];
// 				var rate = Select.kr(data[0], [DC.kr(1), DC.kr(-1), DC.kr(1)]);
// 				var sound = BufRd.ar(2, \audioBuf.kr(0), Phasor.ar(\t_trig.tr, BufRateScale.kr(\audioBuf.kr)*rate*\env.kr(0), start, end, jumpTo), loop:1);
// 				var env2 = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
// 				Out.ar(\outBus.kr(0), sound*\env.kr*env2*\vol.kr(0));
// 			}).writeDefFile;
// 		}
// 	}

// 	loadExtra {
// 	}

// 	init {
// 		delta = 0;
//     	offTime = Main.elapsedTime;
// 		path = PathName(this.class.filenameSymbol.asString).pathOnly;
// 		numControls = 8;
// 		nButtons = NButtons(numControls,4);
// 		textList = List.fill(numControls, {|i| "playButton"++(i+1)});

// 		data = FluidDataSet.new(group.server);
// 		dataBuf = Buffer.alloc(group.server, 5);
		
// 		data.read(path++"dataSet.json", {
// 			data.getPoint("1", dataBuf);
// 		});
		
// 		this.initControlsAndSynths(numControls+2);

// 		numControls.do{arg func, i;
// 			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
// 				{arg val;
// 					var synthNum = nButtons.buttonChange(i,val);

// 					if(val == 1){
// 						synths[0].set(\env, 1, \t_trig, 1);
// 						data.getPoint(synthNum.asString, dataBuf);
						
// 					}{
// 						if (synthNum!=0){
// 							delayedFunc = {
// 								synths[0].set(\env, 1, \t_trig, 1);
// 								data.getPoint(synthNum.asString, dataBuf);
// 							};
// 							SystemClock.sched(0.01,{delayedFunc.value; nil});
// 						}{
// 							delta = (Main.elapsedTime - offTime);
// 							if(delta<0.01){
// 								delayedFunc = {};
// 							};
// 						};
// 						offTime = Main.elapsedTime;
// 					}
// 				},
// 				true));
// 		};

// 		controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "mute", {
// 			arg val;
// 			if(val.value == 1){
// 				synths[0].set(\env, 0);
// 			};
// 		}));

// 		controls.add(TypeOSCFuncObject(this, oscMsgs, 9, "vol", {
// 			arg val;

// 			synths[0].set(\vol, val.value)
// 		}));

// 		this.makeWindow2;

// 		audioBuf = Buffer.read(group.server, "/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/AnalogDrums/audioBuf.wav", action:{
// 			synths.add(Synth(\analogDrums_mod, [\outBus, outBus, \rate, 0, \audioBuf, audioBuf, \dataBuf, dataBuf], group));
// 		});
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

