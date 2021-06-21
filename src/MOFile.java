import java.io.*;
import java.nio.*;

public class
MOFile
{
  ByteArrayInputStream file_stream;
  int num_strings;
  int original_table_offset;
  int translated_table_offset;
  int num_hash_entries;
  int hash_offset;
  ByteArrayInputStream hash_table_stream;
  boolean is_reversed;

  public
  MOFile(String file_name)
  {
    byte[] file_bytes = 
      BinaryFileReader.read_entire_file(file_name); 

    this.file_stream = 
      new ByteArrayInputStream(file_bytes);

    int magic = BinaryFileReader.read_int(this.file_stream, 0, false);
    int target_magic = 0x950412de;
    int target_magic_reversed = 0xde120495;
    if (magic == target_magic)
    {
      this.is_reversed = true;
    }
    else if (magic == target_magic_reversed)
    {
      this.is_reversed = true;
    }
    else
    {
      Breakpoint.bp("File is not a valid .mo");
    }

    this.num_strings = 
      BinaryFileReader.read_int(this.file_stream, 8, this.is_reversed);

    this.original_table_offset = 
      BinaryFileReader.read_int(this.file_stream, 12, this.is_reversed);

    this.translated_table_offset = 
      BinaryFileReader.read_int(this.file_stream, 16, this.is_reversed);

    this.num_hash_entries = 
      BinaryFileReader.read_int(this.file_stream, 20, this.is_reversed);

    this.hash_offset = 
      BinaryFileReader.read_int(this.file_stream, 24, this.is_reversed);

    this.hash_table_stream =
      new ByteArrayInputStream(file_bytes, this.hash_offset, 
                               file_bytes.length - this.hash_offset);
  }

  public String
  get_string(String str)
  {
    int target_index = get_target_index(str);

    return get_translated_str(target_index);
  }

  public int
  get_target_index(String str)
  {
    int hash_value = this.pjw_hash(str);
    int hash_size = this.num_hash_entries;

    // NOTE(Ryan): Inevitable hash collision handling
    int hash_cursor = hash_value % hash_size;
    int orig_hash_cursor = hash_cursor;
    int hash_increment = 1 + (hash_value % (hash_size - 2));

    int final_index = -1;

    while (true)
    {
      int index = 
        BinaryFileReader.read_int(this.hash_table_stream, 4 * hash_cursor, this.is_reversed);

      if (index == 0)
      {
        Breakpoint.bp("Empty .mo file");
      }

      index--;

      String source_str = get_source_string(index);
      if (source_str.equals(str))
      {
        final_index = index;
        break;
      }

      hash_cursor += hash_increment;
      hash_cursor %= hash_size;

      if (hash_cursor == orig_hash_cursor)
      {
        break;
      }

    }

    return final_index;
  }

  public int 
  pjw_hash(String str)
  // hashing is a lossy mapping that allows for sparseness.
  // we go from 2^(large) to 2^(small) to store in array 
  // a valid hash function is to throw away bits, however what is typical
  // is to combine bits by analysing patterns of what we expect numbers to
  // be
  // of course there will be collisions as going from large to small
  {
    int BitsInUnsignedInt = (4 * 8);
    int ThreeQuarters     = ((BitsInUnsignedInt  * 3) / 4);
    int OneEighth         = (BitsInUnsignedInt / 8);
    int HighBits          = (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
    int hash              = 0;
    int test              = 0;

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

  public String
  get_source_string(int index)
  {
    int str_length = this.original_table_offset + 8 * index;
    int str_offset_addr = this.original_table_offset + 8 * index + 4;

    int str_offset = 
      BinaryFileReader.read_int(this.file_stream, str_offset_addr,
                                this.is_reversed); 

    return BinaryFileReader.read_string(this.file_stream, str_offset, 
                                        str_length, this.is_reversed);
  }

  public String
  get_translated_str(int index)
  {
    int str_length = this.translated_table_offset + 8 * index;
    int str_offset_addr = this.translated_table_offset + 8 * index + 4;

    int str_offset = 
      BinaryFileReader.read_int(this.file_stream, str_offset_addr,
                                this.is_reversed); 

    return BinaryFileReader.read_string(this.file_stream, str_offset, 
                                        str_length, this.is_reversed);
  }
}
