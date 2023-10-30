SpaceTrigger_Mod : SignalSwitcher_Mod {
	//modified from Alik Rustamoff's implementation of the Lexicon reverb

	var volBus, djBus, synthSeq, currentSynth, buttonVal, decayRange;

/*	*initClass {
		StartUp.add {

		}
	}*/

	init3 {
		synthName = "SpaceTrigger";

		win.name = "SpaceTrigger"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
		this.initControlsAndSynths(5);

		volBus = Bus.control(group.server);

		decayRange = [0.85,0.95];

		6.do{|i|
			synths.add(Synth("spaceInterruption_mod", [\inBus, localBusses[0], \outBus, outBus, \volBus, volBus, \input_diff_1, rrand(0.4,0.9), \input_diff_2, rrand(0.4,0.9)], outGroup));
		};

		synthSeq = Pseq((0..5), inf).asStream;
		currentSynth = synthSeq.next;

		controls.add(Button()
			.states_([[ "trigger", Color.black, Color.green ], [ "trigger", Color.green, Color.black ]])
			.action_{|v|
				//mute stuff
				synths[currentSynth].set(\outMul, 0, \inVol, 1, \decay, 0.1, \damping, rrand(0.4,0.7), \isCurrent, 0);
				{|current| SystemClock.sched(0.2, {current.set(\decay, 0.7); nil})}.(synths[currentSynth]);

				currentSynth = synthSeq.next;
				//prepare the next
				synths[currentSynth].set(\outMul, 1, \inVol, 0, \isCurrent, 1, \decay, rrand(decayRange[0], decayRange[1]));

		});
		this.addAssignButton(0,\onOff);

		controls.add(Button()
			.states_([[ "mute", Color.black, Color.green ], [ "mute", Color.green, Color.black ]])
			.action_{|v|
				synths[currentSynth].set(\outMul, 0, \inVol, 1, \delaysMul, 0, \delaysMulLag, 0, \decay, 0.1, \damping, rrand(0.4,0.7), \isCurrent, 0);
				{|current| SystemClock.sched(0.2, {current.set(\decay, 0.7)})}.(synths[currentSynth]);
		});
		this.addAssignButton(1,\onOff);

		currentSynth.do{|val| synths[val].set(\isCurrent, 1)};

		controls.add(QtEZSlider("vol", ControlSpec(0, 2, 'amp'), {|val| volBus.set(val.value)}, 0, true, 'horz'));
		this.addAssignButton(2, \continuous);

		controls.add(QtEZRanger(\decay, ControlSpec(0.8,1.1), {|val|
			decayRange = val.value;
		}, [0.85, 0.95], true, 'vert'));

		win.layout_(
			HLayout(
				VLayout(
					HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
					HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15)),
					HLayout(assignButtons[0], assignButtons[1]),
					HLayout(controls[2], assignButtons[2].maxWidth_(40)),
				),controls[3]
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