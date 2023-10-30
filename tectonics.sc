Tectonics_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("tectonics_feedback_mod", {
				var snd, n, trig, locIn, verb, envs, in;
				n = 4;
				snd = Impulse.ar(0)!n;

				trig = NamedControl.tr(\trig, 0, 0);//\trig.tr(0, 0);
				//trig = MouseButton.kr(0, 1, 0);

				in = [In.ar(\inBus0.kr, 2), In.ar(\inBus1.kr, 2)].flatten*{TRand.ar(200,1000, trig!n)};

				locIn = LocalIn.ar(n)*0.1+in.poll;

				locIn = SelectX.ar(TWChoose.kr(trig, [0,1], [0.6,0.4].normalizeSum).lag(0.01),
					[locIn, BitCrusher.ar(locIn, {(TRand.ar(1,32, trig!n))}, {TRand.ar(200,40000,trig!n)})]
				);

				locIn = BHiShelf.ar(locIn, TExpRand.ar(1000,10000,trig), 1, TRand.ar(0, -40, trig));

				snd = snd + locIn.sum;

				snd = Resonz.ar(snd, {TExpRand.kr(0.0001, 10000, trig!n) }, {TRand.kr(0.01, 0.3, trig!n) });
				snd = Integrator.ar(snd, {TRand.kr(0.97, 0.99, trig!n) });

				snd = snd * ({ { LFNoise1.kr(TRand.kr(0.001, 0.1, trig)).range(TRand.kr(-500,500, trig)) } ! n } ! n);
				snd = snd.sum;
				snd = LeakDC.ar(snd);

				LocalOut.ar(DelayC.ar(snd.clip2(TRand.ar(100,1000,trig)).(LPF.ar(_, 30000)),512/SampleRate.ir,(({TRand.ar(0,1.0,trig!n)}**8)*512/SampleRate.ir)));

				snd = snd.softclip.select{|item, i| i<4};

				snd = snd.(LPF.ar(_, 20000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

				envs = Envs.kr(\muteGate.kr(0), \pauseGate.kr(1), \gate.kr(1));

				snd = snd*0.7*envs*\vol.kr(0);

				Out.ar(\outBus.kr, snd);

			}).writeDefFile;


		}

	}

	init3 {

		synthName = "Tectonics";

		win.name = "Techtonics"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		this.initControlsAndSynths(3);

		dontLoadControls = (0..1);

		synths.add(Synth("tectonics_feedback_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], group));
		synths.add(Synth("tectonics_feedback_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], group));

		controls.add(Button()
			.states_([
				["Go", Color.black, Color.white],
				["Go", Color.white, Color.black]
			])
			.action_{arg butt;

				if(butt.value.postln==1){
					synths.do{|item| item.set(\trig, 1, \muteGate, 1)}
				};
			}
		);
		this.addAssignButton(0, \onOff);

		controls.add(Button()
			.states_([
				["Stop", Color.black, Color.white],
				["Stop", Color.white, Color.black]
			])
			.action_{arg butt;
				if(butt.value==1){
					synths.do{|item| item.set(\muteGate, 0)}
				};
			}
		);
		this.addAssignButton(1, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,1,'amp'), {|v|
			synths.do{|item| item.set(\vol, v.value)};
		}, 0, true, 'horz', false, false));

		this.addAssignButton(2, \continuous);

		win.layout_(
			VLayout(
				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				HLayout(controls[2], assignButtons[2])
			)
		);
		win.layout.spacing = 1;
		win.layout.margins = [1,1,1,1];
		win.front;
	}

	pause {
		mixerStrips.do{|item| item.mute};
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
	}

	unpause {
		mixerStrips.do{|item| item.unmute};
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
	}

}




