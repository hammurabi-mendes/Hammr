/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package execinfo;

import java.io.Serializable;

/**
 * This class packages the whole set of node measurements.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class NodeMeasurements implements Serializable {
	private static final long serialVersionUID = 1L;

	private long realTime;
	private long cpuTime;
	private long userTime;

	/**
	 * Constructor method.
	 * 
	 * @param realTime Real time associated with the Node run.
	 * @param cpuTime CPU time associated with the Node run.
	 * @param userTime User time associated with the Node run.
	 */
	public NodeMeasurements(long realTime, long cpuTime, long userTime) {
		this.realTime = realTime;
		this.cpuTime = cpuTime;
		this.userTime = userTime;
	}

	/**
	 * Getter for the real time associated with the Node run.
	 * 
	 * @return The real time associated with the Node run.
	 */
	public long getRealTime() {
		return realTime;
	}

	/**
	 * Getter for the CPU time associated with the Node run.
	 * 
	 * @return The CPU time associated with the Node run.
	 */
	public long getCpuTime() {
		return cpuTime;
	}

	/**
	 * Getter for the user time associated with the Node run.
	 * 
	 * @return The user time associated with the Node run.
	 */
	public long getUserTime() {
		return userTime;
	}
}
