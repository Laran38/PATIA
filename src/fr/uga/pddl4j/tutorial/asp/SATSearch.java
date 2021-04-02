package fr.uga.pddl4j.tutorial.asp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.csp.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.heuristics.relaxation.HeuristicToolKit;
import fr.uga.pddl4j.util.BitExp;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.IntExp;

public class SATSearch{
	
	private static final int TIMEOUT = 300;
	private final int MIN_STEP;
	private final int tailleFact;
	private final String PATH;
	private final CodedProblem pb;
	private final List<BitOp> operators;
	private final List<IntExp> revelantFacts;
	private final IndexFactory numberGenerator;
	private ArrayList<ArrayList<Integer>> toSat;
	private Integer etape;	
	
	public SATSearch(CodedProblem pb) {
		this.numberGenerator = new IndexFactory();		
		this.operators = pb.getOperators();
		this.revelantFacts = pb.getRelevantFacts();
		this.pb = pb;
		this.etape = 0;
		this.toSat = new ArrayList<>();
		this.PATH = "File/test.txt";
		this.tailleFact = revelantFacts.size();
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(Heuristic.Type.FAST_FORWARD, pb);
		final BitState init = new BitState(pb.getInit());
		this.MIN_STEP = heuristic.estimate(init, pb.getGoal());
	}

	public void start() {
		/*
		 * Sur taquin 
		 */
		// time = 0
		if(this.etape == 0)
			genererInit(pb.getInit());
		genererActions();
		genererTransition();
		genererDisjonction();
		this.etape++;
		if(this.MIN_STEP > this.etape)
			this.start();
		else {
			int save = this.toSat.size();
			genererGoal(pb.getGoal());
			//time = 9ms
			genererFichierCNF((this.revelantFacts.size() + this.operators.size()) * this.etape);
			//time = 1,145s
			String res = solverSat(PATH);
			//time = tooLong
			if(res == "") {
				for(int i = this.toSat.size() - 1; i >= save; i--)
					this.toSat.remove(i);
				this.start();
				return;
			}
			else
				afficheFinal(res);
		}
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
	
	private void genererFichierCNF(int nbVar) {
		/*
		 * p cnf nbVar tailleListe
		 *
		 * Pour chaque elem : marque tous les ou + 0
		 */
		try {
			FileWriter fw = new FileWriter(PATH);
			fw.append("p cnf " + nbVar + " " + this.toSat.size() + "\n");
			for (ArrayList<Integer> ts : toSat) {
				for (Integer i : ts)
					fw.append(i + " ");
				fw.append("0\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String solverSat(String path) {
		ISolver solver = SolverFactory.newDefault();
	    solver.setTimeout(this.TIMEOUT); 
	    DimacsReader reader = new DimacsReader(solver);
	    try {
	        IProblem problem = reader.parseInstance(path);
	        if (problem.isSatisfiable()) {
	            return reader.decode(problem.model());
	        } else 
	            System.out.println("Unsatisfiable a l etape " + this.etape);
	        	return "";
	    }
	    catch (Exception e) {
			e.printStackTrace();
		}
	    return "";
	}
	
	private void genererDisjonction() {
		ArrayList<Integer> toAdd;
		for (int i = 0; i < operators.size(); i++) {
			for (int j = i + 1; j <= operators.size(); j++) {
				toAdd = new ArrayList<>();
				toAdd.add(numberGenerator.genererIndex(i + tailleFact, etape, false));
				toAdd.add(numberGenerator.genererIndex(j + tailleFact, etape, false));
				toSat.add(toAdd);
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
		toSat.add(index);
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
			toSat.add(toArr);
		}	
	}
	
	private void genererGoal(BitExp bitExp) {
		ArrayList<Integer> allInt;
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			if (bitExp.getPositive().get(i)) {
				allInt.add(numberGenerator.genererIndex(i, this.etape, true));
				this.toSat.add(allInt);
			}
			else if (bitExp.getNegative().get(i)) {
				allInt.add(numberGenerator.genererIndex(i, this.etape, false));
				this.toSat.add(allInt);
			}
		}	
	}
	
	private void genererInit(BitExp bitExp) {
		ArrayList<Integer> allInt;
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			allInt.add(numberGenerator.genererIndex(i, this.etape, bitExp.getPositive().get(i)));
			this.toSat.add(allInt);
		}	
	}
}