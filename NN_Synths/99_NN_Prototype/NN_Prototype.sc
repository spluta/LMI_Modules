PrototypeElement {
	var <>action, <>msgBox, <>lowBox, <>hiBox, <>warpBox, <>onOffButton, valBox, slider, container, <>controlSpec, <>msg, <>low, <>high, <>warp, <value, layout, <>nnVals, <>location, <>parent;

	*new {

		^super.new.init;
	}

	init {

		//controlSpec = ControlSpec();

		msgBox = TextField();
		msgBox.string = 'msg';
		msgBox.maxHeight_(15).maxWidth_(120).font_(Font("Helvetica", 10));
		msgBox.action = {arg val;
			msg = val.value.asSymbol;
			nnVals.put(location, [msg.asString, nil]);
		};

		onOffButton = Button().maxHeight_(15).maxWidth_(20)
		.states_([["Off", Color.black, Color.red], ["On", Color.black, Color.green]])
		.action_({|button|
			if(button.value==1){parent.increaseNNSize}{parent.decreaseNNSize};
		});

		slider = Slider().maxHeight_(15).maxWidth_(150).orientation_(\horizontal)
		.action_{arg slider;
			action.value(slider);
		};

		layout = HLayout(msgBox, onOffButton, slider);

	}

	setElements {arg vals;
		vals = vals.collect{|item, i| if(i==0){item.asString}{item.asFloat}};
		msgBox.valueAction_(vals[0]);
		onOffButton.valueAction_(1);
	}

	asView {^layout}

	exportModel {

	}

	value_ { arg val;
		value = val; //controlSpec.constrain(val);
		{
			valBox.value = value.round(0.001);
		}.defer;
	}

	doAction { action.value(this) }

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}
}

NN_Prototype_NNMod : NN_Synth_Mod {
	var synthNameBox, reloadSynthButton, synthName;
	var otherValsBusses, onOffBus, envOnOffBus, numControls, exportButton, sendValsButton, volBus,loadArgsButton;

	setAllVals {

		allValsList = List.fill(8, {List.fill(20, {0})});
	}

	init {

		this.makeWindow("NN_Prototype", Rect(0, 0, 200, 40));

		numModels = 8;
		numControls = 80;
		sizeOfNN = 0;

		this.initControlsAndSynths(numControls);
		synths.add(nil);

		nnVals = List.fill(numControls, {|i| [nil, nil]});

		dontLoadControls = (0..(numControls-1));
	}

	//overriding init2 from NN_Synth_Mod

	init2 {arg nameIn, parent, volBusIn, onOff0, onOff1;
		volBus = volBusIn;
		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1], group));
		this.init_window(parent);
	}

	createWindow {
		synthNameBox = TextField().maxHeight_(15).font_(Font("Helvetica", 10))
		.string_("NotAName")
		.action_{arg field;
			synthName = field.value;
		};
		synthName = "NotAName";

		reloadSynthButton = Button()
		.states_([["reloadSynth", Color.black, Color.yellow]])
		.action_{
			if(synths[0]!=nil, {synths[0].set(\gate, 0)});
			synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1], group));
		};

		sendValsButton = Button()
		.states_([["sendVals", Color.black, Color.yellow]])
		.action_{
			if(synths[0]!=nil){controls.do{|item| item.doAction}};
		};

		exportButton = Button()
		.states_([["export", Color.black, Color.yellow]])
		.action_{
			controls.collect{|item|
				[item.msg, item.controlSpec]}.asCompileString;
		};

		loadArgsButton = Button()
		.states_([["load args from file", Color.black, Color.yellow]])
		.action_{
			var temp;
			Dialog.openPanel({ arg path;
				if(path.contains(".csv")){
					temp = CSVFileReader.read(path);
					temp.collect{arg line, i;
						controls[i].setElements(line)
					}
				}
			})
		};

		numControls.do{arg item, i;
			controls.add(
				PrototypeElement()
				.nnVals_(nnVals).location_(i).parent_(this)
				.action_{arg val;
					controlBusses[0].setAt(i, [val.value]);
					valsList.put(i, val.value);
				}
			)
		};

		win.layout = VLayout(
			HLayout(synthNameBox, reloadSynthButton, sendValsButton),
			HLayout(
				VLayout(*controls.copyRange(0,(controls.size/2-1).asInteger)),
				VLayout(*controls.copyRange((controls.size/2).asInteger, controls.size-1))
			),
				HLayout(exportButton, loadArgsButton)
		);
		win.layout.spacing_(0).margins_(0!4);
	}

	increaseNNSize {
		sizeOfNN = sizeOfNN+1;
		valsList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
	}

	decreaseNNSize {
		sizeOfNN = sizeOfNN-1;
		valsList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(synthName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add([item.msg, item.onOffButton.value])
		};

		saveArray.add(temp);

		^saveArray
	}

	load {|loadArray|
		var tempNNSize=0;
		if(loadArray!=nil,{
			loadArray.postln;
			synthNameBox.valueAction_(loadArray[0].asString.postln);
			synthName.postln;
			synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1], group));
			loadArray[1].do{|item, i|
				if(item[0]!=nil){controls[i].msgBox.valueAction_(item[0].asString)};

				try {
					if(item[1]!=nil){
						controls[i].onOffButton.value_(item[1].asInteger);
						if(item[1]==1, {tempNNSize=tempNNSize+1});
					};
				}
			};
			sizeOfNN = tempNNSize;
			valsList = List.fill(sizeOfNN, {0});
			allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
		})
	}
}