package utilities;

public class Pair<FIRST,SECOND> {
	private final FIRST first;
	private final SECOND second;
	
	public Pair(FIRST first, SECOND second)
	{
		this.first = first;
		this.second = second;
	}
	
	public final FIRST getFirst()
	{
		return first;
	}
	
	public final SECOND getSecond()
	{
		return second;
	}
}
