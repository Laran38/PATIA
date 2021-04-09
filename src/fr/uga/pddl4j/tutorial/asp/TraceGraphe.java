package fr.uga.pddl4j.tutorial.asp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;

import fr.uga.pddl4j.planners.Statistics;


public class TraceGraphe extends JFrame {
	/*
	 * Nom du probleme relié au temps mit par l'ASP [0] et par le SAT [1]
	 */
	private final String PATH = "./toPlot.txt";
	private HashMap<String, long[]> listeProblemeTemps;
	
	
	public TraceGraphe() {
		this.listeProblemeTemps = new HashMap<>();
	}
	
	
	/*
	 * Generer les CSV, python pour plot
	 */
	public void trace() {
		//Parcourir la hashmap, plot 
		try {
			FileWriter fw = new FileWriter(PATH);
			for(Entry<String, long[]> keys : listeProblemeTemps.entrySet()) {
				long resSat = keys.getValue()[1];
				long resASS = keys.getValue()[0];
				String name = keys.getKey();
				fw.append(name + ";" + resASS + ";" + resSat);
				System.out.println("probleme " + keys.getKey() + " resolu en " + keys.getValue()[1] + " pour le sat!");
			}
			fw.close();
			System.out.println("FINI");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(String[] args, String name) {
		AStarSolver asp = new AStarSolver(args);
		asp.search();
		SATSearch sats = new SATSearch(args);
		sats.search();
		long[] res = {asp.timeUse(),sats.timeUse()};
		this.listeProblemeTemps.put(name, res);
	}    
}
