import java.io.*;
import java.nio.*;

public class
BinaryFileReader
{
  public static
  ByteArrayInputStream read_entire_file(String file_name)
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
    
    return new ByteArrayInputStream(file_data);
  }

  public static boolean
  areLittleEndian()
  {
    return ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
  }

  private static ByteBuffer
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

  public static <T> T
  read_val(ByteArrayInputStream stream, Class<T> type, int offset)
  {
    byte[] byte_holder = null;
    if (type == Integer.class)
    {
      ByteBuffer val = read_bytes(stream, offset, Integer.BYTES);
      return Integer.valueOf(val.getInt());
    }
  }

  public static void
  main(String[] args)
  {
    ByteArrayInputStream data = 
      BinaryFileReader.read_entire_file("file.bin"); 
    int num = 
      BinaryFileReader.read_val(data, Integer.class, 0).intValue();
  }
}

class
LocalisationText
{
  String mo_data;
  boolean reversed; // endianness
  int num_strings;
  int original_table_offset;
  int translated_table_offset;
  int hash_num_entries;
  int hash_offset;

  char hash_table; // is void*
  boolean is_language_right_to_left;

  public
  LocalisationText(String folder, String language_name)
  {
    String name = String.format("%s/%s", folder, language_name);

    ByteArrayInputStream data = 
      BinaryFileReader.read_entire_file(name);

    // this.hash_table = (void *)data;
    int target_magic = 0x950412de;
    int target_magic_reversed = 0xde120495;
    //int magic = BinaryFileReader.read_val(data, Integer.class, 0);
    //if (magic == target_magic)
    //{

    //}

    data.close();
  }
}
