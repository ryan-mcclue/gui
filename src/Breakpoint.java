public class
Breakpoint
{
  public static void
  bp()
  {
    throw new RuntimeException();
  }

  public static void
  bp(String msg)
  {
    throw new RuntimeException(msg);
  }
}
