/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package graphs.programs.shortestpath;

import execinfo.aggregators.AbstractAggregator;

import utilities.Pair;

public class SPFinishAggregator extends AbstractAggregator<Pair<Boolean,Integer>,Boolean> {
	private static final long serialVersionUID = 1L;

	private boolean[] finalize;

	public SPFinishAggregator(String variable, int numberWorkers) {
		super(variable);

		finalize = new boolean[numberWorkers];
	}

	public synchronized void updateAggregate(Pair<Boolean,Integer> object) {
		finalize[object.getSecond()] = object.getFirst();
	}

	public synchronized Boolean obtainAggregate() {
		for(int i = 0; i < finalize.length; i++) {
			if(finalize[i] == false) {
				return false;
			}
		}

		return true;
	}
}
