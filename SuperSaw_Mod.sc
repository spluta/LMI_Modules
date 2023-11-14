SuperSaw_Mod : Module_Mod {

    *initClass {
		StartUp.add {
            
            SynthDef("SuperSaw_Mod",{
                var detuneCurve = { |x|
                    (10028.7312891634*x.pow(11)) -
                    (50818.8652045924*x.pow(10)) +
                    (111363.4808729368*x.pow(9)) -
                    (138150.6761080548*x.pow(8)) +
                    (106649.6679158292*x.pow(7)) -
                    (53046.9642751875*x.pow(6)) +
                    (17019.9518580080*x.pow(5)) -
                    (3425.0836591318*x.pow(4)) +
                    (404.2703938388*x.pow(3)) -
                    (24.1878824391*x.pow(2)) +
                    (0.6717417634*x) +
                    0.0030115596
                };
                var centerGain = { |x| (-0.55366 * x) + 0.99785 };
                var sideGain = { |x| (-0.73764 * x.pow(2)) + (1.2841 * x) + 0.044372 };

                var top5 = In.kr(\top5Bus.kr(0),10);

                var centerFreq = (Latch.kr(Select.kr(\whichNote.kr(0),top5), 1)+\noteBonus.kr(0)).midicps;
                var amp = Latch.kr(Select.kr(\whichNote.kr+5,top5), 1);

                var center = SawOS.ar(centerFreq, Rand(0, 2), 1, centerGain.(\centerMix.kr(0.4)));
                var detuneFactor = centerFreq * detuneCurve.(In.kr(\detuneBus.kr(0)));
                var freqs = [
                    (centerFreq - (detuneFactor * 0.11002313)),
                    (centerFreq - (detuneFactor * 0.06288439)),
                    (centerFreq - (detuneFactor * 0.01952356)),
                    //(freq + (detuneFactor * 0)),
                    (centerFreq + (detuneFactor * 0.01991221)),
                    (centerFreq + (detuneFactor * 0.06216538)),
                    (centerFreq + (detuneFactor * 0.10745242))
                ];
                var side = Array.fill(6, {|n|
                    SawOS.ar(freqs[n], Rand(0, 2), 1, sideGain.(\sideMix.kr(0.2)))
                });

                var sig = Splay.ar(LeakDC.ar([side.copyRange(0,2),center,side.copyRange(3,5)].flat));
                //var env = In.kr(\ctlBus.kr(0));
                var env = LagUD.kr(min(In.kr(\ctlBus.kr(0)), \gate.kr(1)), LFNoise2.kr(0.1, 1.75, 2.5), LFNoise2.kr(0.1, 3.25, 4.5));
                var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr, 0.1,1,4);

                sig = MoogVCF2.ar(sig, env.linexp(0,1,centerFreq,SampleRate.ir/2), 0.5);
                sig = sig*env.lincurve(0,1,0,1,4)*\amp.kr(1)*envs;
                DetectSilence.ar(sig,2);
                Out.ar(\outBus.kr(0), sig*In.kr(\volBus.kr(0)));
            }).writeDefFile;
        }
    }

    init {
        var ctlBusses = Array.fill(4,{Bus.control(group.server)});
        var lastNums = (0!4);
        var detuneBus = Bus.control(group.server, 1);
        var volBus = Bus.control(group.server, 1);
        var textList;
        var top5Bus = Bus.control(group.server, 10);
        var analysisGroup = Group.head(group);
        var playGroup = Group.tail(group);

		this.makeWindow("SuperSaw", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(11);

		this.makeMixerToSynthBus(1);

        synths = List.newClear(5);

        synths.put(4, 
            Synth("HarmonicAnalysis_Mod", [\inBus, mixerToSynthBus, \top5Bus, top5Bus], analysisGroup)
        );

        4.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i*2, "synth"++i,
				{arg val; 
                    ctlBusses[i].set(val)
                },true)
            );
            controls.add(TypeOSCFuncObject(this, oscMsgs, i*2+1, "synth"++i+"/z",
				{arg val; 
                    if(val==1){
                        var noteBonus = 0, noteSize = rrand(3,4);
                        if(i == 2){noteBonus = 12};
                        if(i == 3){noteBonus = 24};
                        {
                            synths[i].do{|synth| synth.set(\gate, 0)};
                            group.server.sync;
                            synths.put(i, List.newClear(noteSize));
                                
                            noteSize.do{|whichNote|
                                var theSynth = Synth("SuperSaw_Mod",[\outBus, outBus, \top5Bus, top5Bus, \whichNote, whichNote, \ctlBus, ctlBusses[i], \detuneBus, detuneBus, \noteBonus, noteBonus, \volBus, volBus],playGroup);
                                synths[i].put(whichNote, theSynth);
                            };
                        }.fork;
                    }{
                        synths[i].do{|synth| synth.set(\gate, 0)};
                    }
                },true)
            );
		};

        controls.add(TypeOSCFuncObject(this, oscMsgs, 8, "detune",
			{arg val;
				detuneBus.set(val);
			}, true)
        );

        controls.add(TypeOSCFuncObject(this, oscMsgs, 9, "volume",
			{arg val;
				volBus.set(val.lincurve(0,1,0,1));
			}, true)
        );

        win.layout_(
			VLayout(
				*controls
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
    }

}