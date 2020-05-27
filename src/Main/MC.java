package Main;

import SFC.Sfc;
import graph.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * @ author bannerblade
 * @ date 2020/3/25
 */


public class MC {
    private static final int Max = 100000 ;
    private static final double beta = 1;
    public Graph G = new Graph();
    public Collection<Sfc> sfcsets = new HashSet<>();

    public MC(Graph Ga, Collection<Sfc> sfcset){
        G.G_copy(Ga);
        sfcsets.addAll(sfcset);
    }

    public MC(){}

    void MC_ini(){
        //初始化MC，最开始的启动
        //节点初始化，开启了全部节点的VNF和电链路
        for(Switch tmp:G.switchset){
            tmp.setstate(1);
            if(tmp.getID() == 0 ||tmp.getID() == 2 ||tmp.getID() == 4 ){
                for(VNF tmp_vnf:tmp.VNFset){
                    tmp_vnf.setState(1);//tmp_vnf.setState(1);
                }
            }
        }
        //边初始化
        for(Elink tmp_elink:G.elinkset){
            tmp_elink.setstate(1);
            G.rlinkset.add(tmp_elink);
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
        G_desfc.G_copy(G);

        Graph graph = new Graph();

        Collection<Link> tmpESets = new HashSet<>();//存带宽合适的边
        Collection<Link> DijESets = new HashSet<>();//存带宽合适的边

        for(Sfc tmp_sfc:SFCsets){
            //取出一条sfc进行映射操作
            graph.G_copy(G_desfc);//每次映射先复制一份网络，对复制的网络操作
            //先映射点
            int flagSfc=1;
            for(VNF sfcVNF:tmp_sfc.VNFset){
                for(Switch sw:graph.switchset){
                    if(sw.getstate() == 1){
                        for(VNF swVNF:sw.VNFset){
                            if(swVNF.getVNFtype() == sfcVNF.getVNFtype() && (swVNF.VNFcapacity-swVNF.cost)>=sfcVNF.VNFcapacity &&swVNF.getState() == 1){
                                swVNF.cost += sfcVNF.VNFcapacity;
                                sfcVNF.embedID = sw.getID();
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
            if(flagSfc == 0) continue;//失败就跳过这次映射
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
                            }else{
                                for(Link gRlink:graph.rlinkset){//找出带宽合适的边
                                    if((gRlink.getBandwidth() - gRlink.cost) >= sfcLink.getBandwidth()){
                                        tmpESets.add(gRlink);
                                    }
                                }
                                //找路dijkstra(int src_embedID, int dst_embedID, Collection<Link> in_linkstes, int node_num)
                                DijESets = dijkstra(tmpVNFsrc.embedID,tmpVNFdst.embedID,tmpESets,6);
                                for(Link dlink:DijESets){
                                    if(dlink.getid() == Max){
                                        flagSfc = 0;//映射失败
                                    }
                                }
                                if(flagSfc != 0){//找路成功，更新资源
                                    sfcLink.setstate(1);
                                    ///
                                    System.out.println("SFC的ID: " + tmp_sfc.ID + "他的ID是 "+sfcLink.getid() + "的边映射");
                                    System.out.println("用了这些边：");
                                    ///
                                    for(Link e1:DijESets){
                                        //
                                        System.out.println(e1.getid());
                                        //
                                        for(Link e2:graph.rlinkset){
                                            if(e1.getid() == e2.getid()){
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
                continue;
            }else{
                G_desfc.G_copy(graph);
                G_desfc.sfc_num++;
                tmp_sfc.setState(1);
            }


        }

        return G_desfc;
    }


    void MCstart() throws Exception{
        Graph G_tmp = new Graph();
        Graph G_return = new Graph();//结果存在在这里面返回
        
        Graph G_new = new Graph();

        int count=0;
        //MC启动,资源开闭（更新操作在这里进行）----》资源网络的更新Dcf
        for(Switch tmp_sw:G.switchset){//对所有点进行资源转换操作
            Random r = new Random();
            int r_id = r.nextInt(3);
            for(VNF tmp_v:tmp_sw.VNFset){//节点VNF状态更新
                if(tmp_v.getID() == r_id){//随机选一个VNF
                    if(tmp_v.getState() == 1){
                        tmp_v.setState(0);
                    }else{
                        tmp_v.setState(1);
                    }
                }
            }

            //Dcf确定后，部署sfc
            G_tmp = deploy_SFC(G,sfcsets);


            if(count == 0){
                G_return.G_copy(G_tmp);
            }

            if(TimerValue(G_return) < TimerValue(G_tmp) ){//&& G_tmp.getMaxUtility() >= 0
                G_return.G_copy(G_tmp);
            }

            //恢复，把这次动作恢复
            for(VNF tmp_v:tmp_sw.VNFset){//节点VNF状态更新
                if(tmp_v.getID() == r_id){//随机选一个VNF
                    if(tmp_v.getState() == 1){
                        tmp_v.setState(0);
                    }else{
                        tmp_v.setState(1);
                    }
                }
            }
            count++;
        }

        G = (Graph) G_return.deepClone();
    }

    public double TimerValue(Graph graph){
            double value = 0;
            double qf1_to_f2;
            double index_1 ;
            //
            //System.out.println("graph.getMaxUtility()的值：" + graph.getMaxUtility());
            //
            if(graph.switchset.isEmpty()){
                value = -9999999;
                System.out.println("本次有空G" );
            }else{
                qf1_to_f2 = Math.exp((0.001*beta*(graph.getMaxUtility()-1000)));
                value = qf1_to_f2*graph.sfc_num;//先暂时这样。
            }

            //System.out.println("这次TimerValue的值：" + value);
            return value;
    }

}
