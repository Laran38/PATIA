package fr.uga.pddl4j.tutorial.util;

/**
 * Cette factory nous permet de gerer les indices que nous passons au solver SAT
 * <p>
 * Le principe est le suivant :
 * <ol>
 * <li>Un entier etant coder sous 32 bits, nous utiliserons les 16 premiers pour
 * definir l'indice dans la liste, les 16 derniers pour definir l'etape
 * courante.</li>
 * <li>Un booleen est necessaire afin de verifier si la variable est positive ou
 * non.</li>
 * </ol>
 * L'avantage de cette methode, est qu'elle permet de recuperer et d'initialiser
 * des valeurs en O(1).
 * <p>
 * Cela est tres utile, surtout dans des problemes necessitant de creer beaucoup
 * de variable
 */
public class IndexFactory {

	public Integer generateIndex(int index, Integer etape, boolean c) {
		int toRet = ((index + 1) << 16) + etape;
		return c ? toRet : toRet * -1;
	}

	public int getEtape(int valeur) {
		return valeur % (1 << 16);
	}

	public int getIndex(int valeur) {
		return valeur / (1 << 16) - 1;
	}
}
