SpaceInterruption_Mod : SignalSwitcher_Mod {
	//modified from Alik Rustamoff's implementation of the Lexicon reverb

	var volBus, djBus, synthSeq, currentSynth, buttonVal, decayRange;

	*initClass {
		StartUp.add {
			SynthDef("spaceInterruption_thruSig_mod", {
				Out.ar(\outBus.kr(0), In.ar(\inBus.kr, 2)*\thruMute.kr(1));
			}).writeDefFile;

			SynthDef("spaceInterruption_mod", { arg
				predelay = 0.0,
				input_diff_1 = 0.78512552440328,
				input_diff_2 = 0.4613229053115,
				bandwidth = 0.96,		// input bandwidth - randomize
				//decay = 0.9,	// tank decay - 0.8 to 1ish
				decay_diff_1 = 0.4,
				decay_diff_2 = 0.8;
				// tank bandwidth - random 0.2 to 0.8
				var src, in, in1, local;

				var sound, vol, impulse;

				src = In.ar(\inBus.kr, 2)*\inVol.kr(1, 0.01);

				sound = LexiconVerb.ar(src, input_diff_1, input_diff_2, \damping.kr(0.57, 0.1), decay_diff_1, decay_diff_2, \decay.kr(0.9,0.1), predelay, bandwidth);

				vol = In.kr(\volBus.kr);

				Out.ar(\outBus.kr, sound*\isCurrent.kr(0, 0.1)*vol*\outMul.kr(0));
			}).writeDefFile;
		}
	}

	init3 {
		synthName = "SpaceInterruption";

		win.name = "SpaceInterruption"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
		this.initControlsAndSynths(5);

		volBus = Bus.control(group.server);
		djBus = Bus.control(group.server);

		decayRange = [0.8,0.9];

		2.do{|i|
			3.do{synths.add(Synth("spaceInterruption_mod", [\inBus, localBusses[i], \outBus, outBus, \volBus, volBus, \djBus, djBus, \input_diff_1, rrand(0.4,0.9), \input_diff_2, rrand(0.4,0.9)], outGroup))};
		};

		synths.add(Synth("spaceInterruption_thruSig_mod", [\inBus, localBusses[0], \outBus, outBus], outGroup));

		synthSeq = [Pseq((0..2), inf).asStream, Pseq((3..5), inf).asStream];
		currentSynth = [synthSeq[0].next, synthSeq[1].next];

		["left", "right"].do{|string, num|
			controls.add(Button()
				.states_([[ string+"thru", Color.black, Color.green ], [ string+"verb", Color.green, Color.black ]])
				.action_{|v|
					if(v.value==0){
						var temp = currentSynth[num];
						synths[temp].set(\outMul, 0, \inVol, 1, \decay, 0.1, \damping, rrand(0.4,0.7));
						SystemClock.sched(0.2, {synths[temp].set(\decay, 0.7); nil});
						if(controls.copyRange(0,1).collect{|item| item.value}.sum==0){synths[6].set(\thruMute, 1);};
					}{
						synths[6].set(\thruMute, 0);
						if(num==0){
							if(controls[1].value==1){
								controls[1].valueAction_(0)
							}
						}{
							if(controls[0].value==1){
								controls[0].valueAction_(0)
							}
						};
						synths[currentSynth[num]].set(\inVol, 1, \isCurrent, 0);
						currentSynth[num] = synthSeq[num].next;
						synths[currentSynth[num]].set(\outMul, 1, \inVol, 0, \isCurrent, 1, \decay, rrand(decayRange[0], decayRange[1]));
					};
			});
			this.addAssignButton(num,\onOff);
		};

		currentSynth.do{|val| synths[val].set(\isCurrent, 1)};

		controls.add(QtEZSlider("vol", ControlSpec(0, 2, 'amp'), {|val| volBus.set(val.value)}, 0, true, 'horz'));
		this.addAssignButton(2, \continuous);

		controls.add(QtEZRanger(\decay, ControlSpec(0.75,1.1), {|val|
			decayRange = val.value;
		}, [0.8, 0.9], true, 'horz'));

		win.layout_(
			HLayout(
				VLayout(
					HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
					HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15)),
					HLayout(assignButtons[0], assignButtons[1]),
					HLayout(controls[2], assignButtons[2].maxWidth_(40)),
					controls[3];
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	killMeSpecial {
		volBus.free;
		djBus.free;
	}
}