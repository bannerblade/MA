package graph;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @ author bannerblade
 * @ date 2020/3/12
 */
public class Graph implements Serializable {
    private static final int Max = 100000 ;
    private int switchnum = 6;//节点数目
    private int linknum;//边数目
    public Collection<Switch> switchset = new HashSet<>();//这个容器存switch[节点数目]
    public Collection<Elink> elinkset = new HashSet<>();//这个容器存e-Link
    public Collection<Olink> olinkset = new HashSet<>();//这个容器存o-link
    public Collection<Link> rlinkset = new HashSet<>();//这给容器存资源网络的边
    private Collection rlinknum = new HashSet();//这个来存删掉的link-ID号
    public int em_flag = 0;//这个映射函数用来标记是否映射成功。
    public double existence=0;//存在时间
    public double qcf=0;//转移概率

    public Graph(int Gtype){
        if(Gtype == 0){//只有电链路的图
            switchnum = 6;
            linknum = 16;
            //初始化6个switch
            for(int i=0;i<switchnum;i++){
                switchset.add(new Switch(i));
            }
            //初始化16条有向边，电链路层
            elinkset.add(new Elink(0,0,1, 500, 0));
            elinkset.add(new Elink(1,1,0, 500, 0));

            elinkset.add(new Elink(2,0,2, 500, 0));
            elinkset.add(new Elink(3,2,0, 500, 0));

            elinkset.add(new Elink(4,1,2, 500, 0));
            elinkset.add(new Elink(5,2,1, 500, 0));

            elinkset.add(new Elink(6,1,4, 500, 0));
            elinkset.add(new Elink(7,4,1, 500, 0));

            elinkset.add(new Elink(8,2,3, 500, 0));
            elinkset.add(new Elink(9,3,2, 500, 0));

            elinkset.add(new Elink(10,3,4, 500, 0));
            elinkset.add(new Elink(11,4,3, 500, 0));

            elinkset.add(new Elink(12,3,5, 500, 0));
            elinkset.add(new Elink(13,5,3, 500, 0));

            elinkset.add(new Elink(14,4,5, 500, 0));
            elinkset.add(new Elink(15,5,4, 500, 0));
        }

        if(Gtype == 1){//6个点的标准小图生成
            switchnum = 6;
            linknum = 16;
            //初始化6个switch
            for(int i=0;i<switchnum;i++){
                switchset.add(new Switch(i));
            }
            //初始化16条有向边，电链路层
            elinkset.add(new Elink(0,0,1, 200, 0));
            elinkset.add(new Elink(1,1,0, 200, 0));

            elinkset.add(new Elink(2,0,2, 200, 0));
            elinkset.add(new Elink(3,2,0, 200, 0));

            elinkset.add(new Elink(4,1,2, 200, 0));
            elinkset.add(new Elink(5,2,1, 200, 0));

            elinkset.add(new Elink(6,1,4, 200, 0));
            elinkset.add(new Elink(7,4,1, 200, 0));

            elinkset.add(new Elink(8,2,3, 200, 0));
            elinkset.add(new Elink(9,3,2, 200, 0));

            elinkset.add(new Elink(10,3,4, 200, 0));
            elinkset.add(new Elink(11,4,3, 200, 0));

            elinkset.add(new Elink(12,3,5, 200, 0));
            elinkset.add(new Elink(13,5,3, 200, 0));

            elinkset.add(new Elink(14,4,5, 200, 0));
            elinkset.add(new Elink(15,5,4, 200, 0));

            //初始化16条有向边，光链路层波长1,2,3(三个波长)
            //1
            olinkset.add(new Olink(0,0,1, 2000, 1,0));
            olinkset.add(new Olink(1,1,0, 2000, 1,0));

            olinkset.add(new Olink(2,0,2, 2000, 1,0));
            olinkset.add(new Olink(3,2,0, 2000, 1,0));

            olinkset.add(new Olink(4,1,2, 2000, 1,0));
            olinkset.add(new Olink(5,2,1, 2000, 1,0));

            olinkset.add(new Olink(6,1,4, 2000, 1,0));
            olinkset.add(new Olink(7,4,1, 2000, 1,0));

            olinkset.add(new Olink(8,2,3, 2000, 1,0));
            olinkset.add(new Olink(9,3,2, 2000, 1,0));

            olinkset.add(new Olink(10,3,4, 2000, 1,0));
            olinkset.add(new Olink(11,4,3, 2000, 1,0));

            olinkset.add(new Olink(12,3,5, 2000, 1,0));
            olinkset.add(new Olink(13,5,3, 2000, 1,0));

            olinkset.add(new Olink(14,4,5, 2000, 1,0));
            olinkset.add(new Olink(15,5,4, 2000, 1,0));
            //2
            olinkset.add(new Olink(0,0,1, 2000, 1,1));
            olinkset.add(new Olink(1,1,0, 2000, 1,1));

            olinkset.add(new Olink(2,0,2, 2000, 1,1));
            olinkset.add(new Olink(3,2,0, 2000, 1,1));

            olinkset.add(new Olink(4,1,2, 2000, 1,1));
            olinkset.add(new Olink(5,2,1, 2000, 1,1));

            olinkset.add(new Olink(6,1,4, 2000, 1,1));
            olinkset.add(new Olink(7,4,1, 2000, 1,1));

            olinkset.add(new Olink(8,2,3, 2000, 1,1));
            olinkset.add(new Olink(9,3,2, 2000, 1,1));

            olinkset.add(new Olink(10,3,4, 2000, 1,1));
            olinkset.add(new Olink(11,4,3, 2000, 1,1));

            olinkset.add(new Olink(12,3,5, 2000, 1,1));
            olinkset.add(new Olink(13,5,3, 2000, 1,1));

            olinkset.add(new Olink(14,4,5, 2000, 1,1));
            olinkset.add(new Olink(15,5,4, 2000, 1,1));
            //3
            olinkset.add(new Olink(0,0,1, 2000, 1,2));
            olinkset.add(new Olink(1,1,0, 2000, 1,2));

            olinkset.add(new Olink(2,0,2, 2000, 1,2));
            olinkset.add(new Olink(3,2,0, 2000, 1,2));

            olinkset.add(new Olink(4,1,2, 2000, 1,2));
            olinkset.add(new Olink(5,2,1, 2000, 1,2));

            olinkset.add(new Olink(6,1,4, 2000, 1,2));
            olinkset.add(new Olink(7,4,1, 2000, 1,2));

            olinkset.add(new Olink(8,2,3, 2000, 1,2));
            olinkset.add(new Olink(9,3,2, 2000, 1,2));

            olinkset.add(new Olink(10,3,4, 2000, 1,2));
            olinkset.add(new Olink(11,4,3, 2000, 1,2));

            olinkset.add(new Olink(12,3,5, 2000, 1,2));
            olinkset.add(new Olink(13,5,3, 2000, 1,2));

            olinkset.add(new Olink(14,4,5, 2000, 1,2));
            olinkset.add(new Olink(15,5,4, 2000, 1,2));
        }
    }

