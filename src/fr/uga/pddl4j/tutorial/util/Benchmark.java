package fr.uga.pddl4j.tutorial.util;

import java.io.File;

public class Benchmark {

	public static final String PATH = "File/";

	/**
	 * Methode pour commencer le benchmark. Elle commence par un fichier donne dans
	 * le PATH. Elle a aussi besoin d'un traceur pour enregistrer les donnees dans
	 * un fichier csv.
	 */
	public static void start(String PATH, TraceGraphe tg) {
		// ouverture du ficher
		File file = new File(PATH);
		File[] files = file.listFiles();
		if (files != null) {
			// Si c'est un repertoire on le parcours
			if (files[0].isDirectory()) {
				start(files[0].toPath().toString(), tg);
				return;
			} else {
				// Sinon on creer les arguments du fichiers, afin de pouvoir lancer la recherche
				createArgs(files, tg);
			}
		}
	}

	/**
	 * Creation des arguments
	 */
	private static void createArgs(File[] files, TraceGraphe tg) {
		// Recherche du fichier domaine
		String domaine = "";
		for (File f : files) {
			String path = f.toString();
			if (path.contains("domain")) {
				domaine = path;
				break;
			}
		}

		if (domaine == "") {
			System.out.println("Pas de fichier contenant un domaine, affichage impossible");
			System.exit(1);
		}

		// Initialisation de la ligne d'argument, il ne manque que le probleme
		String[] toAdd = { "-o", domaine, "-f", "" };

		// Iteration sur tout les problemes
		for (File f : files) {
			String path = f.toString();
			if (path != domaine) {
				// Affichage du fichier afin de savoir l'etat de la recherche, cette derniere
				// pouvant etre lente
				System.out.println(f.getPath());
				// Mise a jour du probleme dans la ligne d'option
				toAdd[3] = path;
				// Envoie de la ligne au traceur, pour qu'il puisse initialiser et traiter les
				// solveurs
				tg.add(toAdd, f.getPath());
			}
		}
	}
}
