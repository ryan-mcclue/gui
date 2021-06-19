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
  read_short(ByteArrayInputStream stream, int offset)
  {
    return read_bytes(stream, offset, 2).getShort();
  }

  public static int 
  read_int(ByteArrayInputStream stream, int offset)
  {
    return read_bytes(stream, offset, 4).getInt();
  }

  public static long 
  read_long(ByteArrayInputStream stream, int offset)
  {
    return read_bytes(stream, offset, 8).getLong();
  }

  public static int
  swap_int_bytes(int val)
  {
    // 0xff maps to 0x000000ff
    int b0 = val & 0xff; 
    int b1 = (val >> 8) & 0xff;
    int b2 = (val >> 16) & 0xff;
    int b3 = (val >> 24) & 0xff;

    return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
  }

  public static void
  main(String[] args)
  {
    ByteArrayInputStream data = 
      BinaryFileReader.read_entire_file("nl.mo"); 

    boolean is_reversed = false;

    int magic = BinaryFileReader.read_int(data, 0);
    int target_magic = 0x950412de;
    int target_magic_reversed = 0xde120495;
    if (magic == target_magic)
    {
      is_reversed = false;
    }
    if (magic == target_magic_reversed)
    {
      is_reversed = true;
    }

    int val = swap_int_bytes(0xdeadbeef);
    String new_val = String.format("0x%08x", val);

    // need to handle endianness
    int num_strings = BinaryFileReader.read_int(data, 8);
    //int original_table_offset(12);
    //int translated_table_offset(16);
    //int hash_num_entries(20);
    //int hash_offset(24);

    //byte hash_table = data + hash_offset; 
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

    // data.close();
  }
}
