package fr.uga.pddl4j.tutorial.asp;


public class IndexFactory {
	public final static int TAILLE = 1 << 16;
	
	public int genererIndex(Integer index, Integer etape, boolean c) {
		int toRet = ((index + 1) << 16) + etape;
		return c ? toRet : toRet * -1;
	}
	
	public int getEtape(int valeur) {
		return valeur% (1<<16);
	}
	
	public int getIndex(int valeur) {
		return valeur / (1<<16);
	}
	
}
