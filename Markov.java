import java.io.*;
import java.util.*;

public class Markov {
    // IO Stream
    private File source =  null;
    private Scanner in = null;
    //Data Structure
    private HashMap<String,Double> frequencyDistribution = null;
    private HashMap<String, HashSet<ConditionPair>> conditionalDistribution = null;
    private Double tokenCounts = 0.0;

    Markov(String path){
        source = new File(path);
        frequencyDistribution   = new HashMap<String,Double>();
        conditionalDistribution = new HashMap<String,HashSet<ConditionPair>>();
        try {
            in = new Scanner(source);
        } catch (FileNotFoundException e) {
            System.out.println("File not found,plz check again.");
            System.exit(1);
        }
        read();
        calc();
    }

    private void calc(){
        HashMap<String,Double> temp = new HashMap<String, Double>();
        Set<Map.Entry<String, Double>> entries = frequencyDistribution.entrySet();
        for (Iterator<Map.Entry<String, Double>> iterator = entries.iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Double> entry = iterator.next();
            temp.put(entry.getKey(),entry.getValue()/tokenCounts);
        }
        frequencyDistribution = temp;
    }

    private void read(){
        StringBuffer line = new StringBuffer();
        String []tokens = null;
        String token =null;
        String nextToken = null;
        Double frequency = null;
        ConditionPair conditionPair = null;
        HashSet<ConditionPair> conditionPairsSet = null;
        while(in.hasNext()){
            line.append(in.nextLine()+" ");
        }
        tokens = line.toString().split(" ");
        tokenCounts = (double) tokens.length;
        for (int i = 0; i < tokens.length; i++) {
            token = tokens[i];
            if((frequency = frequencyDistribution.get(token))==null){
                frequencyDistribution.put(token,1.0);
            }else{
                frequencyDistribution.put(token,frequency+1.0);
            }
            if(i!=tokens.length-1){
                nextToken = tokens[i+1];
                conditionPair = new ConditionPair(token,nextToken);
                if((conditionPairsSet=conditionalDistribution.get(token))==null){
                    conditionPairsSet = new HashSet<ConditionPair>();
                    conditionPairsSet.add(conditionPair);
                    conditionalDistribution.put(token,conditionPairsSet);
                }else if(conditionPairsSet.contains(conditionPair)){
                    Iterator<ConditionPair> iterator = conditionPairsSet.iterator();
                    while (iterator.hasNext()) {
                        ConditionPair next = iterator.next();
                        if(next.equals(conditionPair)){
                            next.frequency++;
                            break;
                        }
                    }

                }else{
                    conditionPairsSet.add(conditionPair);
                }
            }
        }
//        System.out.println(frequencyDistribution);
//        System.out.println(conditionalDistribution);
    }

    public void create(int cnt,String start){
        String step = start;
        while(cnt-- >0){
            if(step == null) step =getStart();
            else step = getNext(step);
            if(step==null)
                cnt++;
            else
                System.out.print(step+" ");
            if(cnt%10==0)
                System.out.println();
        }
    }

    public void create(int cnt){
        String step = null;
        while(cnt-- >0){
            if(step == null) step =getStart();
            else step = getNext(step);
            if(step==null)
                cnt++;
            else
                System.out.print(step+" ");
            if(cnt%10==0)
                System.out.println();
        }
    }
    private String getNext(String now){
        HashSet<ConditionPair> conditionPairs = conditionalDistribution.get(now);
        if(conditionPairs==null) return null;
        List<ConditionPair> condtionalPairs = new ArrayList<ConditionPair>(conditionPairs);
        double rand = Math.random();
        int all = 0;
        for (Iterator<ConditionPair> iterator = condtionalPairs.iterator(); iterator.hasNext(); ) {
            ConditionPair conditionPair = (ConditionPair)iterator.next();
            all+=conditionPair.frequency;
        }
        rand = rand * all;
        for (int i = 0; i < condtionalPairs.size(); i++) {
            rand -= condtionalPairs.get(i).frequency;
            if(rand<0) return condtionalPairs.get(i).second;
        }
        return null;
    }
    // token i : value(i-1)<value<=value(i)
    private String getStart(){
        ArrayList<Map.Entry<String,Double>> list = new ArrayList<>(frequencyDistribution.entrySet());
        double rand = Math.random(); // get a value [0,1)
        for (int i = 0; i < list.size(); i++) {
            rand -= list.get(i).getValue();
            if(rand<=0){
               return list.get(i).getKey();
           }
        }
        return null;
    }
    public static void main(String args[]){
        Markov markov = new Markov("Economist.txt");
        markov.create(200);
        System.out.println();
        markov.create(200, "African");
    }
}
class ConditionPair {
    String first;
    String second;
    Double frequency;

    ConditionPair(String first, String second) {
        this.first = first;
        this.second = second;
        frequency = 1.0;
    }

    @Override
    public int hashCode() {
        return first.length() + second.length();
    }

    @Override
    public boolean equals(Object obj) {
        ConditionPair compare = (ConditionPair) obj;
        return compare.first.equals(this.first) & compare.second.equals(this.second);
    }

    @Override
    public String toString() {
        return first+":"+second+" f: "+ frequency;
    }
}
