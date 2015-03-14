package primer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import seq.AnnotatedSequence;
import seq.Orientation;
import seq.SequenceRange;
import sequtil.NucleotideUtil;
import melting.CalcTm;
import melting.CalcTmSanta98;
import melting.TmException;

/**
 * 
 * Find one primer within region
 * 
 * @author Johan Henriksson
 *
 */
public class PrimerFinder
	{
	private HashMap<String,Integer> hasKmers=new HashMap<String,Integer>();

	public int minlength=22;
	public int maxlength=30;
	public double optimalTm=70;
	int kmerlen=6;

	public LinkedList<PrimerInfo> primerCandidates=new LinkedList<PrimerInfo>();

	/**
	 * Count one Kmer
	 */
	private void addKmer(String s, int k)
		{
		for(int i=0;i<s.length()-k;i++) //TODO handle wrapping as well(?). or do this on a higher level?
			{
			String kmer=s.substring(i,i+k);
			hasKmers.put(kmer,getKmerCount(kmer)+1);
			}
		}
	
	
	/**
	 * Compare best hit to second best hit
	 */
	private double countSecondBestMatch(AnnotatedSequence seq, String s)
		{
		PrimerFitter fitter=new PrimerFitter();
		fitter.scorePerfectLength=0;
		fitter.run(seq, "", s);

		if(fitter.candidates.size()<2)
			return 0;
		else
			{
			double score1=fitter.candidates.get(0).score;
			double score2=fitter.candidates.get(1).score;
			return score2/score1;
			}
		}
	
	

	public static class PrimerInfo
		{
		public Primer p;
		public double score;
		public double tm;
		}
	
	/**
	 * Perform search
	 */
	public void run(AnnotatedSequence seq, SequenceRange region)
		{
		primerCandidates.clear();
		String base=seq.getSequence();
		String baseLower=seq.getSequence();
		CalcTm tmc=new CalcTmSanta98();
		
		String within=seq.getSequence(region);
		
		addKmer(base, kmerlen);
		addKmer(baseLower, kmerlen);
		
		
		for(int toPos=kmerlen;toPos<within.length();toPos++)
			{
			//Don't bother trying this position if the first K letters are not unique
			if(getKmerCount(within.substring(toPos-kmerlen,toPos))<6)
				{
				for(int fromPos=Math.max(toPos-maxlength,0);fromPos<toPos-minlength;fromPos++)
					{
					try
						{
						String pseq=within.substring(fromPos,toPos);
						double tm=tmc.calcTm(pseq, NucleotideUtil.complement(pseq));
						if(tm>50+10 && tm<70+10)
							{
							Primer p=new Primer();
							p.name=""+Math.random();
							p.sequence=pseq;
							p.orientation=Orientation.FORWARD;
							p.targetPosition=seq.normalizePos(toPos+region.from);
							
							PrimerInfo pi=new PrimerInfo();
							pi.p=p;
							pi.score=countSecondBestMatch(seq, pseq)/*+ Math.abs(tm-optimalTm)/20*/;
							pi.tm=tm;
							if(pi.score<0.8)
								primerCandidates.add(pi);							
							else
								System.out.println(pi.score);
							}
						}
					catch (TmException e)
						{
						System.out.println(e.getMessage());
//						e.printStackTrace();
						}
					
					}
				
				}
			}

		//Sort list of candidates
		Collections.sort(primerCandidates, new Comparator<PrimerInfo>()
			{
			public int compare(PrimerInfo o1, PrimerInfo o2)
				{
				return Double.compare(o1.score, o2.score);
				}
			});
		
		hasKmers.clear();
		}
	
	/**
	 * Get count of kmer
	 */
	private int getKmerCount(String kmer)
		{
		Integer s=hasKmers.get(kmer);
		if(s==null)
			return 0;
		else
			return s;
		}
	

	public static void main(String[] args)
		{
		int searchFrom=3912;
		int searchTo=4050;
		AnnotatedSequence seq=new AnnotatedSequence();
		seq.setSequence("MGCTRGGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTCCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTGAAGCCAGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGAGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCAAGCTAGAGTTTAAACTTGACAGATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAAAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCAGAAGATCACTTGGGTGCGCGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTCCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGATGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAATACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACTATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGAAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAACTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCACTTCCGGCTGGCTGGTTTATTGCTGATAAATCAGGAGCCGGTGAGCGTGGGTCACGCGGTATCATTGCAGCACTGGGGCCGGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACTACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAAGGATAAATTTCTGGTAAGGAGGACACGTATGGAAGTGGGCAAGTTGGGGAAGCCGTATCCGTTGCTGAATCTGGCATATGTGGGAGTATAAGACGCGCAGCGTCGCATCAGGCATTTTTTTCTGCGCCAATGCAAAAAGGCCATCCGTCAGGATGGCCTTTCGGCATAACTAGGACTAGTCATCTTTTTTTAAGCTCAAGTTTTGAAAGACCCCACCTGTAGGTTTGGCAAGCTAGCTTAAGTAACGCCATTTTGCAAGGCATGGAAAATACATAACTGAGAATAGAGAAGTTCAGATCAAGGTTAGGAACAGAGAGACAGCAGAATATGGGCCAAACAGGATATCTGTGGTAAGCAGTTCCTGCCCCGCTCAGGGCCAAGAACAGTTGGAACAGGAGAATATGGGCCAAACAGGATATCTGTGGTAAGCAGTTCCTGCCCCGGCTCAGGGCCAAGAACAGATGGTCCCCAGATGCGGTCCCGCCCTCAGCAGTTTCTAGAGAACCATCAGATGTTTCCAGGGTGCCCCAAGGACCTGAAATGACCCTGTGCCTTATTTGAACTAACCAATCAGTTCGCTTCTCGCTTCTGTTCGCGCGCTTCTGCTCCCCGAGCTCAATAAAAGAGCCCACAACCCCTCACTCGGCGCGCCAGTCCTCCGATAGACTGCGTCGCCCGGGTACCCGTGTTCTCAATAAACCCTCTTGCAGTTGCATCCGACTCGTGGTCTCGCTGTTCCTTGGGAGGGTCTCCTCTGAGTGATTGACTACCCGTCAGCGGGGTCTTTCATTTGGAGGTTCCACCGAGATTTGGAGACCCCTGCCCAGGGACCACCGACCCCCCCGCCGGGAGGTAAGCTGGCCAGCAACTTATCTGTGTCTGTCCGATTGTCTAGTGTCTATGACTGATTTTATGCGCCTGCGTCGGTACTAGTTAGCTAACTAGCTCTGTATCTGGCGGACCCGTGGTGGAACTGACGAGTTCGGAACACCCGGCCGCAACCCTGGGAGACGTCCCAGGGACTTCGGGGGCCGTTTTTGTGGCCCGACCTGAGTCCTAAAATCCCGATCGTTTAGGACTCTTTGGTGCACCCCCCTTAGAGGAGGGATATGTGGTTCTGGTAGGAGACGAGAACCTAAAACAGTTCCCGCCTCCGTCTGAATTTTTGCTTTCGGTTTGGGACCGAAGCCGCGCCGCGCGTCTTGTCTGCTGCAGCATCGTTCTGTGTTGTCTCTGTCTGACTGTGTTTCTGTATTTGTCTGAAAATATGGGCCCGGGCTAGACTGTTACCACTCCCTTAAGTTTGACCTTAGGTCACTGGAAAGATGTCGAGCGGATCGCTCACAACCAGTCGGTAGATGTCAAGAAGAGACGTTGGGTTACCTTCTGCTCTGCAGAATGGCCAACCTTTAACGTCGGATGGCCGCGAGACGGCACCTTTAACCGAGACCTCATCACCCAGGTTAAGATCAAGGTCTTTTCACCTGGCCCGCATGGACACCCAGACCAGGTCCCCTACATCGTGACCTGGGAAGCCTTGGCTTTTGACCCCCCTCCCTGGGTCAAGCCCTTTGTACACCCTAAGCCTCCGCCTCCTCTTCCTCCATCCGCCCCGTCTCTCCCCCTTGAACCTCCTCGTTCGACCCCGCCTCGATCCTCCCTTTATCCAGCCCTCACTCCTTCTCTAGGCGCCCCCATATGGCCATATGAGATCTTATATGGGGCACCCCCGCCCCTTGTAAACTTCCCTGACCCTGACATGACAAGAGTTACTAACAGCCCCTCTCTCCAAGCTCACTTACAGGCTCTCTACTTAGTCCAGCACGAAGTCTGGAGACCTCTGGCGGCAGCCTACCAAGAACAACTGGACCGACCGGTGGTACCTCACCCTTACCGAGTCGGCGACACAGTGTGGGTCCGCCGACACCAGACTAAGAACCTAGAACCTCGCTGGAAAGGACCTTACACAGTCCTGCTGACCACCCCCACCGCCCTCAAAGTAGACGGCATCGCAGCTTGGATACACGCCGCCCACGTGAAGGCTGCCGACCCCGGGGGTGGACAAGGCTGCCGACCCGGGGGTGGACATCCTCTAGACTGAAGCTATAGAAGCTTCTCGAGSAATTCAGTACTTACGTAGCGGCCGCGGCTGACTACAAGGACGACGATGACAAGTAGTTGGCCGCCCCTCTCCCTCCCCCCCCCCCTAACGTTACTGGCCGAAGCCGYTTGGAATAAGGCCGGTGTGCGTTTGTCTATATGTTATTTTCCACCATATTGCCGTCTTTTGGCAATGTGAGGGCCCGGAAACCTGGCCCTATCTTCTTGATGAGCATTCCTAGGGGTCTTTCCCCTCTCGCCAAAGGAATGCAAGGTCTGTTGAATGTCGTGAAGGAAGCAGTTCCTCTGGAAGTTTCTTGAAGATAAACAACGTCTGTAGCAACCCTTTGCAGGCAGCGGAACCCCCCACCTGGCGACAGGTGCCTCTGCGGCCAAAAGCCACGTGTATAAGATACACCTGTAAAGGCGGCACAACCCCAGTGCCACGTTGTGAGTTGGGTAGTTGTGGAAAGAGTCAAATGGCTCTCCTCAAGCGTATTCAACAAGGGGCTGAAGGATGCCCAGAAGGTACCCCATTGTATGGGATCTGATCTGGGGCCTCGGTGCATATGCTTTACATATGTTTAGTCGAGGTTAAAAAACGTCTAGGCCCCCCGAACCACGGGGACGTGGTTTTCCTTTGAAAAACACGATGATAATATGGCCACAACCATGCAGCTTGCCAGCATGGGCTACCTGCGCCGCATGGTGAGCAAGGGCGAGGAGCTGTTCACCGGGGTGGTGCCCATCCTGGTCGAGCTGGACGGCGACGTAAACGGCCACAAGTTCAGCGTGTCCGGCGAGGGCGAGGGCGATGCCACCTACGGCAAGCTGACCCTGAAGTTCATCTGCACCACCGGCAAGCTGCCCGTGCCCTGGCCCACCCTCGTGACCACCYTSACCTACGGCGTGCAGTGCTTCAGCCGCTACCCCGACCACATGAAGCAGCACGACTTCTTCAAGTCCGCCATGCCCGAAGGCTACGTCCAGGAGCGCACCATCTTCTTCAAGGACGACGGCAACTACAAGACCCGCGCCSARGTGRARTTCRAGGGCACACCCTGGTGAACCGCATCGAGCTGAAGGGCATCGACTTCAAGGAGGACGGCAACATCCTGGGGCACAAGCTGGAGTACAACTACAACAGCCACAACGTCTATATCATGGCCGACAAGCAGAAGAACGGCATCAAGGTGAACTTCAAGATCCGCCACAACATCGAGGACGGCAGCGTGCAGCTCGCCGACCACTACCAGCAGAACACCCCCATCGGCGACGGCCCCGTGCTGCTGCCCGACAACCACTACCTGAGCACCCAGTCCGCCCTGAGCAAAGACCCCAACGAGAAGCGCGATCACATGGTCCTGCTGGAGTTCGTGACCGCCGCCGGGATCACTCTCGGCATGGACGAGCTGTACAAGTAAGCGCCGTAGGCAGGTAGTTAACAGATCCGGATTAGTCCAATTTGTTAAAGACAGGATATCAGTGGTCCAGGCTCTAGTTTTGACTCAACAATATCACCAGCTGAAGCCTATAGAGTACGAGCCATAGATAAAATAAAAGATTTTATTTAGTCTCCAGAAAAAGGGGGGAATGAAAGACCCCACCTGTAGGTTTGGCAAGCTAGCTTAAGTAACGCCATTTTGCAAGGCATGGAAAATACATAACTGAGAATAGAGAAGTTCAGATCAAGGTTAGGAACAGAGAGACAGCAGAATATGGGCCAAACAGGATATCTGTGGTAAGCAGTTCCTGCCCCGCTCAGGGCCAAGAACAGTTGGAACAGGAGAATATGGGCCAAACAGGATATCTGTGGTAAGCAGTTCCTGCCCCGGCTCAGGGCCAAGAACAGATGGTCCCCAGATGCGGTCCCGCCCTCAGCAGTTTCTAGAGAACCATCAGATGTTTCCAGGGTGCCCCAAGGACCTGAAATGACCCTGTGCCTTATTTGAACTAACCAATCAGTTCGCTTCTCGCTTCTGTTCGCGCGCTTCTGCTCCCCGAGCTCAATAAAAGAGCCCACAACCCCTCACTCGGCGCGCCAGTCCTCCGATAGACTGCGTCGCCCGGGTACCCGTGTTCTCAATAAACCCTCTTGCAGTTGCATCCGACTCGTGGTCTCGCTGTTCCTTGGGAGGGTCTCCTCTGAGTGATTGACTACCCGTCAGCGGGGTCTTTCAGTTTCTCCCACCTACACAGGTCTCACTGGATCTGTCGACATCGATGGGCGCGGGTGTACACTCCGCCCATCCCGCCCCTAACTCCGCCCAGTTCCGCCCATTCTCCGCCTCATGGCTGACTAATTTTTTTTATTTATGCAGAGGCCGAGGCCGCCTCGGCCTCTGAGCTATTCCAGAAGTAGTGAGGAGGCTTTTTTGGAGGCCTAGGCTTTTGCAAAAAGCTAATTC");

		PrimerFinder f=new PrimerFinder();
		f.run(seq, new SequenceRange(searchFrom, searchTo));
		
		
		System.out.println("----------------");
		for(PrimerInfo pi:f.primerCandidates)
			{
			System.out.println(pi.score+"\t"+pi.p.sequence);
			}

		}
	
	}
