package my.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DbApi {

	private Connection c = null;
	PreparedStatement ps = null, ps2 = null;
	Statement stmt = null;

	// private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();
	private static final DateFormat df = new SimpleDateFormat("MM-dd hh:mm:ss.SSS");
	private static final DateFormat df2 = new SimpleDateFormat("yyyyMMdd  hh:mm:ss");

	// private final int BATCH_SIZE = 40;

	public DbApi() {
		connect();
	}

	/*
	 * public static void main(String[] args) { new DbApi().run(); }
	 * 
	 * void run() { connect();
	 * 
	 * for (int i = 0; i < 100; i++) { tickerList.add(new Ticker("AAPL",
	 * df.format(new Date()), i % 2 == 0, Math.round(Math.random() * 1000) /
	 * 100.0 + 99.0)); }
	 * 
	 * insertRecords(); }
	 */

	private void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/equ", "user", "222");

			ps = c.prepareStatement("insert into ticks values(?,?::timestamp,?,?::numeric::money,now());");

			// "insert into hist_quotes
			// values(?,?,?::numeric::money,?::numeric::money,?::numeric::money,?::numeric::money,?,?,?,?,now());"

			ps2 = c.prepareStatement("insert into hist_quotes values(?,?,?,?,?,?,?,?,?,?,now());");

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Connection to postgres opened");

	}

	public void insertBatch(ArrayList<Ticker> tickerList) {
		try {

			// int cnt = 0;
			for (Ticker ticker : tickerList) {

				// System.out.println(++cnt + "\t" + ticker.toString());

				ps.setString(1, ticker.getSymbol());
				ps.setString(2, ticker.getTimestamp());
				ps.setBoolean(3, ticker.isBlnBid());
				ps.setDouble(4, ticker.getPrice());

				ps.addBatch();

				// if (++cnt % BATCH_SIZE == 0) {
				// int[] res = ps.executeBatch();
				// System.out.println(res[0] + "\t" + res[1]);
				// }
			}

			ps.executeBatch();
			System.out.println(df.format(new Date()));
			// ps.close();
			// c.commit();
			// c.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertBatchHist(ArrayList<HistRecord> histRecs) {
		try {

			// int cnt = 0;
			for (HistRecord rec : histRecs) {

				// System.out.println(++cnt + "\t" + ticker.toString());

				ps2.setString(1, rec.getTicker());
				// String dt = rec.getDate();
				try {
					ps2.setTimestamp(2, new java.sql.Timestamp(df2.parse(rec.getDate()).getTime()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ps2.setDouble(3, rec.getOpen());
				ps2.setDouble(4, rec.getHigh());
				ps2.setDouble(5, rec.getLow());
				ps2.setDouble(6, rec.getClose());
				ps2.setInt(7, rec.getVolume());
				ps2.setInt(8, rec.getCount());
				ps2.setDouble(9, rec.getWap());
				ps2.setBoolean(10, rec.isHasGaps());

				ps2.addBatch();

				// if (++cnt % BATCH_SIZE == 0) {
				// int[] res = ps.executeBatch();
				// System.out.println(res[0] + "\t" + res[1]);
				// }
			}

			ps2.executeBatch();
			System.out.println(df.format(new Date()));
			// ps2.close();
			// c.commit();
			// c.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Ticker[] getRecords(String symbol) {
		ArrayList<Ticker> tck = new ArrayList<Ticker>();

		String qry = "select symbol,timestamp_,isbid,price from ticks where symbol='{SYMBOL}' and timestamp_ >= (current_date-1) order by timestamp_"
				.replace("{SYMBOL}", symbol);

		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(qry);
			while (rs.next()) {
				tck.add(new Ticker(rs.getString("symbol"), rs.getTimestamp("timestamp_"),
						rs.getString("isbid").equals("b"), rs.getDouble("price")));
			}
			rs.close();
			stmt.close();
			// c.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Ticker[] res = new Ticker[tck.size()];

		return (tck.toArray(res));
	}

	public double[][] getSpread(String[] symb) {
		ArrayList<double[]> ls = new ArrayList<double[]>();

		String qry = new String("select max(p1) p1, max(p2) p2 from (select timestamp_, "
				+ "(case when symbol='{S1}' then price::numeric else 0 end) p1, "
				+ "(case when symbol='{S2}' then price::numeric else 0 end) p2 from ticks "
				+ "where symbol in ('{S1}','{S2}') and timestamp_ >= (current_date-0)) a "
				+ "group by timestamp_ order by timestamp_").replace("{S1}", symb[0]).replace("{S2}", symb[1]);

		System.out.println(qry);

		// String qryTst = "select * from (select 1.1 p1, 1.2 p2 union all
		// select 2.1 p1, 2.2 p2 union all select 3.1 p1, 3.2 p2) a order by
		// p1";

		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(qry);

			while (rs.next()) {
				ls.add(new double[] { rs.getDouble("p1"), rs.getDouble("p2") });
			}
			rs.close();
			stmt.close();
			// c.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double[][] res = new double[2][];

		// System.out.println(res[1][0]);
		return (ls.toArray(res));
	}

	public void close() {
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
