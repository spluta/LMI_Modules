
// ArrayTopN {
//     *kr {|stats, numKeys|
//         var maxAmp, maxIndex, mulArray, maxAmp0, maxIndex0, out;
//         #maxAmp0, maxIndex0 = ArrayMax.kr(stats);
//         out = 10.collect{
//             #maxAmp, maxIndex = ArrayMax.kr(stats);
//             mulArray = RotateN.kr(maxIndex.neg,[DC.ar(0)].addAll(Array.fill(numKeys-1, {DC.kr(1)})));
//             stats = stats*mulArray;
//             [maxIndex/2, maxAmp/maxAmp0]
//         };
//         ^out.flop.flat;
//     }
// }

ArrayTopN {
    *kr {|stats, num_vals, num_top=10|
        var maxAmp, maxIndex;
        var one_zero = [DC.kr(0)].addAll(Array.fill(num_vals, DC.kr(1)));
        var topN = num_top.collect{
            #maxAmp, maxIndex = ArrayMax.kr(stats);
            one_zero = (1-PanAz.kr(num_vals, DC.kr(1), maxIndex/num_vals*2, 1, 1, 0));
            stats = stats*one_zero;
            [maxIndex/2, maxAmp]
        };
        ^topN.flat;    
    }
}

HarmonicAnalysis_Mod : Module_Mod {

    *initClass {
        StartUp.add {

            SynthDef("HarmonicAnalysis_Mod", {
                var numKeys = 256, amps, stats, numPeaks = 10, out;
                var featBuf = LocalBuf(numKeys*2);
                var source = In.ar(\inBus.kr(0));
                var analysis = FluidSineFeature.kr(source,numPeaks: numPeaks, freqUnit: 1, maxNumPeaks: numPeaks, maxFFTSize:8192);
                
                var toggle = ToggleFF.ar(Impulse.ar(ControlRate.ir()));

                var envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));
                var one_zero, top10, maxAmp0, maxIndex0;
            
                numPeaks.do{|i|
                    BufWr.kr(analysis[i+numPeaks],featBuf,analysis[i].round(0.5).asInteger()*2+(numKeys*toggle));
                };
                
                numKeys.do{|i|
                    BufWr.kr(0,featBuf,(i+(numKeys*(1-toggle))));
                };
            
                amps = FluidBufToKr.kr(buffer:featBuf, startFrame:0, numFrames:numKeys*2).clump(numKeys).sum.flat;
            
                stats = FluidStats.kr(amps, ControlRate.ir*\analysisDur.kr(1))[0];

                top10 = ArrayTopN.kr(stats, numKeys, 10);

                Out.kr(\top10Bus.kr(0), top10);

            }).writeDefFile;
        }
    }

    init {
        this.makeWindow("HarmonicAnalysis", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(2);

        this.makeMixerToSynthBus;
        
        synths.add(Synth("HarmonicAnalysis_Mod", [\inBus, mixerToSynthBus], group));

        controls.add(TypeOSCFuncObject(this, oscMsgs, 0, "controlBus",
            {arg val;
                synths[0].set(\top10Bus, ModularServers.servers[group.server.asSymbol].globalControlBus.index+val);
            }, true)
        );

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