//this worked Aug 1, 2023
NessyObject_Mod {
	var <>group, <>outBus, <>parent, <>num, <>volBus, synths, bufs, bufsSeq, currentBufs, zVal, lockVal, speed, speedBus, running, outFileA, outFileB, synthSeq, synthSwitch;

	*initClass {
		StartUp.add {
			SynthDef("play_nessie_mod", {|outBus, buf0a, buf0b, volBus, speedBus, rate=1, gate=1, pauseGate=1, muteGate=1|

				var env = EnvGen.kr(Env.asr(0.01, 1, 4), gate, doneAction:2);
				var sound, pauseEnv, muteEnv;

				var speed = In.kr(speedBus);
				var dur = Rand(BufDur.kr(buf0a)-4, BufDur.kr(buf0a));
				var imp = Impulse.kr(1/(dur-2.5/speed));
				var vol = In.kr(volBus).lag(0.2);

				sound = [TGrains2.ar(1, imp, buf0a, speed, dur/2, dur/speed, 0, 1, 2, 2),
					TGrains2.ar(1, imp, buf0b, speed, dur/2, dur/speed, 0, 1, 2, 2)];

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
				Out.ar(outBus, sound*env*pauseEnv*muteEnv*vol/2);
			}).writeDefFile;//load(ModularServers.servers[\lmi1].server);
		}
	}

	*new {arg group, outBus, parent, num, outFileA, outFileB;
		^super.new.group_(group).outBus_(outBus).parent_(parent).num_(num).init;
	}

	init {
		volBus = Bus.control(group.server, 1);
		speedBus = Bus.control(group.server, 1);
		speed = 1;
		volBus.set(0);
		speedBus.set(speed);
		running = false;
		zVal = 0;
		lockVal = 0;
		bufsSeq = Pseq([0,1], inf).asStream;
		bufs = List.newClear(2);
		synths = List.newClear(4);
		synthSeq = Pseq((0..3), inf).asStream;
		synthSwitch = List[0,0,0,0];
	}


	makeAndPlayLoop {
		var inTempText, tempText, oneNDone, twoNDone, shortFileA, shortFileB, nextSynth;

		tempText  = "ns_temp"++"_"++group.server.asString++"_"++group.nodeID.asString++"_"++num;

		shortFileA = (Platform.defaultTempDir++tempText++"A.wav");
		outFileA = (Platform.defaultTempDir++tempText++"_"++"_100A.wav");

		nextSynth = synthSeq.next;
		//make sure all the other synths are dead
		(0..3).reject({|item| item==nextSynth}).do{|num|
			if(synths[num]!=nil){
				synths[num].set(\gate, 0);
				synths[num] = nil;
			};
		};

		synthSwitch[nextSynth] = 1;

		currentBufs = bufsSeq.next;

		bufs[currentBufs].do{|buf| if(buf!=nil){buf.free}};

		oneNDone = 0;
		twoNDone = 0;
		parent.lastBuffer.write(shortFileA, "wav", completionMessage:{
			{

				("/Users/spluta1/Library/Application Support/SuperCollider/Extensions/MyPlugins/TimeStretch/rust/target/release/ness_stretch".quote+"-d 100 -v 0 -s 8 -b 8 -f "++shortFileA.quote+"-o"+outFileA.quote).unixCmd(
					action:{|msg|
						{

							("rm"+shortFileA.quote).unixCmd;

							bufs.put(currentBufs, [
								Buffer.readChannel(group.server, outFileA, channels:[0]),
								Buffer.readChannel(group.server, outFileA, channels:[1])
							]);

							group.server.sync;
							("rm "++outFileA.quote).unixCmd;
							synths[nextSynth] = Synth("play_nessie_mod", [\buf0a, bufs[currentBufs][0], \buf0b, bufs[currentBufs][1], \outBus, outBus, \volBus, volBus, \speedBus, speedBus], group);
							if (synthSwitch[nextSynth] == 0){
								synths[nextSynth].set(\gate, 0);
								synths[nextSynth]=nil;
							};
						}.fork;
				});
			}.fork;
		});

	}

	setZVal {|val|
		zVal = val;
		[zVal, running];
		if (zVal==1){
			if(running==false){
				running = true;
				this.makeAndPlayLoop;
			}
		}{
			if(lockVal==0){
				running = false;
				(0..3).do{|num|
					if(synths[num]!=nil){
						synths[num].set(\gate, 0);
						synths[num] = nil;
					};
				};
				synthSwitch = [0,0,0,0];
			}
		}
	}

	setShift{|val|
		speed = [0.5,1,2][val];
		speedBus.set(speed);
	}

	setLock{|val|
		lockVal = val;
		if (lockVal==1){
			if(running==false){
				running = true;
				this.makeAndPlayLoop;
			}
		}{
			if(zVal==0){
				running = false;
				(0..3).do{|num|
					if(synths[num]!=nil){
						synths[num].set(\gate, 0);
						synths[num] = nil;
					};
				};
				synthSwitch = [0,0,0,0];
			}
		}
	}

	killMeSpecial {

	}
}

