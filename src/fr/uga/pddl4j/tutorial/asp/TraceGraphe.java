package fr.uga.pddl4j.tutorial.asp;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;


public class TraceGraphe extends JFrame {

	private static final long serialVersionUID = 1L;

	/*
	 * Nom du probleme relie au temps mit par l'ASP [0] et par le SAT [1]
	 */
	private final String PATH = "./toPlot.csv";
	private FileWriter fw;
	
	public TraceGraphe() {
		try {
			fw = new FileWriter(PATH);
			fw.append("File; AStar; SAT");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Génère les CSV, on utilisera un programme python pour afficher et tracer les courbes
	 */
	public void add(String[] args, String name) {
	
		AStarSolver asp = new AStarSolver(args);
		asp.search();
		SATSearch sats = new SATSearch(args);
		sats.search();
		try {
			fw.append(name + ";");
			//Si le timeout a expire, on set la valeur a NaN
			if(sats.timeUse() == -1) 
				fw.append(name + ";" + asp.timeUse() + ";" + "\n");
			else 
				fw.append(name + ";" + asp.timeUse() + ";" + sats.timeUse()+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void closeFW() {
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}    
}
