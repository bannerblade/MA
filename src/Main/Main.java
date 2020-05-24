package Main;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import SFC.Sfc;
import graph.Graph;
import graph.Switch;
import graph.VNF;



public class Main {
    public static void main(String[] args) {
        try{
            PrintStream ps = new PrintStream("C:\\Users\\91191\\Desktop\\G_print.txt");
            System.setOut(ps);

            Graph G1 = new Graph(1);//创建标准图，G1是底层图
            Graph G2 = new Graph();//存最终映射结果

            G1.CreateRGraph();//初始化资源网络边。


            Collection<Sfc> sfcset = new HashSet<>();
            sfcset = CreateSFC(300);//需求是300条SFC

            MC myMC = new MC(G1, sfcset);//这里输入G和sfcset，然后运行MC
            myMC.MC_ini();//G初始化
            G2 = myMC.MCstart();

            System.out.println();
            int count = 0;

            while(count < 300){
                //************测试************
                Iterator i = G2.switchset.iterator();
                if(i.hasNext()){
                    System.out.print("总收益："+G2.getMaxUtility());
                    System.out.println("    成功部署的sfc数量：" + G2.sfc_num);
                    G2.sfc_num = 0;
                    G2.em_flag = 0;///新加进去的
                    MC tmp_myMC = new MC(G2, sfcset);
                    G2 = tmp_myMC.MCstart();
                }else{
                    System.out.println("第 " + count + "次的G2是空的！");
                }
                count++;
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
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
                    System.out.println();
                }
            }
        }else {
            System.out.println("空的！");
        }


    }

}



