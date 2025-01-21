PV_Window_ID {
	classvar idnum = 0;

	*next {
		idnum = idnum+1;
		^idnum;
	}
}

FeedbackControl_Mod : Module_Mod {
	var volBus, volsBus, disp, id, buf, buf2, dispReduce, dispIn, counter, thresh, updateGui=true;

	*initClass {
		//will not reload on startup!
		StartUp.add {

			SynthDef("feedbackControl_mod", {arg inBus, buf, buf2, volsBus, thresh, mulFactor, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, topBin, id=0;
				var in, fft, out, volume, envs, pauseEnv, demand, windowStarts, stream, counter;

				in  = In.ar(inBus);

				in = Compander.ar(in, in, 0.5, 1, 0.5, 0.01, 0.01);

				fft = FFT(buf, in);
				fft = PV_Control(fft, buf2, thresh, mulFactor, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, topBin);

				windowStarts = fft > -1;

				SendReply.kr(windowStarts, '/pvwindow', [1], id);
			}).writeDefFile;

			SynthDef("feedbackControlVols_mod", {
				var sound, envs, volume, vols, bandwidth;

				volume = In.kr(\volBus.kr);
				sound  = In.ar(\inBus.kr);
				sound = Compander.ar(sound, sound, 0.5, 1, 0.5, 0.01, 0.01);
				bandwidth = SampleRate.ir/2048;
				vols = Lag.kr(In.kr(\volsBus.kr, 300), 1/bandwidth);

				300.do{arg i;
					var center = (i+1)*(bandwidth);
					sound = MidEQ.ar(sound, center, \qRatio.kr(1)*(bandwidth/center), vols[i].lincurve(0,1,0,1,-4).ampdb);
				};

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, sound.dup*volume*envs);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("FeedbackControl", Rect(318, 645, 345, 250));
		this.initControlsAndSynths(13);

		waitToLoad = true;

		this.makeMixerToSynthBus(1);
		{
			buf = Buffer.alloc(group.server, 2048);
			buf2 = Buffer.alloc(group.server, 2048);

			buf.zero;
			buf2.zero;

			group.server.sync;

			volBus = Bus.control(group.server);
			volsBus = Bus.control(group.server, 300);

			id = PV_Window_ID.next;//group.nodeID;

			synths.add(Synth.tail(group, "feedbackControl_mod" ,[\inBus, mixerToSynthBus, \buf, buf, \buf2, buf2, \volsBus, volsBus.index, \thresh, 0.8, \mulFactor, 0.7, \attackReleaseFrames, 200, \sustainZeroFrames, 50, \waitGoFrames, 100, \tripCount, 5,  \tripBlockFrames, 300, \topBin, 400, \id, id]));

			synths.add(Synth.tail(group, "feedbackControlVols_mod" ,[\inBus, mixerToSynthBus, \outBus, outBus, \volsBus, volsBus.index, \volBus, volBus.index]));

			controls.add(QtEZSlider.new( "vol", ControlSpec(0,1,'amp'),
				{|v|
					volBus.set(v.value);
			}, 0, true, \horz));
			this.addAssignButton(0,\continuous);

			thresh = 8;
			controls.add(QtEZSlider.new("thresh", ControlSpec(0.1,10,'exp'),
				{|v|
					thresh = v.value;
					synths[0].set(\thresh, v.value);
			}, 8, true, \horz ));
			this.addAssignButton(1,\continuous);

			controls.add(QtEZSlider.new("mulFactor", ControlSpec(0,0.9,'linear'),
				{|v|
					synths[0].set(\mulFactor, v.value);
			}, 0.7, true, \horz));
			this.addAssignButton(2,\continuous);

			controls.add(QtEZSlider.new("attackReleaseFrames", ControlSpec(10,1000,'linear'),
				{|v|
					synths[0].set(\attackReleaseFrames, v.value);
			}, 200, true, \horz));
			this.addAssignButton(4,\continuous);

			controls.add(QtEZSlider.new("sustainZeroFrames", ControlSpec(1,1000,'linear'),
				{|v|
					synths[0].set(\sustainZeroFrames, v.value);
			}, 50, true, \horz));
			this.addAssignButton(5,\continuous);

			controls.add(QtEZSlider.new("waitGoFrames", ControlSpec(1,1000,'linear'),
				{|v|
					synths[0].set(\waitGoFrames, v.value);
			}, 100, true, \horz));
			this.addAssignButton(6,\continuous);

			controls.add(QtEZSlider.new("tripCount", ControlSpec(1,20,'linear'),
				{|v|
					synths[0].set(\tripCount, v.value);
			}, 100, true, \horz));
			this.addAssignButton(7,\continuous);

			controls.add(QtEZSlider.new("tripBlockFrames", ControlSpec(1,1000,'linear'),
				{|v|
					synths[0].set(\tripBlockFrames, v.value);
			}, 100, true, \horz));
			this.addAssignButton(8,\continuous);

			controls.add(QtEZSlider.new("topBin", ControlSpec(10,1000,'linear'),
				{|v|
					synths[0].set(\topBin, v.value);
			}, 400, true, \horz));
			this.addAssignButton(9,\continuous);

			controls.add(QtEZSlider.new("ampMin", ControlSpec(1,0,'linear'),
				{|v|
					synths[1].set(\ampMin, v.value);
			}, 0.5, true, \horz));
			this.addAssignButton(10,\continuous);

			controls.add(QtEZSlider.new("qRatio", ControlSpec(0.5,2,'linear'),
				{|v|
					synths[1].set(\qRatio, v.value);
			}, 1, true, \horz));
			this.addAssignButton(11,\continuous);

			controls.add(Button()
				.states_([
					["gui on", Color.black, Color.green],
					["gui off", Color.black, Color.red]
				])
				.action_{arg butt;
					updateGui = (butt.value==0);
				}
			);
			this.addAssignButton(12,\onOff);

			dispIn = MultiSliderView().minWidth_(600).size_(300).thumbSize_(2).gap_(0).isFilled_(true).drawRects_(false).drawLines_(true);

			dispReduce = MultiSliderView().minWidth_(600).size_(300).thumbSize_(2).gap_(0).isFilled_(true).drawRects_(false).drawLines_(true);

			counter = 0;

			OSCFunc({arg msg;
				if(id==msg[2]){
					//if(updateGui){
					counter = counter+1;
					//[counter, id, paused].postln;
					if(counter%2==0&&(paused==false)){
						//id.postln;
						buf2.getn(2, 600, {|floats|
							var mags, nans;
							//floats.postln;
							nans = floats.select{|item| item.isNaN};
							//nans.postln;
							if(nans.size>0){
								"nans".postln;
							}{
								floats = floats.clump(2).flop;
								mags = floats[1];  //the mags of the input
								floats = floats[0];  //the reduction of volume
								volsBus.setn(floats);
								if((counter%44==0)&&updateGui){
									{dispIn.value_(mags/thresh)}.defer;
									{dispReduce.value_(floats)}.defer;
									counter = 0;
								};
							}
						});
						//volsBus.getn(300, {|floats| {dispIn.value_(floats)}.defer;})
					};
			}}, '/pvwindow');

			win.layout_(
				HLayout(
					VLayout(*controls), VLayout(*assignButtons), VLayout(dispIn, dispReduce)
				)
			);

			win.layout.spacing = 0;
			win.layout.margins = [0,0,0,0];
			waitToLoad = false;
		}.fork(AppClock);
	}

	killMeSpecial {
		rout.stop;
	}

}
