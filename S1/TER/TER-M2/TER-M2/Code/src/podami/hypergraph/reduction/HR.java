package podami.hypergraph.reduction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
//import java.util.Collections;

import podami.common.CoupleBitSet;
//import podami.common.CompareCoupleBitSet;



/**
 * <p>Reduction d'un hypergraphe</p>
 * @author Nicolas Durand <nicolas.durand@univ-amu.fr><br>https://pageperso.lis-lab.fr/nicolas.durand/<br>
 * LSIS CNRS UMR 7296<br>Universite d'Aix-Marseille (AMU)
 * @version 1.0
 */
public class HR {

	/**
	 * Nom du fichier contenant les donnees (l'hypergraphe)
	 */
	public String fichierDonnees;

	/**
	 * L'hypergraphe (ensemble d'hyperaretes)
	 */
	public ArrayList<CoupleBitSet> hyper;

	/**
	 * Nombre de sommets de l'hypergraphe
	 */
	public int nbSommets;

	/**
	 * Nombre d'hyperaretes de l'hypergraphe
	 */
	public int nbHyperaretes;

	/**
	 * L'hypergraphe reduit (ensemble d'hyperaretes)
	 */
	public ArrayList<CoupleBitSet> hyperReduit;

	/**
	 * Nombre de sommets de l'hypergraphe reduit
	 */
	public int nbSommetsReduc;

	/**
	 * Nombre d'hyperaretes de l'hypergraphe reduit
	 */
	public int nbHyperaretesReduc;

	/**
	 * Table des occurrences des sommets dans l'ensembles des hyperaretes
	 */
	private HashMap<Integer, Integer> occurrences;

	/**
	 * Graphe des hyperaretes
	 */
	private Graphe graphe;

	/**
	 * 
	 */
	private boolean affichage;

	/**
	 * 
	 */
	private boolean affichage2;

	private boolean calculMin;
	

	/**
	 * Constructeur par defaut
	 */
	public HR() {
		this.fichierDonnees = "";
		this.nbSommets = 0;
		this.occurrences = new HashMap<Integer, Integer>();
		this.hyper = new ArrayList<CoupleBitSet>();
		this.hyperReduit = new ArrayList<CoupleBitSet>();
		this.affichage = false;
		//this.affichage = true;
		this.affichage2 = false;
		//this.affichage2 = true;
		this.calculMin = false;
	}


	/**
	 * Constructeur
	 * @param nomFichier Nom du fichier contenant les donnees (i.e. l'hypergraphe)
	 */
	public HR(String nomFichier, boolean calculMin) {
		this.fichierDonnees = nomFichier;
		this.nbSommets = 0;
		this.occurrences = new HashMap<Integer, Integer>();
		this.hyper = new ArrayList<CoupleBitSet>();
		this.hyperReduit = new ArrayList<CoupleBitSet>();
		this.affichage = false;
		this.affichage2 = false;
		this.calculMin = calculMin;
	}



