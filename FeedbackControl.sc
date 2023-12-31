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

				in = Compander.ar(in, in,
					thresh: 0.5,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				fft = FFT(buf, in);

				fft = PV_Control(fft, buf2, thresh, mulFactor, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, topBin);

				windowStarts = fft > -1;

				/*				stream = Unpack1FFT(buf2, 2048, (1..10), 1);
				demand = Demand.kr(windowStarts, 0, stream).poll;

				stream = Unpack1FFT(buf2, 2048, (1..300), 0);
				demand = Demand.kr(windowStarts, 0, stream);
				Out.kr(volsBus, demand);*/


				SendReply.kr(windowStarts, '/pvwindow', [1], id);

				//envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
			}).writeDefFile;

			SynthDef("feedbackControlVols_mod", {
				var sound, envs, volume, vols, bandwidth;

				volume = In.kr(\volBus.kr);

				sound  = In.ar(\inBus.kr);

				sound = Compander.ar(sound, sound,
					thresh: 0.5,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				bandwidth = SampleRate.ir/2048;

				//In.kr(\volsBus.kr, 10).poll;

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

//everything below here is the old, original implementation in SC, before the UGen

/*MulArray {var <>size, <>mulBus, <>tripBus, <>multis, <>multFactor, <>calculateMethod, temp, <>threshold=0.5, depth, previousVolArrays, avgVolArray, <>mulArray, counterArray, incrementArray, task, maxes, tripped, tripCount, tripMul, tripCountTo, tripCountToRange, <>numBinsLong = 10, clipArray, waiting, <>waitTime;

*new {arg size, mulBus, tripBus, multis, multFactor, calculateMethod;
^super.new.size_(size).mulBus_(mulBus).tripBus_(tripBus).multis_(multis).multFactor_(multFactor).calculateMethod_(calculateMethod).init;
}

init {
waiting = false;
waitTime = [0,3];

this.setTripCountTo([1,10]);

incrementArray = Array.fill(size, {rrand(0.04,0.06)});
tripCountTo = Array.fill(size, {rrand(7, 15)});



mulArray = Array.fill(size, 1.0);
avgVolArray = Array.fill(size, 0.0);

tripped = List.fill(size, false);
tripCount = List.fill(size, 0);
tripMul = Array.fill(size, 1);
tripBus.setn(tripMul);

mulBus.setn(mulArray);
counterArray = List.fill(size, 0);
maxes = List.newClear(0);

}

setTripCountTo {arg inCountArray;
tripCountToRange = inCountArray;
tripCountTo.size.do{|i|
tripCountTo.put(i, rrand(tripCountToRange[0], tripCountToRange[1]));
};
}

addVolArray {arg inArray;
avgVolArray = (avgVolArray*multFactor)+inArray;
this.calculateMulArray0;

}

calculateMulArray0 {
avgVolArray.do{|item, i|
if((item>(threshold))&&(tripped[i]==false),{
//if(waiting == false, {
tripped.put(i, true);
SystemClock.sched(rrand(waitTime[0], waitTime[1]),{
counterArray.put(i, 100);
/*						if(tripped[i].not, {
tripCount.put(i, tripCount[i]+1);
if(tripCount[i]>=tripCountTo[i], {
tripped.put(i, true);
tripMul.put(i, 0);

SystemClock.sched(10.0, {
tripMul.put(i, 1);
tripped.put(i, false);
tripCount.put(i, 0);
tripCountTo.put(i, rrand(tripCountToRange[0], tripCountToRange[1]));
nil;
});
});
});*/
//						waiting = false;
nil;
});//
//					waiting = true;
//});
//counterArray.put(i, 10);

});
};


