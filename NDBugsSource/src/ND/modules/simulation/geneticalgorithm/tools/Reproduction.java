/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.simulation.geneticalgorithm.tools;

import ND.data.Dataset;
import java.util.List;
import java.util.Random;

/**
 *
 * @author scsandra
 */
public class Reproduction extends Thread {

    private final List<Bug> bugsInside;
    List<Bug> population;
    private final Random rand;
    Dataset trainingDataset;
    int bugLife; 

    public Reproduction(List<Bug> bugsInside, List<Bug> population, Random rand, Dataset trainingDataset, int bugLife) {
        this.bugsInside = bugsInside;
        this.population = population;
        this.rand = rand;
        this.bugLife = bugLife;
        this.trainingDataset = trainingDataset;
    }

    public void run() {
        Bug mother = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        Bug father = bugsInside.get(this.rand.nextInt(bugsInside.size()));
        if (!mother.isSameBug(father)) {
            population.add(new Bug(father, mother, trainingDataset, bugLife));
        }
    }
}
