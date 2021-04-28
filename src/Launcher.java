import fr.uga.pddl4j.tutorial.ParserPlanner;
import fr.uga.pddl4j.tutorial.asp.AStarSolver;
import fr.uga.pddl4j.tutorial.sat.SATSearch;
import fr.uga.pddl4j.tutorial.util.TraceGraphe;
import fr.uga.pddl4j.tutorial.util.Benchmark;

import java.util.Arrays;

public class Launcher {

	/**
	 * Traite la liste d'arguments et execute une des options: ASP, SAT, Benchmark
	 * (ASP et SAT).
	 * 
	 * @param args
	 */

	public static void start(String[] args) {
		if (args.length < 1) {
			ParserPlanner.afficherAide();
			System.exit(1);
		}

		String arg = args[0];
		String[] solverArguments = Arrays.copyOfRange(args, 1, args.length);

		switch (arg) {
		case "-a":
			AStarSolver as = new AStarSolver(solverArguments);
			as.search();
			break;
		case "-s":
			SATSearch ss = new SATSearch(solverArguments);
			ss.search();
			break;
		case "-p":
			TraceGraphe tg = new TraceGraphe();
			Benchmark.start(Benchmark.PATH, tg);
			tg.closeFW();
			break;
		default:
			ParserPlanner.afficherAide();
		}
	}

	public static void main(String[] args) {
		start(args);
	}

}
