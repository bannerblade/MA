package SFC;

import java.io.*;
import java.util.*;


import graph.Link;
import graph.VNF;

public class Sfc implements Serializable {
	private static final int Max = 100000 ;
	public int ID;
	public Collection<VNF> VNFset = new HashSet<>();//这个容器存VNF
	public Collection<Link> linkset = new HashSet<>();//这个容器存有向边 带宽需求20-60
	private int state=0;//用来看是否被映射，是=1；
	//private int VNFnum;//这个表示SFC有多少VNF，也可以从VNFset里得到，初始化用。0<VNFnum<=5

	public Sfc(){}

	//随机生成一条SFC
	public Sfc(int id ){
		this.ID= id;
		Random r = new Random();
		int VNFnum = r.nextInt(4) + 2;//产生2-5个VNF
		Random r2 = new Random();//产生第一个VNF的type的随机数
		int tmp_src = Max;
		int tmp_now = Max;

		//添加VNF,且要防止链的两头VNF一样
		for(int j=0;j<VNFnum;j++){
			tmp_now = r.nextInt(5);
			while(tmp_now == tmp_src){
				tmp_now = r.nextInt(5);
			}
			VNFset.add(new VNF(j,tmp_now,r.nextInt(5) + 2));
			tmp_src = tmp_now;
		}
		//添加SFC的有向边,通过VNF ID关联
		for(int j = 0; j<(VNFnum -1); j++){
			linkset.add(new Link(j,j,j+1,r.nextInt(6)+5,3));
		}
	}

	public int getID(){
		return this.ID;
	}
	public int getState(){return this.state;}
	public void setState(int state){this.state = state;}

	public void initial_sfc(){
		this.state = 0;
		for(VNF tmp_vnf:VNFset){
			tmp_vnf.embedID = Max;
		}
		for(Link tmp_l:linkset){
			tmp_l.setstate(0);
		}
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

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(this.getClass() != obj.getClass()) return false;
		Sfc sfc = (Sfc) obj;
		return ID == sfc.getID();
	}
}
