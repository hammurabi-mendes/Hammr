package mapreduce.programs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import utilities.Logging;
import utilities.Pair;

import mapreduce.communication.MRChannelElement;
import appspecs.Node;

public final class ReducerNode<INKEY extends Comparable<INKEY>, INVALUE, OUTKEY extends Comparable<OUTKEY>, OUTVALUE>
		extends Node {
	private final Reducer<INKEY, INVALUE, OUTKEY, OUTVALUE> reducer;

	public ReducerNode(Reducer<INKEY, INVALUE, OUTKEY, OUTVALUE> reducer) {
		this.reducer = reducer;
	}

	@Override
	public void run() {
		try {
			Logging.Info("[ReducerNode] Running...");
			Pair<INKEY, List<INVALUE>> currentElementList = null;
			Pair<String, MRChannelElement<INKEY, INVALUE>> currentPair = null;

			PriorityQueue<Pair<String, MRChannelElement<INKEY, INVALUE>>> qInputElement = new PriorityQueue<Pair<String, MRChannelElement<INKEY, INVALUE>>>(
					getInputChannelNames().size(), new Comparator<Pair<String, MRChannelElement<INKEY, INVALUE>>>() {
						@Override
						public int compare(Pair<String, MRChannelElement<INKEY, INVALUE>> p1,
								Pair<String, MRChannelElement<INKEY, INVALUE>> p2) {
							return p1.getSecond().getKey().compareTo(p2.getSecond().getKey());
						}
					});

			for (String s : getInputChannelNames()) {
				MRChannelElement<INKEY, INVALUE> element = (MRChannelElement<INKEY, INVALUE>) read(s);
				if (element != null) {
//					Logging.Info("[ReducerNode][run] element: <" + element.getKey() + "," + element.getValue()
//							+ ">");
					qInputElement.add(new Pair<String, MRChannelElement<INKEY, INVALUE>>(s, element));
				}
			}

			while (!isKilled() && (currentPair = qInputElement.poll()) != null) {
				String channelName = currentPair.getFirst();
				MRChannelElement<INKEY, INVALUE> currentElement = currentPair.getSecond();
//				Logging.Info("[ReducerNode][run] currentElement: <" + currentElement.getKey() + ","
//						+ currentElement.getValue() + ">");
				MRChannelElement<INKEY, INVALUE> nextElement = (MRChannelElement<INKEY, INVALUE>) read(channelName);
				if (nextElement != null) {
					qInputElement.add(new Pair<String, MRChannelElement<INKEY, INVALUE>>(channelName, nextElement));
				} else {
					getInputChannel(channelName).close();
				}

				if (currentElementList == null) {
					currentElementList = new Pair<INKEY, List<INVALUE>>(currentElement.getKey(),
							new ArrayList<INVALUE>());
				} else if (currentElementList.getFirst().compareTo(currentElement.getKey()) != 0) {
					// reduce the current list
					// System.out.println("[ReducerNode] Reduce.");
					reducer.reduce(currentElementList.getFirst(), currentElementList.getSecond(), writersShuffler);
					currentElementList = new Pair<INKEY, List<INVALUE>>(currentElement.getKey(),
							new ArrayList<INVALUE>());
				}
				currentElementList.getSecond().add(currentElement.getValue());
			}

			if (!isKilled()) {
				if (currentElementList != null) {
					reducer.reduce(currentElementList.getFirst(), currentElementList.getSecond(), writersShuffler);
				}
				reducer.cleanup(writersShuffler);
			} else {
				Logging.Info(String.format("[ReducerNode] Reducer node %s is killed.", getName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			flushAndCloseOutputs();
		}
	}
}
