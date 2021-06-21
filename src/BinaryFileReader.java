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
    ByteBuffer buf = read_bytes(stream, offset, 2)
    if (want_reversed)
    {
      buf.order(ByteOrder.LITTLE_ENDIAN);
    }
    return buf.getShort();
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

  public static String
  read_string(ByteArrayInputStream stream, int offset, int length)
  {
    ByteBuffer str_bytes = read_bytes(stream, offset, length);
    return StandardCharsets.UTF_8.decode(str_bytes).toString();
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

  public static int
  get_target_index(Loc, String s)
  {
    int V = pjw_hash(s);
    int S = Loc.num_hash_entries;

    // probably something to do with the collision handling
    // of the pjw hash
    int hash_cursor = V % S;
    int orig_hash_cursor = hash_cursor;
    int increment = 1 + (V % (S - 2));

    while (true)
    {
      int index = read_int(hash_table, 4 * hash_cursor);

      // empty
      if index == 0 break;

      index--;

      if get_source_string(loc, s, index).equals(s) return index;
      hash_cursor += increment;
      hash_cursor %= S;

      if hash_cursor == orig_hash_cursor break;
    }
  }

  //public static String
  //get_source_string(LocalisationText loc_text, int index)
  //{
  //  int str_length = loc_text.original_table_offset + 8 * index;
  //  int str_offset_addr = loc_text.original_table_offset + 8 * index + 4;

  //  int str_offset = read_int(mo_file_byte_stream, str_offset_addr); 

  //  return read_string(mo_data_byte_stream, str_offset, str_length);
  //}
  
  //public static String
  //get_translated_string(LocalisationText loc_text, int index)
  //{
  //  int str_length = loc_text.translated_table_offset + 8 * index;
  //  int str_offset_addr = loc_text.translated_table_offset + 8 * index + 4;

  //  int str_offset = read_int(mo_file_byte_stream, str_offset_addr); 

  //  return read_string(mo_data_byte_stream, str_offset, str_length);
  //}

  // hashing is a lossy mapping that allows for sparseness.
  // we go from 2^(large) to 2^(small) to store in array 
  // a valid hash function is to throw away bits, however what is typical
  // is to combine bits by analysing patterns of what we expect numbers to
  // be
  // of course there will be collisions as going from large to small
  
   public long 
   pjw_hash(String str)
   {
     long BitsInUnsignedInt = (long)(4 * 8);
     long ThreeQuarters     = ((BitsInUnsignedInt  * 3) / 4);
     long OneEighth         = (BitsInUnsignedInt / 8);
     long HighBits          = (long)(0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
     long hash              = 0;
     long test              = 0;

     for(int i = 0; i < str.length(); i++)
     {
       hash = (hash << OneEighth) + str.charAt(i);

       if((test = hash & HighBits)  != 0)
       {
         hash = (( hash ^ (test >> ThreeQuarters)) & (~HighBits));
       }
     }

     return hash;
   }

  //_i(String str)
  //{
  //  int target_index = get_target_index(locale, str);
  //  return get_translated_str(loc, target_index);
  //}

  public static void
  main(String[] args)
  {
    // globalLocText = make_localisation_text("l10n", "de");
    // fallbackLocText = make_localisation_text("l10n", "en");
    // str = i_("hdr_message_4");

    int num_strings = BinaryFileReader.read_int(mo_file_data, 8);
    int original_table_offset = BinaryFileReader.read_int(mo_file_data, 12);
    int translated_table_offset = BinaryFileReader.read_int(mo_file_data, 16);
    int hash_num_entries = BinaryFileReader.read_int(mo_file_data, 20);
    int hash_offset = BinaryFileReader.read_int(mo_file_data, 24);

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

    ByteArrayInputStream hash_table =
      new ByteArrayInputStream(mo_file_bytes, hash_offset, 
                               mo_file_bytes.length - hash_offset);


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
