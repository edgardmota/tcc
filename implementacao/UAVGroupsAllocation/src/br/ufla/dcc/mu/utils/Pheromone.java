package br.ufla.dcc.mu.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.mu.node.RegularNode;
import br.ufla.dcc.mu.node.UAV;

/**
 * 
 * @author Tales Heimfarth, Ivayr Farah Netto
 *
 */

public class Pheromone {
	
	private double value;
	private NodeId id;
	private final double increment = 0.1;
	private final double decrement = 0.001;
	private Map<Integer,Float> resources = new HashMap<Integer,Float>();
	
	public void update_view (NodeId id)
	{
		SimulationManager.logNodeState(id,"Pheromone"+this.id.asInt(), "float", String.valueOf(value));
	}
	
	public Pheromone (NodeId uav,Map<Integer,Float> resources)
	{
		this.value = 0.0;
		this.id = uav;
		this.setResources(resources);
	}
	
	public boolean setResources(Map<Integer,Float> resources){
		this.resources = resources;
		return true;
	}
	
	public Map<Integer,Float> getResources(){
		return this.resources;
	}
	
	public void set(double d){
		if (this.value < d)
			this.value = d;
	}
	
	public double get(){
		return this.value;
	}
	
	
	public void increase ()
	{
		this.increase(this.increment);
	}
	
	public void increase (double v)
	{
		this.value+=v;
		if (this.value>1.0)
		{
			this.value = 1.0;
		}
	}
	
	public void evaporate ()
	{
		this.evaporate(this.decrement);
	}
	
	public void evaporate (double v)
	{
		this.value-=v;
		if (this.value<0.0)
			this.value = 0.0;
	}
	
	public Pheromone clone(){
		Pheromone clone = new Pheromone(this.id,this.getResources());
		clone.value = this.value;
		
		return clone;
	}
	
}
