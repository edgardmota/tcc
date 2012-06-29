package br.ufla.dcc.mu.wuc;

import java.util.Map;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SelectNextSinkWakeUpCall extends WakeUpCall {
	private Map<Integer,Float> eventNeedsList;
	
	public SelectNextSinkWakeUpCall(Address sender, Map<Integer, Float> eventNeedsList, double delay) {
		super(sender, delay);
		this.setEventNeedsList(eventNeedsList);
	}

	public Map<Integer, Float> getEventNeedsList() {
		return eventNeedsList;
	}

	public void setEventNeedsList(Map<Integer, Float> eventNeedsList) {
		this.eventNeedsList = eventNeedsList;
	}
	
}
