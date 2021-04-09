package fr.uga.pddl4j.tutorial.asp;

import java.io.File;

public class Benchmark {

	static final String PATH = "File/";
	
	public static void start(String PATH) {
		TraceGraphe tg = new TraceGraphe();
        File file = new File(PATH);
        File[] files = file.listFiles();
        if (files != null) {
	        if (files[0].isDirectory()) {
	            start(files[0].toPath().toString());
	            return;
	        } else 
	            createArgs(files, tg);
        }
        tg.trace();
	}

	private static void createArgs(File[] files, TraceGraphe tg) {
		String domaine = "";
		for (File f : files) {
			String path = f.toString();
			if(path.contains("domain")) {
				domaine = path;
				break;
			}	
		}
		
		if (domaine == "") {
			System.out.println("Pas de fichier contenant un domaine, affichage impossible");
			System.exit(1);
		}
		String [] toAdd = {"-o", domaine, "-f", ""};
		for (File f : files) {
			String path = f.toString();
			if(path != domaine) {
				toAdd[3] = path;
				tg.add(toAdd, f.getPath());
			}	
		}
	}
}
