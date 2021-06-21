import java.io.*;
import java.nio.*;
import java.nio.charset.*;

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

  // IMPORTANT(Ryan): Most CPUs these days have an instruction for this
  public static int
  swap_int_bytes(int val)
  {
    // NOTE(Ryan): 0xff maps to 0x000000ff
    int b0 = val & 0xff; 
    int b1 = (val >> 8) & 0xff;
    int b2 = (val >> 16) & 0xff;
    int b3 = (val >> 24) & 0xff;

    return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
  }

  //public static String
  //get_source_string(LocalisationText loc_text, int index)
  //{
  //  int str_length = loc_text.original_table_offset + 8 * index;
  //  int str_offset_addr = loc_text.original_table_offset + 8 * index + 4;

  //  int str_offset = read_int(mo_file_byte_stream, str_offset_addr); 

  //  return read_string(mo_data_byte_stream, str_offset, str_length);
  //}


  public static void
  main(String[] args)
  {
    // globalLocText = make_localisation_text("data/strings", "de");
    // fallbackLocText = make_localisation_text("data/strings", "en");
    // get_string_from_global_loc_text("", "")

    // return byte[] so can copy
    ByteArrayInputStream data = 
      BinaryFileReader.read_entire_file("nl.mo"); 

   // ByteArrayInputStream advanced_data =
   //   new ByteArrayInputStream(data, offset, length);
   //   data + 10;
  
    byte[] val = new byte[4];  
    ByteBuffer buf = ByteBuffer.wrap(val);
    String s = StandardCharsets.UTF_8.decode(buf).toString();

    boolean is_reversed = false;

    int magic = BinaryFileReader.read_int(data, 0);
    int target_magic = 0x950412de;
    int target_magic_reversed = 0xde120495;
    if (magic == target_magic_reversed)
    {
      is_reversed = true;
    }

    int num_strings = BinaryFileReader.read_int(data, 8);
    int original_table_offset = BinaryFileReader.read_int(data, 12);
    int translated_table_offset = BinaryFileReader.read_int(data, 16);
    int hash_num_entries = BinaryFileReader.read_int(data, 20);
    int hash_offset = BinaryFileReader.read_int(data, 24);

    if (is_reversed)
    {
      num_strings = BinaryFileReader.swap_int_bytes(num_strings);
      original_table_offset = 
        BinaryFileReader.swap_int_bytes(original_table_offset);
      translated_table_offset = 
        BinaryFileReader.swap_int_bytes(translated_table_offset);
      hash_num_entries = 
        BinaryFileReader.swap_int_bytes(hash_num_entries);           
      hash_offset = BinaryFileReader.swap_int_bytes(hash_offset);
    }
    // byte hash_table = data + hash_offset;
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
