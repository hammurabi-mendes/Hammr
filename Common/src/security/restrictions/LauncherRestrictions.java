/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package security.restrictions;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;

import security.CollocationStatus;

public class LauncherRestrictions implements Serializable {
	private static final long serialVersionUID = 1L;

	protected boolean useUserSpecificLaunchers;
	protected boolean useApplicationSpecificLaunchers;
	protected boolean useNodeSpecificLaunchers;
	protected boolean useSeparateJVM;

	protected CollocationStatus collocationStatus;

	protected Set<String> launcherIds;

	protected int freeLauncherSlots;

	public LauncherRestrictions() {
		this.useUserSpecificLaunchers = false;
		this.useApplicationSpecificLaunchers = false;

		this.collocationStatus = CollocationStatus.SHARED_OTHERUSER;

		this.launcherIds = new HashSet<String>();
	}

	public boolean isUseUserSpecificLaunchers() {
		return useUserSpecificLaunchers;
	}

	public void setUseUserSpecificLaunchers(boolean useUserSpecificLaunchers) {
		this.useUserSpecificLaunchers = useUserSpecificLaunchers;
	}

	public boolean isUseApplicationSpecificLaunchers() {
		return useApplicationSpecificLaunchers;
	}

	public void setUseApplicationSpecificLaunchers(boolean useApplicaitonSpecificLaunchers) {
		this.useApplicationSpecificLaunchers = useApplicaitonSpecificLaunchers;
	}

	public boolean isUseNodeSpecificLaunchers() {
		return useNodeSpecificLaunchers;
	}

	public void setUseNodeSpecificLaunchers(boolean useNodeSpecificLaunchers) {
		this.useNodeSpecificLaunchers = useNodeSpecificLaunchers;
	}

	public boolean isUseSeparateJVM() {
		return useSeparateJVM;
	}

	public void setUseSeparateJVM(boolean useSeparateJVM) {
		this.useSeparateJVM = useSeparateJVM;
	}

	public CollocationStatus getCollocationStatus() {
		return collocationStatus;
	}

	public void setCollocationStatus(CollocationStatus collocationStatus) {
		this.collocationStatus = collocationStatus;
	}

	public void insertLauncher(String launcherId) {
		launcherIds.add(launcherId);
	}

	public void removeLauncher(String launcherId) {
		launcherIds.remove(launcherId);
	}

	public Set<String> getLauncherIds() {
		return launcherIds;
	}

	public int getFreeLauncherSlots() {
		return freeLauncherSlots;
	}

	public void setFreeLauncherSlots(int freeLauncherSlots) {
		this.freeLauncherSlots = freeLauncherSlots;
	}

	public boolean getIntersection(LauncherRestrictions other) {
		if(this.launcherIds.size() == 0 || other.getLauncherIds().size() == 0) {
			return true;
		}

		Set<String> intersection = new HashSet<String>();
		intersection.addAll(launcherIds);

		intersection.retainAll(other.getLauncherIds());

		return (intersection.size() > 0);
	}
}
