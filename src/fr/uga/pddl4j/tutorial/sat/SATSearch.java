package fr.uga.pddl4j.tutorial.sat;

import java.util.ArrayList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.csp.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.heuristics.relaxation.HeuristicToolKit;
import fr.uga.pddl4j.tutorial.ParserPlanner;
import fr.uga.pddl4j.tutorial.util.IndexFactory;
import fr.uga.pddl4j.util.BitExp;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.IntExp;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;

/**
 * 
 */
public class SATSearch extends ParserPlanner {

	private static final long serialVersionUID = 1L;
	private static final int TIMEOUT = 300;
	private static final int MAX_OPERATORS = 500;

	private final int MIN_STEP;
	private final int FACTS_COUNT;
	private final List<BitOp> operators;
	private final IndexFactory numberGenerator;
	private ArrayList<ArrayList<Integer>> clauses;

	private long time;
	private long timeToEncode = 0;

	public SATSearch(String[] args) {
		super(args);
		numberGenerator = new IndexFactory();
		operators = pb.getOperators();

		clauses = new ArrayList<>();
		FACTS_COUNT = pb.getRelevantFacts().size();
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(Heuristic.Type.FAST_FORWARD, pb);
		final BitState init = new BitState(pb.getInit());
		MIN_STEP = heuristic.estimate(init, pb.getGoal());
	}

	/**
	 * Affichage du resultat sur la sortie standart
	 * 
	 * @param res
	 * @param steps
	 * @return
	 */
	private Plan affichageEtPlan(int[] res, int steps) {
		String[] resString = new String[steps];

		// Tous les bitOps, pour le plan
		BitOp[] resBitOp = new BitOp[steps];
		Plan plan = new SequentialPlan();

		for (int value : res) {
			int index = numberGenerator.getIndex(Math.abs(value));
			int step = numberGenerator.getEtape(Math.abs(value));

			if (index >= FACTS_COUNT && value > 0) {
				// On place l'indice a son etape dans le tableau
				resBitOp[step] = operators.get(index - FACTS_COUNT);
			}
		}

		// Parcours du tableau et affichage
		for (int i = 0; i < steps; i++)
			plan.add(i, resBitOp[i]);

		return plan;

	}

	/**
	 * 
	 * @param step
	 * @param clauses
	 */
	private void generateActions(int step, ArrayList<ArrayList<Integer>> clauses) {
		for (int i = 0; i < operators.size(); i++) {
			int actionId = numberGenerator.generateIndex(i + FACTS_COUNT, step, false);

			BitVector posPreconditions = operators.get(i).getPreconditions().getPositive();
			BitVector negPreconditions = operators.get(i).getPreconditions().getNegative();
			BitVector posEffects = operators.get(i).getUnconditionalEffects().getPositive();
			BitVector negEffects = operators.get(i).getUnconditionalEffects().getNegative();

			for (int j = 0; j < FACTS_COUNT; j++) {
				if (posPreconditions.get(j)) {
					ArrayList<Integer> action = new ArrayList<>();
					action.add(actionId);
					action.add(numberGenerator.generateIndex(j, step, true));
					clauses.add(action);
				}
				if (negPreconditions.get(j)) {
					ArrayList<Integer> action = new ArrayList<>();
					action.add(actionId);
					action.add(numberGenerator.generateIndex(j, step, false));
					clauses.add(action);
				}
				if (posEffects.get(j)) {
					ArrayList<Integer> action = new ArrayList<>();
					action.add(actionId);
					action.add(numberGenerator.generateIndex(j, step + 1, true));
					clauses.add(action);
				}
				if (negEffects.get(j)) {
					ArrayList<Integer> action = new ArrayList<>();
					action.add(actionId);
					action.add(numberGenerator.generateIndex(j, step + 1, false));
					clauses.add(action);
				}
			}
		}
	}

	/**
	 * 
	 * @param step
	 * @param clauses
	 */
	private void generateTransitions(int step, ArrayList<ArrayList<Integer>> clauses) {
		for (int i = 0; i < FACTS_COUNT; i++) {
			ArrayList<Integer> transitionNeg = new ArrayList<>();
			ArrayList<Integer> transitionPos = new ArrayList<>();

			transitionNeg.add(numberGenerator.generateIndex(i, step, true));
			transitionNeg.add(numberGenerator.generateIndex(i, step + 1, false));

			transitionPos.add(numberGenerator.generateIndex(i, step, false));
			transitionPos.add(numberGenerator.generateIndex(i, step + 1, true));

			for (int j = 0; j < operators.size(); j++) {
				if (operators.get(j).getUnconditionalEffects().getPositive().get(i)) {
					transitionNeg.add(numberGenerator.generateIndex(j + FACTS_COUNT, step, true));
				}
				if (operators.get(j).getUnconditionalEffects().getNegative().get(i)) {
					transitionPos.add(numberGenerator.generateIndex(j + FACTS_COUNT, step, true));
				}
			}

			clauses.add(transitionNeg);
			clauses.add(transitionPos);
		}
	}