NessStretchRT_Mod : SignalSwitcher_Mod {
	var texts, functions, transferBus, numBufs, durs, recBufs, recBufSeq, currentBuf, recSynth, hpssGroup, recGroup, bufSeq, bufs, osc, lastRecordedBuffer, routNum, recordTask, <>bufNum, nessyObjects, <>lastBuffer;

	*initClass {
		StartUp.add {
			SynthDef("ness_in_mod", {|inBus0, inBus1, whichInBus=0, transferBus, gate=1, pauseGate=1, muteGate=1|
				var sound;
				var pauseEnv, muteEnv, env;
				var sum = SelectX.ar(whichInBus, [In.ar(inBus0, 2), In.ar(inBus1, 2)]);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
				Out.ar(transferBus, sum*pauseEnv*muteEnv*gate);
			}).writeDefFile;

			SynthDef("record_lilnessies_mod", {|transferBus, dur, buf, currentBufNum|
				var env = EnvGen.kr(Env([0,1,1,0,0], [0.001, dur-0.002, 0.001]));

				var in = In.ar(transferBus, 2);

				4.collect{|i| RecordBuf.ar(in*env, buf, SampleRate.ir*dur*i, loop:0)};

				SendTrig.kr(Env([0,0,1,0], [dur, 0.001, 0.001]).kr(doneAction:2), currentBufNum, 0.9);
			}).writeDefFile;


		}

	}

	init3 {

		synthName = "NessStretchRT";

		win.name = "NessStretchRT"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		this.initControlsAndSynths(17);

		dontLoadControls = (0..16);

		hpssGroup = Group.tail(group);
		recGroup = Group.tail(group);

		numBufs = 32;

		nessyObjects = Array.fill(4, {|i| NessyObject_Mod(group, outBus, this, i)});

		transferBus = Bus.audio(group.server, 2);

		synths.add(Synth("ness_in_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \whichInBus, 0, \transferBus, transferBus], hpssGroup));
		4.do{synths.add(nil)};

		//this part records the temporary buffers
		durs = Array.fill(numBufs, {1/3});

		recBufs = Array.fill(numBufs, {|i| Buffer.alloc(group.server, group.server.sampleRate*durs[i]*4, 2)});

		recBufSeq = Pseq((0..(numBufs-1)), inf).asStream;

		recordTask = Task({inf.do{
			currentBuf = recBufSeq.next;
			Synth("record_lilnessies_mod", [\dur, durs[currentBuf], \buf, recBufs[currentBuf], \currentBufNum, currentBuf, \transferBus, transferBus], recGroup);
			(1/16).wait;
		}}).play;

		//keeps track of the last recorded buffer
		bufSeq = Pseq((0..(numBufs-1)), inf).asStream;
		bufs = List.newClear(numBufs);
		osc = OSCFunc({ arg msg, time;
			var tempText;

			lastRecordedBuffer = msg[2];

			recBufs[lastRecordedBuffer].normalize;
			lastBuffer = recBufs[lastRecordedBuffer];

		},'/tr', group.server.addr);


		texts = [
			"whichInput", "vol1", "zVol1", "shift1", "unlock/lock1",
			"vol2", "zVol2", "shift2", "unlock/lock2",
			"vol3", "zVol3", "shift3", "unlock/lock3",
			"vol4", "zVol4", "shift4", "unlock/lock4"
		];

		functions = [
			{arg val; synths[0].set(\whichInBus, val)},

			{arg val; nessyObjects[0].volBus.set((val**2)*2)},
			{arg val;  nessyObjects[0].setZVal(val)},
			{arg val;  nessyObjects[0].setShift(val)},
			{arg val; nessyObjects[0].setLock(val)},

			{arg val; nessyObjects[1].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[1].setZVal(val)},
			{arg val;  nessyObjects[1].setShift(val)},
			{arg val; nessyObjects[1].setLock(val)},

			{arg val; nessyObjects[2].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[2].setZVal(val)},
			{arg val;  nessyObjects[2].setShift(val)},
			{arg val; nessyObjects[2].setLock(val)},

			{arg val; nessyObjects[3].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[3].setZVal(val)},
			{arg val;  nessyObjects[3].setShift(val)},
			{arg val; nessyObjects[3].setLock(val)}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func));
		};

		[0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0].do{|item, i| controls[i].setExternal_(item)};

		win.bounds_((70*numMixers)@(150+(texts.size*16)));

		win.layout_(
			VLayout(
				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
				VLayout(*controls.collect({arg item; item.view}))
			)
		);
		win.layout.spacing = 1;
		win.layout.margins = [1,1,1,1];
		win.front;
	}

	killMeSpecial {
		nessyObjects.do{|obj| obj.killMeSpecial}
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

