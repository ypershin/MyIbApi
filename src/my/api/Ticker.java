package my.api;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Ticker {
	private String symbol;
	private String timestamp;
	private boolean blnBid;
	private double price;

	public Ticker(String symbol, boolean blnBid, double price) {
		super();
		this.symbol = symbol;
		this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		this.blnBid = blnBid;
		this.price = price;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public boolean isBlnBid() {
		return blnBid;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return (symbol + "," + timestamp + "," + blnBid + "," + price);
	}

}
