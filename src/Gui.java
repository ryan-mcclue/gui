import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.event.*;
import javafx.scene.canvas.*;
import javafx.scene.input.*;
import javafx.geometry.*;

import java.nio.file.*;

class 
AutoScalingCanvas extends Region {

  private final Canvas canvas;

  public AutoScalingCanvas(double canvasWidth, double canvasHeight) {
    this.canvas = new Canvas(canvasWidth, canvasHeight);
    getChildren().add(canvas);
  }

  public GraphicsContext getGraphicsContext2D() {
    return canvas.getGraphicsContext2D();
  }

  @Override
  protected void layoutChildren() {
    double x = getInsets().getLeft();
    double y = getInsets().getTop();
    double w = getWidth() - getInsets().getRight() - x;
    double h = getHeight() - getInsets().getBottom() - y;

    // preserve aspect ratio while also staying within the available space
    double sf = Math.min(w / canvas.getWidth(), h / canvas.getHeight());
    canvas.setScaleX(sf);
    canvas.setScaleY(sf);

    positionInArea(canvas, x, y, w, h, -1, HPos.CENTER, VPos.CENTER);
  }
}

class
MenuState
{
  int music_volume = 0;
  MenuPage current_menu_page;
}

class
MenuPage
{
  int current_choice;
  int num_menu_items;
  abstract void draw();
}

MenuPage main;
MenuPage load;

