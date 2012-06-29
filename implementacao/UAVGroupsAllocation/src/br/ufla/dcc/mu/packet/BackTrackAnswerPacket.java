package br.ufla.dcc.mu.packet;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;

public class BackTrackAnswerPacket extends ApplicationPacket {
	private boolean proceed;

	public BackTrackAnswerPacket(Address sender, NodeId receiver, boolean proceed) {
		super(sender, receiver);
		this.setProceed(proceed);
	}

	public boolean canProceed() {
		return proceed;
	}

	public void setProceed(boolean proceed) {
		this.proceed = proceed;
	}
	
}
