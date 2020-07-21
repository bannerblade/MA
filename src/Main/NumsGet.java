package Main;

import SFC.Sfc;

import java.util.Collection;

/**
 * @ author bannerblade
 * @ date 2020/7/13
 */
public class NumsGet {
    public  int getEmbedSfcNums(Collection<Sfc> SfcSets) {
        int res = 0;
        for(Sfc sfc:SfcSets){
            if(sfc.getState() == 1) res++;
        }
        return res;
    }
}
