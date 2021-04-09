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

public class SATSearch extends ParserPlanner {
	
	private static final long serialVersionUID = 1L;
	private static final int TIMEOUT = 300;
	private final int MIN_STEP;
	private final int numberOfFact;
	private final List<BitOp> operators;
	private final List<IntExp> revelantFacts;
	private final IndexFactory numberGenerator;
	private ArrayList<ArrayList<Integer>> clauses;
	private Integer etape;
	private long time;
	private long timeToEncode = 0;
	
	public SATSearch(String[] args) {
		super(args);
		this.numberGenerator = new IndexFactory();		
		this.operators = pb.getOperators();
		this.revelantFacts = pb.getRelevantFacts();
		this.etape = 0;

		PATH += "test.txt";

		this.clauses = new ArrayList<>();
		this.numberOfFact = revelantFacts.size();
		final Heuristic heuristic = HeuristicToolKit.createHeuristic(Heuristic.Type.FAST_FORWARD, pb);
		final BitState init = new BitState(pb.getInit());
		this.MIN_STEP = heuristic.estimate(init, pb.getGoal());
	}
	
	
	/* 
	 * Affichage du resultat sur la sortie standart
	 */
	
	private void display(String res) {
		//Decoupage du resultat donner par le sat Solver
		String[] rest = res.split(" ");
		//Tableau associant une action et son etape, cela permettra d'avoir le bon ordre
		String[] resString = new String[this.etape + 1]; 
		for (String courant : rest) {
			//Recuperation des valeurs necessaires grace a la factory
			int value = Integer.parseInt(courant);
			int index = Math.abs(numberGenerator.getIndex(value));
			int etape = Math.abs(numberGenerator.getEtape(value));
			//Si l'index est une action positive, alors elle doit etre faite, et donc etre affichee
			if (index > this.numberOfFact && value > 0) {
				//Reduction de l'indice, afin que ce dernier corresponde a l'indice dans la liste des operations
				index -= this.numberOfFact;
				//recuperation de l'action a faire
				BitOp operation = operators.get(index - 1);
				resString[etape] = "\t" + operation.getName() + " ";
				//On associe l'action et ses variables a son etape
				for (int op : operation.getInstantiations())
					resString[etape] += pb.getConstants().get(op) + " ";
			}
		}
		//Parcours du tableau et affichage
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
	
	/*
	 * Fait en sorte qu'une action soit une unique a une certaine etape
	 */
	private void genererDisjonction() {
		ArrayList<Integer> toAdd;
		//Parcours 
		for (int i = 0; i < operators.size(); i++) {
			for (int j = i + 1; j <= operators.size(); j++) {
				//Creation d'une nouvelle clause
				toAdd = new ArrayList<>();
				//Ajout de -i
				toAdd.add(numberGenerator.generateIndex(i + numberOfFact, etape, false));
				//Ajout de -j
				toAdd.add(numberGenerator.generateIndex(j + numberOfFact, etape, false));
				//Ajout dans le solveur sat la clause cree
				clauses.add(toAdd);
			}
		}		
	}

	/*
	 * Generation des transitions entre les predicats courant et les futures actions possible
	 */
	private void genererTransition() {
		//Parcours de tous les predicats
		for(int i = 0; i < revelantFacts.size(); i++) {
			ArrayList<Integer> indexPos = new ArrayList<>();
			ArrayList<Integer> indexNeg = new ArrayList<>();
			//Decalage par apport au fact
			int index = this.numberOfFact;
			//Parcours de toutes les actions 
			for(BitOp ai : operators) {
				BitExp effect = ai.getCondEffects().get(0).getEffects();
				//Si le predicat courant est une condition positive, on creer son indice et on l'ajoute 
				if (effect.getPositive().get(i))
					indexPos.add(numberGenerator.generateIndex(index, etape, true));
				//Si le predicat courant est une condition negative, on creer son indice et on l'ajoute 
				if (effect.getNegative().get(i))
					indexNeg.add(numberGenerator.generateIndex(index, etape, true));
				index ++;
			}
			int i1 = numberGenerator.generateIndex(i, etape, true);
			int i2 = numberGenerator.generateIndex(i, etape + 1, true);
			//Transformation des transitions crees, et ajout dans le sat
			implicationTransition(i1 * -1, i2 * - 1, indexPos);
			implicationTransition(i1, i2, indexNeg);
		}
		
	}

	/*
	 * Resoud les implications de la forme v1 => v2
	 * Il suffit d'ajouter -v1 V v2
	 */
	private void implicationTransition(int f1, int f2, ArrayList<Integer> index) {
		index.add(f1 * -1);
		index.add(f2);
		clauses.add(index);
	}

	private void genererActions() {
		int posCourante = revelantFacts.size();
		for(BitOp ai : operators) {
			ArrayList<Integer> toAdd = new ArrayList<>();
			int indiceAction = numberGenerator.generateIndex(posCourante, this.etape, true);
			for (int i = 0; i < revelantFacts.size(); i++) {
				//On recupere les predicats et on les ajoutes selon si ils sont positif ou non
				BitExp precondition = ai.getPreconditions();
				if (precondition.getPositive().get(i))
					toAdd.add(numberGenerator.generateIndex(i, this.etape, true));
				if (precondition.getNegative().get(i))
					toAdd.add(numberGenerator.generateIndex(i, this.etape, false));
				//On recupere les Effets et on les ajoutes selon si ils sont positif ou non
				BitExp effect = ai.getCondEffects().get(0).getEffects();
				if (effect.getPositive().get(i))
					toAdd.add(numberGenerator.generateIndex(i, this.etape + 1, true));
				if (effect.getNegative().get(i))
					toAdd.add(numberGenerator.generateIndex(i, this.etape + 1, false));
			}
			//On resoud l'implication
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
		//Parcours des revelantFacts
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			//Si l'instance est positive alors on ajoute le predicat
			if (bitExp.getPositive().get(i)) {
				allInt.add(numberGenerator.generateIndex(i, this.etape, true));
				this.clauses.add(allInt);
			}
			//Si il est negatif on a joute la negation du predicat
			else if (bitExp.getNegative().get(i)) {
				allInt.add(numberGenerator.generateIndex(i, this.etape, false));
				this.clauses.add(allInt);
			}
			//On ne fait rien sinon
		}	
	}
	
	private void genererInit(BitExp bitExp) {
		ArrayList<Integer> allInt;
		//Parcours des predicats, ajout du predicat si il fait partie de l'initialisation positive
		//On ajoute son negatif sinon
		for (int i = 0; i < revelantFacts.size(); i++) {
			allInt = new ArrayList<>();
			allInt.add(numberGenerator.generateIndex(i, this.etape, bitExp.getPositive().get(i)));
			this.clauses.add(allInt);
		}	
	}

	/*
	 * Recherche de solution au probleme donne, programme principal
	 */
	@Override
	public void search() {
		//Si on est a la premiere etape alors ajoute les predicats initiaux
		if(this.etape == 0) {
			timeToEncode = System.currentTimeMillis();
			genererInit(pb.getInit());
		}
		//On genere en suite les actions et les predicats qu'elle impliquent a l'etape K
		genererActions();
		//On fait en sorte de faire les transitions entre les actions a l'etape K et l'etape K+1
		genererTransition();
		//On fait en sorte qu'a l'etape K, une et une seule action est possible
		genererDisjonction();
		//Une fois tout cela fait, on peut passer a l'etape suivante
		this.etape++;
		//Si on a pas depasser le seuil de l'heuristique alors on recommence sans passer par le solveur SAT
		if(this.MIN_STEP > this.etape)
			this.search();
		else {
			//Sinon on Genere les clauses pour les objectifs
			//On doit avant tout sauvegarder ceux qui ont ete ajoute, afin de pouvoir les enelever si le probleme n est pas satisfaible
			int save = this.clauses.size();
			genererGoal(pb.getGoal());
			//On met a jour le chronometre
			super.getStatistics().setTimeToEncode(System.currentTimeMillis() - timeToEncode);
			time= System.currentTimeMillis();

			String res;
			try {
				res = solverSat();
			} catch (Exception e) {
				//Si le timeout a ete depasser on set le temps pour solve a -1 et on arrete
				super.getStatistics().setTimeToSearch(-1);
				return;
			}
			super.getStatistics().setTimeToSearch(System.currentTimeMillis() - time);
			//Sinon si il est insatisfaisable on supprime les clauses finales et on recommence
			if(res == "") {
				
				for(int i = this.clauses.size() - 1; i >= save; i--)
					this.clauses.remove(i);
				this.search();
			}
			else
				//Sinon on affiche le resultat et on sort
				display(res);
		}
	}
	
	//Recherche par CodedProblem, obligatiore par la classe mere, nous ne l'utilisons pas
	@Override
	public Plan search(CodedProblem arg) {
		this.pb = arg;
		this.search();
		return null;
	}


	public CodedProblem getPB() {
		return this.pb;
	}

}
