package restrictionEnzyme;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import sequtil.DegenerateBases;

/**
 * Definition of a restriction enzyme
 * 
 * @author Johan Henriksson
 *
 */
public class RestrictionEnzyme
	{
	/**
	 * Name of enzyme
	 */
	public String name;
	
	/**
	 * Sequence it matches - the upper 5-3'
	 */
	public String sequence;

	/**
	 * List of cuts
	 */
	public LinkedList<RestrictionEnzymeCut> cuts=new LinkedList<RestrictionEnzymeCut>();
	
	/**
	 * Inactivation temperature
	 */
	public Double tempInactivation;
	
	/**
	 * Incubation temperature
	 */
	public Double tempIncubation;
	
	/**
	 * Efficiences in different buffers
	 */
	public TreeMap<String, Double> bufferEfficiency=new TreeMap<String, Double>(); //Buffer name -> efficiency
	
	
	/**
	 * Return matching as a regular expression
	 */
	public String getRegexp()
		{
		StringBuilder s=new StringBuilder();
		for(char c:sequence.toCharArray())
			s.append("["+DegenerateBases.getLettersFor(""+c)+"]");
		return s.toString();
		}

	
	/**
	 * Parse a sequence that contains /, or several (a/b)
	 */
	public boolean parseSequence(String seq)
		{
		seq=seq.trim();
		if(seq.contains("("))
			{
			while(seq.contains("("))
				{
				int ind=seq.indexOf("/");
				int ind2=seq.indexOf('(');
				int ind3=seq.indexOf(')');
				
				RestrictionEnzymeCut cut=new RestrictionEnzymeCut();
				cut.upper=Integer.parseInt(seq.substring(ind2+1,ind));
				cut.lower=Integer.parseInt(seq.substring(ind+1,ind3));
				seq=seq.substring(0,ind2) + seq.substring(ind3+1);
				cuts.add(cut);
				}
			
			sequence=seq;
			}
		else if(seq.contains("/"))
			{
			int ind=seq.indexOf("/");
			sequence=seq.replace("/", "");
			
			RestrictionEnzymeCut cut=new RestrictionEnzymeCut();
			cut.upper=cut.lower=ind;
			cuts.add(cut);
			}
		else
			return false;
		return true;
		}
	

	/**
	 * Find the best common buffer
	 */
	public static TreeMap<String, Double> getCommonBufferEfficiency(Collection<RestrictionEnzyme> enz)
		{
		TreeMap<String, Double> map=new TreeMap<String, Double>();
		Iterator<RestrictionEnzyme> it=enz.iterator();
		if(it.hasNext())
			{
			map.putAll(it.next().bufferEfficiency);
			for(RestrictionEnzyme e:enz)
				{
				map.keySet().retainAll(e.bufferEfficiency.keySet());
				for(String buf:e.bufferEfficiency.keySet())
					{
					double eff=e.bufferEfficiency.get(buf);
					if(map.get(buf)==null || map.get(buf)>eff)
						map.put(buf,eff);
					}
				}
			}
		return map;
		}
	
	
	}