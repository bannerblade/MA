package graph;

import java.io.*;
import java.util.*;

/**
 * @ author bannerblade
 * @ date 2020/3/12
 */
public class Graph implements Serializable {
    private static final int Max = 100000 ;
    private int switchnum;//节点数目
    private int linknum;//边数目
    public Collection<Switch> switchset = new HashSet<>();//这个容器存switch[节点数目]
    public Collection<Elink> elinkset = new HashSet<>();//这个容器存e-Link
    public Collection<Olink> olinkset = new HashSet<>();//这个容器存o-link
    public Collection<Link> rlinkset = new HashSet<>();//这给容器存资源网络的边
    private Collection rlinknum = new HashSet();//这个来存删掉的link-ID号
    public int em_flag = 0;//这个映射函数用来标记是否映射成功。
    public double existence=0;//存在时间
    public double qcf=0;//转移概率
    public int OrLinkIndex = -1;

    public Graph(String fileRPath) throws IOException {
        File fl = new File(fileRPath);
        FileInputStream fls = new FileInputStream(fl);
        InputStreamReader isr = new InputStreamReader(fls);
        BufferedReader br = new BufferedReader(isr);

        int m=0,n=0;
        n = Integer.parseInt(br.readLine().trim());//节点数目
        m = Integer.parseInt(br.readLine().trim());//电链路数目
        switchnum = n;
        linknum = m;

        //初始化switch
        for(int i=0;i<switchnum;i++){
            switchset.add(new Switch(i));
        }
        for(int i=0;i<linknum;i++){//电链路层
            //String[] tps = br.readLine().split(",");
            String[] tps = br.readLine().split(" ");
            //边添加进去,有向边,电链路层 elinkset.add(new Elink(0,src,dst, 200, 0));
            elinkset.add(new Elink(Integer.parseInt(tps[0].trim()), Integer.parseInt(tps[1].trim()), Integer.parseInt(tps[2].trim()),
                    400,0));
            elinkset.add(new Elink(Integer.parseInt(tps[0].trim()) + linknum, Integer.parseInt(tps[2].trim()), Integer.parseInt(tps[1].trim()),
                    400,0));
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()),
                    2000,51,1));
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[2].trim()), Integer.parseInt(tps[1].trim()),
                    2000,51,1));
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()),
                    2000,51,2));
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[2].trim()), Integer.parseInt(tps[1].trim()),
                    2000,51,2));
        }
        br.close();
    }

    public Graph(){}

    public int getSwitchnum(){return this.switchnum;}
    public int getLinknum(){return this.linknum;}
    public int getOpenVNFNum(){
        int count = 0;
        for(Switch sw:switchset){
            for(VNF nf:sw.VNFset){
                if(nf.getState() == 1){
                    count++;
                }
            }
        }
        return count;
    }

    public int getOpenOpathNum(){
        int count = 0;
        for(Link ln:rlinkset){
            if(ln.getType() >= 50 && ln.getstate()==1){
                count++;
            }
        }
        return count;
    }

    public void delelink(int id){
        for(Elink tmp:elinkset){
            if(tmp.getid() == id){
                tmp.setstate(0);
                elinkset.remove(new Elink(tmp.getid()));
                rlinknum.add(id);
                this.linknum--;
            }
        }
    }

    public void RecoverResourceCapacity(){//恢复G的资源容量
        for(Switch sw:switchset){
            for(VNF tp_vnf:sw.VNFset){
                tp_vnf.cost = 0;
                //tp_vnf.embedID = Max;
            }
        }
        for(Link tmp_l:rlinkset){
            tmp_l.cost = 0;
        }
    }

    //统计收益
    public int getMaxUtility(){
        int result = 0;
        //计算Un
        Switch tmp = new Switch();
        Iterator i = switchset.iterator();
        while(i.hasNext()){
            tmp = (Switch) i.next();
            if(1 == tmp.getstate()){
                VNF tmp_vnf = new VNF();
                Iterator j = tmp.VNFset.iterator();
                while(j.hasNext()){
                    tmp_vnf = (VNF) j.next();
                    if(1 == tmp_vnf.getState()){
                        result += tmp_vnf.cost*tmp_vnf.getprice();//,price = usf
                    }
                }
            }
        }
        //Un-Zn-Zo-Ze
        result -= getNegUtility();

        return result;
    }

    public double getRmn(Link mn){
        double res=0;
        res = mn.cost/mn.getBandwidth();
        return res;
    }

    public double getRos(Switch sw){
        double res=0;
        double up=0,down=0;
        for(VNF nf:sw.VNFset){
            up += nf.cost;
            down += nf.getVNFcapacity();
        }
        res = up/down;
        return res;
    }

    public int getNegUtility(){
        int result = 0;
        //Zn
        for(Switch sw:switchset){
            result  += sw.PW; //减去switch启动损耗
            for(VNF nf:sw.VNFset){
                double rs = getRos(sw);
                if(1 == nf.getState()){
                    result += nf.VPW;//-psb
                    result += rs*nf.psh*nf.cost;//ro,phs
                }
            }
        }
        //Ze
        for(Link ln:rlinkset){
            double rmn = getRmn(ln);
            if(ln.getType() >= 50 && ln.getstate() == 1){
                result += ln.getO_total_price();//Zo
            }
            else if(ln.getType() < 50 && ln.getstate() == 1){//Ze
                result += rmn*ln.getPrice()*ln.cost;
                result += ln.peb;
            }
        }
        return result;
    }

    public double getExistence(){
        this.existence = this.qcf*this.getMaxUtility();
        if(this.existence < 0) this.existence = 0.000000001;
        return this.existence;
    }

    public void CreateRGraph(){//初始化，生成资源网络,只需要生成资源网络的边（rlink）
        //VNF资源不变，把边加入资源网络的边
        Iterator i = elinkset.iterator();
        Link tmp = new Link();//把现有elink全部加入到rlink
        while(i.hasNext()){
            tmp = (Link) i.next();
            tmp.setstate(1);
            this.rlinkset.add(tmp);
        }
    }

    public String GetInfo(){
        StringBuilder res = new StringBuilder();

        Iterator i = this.switchset.iterator();
        if(i.hasNext()){
            for(Switch tmp_s:this.switchset){
                res.append("\n");
                res.append("SW的ID: ").append(tmp_s.getID()).append("---->>>>");
                res.append("SW状态").append(tmp_s.getstate());
                res.append("\n");
                for(VNF tmpv:tmp_s.VNFset){
                    res.append("  VNF的ID：").append(tmpv.getID()).append("------>>>>>>");
                    res.append("  VNF状态").append(tmpv.getState());
                    res.append("  VNF类型").append(tmpv.getVNFtype());
                    res.append("  VNF容量").append(tmpv.VNFcapacity).append("******");
                    res.append("  VNF的cost").append(tmpv.cost);
                    res.append("\n");
                }
            }
            res.append("\n");
            res.append("rlink信息");
            res.append("\n");
            for(Link rl:this.rlinkset){
                res.append("rlink的ID: ").append(rl.getid()).append("容量：").append(rl.getBandwidth()).append("使用了的cost: ").append(rl.cost).append("-->  src：").append(rl.getsrcid()).append("  dst: ").append(rl.getdstid());
/*                if(rl.cost > 0){
                    res.append("rlink的ID: ").append(rl.getid()).append("容量：").append(rl.getBandwidth()).append("使用了的cost: ").append(rl.cost).append("-->  src：").append(rl.getsrcid()).append("  dst: ").append(rl.getdstid());
                }else{
                    res.append("没有");
                }*/
                res.append("\n");
            }
        }else {
            res.append("空的！");
            res.append("\n");
        }
        return res.toString();
    }

    public void conOp(){
        Random r = new Random();
        int id = r.nextInt(18);
        for(Olink ln:olinkset){
            if(ln.getid() == id){
                ln.reverseState();
            }
        }
    }

    public void cloneG(Graph Ga){//G_copy
        this.switchnum = Ga.switchnum;
        this.linknum = Ga.linknum;
        this.existence = Ga.existence;
        this.qcf = Ga.qcf;
        this.OrLinkIndex = Ga.OrLinkIndex;

        switchset.clear();
        for(Switch tmp_sw: Ga.switchset){
            switchset.add(new Switch(tmp_sw.getID(), tmp_sw.getstate(), tmp_sw.PW));
            for(VNF tmp_vnf:tmp_sw.VNFset){
                for(Switch sw1:switchset){
                    if(sw1.getID() == tmp_sw.getID()){
                        sw1.VNFset.add(new VNF(tmp_vnf.getID(), tmp_vnf.getVNFtype(),tmp_vnf.getVNFcapacity(), tmp_vnf.cost, tmp_vnf.getprice(),tmp_vnf.getState(), tmp_vnf.embedID, tmp_vnf.VPW));
                    }
                }
            }
        }

        elinkset.clear();
        for(Elink tmp_elink:Ga.elinkset){
            elinkset.add(new Elink(tmp_elink.getid(), tmp_elink.getsrcid(), tmp_elink.getdstid(),tmp_elink.getBandwidth(),tmp_elink.getType()));
        }


        olinkset.clear();
        for(Olink tmp_olink:Ga.olinkset){
            olinkset.add(new Olink(tmp_olink.getid(), tmp_olink.getsrcid(),tmp_olink.getdstid(),tmp_olink.getBandwidth(),tmp_olink.getType(),tmp_olink.getWave(),tmp_olink.rlinkID));
        }


        rlinkset.clear();
        for(Link tmp_rl:Ga.rlinkset){
            rlinkset.add(new Link(tmp_rl.getid(),tmp_rl.getsrcid(),tmp_rl.getdstid(),tmp_rl.cost,tmp_rl.getPrice(),tmp_rl.getBandwidth(),tmp_rl.getstate(),tmp_rl.getType(),tmp_rl.getWave(),tmp_rl.getO_total_price()));
        }


        this.rlinknum.clear();
        this.rlinknum.addAll(Ga.rlinknum);

        em_flag = Ga.em_flag;

    }

    public Object deepClone() throws Exception{
        //把对象写到流里
        ByteArrayOutputStream bo =new ByteArrayOutputStream();
        ObjectOutputStream oo =new ObjectOutputStream(bo);
        oo.writeObject(this);
        //将对象读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);

        return (oi.readObject());
    }


}