public class 
Gui extends Application
{
  GraphicsContext gc;
  int render_width = 1280;
  int render_height = 720;

  boolean in_menu;
  int menu_choice;
  final int BIG_FONT_SIZE = 42;
  // it seems that scaling to non-integer will cause blurry text?
  // how to avoid? (just have standard window size, 
  // and them fullscreen size)
  // how to make fullscreen?
  Font menu_title_font = 
    Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 
              (int)(BIG_FONT_SIZE * 1.2));
  Font menu_body_font = 
    Font.font("Arial", FontWeight.BOLD,
              (int)(BIG_FONT_SIZE * 1.0));
  Point2D body_font_dim = get_text_dim(menu_body_font, "Ag");
  Font menu_version_font = 
    Font.font("Arial", FontWeight.BOLD,
              (int)(BIG_FONT_SIZE * 0.4));
  boolean asking_for_exit_confirmation;
  MenuState state;
  int menu_item_resume = -1;
  int menu_item_exit = -1;
  int num_menu_items = 2;
  int num_menu_items_drawn;

  @Override
  public void
  start(Stage primary_stage) throws Exception
  {
    primary_stage.setTitle("GUI");

    AutoScalingCanvas canvas = new AutoScalingCanvas(render_width, 
                                                     render_height);
    Scene scene = new Scene(canvas);
    scene.setFill(Color.BLACK);
    primary_stage.setScene(scene);

    gc = canvas.getGraphicsContext2D();

    gc.setTextAlign(TextAlignment.LEFT);
    gc.setTextBaseline(VPos.TOP);
    gc.setFontSmoothingType(FontSmoothingType.LCD);

    scene.setOnKeyPressed(new EventHandler<KeyEvent>(){
      @Override
      public void handle(KeyEvent event)
      {
        // esc_down = (event.getCode() == KeyCode.ESCAPE);
      }
    });

    scene.setOnKeyReleased(new EventHandler<KeyEvent>(){
      @Override
      public void handle(KeyEvent event)
      {
        if (event.getCode() == KeyCode.ESCAPE)
        {
          if (!in_menu)
          {
            in_menu = true;
            current_menu_page = main_menu_page;
          }
          else
          {
            if (current_menu_page == load_menu_page)
            {
              current_menu_page = main_menu_page;
            }
            else
            {
              in_menu = false;  
            }
          }

        }
        if (event.getCode() == KeyCode.DOWN)
        {
          advance_menu_choice(+1);
        }
        if (event.getCode() == KeyCode.UP)
        {
          advance_menu_choice(-1);
        }
        if (event.getCode() == KeyCode.ENTER)
        {
          // get a shorter name for this variable for this scope
          int choice = menu_choice;
          if (menu_choice == menu_item_exit)
          {
            if (asking_for_exit_confirmation)
            {
              Platform.exit();
            }
            else
            {
              asking_for_exit_confirmation = true;
            }
          }
          if (menu_choice == menu_item_resume)
          {
            in_menu = !in_menu;
          }

          if (menu_choice == menu_item_load)
          {
            current_menu_page = load_menu_page; 
            // reset_menu_item_indices
          }
        }
      }
    });

    new AnimationTimer()
    {
      @Override
      public void
      handle(long total_elapsed_time_ns)
      {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1280, 720);

        if (in_menu)
        {
          draw_menu();
        }
        else
        {
          gc.setFill(Color.RED);
          gc.fillRect(100, 100, 100, 100);
        }
      }
    }.start();

    primary_stage.show();
  }

  public void
  draw_menu()
  {
    num_menu_items_drawn = 0;
    // menu heading white
    // menu options greyed variants of white
    gc.setFill(Color.web("#03396c"));
    gc.fillRect(0, 0, render_width, render_height);

    String title = "Hello Ryan!";
    Point2D title_text_dim = get_text_dim(menu_title_font, title);
    double title_y = render_height * 0.16;
    draw_text(menu_title_font, title,
             (render_width / 2) - (title_text_dim.getX() / 2), 
             title_y, 
             Color.WHITE);
    // normalising = (val / max)

    String body = "Inquire";
    Point2D body_text_dim = get_text_dim(menu_body_font, body);
    draw_text(menu_body_font, body,
             (render_width / 2) - (body_text_dim.getX() / 2), 
             title_y + body_text_dim.getY() * 2, 
             Color.WHITE);

    String version = "Version 0.0.1";
    Point2D version_text_dim = get_text_dim(menu_version_font, version);
    draw_text(menu_version_font, version,
             (render_width - version_text_dim.getX()), 0, 
             Color.color(0.3, 0.3, 0.3, 0.4));

    double stride = 1.7 * body_font_dim.getY();

    // multiplying by 0.5 stays in float as oppose to integral division?
    double centre_x = render_width * 0.5;
    double cursor_y = render_height * 0.5;
    String resume = "Resume";
    Point2D resume_text_dim = get_text_dim(menu_body_font, resume);
    menu_item_resume = draw_menu_item(resume,
             (centre_x - (resume_text_dim.getX() / 2)), cursor_y);
    cursor_y += stride;

    String exit = "Exit";
    // if (menu_state.asking_for_confirmation)
    if (asking_for_exit_confirmation)
    {
      exit = "Exit? Are you sure?";
    }
    Point2D exit_text_dim = get_text_dim(menu_body_font, exit);
    menu_item_exit = draw_menu_item(exit,
             (centre_x - (exit_text_dim.getX() / 2)), cursor_y);
    cursor_y += stride;

    if (menu_choice != menu_item_exit)
    {
      asking_for_exit_confirmation = false;
    }
  }

  public Point2D
  get_text_dim(Font font, String str)
  {
    Text text = new Text(str); 
    text.setFont(font);
    return new Point2D((int)text.getBoundsInLocal().getWidth(),
                       (int)text.getBoundsInLocal().getHeight());
  }

  public void
  advance_menu_choice(int delta)
  {
    menu_choice += delta;
    if (menu_choice < 0)
    {
      menu_choice = (num_menu_items - 1);
    }
    if (menu_choice >= num_menu_items)
    {
      menu_choice = 0;
    }
  }

  public void
  draw_text(Font font, String text, double x, double y, Color color)
  {
    gc.setFont(font);
    gc.setFill(color);
    gc.fillText(text, (int)x, (int)y);
  }

  public double
  lerp(double s, double e, double p)
  {
    return ((1 - p) * s) + (p * e);
  }

  public Color
  lerp_color(Color s, Color e, double progression)
  {
    return Color.color(
             lerp(s.getRed(), e.getRed(), progression),
             lerp(s.getGreen(), e.getGreen(), progression),
             lerp(s.getBlue(), e.getBlue(), progression));
  }

  public int
  draw_menu_item(String text, double x, double y)
  {
    /*
     * HAVE SOME COUNTER THAT INDICATES HOW MANY SECONDS HAVE PASSED?
     * delta = time.now() - confirmation_time;
     * flash_duration_seconds = 1.0f;
     * color_prog = delta / flash_duration;
     * color_prog = sqrt(color_prog); convert linear to a hump
     * color_prog = 1 - color_prog; start on color
     * if (delta < 0)
     * {
     *   render_different_color 
     * }
     */

    // immediate mode is to just check where the mouse is on drawing
    // if retain state, gui library becomes very complicated
    Color item_color = Color.color(1, 1, 1);

    if (menu_choice == num_menu_items_drawn)
    {
      item_color = Color.color(1, 0, 0);

      // if want to strobe something, pass to cos.
      // multiplying will speed up strobe
      // TODO(Ryan): Calculate and use frame_dt
      double t = Math.cos(System.currentTimeMillis() / 300);
      // square as we only want positive values and will give a sharpness to it
      t *= t;
      // accelerate towards 1.0
      // however, don't go all the way to 1.0
      t = 0.4 + 0.54 * t; 
      Color backing_color = lerp_color(item_color, Color.WHITE, t); 
      int offset = (int)(body_font_dim.getY() / 40);
      draw_text(menu_body_font, text, x + offset, y - offset, backing_color);
    }

    draw_text(menu_body_font, text, x, y, item_color);

    int index = num_menu_items_drawn;
    num_menu_items_drawn += 1;
    return index;
  }
}

class
LocalizationText 
{
  char* mo_data;
  boolean is_reversed; // endianness?

  // i18n approx. 16:00
  // INVESTIGATE READING BINARY FILE
  public
  LocalizationText(String folder, String language_name)
  {
    String name = System.format("%s/%s", folder, language_name);
    byte[] data = read_entire_file(name);
    if (data.length < 24)
    {
      // too short to be a valid .mo file
    }

    InputStream file_reader = new ByteArrayInputStream(data);

    byte[] val = new byte[5];
    int bytes_read = file_reader.read(val, 0, 5);
    // sets pos to 0
    file_reader.reset();
     
  }

  public long
  bin_read_long(ByteArrayInputStream input_stream, int offset)
  {
    file_reader.reset();

    file_reader.skip(offset);
    byte[] val = new byte[Long.BYTES];
    int bytes_read = file_reader.read(val, 0, Long.BYTES);

    return ByteBuffer.wrap(val).longValue();

    file_reader.reset();
  }
}