    public Graph(String fileRPath, String fileWPath) throws IOException {
        File fl = new File(fileRPath);
        FileInputStream fls = new FileInputStream(fl);
        InputStreamReader isr = new InputStreamReader(fls);
        BufferedReader br = new BufferedReader(isr);

        File  fwl = new File(fileWPath);
        FileOutputStream fos = new FileOutputStream(fwl);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);

        int m,n;
        n = Integer.parseInt(br.readLine().trim());//节点数目
        m = Integer.parseInt(br.readLine().trim());//电链路数目
        switchnum = n;
        linknum = m;
        bw.write(n);
        bw.newLine();
        bw.write(m);
        bw.newLine();
        //int oLinkNums=
        //初始化6个switch
        for(int i=0;i<switchnum;i++){
            switchset.add(new Switch(i));
        }
        for(int i=0;i<linknum;i++){//电链路层
            String[] tps = br.readLine().split(",");
            //边添加进去,初始化16条有向边，电链路层
            elinkset.add(new Elink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()) ,
                    Integer.parseInt(tps[3].trim()),Integer.parseInt(tps[4].trim())));
            bw.write(Arrays.toString(tps));
            bw.newLine();
        }
        for(int i=0;i<linknum;i++){//O1光链路层
            String[] tps = br.readLine().split(",");
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()) ,
                    Integer.parseInt(tps[3].trim()),Integer.parseInt(tps[4].trim()),Integer.parseInt(tps[5].trim())));
            bw.write(Arrays.toString(tps));
            bw.newLine();
        }
        for(int i=0;i<linknum;i++){//O2光链路层
            String[] tps = br.readLine().split(",");
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()) ,
                    Integer.parseInt(tps[3].trim()),Integer.parseInt(tps[4].trim()),Integer.parseInt(tps[5].trim())));
            bw.write(Arrays.toString(tps));
            bw.newLine();
        }
        for(int i=0;i<linknum;i++){//O3光链路层
            String[] tps = br.readLine().split(",");
            olinkset.add(new Olink(Integer.parseInt(tps[0].trim()),Integer.parseInt(tps[1].trim()) ,Integer.parseInt(tps[2].trim()) ,
                    Integer.parseInt(tps[3].trim()),Integer.parseInt(tps[4].trim()),Integer.parseInt(tps[5].trim())));
            bw.write(Arrays.toString(tps));
            bw.newLine();
        }
        br.close();
        bw.close();
    }

    public Graph(){}

    public int getSwitchnum(){return this.switchnum;}
    public int getLinknum(){return this.linknum;}


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

    public void recoverResourceCapacity(){//恢复G的资源容量
        for(Switch sw:switchset){
            for(VNF tp_vnf:sw.VNFset){
                tp_vnf.cost = 0;
                //tp_vnf.embedID = Max;
            }
        }
        for(Link tmp_l:rlinkset){
            tmp_l.cost = 0;
        }
        ///光链路缺
    }

    //统计收益
    public int getMaxUtility(){
        int result = 0;
        //计算节点收益
        Switch tmp = new Switch();
        Iterator i = switchset.iterator();
        while(i.hasNext()){
            tmp = (Switch) i.next();
            if(1 == tmp.getstate()){
                //result  -= tmp.PW; //减去switch启动损耗
                //计算VNF里的消耗
                VNF tmp_vnf = new VNF();
                Iterator j = tmp.VNFset.iterator();
                while(j.hasNext()){
                    tmp_vnf = (VNF) j.next();
                    if(1 == tmp_vnf.getState()){
                        //result -= tmp_vnf.VPW;
                        result += tmp_vnf.cost*tmp_vnf.getprice();
                    }
                }
            }
        }
        //计算边损耗,光模块开关会导致switch的PW值变化。直接算资源网络的link里的消耗
        Link tmp_link = new Link();
        Iterator k = this.rlinkset.iterator();
        while(k.hasNext()){
            tmp_link = (Link) k.next();
            if(1 == tmp_link.getstate()){
                if(tmp_link.getType() >= 50){ //判断光通路和普通链路，价格分开计算
                    result -= tmp_link.getO_total_price();
                }else{
                    result -= tmp_link.getPrice()*tmp_link.cost;
                }
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

    public void cloneG(Graph Ga){//G_copy
        this.switchnum = Ga.switchnum;
        this.linknum = Ga.linknum;
        this.existence = Ga.existence;
        this.qcf = Ga.qcf;

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