	/**
	 * 
	 * @param step
	 * @param clauses
	 */
	private void generateDisjunctions(int step, ArrayList<ArrayList<Integer>> clauses) {
		for (int i = 0; i < operators.size() - 1; i++) {
			for (int j = i + 1; j < operators.size(); j++) {
				ArrayList<Integer> disjunction = new ArrayList<>();
				disjunction.add(numberGenerator.generateIndex(i + FACTS_COUNT, step, false));
				disjunction.add(numberGenerator.generateIndex(j + FACTS_COUNT, step, false));
				clauses.add(disjunction);
			}
		}
	}

	/**
	 * 
	 * @param expression
	 * @param clauses
	 */
	private void generateInit(BitExp expression, ArrayList<ArrayList<Integer>> clauses) {
		for (int i = 0; i < FACTS_COUNT; i++) {
			ArrayList<Integer> clause = new ArrayList<>();
			clause.add(numberGenerator.generateIndex(i, 0, expression.getPositive().get(i)));
			clauses.add(clause);
		}
	}

	/**
	 * 
	 * @param expression
	 * @param step
	 * @return goal clauses
	 */
	private ArrayList<ArrayList<Integer>> generateGoal(BitExp expression, int step) {
		ArrayList<ArrayList<Integer>> clauses = new ArrayList<>();

		for (int i = 0; i < FACTS_COUNT; i++) {
			if (expression.getPositive().get(i)) {
				ArrayList<Integer> clause = new ArrayList<>();
				clause.add(numberGenerator.generateIndex(i, step, expression.getPositive().get(i)));
				clauses.add(clause);
			}
		}

		return clauses;
	}

	public CodedProblem getPB() {
		return this.pb;
	}

	/**
	 * Recherche de solution au probleme donne, programme principal
	 * @param cp
	 * @return
	 */
	@Override
	public Plan search(CodedProblem cp) {
		if (pb.getOperators().size() > MAX_OPERATORS) {
			return null;
		}

		int[] result = {};

		int step = 0;
		for (; step < MIN_STEP + 5; step++) {

			// add clause for initial state
			if (step == 0) {
				timeToEncode = System.currentTimeMillis();
				generateInit(pb.getInit(), clauses);
			}

			// generate clauses for current step
			generateActions(step, clauses);

			// generate transitions
			generateTransitions(step, clauses);

			// generate disjunctions
			generateDisjunctions(step, clauses);

			if (step >= MIN_STEP - 1) {
				ArrayList<ArrayList<Integer>> goal = generateGoal(pb.getGoal(), step + 1);
				clauses.addAll(goal);

				// On met a jour le chronometre
				super.getStatistics().setTimeToEncode(System.currentTimeMillis() - timeToEncode);
				time = System.currentTimeMillis();

				try {
					result = solverSat();
				} catch (Exception e) {
					System.out.println("[SAT] timeout exception");
					// Si le timeout a ete depassé on set le temps pour solve a -1 et on arrete
					super.getStatistics().setTimeToSearch(-1);
					return null;
				}

				super.getStatistics().setTimeToSearch(System.currentTimeMillis() - time);

				if (result.length == 0) {
					clauses.removeAll(goal);
				} else {
					break;
				}
			}
		}

		return affichageEtPlan(result, step + 1);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private int[] solverSat() throws Exception {
		final int MAXVAR = 1000000;
		final int NBCLAUSES = clauses.size();

		ISolver solver = SolverFactory.newDefault();

		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
		solver.setTimeout(TIMEOUT);

		for (ArrayList<Integer> clause : clauses) {
			try {
				int[] arr = clause.stream().mapToInt(el -> el).toArray();
				solver.addClause(new VecInt(arr));
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
		}

		IProblem problem = solver;

		System.out.println("[SAT] checking satisfiability");

		int[] model = new int[] {};

		if (problem.isSatisfiable()) {
			model = problem.model();
		}

		return model;
	}
}
