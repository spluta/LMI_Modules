Proteus_Test_Mod : Module_Mod {
	var nButtons, numControls, textList, models_dict, prot_stream;

	*initClass {
		StartUp.add {
			SynthDef(\proteus_mod,{
				var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				var out, in = In.ar(\inBus.kr(0), 2);

				var prt1 = Proteus.ar(in, \gain_knob.kr(0), \double_trouble1, \bypass1.kr(1));
				var prt2 = Proteus.ar(in, \gain_knob.kr, \double_trouble2, \bypass2.kr(1));
				
				in = in*Env.asr(0.05,1,0.05).kr(0, \in_gate.kr(1));
				prt1 = prt1*Env.asr(0.05,1,0.01).kr(0, \prt1_gate.kr(1));
				prt2 = prt2*Env.asr(0.05,1,0.01).kr(0, \prt2_gate.kr(1));

				out = (in*\mix_vol.kr(0))+prt1+prt2;

				Out.ar(\outBus.kr(0), out*\vol.kr(0.2)/2*envs);
			}).writeDefFile;
		}
	}

	loadExtra {
	}

	init {
		this.makeMixerToSynthBus(2);
		numControls = 8;
		this.initControlsAndSynths(numControls+4);

		synths.add(Synth(\proteus_mod, [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		nButtons = NButtons(8,4);
		textList = List.fill(numControls, {|i| "playButton"++i});

		models_dict = Object.readArchive(PathName(Proteus_Mod.filenameSymbol.asString).pathOnly+/+"models_dict");

		prot_stream = Pseq([1,2], inf).asStream;

		numControls.do{arg i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
				{arg val;
					var synthNum = nButtons.buttonChange(i,val);
					var next_model = models_dict[synthNum.asSymbol];
					if (synthNum>0){
						switch(prot_stream.next)
						{1}{
							Proteus.loadModel(synths[0], \double_trouble1, next_model[0], 0);
							synths[0].set(\bypass1, 0, \bypass2, 1, \in_gate, 0, \prt1_gate, 1, \prt2_gate, 0);
						}
						{2}{
							Proteus.loadModel(synths[0], \double_trouble2, next_model[0], 0);
							synths[0].set(\bypass1, 1, \bypass2, 0, \in_gate, 0, \prt1_gate, 0, \prt2_gate, 1);
						}
						{"whoops".postln};
					}
				},
				true));
		};

		controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "off button",
			{arg val;
				synths[0].set(\bypass1, 1, \bypass2, 1, \in_gate, 1, \prt1_gate, 0, \prt2_gate, 0);
			},
			true)
		);

		controls.add(TypeOSCFuncObject(this, oscMsgs, 9, "mix", {
			arg val;

			synths[0].set(\mix_vol, val.value)
		}));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 10, "gain_knob", {
			arg val;

			synths[0].set(\gain_knob, val.value)
		}));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 11, "vol", {
			arg val;

			synths[0].set(\vol, val.value)
		}));

		this.makeWindow2;
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
		//dataSynth.free;
	}
}

Proteus_FX_Mod : Module_Mod {
	var nButtons, numControls, textList, models_dict, prot_stream;

	*initClass {
		StartUp.add {
			SynthDef(\proteus_in_mod,{
				var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
			
				var out, in = In.ar(\inBus.kr(0), 2);
				var num_prots = 12;

				var bypass = LagUD.kr(1-\prt_gate.kr((0!num_prots)), 0.05, 0);
			
				var prt_l = Proteus.ar(in[0], \gain_knob.kr(0, 0.1), (1..num_prots).collect{|item|item.asSymbol}, bypass);
				var prt_r = Proteus.ar(in[1], \gain_knob.kr, (1..num_prots).collect{|item|item.asSymbol}, bypass);
				
				in = in*Env.asr(0.05,1,0.05).kr(0, \in_gate.kr(1));
				prt_l = prt_l*Env.asr(0.01,1,0.01).kr(0, \prt_gate.kr);
				prt_r = prt_r*Env.asr(0.01,1,0.01).kr(0, \prt_gate.kr);
			
				out = (in*\direct_vol.kr(0))+([Mix(prt_l),Mix(prt_r)]*\mix_vol.kr(0, 0.1));

				out = LeakDC.ar(out);
			
				Out.ar(\outBus.kr(0), out*envs);
			}).writeDefFile;
		}
	}

	init {
		this.makeMixerToSynthBus(2);
		numControls = 12;
		this.initControlsAndSynths(numControls+4);

		synths.add(Synth(\proteus_in_mod, [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		textList = List.fill(numControls, {|i| "playButton"++i});

		models_dict = Object.readArchive(PathName(Proteus_FX_Mod.filenameSymbol.asString).pathOnly+/+"models_dict");

		[176,48,52,3,49,104,37,140,36,196,43,8].do{|item, i|
			Proteus.loadModel(synths[0], (i+1).asSymbol, models_dict[item.asSymbol][0], false);
		};

		prot_stream = Pseq([1,2], inf).asStream;

		numControls.do{arg i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, textList[i],
				{arg val;
					if(val==1){
						var setArray = (0!12);
						setArray.put(i, 1);
						synths[0].set(\prt_gate, setArray);
					}
				},
				true));
		};

		controls.add(TypeOSCFuncObject(this, oscMsgs, 12, "off button",
			{arg val;
				var setArray = (0!12);
				synths[0].set(\prt_gate, setArray);
			},
			true)
		);

		controls.add(TypeOSCFuncObject(this, oscMsgs, 13, "direct", {
			arg val;

			synths[0].set(\direct_vol, val.value)
		}));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 14, "gain_knob", {
			arg val;

			synths[0].set(\gain_knob, val.value)
		}));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 15, "mix_vol", {
			arg val;

			synths[0].set(\mix_vol, val.value)
		}));

		this.makeWindow2;
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
		//dataSynth.free;
	}
}
