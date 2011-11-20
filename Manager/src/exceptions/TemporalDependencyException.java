/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package exceptions;

import appspecs.Node;

/**
 * This class represent a temporal dependency error. This happens when node1 should execute
 * before node2 (because, say, node1 produces a file that should be consumed by node2), but
 * also node2 should execute before node1.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class TemporalDependencyException extends Exception {
	private static final long serialVersionUID = 1L;

	private Node node1;
	private Node node2;

	/**
	 * Constructor method.
	 * 
	 * @param node1 First node involved in the dependency error.
	 * @param node2 Second node involved in the dependency error.
	 */
	public TemporalDependencyException(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public String toString() {
		return "Nodes " + node1 +  " and " + node2 + " represent an invalid temporal dependency exception";
	}
}
