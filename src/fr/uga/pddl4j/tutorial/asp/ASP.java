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

abstract public class ASP extends AbstractStateSpacePlanner {
	
	private static final long serialVersionUID = 1L;
	protected Properties arguments;
	protected long time;
	protected CodedProblem pb;
	protected static String PATH = "";
	
	public ASP() {
	}

	public ASP(String[] args) {
		this.arguments = gererOptions(args);
		this.time = System.currentTimeMillis();
		this.pb = this.parse(args, new TraceGraphe());
		super.getStatistics().setTimeToParse(System.currentTimeMillis() - time);
	}

	public ASP(final Properties arguments) {
	    super();
	    this.arguments = arguments;
	}

	private static void afficherAide() {
		StringBuilder sb = new StringBuilder();
		sb = sb.append("\nMenu d'aide:\n");
		sb = sb.append("DESCRIPTIONS DES OPTIONS \n");
		sb = sb.append("-o <str>   operator file name \n");
		sb = sb.append("-f <str>   fact file name \n");
		sb = sb.append("-w <num>   the weight used in the a star search (preset: 1) \n");
		sb = sb.append("-t <num>   specifies the maximum CPU-time in seconds (preset: 300) \n");
		sb = sb.append("-h    print this message \n");
		Planner.getLogger().trace(sb);
	}
	
	private static boolean estValide(int i, String[] fichiers) {
		boolean existe = new File(fichiers[i+1]).exists();
		if(!existe)
			Planner.getLogger().trace("Erreur \nLe fichier "+fichiers[i+1]+" n'existe pas\n");
			
		return i < fichiers.length && existe; 
	}
		
	protected static Properties gererOptions(String[] args) {
		Properties arg = StateSpacePlanner.getDefaultArguments();
		String type;
		Object value;
		for(int i=0; i < args.length; i+=2) {
			switch (args[i]) {
			case "-o": 
				if(!estValide(i, args))	return null;
				type = Planner.DOMAIN;
				value = new File(args[i+1]);
				break;
			case "-f": 
				if(!estValide(i, args))	return null;
				type = Planner.PROBLEM;
				value = new File(args[i+1]);
				break;
			case "-t": 
				if (i >= args.length ||
					(int)(value = Integer.parseInt(args[i + 1]) * 1000) < 0) return null;
				type = Planner.TIMEOUT;
				arg.put(type, value);
				break;
			case "-p": 
				TraceGraphe tracer = new TraceGraphe();
				Benchmark.start(Benchmark.PATH, tracer);
				tracer.closeFW();
				return null;
			case "-w": 
				value = Double.parseDouble(args[i + 1]);
				if ((int) value < 0) return null;
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
			
	public CodedProblem parse(String[] args, TraceGraphe tg) {
		time = System.currentTimeMillis();
		final Properties arguments = ASP.gererOptions(args);
		  if (arguments == null) {
		    ASP.afficherAide();
		    System.exit(0);
		  }
		  final ProblemFactory fact = ProblemFactory.getInstance();
		  File domain = (File) arguments.get(Planner.DOMAIN);
		  File problem = (File) arguments.get(Planner.PROBLEM);
		  try {
			  ErrorManager em;
			  em = fact.parse(domain, problem);
			  if(!em.isEmpty()) {
				  em.printAll();
				  Planner.getLogger().trace(em);
			  	  System.exit(1);
			  }
		  }
		  catch (Exception e) {
			  Planner.getLogger().trace("\nunexpected error when parsing the PDDL planning problem description.");
			  System.exit(0);
		  }
		  final CodedProblem pb = fact.encode();
		  Planner.getLogger().trace("\nencoding problem done successfully (" 
	  		    + pb.getOperators().size() + " ops, "
	  		    + pb.getRelevantFacts().size() + " facts)\n");
		  
		  if (!pb.isSolvable()) {
			  Planner.getLogger().trace(String.format("goal can be simplified to FALSE." 
			                                            +  "no search will solve it%n%n"));
			  System.exit(0);
			}	
		  return pb;
	}
	
	public long timeUse() {
		return this.getStatistics().getTimeToEncode() + this.getStatistics().getTimeToParse() + this.getStatistics().getTimeToSearch();
	}
	
	public void search() {
		Plan plan = this.search(this.pb);
		if (plan != null) {
			  Planner.getLogger().trace(String.format("%nfound plan as follows:%n%n" + pb.toString(plan)));
			  Planner.getLogger().trace(String.format("%nplan total cost: %4.2f%n%n", plan.cost()));
			} else {
			  Planner.getLogger().trace(String.format(String.format("%nno plan found%n%n")));
		}
	}
	
	public static void main(String[] args) {
		new AStarSolver(args);
		
	}


}
