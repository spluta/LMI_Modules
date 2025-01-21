b = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 54, 56, 57, 58, 60, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 86, 88, 89, 90, 92, 96, 97, 98, 99, 100, 101, 102, 104, 105, 106, 108, 112, 113, 114, 116, 120, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 144, 145, 146, 147, 148, 149, 150, 152, 153, 154, 156, 160, 161, 162, 163, 164, 165, 166, 168, 169, 170, 172, 176, 177, 178, 180, 184, 192, 193, 194, 195, 196, 197, 198, 200, 201, 202, 204, 208, 209, 210, 212, 216, 224, 225, 226, 228, 232, 240];

m.size

m = PathName("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/Proteus/Proteus_tone_packs").files.select({|file| file.extension=="json"});
m = m.add(m[0]);
m = m.add(m[1]);

m[0].fullPath

c = Dictionary();

b.do{|item, i| c.put(item.asSymbol, [m[i].fullPath, 0.5].postln)}
c
c.writeArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/Proteus/models_dict")

c = Object.readArchive("/Users/spluta1/Documents/SC/LiveModularInstrument/LMI_Modules/Proteus/models_dict")