package interfaces;

import java.io.Serializable;

import conf.Config;

public class LauncherStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String launcherId;
	public final String host;
	public final String rackId;
	public int totalSlots = Config.DEFAULT_SLOT_NUM;
	public int ocupiedSlots = 0;
	// # of slots which are be blocked. By default these slots are running
	// Single-NodeGroup ngbs since they can be blocked without blocking
	// nodegroups running in other launchers.
	public int blockableSlots = 0;
	// # of NodeGroups in the blocking queue
	public int blockQueueSize = 0;

	public LauncherStatus(String id, String host, String rid)
	{
		launcherId = id;
		this.host = host;
		rackId = rid;
	}
	
	public boolean isAvailable(){
		return getMaxAvailableSlotsNum() > 0;
	}
	
	public String getLauncherId() {
		return launcherId;
	}

	public String getRackId(){
		return rackId;
	}
	
	public int getFreeSlotsNum() {
		return totalSlots - ocupiedSlots;
	}

	/**
	 * @return the # of free slots plus blockabkeSlots
	 */
	public int getMaxAvailableSlotsNum() {
		return getFreeSlotsNum();
	}

	public int getBlockableSlots(){
		return blockableSlots;
	}
	
}
