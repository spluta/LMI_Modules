MakeNNTrainings

//make random datasets
(
s.waitForBoot{
	a = FluidDataSet(s);
	~numIns = 3;
	~numPoints = 31;
	~folder = "/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/NN_Synths/13_FM7aOSB/00_stick_model_0/";
	fork{

		8.do{|num|
			var ins = FluidDataSet(s);
			var outs = FluidDataSet(s);
			var hiddenArray = (3, 3+(~numPoints/5)..~numPoints).floor.asInteger.copyRange(1,3).postln;
			var mlp = FluidMLPRegressor(s, hiddenArray,2,1,0,-1,1000,0.1,0.1,1,0);
			//ins
			~point = Buffer.alloc(s, ~numIns, 1);
			s.sync;

			~inPoints = [[0,0,0],
			[0,0,1],
			[1,0,0],
			[1,0,1],
			[1,1,0],
			[1,1,1],
			[0,1,0],
			[0,1,1]];

			~inPoints.do{|item, i|
				~point.loadCollection(item, 0, {|buf|
					ins.addPoint(i.asString, buf);
				});
				0.2.wait;
			};
			ins.write(~folder++"inDataSet"++num++".json");
			//outs
			~point = Buffer.alloc(s, ~numPoints, 1);
			s.sync;
			~inPoints.size.do{|i|
				~point.loadCollection(Array.fill(~numPoints, {1.0.rand}), 0, {|buf|
					outs.addPoint(i.asString, buf);
				});
				0.2.wait;
			};
			a.print;
			outs.write(~folder++"outDataSet"++num++".json");

			mlp.fit(ins, outs);
			mlp.write(~folder++"modelFile"++num++".json");
			s.sync;
			1.wait;
		}
	}
}
)


//make random datasets
(
~numIns = 4;
~numPoints = 12;
~folder = "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/06_CrossFeedback1/model0/";
fork{
	8.do{|num|
		var ins = FluidDataSet(s);
		var outs = FluidDataSet(s);
		var hiddenArray = (3, 3+(~numPoints/5)..~numPoints).floor.asInteger.copyRange(1,3);
		var mlp = FluidMLPRegressor(s, hiddenArray,2,1,0,-1,1000,0.1,0.1,1,0);
		//ins
		~point = Buffer.alloc(s, ~numIns, 1);
		s.sync;

		ins.read(~folder++"inDataSet"++num++".json");
		//outs
		~point = Buffer.alloc(s, ~numPoints, 1);
		s.sync;
		outs.read(~folder++"outDataSet"++num++".json", {
			if(outs.size>~numPoints){

		4
		outs.

		a.print;
		outs.write(~folder++"outDataSet"++num++".json");

		mlp.fit(ins, outs);
		mlp.write(~folder++"modelFile"++num++".json");
		s.sync;
		1.wait;

	}
}
)