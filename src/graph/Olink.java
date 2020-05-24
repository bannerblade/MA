package graph;

/**
 * @ author bannerblade
 * @ date 2020/3/19
 */
public class Olink extends Link{
    private static final int Max = 100000 ;
    private int Opw = 20;//光发射模块能耗
    private int Opwtran = 2;//光转发模块能耗
    public int rlinkID = Max;

    public Olink (int id,int src , int dst, int Bandwidth,int type,int wave){
        super(id, src, dst, Bandwidth, type);
        //500 光链路每个波段边的带宽大小
        super.setWave(wave);
    }

    public Olink(int id){
        super.setID(id);
    }

    public Olink(){}

    public int getOpw(){return this.Opw;}
    public int getOpwtran(){return this.Opwtran;}
}