counterArray.do{|item,i|
if((item!=nil)&&(mulArray[i]!=nil),{
if((item>0)&&(mulArray[i]>0),{

mulArray.put(i, (mulArray[i]-incrementArray[i]).asFloat.abs);
counterArray.put(i, item-1);
if(counterArray[i]<=0, {tripped.put(i, false); counterArray.put(i,0)});
},{
if(mulArray[i]<1,{
mulArray.put(i, (mulArray[i]+incrementArray[i]).asFloat.abs);
incrementArray.put(i, rrand(0.04,0.06));
});
if(mulArray[i]>1.0, {mulArray.put(i, 1.0)});
});
},
{
counterArray.put(i, 0);
mulArray.put(i,1);

});
};

mulBus.setn(mulArray);
//tripBus.setn(tripMul);
}

turnDispOn {

task = Task({ {
//multis[0].value_(avgVolArray);
multis[0].value_(mulArray);
multis[1].value_(tripMul);
multis[2].value_(avgVolArray);
0.5.wait;
}.loop });
task.start(AppClock);
}

turnDispOff {
task.stop
}

killMe {
task.stop;
}

}

FeedbackDetection {
var <>group, <>inBus, <>outBus, <>size, <>multis, thresholdShort=0.5, thresholdLong = 0.5, temp, mulBus0, tripBus, synth, task, getArray, finalVals, tot, sepArray, buffer, inc, waitTime=0.5, mulArray0, mulArray1;

*new {arg group, inBus, outBus, multis;
^super.new.group_(group).inBus_(inBus).outBus_(outBus).multis_(multis).init;
}

*initClass {
StartUp.add {
SynthDef("spectralLimiter_mod", {arg inBus, outBus, bufnum, threshold, mulBus0, tripBus, pauseGate = 1, gate=1;
var mulArray0, tripMul, chain0, chain1, env, pauseEnv, in, fftSize, clip, outSig;

fftSize = 2048;

in = In.ar(inBus, 2);

mulArray0 = Lag.kr(In.kr(mulBus0, 320), 0.1);

tripMul = Lag.kr(In.kr(tripBus, 320), 3);

FFT(bufnum, in[0]);

chain0 = FFT(LocalBuf(fftSize), in[0], 0.75);

chain0 = chain0.pvcollect(fftSize, {|mag, phase, index|
mag*mulArray0[index]*tripMul[index];
}, frombin: 0, tobin: 319, zeroothers: 1);

env = EnvGen.kr(Env.asr(2,1,0), gate, doneAction:2);
pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

outSig = IFFT(chain0);

Out.ar(outBus, outSig*env*pauseEnv);
}).writeDefFile;
}
}

init {
size = 320;

mulBus0 = Bus.control(group.server, size);
tripBus = Bus.control(group.server, size);

mulArray0 = MulArray(size, mulBus0, tripBus, multis, 0.5, 0);
//mulArray1 = MulArray(size, tripBus, multis[1], 0.25, 0);

{
buffer = Buffer.alloc(group.server, 2048, 1);
group.server.sync;
0.4.wait;

synth = Synth("spectralLimiter_mod", [\inBus, inBus, \outBus, outBus, \bufnum, buffer.bufnum, \threshold, 0.5, \mulBus0, mulBus0.index, \tripBus, tripBus.index], group);
group.server.sync;
0.4.wait;

task = Task({ { buffer.getn(2, size*2, { arg buf;
var z, x;
z = buf.clump(2).flop;
z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
x = Complex(z[0], z[1]);
getArray = (Array.newFrom(x.magnitude)* 0.05).squared;
mulArray0.addVolArray(getArray);
}); 0.2.wait;}.loop });
task.start;
}.fork;
}

setThresholdShort {arg in;
mulArray0.threshold_(in);
}

setMultFactor0 {arg in;
mulArray0.multFactor_(in);
}

setTripCountTo {arg in;
mulArray0.setTripCountTo(in);
}

pause {
if (task!=nil,{
task.pause;
synth.set(\pauseGate, 0);
});
}

unpause {
task.resume;
synth.set(\pauseGate, 1);
synth.run(true);
}

setAnalSec {arg analSex;
waitTime = 1/analSex;
}

turnDispOn {
mulArray0.turnDispOn;
}

turnDispOff {
mulArray0.turnDispOff;
}

setMulArrayWaitTime {arg in;
mulArray0.waitTime_(in);
}

killMe {
mulArray0.killMe;
task.stop;
synth.set(\gate, 0);
}
}*/

