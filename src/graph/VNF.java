package graph;

import java.io.Serializable;

/**
 * @ author bannerblade
 * @ date 2020/3/9
 */
public class VNF implements Serializable {
    private static final int Max = 100000 ;

    private  int ID;
    private int VNFtype; //假定有5种type：0，1，2，3，4
    public int VNFcapacity;//VNF容量，直接算作CPU使用资源量。
    public int cost = 0;//vnf资源正在使用的量
    private int price=1;//单位资源收益，默认1，再switch初始化的时候会更改
    public int psh=1;//单位损耗
    private int state;//VNF的状态，这个是图用的，SFC不用。
    public int embedID = Max;//存映射到的那个点的ID
    public int embedVnfID = Max;//存映射到的那个点的vnf-ID

    public int VPW = 200;//VNF空载损耗

    public VNF(int VNFtype, int VNFcapacity){
        this.VNFtype = VNFtype;
        this.VNFcapacity = VNFcapacity;
    }

    public VNF(int ID,int VNFtype, int VNFcapacity){
        this.ID = ID;
        this.VNFtype = VNFtype;
        this.VNFcapacity = VNFcapacity;
    }

    public VNF(int ID,int VNFtype, int VNFcapacity, int price){
        this.ID = ID;
        this.VNFtype = VNFtype;
        this.VNFcapacity = VNFcapacity;
        this.price = price;
        this.state = 0;
    }

    public VNF(int ID,int VNFtype, int VNFcapacity, int cost, int price,int state, int embedID, int VPW){
        this.ID = ID;
        this.VNFtype = VNFtype;
        this.VNFcapacity = VNFcapacity;
        this.price = price;
        this.state = state;
        this.cost = cost;
        this.embedID = embedID;
        this.VPW = VPW;
    }

    public VNF(int ID){
        this.ID = ID;
    }

    public VNF(){}

    public  int getState(){return this.state;}
    public void setState(int state){this.state = state;}

    public int getID(){return this.ID;}
    public void setID(int ID){this.ID = ID;}

    public int getprice(){return this.price;}
    public void setPrice(int price){this.price = price;}

    public int getVNFtype(){
        return this.VNFtype ;
    }

    public void setVNFtype(int VNFtype){
        this.VNFtype = VNFtype ;
    }

    public int getVNFcapacity(){
        return this.VNFcapacity;
    }

    public void setVNFcapacity(int VNFcapacity){
        this.VNFcapacity = VNFcapacity;
    }

    public void reverseState(){
        if(this.getState() == 1){//节点VNF状态更新
            this.setState(0);
            this.cost = 0;
            this.embedID = Max;
            this.embedVnfID =Max;
        }else{
            this.setState(1);
        }
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
        VNF vnf = (VNF) obj;
        return ID == vnf.getID();
    }
}
