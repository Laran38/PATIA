package fr.uga.pddl4j.tutorial.util;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;

import fr.uga.pddl4j.tutorial.asp.AStarSolver;
import fr.uga.pddl4j.tutorial.sat.SATSearch;
import fr.uga.pddl4j.util.Plan;

public class TraceGraphe extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Nom du probleme relie au temps mit par l'ASP [0] et par le SAT [1]
	 */
	private final String PATH = "./toPlot.csv";
	private FileWriter fw;

	public TraceGraphe() {
		try {
			fw = new FileWriter(PATH);
			fw.append("File; AStar; SAT; AStarP; SATP\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Genere les CSV, on utilisera un programme python pour afficher et tracer les
	 * courbes
	 */
	public void add(String[] args, String name) {

		AStarSolver asp = new AStarSolver(args);
		Plan pa = asp.search();
		SATSearch sats = new SATSearch(args);
		Plan ps = sats.search();
		name = gererName(name);
		try {
			fw.append(name + ";");

			// Si le timeout a expire, on set la valeur a NaN
			if (sats.timeUse() == -1)
				fw.append(asp.timeUse() + ";" + ";" + pa.cost() + "\n");
			else
				fw.append(asp.timeUse() + ";" + sats.timeUse() + ";" + pa.cost() + ";" + ps.cost() + "\n");

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

	private String gererName(String name) {
		String[] all = name.split("/");
		all = all[all.length - 1].split(".pddl");
		return all[all.length - 1];
	}
}
