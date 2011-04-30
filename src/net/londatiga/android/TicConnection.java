package net.londatiga.android;

import android.preference.PreferenceManager;
import android.util.Log;

import android.content.SharedPreferences;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.Socket;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

public class TicConnection {
	private Socket mSocket;
	private OutputStreamWriter mWriter;
	private InputStream mReader;
	private TicListener mListener;
	private ReceivingThread mRecvThread;
	private SharedPreferences mShared;
	
	private boolean connected = false;

	private static final String TAG = "Tictactoe";
	
	public TicConnection(Context context) {
		mShared = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private void connect() throws Exception {
		try {
			String server 	= mShared.getString("server", "192.168.1.1");
			String port		= mShared.getString("port", "1234");
			
			Log.d(TAG, "Opening connection to " + server + ":" + port);
			
			mSocket 		= new Socket(InetAddress.getByName(server), Integer.valueOf(port));
			
			mWriter 		= new OutputStreamWriter(mSocket.getOutputStream());
			mReader 		= mSocket.getInputStream();
	 	} catch (Exception e) {
	 		throw e;
	 	}
	}
	
	public void login(final String username, final boolean doConnect) {
		new Thread() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "Connection thread started");
					
					if (doConnect) connect();
					
					Log.d(TAG, "Registering user " + username);
					
					mWriter.write("{\"subscribe\":\"" + username + "\"}");
					mWriter.flush();

                    String response = "";

					while (!(response = streamToString2(mReader)).equals("")) {
                        Log.d(TAG, response);

						if (!response.equals("")) {
							JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();							
							String resp			= jsonObj.getString("response");
							
							if (resp.equals("rejected")) {
								mListener.onFail("rejected");								
								mSocket.close();
								
								String error = jsonObj.getString("error");
								
								Log.e(TAG, error + ", socket closed");
								
								mListener.onFail(error);
								
								break;
							} else if (resp.equals("accepted")) {
								connected = true;
								
								mListener.onSuccess();
								
								mRecvThread = new ReceivingThread();
								mRecvThread.start();
								
								Log.d(TAG, "Connected to server as user " + username);
								
								break;
							}
						}
					}
				} catch (Exception e) {
					connected = false;
					
					mListener.onFail("Can't connect to server");
					
					Log.e(TAG, "Can't connect to server");
					
					e.printStackTrace();
				}
				
				Log.d(TAG, "Connection thread ended");				
			}

		}.start();
	}

	public void disconnect() {
		if (connected) {
			if (mRecvThread != null) mRecvThread.shutdown();
			connected = false;
			
			try {
				if (mRecvThread.isRunning()) mRecvThread.shutdown();
				
				mWriter.close();
				mReader.close();
				mSocket.close();
			} catch (Exception e) {
				Log.e(TAG, "Closing socket failed");
				
				e.printStackTrace();
			}
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void setListener(TicListener listener) {
		mListener = listener;
	}
	
	public void send(final String packet) {
		
		new Thread() {
			@Override
			public void run() {
				Log.d(TAG, "Sending packet " + packet);
				
				try {
					mWriter.write(packet);
					mWriter.flush();
					
					mListener.onSendSuccess();
				} catch (Exception e) {
					mListener.onSendFail("Sent failed, write socket error");
					
					Log.e(TAG, "Write socket error");
					
					e.printStackTrace();
				}
			}
		}.start();
	}
	
    private String streamToString2(InputStream mReader) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(mReader));
        return br.readLine();
    }

	class ReceivingThread extends Thread {
		private volatile boolean running = false;
		
		public boolean isRunning() {
			return running;
		}
		
		public void shutdown() {
			running = false;
		}
	
		@Override
		public void run() {
			Log.d(TAG, "Receiving thread running");
			
			running = true;
			
			try {
				while (running) {
					String response 	= streamToString2(mReader);
					
					JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();							
					
					Log.d(TAG, response);
					
					if (jsonObj.has("board")) {
						JSONArray jsonBoard = (JSONArray) jsonObj.get("board");
						
						int length	= jsonBoard.length();
						int[] board = new int[length];
						
						for (int i = 0; i < length; i++) {
							board[i] = jsonBoard.getInt(i);
						}
						
						mListener.onBoard(board, jsonObj.getString("status"), jsonObj.getBoolean("moving"));
					} else if (jsonObj.has("playing")) {
						String username = jsonObj.getString("playing");
						
						mListener.onStart(username);
					}
				}
			} catch (Exception e) {
				mListener.onFail("Read error");
				
				Log.e(TAG, "Read error");
				
				e.printStackTrace();
			} 
			
			Log.d(TAG, "Receiving thread terminated");
		}
	}
	
	public interface TicListener {
		abstract public void onSuccess();
		abstract public void onFail(String error);
		abstract public void onSendSuccess();
		abstract public void onSendFail(String error);
		abstract public void onStart(String username);
		abstract public void onBoard(int[] board, String status, boolean move);
	}
}