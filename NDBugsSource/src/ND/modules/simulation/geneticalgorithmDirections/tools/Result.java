package ND.modules.simulation.geneticalgorithmDirections.tools;

import ND.modules.simulation.geneticalgorithmDirections.tools.Bug.status;
import java.util.HashMap;
import java.util.Map;

public class Result {

        public Map<String, status> values;
        public double score;
        public int count = 1;

        public Result(double score) {
                this.values = new HashMap<>();
                this.score = score;
        }

        public void addValue(String value, status stt) {
                this.values.put(value, stt);
        }       
      

        public Map<String, status> getValues() {
                return this.values;
        }
        
        public double getScore(){
            if(this.score == Double.NaN) return 0.0;
            return this.score;
        }

        public boolean isIt(Map<String, status> values2) {
                if (values2.size() != this.values.size()) {
                        return false;
                }
                for (String val : values2.keySet()) {
                        if (!this.values.containsKey(val)) {
                                return false;
                        }
                }    
                return true;
        }
        
        public int getCount(){
                return this.count;
        }

        public void count() {
                count++;
        }

        @Override
        public String toString() {
                 String text = " Training - Score: " + score + " - Count: " + count + "\n";
                
                for (String val : values.keySet()) {
                        text += val + " - " + values.get(val).toString() + ",";
                }
                text += "\n ----------------------------------------------------------\n";
                return text;
        }
}
