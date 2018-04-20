package projekt2n40.Auktionator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.UserError;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@StyleSheet("/custom.css")
public class Tenderer
{

  private String name;
  private int round;
  private int[] offerList;
  private TextField[] tfList;
  private Label[] iconList;

  public Tenderer(String name, int round)
  {
    offerList = new int[round + 1];
    tfList = new TextField[round + 1];
    iconList = new Label[round + 1];
    this.name = name;
    this.round = round;
  }

  public void init()
  {
    Notification notif = new Notification("");
    notif.setPosition(Position.TOP_CENTER);
    notif.setHtmlContentAllowed(true);
    notif.setDelayMsec(5000);
    for (int i = 0; i < round + 1; i++)
    {
      iconList[i] = new Label(VaadinIcons.CHECK.getHtml(), ContentMode.HTML);
      iconList[i].setVisible(false);
      TextFieldX tf = new TextFieldX(this, iconList[i]);
      tf.setWidth("100px");
      tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
      if (i < round)
      {
        tf.setEnabled(false);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setPlaceholder("???");
      } else
      {
        if (offerList[round] > 0)
        {
          tf.setPlaceholder("Ändern");
          tf.getStatusIcon().setVisible(true);
        } else tf.setPlaceholder("Eintragen");
        tf.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, null)
        {
          private static final long serialVersionUID = 1L;

          @Override
          public void handleAction(Object sender, Object target)
          {
            TextFieldX tf = (TextFieldX) target;
            int value = 0;
            try
            {
              value = Integer.valueOf(tf.getValue());
              if (value <= 0) throw new NumberFormatException();
              update(tf.getTenderer().getName(), tf);
            } catch (NumberFormatException e)
            {
              Statement stx = null;
              try
              {
                stx = AuktionUi.con.createStatement();
                stx.executeUpdate("REPLACE INTO offer VALUES('" + tf.getTenderer().getName() + "'," + round + ",-1)");
              } catch (SQLException ex)
              {
                ex.printStackTrace();
              } finally
              {
                try
                {
                  if (stx != null) stx.close();
                } catch (SQLException ex)
                {}
              }
              notif.setCaption("Nur ganze Euro Beträge > 0 sind Gültig.");
              notif.setIcon(VaadinIcons.CLOSE_CIRCLE_O);
              notif.show(Page.getCurrent());
              tf.getTenderer().set(round, 0);
              tf.getStatusIcon().setVisible(false);
              tf.setPlaceholder("Eintragen");
              tf.setValue("");
            }
          }

          void update(String id, TextFieldX tf)
          {
            Statement st = null;
            ResultSet rs = null;
            boolean b = false;
            try
            {
              st = AuktionUi.con.createStatement();
              st.setQueryTimeout(30); // set timeout to 30 sec.
              rs = st.executeQuery("SELECT * FROM (SELECT name, MAX(round) x FROM offer WHERE value >= 0 GROUP BY name) WHERE x = " + (round - 1) + " AND name!='" + id + "'");
              b = !rs.next();
            } catch (SQLException e)
            {
              e.printStackTrace();
            } finally
            {
              try
              {
                if (rs != null) rs.close();
                if (st != null) st.close();
              } catch (SQLException e)
              {}
            }
            if (b)
            {
              String s;
              int value = Integer.valueOf(tf.getValue());
              int last = (round == 0) ? 0 : AuktionUi.self.tenderers.get(id).offerList[round - 1];
              int lastSum = (round == 0) ? 0 : AuktionUi.self.sum[round - 1];
              if (value < last) s = "Du hast dein Gebot um " + (last - value) + " auf " + value + " reduziert.";
              else s = "Du hast dein Gebot um " + (value - last) + " auf " + value + " erhöht." + ((AuktionUi.self.goal > lastSum) ? " Damit trägst du " + String.format("%.1f", 100.0 * (value - last) / (AuktionUi.self.goal - lastSum)) + "% des Restbetrages." : "");
              ConfirmDialog.show(AuktionUi.self, "Letzte Chance! Ende der Runde!", s, "OK", "Abrechen", dialog -> {
                if (dialog.isConfirmed())
                {
                  Statement stx = null;
                  try
                  {
                    stx = AuktionUi.con.createStatement();
                    stx.executeUpdate("REPLACE INTO offer VALUES('" + id + "'," + round + "," + tf.getValue() + ")");
                  } catch (SQLException e)
                  {
                    e.printStackTrace();
                  } finally
                  {
                    try
                    {
                      if (stx != null) stx.close();
                    } catch (SQLException e)
                    {}
                  }
                  Page.getCurrent().reload();
                } else
                {
                  notif.setCaption("Wiederhole Deine Eingabe");
                  notif.setIcon(VaadinIcons.CLOSE_CIRCLE_O);
                  notif.show(Page.getCurrent());
                  tf.getTenderer().set(round, 0);
                  tf.setComponentError(new UserError("Wiederhole Deine Eingabe"));
                  tf.setPlaceholder("Eintragen");
                  tf.getStatusIcon().setVisible(false);
                  tf.setValue("");
                }
              });
            } else
            {
              Statement stx = null;
              try
              {
                stx = AuktionUi.con.createStatement();
                stx.executeUpdate("REPLACE INTO offer VALUES('" + id + "'," + round + "," + tf.getValue() + ")");
              } catch (SQLException e)
              {
                e.printStackTrace();
              } finally
              {
                try
                {
                  if (stx != null) stx.close();
                } catch (SQLException e)
                {}
              }
              int value = Integer.valueOf(tf.getValue());
              int last = (round == 0) ? 0 : AuktionUi.self.tenderers.get(id).offerList[round - 1];
              int lastSum = (round == 0) ? 0 : AuktionUi.self.sum[round - 1];
              if (value < last) notif.setCaption("Du hast dein Gebot um " + (last - value) + " auf " + value + " reduziert.");
              else notif.setCaption("Du hast dein Gebot um " + (value - last) + " auf " + value + " erhöht." + ((AuktionUi.self.goal > lastSum) ? "<br /> Damit trägst Du <b>" + String.format("%.1f", 100.0 * (value - last) / (AuktionUi.self.goal - lastSum)) + "%</b>  des Restbetrages." : ""));
              notif.setIcon(VaadinIcons.CHECK_CIRCLE_O);
              notif.show(Page.getCurrent());
              tf.getTenderer().set(round, Integer.valueOf(value));
              tf.setComponentError(null);
              tf.setPlaceholder("Ändern");
              tf.getStatusIcon().setVisible(true);
              tf.setValue("");
            }
          }
        });
      }
      tfList[i] = tf;

    }

  }

  public void uncover()
  {
    for (int i = 0; i < round; i++)
      tfList[i].setValue(String.valueOf(offerList[i]));
  }

  public void update()
  {}

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getOffer(int i)
  {
    return offerList[i];
  }

  public int size()
  {
    return round;
  }

  public void set(int i, int value)
  {
    offerList[i] = value;
  }

  public Component getContent(int i)
  {
    return tfList[i];
  }

  public Component getStatusIcon(int i)
  {
    return iconList[i];
  }

  class TextFieldX extends TextField
  {
    private static final long serialVersionUID = 1L;
    private Tenderer tenderer;
    private Label statusIcon;

    public TextFieldX(Tenderer tenderer, Label statusIcon)
    {
      this.tenderer = tenderer;
      this.statusIcon = statusIcon;
    }

    public Tenderer getTenderer()
    {
      return tenderer;
    }

    public Label getStatusIcon()
    {
      return statusIcon;
    }
  }
}