// FeedbackControl_Mod : Module_Mod {
// 	var volBus, feedbackDetection, synthGroup, limiterGroup, multis, tempArray, transferBus;
//
// 	*initClass {
// 		StartUp.add {
//
// 			SynthDef("delayFeedback_mod", {arg inBus, outBus, volBus, delay = 0.02158, varAmount=0.5, varFreq=0.02, pauseGate = 1, gate = 1;
// 				var in, convolveAudio, volume, env, pauseEnv;
//
// 				volume = In.kr(volBus);
//
// 				//in  = LPF.ar(In.ar(inBus, 2), 2800);
//
// 				in  = In.ar(inBus, 2);
//
// 				in = Compander.ar(in, in,
// 					thresh: 0.5,
// 					slopeBelow: 1,
// 					slopeAbove: 0.5,
// 					clampTime: 0.01,
// 					relaxTime: 0.01
// 				);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				Out.ar(outBus, Mix.new(Limiter.ar(in, 0.1, 0.03)*volume));
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("FeedbackControl", Rect(318, 645, 345, 250));
// 		this.initControlsAndSynths(6);
//
// 		this.createMultis;
//
// 		this.makeMixerToSynthBus(2);
//
// 		transferBus = Bus.audio(group.server,2);
//
// 		volBus = Bus.control(group.server);
//
// 		limiterGroup = Group.tail(group);
// 		synthGroup = Group.tail(group);
//
// 		feedbackDetection = FeedbackDetection(limiterGroup, mixerToSynthBus.index, transferBus, multis);
//
// 		synths.add(Synth("delayFeedback_mod",[\inBus, transferBus.index, \outBus, outBus.index, \volBus, volBus.index], synthGroup));
//
// 		controls.add(EZSlider.new(win,Rect(5, 5, 60, 220), "vol", ControlSpec(0,1,'amp'),
// 			{|v|
// 				volBus.set(v.value);
// 		}, 0, layout:\vert));
// 		this.addAssignButton(0,\continuous,Rect(5, 230, 60, 20));
//
// 		controls.add(EZSlider.new(win,Rect(65, 5, 60, 220), "threshold", ControlSpec(0,1,'linear'),
// 			{|v|
// 				feedbackDetection.setThresholdShort(v.value);
// 		}, 0.5, layout:\vert));
// 		this.addAssignButton(1,\continuous, Rect(65, 230, 60, 20));
//
// 		controls.add(EZSlider.new(win,Rect(125, 5, 60, 220), "MultFactor", ControlSpec(0.1,0.9,'linear'),
// 			{|v|
// 				feedbackDetection.setMultFactor0(v.value);
// 		}, 0.5, layout:\vert));
// 		this.addAssignButton(2,\continuous,Rect(125, 230, 60, 20));
//
// 		controls.add(EZRanger.new(win,Rect(185, 5, 60, 220), "TripCountTo", ControlSpec(1,20,'linear'),
// 			{|v|
// 				feedbackDetection.setTripCountTo(v.value);
// 		}, [1, 10], layout:\vert));
// 		this.addAssignButton(3,\range,Rect(185, 230, 60, 20));
//
// 		controls.add(EZRanger.new(win,Rect(245, 5, 60, 220), "WaitTime", ControlSpec(1,20,'linear'),
// 			{|v|
// 				feedbackDetection.setMulArrayWaitTime(v.value);
// 		}, [1, 3], layout:\vert));
// 		this.addAssignButton(4,\range,Rect(245, 230, 60, 20));
//
// 		controls.add(Button(win, Rect(5, 260, 90, 20))
// 			.states_([["dispOff", Color.blue, Color.black],["dispOn", Color.black, Color.blue]])
// 			.action_({arg butt;
// 				if(butt.value==0,{
// 					feedbackDetection.turnDispOff
// 					},{
// 						feedbackDetection.turnDispOn
// 				})
// 			})
// 		);
//
// 		numChannels = 2;
// 	}
//
// 	createMultis {arg size = 320;
//
// 		multis = List.newClear(0);
//
// 		multis.add(MultiSliderView(win, Rect(size, 5, size, 350)));
// 		tempArray = Array.new;
// 		size.do({arg i;
// 			tempArray = tempArray.add(i/size);
// 		});
//
// 		multis[0].value_(tempArray);
// 		multis[0].readOnly = false;
// 		multis[0].xOffset_(1);
// 		multis[0].thumbSize_(1);
// 		multis[0].strokeColor_(Color.black);
// 		multis[0].drawLines_(true);
// 		multis[0].drawRects_(false);
// 		multis[0].indexThumbSize_(0.5);
// 		multis[0].valueThumbSize_(0.1);
// 		multis[0].isFilled_(false);
// 		multis[0].gap_(1);
//
// 		multis.add(MultiSliderView(win, Rect(size, 355, size, 350)));
// 		multis[1].value_(tempArray);
// 		multis[1].readOnly = false;
// 		multis[1].xOffset_(1);
// 		multis[1].thumbSize_(1);
// 		multis[1].strokeColor_(Color.black);
// 		multis[1].drawLines_(true);
// 		multis[1].drawRects_(false);
// 		multis[1].indexThumbSize_(0.5);
// 		multis[1].valueThumbSize_(0.1);
// 		multis[1].isFilled_(false);
// 		multis[1].gap_(1);
//
// 		multis.add(MultiSliderView(win, Rect(0, 355, size, 350)));
// 		multis[2].value_(tempArray);
// 		multis[2].readOnly = false;
// 		multis[2].xOffset_(1);
// 		multis[2].thumbSize_(1);
// 		multis[2].strokeColor_(Color.black);
// 		multis[2].drawLines_(true);
// 		multis[2].drawRects_(false);
// 		multis[2].indexThumbSize_(0.5);
// 		multis[2].valueThumbSize_(0.1);
// 		multis[2].isFilled_(false);
// 		multis[2].gap_(1);
// 		//
// 		//		multis.add(MultiSliderView(win, Rect(515+size, 355, size, 350)));
// 		//		multis[3].value_(tempArray);
// 		//		multis[3].readOnly = false;
// 		//		multis[3].xOffset_(1);
// 		//		multis[3].thumbSize_(1);
// 		//		multis[3].strokeColor_(Color.black);
// 		//		multis[3].drawLines_(true);
// 		//		multis[3].drawRects_(false);
// 		//		multis[3].indexThumbSize_(0.5);
// 		//		multis[3].valueThumbSize_(0.1);
// 		//		multis[3].isFilled_(false);
// 		//		multis[3].gap_(1);
// 		//
// 		//		multis.add(MultiSliderView(win, Rect(5, 355, size, 350)));
// 		//		tempArray = Array.newClear(0);
// 		//		size.do{|i|tempArray = tempArray.add(1-(i/(size*2)))};
// 		//		multis[4].value_(tempArray);
// 		//		multis[4].readOnly = false;
// 		//		multis[4].xOffset_(1);
// 		//		multis[4].thumbSize_(1);
// 		//		multis[4].strokeColor_(Color.black);
// 		//		multis[4].drawLines_(true);
// 		//		multis[4].drawRects_(false);
// 		//		multis[4].indexThumbSize_(0.5);
// 		//		multis[4].valueThumbSize_(0.1);
// 		//		multis[4].isFilled_(false);
// 		//		multis[4].gap_(1);
// 	}
//
// 	pause {
// 		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
// 		feedbackDetection.pause;
// 	}
//
// 	unpause {
// 		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
// 		feedbackDetection.unpause;
// 	}
//
// 	killMeSpecial {
// 		feedbackDetection.killMe;
// 	}
// }