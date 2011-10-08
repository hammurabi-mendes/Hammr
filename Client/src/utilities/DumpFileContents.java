package utilities;

import java.io.FileNotFoundException;
import java.io.IOException;

import communication.ChannelElement;
import communication.FileChannelElementReader;

public class DumpFileContents {
	private String filename;

	public DumpFileContents(String filename) {
		this.filename = filename;
	}

	public void dump() throws FileNotFoundException, IOException {
		FileChannelElementReader reader = new FileChannelElementReader(filename);

		ChannelElement element;

		while((element = reader.read()) != null) {
			System.out.println(element);
		}
	}
}
