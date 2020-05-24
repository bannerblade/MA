package graph;

/**
 * @ author bannerblade
 * @ date 2020/3/18
 */
public class Elink extends Link{

    public Elink (int id,int src , int dst, int Bandwidth,int type){
        super(id, src, dst, Bandwidth, type);
        //500 电链路初始化图的边的带宽大小
        super.setPrice(1);//price = 1;电链路能耗
    }

    public Elink(int id){
        super.setID(id);
    }

    public Elink(){}

}
