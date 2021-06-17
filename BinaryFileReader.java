import java.io.*;
import java.nio.*;

public class
BinaryFileReader
{
  public static
  byte[] read_entire_file(String file_name)
  {
    InputStream file_stream = null;
    try 
    {
      file_stream = new FileInputStream(file_name);
    }
    catch (FileNotFoundException e)
    {
      Breakpoint.bp(e.getMessage());
    }
    
    byte[] file_data = null;
    try
    {
      file_data = file_stream.readAllBytes();
      file_stream.close();
    }
    catch (IOException e)
    {
      Breakpoint.bp(e.getMessage());
    }
    
    return file_data;
  }

  public static long
  read_long(ByteArrayInputStream stream, int offset)
  {
    stream.reset();

    byte[] byte_holder = new byte[Long.BYTES];

    stream.skip(offset);
    int bytes_read = stream.read(byte_holder, 0, Long.BYTES);
    if (bytes_read == 0 || bytes_read == -1)
    {
      Breakpoint.bp();
    }

    long val = ByteBuffer.wrap(byte_holder).getLong();

    stream.reset();

    return val;
  }

  public static void
  main(String[] args)
  {
    byte[] data = BinaryFileReader.read_entire_file("file.txt"); 
  }
}
