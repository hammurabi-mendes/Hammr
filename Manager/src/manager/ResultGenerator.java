package manager;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Set;

import execinfo.ResultSummary;
import execinfo.NodeMeasurements;

/**
 * Generates a summary for the whole application execution, in a separate thread.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ResultGenerator extends Thread {
	private String baseDirectory;

	private String application;

	private long runningTime;

	// Result summaries received for the NodeGroups of this application
	
	private Set<ResultSummary> resultSummaries;

	/**
	 * Constructor method.
	 * 
	 * @param baseDirectory Directory where the report will be generated.
	 * @param application Name of the application being summarized.
	 * @param runningTime Application running time (real).
	 * @param resultSummaries Result summaries received in the application execution.
	 */
	public ResultGenerator(String baseDirectory, String application, long runningTime, Set<ResultSummary> resultSummaries) {
		this.baseDirectory = baseDirectory;

		this.application = application;

		this.runningTime = runningTime;

		this.resultSummaries = resultSummaries;
	}

	/**
	 * Generates the report, containing:
	 * 	     1) Individual Node running times (real/CPU/user);
	 *       2) Individual NodeGroup running time (real);
	 *       3) Average Node running time (real/CPU/user);
	 *       4) Average NodeGroup running time (real).
	 */
	public void run() {
		String completeFilename = baseDirectory + "/" + application + ".dat";

		try {
			FileWriter file = new FileWriter(completeFilename);

			file.write("RunningTime: " + getHumanReadableTime(runningTime) + "\n");

			file.write("\n");

			long averageNodeGroupTime = 0;
			
			long averageNodeCPUTime = 0;
			long averageNodeUserTime = 0;
			long averageNodeRealTime = 0;

			int numberNodes = 0;

			// Make a first pass just to discover the number of nodes

			for(ResultSummary resultSummary: resultSummaries) {
				Set<String> nodeNames = resultSummary.getNodeNames();

				numberNodes += nodeNames.size();
			}

			for(ResultSummary resultSummary: resultSummaries) {
				file.write("NodeGroup \"" + resultSummary.getNodeGroupSerialNumber() + "\" running time: " + getHumanReadableTime(resultSummary.getNodeGroupTiming()) + "\n");

				averageNodeGroupTime += resultSummary.getNodeGroupTiming() / resultSummaries.size();

				Set<String> nodeNames = resultSummary.getNodeNames();

				for(String nodeName: nodeNames) {
					NodeMeasurements nodeMeasurements = resultSummary.getNodeMeasurement(nodeName);

					file.write("\tNode \"" + nodeName + "\" CPU  time: " + getHumanReadableTime(nodeMeasurements.getCpuTime()) + "\n");
					averageNodeCPUTime += (nodeMeasurements.getCpuTime() / numberNodes);

					file.write("\tNode \"" + nodeName + "\" Real time: " + getHumanReadableTime(nodeMeasurements.getRealTime()) + "\n");
					averageNodeRealTime += (nodeMeasurements.getRealTime() / numberNodes);

					file.write("\tNode \"" + nodeName + "\" User time: " + getHumanReadableTime(nodeMeasurements.getUserTime()) + "\n");
					averageNodeUserTime += (nodeMeasurements.getUserTime() / numberNodes);

					file.write("\n");
				}
			}

			file.write("Average NodeGroup time: " + getHumanReadableTime(averageNodeGroupTime) + "\n");
			file.write("Average Node CPU  time: " + getHumanReadableTime(averageNodeCPUTime) + "\n");
			file.write("Average Node User time: " + getHumanReadableTime(averageNodeUserTime) + "\n");
			file.write("Average Node Real time: " + getHumanReadableTime(averageNodeRealTime) + "\n");

			file.close();
		} catch (IOException exception) {
			System.err.println("Error manipulating output file in " + completeFilename + "!");

			exception.printStackTrace();
			return;
		}
	}

	/**
	 * Converts a epoch-based time into a human-readable form.
	 * 
	 * @param time Epoch-based time long.
	 * 
	 * @return An human-readable form of the informed time.
	 */
	private String getHumanReadableTime(long time) {
		long units[] = new long[] {0, 0, 0, 0, 0};

		// Get milliseconds
		units[4] = time % 1000;

		// Convert to seconds
		time = time/1000;

		// Get seconds
		units[3] = time % 60; 

		// Convert to minutes
		time = time / 60;

		// Get minutes
		units[2] = time % 60;

		// Convert to hours
		time = time / 60;

		// Get hours
		units[1] = time = time % 24;

		// Convert to days
		time = time / 24;

		// Get days
		units[0] = time;

		String result = "";

		boolean previous = false;

		// Only insert days if we have more than one day

		if(units[0] > 0) {
			if(previous) {
				result += ", ";
			}

			result += units[0] + "days";
			previous = true;
		}

		// Only insert hours if we have more than one hour

		if(units[1] > 0) {
			if(previous) {
				result += ", ";
			}

			result += units[1] + "hours";
			previous = true;
		}

		// Always include minutes

		if(previous) {
			result += ", ";
		}

		result += units[2] + "min";
		previous = true;

		// Always include seconds

		if(previous) {
			result += ", ";
		}

		result += units[3] + "sec";
		previous = true;

		// Insert milliseconds only if we don't have days or hours

		if(units[0] == 0 && units[1] == 0 && units[4] > 0) {
			if(previous) {
				result += ", ";
			}

			result += units[4] + "millisec";
			previous = true;
		}

		return result;
	}
}
