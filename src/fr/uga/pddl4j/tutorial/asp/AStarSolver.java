package fr.uga.pddl4j.tutorial.asp;

import java.util.Properties;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.search.strategy.AStar;
import fr.uga.pddl4j.planners.statespace.search.strategy.Node;
import fr.uga.pddl4j.planners.statespace.search.strategy.StateSpaceStrategy;
import fr.uga.pddl4j.util.Plan;

public final class AStarSolver extends ASP {

	public AStarSolver(String[] args) {
		super(args);
	}

	@Override
	public Plan search(final CodedProblem problem) {
		long time = System.currentTimeMillis();
		int timeout = (int) this.arguments.get(Planner.TIMEOUT);
		double weight = (double) arguments.get(StateSpacePlanner.WEIGHT);         
		StateSpaceStrategy astar = new AStar(timeout, Heuristic.Type.FAST_FORWARD, weight);
		super.getStatistics().setTimeToEncode(System.currentTimeMillis() - time);
		time= System.currentTimeMillis();
		Node goalNode = astar.searchSolutionNode(problem);
		super.getStatistics().setTimeToSearch(System.currentTimeMillis() - time);
		return astar.extractPlan(goalNode, problem);
	}



	
}
