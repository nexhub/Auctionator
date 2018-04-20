package projekt2n40.Auktionator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AuktionatorApplication
{
  public static void main(String[] args) throws ClassNotFoundException, IOException
  {
    final String jarName = new ApplicationHome(AuktionatorApplication.class).getSource().getName();
    final String hint = "\n java [-Dserver.port=PORT] -jar " + jarName + " auction-file.db START \n\n with an existing auktion-file, or \n\n java -jar " + jarName + " auction-file.db CREATE 5000 Anna Max 'Alex P.' ... \n\n to create a new auction-file with the value of 5000 with the bidders Anna, Max, Alex P. etc";

    Class.forName("org.sqlite.JDBC");
    int goal = -1;
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;

    if (args.length == 2 && args[1].toUpperCase().equals("START"))
    {
      if (args.length > 1) try
      {
        con = DriverManager.getConnection("jdbc:sqlite:" + args[0]);
        st = con.createStatement();
        st.setQueryTimeout(30);
        rs = st.executeQuery("SELECT * FROM init");
        if (rs.next()) goal = rs.getInt(1);
        if (goal < 0) throw new SQLException();
      } catch (SQLException e)
      {
        System.err.println("Can not handle auktion-file: " + args[0] + " try\n" + hint);
        return;
      } finally
      {
        try
        {
          if (rs != null) rs.close();
          if (st != null) st.close();
          if (con != null) con.close();
        } catch (SQLException e)
        {}
      }

      SpringApplication.run(AuktionatorApplication.class, goal + "", args[0]);
      return;
    } else if (args.length > 3 && args[1].toUpperCase().equals("CREATE"))
    {
      try
      {
        goal = Integer.parseInt(args[2]);
        if (goal < 1) throw new NumberFormatException();
      } catch (NumberFormatException e)
      {
        System.err.println("auctionator: missing or wrong parameters. try\n" + hint);
        return;
      }
      File f = new File(args[0]);
      if (f.exists() && !f.isDirectory())
      {
        System.out.print("File " + args[0] + " will be replaced. Are you sure? (y/N) ");
        int key = System.in.read();
        if (key == 'y' || key == 'Y' || key == 'j' || key == 'J')
        {
          System.out.println(" Bakup old auktion-file to: " + args[0] + ".backup");
          f.renameTo(new File(args[0] + ".backup"));
          f.delete();
        } else return;
      }
      try
      {
        con = DriverManager.getConnection("jdbc:sqlite:" + args[0]);
        con.setAutoCommit(false);
        st = con.createStatement();
        st.setQueryTimeout(30);
        st.executeUpdate("CREATE TABLE `init` ( `goal` INTEGER )");
        st.executeUpdate("INSERT INTO `init`(`goal`) VALUES (" + args[2] + ");");
        st.executeUpdate("CREATE TABLE `offer` ( `name` TEXT, `round` INTEGER, `value` INTEGER, PRIMARY KEY(`name`,`round`))");
        for (int i = 3; i < args.length; i++)
          st.executeUpdate("INSERT INTO `offer` (`name`,`round`,`value`) VALUES ('" + args[i] + "',-1,0);");
        con.commit();
        System.out.println("\nCREATE " + args[0] + "  Start Auktionator with:\n java -jar " + jarName + " " + args[0] + " START");
      } catch (SQLException e)
      {
        System.err.println("Can not create auktion-file: " + args[0]);
        e.printStackTrace();
        return;
      } finally
      {
        try
        {
          if (rs != null) rs.close();
          if (st != null) st.close();
          if (con != null) con.close();
        } catch (SQLException e)
        {}
      }

    } else System.err.println("Auctionator: missing or wrong parameters. try\n" + hint);

  }

  @Component
  public class CommandLineAppStartupRunner implements CommandLineRunner
  {

    @Autowired
    Environment environment;

    @Override
    public void run(String... args) throws Exception
    {
      System.out.println("\n----------------------\n\n Auctionator-WebAPP has started. Try  http://localhost:"+environment.getProperty("local.server.port")+"/auction  in your browser.\n\n----------------------\n");
    }
  }
}
