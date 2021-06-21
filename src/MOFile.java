public class
MOFile
{
  ByteArrayInputStream file_stream;
  int num_strings;
  int original_table_offset;
  int translated_table_offset;
  int hash_num_entries;
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

    int magic = BinaryFileReader.read_int(this.file_stream, 0);
    int target_magic = 0x950412de;
    int target_magic_reversed = 0xde120495;
    if (mo_magic == target_magic)
    {
      this.is_reversed = true;
    }
    else if (mo_magic == target_magic_reversed)
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
    this.hash_num_entries = 
      BinaryFileReader.read_int(this.file_stream, 20, this.is_reversed);
    this.hash_offset = 
      BinaryFileReader.read_int(this.file_stream, 24, this.is_reversed);

  }
}
