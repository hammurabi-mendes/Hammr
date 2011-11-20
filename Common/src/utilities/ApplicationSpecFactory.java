package utilities;

import java.io.IOException;
import java.util.List;

import communication.writer.ChannelElementWriter;

import appspecs.Node;
import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;

import conf.Config;

import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.appspecs.MapReduceConfiguration;
import mapreduce.communication.MRChannelElement;

import mapreduce.programs.Mapper;
import mapreduce.programs.MapperNode;
import mapreduce.programs.Reducer;
import mapreduce.programs.ReducerNode;

public class ApplicationSpecFactory {
	public static MapReduceSpecification getMapReduceApplicationSpecification(MapReduceConfiguration conf)
			throws InstantiationException, IllegalAccessException, IOException, OverlappingOutputException, InexistentInputException {

		MapReduceSpecification spec = new MapReduceSpecification(conf.getJobName(), conf.getUserPoolName(), Config.DEFAULT_HDFS_BASEDIR);

		List<String> inputs = DistributedFileSystemFactory.getDistributedFileSystem().list(spec.getAbsoluteFileName(conf.getInputfileDir()));
		if(inputs.size() == 0)
		{
			Logging.Info("[ApplicationSpecFactory][getMapReduceApplicationSpecification] Warning: No input files. InputDir: " + spec.getAbsoluteFileName(conf.getInputfileDir()));
		}
		
		int nMappers = 0;
		for(String input : inputs)
		{
			long fileLength = DistributedFileSystemFactory.getDistributedFileSystem().getFileLength(input);
			long blocksize = DistributedFileSystemFactory.getDistributedFileSystem().getBlockSize(input);
			nMappers += (int) ((fileLength / blocksize) + 1);
		}
		
		int nReducers = conf.getReducerNum();
		Class cMapper = conf.getMapperClass();
		Class cReducer = conf.getReducerClass();
		Class cCombiner = conf.getCombinerClass();
		
		Node[] mappers = new MapperNode[nMappers];
		Node[] reducers = new ReducerNode[nReducers];
		String[] outputs = new String[nReducers];

		for (int i = 0; i < mappers.length; i++) {
			if (cCombiner != null) {
				mappers[i] = (MapperNode) new MapperNode((Mapper) cMapper.newInstance(), (Reducer) cCombiner.newInstance());
			}
			else
			{
				mappers[i] = (MapperNode) new MapperNode((Mapper) cMapper.newInstance(), new Reducer(){
					@Override
					public void reduce(Comparable key, Iterable values, ChannelElementWriter writer) throws Exception {
						for(Object value : values)
						{
							writer.write(new MRChannelElement(key, value));
						}
					}
				});
			}
		}

		for (int i = 0; i < reducers.length; i++) {
			reducers[i] = (ReducerNode) new ReducerNode((Reducer)cReducer.newInstance());
			outputs[i] = String.format("%s.%d", conf.getOutputfileDir() + "/" + conf.getOutputFilenamePrefix(), i);
		}

		spec.insertMappers(inputs.toArray(new String[0]), mappers);
		spec.insertReducers(outputs, reducers);
		spec.setupCommunication(conf.getCommnicationType());

		return spec;
	}
}
