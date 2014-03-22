/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph.network;


/**
 *
 * @author scsandra
 */
public class Node {

        final private String id;
        public Node(String id) {
                this.id = id;
        } 
        
        public String getId() {
                return id;
        }      

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((id == null) ? 0 : id.hashCode());
                return result;
        }
       
}
