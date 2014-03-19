/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network3;

/**
 *
 * @author scsandra
 */
public class Ant {

        String nodeId;
        String previousNodeId = "";
        boolean moved = false;

        public Ant(String id) {
                this.nodeId = id;
        }

        public void setNodeId(String id) {
                this.nodeId = id;
        } 
        
        public void setPreviousNodeId(String id) {
                this.previousNodeId = id;
        }        
        
        public boolean didMove() {
                return this.moved;
        }

        public void setMovement(boolean movement) {
                this.moved = movement;
        }

        public String getNodeId() {
                return this.nodeId;
        }
        
        public String getPreviousNodeId() {
                return this.previousNodeId;
        }        
      
}
