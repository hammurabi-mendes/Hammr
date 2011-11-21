package client;

import fs.FileSystem;
import interfaces.Manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;

import utilities.ApplicationSpecFactory;
import utilities.DistributedFileSystemFactory;
import utilities.RMIHelper;

import communication.writer.ChannelElementWriter;

import mapreduce.appspecs.MapReduceConfiguration;
import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.communication.MRChannelElement;
import mapreduce.programs.Mapper;
import mapreduce.programs.Reducer;

public class PiMapReduce {

	private static String usage = "PiMapReduce <nMappers> <nSamples>";
	private static String registryLocation = null;

	static {
		String property = "java.rmi.server.location";
		registryLocation = System.getProperty(property);
		if (registryLocation == null) {
			System.err.printf("Failed to get %s. Exit.\n", property);
			System.exit(1);
		}
	}

	private static class HaltonSequence {
		/** Bases */
		static final int[] P = { 2, 3 };
		/** Maximum number of digits allowed */
		static final int[] K = { 63, 40 };

		private long index;
		private double[] x;
		private double[][] q;
		private int[][] d;

		/**
		 * Initialize to H(startindex), so the sequence begins with
		 * H(startindex+1).
		 */
		HaltonSequence(long startindex) {
			index = startindex;
			x = new double[K.length];
			q = new double[K.length][];
			d = new int[K.length][];
			for (int i = 0; i < K.length; i++) {
				q[i] = new double[K[i]];
				d[i] = new int[K[i]];
			}

			for (int i = 0; i < K.length; i++) {
				long k = index;
				x[i] = 0;

				for (int j = 0; j < K[i]; j++) {
					q[i][j] = (j == 0 ? 1.0 : q[i][j - 1]) / P[i];
					d[i][j] = (int) (k % P[i]);
					k = (k - d[i][j]) / P[i];
					x[i] += d[i][j] * q[i][j];
				}
			}
		}

		/**
		 * Compute next point. Assume the current point is H(index). Compute
		 * H(index+1).
		 * 
		 * @return a 2-dimensional point with coordinates in [0,1)^2
		 */
		double[] nextPoint() {
			index++;
			for (int i = 0; i < K.length; i++) {
				for (int j = 0; j < K[i]; j++) {
					d[i][j]++;
					x[i] += q[i][j];
					if (d[i][j] < P[i]) {
						break;
					}
					d[i][j] = 0;
					x[i] -= (j == 0 ? 1.0 : q[i][j - 1]);
				}
			}
			return x;
		}
	}

	public static class QmcMapper extends Mapper<Long, String, Integer, Long> {
		private static final long serialVersionUID = 1L;

		@Override
		public void map(Long key, String value, ChannelElementWriter<MRChannelElement<Integer, Long>> writer)
				throws Exception {
			StringTokenizer st = new StringTokenizer(value);
			long offset = Long.parseLong(st.nextToken());
			long num = Long.parseLong(st.nextToken());

			final HaltonSequence haltonsequence = new HaltonSequence(offset);
			long numInside = 0L;
			long numOutside = 0L;

			for (long i = 0; i < num; ++i) {
				// generate points in a unit square
				final double[] point = haltonsequence.nextPoint();

				// count points inside/outside of the inscribed circle of the
				// square
				final double x = point[0] - 0.5;
				final double y = point[1] - 0.5;
				if (x * x + y * y > 0.25) {
					numOutside++;
				} else {
					numInside++;
				}
			}
			writer.write(new MRChannelElement<Integer, Long>(1, numInside));
			writer.write(new MRChannelElement<Integer, Long>(0, numOutside));
			System.out.println("[PiMapReduce][map] numInside: " + numInside);
			System.out.println("[PiMapReduce][map] numOutside: " + numOutside);
		}
	}

	public static class QmcReducer extends Reducer<Integer, Long, Double, Integer> {

		private long numInside = 0;
		private long numOutside = 0;

		@Override
		public void reduce(Integer inside, Iterable<Long> values,
				ChannelElementWriter<MRChannelElement<Double, Integer>> writer) throws Exception {
			System.out.println("[PiMapReduce][reduce] inside: " + inside + " values: " + values);
			if (inside == 1) {
				for (long value : values) {
					numInside += value;
				}
			} else {
				for (long value : values) {
					numOutside += value;
				}
			}
		}

		@Override
		public void cleanup(ChannelElementWriter<MRChannelElement<Double, Integer>> writer) throws IOException {
			System.out.println("[QmcReducer][cleanup] numInside: " + numInside + " numOutside: " + numOutside);
			writer.write(new MRChannelElement<Double, Integer>(4 * (double) numInside / (numInside + numOutside), 1));
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println(usage);
			System.exit(2);
		}

		final int nMappers = Integer.parseInt(args[0]);
		final long nSamples = Long.parseLong(args[1]);
		try {
			System.out.println("Number of Maps  = " + nMappers);
			System.out.println("Samples per Map = " + nSamples);
			MapReduceConfiguration config = new MapReduceConfiguration();
			config.setJobName(PiMapReduce.class.getSimpleName());
			config.setUserPoolName("anonymous");
			config.setMapperClass(QmcMapper.class);
			config.setReducerClass(QmcReducer.class);
			config.setReducerNum(1);

			MapReduceSpecification spec = ApplicationSpecFactory.getMapReduceApplicationSpecification(config);

			// Generator input files
			String inputDir = spec.getAbsoluteFileName(config.getInputfileDir());
			FileSystem fs = DistributedFileSystemFactory.getDistributedFileSystem();
			List<String> tmpFiles = fs.list(inputDir);
			for (String tmpFile : tmpFiles) {
				fs.remove(tmpFile);
			}
			
			for (int i = 0; i < nMappers; ++i) {
				String inputPath = inputDir + "/input." + i;
				System.out.println("Create Input: " + inputPath);
				OutputStream oStream = fs.create(inputPath);
				oStream.write(String.valueOf(i * nSamples).getBytes("utf-8"));
				oStream.write(" ".getBytes("utf-8"));
				oStream.write(String.valueOf(nSamples).getBytes("utf-8"));
				oStream.close();
			}
			
			spec = ApplicationSpecFactory.getMapReduceApplicationSpecification(config);
			
			Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");
			manager.registerApplication(spec);
			System.out.println("[GrepMapReduce] Submit Application Done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
