package Main;

import SFC.Sfc;
import graph.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * @ author bannerblade
 * @ date 2020/3/25
 */


public class MC {
    private static final int Max = 100000 ;
    private static final double beta = 1;
    public Graph G = new Graph();
    public OutputStreamWriter osw;
    public Collection<Sfc> sfcsets = new HashSet<>();
    //public Set<Sfc> resSfcSets = new HashSet<>();////ResSfcsets装关联sfc

    public MC(Graph Ga, Collection<Sfc> sfcset){
        G.cloneG(Ga);
        sfcsets.addAll(sfcset);
        //resSfcSets.addAll(sfcset);
    }

    void MC_ini(){
        //初始化MC，最开始的启动
        //节点初始化，开启了全部节点的VNF和电链路
        for(Switch tmp:G.switchset){
            tmp.setstate(1);
            if(tmp.getID() == 0 || tmp.getID() == 2){
                for(VNF tmp_vnf:tmp.VNFset){
                    if(tmp_vnf.getID() == 1 || tmp_vnf.getID() == 2) tmp_vnf.setState(1);
                }
            }else{
                for(VNF tmp_vnf:tmp.VNFset){
                    if(tmp_vnf.getID() == 1) tmp_vnf.setState(1);
                }
            }
        }
        //边初始化
        for(Elink tmpElink:G.elinkset){
            tmpElink.setstate(1);
            G.rlinkset.add(tmpElink);
        }

    }

