package my.api;

import com.ib.controller.ApiController.IConnectionHandler;
import java.util.ArrayList;

public abstract class ConnectionHandlerAdapter implements IConnectionHandler {

	@Override
	public void disconnected() {
		System.out.println("disconnected");
	}

	@Override
	public void accountList(ArrayList<String> list) {
		System.out.println("received account list");
	}

	@Override
	public void error(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void message(int id, int errorCode, String errorMsg) {
		System.out.println(id + " " + errorCode + " " + errorMsg);
	}

	@Override
	public void show(String string) {
		System.out.println(string);
	}

}
