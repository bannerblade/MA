package Main;

import SFC.Sfc;
import graph.Graph;

import java.util.Collection;
import java.util.Random;

/**
 * @ author bannerblade
 * @ date 2020/7/13
 */
public class NumsGet {
    public  double getEmbedSfcNums(Collection<Sfc> SfcSets) {
        double res = 0;
        for(Sfc sfc:SfcSets){
            if(sfc.getState() == 1) res++;
        }
        res = res/SfcSets.size();
        return res;
    }
}
