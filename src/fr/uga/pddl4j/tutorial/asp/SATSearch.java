package fr.uga.pddl4j.tutorial.asp;

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
import fr.uga.pddl4j.util.BitExp;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.IntExp;
import fr.uga.pddl4j.util.Plan;

public class SATSearch extends ASP {
	
	private static final long serialVersionUID = 1L;
	private long timeToEncode = 0;
	private static final int TIMEOUT = 100;
	private final int MIN_STEP;
	private final int tailleFact;
	private final List<BitOp> operators;
	private final List<IntExp> revelantFacts;
	private final IndexFactory numberGenerator;
	private ArrayList<ArrayList<Integer>> clauses;
	private Integer etape;
	private long time;

	public SATSearch(String[] args) {
		super(args);
		this.numberGenerator = new IndexFactory();		
		this.operators = pb.getOperators();
		this.revelantFacts = pb.getRelevantFacts();
		this.etape = 0;

		PATH += "test.txt";

		this.clauses = new ArrayList<>();
		this.tailleFact = revelantFacts.size();
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(Heuristic.Type.FAST_FORWARD, pb);
		final BitState init = new BitState(pb.getInit());
		this.MIN_STEP = heuristic.estimate(init, pb.getGoal());
	}

	private void afficheFinal(String res) {
		String[] rest = res.split(" ");
		String[] resString = new String[this.etape + 1]; 
		for (String courant : rest) {
			int value = Integer.parseInt(courant);
			int index = Math.abs(numberGenerator.getIndex(value));
			int etape = Math.abs(numberGenerator.getEtape(value));
			if (index > this.tailleFact && value > 0) {
				index -= this.tailleFact;
				BitOp operation = operators.get(index - 1);
				resString[etape] = "\t" + operation.getName() + " ";
				for (int op : operation.getInstantiations())
					resString[etape] += pb.getConstants().get(op) + " ";
			}
		}
		for (int i = 0; i < this.etape; i++) {
			System.out.println("Etape " + i + " : ");
			System.out.println(resString[i]);
		}
	
	}
	
	private String solverSat() throws Exception {
		final int MAXVAR = 1000000;
		final int NBCLAUSES = 500000;

		ISolver solver = SolverFactory.newDefault();
		DimacsReader reader = new DimacsReader(solver);

		// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
		solver.setTimeout(TIMEOUT);

		for (int i = 0; i < clauses.size(); i++) {
			try {
				int[] clause = new int[clauses.get(i).size()];
				for (int j = 0; j < clauses.get(i).size(); j++) {
					clause[j] = clauses.get(i).get(j);
				}
				solver.addClause(new VecInt(clause));
			}
			catch (ContradictionException e) {
				e.printStackTrace();
			}
		}

		IProblem problem = solver;
		
		if (problem.isSatisfiable()) 
			return reader.decode(problem.model());
		else 
			return "";
		
	}
	
	private void genererDisjonction() {
		ArrayList<Integer> toAdd;
		for (int i = 0; i < operators.size(); i++) {
			for (int j = i + 1; j <= operators.size(); j++) {
				toAdd = new ArrayList<>();
				toAdd.add(numberGenerator.genererIndex(i + tailleFact, etape, false));
				toAdd.add(numberGenerator.genererIndex(j + tailleFact, etape, false));
				clauses.add(toAdd);
			}
		}		
	}

	private void genererTransition() {
		for(int i = 0; i < revelantFacts.size(); i++) {
			ArrayList<Integer> indexPos = new ArrayList<>();
			ArrayList<Integer> indexNeg = new ArrayList<>();
			int index = revelantFacts.size();
			for(BitOp ai : operators) {
				BitExp effect = ai.getCondEffects().get(0).getEffects();
				if (effect.getPositive().get(i))
					indexPos.add(numberGenerator.genererIndex(index, etape, true));
				if (effect.getNegative().get(i))
					indexNeg.add(numberGenerator.genererIndex(index, etape, true));
				index ++;
			}
			int i1 = numberGenerator.genererIndex(i, etape, true);
			int i2 = numberGenerator.genererIndex(i, etape + 1, true);
			implicationTransition(i1 * -1,i2, indexPos);
			implicationTransition(i1, i2 * -1, indexNeg);
		}
		
	}

	private void implicationTransition(int f1, int f2, ArrayList<Integer> index) {
		index.add(f1 * -1);
		index.add(f2 * -1);
		clauses.add(index);
	}

	private void genererActions() {
		int posCourante = revelantFacts.size();
		for(BitOp ai : operators) {
			ArrayList<Integer> toAdd = new ArrayList<>();
			int indiceAction = numberGenerator.genererIndex(posCourante, this.etape, true);
			for (int i = 0; i < revelantFacts.size(); i++) {
				BitExp precondition = ai.getPreconditions();
				if (precondition.getPositive().get(i))
					toAdd.add(numberGenerator.genererIndex(i, this.etape, true));
				if (precondition.getNegative().get(i))
					toAdd.add(numberGenerator.genererIndex(i, this.etape, false));
				
				BitExp effect = ai.getCondEffects().get(0).getEffects();
				if (effect.getPositive().get(i))
					toAdd.add(numberGenerator.genererIndex(i, this.etape + 1, true));
				if (effect.getNegative().get(i))
					toAdd.add(numberGenerator.genererIndex(i, this.etape + 1, false));
			}
			implication(indiceAction, toAdd);
			posCourante ++;
		}
	}

	private void implication(int indiceAction, ArrayList<Integer> toAdd) {
		indiceAction *= -1;
		ArrayList<Integer> toArr;
		for(Integer i : toAdd) {
			toArr = new ArrayList<>();
			toArr.add(indiceAction);
			toArr.add(i);
			clauses.add(toArr);
		}	
	}
	
	private void genererGoal(BitExp bitExp) {
		ArrayList<Integer> allInt;
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			if (bitExp.getPositive().get(i)) {
				allInt.add(numberGenerator.genererIndex(i, this.etape, true));
				this.clauses.add(allInt);
			}
			else if (bitExp.getNegative().get(i)) {
				allInt.add(numberGenerator.genererIndex(i, this.etape, false));
				this.clauses.add(allInt);
			}
		}	
	}
	
	private void genererInit(BitExp bitExp) {
		ArrayList<Integer> allInt;
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			allInt.add(numberGenerator.genererIndex(i, this.etape, bitExp.getPositive().get(i)));
			this.clauses.add(allInt);
		}	
	}

	public CodedProblem getPB() {
		return this.pb;
	}

	@Override
	public void search() {
		// time = 0
		if(this.etape == 0) {
			timeToEncode = System.currentTimeMillis();
			genererInit(pb.getInit());
		}
		genererActions();
		genererTransition();
		genererDisjonction();
		this.etape++;
		if(this.MIN_STEP > this.etape)
			this.search();
		else {
			int save = this.clauses.size();
			genererGoal(pb.getGoal());
			super.getStatistics().setTimeToEncode(System.currentTimeMillis() - timeToEncode);
			time= System.currentTimeMillis();

			String res;
			try {
				res = solverSat();
			} catch (Exception e) {
				super.getStatistics().setTimeToSearch(TIMEOUT);
				return;
			}
			super.getStatistics().setTimeToSearch(System.currentTimeMillis() - time);
			
			if(res == "") {
				for(int i = this.clauses.size() - 1; i >= save; i--)
					this.clauses.remove(i);
				this.search();
			}
			else
				afficheFinal(res);
		}
	}
	
	@Override
	public Plan search(CodedProblem arg) {
		this.pb = arg;
		this.search();
		return null;
	}
}
