package ND.modules.simulation.geneticalgorithm.tools;

import java.util.ArrayList;
import java.util.List;

public class Result {

        public List<String> values;
        public double score;  
        public int count = 1;

        public Result(double score) {
                this.values = new ArrayList<String>();
                this.score = score;
        }

        public void addValue(String value) {
                this.values.add(value);
        }

        public List<String> getValues() {
                return this.values;
        }
        
        public double getScore(){
            if(this.score == Double.NaN) return 0.0;
            return this.score;
        }

        public boolean isIt(List<String> values2) {
                if (values2.size() != this.values.size()) {
                        return false;
                }
                for (String val : values2) {
                        if (!this.values.contains(val)) {
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
                
                for (String val : values) {
                        text += val + " - ";
                }
                text += "\n ----------------------------------------------------------\n";
                return text;
        }
}
