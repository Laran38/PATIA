package fr.uga.pddl4j.tutorial.asp;

import java.io.File;
import java.util.Properties;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.util.Plan;

abstract public class ParserPlanner extends AbstractStateSpacePlanner {

	private static final long serialVersionUID = 1L;
	protected Properties arguments;
	protected long time;
	protected CodedProblem pb;
	protected static String PATH = "";

	protected ParserPlanner() {
	}

	/**
	 * Parse les arguments et enregistre le temps pour parser
	 */

	public ParserPlanner(String[] args) {
		this.arguments = gererOptions(args);
		this.time = System.currentTimeMillis();
		this.pb = this.parse(args);
		super.getStatistics().setTimeToParse(System.currentTimeMillis() - time);
	}

	public ParserPlanner(final Properties arguments) {
		super();
		this.arguments = arguments;
	}

	/*
	 * Parse le problème.
	 */
	public CodedProblem parse(String[] args) {
		time = System.currentTimeMillis();
		final Properties arguments = ParserPlanner.gererOptions(args);
		if (arguments == null) {
			ParserPlanner.afficherAide();
			System.exit(0);
		}
		final ProblemFactory fact = ProblemFactory.getInstance();
		File domain = (File) arguments.get(Planner.DOMAIN);
		File problem = (File) arguments.get(Planner.PROBLEM);

		try {
			ErrorManager em;
			em = fact.parse(domain, problem);
			if (!em.isEmpty()) {
				em.printAll();
				Planner.getLogger().trace(em);
				System.exit(1);
			}
		} catch (Exception e) {
			Planner.getLogger().trace("\nunexpected error when parsing the PDDL planning problem description.");
			System.exit(0);
		}

		final CodedProblem pb = fact.encode();
		Planner.getLogger().trace("\nencoding problem done successfully (" + pb.getOperators().size() + " ops, "
				+ pb.getRelevantFacts().size() + " facts)\n");

		if (!pb.isSolvable()) {
			Planner.getLogger()
					.trace(String.format("goal can be simplified to FALSE." + "no search will solve it%n%n"));
			System.exit(0);
		}

		return pb;
	}

	/*
	 * Affichage de toutes les options possible
	 */
	public static void afficherAide() {
		StringBuilder sb = new StringBuilder();
		sb = sb.append("\nHelp:\n");
		sb = sb.append("options description  \n");
		sb = sb.append("-mode -opt  \n");
		sb = sb.append("with -mode : \n");
		sb = sb.append("-a    A star solver \n");
		sb = sb.append("-s    SAT Solver \n");
		sb = sb.append("-p    Benchmark, A star and SAT\n");
		sb = sb.append("with -opt :  \n");
		sb = sb.append("-h    print this message \n");
		sb = sb.append("-o <str>   operator file name \n");
		sb = sb.append("-f <str>   fact file name \n");
		sb = sb.append("-w <num>   the weight used in the a star search (preset: 1) \n");
		sb = sb.append("-t <num>   specifies the maximum CPU-time in seconds (preset: 300) \n");
		sb = sb.append("-p 		   lunch the benchmark. Can be very long ! \n");
		sb = sb.append("-h    print this message \n");
		Planner.getLogger().trace(sb);
	}

	/**
	 * Renvoie si un fichier est valide pour le parser
	 */
	private static boolean estValide(int i, String[] fichiers) {
		boolean existe = new File(fichiers[i + 1]).exists();
		if (!existe)
			Planner.getLogger().trace("Error \nThe file " + fichiers[i + 1] + " does not exist\n");

		return i < fichiers.length && existe;
	}

	/**
	 * Parcours les options et agis differement selon ce qu'il recoit
	 */
	protected static Properties gererOptions(String[] args) {
		Properties arg = StateSpacePlanner.getDefaultArguments();
		String type;
		Object value;
		for (int i = 0; i < args.length; i += 2) {
			switch (args[i]) {
			case "-o":
				if (!estValide(i, args))
					return null;
				type = Planner.DOMAIN;
				value = new File(args[i + 1]);
				break;
			case "-f":
				if (!estValide(i, args))
					return null;
				type = Planner.PROBLEM;
				value = new File(args[i + 1]);
				break;
			case "-t":
				if (i >= args.length || (int) (value = Integer.parseInt(args[i + 1]) * 1000) < 0)
					return null;
				type = Planner.TIMEOUT;
				arg.put(type, value);
				break;
			case "-w":
				value = Double.parseDouble(args[i + 1]);
				if ((int) value < 0)
					return null;
				type = StateSpacePlanner.WEIGHT;
				break;
			default:
				return null;

			}
			arg.put(type, value);
		}
		if (estSpecifie(arg, Planner.DOMAIN) && estSpecifie(arg, Planner.PROBLEM))
			return arg;
		return null;
	}

	private static boolean estSpecifie(Properties arg, String aSpecifie) {
		return arg.get(aSpecifie) != null;
	}

	/**
	 * Recupere le temps mis par le solver, renvoie -1 si ce dernier n'a pas fini
	 */
	public long timeUse() {
		if (this.getStatistics().getTimeToSearch() == -1)
			return -1;
		return this.getStatistics().getTimeToEncode() + this.getStatistics().getTimeToParse()
				+ this.getStatistics().getTimeToSearch();
	}

	/**
	 * Trace le plan selon un probleme donne
	 */
	public void search() {
		Plan plan = this.search(this.pb);
		if (plan != null) {
			Planner.getLogger().trace(String.format("%nfound plan as follows:%n%n" + pb.toString(plan)));
			Planner.getLogger().trace(String.format("%nplan total cost: %4.2f%n%n", plan.cost()));
		} else {
			Planner.getLogger().trace(String.format(String.format("%nno plan found%n%n")));
		}
	}

}
