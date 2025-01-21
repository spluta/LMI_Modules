GlassSines3_Mod : Module_Mod {

    *initClass {
		StartUp.add {
            SynthDef("glassSine3_mod", {
				var sine, envs;

                var top10 = In.kr(\top10Bus.kr(0),20);
                var freq = (Latch.kr(Select.kr(\whichNote.kr(0),top10), \grabNote.kr(1))+\noteBonus.kr(0)).midicps+\freqBonus.kr(0);
                var env = LagUD.kr(min(In.kr(\ctlBus.kr(0)), \zvol.kr(0)), LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5));
				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, env*0.5);

				//mainVol = Lag.kr(In.kr(\volBus.kr), 0.1).lincurve(0,1,0,1,4);

                envs = Envs.kr(\muteGate.kr(1),\pauseGate.kr(1),\gate.kr(1), 0.1,1,4);

				Out.ar(\outBus.kr, Pan2.ar(sine*AmpComp.kr(freq)*envs, Rand(-1, 1)));
			}).writeDefFile;

            SynthDef("glassSum3_mod", {
                var mainVol = Lag.kr(In.kr(\volBus.kr), 0.1).lincurve(0,1,0,1,4);

                var envs = Envs.kr(\muteGate.kr(1),\pauseGate.kr(1),\gate.kr(1), 0.1,1,4);
                var in = In.ar(\inBus.kr(0),2)*mainVol;
                in = Limiter.ar(in, 0.95);
                Out.ar(\outBus.kr, in*envs);
            }).writeDefFile;
        }
    }

    init {
        var ctlBusses = Array.fill(5,{Bus.control(group.server)});
        var lastNums = (0!5);
        var textList;
        //var top10Bus = Bus.control(group.server, 10);
        var volBus = Bus.control(group.server).set(0);
        var sumBus = Bus.audio(group.server, 2);
        var analysisGroup = Group.head(group);
        var playGroup = Group.tail(group);
        var volGroup = Group.after(playGroup);
        var zVals = (0!5);
        var top10Bus = ModularServers.servers[group.server.asSymbol].globalControlBus.index;


		this.makeWindow("GlassSines3", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(12);

		this.makeMixerToSynthBus(1);

        synths = List.newClear(6);

        // synths.put(5, 
        //     Synth("HarmonicAnalysis_Mod", [\inBus, mixerToSynthBus, \top10Bus, top10Bus], analysisGroup)
        // );


        synths.put(5, 
            Synth("glassSum3_mod", [\inBus, sumBus, \outBus, outBus, \volBus, volBus], volGroup);
        );

        5.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i*2, "synth"++i,
				{arg val; 
                    ctlBusses[i].set(val)
                },true)
            );
            controls.add(TypeOSCFuncObject(this, oscMsgs, i*2+1, "synth"++i+"/z",
				{arg val; 
                    if(val.round.asInteger==1){
                        if(zVals.sum<1){
                            var noteSize = rrand(3,4);
                            var noteBonus = [-24,-12,0,12,24][i];
                            [-10,-5,0,5,10].do{|bonus,i2|
                                synths.put(i2, Synth("glassSine3_mod",[\top10Bus, top10Bus, \ctlBus, ctlBusses[i2], \noteBonus, noteBonus, \freqBonus, bonus, \outBus, sumBus], playGroup));
                            };
                        };
                        synths[i].set(\zvol, 1);
                        zVals[i]=1;
                    }{
                        zVals[i]=0;
                        synths[i].set(\zvol, 0);
                        if(zVals.sum<1){
                            5.do{|i| synths[i].set(\gate,0)};
                        }
                    }
                },true)
            );
		};

        controls.add(TypeOSCFuncObject(this, oscMsgs, 10, "volume",
			{arg val;
				volBus.set(val);
			}, true)
        );

        controls.add(TypeOSCFuncObject(this, oscMsgs, 11, "top10Bus",
			{arg val;
                top10Bus = val;
                5.do{|i| synths[i].set(\top10Bus,top10Bus+ModularServers.servers[group.server.asSymbol].globalControlBus.index)};
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