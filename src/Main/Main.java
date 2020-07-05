package Main;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import SFC.Sfc;
import graph.Graph;
import graph.Link;
import graph.Switch;
import graph.VNF;



public class Main {
    public static void main(String[] args) {
        try{
            PrintStream ps = new PrintStream("C:\\Users\\91191\\Desktop\\G_print.txt");
            System.setOut(ps);

            Graph G1 = new Graph(1);//创建标准图，G1是底层图

            G1.CreateRGraph();//初始化资源网络边。


            Collection<Sfc> sfcset = new HashSet<>();
            sfcset = CreateSFC(3);//需求是300条SFC

            MC myMC = new MC(G1, sfcset);//这里输入G和sfcset，然后运行MC
            myMC.MC_ini();//G初始化
            myMC.MCstart();

            System.out.println();
            int count = 0;

            while(count < 5){
                //************测试************
                Iterator i = myMC.G.switchset.iterator();
                if(i.hasNext()){
                    System.out.print("总收益："+myMC.G.getMaxUtility());
                    System.out.println("    成功部署的sfc数量：" + myMC.G.sfc_num);
                    myMC.G.sfc_num = 0;
                    myMC.G.em_flag = 0;///新加进去的
                    ///*
                    PrintGstate(myMC.G);
                    PrintAllSfc(sfcset);
                    System.out.println();
                    System.out.println("---------------------------------------------------------------------");
                    System.out.println("----------------------------下次映射开始-----------------------------");
                    System.out.println("---------------------------------------------------------------------");
                    // */
                    for(Sfc tm_sfc:myMC.sfcsets){tm_sfc.initial_sfc();}//把sfc集合里面的元素状态初始化。
                    myMC.G.recoverResourceCapacity();//G资源容量初始化
                    myMC.MCstart();
                }else{
                    System.out.println("第 " + count + "次的G是空的！");
                }
                count++;
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (Exception E){
            E.printStackTrace();
        }



    }


   public static Collection<Sfc> CreateSFC(int num){//创建SFC群，装再SFCset里返回。
       Collection<Sfc> sfcset = new HashSet<>();
      for(int i=0;i<num;i++){
          sfcset.add(new Sfc(i));
      }
       return sfcset;
   }

    public static void PrintGstate(Graph G){
        Iterator i = G.switchset.iterator();
        if(i.hasNext()){
            for(Switch tmp_s:G.switchset){
                System.out.println("\n");
                System.out.print("SW的ID: " + tmp_s.getID() + "---->>>>");
                System.out.println("SW状态" + tmp_s.getstate());
                for(VNF tmpv:tmp_s.VNFset){
                    System.out.print("  VNF的ID：" + tmpv.getID() + "------>>>>>>" );
                    System.out.print("  VNF状态" + tmpv.getState());
                    System.out.print("  VNF类型" + tmpv.getVNFtype());
                    System.out.print("  VNF容量" + tmpv.VNFcapacity+"******");
                    System.out.print("  VNF的cost" + tmpv.cost);
                    System.out.println();
                }
            }
            System.out.println("\n");
            System.out.println("rlink信息");
            for(Link rl:G.rlinkset){
                if(rl.cost > 0){
                    System.out.println("rlink的ID: " + rl.getid() +"容量：" + rl.getBandwidth()+"使用了的cost: " + rl.cost +"-->  src：" + rl.getsrcid() + "  dst: "+ rl.getdstid());
                }
            }
        }else {
            System.out.println("空的！");
        }
    }

    public static void PrintAllSfc(Collection<Sfc> SfcSets){
        System.out.println();
        System.out.println("SFC的VNF信息：");
        for(Sfc sfc:SfcSets){
            System.out.println();
            System.out.println("SFC的ID：" + sfc.ID + "------>>>>>>现在的状态" + sfc.getState());
            for(VNF vnf:sfc.VNFset){
                System.out.print("SFC的VNF的ID：" + vnf.getID() +"--->>>");
                System.out.print ("SFC的VNF的类型：" + vnf.getVNFtype());
                System.out.print("SFC的VNF的容量：" + vnf.getVNFcapacity());
                System.out.print("SFC的VNF的映射点：" + vnf.embedID);
                System.out.println();
            }
            System.out.println();
            System.out.println("SFC的Link信息：");
            for(Link link:sfc.linkset){
                System.out.println();
                System.out.println("SFC的Link的ID：" + link.getid());
                System.out.print("源目的节点：" +"src:" + link.getsrcid() + "  dst:" + link.getdstid());
                System.out.print("带宽需求：" + link.getBandwidth() + "  现在的状态：" + link.getstate());
            }
            System.out.println();
        }
    }
}



