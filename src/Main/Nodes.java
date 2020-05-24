package Main;

/**
 * @ author bannerblade
 * @ date 2020/3/21
 */
class Nodes{
    private static final int Max = 100000 ;
    public int ID;
    public int dis = Max;//点到源点距离
    public int pre_node = Max;//前置节点
    public int flag = 0;//确认最短距离

    Nodes(int ID, int dis,int pre_node){
        this.ID = ID;
        this.dis = dis;
        this.pre_node = pre_node;
    }

    Nodes(int ID, int dis,int pre_node,int flag){
        this.ID = ID;
        this.dis = dis;
        this.pre_node = pre_node;
        this.flag = flag;
    }

    Nodes(int ID){
        this.ID = ID;
    }

    Nodes(){}

    @Override
    public int hashCode(){
        return ID;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(this.getClass() != obj.getClass()) return false;
        Nodes node = (Nodes) obj;
        return ID == node.ID;
    }
}