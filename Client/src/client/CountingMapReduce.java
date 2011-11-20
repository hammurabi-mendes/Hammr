package client;

import java.io.IOException;
import java.util.StringTokenizer;

import communication.writer.ChannelElementWriter;

import interfaces.Manager;
import utilities.ApplicationSpecFactory;
import utilities.RMIHelper;
import mapreduce.appspecs.MapReduceConfiguration;
import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.communication.MRChannelElement;
import mapreduce.programs.Mapper;
import mapreduce.programs.Reducer;

public class CountingMapReduce {
	private static final String usage = "CountingMapReduce [FILE|TCP] [TRUE|FALSE] username";
	private static final int nReducers = 1;
	private static final String outputFilename = "output.dat";

	public static final class CountingMapper extends Mapper<Long, String, String, Integer> {
		private static final long serialVersionUID = 1L;
		private Integer one = new Integer(1);

		@Override
		public void map(Long key, String value, ChannelElementWriter<MRChannelElement<String, Integer>> writer)
				throws Exception {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line, ",.\"?'-! ;():");
			try {
				while (tokenizer.hasMoreTokens()) {
					String word = tokenizer.nextToken();
					writer.write(new MRChannelElement<String, Integer>(word, one));
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
			Thread.sleep(10);
		}
	}

	public static final class CountingReducer extends Reducer<String, Integer, String, Integer> {
		private static final long serialVersionUID = 1L;

		@Override
		public void reduce(String key, Iterable<Integer> values,
				ChannelElementWriter<MRChannelElement<String, Integer>> writer) throws IOException {
			int sum = 0;
			for (int value : values) {
				sum += value;
			}
			writer.write(new MRChannelElement<String, Integer>(key, sum));
		}
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println(usage);
			System.exit(1);
		}

		try {
			String type = args[0];
			String userName = args[2];
			String property = "java.rmi.server.location";
			String registryLocation = System.getProperty(property);
			if (registryLocation == null) {
				System.err.printf("Failed to get %s. Exit.\n", property);
				System.exit(1);
			}

			MapReduceSpecification.Type tType = null;
			if ("FILE".equalsIgnoreCase(type)) {
				tType = MapReduceSpecification.Type.FILEBASED;
			} else if ("TCP".equalsIgnoreCase(type)) {
				tType = MapReduceSpecification.Type.TCPBASED;
			} else {
				System.err.println(String.format("Unrecognized Type: %s. Exit.", type));
				System.exit(1);
			}

			MapReduceConfiguration config = new MapReduceConfiguration();
			config.setJobName(CountingMapReduce.class.getSimpleName());
			config.setUserPoolName(userName);
			config.setMapperClass(CountingMapper.class);
			config.setReducerClass(CountingReducer.class);
			config.setCombinerClass(CountingReducer.class);
			config.setReducerNum(nReducers);
			config.setOutputFilePrefix(outputFilename);
			config.setCommunicationType(tType);
			MapReduceSpecification spec = ApplicationSpecFactory.getMapReduceApplicationSpecification(config);

			Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");
			manager.registerApplication(spec);
			System.out.println("[CountingMapReduce] Submit Application Done.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
