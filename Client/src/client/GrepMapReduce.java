package client;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import communication.writer.ChannelElementWriter;

import interfaces.Manager;
import mapreduce.appspecs.MapReduceConfiguration;
import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.communication.MRChannelElement;
import mapreduce.programs.Mapper;
import mapreduce.programs.Reducer;
import utilities.ApplicationSpecFactory;
import utilities.RMIHelper;

public class GrepMapReduce {
	private static final String usage = "GrepMapReduce [FILE|TCP] [TRUE|FALSE] username applicationname";
	private static final int nReducers = 4;
	private static final String sPattern = "Republic";

	public static final class GrepMapper extends Mapper<Long, String, String, Integer> {
		private static final long serialVersionUID = 1L;
		private final Pattern p = Pattern.compile(sPattern);
		private static final Integer one = new Integer("1");

		private int count = 0;
		
		@Override
		public void map(Long key, String value, ChannelElementWriter<MRChannelElement<String, Integer>> writer)
				throws IOException {
			writer.write(new MRChannelElement<String, Integer>(value, one));
			count++;
		}
		
		@Override
		public void cleanup(ChannelElementWriter<MRChannelElement<String, Integer>> writer)
		{
			System.out.println("[GrepMapper] count: " + count);
		}
	}

	public static final class GrepReducer extends Reducer<String, Integer, String, Integer> {
		private static final long serialVersionUID = 1L;
		private static final Integer one = new Integer("1");
		private int count = 0;
		
		
		@Override
		public void reduce(String key, Iterable<Integer> values,
				ChannelElementWriter<MRChannelElement<String, Integer>> writer) throws IOException {
			for (Integer value : values) {
				//System.out.println("[GrepReducer] write: " + key);
				writer.write(new MRChannelElement<String, Integer>(key, value));
				count++;
			}
		}
		
		@Override
		public void cleanup(ChannelElementWriter<MRChannelElement<String, Integer>> writer)
		{
			System.out.println("[GrepReducer] count: " + count);
		}
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println(usage);
			System.exit(1);
		}

		try {
			String type = args[0];
			String userName = args[2];
			String appName = args[3];
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
			config.setJobName(appName);
			config.setUserPoolName(userName);
			config.setMapperClass(GrepMapper.class);
			config.setReducerClass(GrepReducer.class);
			config.setReducerNum(nReducers);
			config.setCommunicationType(tType);
			MapReduceSpecification spec = ApplicationSpecFactory.getMapReduceApplicationSpecification(config);

			Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");
			manager.registerApplication(spec);
			System.out.println("[GrepMapReduce] Submit Application Done.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
