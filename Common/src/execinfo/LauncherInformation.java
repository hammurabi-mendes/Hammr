/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package execinfo;

import java.util.Set;
import java.util.HashSet;

import java.io.Serializable;

import security.CollocationStatus;

public class LauncherInformation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;

	private String host;
	private String rack;

	private int totalSlots;
	private int ocupiedSlots;

	private Set<String> userRestrictions;
	private Set<String> applicationRestrictions;
	private Set<String> nodeRestrictions;

	private CollocationStatus collocationStatus;

	public LauncherInformation(String id, String host, String rack, int totalSlots) {
		this.id = id;

		this.host = host;
		this.rack = rack;

		this.totalSlots = totalSlots;

		this.userRestrictions = new HashSet<String>();
		this.applicationRestrictions = new HashSet<String>();
		this.nodeRestrictions = new HashSet<String>();

		this.collocationStatus = CollocationStatus.SHARED_OTHERUSER;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public String getRack(){
		return rack;
	}

	public int getTotalSlots() {
		return totalSlots;
	}

	public void setTotalSlots(int totalSlots) {
		this.totalSlots = totalSlots;
	}

	public int getOcupiedSlots() {
		return ocupiedSlots;
	}

	public void setOcupiedSlots(int ocupiedSlots) {
		this.ocupiedSlots = ocupiedSlots;
	}

	public boolean isAvailable(){
		return getFreeSlots() > 0;
	}

	public int getFreeSlots() {
		return totalSlots - ocupiedSlots;
	}

	public void setUserRestrictions(String[] userRestrictions) {
		for(String userRestriction: userRestrictions) {
			this.userRestrictions.add(userRestriction);
		}		
	}

	public Set<String> getUserRestrictions() {
		return userRestrictions;
	}

	public void setApplicationRestrictions(String[] applicationRestrictions) {
		for(String applicationRestriction: applicationRestrictions) {
			this.applicationRestrictions.add(applicationRestriction);
		}
	}

	public Set<String> getApplicationRestrictions() {
		return applicationRestrictions;
	}

	public void setNodeRestrictions(String[] nodeRestrictions) {
		for(String nodeRestriction: nodeRestrictions) {
			this.nodeRestrictions.add(nodeRestriction);
		}
	}

	public Set<String> getNodeRestrictions() {
		return nodeRestrictions;
	}

	public void setCollocationStatus(CollocationStatus collocationStatus) {
		this.collocationStatus = collocationStatus;
	}

	public CollocationStatus getColocationStatus() {
		return collocationStatus;
	}
}
