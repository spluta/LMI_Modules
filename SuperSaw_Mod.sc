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

                var top10 = In.kr(\top10Bus.kr(0),20);

                var centerFreq = (Latch.kr(Select.kr(\whichNote.kr(0),top10), 1)+\noteBonus.kr(0)).midicps;
                //var amp = Latch.kr(Select.kr(\whichNote.kr+5,top10), 1);

                var detune_start = Rand(0,1);
                var detune_end = (detune_start+Rand(0.4,0.6)).wrap(0,1);

                var center = SawOS.ar(centerFreq, Rand(0, 2), 2, centerGain.(\centerMix.kr(0.4)));
                var detuneFactor = centerFreq * detuneCurve.(Line.kr(detune_start,detune_end,\dur.kr(10)));//(In.kr(\detuneBus.kr(0)));
                var freqs = [
                    (centerFreq - (detuneFactor * 0.11002313)),
                    (centerFreq - (detuneFactor * 0.06288439)),
                    (centerFreq - (detuneFactor * 0.01952356)),
                    (centerFreq + (detuneFactor * 0.01991221)),
                    (centerFreq + (detuneFactor * 0.06216538)),
                    (centerFreq + (detuneFactor * 0.10745242))
                ];
                var side = Array.fill(6, {|n|
                    SawOS.ar(freqs[n], Rand(0, 2), 2, sideGain.(\sideMix.kr(0.2)))
                });

                var sig = Splay.ar(LeakDC.ar([side.copyRange(0,2),center,side.copyRange(3,5)].flat));
                var env = Env([0,1,0],[\dur.kr/2,\dur.kr/2]).kr(doneAction:2);//LagUD.kr(min(In.kr(\ctlBus.kr(0)), \gate.kr(1)), LFNoise2.kr(0.1, 1.75, 2.5), 10);
                var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), 1, 0.1,1,4);

                sig = VAMoogLadderOS.ar(sig, env.linexp(0,1,centerFreq,TRand.kr(SampleRate.ir/4, SampleRate.ir/3)),Rand(0.5,0.7),0);
                sig = sig*env.lincurve(0,1,0,2,4)*\amp.kr(1)*envs;
                DetectSilence.ar(sig,0.001,doneAction:2);
                Out.ar(\outBus.kr(0), sig*(In.kr(\volBus.kr(0)).lincurve(0,1,0,1,4))
                *Latch.kr(Select.kr(\localVolBus.kr(0),top10),1));
            }).writeDefFile;
        }
    }

    init {
        var ctlBusses = Array.fill(4,{Bus.control(group.server)});
        var lastNums = (0!4);
        var detuneBus = Bus.control(group.server, 1);
        var volBus = Bus.control(group.server, 1);
        var textList;
        var top10Bus = ModularServers.servers[group.server.asSymbol].globalControlBus.index;
        var analysisGroup = Group.head(group);
        var playGroup = Group.tail(group);

		this.makeWindow("SuperSaw", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(11);

		this.makeMixerToSynthBus(1);

        synths = List.newClear(5);

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
                        {
                            synths[i].do{|synth| synth.set(\gate, 0)};
                            group.server.sync;
                            synths.put(i, List.newClear(noteSize));
                                
                            noteSize.do{|whichNote|
                                var theSynth = Synth("SuperSaw_Mod",[\outBus, outBus, \top10Bus, top10Bus, \whichNote, whichNote, \ctlBus, ctlBusses[i], \detuneBus, detuneBus, \noteBonus, noteBonus, \volBus, volBus, \dur, rrand(8,12), \localVolBus, whichNote+10],playGroup);
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
				volBus.set(val);
			}, true)
        );

        controls.add(TypeOSCFuncObject(this, oscMsgs, 10, "top10Bus",
        {arg val;
            top10Bus = val;
            //5.do{|i| synths[i].set(\top10Bus,top10Bus+ModularServers.servers[group.server.asSymbol].globalControlBus.index)};
            //volBus.set(val);
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