package net.londatiga.android;

import android.content.Context;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.Socket;

public class TicConnection {
	private Context mContext;
	private Socket mSocket;
	private OutputStreamWriter mWritter;
	private InputStreamReader mReader;
	
	private boolean connected = false;
	
	private static final String TAG = "TicTacToe";
	private static final String HOST_ADDR = "192.168.1.1";
	private static final int HOST_PORT = 1234;
	
	public TicConnection(Context context) {
		mContext = context;
	}
	
	public void connect() {
		try {
			InetAddress serverAddr = InetAddress.getByName(HOST_ADDR);
			
			mSocket 	= new Socket(serverAddr, HOST_PORT);
			
			mWritter 	= new OutputStreamWriter(mSocket.getOutputStream());
			mReader 	= new InputStreamReader(mSocket.getInputStream());
			
			connected	= true;
		} catch (Exception e) {
			connected = false;
			Log.e(TAG, "Connection failed");
			e.printStackTrace();
		}
	}
	private final class ReceivingThread extends Thread {
		@Override
		public void run() {
			
		}
	}
	
	private  final class SendingThread extends Thread {
		@Override
		public void run() {
			
		}
	}
	
	public interface TicListener {
		abstract public void onSuccess();
		abstract public void onFail(String error);
	}
}