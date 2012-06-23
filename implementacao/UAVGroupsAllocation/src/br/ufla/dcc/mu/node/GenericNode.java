package br.ufla.dcc.mu.node;

import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.TrafficGeneration;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.user.SendDelayedWakeUp;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.mu.utils.Converter;

public abstract class GenericNode extends ApplicationLayer{

	@Override
	public void processEvent(Initialize init) {
		SimulationManager.logNodeState(this.node.getId(), "TypeOfNode", "int", Converter.convertNodeNameToType(this.node));
		SimulationManager.logNodeState(this.node.getId(), "NodeId", "int", this.node.getId().toString());
	}


	@Override
	public void processEvent(TrafficGeneration tg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getPacketTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	
	public void sendDelayed(Packet packet, double delay){
		SendDelayedWakeUp  wuc = new SendDelayedWakeUp(this.getSender(), delay, packet);
		this.sendEventSelf(wuc);
	}
	
	public void processWakeUpCall(WakeUpCall wuc){
		if(wuc instanceof SendDelayedWakeUp){
			this.sendPacket(((SendDelayedWakeUp) wuc).getPkt());
		}
	}
	
}
