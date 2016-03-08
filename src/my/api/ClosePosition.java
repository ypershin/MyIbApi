package my.api;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.NewTickType;
import com.ib.controller.OrderStatus;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.MktDataType;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.TimeInForce;

public class ClosePosition extends ConnectionHandlerAdapter implements ILogger {
	private final ApiController m_controller = new ApiController(this, this, this);
	private String m_symbol;
	private NewContract m_contract;
	private int m_position;
	private double m_bid;
	private double m_ask;
	private boolean m_placedOrder;

	public static void main(String[] args) {
		new ClosePosition().run("MSFT");
	}

	void run(String symbol) {
		m_symbol = symbol;
		m_controller.connect("127.0.0.1", 7496, 0);
	}

	@Override
	public void connected() {
		// TODO Auto-generated method stub

		print("requesting positions");

		m_controller.reqPositions(new IPositionHandler() {

			@Override
			public void positionEnd() {
				// TODO Auto-generated method stub
				onHavePosition();
			}

			@Override
			public void position(String account, NewContract contract, int position, double avgCost) {
				// TODO Auto-generated method stub
				if (contract.symbol().equals(m_symbol) && contract.secType() == SecType.STK) {
					m_contract = contract;
					m_position = position;
				}
			}
		});

	}

	protected void onHavePosition() {
		print("Current postion is " + m_position);

		if (m_position != 0) {

			print("requesting market data");

			m_controller.reqTopMktData(m_contract, "", false, new ITopMktDataHandler() {
				@Override
				public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {
					if (tickType == NewTickType.BID) {
						m_bid = price;
						print("recieved bid " + price);
					} else if (tickType == NewTickType.ASK) {
						m_ask = price;
						print("received ask " + price);
					}

//					checkPrices(this);
				}

				@Override
				public void tickSize(NewTickType tickType, int size) {
					// TODO Auto-generated method stub

				}

				@Override
				public void tickString(NewTickType tickType, String value) {
					// TODO Auto-generated method stub

				}

				@Override
				public void tickSnapshotEnd() {
					// TODO Auto-generated method stub

				}

				@Override
				public void marketDataType(MktDataType marketDataType) {
					// TODO Auto-generated method stub

				}
			});
		} else {
			print("There is no position to close");
			m_controller.disconnect();
			System.exit(0);
		}

		/*
		 * System.out.print("continue? y/n: "); try { if ( System.in.read() ==
		 * 'n') { m_controller.disconnect(); System.exit(0); } else {
		 * connected(); } } catch (IOException e) { e.printStackTrace(); }
		 */
	}

	void checkPrices(ITopMktDataHandler handler) {
		if (m_bid != 0 && m_ask != 0 && !m_placedOrder) {
			m_placedOrder = true;
			print("desubscribing market data");
			m_controller.cancelTopMktData(handler);

			// placeOrder();
		}
	}

	void placeOrder() {
		double midPrice = Math.round((m_bid + m_ask) / 2 * 100) / 100.0;
		// midPrice += 1.0;

		m_contract.exchange("SMART");
		m_contract.primaryExch("ISLAND");

		NewOrder order = new NewOrder();
		order.action(m_position > 0 ? Action.SELL : Action.BUY);
		order.totalQuantity(Math.abs(m_position));

		// order.orderType(OrderType.MKT);

		order.lmtPrice(midPrice);
		order.tif(TimeInForce.DAY);

		print("placing order " + order);

		m_controller.placeOrModifyOrder(m_contract, order, new IOrderHandler() {

			@Override
			public void orderState(NewOrderState orderState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void orderStatus(OrderStatus status, int filled, int remaining, double avgFillPrice, long permId,
					int parentId, double lastFillPrice, int clientId, String whyHeld) {
				// TODO Auto-generated method stub

			}

			@Override
			public void handle(int errorCode, String errorMsg) {
				print("Order code and message: " + errorCode + " " + errorMsg);
			}

		});

	}

	@Override
	public void log(String valueOf) {
		// TODO Auto-generated method stub

	}

	void print(String str) {
		System.out.println(str);
	}

}
