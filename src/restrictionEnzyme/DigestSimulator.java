package restrictionEnzyme;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import seq.AnnotatedSequence;
import seq.RestrictionSite;

/**
 * 
 * Simulator of digests
 * 
 * @author Johan Henriksson
 *
 */
public class DigestSimulator
	{
	public LinkedList<RestrictionDigestFragment> cutregions=new LinkedList<RestrictionDigestFragment>();

	public void simulate(AnnotatedSequence seq, LinkedList<RestrictionEnzyme> enzymes)
		{
		//Collect a list of cutting positions
		LinkedList<RestrictionSite> sites=new LinkedList<RestrictionSite>();
		for(RestrictionEnzyme e:enzymes)
			for(RestrictionSite s:seq.restrictionSites.get(e))
				//TODO only keep those that cut both sides
				sites.add(s);
		
		//Sort from left to right
		Collections.sort(sites,new Comparator<RestrictionSite>()
			{
			public int compare(RestrictionSite a, RestrictionSite b)
				{
				return Double.compare(a.cuttingUpperPos, b.cuttingUpperPos);
				}
			});

		//Create the fragments
		for(int i=1;i<sites.size()-1;i++)
			{
			RestrictionDigestFragment r=new RestrictionDigestFragment();
			r.origseq=seq;
			r.fromSite=sites.get(i);
			r.toSite=sites.get(i);
			cutregions.add(r);
			}
		if(!sites.isEmpty())
			{
			if(seq.isCircular)
				{
				RestrictionDigestFragment r=new RestrictionDigestFragment();
				r.origseq=seq;
				int n=sites.size()-1;
				r.fromSite=sites.get(n);
				r.toSite=sites.get(0);
				cutregions.add(r);
				}
			else
				{
				//Keep first and last fragment
				RestrictionDigestFragment r1=new RestrictionDigestFragment();
				r1.origseq=seq;
				r1.fromSite=null;
				r1.toSite=sites.get(0);
				cutregions.add(r1);
				
				RestrictionDigestFragment r2=new RestrictionDigestFragment();
				r2.origseq=seq;
				r2.fromSite=sites.get(sites.size()-1);
				r2.toSite=null;
				cutregions.add(r2);
				}
			
			}
		}

	}