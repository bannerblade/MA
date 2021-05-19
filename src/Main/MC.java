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

    public int flagV2=-1;
    public int flagV1=-1;
    public int flagSW1=-1;
    public int flagSW2=-1;
    public int enFlag = 1;

    public int flagOPath1=0;
    public int flagOPath2=0;

    public MC(Graph Ga, Collection<Sfc> sfcset){
        G.cloneG(Ga);
        sfcsets.addAll(sfcset);
    }

    void MC_ini(){
        //初始化MC，最开始的启动
        //节点初始化，开启了全部节点的VNF和电链路
        for(Switch tmp:G.switchset){
            tmp.setstate(1);
            if(tmp.getID()%4 == 0 ){
                for(VNF tmp_vnf:tmp.VNFset){
                    if(tmp_vnf.getID() == 1 || tmp_vnf.getID() == 2) tmp_vnf.setState(1);
                }
            }/*else{
                for(VNF tmp_vnf:tmp.VNFset){
                    if(tmp_vnf.getID() == 1) tmp_vnf.setState(1);
                }
            }*/
        }
        //边初始化1
        for(Elink tmpElink:G.elinkset){
            tmpElink.setstate(1);
            G.rlinkset.add(tmpElink);
        }
        /*//边初始化2
        for(Olink tmpElink:G.olinkset){
            tmpElink.setstate(0);
            G.rlinkset.add(tmpElink);
        }*/
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
            Sfc sc = new Sfc();
            sc.ID = sfc.ID;
            sc.setState(sfc.getState());

            for(VNF nf:sfc.VNFset){
                VNF tVnf = new VNF(nf.getID(),nf.getVNFtype(),nf.getVNFcapacity());
                tVnf.cost = nf.cost;
                tVnf.setPrice(nf.getprice());
                tVnf.setState(nf.getState());
                tVnf.embedID = nf.embedID;
                tVnf.embedVnfID = nf.embedVnfID;

                sc.VNFset.add(tVnf);
            }

            for(Link ln:sfc.linkset){
                Link TLink = new Link(ln.getid(),ln.getsrcid(),ln.getdstid(),ln.getBandwidth(),ln.getType());
                TLink.cost = ln.cost;
                TLink.setPrice(ln.getPrice());
                TLink.setstate(ln.getstate());
                TLink.setWave(ln.getWave());

                sc.linkset.add(TLink);
            }
            res.add(sc);
        }
        return res;
    }

    void MCstart() throws Exception{
        //打印资源的信息，图G的。网络信息
        this.osw.write("==============================================" + "\r\n");
        this.osw.write("===================before=====================" + "\r\n");
        this.osw.write("==============================================" + "\r\n");
        //this.osw.write(G.GetInfo());
        NumsGet getnum = new NumsGet();
        //开始操作
        Switch TargetSw = null;
        int targetVNFID = -1;
        int tmpMax = Integer.MIN_VALUE;
        //Map<Integer,Double> QsfMap = new HashMap<>();
        for(Switch sw:G.switchset){//确定TargetSw,
            for(VNF nf:sw.VNFset){
                VNF tmpV = new VNF(nf.getID(),nf.getVNFtype(),nf.getVNFcapacity(),nf.cost,nf.getprice(),nf.getState(),nf.embedID,nf.VPW);
                Set<Sfc> TmpSfcSets = copySfcSet(sfcsets);
                nf.reverseState();//状态翻转
                Graph TmpG = VDeploySFC(G,TmpSfcSets,enFlag);////映射,1是正序，其他数字反序
                double ss = GetQcfValue(TmpG);
                int tp = (int) (TmpG.getMaxUtility()*getnum.getEmbedSfcNums(TmpSfcSets));
                if(tmpMax < tp && flagV2 != nf.getID() && flagSW2 != sw.getID()){
                    tmpMax  = tp;
                    TargetSw = sw;
                    targetVNFID = nf.getID();
                }
                nf.reverseState();//把翻转状态变回来
                nf.cost = tmpV.cost;
                nf.setState(tmpV.getState());
                nf.embedVnfID = tmpV.embedVnfID;
                nf.embedID = tmpV.embedID;
            }
        }
        ///
        Collection<Link> resLinkstes = new HashSet<>();
        for(int m=0;m<G.getSwitchnum();m++){
            for(int n=0;n<G.getSwitchnum();n++){
                Random r = new Random();
                int w = r.nextInt(1) + 1;
                if(m != n && !hasLink(resLinkstes,m,n,51)){
                    Collection<Link> tpLinkstes = new HashSet<>();
                    for(Olink oln:G.olinkset){
                        if(oln.getWave() == w && oln.getstate()==0){
                            tpLinkstes.add(oln);
                        }
                    }
                    Collection<Link> tpr = dijkstra(m, n, tpLinkstes, G.switchset.size());
                    if(tpr.size() != 0){
                        int id = 200;
                        while (G.rlinkset.contains(new Link(id,m,n,2000,51))){
                            id = 200 + r.nextInt(50);
                        }
                        G.rlinkset.add(new Link(id,m,n,2000,51,tpr,w));
                    }
                }
            }
        }
        ///
        Set<Sfc> EnbSfcSets = copySfcSet(sfcsets);
        int flagO=0;
        int MnId=-1;

        for(Link rl:G.rlinkset){
            if(rl.getType() >= 50){
                Link tmpL = new Link(rl.getid(),rl.getsrcid(),rl.getdstid(),rl.cost,rl.getPrice(),rl.getBandwidth(),rl.getstate(),rl.getType(),rl.getWave(),rl.getO_total_price());
                Set<Sfc> TmpSfcSets = copySfcSet(sfcsets);
                rl.reverseState();//状态翻转
                Graph TmpG = VDeploySFC(G,TmpSfcSets,enFlag);//映射
                int tp = (int) (TmpG.getMaxUtility()*getnum.getEmbedSfcNums(TmpSfcSets));
                if(tmpMax < tp && flagOPath2 != G.getOpenOpathNum()){
                    flagO = 1;
                    tmpMax  = tp;
                    MnId = rl.getid();
                }
                rl.reverseState();//状态翻转回来
                rl.setstate(tmpL.getstate());
                rl.cost = tmpL.cost;
            }
        }
        flagV2 = flagV1;
        flagSW2 = flagSW1;
        double T=0.0;
        if(flagO == 0){
            //已经确认SW和VNF
            assert TargetSw != null;
            for(VNF nf:TargetSw.VNFset){//选择该VNF进行状态反转，并且更新cf
                if(nf.getID() == targetVNFID){
                    nf.reverseState();
                    break;
                }
            }
            flagV1 = targetVNFID;
            flagSW1 = TargetSw.getID();
        }else{
            for(Link rl:G.rlinkset){
                if(rl.getid() == MnId && rl.getType() >= 50){
                    rl.reverseState();
                    if(rl.getstate() == 1){
                        for(Olink oln:G.olinkset){
                            if(oln.getWave() ==rl.getWave() && rl.usedOpathSet.contains(oln)){
                                oln.setstate(1);
                            }
                        }
                    }else{
                        for(Olink oln:G.olinkset){
                            if(oln.getWave() ==rl.getWave() && rl.usedOpathSet.contains(oln)){
                                oln.setstate(0);
                            }
                        }
                    }
                    break;
                }
            }
            Graph Gd = new Graph();
            Gd.cloneG(G);
            for(Link rl:Gd.rlinkset){
                if(rl.getType() >= 50 && rl.getstate() == 0){
                    G.rlinkset.remove(rl);
                }
            }

            flagV1 = -1;
            flagOPath2 = flagOPath1;
            flagOPath1 = G.getOpenOpathNum();
        }

        G = VDeploySFC(G,EnbSfcSets,enFlag);
        T = 1/GetQcfValue(G);
        G.existence = GetRandomExponentialTimer(T);


        sfcsets = copySfcSet(EnbSfcSets);



        //操作结束，打印资源的信息，图G的。网络信息
        this.osw.write("==============================================" + "\r\n");
        this.osw.write("====================after=====================" + "\r\n");
        this.osw.write("==============================================" + "\r\n");
        this.osw.write(G.GetInfo());
        this.osw.write("\r\n");

        printSfcState(this.osw);

    }

    double GetRandomExponentialTimer(double lamda){
        double time = 0.0;
        //Math.random()产生0-1随机数，Math.log e为底的对数。
        Random r = new Random();
        time = -(1 / lamda) * Math.log(r.nextDouble());
        //time = -(1 / lamda) * Math.log(Math.random());
        //time = -(1 / lamda) * Math.log(1-Math.random());
        return time;
    }

    void IniSFCsSets(Collection<Sfc> SFCsets){
        for(Sfc sf:SFCsets){
            sf.setState(0);
            for(VNF vf:sf.VNFset){
                vf.embedID = 100000;
                vf.embedVnfID = 100000;
                vf.setState(0);
            }
            for(Link ln:sf.linkset){
                ln.setstate(0);
            }
        }
    }


    Link GetLink(Collection<Link> set,int m,int n,int type){
        Link resLink = new Link();
        for(Link l:set){
            if(l.getsrcid() == m && l.getdstid() == n &&l.getType() == type){
                resLink = l;
            }
        }
        return resLink;
    }

    boolean hasLink(Collection<Link> set,int m,int n,int type){
        for(Link l:set){
            if(l.getsrcid() == m && l.getdstid() == n &&l.getType() == type){
                return true;
            }
        }
        return false;
    }

    double GetTs(Switch s, Collection<Sfc> SFCsets) throws Exception {
        double SumOfQcf=1;
        NumsGet getnum = new NumsGet();///可以删

        for(VNF nf:s.VNFset){
            Set<Sfc> TmpSfcSets = copySfcSet(SFCsets);
            //状态翻转
            VNF tmpV = new VNF(nf.getID(),nf.getVNFtype(),nf.getVNFcapacity(),nf.cost,nf.getprice(),nf.getState(),nf.embedID,nf.VPW);
            nf.reverseState();
            //映射
            Graph TmpG = VDeploySFC(G,TmpSfcSets,enFlag);//1是正序，其他数字反序
            //
            this.osw.write("sfc这1次部署结果: " + getnum.getEmbedSfcNums(TmpSfcSets) + "  " );
            //
            SumOfQcf += GetQcfValue(TmpG);//计算Qcf
            nf.reverseState();//把翻转状态变回来
            nf.cost = tmpV.cost;
            nf.setState(tmpV.getState());
            nf.embedVnfID = tmpV.embedVnfID;
            nf.embedID = tmpV.embedID;

        }
        this.osw.write("\r\n");
        return 1/SumOfQcf;

    }

    int GetSWMaxV(Switch s, Collection<Sfc> SFCsets) throws Exception {
        NumsGet getnum = new NumsGet();///可以删
        int res = Integer.MIN_VALUE;

        for(VNF nf:s.VNFset){
            Set<Sfc> TmpSfcSets = copySfcSet(SFCsets);
            //状态翻转
            VNF tmpV = new VNF(nf.getID(),nf.getVNFtype(),nf.getVNFcapacity(),nf.cost,nf.getprice(),nf.getState(),nf.embedID,nf.VPW);
            nf.reverseState();
            //映射
            Graph TmpG = VDeploySFC(G,TmpSfcSets,enFlag);//1是正序，其他数字反序
            int tpm = TmpG.getMaxUtility();
            if(res < tpm){
                res = tpm;
            }
            this.osw.write( " VNF部署结果: " + tpm + "  SFC结果: " + getnum.getEmbedSfcNums(TmpSfcSets));

            nf.reverseState();//把翻转状态变回来
            nf.cost = tmpV.cost;
            nf.setState(tmpV.getState());
            nf.embedVnfID = tmpV.embedVnfID;
            nf.embedID = tmpV.embedID;

        }
        return res;
    }

    double GetTo(Link mn, Collection<Sfc> SFCsets) throws Exception {
        double SumOfQcf=0.0;
        //状态翻转
        mn.reverseState();
        //映射
        Graph TmpG = VDeploySFC(G,SFCsets,enFlag);//1是正序，其他数字反序
        SumOfQcf += GetQcfValue(TmpG);//计算Qcf
        //把翻转状态变回来
        mn.reverseState();
        return 1/SumOfQcf;
    }

    double GetQcfValue(Graph graph){
        double value ;
        double qf1_to_f2;

        if(graph.switchset.isEmpty()){
            value = 0;
            System.out.println("本次有空G" );
        }else{
            qf1_to_f2 = Math.pow(Math.E, 0.5*beta*(graph.getMaxUtility() - G.getMaxUtility())-1);
            value = qf1_to_f2;
        }

        return value;
    }

    void printSfcState(OutputStreamWriter osw) throws IOException {
        for(Sfc sfc:sfcsets){
            osw.write("SFC-ID是："+sfc.ID+"===>>>>状态：" + sfc.getState());
            osw.write("\r\n");
        }
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
            Map<Integer,Integer> VNFMap = new HashMap<>();
            //先映射点
            int flagSfc=1;
            for(VNF sfcVNF:tmp_sfc.VNFset){
                VNFMap.put(sfcVNF.getID(),sfcVNF.cost);
                for(Switch sw:graph.switchset){
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
            List<Link> linkList = new ArrayList<>(tmp_sfc.linkset);
            if(flag == 1){
                linkList.sort(new Comparator<Link>() {
                    @Override
                    public int compare(Link o1, Link o2) {
                        return Integer.compare(VNFMap.get(o1.getsrcid())+VNFMap.get(o1.getdstid()), VNFMap.get(o2.getsrcid())+VNFMap.get(o2.getdstid()));
                    }
                });
            }else if(flag == 2){
                linkList.sort(new Comparator<Link>() {
                    @Override
                    public int compare(Link o1, Link o2) {
                        return Integer.compare(o1.getBandwidth(), o2.getBandwidth());
                    }
                });
            }else{
                linkList.sort(new Comparator<Link>() {
                    @Override
                    public int compare(Link o1, Link o2) {
                        return Integer.compare(o1.getBandwidth()/(1+VNFMap.get(o1.getsrcid())+VNFMap.get(o2.getdstid())), o2.getBandwidth()/(1+VNFMap.get(o2.getsrcid())+VNFMap.get(o2.getdstid())));
                    }
                });
            }

            int LFindFlag;
            //for(Link sfcLink:tmp_sfc.linkset){
            for(Link sfcLink:linkList){
                LFindFlag = 0;
                for(VNF tmpVNFsrc:tmp_sfc.VNFset){
                    for(VNF tmpVNFdst:tmp_sfc.VNFset){
                        if(tmpVNFsrc.getID() == sfcLink.getsrcid() && tmpVNFdst.getID() == sfcLink.getdstid()){
                            LFindFlag = 1;
                            if(tmpVNFsrc.embedID == tmpVNFdst.embedID && tmpVNFdst.embedID != Max){
                                sfcLink.setstate(1);//表示映射成功
                            }//else{
                            if(tmpVNFsrc.embedID != tmpVNFdst.embedID && tmpVNFdst.embedID != Max && tmpVNFsrc.embedID != Max){
                                for(Link gRlink:graph.rlinkset){//找出带宽合适的边
                                    if(gRlink.getstate() == 1 && (gRlink.getBandwidth() - gRlink.cost) >= sfcLink.getBandwidth()){
                                        tmpESets.add(gRlink);
                                    }
                                }
                                //找路dijkstra
                                DijESets = dijkstra(tmpVNFsrc.embedID,tmpVNFdst.embedID,tmpESets,G.switchset.size());
                                for(Link dlink:DijESets){
                                    if(dlink.getid() == Max){
                                        flagSfc = 0;//映射失败
                                    }
                                }
                                if(flagSfc != 0){//找路成功，更新资源
                                    sfcLink.setstate(1);

                                    for(Link e1:DijESets){
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