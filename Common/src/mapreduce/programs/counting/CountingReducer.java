/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package mapreduce.programs.counting;

import java.util.Collections;
import java.util.Comparator;

import java.util.Map;
import java.util.Map.Entry;

import java.util.List;
import java.util.ArrayList;

import mapreduce.communication.MRChannelElement;

import mapreduce.programs.Reducer;

//public class CountingReducer<O> extends Reducer<O,Long> {
//	private static final long serialVersionUID = 1L;
//
//	private CountingCombiner<O> combiner;
//
//	public CountingReducer() {
//		this.combiner = new CountingCombiner<O>();
//	}
//
//	public void reduce(O object, Long value) {
//		combiner.add(object, value);
//	}
//
//	public void finalizeReduce() {
//		List<Map.Entry<O,Long>> currentEntries = new ArrayList<Map.Entry<O,Long>>(combiner.getCurrentEntries());
//
//		Collections.sort(currentEntries, new CountingEntryComparator<O>());
//
//		for(Map.Entry<O,Long> currentEntry: currentEntries) {
//			O object = currentEntry.getKey();
//			Long value = currentEntry.getValue();
//
//			writeSomeone(new MRChannelElement<O,Long>(object, value));
//		}
//	}
//}
//
//class CountingEntryComparator<O> implements Comparator<Map.Entry<O,Long>> {
//	public int compare(Entry<O, Long> first, Entry<O, Long> second) {
//		return first.getValue().compareTo(second.getValue());
//	}
//}
