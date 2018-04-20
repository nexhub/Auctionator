package projekt2n40.auctionator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;


@SpringUI(path = "/auction")
@Configuration
@Title("Mietversteigerung")
public class AuktionUi extends UI
{
  @Autowired
  ApplicationArguments args;
  
  private static final long serialVersionUID = 1L;
  private Grid<Tenderer> grid = new Grid<Tenderer>(Tenderer.class);
  HashMap<String, Tenderer> tenderers;
  int[] sum;
  static Connection con = null;
  static AuktionUi self;
  int goal;
  int round = 1;

  public AuktionUi()
  {
    AuktionUi.self = this;
  }

  @Override
  protected void init(VaadinRequest vaadinRequest)
  {
    goal=Integer.valueOf(args.getSourceArgs()[0]);
    
    try
    {
      Class.forName("org.sqlite.JDBC");
      AuktionUi.con = DriverManager.getConnection("jdbc:sqlite:"+args.getSourceArgs()[1]);
    } catch (ClassNotFoundException | SQLException e)
    {
      e.printStackTrace();
    }
    
    try
    {
      tenderers = loadTenderer();
    } catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    sum = new int[round];
    for (Tenderer t : tenderers.values())
    {
      t.init();
      for (int i = 0; i < round; i++)
        sum[i] += t.getOffer(i);
    }

    Label head = new Label(String.format("Es sind %d Euro aufzubringen.", goal) + ((round > 0) ? String.format(" In der Letzten Runde wurden davon %.1f", 100.0 * sum[round - 1] / goal) + " % erreicht." + ((sum[round - 1] < goal) ? String.format(" Es Fehlen noch %d Euro", goal - sum[round - 1]) : "") : ""));
    VerticalLayout layout = new VerticalLayout(head, grid, new Button("Ich bin Neugierig", e -> {
      for (Tenderer t : tenderers.values())
        t.uncover();
    }));

    grid.setSelectionMode(SelectionMode.NONE);

    for (int i = 0; i <= round; i++)
    {
      final int z = i;
      if (z >= round)
      {
        grid.addComponentColumn(tenderer -> tenderer.getContent(z)).setCaption("Eingabe + Enter").setSortable(false).setWidth(150);;
        grid.addComponentColumn(tenderer -> tenderer.getStatusIcon(z)).setCaption("").setSortable(false).setWidth(55);        
      } else
      {
        grid.addComponentColumn(tenderer -> tenderer.getContent(z)).setCaption("Σ = " + sum[z]).setSortable(false);
      }
    }

    grid.getColumn("name").setSortable(false);

    grid.setItems(tenderers.values());
    //layout.setSizeFull();
    grid.setSizeFull();
    grid.setHeightByRows(tenderers.size());
    setContent(layout);
  }

  private HashMap<String, Tenderer> loadTenderer() throws ClassNotFoundException
  {
    HashMap<String, Tenderer> tMap = new HashMap<String, Tenderer>();
    Statement st = null;
    ResultSet rs = null;
    try
    {
      st = con.createStatement();
      st.setQueryTimeout(30); // set timeout to 30 sec.
      rs = st.executeQuery("SELECT MIN(x),MAX(x) FROM (SELECT MAX(round) x FROM offer WHERE value >= 0 GROUP BY name)");
      rs.next();
      round = rs.getInt(2);
      if (round == rs.getInt(1)) round++;
      rs.close();
      rs = st.executeQuery("SELECT * FROM offer");
      while (rs.next())
      {
        String name = rs.getString(1);
        int i = rs.getInt(2);
        int value = rs.getInt(3);
        Tenderer tenderer = tMap.get(name);
        if (tenderer == null) tMap.put(name, tenderer = new Tenderer(rs.getString(1), round));
        if (i>=0) tenderer.set(i, value);
      }
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
    return tMap;
  }
}
