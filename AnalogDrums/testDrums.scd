

//load the buffers into 6 lists
(
    ~bufs = List.fill(6, {List.newClear(0)});
    p = PathName("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/AnalogDrums/euro_drums_2/samples");
    f = p.files.collect{|f| if(f.extension=="wav"){f}}.postln;

    (1..6).do{|i|
        var f2 = f.select{|g|  
            (g.fileName[0].asString|==|i.asString).postln;
        };
        f2.postln;
        f2.do{|g| 
            g.fullPath.postln;
            ~bufs[i-1].add(Buffer.read(s, g.fullPath));
        };
    };
)
//create a big buffer and a data array of start and end times for each buffer in the big buf, plus start and end times of each chunk of buffers
(
    var startFrame = 0;
    ~bigBuf = Buffer(s);
    ~playData = List.newClear(0);
        ~bufs.do{|bufArray, i|
            var loopStart = startFrame;
            var loopEnd = startFrame + ~bufs[i].collect{|b| b.numFrames}.sum;
            bufArray.do{|buf|
                buf.postln;
                ~playData.add([startFrame, startFrame+buf.numFrames, loopStart, loopEnd].postln);
                FluidBufCompose.processBlocking(server: s, source: buf, startFrame: 0, numFrames: -1, startChan: 0, numChans: -1, gain: 1, destination: ~bigBuf, destStartFrame: startFrame.postln, destStartChan: 0, destGain: 0);
                
                startFrame = startFrame + buf.numFrames;
            }
        }
)

~bigBuf.play

~playData.writeArchive(pathname: "/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/Drums_11_23_23/playData");

~bigBuf.write("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/AnalogDrums/euro_drums_2/audioBuf.wav", "WAV", "float");



//start here

~bigBuf = Buffer.read(s, "/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/Drums_11_23_23/bigBuf.wav");

(
~playData = Object.readArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/Drums_11_23_23/playData");
~playDataB = ~playData.collect{|p| 
    [[0].addAll(p), [1].addAll(p),[2].addAll(p)];
}.flatten(1);
)
~playDataB.size

(
~possibleButtons = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 54, 56, 57, 58, 60, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 86, 88, 89, 90, 92, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 108, 112, 113, 114, 116, 120, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 144, 145, 146, 147, 148, 149, 150, 152, 153, 154, 156, 160, 161, 162, 163, 164, 165, 166, 168, 169, 170, 172, 176, 177, 178, 180, 184, 192, 193, 194, 195, 196, 197, 198, 200, 201, 202, 204, 208, 209, 210, 212, 216, 224, 225, 226, 228, 232, 240];

~dict = Dictionary.new;
~possibleButtons.do{|p,i| ~dict.put(p.asString, ~playDataB[i%~playData.size])};

//~possibleButtons = ~possibleButtons.collect{|p,i| [p].addAll(~playDataB[i])};

)



~dict["192"]

~flDict = Dictionary.new;
~flDict.put(\cols, 5);
~flDict.put(\data, ~dict);
~ds = FluidDataSet(s).load(~flDict).print;
~ds.write("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/AnalogDrums/euro_drums_2/dataSet.json");

~ds = FluidDataSet(s).read("/Users/spluta1/Documents/SC/LiveModularInstrument/Audio/Drums_11_23_23/dataSet.json");

~ds.dump

// (
//     a = {
//         var env = Lag.kr(\rate.kr(0).abs>0, 0.01);
//         var sound = BufRd.ar(2, ~bigBuf, Phasor.ar(\t_trig.tr, BufRateScale.kr(~bigBuf)*\rate.kr, \start.kr(0), \end.kr(0), \jumpTo.kr(0)), loop:1);
    
//         sound*env
//     }.play
// )
(
    var dataBus = Bus.control(s, 5);
    var dataGroup = Group.tail(s);
    var synthGroup = Group.tail(s);
    ~dataBuf = Buffer.alloc(s, 5);
    ~dataSynth = {
        Out.kr(dataBus, FluidBufToKr.kr(~dataBuf))
    }.play(dataGroup);

    a = {
        var data = FluidBufToKr.kr(~dataBuf).poll;
        var env = Lag.kr(\rate.kr(0).abs>0, 0.01);
        var kind = data[0];
        var start = Select.kr(data[0], [data[3], data[3], data[1]]);
        var end = Select.kr(data[0], [data[4], data[4], data[1]+rrand(500,3000)]);
        var jumpTo = data[1];
        var sound = BufRd.ar(2, ~bigBuf, Phasor.ar(\t_trig.tr, BufRateScale.kr(~bigBuf)*\rate.kr, start, end, jumpTo), loop:1);
    
        sound*env
    }.play(synthGroup)
)

