package fr.uga.pddl4j.tutorial.asp;

import fr.uga.pddl4j.tutorial.ParserPlanner;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.search.strategy.AStar;
import fr.uga.pddl4j.planners.statespace.search.strategy.Node;
import fr.uga.pddl4j.planners.statespace.search.strategy.StateSpaceStrategy;
import fr.uga.pddl4j.tutorial.ParserPlanner;
import fr.uga.pddl4j.util.Plan;

public final class AStarSolver extends ParserPlanner {

	private static final long serialVersionUID = 1L;

	public AStarSolver(String[] args) {
		super(args);
	}

	/**
	 * Le problème est parsé par la classe mere. Nous n'avons
	 * besoin que du solver
	 */
	@Override
	public Plan search(final CodedProblem problem) {
		// Initialisation du chronometre
		long time = System.currentTimeMillis();
		int timeout = DEFAULT_TIMEOUT;
		double weight = (double) arguments.get(StateSpacePlanner.WEIGHT);
		StateSpaceStrategy astar = new AStar(timeout, Heuristic.Type.FAST_FORWARD, weight);

		// Mise a jour du temps pour encoder
		super.getStatistics().setTimeToEncode(System.currentTimeMillis() - time);

		// Mise a jour du chronometre
		time = System.currentTimeMillis();
		Node goalNode = astar.searchSolutionNode(problem);

		// Mise a jour du temps de recherche
		super.getStatistics().setTimeToSearch(System.currentTimeMillis() - time);
		return astar.extractPlan(goalNode, problem);
	}

}
