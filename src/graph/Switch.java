package graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * @ author bannerblade
 * @ date 2020/3/10
 */
public class Switch {
    private int ID;
    private int state;//交换机使用状态，on = 1,off =0;
    public int PW = 100;//交换机开启功耗，损耗
    public Collection<VNF> VNFset = new HashSet<>();//这个容器存VNF，三个VNF，固定
    //public Collection<VNF> embed_VNFsets = new HashSet<>();//这个容器存SFC的VNF，表示映射到这个点上


    public int getID(){
        return this.ID;
    }

    public void setID(int id){
        this.ID = id;
    }

    public void setVNFs(){
        for(int i=0;i<3;i++){
            Random r = new Random();
            VNFset.add(    new VNF(i, r.nextInt(5), r.nextInt(50) + 100, r.nextInt(5)+5));
        }
    }

    public Switch (int ID){
        this.ID = ID;
        this.state = 0;

        for(int i=0;i<3;i++){//随机设置3个VNF，type 随机，资源容量100-150；
            Random r = new Random();
            VNFset.add(    new VNF(i, r.nextInt(5),  r.nextInt(50) + 100, r.nextInt(5)+5));
        }
    }

    public Switch (){}

    public int getstate(){return this.state;}
    public void setstate (int state){this.state = state;}

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





