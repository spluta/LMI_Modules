ArrayTopN {
    *kr {|stats, numKeys, howMany|
        var maxAmp, maxIndex, mulArray, maxAmp0, maxIndex0, out;
        #maxAmp0, maxIndex0 = ArrayMax.kr(stats);
        out = howMany.collect{
            #maxAmp, maxIndex = ArrayMax.kr(stats);
            mulArray = RotateN.kr(maxIndex.neg,[DC.ar(0)].addAll(Array.fill(numKeys-1, {DC.kr(1)})));
            stats = stats*mulArray;
            [maxIndex, maxAmp/maxAmp0]
        };
        ^out.flop.flat;
    }
}

HarmonicAnalysis_Mod : Module_Mod {

    *initClass {
        StartUp.add {
            var arrayTopN = {|stats, numKeys, howMany|
                var maxAmp, maxIndex, mulArray, maxAmp0, maxIndex0, out;
                #maxAmp0, maxIndex0 = ArrayMax.kr(stats);
                out = howMany.collect{
                    #maxAmp, maxIndex = ArrayMax.kr(stats);
                    mulArray = RotateN.kr(maxIndex.neg,[DC.ar(0)].addAll(Array.fill(numKeys-1, {DC.kr(1)})));
                    stats = stats*mulArray;
                    [maxIndex, maxAmp/maxAmp0]
                };
                out = out.flop;
            };
            
            SynthDef("HarmonicAnalysis_Mod", {
                var numKeys = 128, amps, stats;
                var featBuf = LocalBuf(numKeys*2);
                var source = In.ar(\inBus.kr(0));
                var analysis = FluidSineFeature.kr(source,numPeaks: 5, freqUnit: 1, maxNumPeaks: 5, maxFFTSize:8192);
                
                var toggle = ToggleFF.ar(Impulse.ar(ControlRate.ir()));

                var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
            
                5.do{|i|
                    BufWr.kr(analysis[i+5],featBuf,analysis[i].round.asInteger()+(numKeys*toggle));
                };
                
                numKeys.do{|i|
                    BufWr.kr(0,featBuf,(i+(numKeys*(1-toggle))));
                };
            
                amps = FluidBufToKr.kr(buffer:featBuf, startFrame:0, numFrames:numKeys*2).clump(numKeys).sum.flat;
            
                stats = FluidStats.kr(amps, ControlRate.ir*\analysisDur.kr(1))[0];

                Out.kr(\top5Bus.kr(0), ArrayTopN.kr(stats,numKeys,5).flat);
            
            }).writeDefFile;
        }
    }

    init {
        this.makeWindow("HarmonicAnalysis", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(1);
        
        synths.add(Synth("HarmonicAnalysis_Mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));

        controls.add(TypeOSCFuncObject(this, oscMsgs, 0, "analysisDur",
            {arg val;
                synths[0].set(\analysisDur, val.linexp(0,1,0.1,5));
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