package graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * @ author bannerblade
 * @ date 2020/3/10
 */
public class Switch implements Serializable {
    private int ID;
    private int state;//交换机使用状态，on = 1,off =0;
    public int PW = 30;//交换机开启功耗，损耗100
    public Collection<VNF> VNFset = new HashSet<>();//这个容器存VNF，三个VNF，固定

    public int getID(){
        return this.ID;
    }
    public void setID(int id){
        this.ID = id;
    }


    public Switch (int ID){
        this.ID = ID;
        this.state = 0;

        for(int i=0;i<8;i++){//随机设置3个VNF，type 随机，资源容量100-150；
            Random r = new Random();
            //public VNF(ID,VNFtype, VNFcapacity, cost, price,state, embedID,  VPW)
            VNFset.add(    new VNF(i, r.nextInt(5),   r.nextInt(20) + 100, r.nextInt(5)+8));
        }
    }

    public Switch (int ID,int state, int in_PW){
        this.ID = ID;
        this.state = state;
        this.PW = in_PW;
    }

    public Switch (){}

    public int getstate(){return this.state;}
    public void setstate (int state){this.state = state;}

    public int getCostSum(){
        int res=0;
        for(VNF nf:VNFset){
            res += nf.cost;
        }
        return res;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        if(this.getClass() != obj.getClass()) return false;
        Switch sw = (Switch) obj;
        if(ID != sw.ID) return  false;
        return true;
    }
}





