package com.search.suggestion.common;

import com.search.suggestion.data.ScoredObject;
import com.search.suggestion.data.SearchPayload;
import com.search.suggestion.data.SuggestPayload;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import static com.search.suggestion.common.Precondition.checkPointer;

/**
 * Aggregator to collect, merge and transform {@link ScoredObject} elements.
 */
@SuppressWarnings("unchecked")
public class Aggregator<T>
{
    private final Map<T, Double> scores;
    private final Comparator<ScoredObject<T>> comparator;

    /**
     * Constructs a new {@link Aggregator}.
     */
    public Aggregator()
    {
        this(null);
    }

    /**
     * Constructs a new {@link Aggregator}.
     */
    public Aggregator(@Nullable Comparator<ScoredObject<T>> comparator)
    {
        this.scores = new HashMap<>();
        this.comparator = comparator;
    }

    /**
     * Adds a single element, if not present already.
     *
     * @throws NullPointerException if {@code element} is null;
     */
    public boolean add(ScoredObject<T> element)
    {
        return addAll(Arrays.asList(element));
    }

    /**
     * Adds a collection of elements, if not present already.
     *
     * @throws NullPointerException if {@code elements} is null or contains a null element;
     */
    public boolean addAll(Collection<ScoredObject<T>> elements)
    {
        checkPointer(elements != null);
        boolean result = false;
        for (ScoredObject<T> element : elements)
        {
            checkPointer(element != null);
            Double score = scores.get(element.getObject());

            if (score == null || element.getScore()==0)
            {
                scores.put(element.getObject(), element.getScore());
                result = true;
            }
            else if (element.getScore() != 0)
            {

                scores.put(element.getObject(), score + element.getScore());
                result = true;
            }
            //System.out.println("Score is " + 	scores.get(element.getObject()));
        }
        return result;
    }

    /**
     * Returns {@code true} if no elements exist.
     */
    public boolean isEmpty()
    {
        return scores.isEmpty();
    }

    /**
     * Retain the elements in common, compared according to the objects scored.
     *
     * @throws NullPointerException if {@code element} is null;
     */
    public boolean retain(ScoredObject<T> element)
    {
        return retainAll(Arrays.asList(element));
    }

