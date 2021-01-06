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
        int simulateTimes = 1;//选择仿真次数。
        for(int j=0;j<simulateTimes;j++){
            try{
                PrintStream ps = new PrintStream("C:\\Users\\91191\\Desktop\\G_print" + j + ".txt");
                System.setOut(ps);

                //打印资源的信息，图G的。网络信息
                File GInfoFile = new File("C:\\Users\\91191\\Desktop\\MC1.2\\txt\\GInfo.txt");
                FileOutputStream fos = new FileOutputStream(GInfoFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                NumsGet getnum = new NumsGet();
                Graph G1 = new Graph(1);//创建标准图，G1是底层图

                G1.CreateRGraph();//初始化资源网络边。

                String fileSfcPath = "C:\\Users\\91191\\Desktop\\MC1.2\\txt\\sfcs.txt";
                String fileSfcPath2 = "C:\\Users\\91191\\Desktop\\MC1.2\\txt\\MINIsfcs.txt";
                String fileSfcPath3 = "C:\\Users\\91191\\Desktop\\MC1.2\\txt\\sfcEmbeddingInfo.txt";


                //Collection<Sfc> sfcset = FileCreateSfc(fileSfcPath);
                Collection<Sfc> MiniSfcSet = FileCreateSfc(fileSfcPath2);
                //Collection<Sfc> MiniSfcSet = CreateSFC(5);
                //Collection<Sfc> sfcset = CreateSFC(60);//需求是300条SFC
                //getSfcs(fileSfcPath2,MiniSfcSet);  //把产生的sfc打印到txt

                MC myMC = new MC(G1, MiniSfcSet);//这里输入G和sfcset，然后运行MC
                myMC.osw = osw;

                myMC.osw.write("**********************************");
                myMC.osw.write("\r\n");
                myMC.osw.write("************第  " + 0 + "  次************");
                myMC.osw.write("\r\n");
                myMC.osw.write("**********************************");
                myMC.osw.write("\r\n");

                myMC.MC_ini();//G初始化
                myMC.MCstart();

                System.out.println();
                int count = 1;


                while(count < 150){
                    //************测试************
                    myMC.osw.write("\r\n");
                    myMC.osw.write("**********************************");
                    myMC.osw.write("\r\n");
                    myMC.osw.write("************第  " + count + "  次************");
                    myMC.osw.write("\r\n");
                    myMC.osw.write("**********************************");
                    myMC.osw.write("\r\n");
                    Iterator i = myMC.G.switchset.iterator();
                    if(i.hasNext()){
                        System.out.print("总收益："+myMC.G.getMaxUtility());
                        //System.out.print("    存在时间："+myMC.G.getExistence());
                        System.out.print("    存在时间："+exiR(myMC.G.existence,myMC.G.getExistence())*getnum.getEmbedSfcNums(myMC.sfcsets)/myMC.sfcsets.size());
                        System.out.print("    转移概率："+myMC.G.qcf);
                        System.out.println("    成功部署的sfc数量：" +getnum.getEmbedSfcNums(myMC.sfcsets));
                        myMC.G.em_flag = 0;///新加进去的
                        myMC.MCstart();
                    }else{
                        System.out.println("第 " + count + "次的G是空的！");
                    }
                    count++;
                }
                getEmbeddingInfo(fileSfcPath3, myMC.sfcsets);
                osw.close();
            }   catch (Exception E){
                E.printStackTrace();
            }
        }
    }

    public static double exiR(double old,double now){
        double re = now;
        if(old > re) re *= 0.000001 ;
        return re;
    }

    public static Collection<Sfc> CreateSFC(int num){//创建SFC群，装再SFCset里返回。
       Collection<Sfc> sfcset = new HashSet<>();
      for(int i=0;i<num;i++){
          sfcset.add(new Sfc(i));
      }
       return sfcset;
   }

    public static Collection<Sfc> FileCreateSfc(String file) throws IOException {
        Collection<Sfc> sfcset = new HashSet<>();
        File fl = new File(file);
        FileInputStream fis = new FileInputStream(fl);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int num = Integer.parseInt(br.readLine().trim());//节点数目
        for(int i=0;i<num;i++){
            int sfcId = Integer.parseInt(br.readLine().trim());//ID
            String[] tps = br.readLine().split(",");
            int vnum = Integer.parseInt(tps[0].trim());//vnf的数目
            int lnum = Integer.parseInt(tps[1].trim());//link的数目

            Collection<VNF> VNFset = new HashSet<>();//这个容器存VNF
            Collection<Link> linkset = new HashSet<>();//这个容器存有向边
            for(int j=0;j<vnum;j++){//vnf
                String[] ts = br.readLine().split(",");
                VNFset.add(new VNF(Integer.parseInt(ts[0].trim()),Integer.parseInt(ts[1].trim()),Integer.parseInt(ts[2].trim())));
            }
            for(int j=0;j<lnum;j++){//link
                String[] ts = br.readLine().split(",");
                linkset.add(new Link(Integer.parseInt(ts[0].trim()),Integer.parseInt(ts[1].trim()),
                        Integer.parseInt(ts[2].trim()), Integer.parseInt(ts[3].trim()),3));
            }
            sfcset.add(new Sfc(sfcId,VNFset,linkset));
        }
        br.close();
        return sfcset;
    }

    public static void getEmbeddingInfo(String file, Collection<Sfc> set) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.newLine();
        for(Sfc sfc:set){
            if(sfc.getState() == 1){
                bw.write("sfc的ID：" + sfc.ID);
                bw.newLine();
                for(VNF vnf:sfc.VNFset){
                    bw.write("vnf的ID：" + vnf.getID() + "  映射到的点：" + vnf.embedID);
                    bw.newLine();
                }
                for(Link link:sfc.linkset){
                    bw.write("link的ID：" + link.getid() + "  , 用到的边：");
                    bw.newLine();
                    if(link.usedLinkSet.size() == 0) bw.write("没有用到rLink,可能映射到同一个点了。");
                    for(Link l:link.usedLinkSet){
                        bw.write("rLink的ID：" + l.getid());
                        bw.newLine();
                    }
                    bw.newLine();
                }
            }
        }
        bw.close();
    }

    public static void getSfcs(String file, Collection<Sfc> set) throws IOException{
        File fl = new File(file);//注意一下打印的sfc数目，以前是自己手动添加的。现在改自动了，小心点。
        FileOutputStream fos = new FileOutputStream(fl);
        OutputStreamWriter osw = new OutputStreamWriter(fos);

        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(set.size());
        bw.newLine();
        for(Sfc sfc:set){
            bw.write(Integer.toString(sfc.ID));
            bw.newLine();
            bw.write(Integer.toString(sfc.VNFset.size()));//vnf的数目
            bw.write(",");
            bw.write(Integer.toString(sfc.linkset.size()));//link的数目
            bw.newLine();
            for(VNF vnf:sfc.VNFset){
                bw.write(Integer.toString(vnf.getID()));//vnf的ID
                bw.write(",");
                bw.write(Integer.toString(vnf.getVNFtype()));//vnf的type
                bw.write(",");
                bw.write(Integer.toString(vnf.VNFcapacity));//vnf的capacity
                bw.newLine();
            }
            for(Link link:sfc.linkset){
                bw.write(Integer.toString(link.getid()));
                bw.write(",");
                bw.write(Integer.toString(link.getsrcid()));
                bw.write(",");
                bw.write(Integer.toString(link.getdstid()));
                bw.write(",");
                bw.write(Integer.toString(link.getBandwidth()));
                bw.newLine();
            }
        }
        bw.close();
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



