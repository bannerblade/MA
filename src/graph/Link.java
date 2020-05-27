package graph;
import java.io.*;

public class Link implements Serializable{
	private int ID;
	private int src;
	private int dst;
	public int cost = 0;//带宽使用量
	private int price = 10;//单位资源费用, 最开始的时候是price = 0;
	private int Bandwidth ;//elink 200，光一个波段2000，//设定 光链路带宽是电链路带宽的10倍
	private int state = 0;//链路使用状态，on = 1；也表示SFC里link的映射状态，state = 1，表示被映射了
	private int type;//链路类型，电链路0，光链路1，sfc有向边3。资源网络的边type=4.光通路type = 5x,r如50表示wave=0的光通路;
	private int wave;//光链路波段,假设有3个波段
	private int O_total_price = 0;//光通路的就总价格,500

	public Link (int id,int src , int dst, int Bandwidth,int type){
		this.ID = id;
		this.src = src;
		this.dst = dst;
		this.Bandwidth = Bandwidth;//500 电链路初始化图的边的带宽大小
		this.type = type;
		this.wave = 1000;
	}

	public Link (int id,int src , int dst, int Bandwidth,int type, int op){
		this.ID = id;
		this.src = src;
		this.dst = dst;
		this.Bandwidth = Bandwidth;//500 电链路初始化图的边的带宽大小
		this.type = type;
		this.wave = 1000;
		this.O_total_price = op ;
	}

	public Link (int id,int src , int dst, int in_cost, int in_price, int Bandwidth,int in_state, int type, int inwave,int op){
		this.ID = id;
		this.src = src;
		this.dst = dst;
		this.Bandwidth = Bandwidth;//500 电链路初始化图的边的带宽大小
		this.type = type;
		this.wave = 1000;
		this.O_total_price = op ;
		this.cost = in_cost;
		this.price = in_price;
		this.state = in_state;
		this.wave = inwave;
	}

	public Link(int id){
		this.ID = id;
	}

	public Link(){}

	public int getO_total_price(){return this.O_total_price;}
	public int getid(){
		return this.ID;
	}
	public void setID(int id){this.ID = id;}
	public int getstate(){
		return this.state;
	}
	public void setstate(int state){ this.state = state; }
	public int getsrcid(){ return this.src; }
	public int getdstid(){
		return this.dst;
	}
	public int getcost(){
		return this.cost;
	}
	public int getBandwidth(){return  this.Bandwidth;}
	public void setBandwidth(int Bandwidth){this.Bandwidth = Bandwidth;}
	public int getType(){return this.type;}
	public void setWave(int wave){this.wave = wave;}
	public int getWave(){return this.wave;}
	public int getPrice(){return this.price ;}
	public void setPrice(int price){this.price = price;}

	@Override
	public int hashCode(){
		return ID;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(this.getClass() != obj.getClass()) return false;
		Link link = (Link) obj;
		return ID == link.ID;
	}

}