    /**
     * Retains the elements in common, compared according to the objects scored.
     *
     * @throws NullPointerException if {@code elements} is null or contains a null element;
     */
    public boolean retainAll(Collection<ScoredObject<T>> elements)
    {
        checkPointer(elements != null);
        // Intersect
        Collection<T> set = new HashSet<>();
        for (ScoredObject<T> element : elements)
        {
            checkPointer(element != null);
            set.add(element.getObject());
        }
        boolean result = scores.keySet().retainAll(set);
        // Combine scores
        for (ScoredObject<T> element : elements)
        {
            if (element.getScore() == 0)
            {
                continue;
            }
            Double score = scores.get(element.getObject());
            if (score != null)
            {
                scores.put(element.getObject(), score + element.getScore());
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the number of elements.
     */
    public int size()
    {
        return scores.size();
    }

    /**
     * Returns a {@link List} of all objects scored, sorted according to the default comparator.
     */
    public List<T> values()
    {
        List<ScoredObject<T>> list = new ArrayList<>();
        for (Entry<T, Double> entry : scores.entrySet())
        {

        	//System.out.println("Actual score is "+entry.getValue()+" For "+entry.getKey().getSearch());
            list.add(new ScoredObject<>(entry.getKey(), entry.getValue()));
        }
        Collections.sort(list, comparator);
        List<T> result = new ArrayList<>();
        for (ScoredObject<T> element : list)
        {
            result.add(element.getObject());
        }
        return result;
    }
    /**
     * Returns a {@link List} of all objects scored, sorted according to the default comparator.
     */
    public TreeMap <Double,List<SuggestPayload>> values(SearchPayload json, boolean bool)
    {
        String bucketKey = json.getFirstBucket();
        int bucketValue = json.getBucket(bucketKey);
    	//Double allowedScore = (Double) (json.getRealText().split(" ").length -.5);
    	/*String query = json.getString("q");*/
    	Double value;
        TreeMap<Double,List<SuggestPayload>> tmap = new TreeMap<Double,List<SuggestPayload>>(Collections.reverseOrder());
        
        for (Entry<T, Double> entry : scores.entrySet())
        {
        	SuggestPayload oldsr = (SuggestPayload) entry.getKey();
        	SuggestPayload sr = new SuggestPayload(oldsr.getSearch(), new HashMap<String,Integer>());
        	sr.copy(oldsr);
        	
        	value = entry.getValue();
        	value = (double)Math.round(value * 100) / 100;
        	
        	//System.out.println(" Before sorting for name "+sr.getSearch()+" count is "+sr.getCount()+" value is "+value);
        	//if(value>=allowedScore){
        		UpdateMap(tmap,sr,value);
        	//}
        	
        	//System.out.println("LIST"+tmap);
        	
            //list.add(new ScoredObject<>(entry.getKey(), value));
        }
        return tmap;
    }
    /**
     * Returns a {@link List} of all objects scored, sorted according to the default comparator.
     */
    public List<T> values(SearchPayload json)
    {
        String bucketKey = json.getFirstBucket();

    	//Double allowedScore = (Double) (json.getRealText().split(" ").length -.5);
    	/*String query = json.getString("q");*/
    	Double value;
        TreeMap<Double,List<SuggestPayload>> tmap = new TreeMap<Double,List<SuggestPayload>>(Collections.reverseOrder());
        
        for (Entry<T, Double> entry : scores.entrySet())
        {
        	SuggestPayload oldsr = (SuggestPayload) entry.getKey();
        	SuggestPayload sr = new SuggestPayload(oldsr.getSearch(), new HashMap<>());
        	sr.copy(oldsr);
        	
        	value = entry.getValue();
        	value = (double)Math.round(value * 100) / 100;
        	
        	//System.out.println(" Before sorting for name "+sr.getSearch()+" count is "+sr.getCount()+" value is "+value);
        	//if(value>=allowedScore){
        		UpdateMap(tmap,sr,value);
        	//}
        	
        	//System.out.println("LIST"+tmap);
        	
            //list.add(new ScoredObject<>(entry.getKey(), value));
        }
        Set set = tmap.entrySet();
        Iterator iterator = set.iterator();
        List<T> result = new ArrayList<>();
        while(iterator.hasNext()) {
           Entry mentry = (Entry)iterator.next();
           ArrayList <SuggestPayload> al = (ArrayList<SuggestPayload>)mentry.getValue();
           TreeMap<Integer,List<SuggestPayload>> tMapWithBucket = new TreeMap<Integer,List<SuggestPayload>>(Collections.reverseOrder());
           TreeMap<Integer,List<SuggestPayload>> tMapNoBucket = new TreeMap<Integer,List<SuggestPayload>>(Collections.reverseOrder());
           for(int i=0;i<al.size();i++) {

        	   SuggestPayload sr = al.get(i);
        	   if(sr.getFilter(bucketKey) != null && sr.getFilter(bucketKey) == json.getBucket(bucketKey)) {
        		   // custom bucket
        		   UpdateMap(tMapWithBucket,sr,sr.getCount());
        	   }
        	   else {
        		   //Non user bucket
        		   UpdateMap(tMapNoBucket,sr,sr.getCount());
        	   }

           }
           //System.out.println("user wlaa "+tMapWithBucket);
           //System.out.println("Bina user wala "+tMapNoBucket);
           UpdateResults(result,tMapWithBucket);
           UpdateResults(result,tMapNoBucket);
           //System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
          // System.out.println(mentry.getValue());
        }
        //System.out.println(result);
        //System.out.println(tmap);
        //System.out.println(list);
       // Collections.sort(list, comparator);

       /* for (ScoredObject<T> element : list)
        {
            result.add(element.getObject());
        }*/
        return result;
    }
    public void UpdateResults(List<T> result,TreeMap<Integer,List<SuggestPayload>> tmap) {
    	Set set = tmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
        	Entry mentry = (Entry)iterator.next();
        	ArrayList<SuggestPayload> al = (ArrayList<SuggestPayload>)mentry.getValue();
        	for(int i=0;i<al.size();i++) {
        		result.add((T)al.get(i));
        	}
        }
    }
    public <T>void UpdateMap(TreeMap<T,List<SuggestPayload>> tmap, SuggestPayload sr, T key) {
    	//System.out.println("coming here for "+key);
    	if(tmap.containsKey(key)) {
		    List<SuggestPayload> templist =  tmap.get(key);
		    boolean norecord = true;
		    T newkey = key;
		    //System.out.println("-------- For loop started -------");
		    //System.out.println("Searching for --->"+sr.getRealText());
		    for (Object srt : templist.toArray()) {
		    	SuggestPayload srs = (SuggestPayload)srt;
		    	String name = sr.getSearch();
		    	String name1 = srs.getSearch();
		    	String rn = sr.getRealText();
		    	String rn1 = srs.getRealText();
		    	//System.out.println(rn);
		    	//System.out.println(rn1);
		    	
		    	//System.out.println("start of this -->"+sr.getSearch()+" "+srs.getSearch()+" "+sr.getRealText()+" "+srs.getRealText()+" end of this");

                if(sr.getSearch().equals(srs.getSearch()) || sr.getRealText().equals(srs.getRealText())) {
		    		norecord = false;
		    		//System.out.println("ONe found"+sr.getRealText());
		    		srs.setCount(srs.getCount()+sr.getCount());
		    		break;
		    	}
		    }
		    if(norecord) {
		    	//System.out.println(templist);
		    	//System.out.println("not found in  list -->"+ sr.getRealText());
	       		templist.add(sr);
	       		tmap.remove(key);
	       		tmap.put(key, templist);
		    }
	   }
	   else
	   {
		   List<SuggestPayload> templist = new ArrayList<SuggestPayload>();
		   templist.add(sr);
		   tmap.put(key, templist);
	   }
    }
}