    Collection<Link> dijkstra(int src_embedID, int dst_embedID, Collection<Link> in_linkstes, int node_num){
        //找最短路
        Collection<Link> linksets = new HashSet<>();//存结果
        Collection<Link> linksets_fall = new HashSet<>();//若失败，返回这个结果
        Collection<Nodes> nodesets = new HashSet<>();//中间变量集合，存结果的
        int k = -1 ;//一个中间变量,前置节点
        int flag_success = 0;//看能不能找到一条路，成功为1；

        //初始化，把src到其他点的距离初始化为Max;
        nodesets.add(new Nodes(src_embedID,0,src_embedID,1));//源节点到自己的距离为0
        for(Link tmp:in_linkstes){
            if(tmp.getsrcid() == src_embedID){//加入直连的边
                nodesets.add(new Nodes(tmp.getdstid(),tmp.cost,src_embedID));
            }
        }

        for(int j = 0;j<node_num;j++){//把点集合补充完整。这些点非直连，初始化
            Nodes tmp_j_node = new Nodes(j);
            if(nodesets.contains(tmp_j_node)) continue;
            nodesets.add(new Nodes(j));
        }

        //遍历所有顶点
        int count=0;
        for(Nodes tmp_node:nodesets){
            count++;
            if(tmp_node.ID == src_embedID) continue;

            int min = Max;
            for(Nodes tmp_N2:nodesets){//找出本次的最短距离的那个点
                if(tmp_N2.flag == 0 && tmp_N2.dis < min){
                    min = tmp_N2.dis;
                    k = tmp_N2.ID;
                }
            }

            for(Nodes tmp_K:nodesets){ //已经找到最短距离，标记1
                if(tmp_K.ID == k) {
                    tmp_K.flag = 1;
                    if(count == 1){
                        tmp_K.pre_node = src_embedID;
                    }
                }
            }

            for(Link tmp_l:in_linkstes){//距离更新
                if(tmp_l.getsrcid() == k){
                    for(Nodes td2:nodesets){
                        if(td2.ID ==tmp_l.getdstid()){
                            if(td2.flag == 0 &&  td2.dis > (min + tmp_l.cost)){
                                td2.dis = min + tmp_l.cost;
                                td2.pre_node = k;
                            }
                        }
                    }
                }
            }
        }

        //存结果
        int tmp_dst = dst_embedID;
        int tmp_src = Max;

        for(Nodes tmp:nodesets){
            if(tmp.ID == tmp_dst && tmp.dis!=Max){
                flag_success = 1;
                //while(tmp_src != src_embedID){
                while(tmp_dst != src_embedID){
                    for(Nodes rnode:nodesets){
                        if(rnode.ID == tmp_dst) {//找到目的节点
                            for(Link l:in_linkstes){//找到相应的关联边
                                if(l.getsrcid() == rnode.pre_node && l.getdstid()== rnode.ID){
                                    linksets.add(new Link(l.getid()));
                                    tmp_dst = rnode.pre_node;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        if(flag_success == 0) {
            linksets_fall.add(new Link(Max));
            // System.out.println("d 失败了，没找到路");
            for(Link lll:linksets_fall){
                // System.out.println("dji里的link " + lll.getid());
            }
            return linksets_fall;
        }else {
            return linksets;
        }


    }

    Set<Sfc> copySfcSet(Collection<Sfc> sets) throws Exception {
        Set<Sfc> res = new HashSet<>();
        for(Sfc sfc:sets){
            Sfc nf = (Sfc) sfc.deepClone();
            res.add(nf);
        }
        return res;
    }

    void MCstart() throws Exception{
        //打印资源的信息，图G的。网络信息
        this.osw.write("==============================================" + "\r\n");
        this.osw.write("===================before=====================" + "\r\n");
        this.osw.write("==============================================" + "\r\n");
        this.osw.write(G.GetInfo());

        //准备操作
        Graph G_tmp ;
        Graph G_return = new Graph();//结果存在在这里面返回
        Graph GMin = new Graph();//存每轮的最小可能的那个结果11.26

        Map<Integer,Graph> gSets = new HashMap<>();//把产生的新结果存入到这个集合里面。
        int gFlag = 0;//标志位，有可能结果不好导致Greturn无法更新。
        Graph dealedG = (Graph) G.deepClone();

        Set<Sfc> delSfcSets = new HashSet<>();//对my.sfset的拷贝，数据操作用这个set操作
        Set<Sfc> tmpSfcSets = new HashSet<>();//对每次更新的存储，最后赋给my.sfcsets
        Set<Sfc> MinSfcSets = new HashSet<>();//这是min结果对应的sets


        //MC启动,状态变化操作----》资源网络的更新Dcf
        //对每个点进行状态预变化，最终选取产生最好的结果的点进行VNF状态变化，其他点VNF状态不变
        for(int i=0;i<6;i++){
            Switch tmpSW = null;
            for(Switch tsw:dealedG.switchset){
                if(tsw.getID() == i){
                    tmpSW = tsw;
                    break;
                }
            }
        //for(Switch tmp_sw:dealedG.switchset){
            Random r = new Random();
            int r_id = r.nextInt(3);//从该sw的3个vnf里随机选取一个vnf来进行状态变化
            assert tmpSW != null;//assert关键字声明一个断言。如果表达式的结果为true，那么断言为真，并且无任何行动
            //如果表达式为false，则断言失败，则会抛出一个AssertionError对象。
            for(VNF tmp_v:tmpSW.VNFset){
                if(tmp_v.getID() == r_id){
                    tmp_v.reverseState();//反转状态
                    break;
                }
            }

            //复制sfc集合,保证循环里面每次处理的的sfc都是没有映射的干净的sfc
            delSfcSets.clear();
            delSfcSets = copySfcSet(this.sfcsets);


            //Dcf确定后，部署sfc
            //G_tmp = RandomDeploySFC(dealedG,delSfcSets);
            G_tmp = VDeploySFC(dealedG,delSfcSets,2);//1是正序，其他数字反序
            gSets.put(i,G_tmp);

            //转移概率*10
            G_tmp.qcf = 10*Math.pow(Math.E,beta*(G_tmp.getMaxUtility()-G.getMaxUtility())-1);
            if(G_tmp.qcf > 10) G_tmp.qcf = 10*1;//概率不能大于1


            if(i == 0){//这个是确保g每次都有更新和循环外的gFlag配合。
                G_return.cloneG(G_tmp);
                tmpSfcSets = copySfcSet(delSfcSets);

                //11.26
                GMin.cloneG(G_tmp);
                MinSfcSets = copySfcSet(delSfcSets);
            }
            //11.26
            if(i>0 && TimerValue(GMin) > TimerValue(G_tmp)){
                GMin.cloneG(G_tmp);
                MinSfcSets = copySfcSet(delSfcSets);
            }
            ///

            if(TimerValue(G_return) < TimerValue(G_tmp) ){
                G_return.cloneG(G_tmp);
                tmpSfcSets = copySfcSet(delSfcSets);
                if(gFlag == 0) gFlag = 1;
            }


            //恢复，把这次动作恢复
            dealedG = (Graph) G.deepClone();

        }
        GMin.existence = G.getExistence();
        G_return.existence = G.getExistence();
/*        //
        //对光链路层进行资源转化操作
        //选两个活跃节点建立或者拆除
        Graph dealedGO = new Graph();
        dealedGO.cloneG(G);

        Collection<Link> tmp_linksets = new HashSet<>();//返回映射结果的边集装到这个里面
        int flag_success_op = 1;//砍光路是否建立成功的。
        int flag_r1 = 0,flag_r2 = 0;//随机选两个节点
        Random r = new Random();
        int r1,r2,r_b;
        r1 = r.nextInt(dealedGO.getSwitchnum());
        r2 = r.nextInt(dealedGO.getSwitchnum());
        while(r1 == r2){
            r2 = r.nextInt(dealedGO.getSwitchnum());
        }

        Collection<Link> tmp_O_lin = new HashSet<>();
        r_b = r.nextInt(3);//任意选一个波长

        //看是否已经有光通路了
        int flag_on = 0;
        for(Link tmp_r2:dealedGO.rlinkset){
            if(tmp_r2.getsrcid() == r1 && tmp_r2.getdstid() == r2 && tmp_r2.getType() ==(50+r_b)){
                flag_on = 1;
            }
        }
        //更新操作，开了的关，关了的开。
        if(flag_on == 0){//没有光通路，建立光通路
            for(Olink tmp:dealedGO.olinkset){
                if(tmp.getWave() == r_b && tmp.getstate() == 0){
                    tmp_O_lin.add(new Link(tmp.getid(), tmp.getsrcid(), tmp.getdstid(), tmp.getBandwidth(), tmp.getType()));
                }
            }
            tmp_linksets = dijkstra(r1, r2, tmp_O_lin, dealedGO.getSwitchnum());//找出r1,r2之间的最短路
            for(Link l2:tmp_linksets){
                if(l2.getid() == Max){//无最短路
                    flag_success_op = 0;
                    // System.out.println("光通路建立这里，失败了");
                    break;
                }
            }

            if(flag_success_op == 1){//给光通路设置价格
                int tmp_price = 0;
                if(tmp_linksets.size() > 1){
                    tmp_price = (tmp_linksets.size()-1)*2 + 40;//收发节点40，中继节点2
                }else{
                    tmp_price = 40;
                }

                for(Link tmp_l:tmp_linksets){//建立通路
                    for(Olink tmp:dealedGO.olinkset){
                        if(tmp.getWave() == r_b && tmp.getid() == tmp_l.getid()){
                            tmp.setstate(1);
                            int tpID=0;
                            for(Link ll:dealedGO.rlinkset){
                                tpID += ll.getid();
                            }
                            tmp.rlinkID = tpID;//dealedG.rlinkset.size();//标记这些已用，并且标记其参与搭建的rlink
                            break;
                        }
                    }
                }
                dealedGO.rlinkset.add(new Link(dealedGO.rlinkset.size(), r1, r2, 2000, 50+r_b, tmp_price));
            }
        }else{//有光通路，拆除 //olink里存路径搭建信息和对应的rlink ID号，拆的时候按ID来排查拆除。
            int tmp_rid = Max;
            Iterator i = dealedGO.rlinkset.iterator();
            //Link tmp_rl = new Link();
            while(i.hasNext()){
                Link tmp_rl = (Link) i.next();
                if(tmp_rl.getType() == (50+r_b) && tmp_rl.getsrcid() == r1 &&tmp_rl.getdstid() == r2){
                    tmp_rid = tmp_rl.getid();
                    i.remove();
                }
            }
            for(Olink tmp:dealedGO.olinkset){
                if(tmp.rlinkID == tmp_rid && tmp.getWave()== r_b){
                    tmp.rlinkID = Max;
                    tmp.setstate(0);
                }
            }
        }
        //Dcf确定后，部署sfc
        G_tmp = deploy_SFC(dealedGO,sfcsets);

        if(TimerValue(G_return) < TimerValue(G_tmp)){
            G_return.cloneG(G_tmp);
        }

        //*/

        Random gr = new Random();//
        if(gr.nextDouble() < 1-TimerValue(GMin)){
            gFlag = 2;
        }

        if(gFlag == 0){//如果结果都不好，就随机选一个状态
            Random nr = new Random();
            G_return.cloneG(gSets.get(nr.nextInt(6)));
        }
        else if(gFlag == 2){
            sfcsets = copySfcSet(MinSfcSets);
            G = (Graph) GMin.deepClone();
        }else{
            //最终结果的拷贝更新。
            sfcsets = copySfcSet(tmpSfcSets);
            G = (Graph) G_return.deepClone();
        }


        this.osw.write("==============================================" + "\r\n");
        this.osw.write("====================after=====================" + "\r\n");
        this.osw.write("==============================================" + "\r\n");
        this.osw.write(G.GetInfo());
        this.osw.write("\r\n");

        printSfcState(this.osw);

    }

    public double TimerValue(Graph graph){
        double value ;
        double qf1_to_f2;
        //double index_1 ;
        //
        //System.out.println("graph.getMaxUtility()的值：" + graph.getMaxUtility());
        //
        if(graph.switchset.isEmpty()){
            //value = -9999999;
            value = 0;
            System.out.println("本次有空G" );
        }else{
            //qf1_to_f2 = Math.exp((0.001*beta*(graph.getMaxUtility()-5)));//-1000
            qf1_to_f2 = Math.pow(Math.E, beta*(graph.getMaxUtility() - G.getMaxUtility()));
            value = qf1_to_f2;//value = qf1_to_f2*graph.sfc_num;//先暂时这样。
        }

        //System.out.println("这次TimerValue的值：" + value);
        return value;
    }

    void printSfcState(OutputStreamWriter osw) throws IOException {
        for(Sfc sfc:this.sfcsets){
            osw.write("SFC-ID是："+sfc.ID+"===>>>>状态：" + sfc.getState());
            osw.write("\r\n");
        }
    }

    Graph RandomDeploySFC(Graph G, Collection<Sfc> SFCsets){//随机选择一个SW的点，映射该VNF。
        Graph Gdesfc = new Graph();
        Gdesfc.cloneG(G);

        Graph graph = new Graph();

        Collection<Link> tmpESets = new HashSet<>();//存带宽合适的边
        Collection<Link> DijESets ;//DijESets = new HashSet<>();
        Random r = new Random();

        for(Sfc tmp_sfc:SFCsets){
            //取出一条sfc进行映射操作
            graph.cloneG(Gdesfc);//每次映射先复制一份网络，对复制的网络操作
            //先映射点
            int flagSfc=1;
            for(VNF sfcVNF:tmp_sfc.VNFset){
                int enID = r.nextInt(7);
                Set<Integer> set = new HashSet<>();
                while (sfcVNF.embedID == Max && set.size() < 6){
                    enVNF(graph.switchset,enID,sfcVNF);
                    if(sfcVNF.embedID != Max) break;
                    else{
                        set.add(enID);
                        enID = r.nextInt(7);
                    }
                }

                if(sfcVNF.embedID == Max){
                    flagSfc = 0;
                    break;
                }
            }
            if(flagSfc == 0) {
//                System.out.println("它的点映射失败了！ ");
                continue;//失败就跳过这次映射
            }
//            System.out.println("它的点映射成功了！ 开始进行边映射");
            //映射边
            int LFindFlag;
            for(Link sfcLink:tmp_sfc.linkset){
                LFindFlag = 0;
                for(VNF tmpVNFsrc:tmp_sfc.VNFset){
                    for(VNF tmpVNFdst:tmp_sfc.VNFset){
                        if(tmpVNFsrc.getID() == sfcLink.getsrcid() && tmpVNFdst.getID() == sfcLink.getdstid()){
                            LFindFlag = 1;
                            if(tmpVNFsrc.embedID == tmpVNFdst.embedID && tmpVNFdst.embedID != Max){
                                sfcLink.setstate(1);//表示映射成功
//                                System.out.println("VNFsrc: "+ tmpVNFsrc.getID() + " VNFdst" + tmpVNFdst.getID() +"映射到了同一个点");
                            }//else{
                            if(tmpVNFsrc.embedID != tmpVNFdst.embedID && tmpVNFdst.embedID != Max && tmpVNFsrc.embedID != Max){
//                                System.out.println("VNFsrc: "+ tmpVNFsrc.getID() + " VNFdst" + tmpVNFdst.getID() +"没有映射到了同一个点");
                                for(Link gRlink:graph.rlinkset){//找出带宽合适的边
                                    if((gRlink.getBandwidth() - gRlink.cost) >= sfcLink.getBandwidth()){
                                        tmpESets.add(gRlink);
                                    }
                                }
                                //找路dijkstra
                                DijESets = dijkstra(tmpVNFsrc.embedID,tmpVNFdst.embedID,tmpESets,6);
                                for(Link dlink:DijESets){
                                    if(dlink.getid() == Max){
//                                        System.out.println("两个点找路失败了！");
                                        flagSfc = 0;//映射失败
                                    }
                                }
                                if(flagSfc != 0){//找路成功，更新资源
                                    sfcLink.setstate(1);
                                    /*
                                   System.out.println("找路成功了");
                                   System.out.println("这次边映射用了这些边：");
                                   */
                                    for(Link e1:DijESets){
//                                      System.out.println(e1.getid());
                                        for(Link e2:graph.rlinkset){
                                            if(e1.getid() == e2.getid()){
                                                sfcLink.usedLinkSet.add(e2);//新加，把使用过的边的信息存入，sfclink.
                                                e2.cost += sfcLink.getBandwidth();
                                                break;
                                            }
                                        }
                                    }
                                }
                                DijESets.clear();
                                break;
                            }
                        }
                    }
                    if(LFindFlag == 1) break;
                }
                tmpESets.clear();
                if(flagSfc == 0) break;
            }

            if(flagSfc == 0){
                //continue;
            }else{
                Gdesfc.cloneG(graph);
                tmp_sfc.setState(1);
            }
        }

        return Gdesfc;
    }

    public void enVNF(Collection<Switch> swSet,int ID,VNF sfcVNF){
        for(Switch sw:swSet){
            if(sw.getID() == ID){
                if(sw.getstate() == 1){
                    for(VNF swVNF:sw.VNFset){
                        if(swVNF.getVNFtype() == sfcVNF.getVNFtype() && (swVNF.VNFcapacity-swVNF.cost)>=sfcVNF.VNFcapacity &&swVNF.getState() == 1){
                            swVNF.cost += sfcVNF.VNFcapacity;
                            sfcVNF.embedID = sw.getID();
                            sfcVNF.embedVnfID = swVNF.getID();
                        }
                    }
                }
            }
            break;
        }
    }

    Graph VDeploySFC(Graph G, Collection<Sfc> SFCsets,int flag){
        Graph G_desfc = new Graph();
        G_desfc.cloneG(G);

        Graph graph = new Graph();

        Collection<Link> tmpESets = new HashSet<>();//存带宽合适的边
        Collection<Link> DijESets ;//DijESets = new HashSet<>();

        for(Sfc tmp_sfc:SFCsets){
            //取出一条sfc进行映射操作
            graph.cloneG(G_desfc);//每次映射先复制一份网络，对复制的网络操作
            //先映射点
            int flagSfc=1;
            for(VNF sfcVNF:tmp_sfc.VNFset){
                List<Switch> SWList = new ArrayList<>(graph.switchset);
                if(flag == 1){
                    SWList.sort(new Comparator<Switch>() {
                        @Override
                        public int compare(Switch o1, Switch o2) {
                            return Integer.compare(o1.getID(), o2.getID());
                        }
                    });
                }else{
                    SWList.sort(new Comparator<Switch>() {
                        @Override
                        public int compare(Switch o1, Switch o2) {
                            return Integer.compare(o2.getID(), o1.getID());
                        }
                    });
                }

                ///
                for(Switch sw:SWList){
                    if(sw.getstate() == 1){
                        List<VNF> VNFList = new ArrayList<>(sw.VNFset);
                        VNFList.sort(new Comparator<VNF>() {
                            @Override
                            public int compare(VNF o1, VNF o2) {
                                return Integer.compare(o1.getID(), o2.getID());
                            }
                        });
                        for(VNF swVNF:VNFList){
                            if(swVNF.getVNFtype() == sfcVNF.getVNFtype() && (swVNF.VNFcapacity-swVNF.cost)>=sfcVNF.VNFcapacity &&swVNF.getState() == 1){
                                swVNF.cost += sfcVNF.VNFcapacity;
                                sfcVNF.embedID = sw.getID();
                                sfcVNF.embedVnfID = swVNF.getID();
                                break;
                            }
                        }
                    }
                    if(sfcVNF.embedID != Max) break;
                }
                /*for(Switch sw:graph.switchset){
                    if(sw.getstate() == 1){
                        for(VNF swVNF:sw.VNFset){
                            if(swVNF.getVNFtype() == sfcVNF.getVNFtype() && (swVNF.VNFcapacity-swVNF.cost)>=sfcVNF.VNFcapacity &&swVNF.getState() == 1){
                                swVNF.cost += sfcVNF.VNFcapacity;
                                sfcVNF.embedID = sw.getID();
                                sfcVNF.embedVnfID = swVNF.getID();
                                break;
                            }
                        }
                    }
                    if(sfcVNF.embedID != Max) break;
                }*/
                if(sfcVNF.embedID == Max){
                    flagSfc = 0;
                    break;
                }
            }
            if(flagSfc == 0) {
//                System.out.println("它的点映射失败了！ ");
                continue;//失败就跳过这次映射
            }
//            System.out.println("它的点映射成功了！ 开始进行边映射");
            //映射边
            int LFindFlag;
            for(Link sfcLink:tmp_sfc.linkset){
                LFindFlag = 0;
                for(VNF tmpVNFsrc:tmp_sfc.VNFset){
                    for(VNF tmpVNFdst:tmp_sfc.VNFset){
                        if(tmpVNFsrc.getID() == sfcLink.getsrcid() && tmpVNFdst.getID() == sfcLink.getdstid()){
                            LFindFlag = 1;
                            if(tmpVNFsrc.embedID == tmpVNFdst.embedID && tmpVNFdst.embedID != Max){
                                sfcLink.setstate(1);//表示映射成功
//                                System.out.println("VNFsrc: "+ tmpVNFsrc.getID() + " VNFdst" + tmpVNFdst.getID() +"映射到了同一个点");
                            }//else{
                            if(tmpVNFsrc.embedID != tmpVNFdst.embedID && tmpVNFdst.embedID != Max && tmpVNFsrc.embedID != Max){
//                                System.out.println("VNFsrc: "+ tmpVNFsrc.getID() + " VNFdst" + tmpVNFdst.getID() +"没有映射到了同一个点");
                                for(Link gRlink:graph.rlinkset){//找出带宽合适的边
                                    if((gRlink.getBandwidth() - gRlink.cost) >= sfcLink.getBandwidth()){
                                        tmpESets.add(gRlink);
                                    }
                                }
                                //找路dijkstra
                                DijESets = dijkstra(tmpVNFsrc.embedID,tmpVNFdst.embedID,tmpESets,6);
                                for(Link dlink:DijESets){
                                    if(dlink.getid() == Max){
//                                        System.out.println("两个点找路失败了！");
                                        flagSfc = 0;//映射失败
                                    }
                                }
                                if(flagSfc != 0){//找路成功，更新资源
                                    sfcLink.setstate(1);

                                    for(Link e1:DijESets){
//                                      System.out.println(e1.getid());
                                        for(Link e2:graph.rlinkset){
                                            if(e1.getid() == e2.getid()){
                                                sfcLink.usedLinkSet.add(e2);//新加，把使用过的边的信息存入，sfclink.
                                                e2.cost += sfcLink.getBandwidth();
                                                break;
                                            }
                                        }
                                    }
                                }
                                DijESets.clear();
                                break;
                            }
                        }
                    }
                    if(LFindFlag == 1) break;
                }
                tmpESets.clear();
                if(flagSfc == 0) break;
            }

            if(flagSfc == 0){
                //continue;
            }else{
                G_desfc.cloneG(graph);
                tmp_sfc.setState(1);
            }
        }

        return G_desfc;
    }
}