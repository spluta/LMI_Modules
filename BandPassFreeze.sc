BandPassFreeze_Mod : Module_Mod {
	var win, frozenAudioBus, fftBus, levelBus, updateDisplayRout, volDisplay, displayVol, volumeDisplay, onOff, rout, volBus, threshBus, onOffBus, buffers, soundGroup, panGroup, transferBus, getTrig, trigButton, waitForSet, count;

	*initClass {
		StartUp.add {
			SynthDef("bandPassFreeze2_mod", { arg audioInBus, transferBus/*, buffer, buf0, buf1, buf2*/, thresh, low = 0.1, hi = 0.5, t_trig=0, gate = 1, pauseGate = 1;
				var audioIn, fftIn, chain, chain0, chain1, chain2, outSig, trig0, trig1, trig2, trig, amp, pan0, pan1, pan2, env, env0, env1, env2, outSig0, outSig1, outSig2, pauseEnv, muteEnv, buf0, buf1, buf2, buffer;

				audioIn = In.ar(audioInBus)*EnvGen.kr(Env.dadsr(1,0,0,1,0), 1);

				amp = Amplitude.kr(audioIn);

				buffer = LocalBuf(2048);

				buf0 = LocalBuf(2048);
				buf1 = LocalBuf(2048);
				buf2 = LocalBuf(2048);

				//trig = Trig1.ar(amp-thresh,0.01);

				trig = Coyote.kr(audioIn, thresh: thresh, minDur: 0.1)+Trig1.kr(Decay.kr(t_trig, 0.1)-0.1, 0.1);

				SendTrig.kr(trig, 10, 1);

				trig0 = Trig1.ar(PulseDivider.kr(trig, 3, 0), 0.02);
				trig1 = Trig1.ar(PulseDivider.kr(trig, 3, 1), 0.02);
				trig2 = Trig1.ar(PulseDivider.kr(trig, 3, 2), 0.02);

				chain = FFT(buffer, audioIn);

				chain0 = PV_Copy(chain, buf0);
				chain1 = PV_Copy(chain, buf1);
				chain2 = PV_Copy(chain, buf2);

				chain0 = PV_Freeze(chain0, 1 - (trig0+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));
				chain1 = PV_Freeze(chain1, 1 - (trig1+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));
				chain2 = PV_Freeze(chain2, 1 - (trig2+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));

				chain0 = PV_BrickWall(chain0, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));
				chain1 = PV_BrickWall(chain1, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));
				chain2 = PV_BrickWall(chain2, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));

				outSig0 = IFFT(chain0);
				outSig1 = IFFT(chain1);
				outSig2 = IFFT(chain2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(1.1,1,0.1), gate, doneAction:2);

				Out.ar(transferBus, [outSig0, outSig1, outSig2]);
			}).writeDefFile;

			SynthDef("bpfPan2_mod", {arg transferBus, audioOutBus, onOff = 1, vol=0, pauseGate = 1, gate = 1;
				var in, pan0, pan1, pan2, outSig0, outSig1, outSig2, outSig, pauseEnv, muteEnv, env;

				in = In.ar(transferBus, 3);

				pan0 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				pan1 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				pan2 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				outSig0 = Pan2.ar(in[0], pan0, 1);
				outSig1 = Pan2.ar(in[1], pan1, 1);
				outSig2 = Pan2.ar(in[2], pan2, 1);

				outSig = outSig0+outSig1+outSig2;

				outSig = Compander.ar(outSig, outSig,
					thresh: 0.8,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				muteEnv = EnvGen.kr(Env.asr(0.001, 1, 0.001), onOff);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				outSig = outSig*vol*env*pauseEnv*muteEnv;

				Out.ar(audioOutBus, outSig);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BandPassFreeze", );
		this.initControlsAndSynths(5);

		soundGroup = Group.head(group);
		panGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 3);

		this.makeMixerToSynthBus;

		synths = List.newClear(2);

		count = 0;
		this.goForIt;

		levelBus = Bus.control(group.server, 1);

		onOff = 0;

	}

	goForIt {

		synths.put(0, Synth("bandPassFreeze2_mod", [\audioInBus, mixerToSynthBus.index, \transferBus, transferBus/*, \buffer, buffers[0].bufnum, \buf0, buffers[1].bufnum, \buf1, buffers[2].bufnum, \buf2, buffers[3].bufnum*/, \thresh, 1], soundGroup));

		synths.put(1, Synth("bpfPan2_mod", [\transferBus, transferBus, \audioOutBus, outBus, \vol, 0], panGroup));


		controls.add(QtEZSlider("Amp", ControlSpec(0.001, 4, \amp),
			{arg slider;
				synths[1].set(\vol, slider.value);
		}, 0, true, orientation:\horz));
		this.addAssignButton(0, \continuous);

		controls.add(QtEZSlider("Thresh", ControlSpec(0.0, 1.0, \cos), {arg slider;
			synths[0].set(\thresh, slider.value);
		}, 0.1, true, orientation:\horz));
		this.addAssignButton(1, \continuous);

		controls.add(QtEZSlider("Speed", ControlSpec(0.025, 12.0, \linear), {arg slider;
			synths[0].set(\low, slider.value, \hi, slider.value+(2*(slider.value.sqrt)));
		}, 0.1, true, orientation:\horz));
		this.addAssignButton(2, \continuous);

		controls.add(Button()
			.states_([
				["On", Color.green, Color.black],
				["Off", Color.black, Color.red]
			])
			.action_{arg butt;
				synths[1].set(\onOff, (butt.value+1).wrap(0,1))
		});
		this.addAssignButton(3,\onOff);

		controls.add(Button()
			.states_([
				["manTrig", Color.red, Color.blue],
				["manTrig", Color.blue, Color.red]
			])
			.action_{arg butt;
				synths[0].set(\t_trig, 1)
		});
		this.addAssignButton(4,\onOff);

		trigButton = Button()
		.states_([["trig", Color.red, Color.blue],["trig", Color.blue, Color.red]]);

		getTrig = OSCFunc({|msg, time|
			if(msg[2]==10, {
				{trigButton.value = (trigButton.value+1).wrap(0,1)}.defer
		})}, '/tr');

		win.bounds = Rect(760.0, 644.0, 361.0, 84.0);
		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				HLayout(controls[2], assignButtons[2]),
				HLayout(controls[3], assignButtons[3], controls[4],  assignButtons[4], trigButton)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];


		win.front;

	}

	/*	//overriding the load method to accomodate the late buffer allocation
	load {arg loadArray;
	var routB;

	routB = Routine({
	20.do{
	if (waitForSet == false, {
	0.5.wait;
	},{
	loadArray[1].do{arg controlLevel, i;
	//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
	if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
	controls[i].valueAction_(controlLevel);
	});
	};

	loadArray[2].do{arg msg, i;
	waitForSetNum = i;
	if(msg!=nil,{
	MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
	assignButtons[i].instantButton.value_(1);
	})
	};

	if(win!=nil,{
	win.bounds_(loadArray[3]);
	win.visible_(false);
	});

	this.loadExtra(loadArray);
	routB.stop;
	})
	}
	});

	AppClock.play(routB);
	}*/

	killMeSpecial {
		getTrig.stop;
	}


}
