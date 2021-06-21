import java.io.*;
import java.nio.*;
import java.nio.charset.*;

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

  public static ByteBuffer
  read_bytes(ByteArrayInputStream stream, int offset, int num_bytes)
  {
    stream.reset();
    stream.skip(offset);

    byte[] byte_holder = new byte[num_bytes];
    int bytes_read = stream.read(byte_holder, 0, byte_holder.length);
    if (bytes_read == 0 || bytes_read == -1)
    {
      Breakpoint.bp();
    }

    return ByteBuffer.wrap(byte_holder);
  }

  public static byte 
  read_byte(ByteArrayInputStream stream, int offset)
  {
    return read_bytes(stream, offset, 1).get();
  }

  public static short 
  read_short(ByteArrayInputStream stream, int offset, boolean want_reversed) 
  {
    ByteBuffer buf = read_bytes(stream, offset, 2);
    if (want_reversed)
    {
      buf.order(ByteOrder.LITTLE_ENDIAN);
    }
    return buf.getShort();
  }

  public static int 
  read_int(ByteArrayInputStream stream, int offset, boolean want_reversed)
  {
    ByteBuffer buf = read_bytes(stream, offset, 4);
    if (want_reversed)
    {
      buf.order(ByteOrder.LITTLE_ENDIAN);
    }
    return buf.getInt();
  }

  public static long 
  read_long(ByteArrayInputStream stream, int offset, boolean want_reversed)
  {
    ByteBuffer buf = read_bytes(stream, offset, 8);
    if (want_reversed)
    {
      buf.order(ByteOrder.LITTLE_ENDIAN);
    }
    return buf.getLong();
  }

  public static String
  read_string(ByteArrayInputStream stream, int offset, int length, 
              boolean want_reversed)
  {
    ByteBuffer buf = read_bytes(stream, offset, length);
    if (want_reversed)
    {
      buf.order(ByteOrder.LITTLE_ENDIAN);
    }
    return StandardCharsets.UTF_8.decode(buf).toString();
  }

  public static void
  main(String[] args)
  {
    MOFile mo_file = new MOFile("nl.mo"); 

    String s = mo_file.get_string("Resume");
  }
  // IMPORTANT(Ryan): Most CPUs these days have an instruction for this
  // NOTE(Ryan): 0xff is an integer literal mapping to 0x000000ff.
  // sign extension occurs with non-literal or cast
}
