ProtoType_Mod :  Module_Mod {
	var goButton, controlsBus, exportButton, withTextView = true, numControls = 10, exportName, textList, startingAt, savedProtos, menuNumber=0, savePresetButton, deletePresetButton, presetPopUp;

	init {
		textList = Array.fill(numControls, {"text"});
		savedProtos = Object.readArchive(PathName(this.class.filenameSymbol.asString).pathOnly++"ProtoTypePresets");
		this.init2;
	}

	init3 {
		goButton = Button()
		.states_([["go", Color.grey, Color.white]])
		.action_{arg butt;
			group.freeAll;
			try {
				var def, synth, synthMsg;
				def = ("{arg inBus, controlsBus, outBus;"
					++controls[0].string++"}"
				).interpret
				.asSynthDef(
					fadeTime: 0.02,
					name: SystemSynthDefs.generateTempName
				);
				synth = Synth.basicNew(def.name, group.server);
				synthMsg = synth.newMsg(group.asTarget, [\inBus, mixerToSynthBus, \controlsBus, controlsBus, \outBus, outBus]);
				def.doSend(group.server, synthMsg);
			}
		};

		exportName = TextField().string_("Temp_Mod");

		savePresetButton = Button()
		.states_([["save preset", Color.green, Color.white]])
		.action_{arg butt;
			savedProtos.add([exportName.value.asString, controls[0].string]);
			savedProtos.writeArchive(PathName(this.class.filenameSymbol.asString).pathOnly++"ProtoTypePresets");
			presetPopUp.items = ["nil"].addAll(savedProtos.flop[0]);
			presetPopUp.value_(presetPopUp.items.size-1);
		};

		deletePresetButton = Button()
		.states_([["delete preset", Color.grey, Color.white]])
		.action_{arg butt;
			if(menuNumber>0){
				savedProtos.removeAt(menuNumber-1);
				savedProtos.writeArchive(PathName(this.class.filenameSymbol.asString).pathOnly++"ProtoTypePresets");
				presetPopUp.items = ["nil"].addAll(savedProtos.flop[0]);
				presetPopUp.value_(0);
			}
		};

		presetPopUp = PopUpMenu();
		presetPopUp.items = [nil].addAll(savedProtos.flop[0]);
		presetPopUp.action = {arg menu;
			menuNumber = menu.value.postln;
			if(menu.value>0){
				savedProtos.postln;
				controls[0].string = savedProtos.flop[1][menu.value-1].postln;
			}
		};

		exportButton = Button()
		.states_([["export", Color.blue, Color.white]])
		.action_{arg butt;
			var numControls = controls.copyRange(1,10).select{|item| item.textField.string!=""}.size;
			var text = exportName.value++" : ProtoType_Mod {

*initClass {
StartUp.add {
SynthDef("++($\\)++exportName.value.toLower++",{arg inBus, controlsBus, outBus;
"++controls[0].string++"
}).writeDefFile;

}
}

loadExtra {
}

init {
numControls = "++numControls++";
textList = Array.fill(numControls, {"++($")++"text"++($")++"});
withTextView = false;
this.init2;
}

init3 {
synths.add(Synth("++($")++exportName.value.toLower++($")++", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
}
}";
			var file = (PathName(this.class.filenameSymbol.asString).pathOnly++exportName.value++".sc");

			file = File.new(file, "w");
			file.write(text);
			file.close;
		};

	}

	init2 {
		this.initControlsAndSynths(numControls+2);
		this.makeMixerToSynthBus(2);

		controlsBus = Bus.control(group.server, numControls);

		startingAt = 0;
		if(withTextView){
			startingAt = 1;
			controls.add(MyTextView().string_(""));
		};

		numControls.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i+startingAt, textList[i],
				{arg val; controlsBus.setAt(i, val)},
				true));
		};

		this.init3;

		this.makeWindow2;
	}

	makeWindow2 {
		var temp;

		temp = this.class.asString;
		this.makeWindow(temp.copyRange(0, temp.size-5));

		if(withTextView){

			win.layout_(
				VLayout(
					goButton,controls[0],
					VLayout(*controls.copyRange(1,numControls).collect({arg item; item})),
					exportName,
					HLayout(savePresetButton, deletePresetButton),
					presetPopUp,
					exportButton
				)
			);
			win.layout.spacing_(1).margins_(1!4);
			win.view.resizeTo(10*17,numControls+15*17);
		}{
			win.layout_(
				VLayout(
					VLayout(*controls.copyRange(0,numControls-1).collect({arg item; item}))
				)
			);
			win.layout.spacing_(1).margins_(1!4);
			win.view.resizeTo(10*17,numControls*17);
		};


		win.front;
	}

	loadExtra {
		goButton.valueAction_(1);
	}

	killMeSpecial {
		group.freeAll;
	}
}