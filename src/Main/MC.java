package Main;

import SFC.Sfc;
import graph.*;

import java.util.*;

/**
 * @ author bannerblade
 * @ date 2020/3/25
 */


public class MC {
    private static final int Max = 100000 ;
    private static final double beta = 1;
    public Graph G = new Graph();
    public Collection<Sfc> sfcsets = new HashSet<>();
    public Set<Sfc> resSfcSets = new HashSet<>();////ResSfcsets装关联sfc

    public MC(Graph Ga, Collection<Sfc> sfcset){
        G.cloneG(Ga);
        sfcsets.addAll(sfcset);
        resSfcSets.addAll(sfcset);
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

    Graph deploy_SFC(Graph G, Collection<Sfc> SFCsets){
        Graph G_desfc = new Graph();
        G_desfc.cloneG(G);

        Graph graph = new Graph();

        Collection<Link> tmpESets = new HashSet<>();//存带宽合适的边
        Collection<Link> DijESets ;//DijESets = new HashSet<>();

        for(Sfc tmp_sfc:SFCsets){
/*            System.out.println();
            System.out.println("==================================");
            System.out.println("SFC的ID: " + tmp_sfc.ID + " 的映射");*/
            //取出一条sfc进行映射操作
            graph.cloneG(G_desfc);//每次映射先复制一份网络，对复制的网络操作
            //先映射点
            int flagSfc=1;
            for(VNF sfcVNF:tmp_sfc.VNFset){
                for(Switch sw:graph.switchset){
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
                G_desfc.cloneG(graph);
                tmp_sfc.setState(1);
            }
        }

        return G_desfc;
    }

    void MCstart() throws Exception{
        Graph G_tmp ;
        Graph G_return = new Graph();//结果存在在这里面返回

        //Graph dealedG = (Graph) G.deepClone();
        Graph dealedG = new Graph();;
        dealedG.cloneG(G);

        Set<Sfc> delSfcSets = new HashSet<>();//对my.sfset的拷贝，数据操作用这个set操作
        Set<Sfc> tmpSfcSets = new HashSet<>();//对每次更新的存储，最后赋给my.sfcsets

        int count=0;
        //MC启动,资源开闭（更新操作在这里进行）----》资源网络的更新Dcf
        for(Switch tmp_sw:dealedG.switchset){//对所有点进行资源转换操作
/*          System.out.println();
            System.out.println();
            System.out.println("=====^^^^^^^^^^^=====");
            System.out.println("本次资源更新操作开始");*/
            int changedVnfType = -1;//变化的一个vnf type
            Random r = new Random();
            int r_id = r.nextInt(3);
            for(VNF tmp_v:tmp_sw.VNFset){//节点VNF状态更新
                if(tmp_v.getID() == r_id){//随机选一个VNF
                    changedVnfType = tmp_v.getVNFtype();//变化的一个vnf type
                    if(tmp_v.getState() == 1){
                        tmp_v.setState(0);
                    }else{
                        tmp_v.setState(1);
                    }
                }
            }

            //复制sfc集合
            delSfcSets.clear();
            for(Sfc sfc:sfcsets){
                Sfc nf = (Sfc) sfc.deepClone();
                delSfcSets.add(nf);
            }
            //找本次关联sfc集合；
            this.resSfcSets.clear();
            for(Sfc tmpSfc:delSfcSets){
                for(VNF sfcVNF:tmpSfc.VNFset){
                    if(sfcVNF.getVNFtype() == changedVnfType){
                        this.resSfcSets.add(tmpSfc);
                        break;
                    }
                }
            }
 /*           //更新图G，找本次关联sfc用掉的资源返还回去。
            for(Sfc tmpSfc:this.resSfcSets) {
                if (tmpSfc.getState() == 0) continue;
                for (VNF sfcVNF : tmpSfc.VNFset) {//sfcVNF映射初始化
                    if(sfcVNF.getVNFtype() != changedVnfType) continue;
                    for (Switch Gsw : dealedG.switchset) {
                        if (Gsw.getID() == sfcVNF.embedID) {
                            sfcVNF.embedID = Max;
                            for (VNF Gvnf : Gsw.VNFset) {
                                if (Gvnf.getID() == sfcVNF.embedVnfID) {
                                    Gvnf.cost -= sfcVNF.VNFcapacity;
                                    sfcVNF.embedVnfID = Max;
                                }
                            }
                        }
                    }
                }
                tmpSfc.setState(0);//SFC状态重新标记，初始化
            }*/

            //更新图G，找本次关联sfc用掉的资源返还回去。
            for(Sfc tmpSfc:this.resSfcSets){
                if(tmpSfc.getState() == 0) continue;
                for(VNF sfcVNF:tmpSfc.VNFset){//sfcVNF映射初始化
                    for(Switch Gsw:dealedG.switchset){
                        if(Gsw.getID() == sfcVNF.embedID){
                            sfcVNF.embedID = Max;
                            for(VNF Gvnf:Gsw.VNFset){
                                if(Gvnf.getID() == sfcVNF.embedVnfID){
                                    Gvnf.cost -=  sfcVNF.VNFcapacity;
                                    sfcVNF.embedVnfID = Max;
                                }
                            }
                        }
                    }
                }
                for(Link sfcLink:tmpSfc.linkset){//sfcVNF映射初始化//sfclink映射初始化
                    for(Link gLink:sfcLink.usedLinkSet){
                        for(Link e:dealedG.rlinkset){
                            if(gLink.getid() == e.getid()){
                                //sfcLink.usedLinkSet.remove(e);//删除存入的边信息，sfclink.
                                e.cost -= sfcLink.getBandwidth();
                                break;
                            }
                        }
                    }
                    sfcLink.setstate(0);//有向边状态重新标记，初始化
                    sfcLink.usedLinkSet.clear();//删除存入的边信息，初始化。
                }
                tmpSfc.setState(0);//SFC状态重新标记，初始化
            }

            //Dcf确定后，部署sfc
            G_tmp = deploy_SFC(dealedG,resSfcSets);//G_tmp = deploy_SFC(G,sfcsets);

            //tmpSfcSets.clear();
            if(count == 0){//这个是初始化相关的。保证必须更新一次，后期考虑如何去掉。
                G_return.cloneG(G_tmp);
                tmpSfcSets.clear();
                tmpSfcSets.addAll(resSfcSets);
            }

            if(TimerValue(G_return) < TimerValue(G_tmp) ){
                G_return.cloneG(G_tmp);
                tmpSfcSets.clear();
                tmpSfcSets.addAll(resSfcSets);
            }

            //恢复，把这次动作恢复

            dealedG = (Graph) G.deepClone();

            count++;
        }
        sfcsets.removeAll(tmpSfcSets);
        sfcsets.addAll(tmpSfcSets);
        G = (Graph) G_return.deepClone();
    }

    public double TimerValue(Graph graph){
            double value ;
            double qf1_to_f2;
            //double index_1 ;
            //
            //System.out.println("graph.getMaxUtility()的值：" + graph.getMaxUtility());
            //
            if(graph.switchset.isEmpty()){
                value = -9999999;
                System.out.println("本次有空G" );
            }else{
                qf1_to_f2 = Math.exp((0.001*beta*(graph.getMaxUtility()-1000)));
                value = qf1_to_f2;//value = qf1_to_f2*graph.sfc_num;//先暂时这样。
            }

            //System.out.println("这次TimerValue的值：" + value);
            return value;
    }

}