(
    // var doIt = {|data|
    //     switch(data[0].postln)
    //     {0}{a.set(\rate, 1, \t_trig, 1, \start, data[3], \end, data[4], \jumpTo, data[1]);}
    //     {1}{a.set(\rate, -1, \t_trig, 1, \start, data[3], \end, data[4], \jumpTo, data[1]);}
    //     {2}{a.set(\rate, 1, \t_trig, 1, \start, data[1], \end, data[1]+rrand(500,3000), \jumpTo, data[1]);}
    // };
    var buttons = NButtons(8,4);
    var delta = 0;
    var offTime = Main.elapsedTime;
    var delayedFunc;
    //var speedLimit = SpeedLimit({|val| val.postln}, 0.2);
    MIDIClient.init;
    MIDIIn.connectAll;
    MIDIFunc.noteOn({|vel, num, chan, src|
        var playNum = buttons.buttonChange(num-44, 1).postln;
        //var data = ~dict[playNum.asSymbol()].postln;
        //doIt.(data);
        a.set(\rate, 1, \t_trig, 1);
        ~ds.getPoint(playNum.asString, ~dataBuf);
        
    }, (44..51));
    MIDIFunc.noteOff({|vel, num, chan, src|
        var playNum = buttons.buttonChange(num-44, 0);

        if(playNum!=0){
            //var data = ~dict[playNum.asSymbol()].postln;
            delayedFunc = {
                a.set(\rate, 1, \t_trig, 1);
                ~ds.getPoint(playNum.asString, ~dataBuf)};
            SystemClock.sched(0.01,{delayedFunc.value; nil});
        }{
            var delta = (Main.elapsedTime - offTime).postln;
            if(delta<0.015){
                "fuck".postln;
                delayedFunc = {};
                //a.set(\rate, 0);
            };
        };

        offTime = Main.elapsedTime;

    }, (44..51));
)


(
var buttons = NButtons(8,4);
var delta = 0;
var offTime = Main.elapsedTime;
var delayedFunc;
MIDIClient.init;
MIDIIn.connectAll;
MIDIFunc.noteOn({|vel, num, chan, src|
    var playNum = buttons.buttonChange(num-44, 1);

    
}, (44..51));
MIDIFunc.noteOff({|vel, num, chan, src|
    var playNum = buttons.buttonChange(num-44, 0);
    
    
    if(playNum!=0){
        //"do the thing".postln;
        delayedFunc = {"delayed thing".postln; nil};
        SystemClock.sched(0.01,{delayedFunc.value});
    }{
        var delta = (Main.elapsedTime - offTime).postln;
        if(delta<0.01){
            delayedFunc = {};
            a.
        };
    };
    offTime = Main.elapsedTime;
}, (44..51));

)







~playData
(
    n = ~playData.size.rand;
a.set(\rate, 1, \t_trig, 1, \start, ~playData[n][0], \end, ~playData[n][1], \jumpTo, 0);
)
a.set(\rate, 1, \t_trig, 1, \start, 141422, \end, 141422+5000, \jumpTo, 141422);

a.set(\rate, -1, \t_trig, 1, \start, 141422, \end, 141422+25000, \jumpTo, 141422+5000);

a.set(\rate, 0)

~bufs[0][0].play

~bigBuf.numFrames
~bigBuf.play

s.serverWindow

BufRd.ar(1, buf1, Phasor.ar(\t_trig1.tr(0,0), 1, start, end, jumpTo), loop:1);
(
a = {|t_trig = 0|
    WhiteNoise.ar(0.1)*t_trig;   
}.play
)

a.set(\t_trig, 1);

(
    a = {
        WhiteNoise.ar(0.1)*\t_trig.tr;   
    }.play
)
    
a.set(\t_trig, 1);


(
    ~startTimes.do{|st|
        ~ds.addSlice(~loader.buffer, st[0], st[1]);
    }
)

TGrains2

FluidBufCompose.processBlocking(s, )

