package my.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class DbApi {

	private Connection c = null;
	PreparedStatement ps = null;

	// private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();
	// private static final DateFormat df = new SimpleDateFormat("yyyy-mm-dd
	// hh:mm:ss.SSS");

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

			String sql = "insert into ticks values(?,?::timestamp,?,?::numeric::money,now());";
			ps = c.prepareStatement(sql);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");

	}

	public void insertBatch(ArrayList<Ticker> tickerList) {
		try {

			int cnt = 0;
			for (Ticker ticker : tickerList) {

				System.out.println(++cnt + "\t" + ticker.toString());

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
			// ps.close();
			// c.commit();
			// c.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

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
