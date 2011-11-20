package mapreduce.appspecs;

import mapreduce.appspecs.MapReduceSpecification.Type;
import mapreduce.programs.Reducer;

import mapreduce.programs.Mapper;

/**
 * Configuration for a mapred job.
 * @author ljin
 *
 */
public final class MapReduceConfiguration {
	//relative path 
	private String m_sInputfileDir = "input";
	private String m_sOutputfileDir = "output";
	private String m_sJobName;
	private String m_sUserPoolName = "anonymous";
	private int m_nReducers;
	private String m_sOutputFilenamePrefix = "part";
	private MapReduceSpecification.Type m_communicationType = MapReduceSpecification.Type.FILEBASED;
	
	private Class<? extends Mapper> m_cMapper = null;
	private Class<? extends Reducer> m_cReducer = null;
	private Class<? extends Reducer> m_cCombiner = null;
	
	public void setInputfileDir(String dir)
	{
		m_sInputfileDir = dir;
	}
	
	public String getInputfileDir()
	{
		return m_sInputfileDir;
	}
	
	public void setOutputfileDir(String dir)
	{
		m_sOutputfileDir = dir;
	}
	
	public String getOutputfileDir()
	{
		return m_sOutputfileDir;
	}
	
	public void setMapperClass(Class<? extends Mapper<?,?,?,?>> clazz)
	{
		m_cMapper = clazz;
	}
	
	public Class<? extends Mapper> getMapperClass(){
		return m_cMapper;
	}
	
	public void setReducerClass(Class<? extends Reducer> clazz)
	{
		m_cReducer = clazz;
	}
	
	public Class<? extends Reducer> getReducerClass(){
		return m_cReducer;
	}
	
	public int getReducerNum()
	{
		return m_nReducers;
	}
	
	public String getJobName()
	{
		return m_sJobName;
	}
	
	public String getOutputFilenamePrefix()
	{
		return m_sOutputFilenamePrefix;
	}
	
	public MapReduceSpecification.Type getCommnicationType()
	{
		return m_communicationType;
	}

	public Class getCombinerClass() {
		return m_cCombiner;
	}

	public void setJobName(String name) {
		m_sJobName = name;
	}

	public void setUserPoolName(String userPoolName) {
		m_sUserPoolName = userPoolName;
	}
	
	public void setReducerNum(int nreducers) {
		m_nReducers = nreducers;		
	}

	public void setCombinerClass(Class<? extends Reducer> clazz) {
		m_cCombiner = clazz;
	}

	public void setOutputFilePrefix(String outputfilenameprefix) {
		m_sOutputFilenamePrefix = outputfilenameprefix;
	}

	public void setCommunicationType(Type tType) {
		m_communicationType = tType;	
	}

	public String getUserPoolName() {
		return m_sUserPoolName;
	}
}