	/**
	 * Reduction de l'hypergraphe
	 */
	public void computeReduction() {
		BufferedReader br;
		StringTokenizer st;
		String ligne = "";
		String sommet = ""; // numero du sommet lu
		int p = 0; // numero de l'hyperarete lu
		int numsommet;

		try {
			if(this.fichierDonnees.equals("") == true) {
				//compter le nombre de sommets de l'hypergraphe
				BitSet b = new BitSet();
				for(CoupleBitSet cb : this.hyper) {
					b.or(cb.getVertexes());
				}
				this.nbSommets = b.cardinality();
				this.nbHyperaretes = this.hyper.size();
			}
			else {
				br = new BufferedReader(new FileReader(this.fichierDonnees));
				p = 0;
				CoupleBitSet hyperarete = null;

				while(br.ready())
				{
					p++; // numero hyperarete (= numero de la ligne lue)
					sommet = "";
					ligne = br.readLine();
					st = new StringTokenizer(ligne, " \t\n\r");
					hyperarete = new CoupleBitSet(); // l'hyperarete en cours de lecture
					while(st.hasMoreTokens())
					{
						sommet = st.nextToken();
						numsommet = Integer.parseInt(sommet);
						if(numsommet > this.nbSommets) this.nbSommets = numsommet;
						hyperarete.addVertex(Integer.parseInt(sommet));
					}
					hyper.add(hyperarete);
				}
				br.close();

				this.nbHyperaretes = p;
			}
			

			//TODO tri des hyperaretes ?
			// (pour ne pas etre sensible a l'ordre des hyperaretes lors de 
			// la selection des aretes si egalite de poids max)
			//Collections.sort(this.hyper, new CompareCoupleBitSet());
			
			
			if(affichage) System.out.print("\n");

			if(affichage) System.out.println("Hypergraphe =\n"+this.nbSommets+" sommets ; "+this.nbHyperaretes+" hyperaretes");
			if(affichage2) System.out.println("Ensemble des hyperaretes : ");
			if(affichage2) CoupleBitSet.afficheListCoupleBitSetVertex(hyper);

						
			if(affichage) System.out.println("\nOccurrences des sommets dans les hyperaretes : ");
			CoupleBitSet cbs = null;
			BitSet bs = null;
			int m = 0;
			int index = 0;
			Integer numero = null;
			for(Iterator<CoupleBitSet> it=hyper.iterator(); it.hasNext();)
			{
				cbs = (CoupleBitSet)it.next();
				bs = cbs.getVertexes();
				m = 0;
				index = -1;
				while(m <= bs.size())
				{
					index = bs.nextSetBit(m);
					if(index == -1) break;
					numero = new Integer(index);
					if(occurrences.containsKey(numero))
						occurrences.put(numero, occurrences.get(numero)+1);
					else occurrences.put(numero, 1);
					m = index + 1;
				}
			}

			if(affichage2) System.out.println(occurrences.toString());
			
			this.graphe = new Graphe(this.nbHyperaretes);
					
			
			if(affichage) System.out.println("\nCalcul des intersections et des poids des aretes");
			cbs = null;
			CoupleBitSet cbs2 =null;
			BitSet inter = null;
			float poids = -1;
			boolean aucuneintersection = true;

			for(int i=1; i<=this.hyper.size(); i++)
			{
				cbs = this.hyper.get(i-1);
				for(int j=i+1; j<=this.hyper.size(); j++)
				{
					cbs2 = this.hyper.get(j-1);
					if(affichage2) {System.out.print("cbs= "); cbs.displayVertexSet(null); System.out.print("\n");}
					if(affichage2) {System.out.print("cbs2= "); cbs2.displayVertexSet(null); System.out.print("\n");}
					inter = cbs.intersectionVertexSet(cbs2);

					//poids = somme des occurrences des sommets presents dans l'intersection
					poids = 0;
					int l = 1;
					index = 0;
					while(l <= inter.size())
					{
						index = inter.nextSetBit(l);
						if(index == -1) break;
						poids = poids + this.occurrences.get(new Integer(index)).intValue();
						l = index + 1;
						aucuneintersection = false;
					}

					if(poids > 0) this.graphe.addEdge(i, j, poids);

					if(affichage2) System.out.println("inter= "+inter+" ("+poids+")");
					if(affichage2) System.out.print("\n");
				}
			}

			
			if(affichage2) {
				System.out.println("\nGraphe des hyperaretes :");
				System.out.println(this.graphe.toString());
			}


			// algorithme glouton de selection d'aretes du graphe (des ei)
			// jusqu'a epuisement des aretes
			// si arete ei-ej selectionnee alors on eneleve les aretes ek-el ou k=i ou k=j et l dans {1,...}

			float valmax = 0;
			int imax = 0;
			int jmax = 0;

			// boucle tant que graphe pas vide
			while(!graphevide())
			{

				//recherche valeur max
				valmax = 0;
				imax = 0;
				jmax = 0;

				Arete max = this.graphe.valMax();
				valmax = max.valeur;
				imax = max.sommet1;
				jmax = max.sommet2;

				if(affichage2) System.out.println("\nvalmax="+valmax+" i="+imax+" j="+jmax);

				cbs = this.hyper.get(imax-1);
				cbs2 = this.hyper.get(jmax-1);
				inter = cbs.intersectionVertexSet(cbs2);

				///////////////////////////////////////////////////////////////////////////////
				// pas de double ni de sur-ensemble
				if(calculMin == true) {
					CoupleBitSet itinter = new CoupleBitSet(inter);
					ArrayList<CoupleBitSet> itemSetToRemove = new ArrayList<CoupleBitSet>();
					if(okPourInsertion(hyperReduit, itinter, itemSetToRemove)) {
						hyperReduit.add(itinter);
					}
					//pour le min
					for(CoupleBitSet ii : itemSetToRemove) {
						hyperReduit.remove(ii);
					}
				}
				else
				{
					CoupleBitSet itinter = new CoupleBitSet(inter);
					hyperReduit.add(itinter);
				}
				//////////////////////////////////////////////////////////////////////////////

				if(affichage2) {
					System.out.println("\nEnsemblesSupports = ");
					CoupleBitSet.afficheListCoupleBitSetVertex(hyperReduit);
				}

				//suppression des aretes reliees a ei et ej
				this.graphe.cleanGraphe(imax, jmax);

				//affichage du nouveau graphe
				if(affichage2) {
					System.out.println("\nNouveau graphe :");
					System.out.println(this.graphe.toString());
				}

			}

			if(affichage) System.out.println("\nPlus d'aretes dans le graphe.");


			if(aucuneintersection == true) {
				System.out.println("Hypergraphe non reduit car aucune intersection non vide.");
				hyperReduit = hyper;
			}

			else {
				if(affichage) {
					System.out.println("\nHypergraphe reduit :");
					CoupleBitSet.afficheListCoupleBitSetVertex(hyperReduit);
				}

				BitSet bset = new BitSet();
				bset.clear();
				for(Iterator<CoupleBitSet> i = hyperReduit.iterator(); i.hasNext();) {
					cbs = (CoupleBitSet)i.next();
					bset.or(cbs.getVertexes());
				}

				if(affichage) System.out.println("Hypergraphe reduit : "+bset.cardinality()+" sommets ; "+hyperReduit.size()+" hyperaretes");

				this.nbSommetsReduc = bset.cardinality();
				this.nbHyperaretesReduc = hyperReduit.size();
			}		

			if(affichage) System.out.println("Fin.");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}




	/**
	 * Teste si un itemset (vertexset) n'est pas dans une liste et qu'il n'est pas non plus un sur-ensemble d'un itemset de la liste
	 * @param hyperRed Une liste d'itemset
	 * @param it Un itemset
	 * @return true si tests ok
	 */
	private boolean okPourInsertion(ArrayList<CoupleBitSet> hyperRed, CoupleBitSet it, ArrayList<CoupleBitSet> itemSetToRemove) {
		for(CoupleBitSet i : hyperRed) {
			if(i.equalsVertexSet(it)) return false;
			if(i.includedVertexSet(it.getVertexes())) return false;
			if(it.includedVertexSet(i.getVertexes())) itemSetToRemove.add(i);
		}

		return true;
	}



	/**
	 * 
	 * @return
	 */
	private boolean graphevide() {
		return this.graphe.isEmpty();
	}



	/**
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public void save(BufferedWriter bw) throws IOException {
		CoupleBitSet cbs = null;
		for(Iterator<CoupleBitSet> i = this.hyperReduit.iterator(); i.hasNext();) {
			cbs = (CoupleBitSet)i.next();
			cbs.save(bw, null);
		}
	}


	/**
	 * fixe si l'hypergraphe reduit sera min ou pas
	 * @param valeur
	 */
	public void setCalculMin(boolean valeur) {
		this.calculMin = valeur;
	}
	
	/**
	 * retourne si l'hypergraphe reduit sera min ou pas
	 * @return
	 */
	public boolean getCalculMin() {
		return this.calculMin;
	}


	/**
	 * 
	 * @param nomFichier
	 */
	public static void doJob(String nomFichier, boolean calculMin) {
		long START = 0;
		long END = 0;
		HR sp = new HR(nomFichier, calculMin);

		START = System.currentTimeMillis();
		sp.computeReduction();
		END = System.currentTimeMillis();
		double tempsexecution = END - START;
		System.out.print(tempsexecution+" ms");		

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(sp.fichierDonnees+".red"));
			bw.write("# "+tempsexecution+" ms\n");
			bw.write("# hypergraphe= "+sp.nbSommets+" sommets ; "+sp.nbHyperaretes+" hyperaretes\n");
			bw.write("# hypreduit= "+sp.nbSommetsReduc+" sommets ; "+sp.nbHyperaretesReduc+" hyperaretes\n");
			sp.save(bw);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<2)
			System.out.println("Usage: HR nom_fichier_hypergraphe calculMin\n");
		else
		{	
			doJob(args[0], Boolean.parseBoolean(args[1]));
		}
	}


}
