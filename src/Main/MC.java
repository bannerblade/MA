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
            if(tmp.getID() == 0 ||tmp.getID() == 2 ||tmp.getID() == 4 ){
                tmp.setstate(1);//tmp.setstate(1);
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
        //Collection<Link> linksets_fall = null;//若失败，返回这个结果
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

    Graph deploy_piar(VNF src , VNF dst, Link link, Collection<VNF> VNFsets, Graph G){
        //映射一对piar
        ///这个VNFsets 存映射过的VNF。
        Graph G_copy = new Graph();
        Graph G_self = new Graph();
        G_copy.G_copy(G);//G_copy = G;
        G_self.G_copy(G);//G_self = G;//失败返回一个和G一样的图
        int flag_s = 0, flag_d = 0;
        int flag_success = 1;//看这次映射是否成功

        if( !VNFsets.isEmpty() ){
            for(VNF tmp:VNFsets){
                if(tmp.getID() == src.getID()){
                    flag_s = 1;
                }
                if(tmp.getID() == dst.getID()){
                    flag_d = 1;
                }
            }
        }

        //dst没被映射
        if(flag_d == 0){
            for(Switch tmp_s:G_copy.switchset){
                if(tmp_s.getstate() == 1){
                    for(VNF tmp_vnf:tmp_s.VNFset){
                        if((tmp_vnf.VNFcapacity - tmp_vnf.cost) >= dst.VNFcapacity && tmp_vnf.getVNFtype() == dst.getVNFtype() && tmp_vnf.getState() == 1){
                            tmp_vnf.cost += dst.VNFcapacity;//图G——copy扣除消耗资源
                            VNFsets.add(dst);//标记映射
                            flag_d = 1;
                            dst.embedID = tmp_s.getID();//在VNF上登记其映射到的点的ID
                            break;
                        }
                    }
                }
                if(flag_d == 1) break;
            }
        }
        //src没有被映射了
        if(flag_s == 0 ){
            for(Switch tmp_s:G_copy.switchset){
                if(tmp_s.getstate() == 1){
                    for(VNF tmp_vnf:tmp_s.VNFset){
                        if((tmp_vnf.VNFcapacity - tmp_vnf.cost) >= src.VNFcapacity && tmp_vnf.getVNFtype() == src.getVNFtype() && tmp_vnf.getState() == 1){
                            tmp_vnf.cost += src.VNFcapacity;//图G——copy扣除消耗资源
                            VNFsets.add(src);//标记映射
                            flag_s = 1;
                            src.embedID = tmp_s.getID();//在VNF上登记其映射到的点的ID
                            break;
                        }
                    }
                }
                if(flag_s == 1) break;
            }
        }
        //src和dst都被映射了
        if(flag_s == 1 && flag_d == 1){
            if(src.embedID == dst.embedID && src.embedID != Max){//映射到一个点上了
                link.setstate(1);
            }else{//没有映射到一个点
                Collection<Link> tmp_e_sets = new HashSet<>();//存带宽合适的边
                for(Link tmp_e:G_copy.rlinkset){
                    if((tmp_e.getBandwidth()-tmp_e.cost) >= link.getBandwidth()){
                        tmp_e_sets.add(tmp_e);
                    }
                }
                Collection<Link> tmp_linksets = new HashSet<>();//返回映射结果的边集装到这个里面
                tmp_linksets = dijkstra(src.embedID,dst.embedID,tmp_e_sets,G_copy.getSwitchnum());//输入源目的VNF和边集，返回路径边集合

////
                for(Link ll:tmp_linksets){
                    if(ll.getid() == Max){
                        flag_success = 0;//映射失败
                    }
                }

                if(flag_success == 0){
                    System.out.println("mark-2, dijsktra没用找到通路，失败了");
                }else {
                   // System.out.println("mark-3, 成功了。有边：" + tmp_linksets.size());
                    for(Link e1:tmp_linksets){
                        for(Link e2:G_copy.rlinkset){
                            if(e1.getid() == e2.getid()){
                                e2.cost += link.getBandwidth();
                            }
                        }
                    }
                }
 ////
            }
        }else {//到最后，两个点有一个没有被映射，则失败
            flag_success = 0;
        }

        if(flag_success == 1) {
            G_copy.em_flag = 1;
            return G_copy;
        }else {//失败
            G_self.em_flag = 0;
            //System.out.println("本次pair映射失败了");
            return G_self;
        }
    }

    Graph deploy_SFC(Graph G, Collection<Sfc> SFCsets){
        Graph G_copy = new Graph();//映射成功会让G = G_copy
        G_copy.G_copy(G);//G_copy = G;

        for(Sfc tmp_sfc:SFCsets){
            //取出一条sfc进行映射操作
            //从link找VNF
            Collection<VNF> embeded_VNFsets = new HashSet<>();//这个存本次映射，SFC映射了的的VNF，标记VNF映射的
            int flag_sfc = 1;//看本次映射是否成功的

            Graph G_tp = new Graph();//中间变量
            G_tp.G_copy(G_copy);

            for(Link tmp_lin:tmp_sfc.linkset){//取出这条sfc的每一个有向link进行映射
                for(VNF tmp_v1:tmp_sfc.VNFset){
                    for(VNF tmp_v2:tmp_sfc.VNFset){
                        if(tmp_v1.getID() == tmp_lin.getsrcid() && tmp_v2.getID() == tmp_lin.getdstid()){
                            Graph G_tmp ;//中间变量,5/5
                            G_tmp = deploy_piar(tmp_v1, tmp_v2 , tmp_lin, embeded_VNFsets, G_tp);
                            //若映射成功，下面是对g_tp资源更新
                            if(G_tmp.em_flag == 1){

                                Graph G_copy_2 = new Graph();
                                G_copy_2.G_copy(G_tp);//G_copy_2这给没什么用，不用管。
                                //G_copy_2目的是把映射了的VNF装进embeded_VNFsets

                                tmp_lin.setstate(1);//表示SFC的这条边映射成功了

                                G_tp.G_copy(G_tmp);//中间变量更新

                                //下面这堆操作是，把映射的VNF记录其映射的点。这可以用pair再图上操作。
                                int flag_s = 0, flag_d = 0;
                                if( !embeded_VNFsets.isEmpty() ){
                                    for(VNF tmp:embeded_VNFsets){
                                        if(tmp.getID() == tmp_v1.getID()){
                                            flag_s = 1;//源节点已经映射
                                        }
                                        if(tmp.getID() == tmp_v2.getID()){
                                            flag_d = 1;//目的节点已经映射
                                        }
                                    }
                                }
                                //dst没被映射,把dst映射信息写入
                                if(flag_d == 0){
                                    for(Switch tmp_s:G_copy_2.switchset){
                                        if(tmp_s.getstate() == 1){
                                            for(VNF tmp_vnf:tmp_s.VNFset){
                                                if((tmp_vnf.VNFcapacity - tmp_vnf.cost) >= tmp_v2.VNFcapacity && tmp_vnf.getVNFtype() == tmp_v2.getVNFtype() && tmp_vnf.getState() == 1){
                                                    tmp_vnf.cost += tmp_v2.VNFcapacity;//图G——copy扣除消耗资源
                                                    embeded_VNFsets.add(tmp_v2);//标记映射
                                                    flag_d = 1;
                                                    tmp_v2.embedID = tmp_s.getID();//在VNF上登记其映射到的点的ID
                                                    break;
                                                }
                                            }
                                        }
                                        if(flag_d == 1) break;
                                    }
                                }
                                //src更新映射信息
                                if(flag_s == 0 ){
                                    for(Switch tmp_s:G_copy_2.switchset){
                                        if(tmp_s.getstate() == 1){
                                            for(VNF tmp_vnf:tmp_s.VNFset){
                                                if((tmp_vnf.VNFcapacity - tmp_vnf.cost) >= tmp_v1.VNFcapacity && tmp_vnf.getVNFtype() == tmp_v1.getVNFtype() && tmp_vnf.getState() == 1){
                                                    tmp_vnf.cost += tmp_v1.VNFcapacity;//图G——copy扣除消耗资源
                                                    embeded_VNFsets.add(tmp_v1);//标记映射
                                                    flag_s = 1;
                                                    tmp_v1.embedID = tmp_s.getID();//在VNF上登记其映射到的点的ID
                                                    break;
                                                }
                                            }
                                        }
                                        if(flag_s == 1) break;
                                    }
                                }
                            }else{
                                flag_sfc = 0;
                                //System.out.print("映射失败了");
                                //break;//本次映射失败
                            }
                            break;
                        }
                    }
                    if(flag_sfc == 0){
                        break;
                    }
                }
                if(flag_sfc == 0){
                    break;
                }
            }
            if(flag_sfc == 1){
                G_copy.G_copy(G_tp);
                G_copy.sfc_num++;//映射成功加1
            }
        }
        return G_copy;
    }


    Graph MCstart(){
        Graph G_tmp = new Graph();
        Graph G_return = new Graph();//结果存在在这里面返回

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

            if(G_tmp.getMaxUtility() < 0 && tmp_sw.getID() == 0){
                G_return.G_copy(G_tmp);
            }


            if(TimerValue(G_return) < TimerValue(G_tmp) && G_tmp.getMaxUtility() >= 0){
                G_return.G_copy(G_tmp);
            }



        }

        //对光链路层进行资源转化操作
        //选两个活跃节点建立或者拆除
        Collection<Link> tmp_linksets = new HashSet<>();//返回映射结果的边集装到这个里面
        int flag_success_op = 1;//砍光路是否建立成功的。
        int flag_r1 = 0,flag_r2 = 0;//随机选两个节点
        Random r = new Random();
        int r1,r2,r_b;
        r1 = r.nextInt(G.getSwitchnum());
        r2 = r.nextInt(G.getSwitchnum());
        while(r1 == r2){
            r2 = r.nextInt(G.getSwitchnum());
        }

        Collection<Link> tmp_O_lin = new HashSet<>();
        r_b = r.nextInt(3);//任意选一个波长

        //看是否已经有光通路了
        int flag_on = 0;
        for(Link tmp_r2:G.rlinkset){
            if(tmp_r2.getsrcid() == r1 && tmp_r2.getdstid() == r2 && tmp_r2.getType() ==(50+r_b)){
                flag_on = 1;
            }
        }
        //更新操作，开了的关，关了的开。
        if(flag_on == 0){//没有光通路，建立光通路
            for(Olink tmp:G.olinkset){
                if(tmp.getWave() == r_b){
                    tmp_O_lin.add(new Link(tmp.getid(), tmp.getsrcid(), tmp.getdstid(), tmp.getBandwidth(), tmp.getType()));
                }
            }
            tmp_linksets = dijkstra(r1, r2, tmp_O_lin, G.getSwitchnum());//找出r1,r2之间的最短路
            for(Link l2:tmp_linksets){
                if(l2.getid() == Max){//无最短路
                    flag_success_op = 0;
                   // System.out.println("光通路建立这里，失败了");
                    break;
                }
            }

            if(flag_success_op == 1){
                int tmp_price = 0;
                if(tmp_linksets.size() > 1){
                    tmp_price = (tmp_linksets.size()-2)*2 + 40;
                }else{
                    tmp_price = 40;
                }

                for(Link tmp_l:tmp_linksets){//建立通路
                    for(Olink tmp:G.olinkset){
                        if(tmp.getWave() == r_b && tmp.getid() == tmp_l.getid()){
                            tmp.setstate(1);
                            tmp.rlinkID = G.rlinkset.size();//标记这些已用，并且标记其参与搭建的rlink
                        }
                    }
                }
                G.rlinkset.add(new Link(G.rlinkset.size(), r1, r2, 2000, 50+r_b, tmp_price));
            }
        }else{//有光通路，拆除 //olink里存路径搭建信息和对应的rlink ID号，拆的时候按ID来排查拆除。
            int tmp_rid = Max;
            Iterator i = G.rlinkset.iterator();
            Link tmp_rl = new Link();
            while(i.hasNext()){
                tmp_rl = (Link) i.next();
                if(tmp_rl.getType() == (50+r_b)){
                    tmp_rid = tmp_rl.getid();
                    i.remove();
                }
            }
            for(Olink tmp:G.olinkset){
                if(tmp.rlinkID == tmp_rid && tmp.getWave()== r_b){
                    tmp.setstate(0);
                }
            }
        }
        //Dcf确定后，部署sfc
        G_tmp = deploy_SFC(G,sfcsets);


        if(TimerValue(G_return) < TimerValue(G_tmp) && G_tmp.getMaxUtility() >= 0){
               G_return.G_copy(G_tmp);
        }


        return G_return;
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
