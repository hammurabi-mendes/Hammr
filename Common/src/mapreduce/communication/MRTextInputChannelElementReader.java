package mapreduce.communication;

import java.io.EOFException;
import java.io.IOException;


import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

import communication.channel.DistributedDataInputChannel;
import communication.channel.DistributedFileInputChannel;
import communication.channel.DistributedFileSplitInputChannel;
import communication.reader.ChannelElementReader;

import utilities.DistributedFileSystemFactory;


/**
 * A channel element reader for plain text HDFS files. Files are broken into
 * lines. Each ChannelElement is one line of file. Generates
 * MRChannelElement<Long,String>, where Long is the ending pos of the line and the
 * Text is the content of the line.
 * 
 * @author ljin
 * 
 */

public class MRTextInputChannelElementReader implements ChannelElementReader {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LineReader in = null;
	private long pos = -1;
	private final long end;
	private Text buffer = null;

	public MRTextInputChannelElementReader(DistributedDataInputChannel channel) throws IOException {
		if (channel instanceof DistributedFileInputChannel) {
			pos = 0;
		} else if (channel instanceof DistributedFileSplitInputChannel) {
			pos = ((DistributedFileSplitInputChannel) channel).getOffset();
		}
		end = pos + channel.getLength();
		buffer = new Text();

		FSDataInputStream inputStream = null;
		try {
			inputStream = (FSDataInputStream) DistributedFileSystemFactory.getDistributedFileSystem().open(
					channel.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean skipFirstLine = false;

		if (pos != 0) {
			skipFirstLine = true;
			--pos;
			inputStream.seek(pos);
		}
		in = new LineReader(inputStream);

		if (skipFirstLine) {
			pos += in.readLine(new Text());
		}
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public MRChannelElement<Long,String> read() throws EOFException, IOException {
		if (pos >= end) {
			throw new EOFException();
		} else {
			pos += in.readLine(buffer);
			// System.out.println("[TextInputChannelElementReader] Read: " +
			// buffer.toString());
			return new MRChannelElement<Long,String>(pos,buffer.toString());
		}
	}
}
