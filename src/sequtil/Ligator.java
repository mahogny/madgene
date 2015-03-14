package sequtil;

import java.util.HashMap;
import java.util.LinkedList;

import seq.AnnotatedSequence;
import seq.Orientation;
import seq.SequenceRange;

/**
 * 
 * From a set of sequences, figure out how they can be ligated
 * 
 * @author Johan Henriksson
 *
 */
public class Ligator
	{

	public LinkedList<AnnotatedSequence> sequences=new LinkedList<AnnotatedSequence>();
	
	
	public HashMap<Integer, HashMap<Integer, Orientation>> canLigate=new HashMap<Integer, HashMap<Integer,Orientation>>();

	/**
	 * Check how fragments can ligate: Either A-B, or A-rot(B)
	 */
	public void calculateCanLigate()
		{
		for(int i=0;i<sequences.size();i++)
			for(int j=0;j<sequences.size();j++)
				{
				AnnotatedSequence seqA=sequences.get(i);
				AnnotatedSequence seqB=sequences.get(j);
			
				boolean checkAtoB=false, checkAtoRotB=false;
				
//				System.out.println(i+"\t"+j+"\t"+isBluntBegin(seqA)+"\t"+isBluntEnd(seqA));
				if(isBluntEnd(seqA))
					{
					if(isBluntBegin(seqB))
						checkAtoB=true;
					if(isBluntEnd(seqB))
						checkAtoRotB=true;
					//Might want more info here? note that we only story information about piece A to piece B
					}
				else
					{
					//Check if forward is fine
					AnnotatedSequence stickyEndA=extractStickyEnd(seqA);
					AnnotatedSequence stickyBeginB=extractStickyBegin(seqB);
					checkAtoB=isComplementary(stickyEndA, stickyBeginB);
					
					//Check if rotated is fine
					AnnotatedSequence stickyBeginRotB=extractStickyEnd(seqB);
					stickyBeginRotB.reverseSequence();
					checkAtoRotB=isComplementary(stickyEndA, stickyBeginRotB);
					}

				setCanLigate(i, j, toOrient(checkAtoB, checkAtoRotB));
				}
		}

	
	/**
	 * 
	 */
	private boolean isComplementary(AnnotatedSequence stickyA, AnnotatedSequence stickyB)
		{
		if(stickyA.getLength()!=stickyB.getLength())
			return false;
		else if(stickyA.getLength()==0)
			return true;
		else
			{
			//Figure out which strand to compare
			String seqA, seqB;
			seqA=stickyA.getSequence();
			if(seqA.charAt(0)!=' ')
				seqB=NucleotideUtil.complement(stickyB.getSequenceLower());
			else
				{
				seqA=stickyA.getSequenceLower();
				seqB=NucleotideUtil.complement(stickyB.getSequence());
				}
			
			//Compare all characters
			for(int i=0;i<seqA.length();i++)
				if(seqA.charAt(i)!=seqB.charAt(i))
					return false;
			return true;
			}
		}
	
	/**
	 * Extract orientation from 2 checks
	 */
	private Orientation toOrient(boolean fwd, boolean rev)
		{
		if(fwd)
			{
			if(rev)
				return Orientation.NOTORIENTED;
			else
				return Orientation.FORWARD;
			}
		else
			{
			if(rev)
				return Orientation.REVERSE;
			else
				return null;
			}
		}
	
	/**
	 * Extract the sticky beginning part of a sequence
	 */
	private AnnotatedSequence extractStickyBegin(AnnotatedSequence seq)
		{
		AnnotatedSequence sticky=new AnnotatedSequence();
		String upr=seq.getSequence();
		String lwr=seq.getSequenceLower();
		int i=0;
		for(;i<seq.getLength();i++)
			if(upr.charAt(i)!=' ' && lwr.charAt(i)!=' ')
				break;
		System.out.println(i);
		SequenceRange r=new SequenceRange(0,i);
		sticky.setSequence(seq.getSequence(r), seq.getSequenceLower(r));
		return sticky;
		}
	
	/**
	 * Extract the sticky end part of a sequence
	 */
	private AnnotatedSequence extractStickyEnd(AnnotatedSequence seq)
		{
		AnnotatedSequence sticky=new AnnotatedSequence();
		String upr=seq.getSequence();
		String lowr=seq.getSequenceLower();
		int i=seq.getLength()-1;
		for(;i>=0;i--)
			if(upr.charAt(i)!=' ' && lowr.charAt(i)!=' ')
				{
				i++;
				break;
				}
		SequenceRange r=new SequenceRange(i,0);
		sticky.setSequence(seq.getSequence(r), seq.getSequenceLower(r));
		return sticky;
		}
	
	/**
	 * Check if a sequence is blunt-ended, at end
	 */
	private boolean isBluntEnd(AnnotatedSequence seq)
		{
		int length=seq.getLength();
		SequenceRange r=new SequenceRange(length-1, 0);
//		System.out.println("#"+seq.getSequence(r)+"#");
		return !(seq.getSequence(r).equals(" ") || seq.getSequenceLower(r).equals(" "));
		}
	
	
	/**
	 * Check if a sequence is blunt-ended, at beginning
	 */
	private boolean isBluntBegin(AnnotatedSequence seq)
		{
		SequenceRange r=new SequenceRange(0,1);
		System.out.println("#"+seq.getSequence(r)+"#");
		return !(seq.getSequence(r).equals(" ") || seq.getSequenceLower(r).equals(" "));
		}

	
	/**
	 * Set status of ligate:ability for fragment i to j
	 */
	public void setCanLigate(int i, int j, Orientation o)
		{
		HashMap<Integer, Orientation> m=canLigate.get(i);
		if(m==null)
			canLigate.put(i,m=new HashMap<Integer, Orientation>());
		m.put(j, o);
		}
	
	
	/**
	 * Self-ligate plasmid to make it circular
	 */
	public static void selfCircularize(AnnotatedSequence seq)
		{
		//TODO: check if self-ligation is valid?
		
		//Fill in the first gap. This way, no annotation need be moved
		String upr=seq.getSequence();
		String lwr=seq.getSequenceLower();
		int len=upr.length();
		if(upr.charAt(0)==' ')
			{
			//Lower overhang
			int i=0;
			for(;;i++)
				if(upr.charAt(i)!=' ')
					break;

			upr=upr.substring(len-i) + upr.substring(i);
			lwr=lwr.substring(0, len-1);
			}
		else
			{
			//Upper overhang
			int i=0;
			for(;;i++)
				if(lwr.charAt(i)!=' ')
					break;

			upr=upr.substring(0, len-1);
			lwr=lwr.substring(len-i) + lwr.substring(i);
			}
		
		//TODO may want to normalize position of annotation. likely not needed
		}
	
	
	public static void main(String[] args)
		{
		Ligator lig=new Ligator();
		
		AnnotatedSequence seqA=new AnnotatedSequence();
		seqA.setSequence(
				"aaatttggg   ",
				"   tttgggttt");
		lig.addSequence(seqA);
		
		AnnotatedSequence seqB=new AnnotatedSequence();
		seqB.setSequence(
				"agatttggg   ",
				"   tttgggtct");
		lig.addSequence(seqB);

		AnnotatedSequence seqD=new AnnotatedSequence(seqB);
		seqD.reverseSequence();
		lig.addSequence(seqD);

		AnnotatedSequence seqC=new AnnotatedSequence();
		seqC.setSequence(
				"agatttggg",
				"   tttggg");
		lig.addSequence(seqC);
		
		
		lig.compute();
		lig.showPairs();		
		}

	private void showPairs()
		{
		for(int i=0;i<sequences.size();i++)
			{
			for(int j=0;j<sequences.size();j++)
				System.out.print(canLigate.get(i).get(j)+"\t");
			System.out.println();
			}
		}


	private void compute()
		{
		calculateCanLigate();
		}

	private void addSequence(AnnotatedSequence seqA)
		{
		sequences.add(seqA);
		}
	}
