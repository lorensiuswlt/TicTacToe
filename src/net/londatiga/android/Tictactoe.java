package net.londatiga.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import net.londatiga.android.TicConnection.TicListener;

public class Tictactoe extends Activity implements TicListener {
    private TicAdapter mAdapter;
    private GridView mGridView;
    private ProgressDialog mProgress;
    private TicConnection mConnection;
    private TextView mStatusTxt;
    private Button mConnectBtn;
    private String mUsername;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mGridView			= (GridView) findViewById(R.id.gridView);
        mConnectBtn			= (Button) findViewById(R.id.connect);
        mStatusTxt			= (TextView) findViewById(R.id.status);
        
        mStatusTxt.setText("");
        
        mProgress			= new ProgressDialog(this);
        
        mProgress.setCancelable(false);
        mProgress.setMessage("Connecting ...");
        
        mConnection = new TicConnection(this);
        
        mConnection.setListener(this);
        
        LayoutInflater inflater = LayoutInflater.from(this);
		View view				= inflater.inflate(R.layout.dialog_username, null);
		
		final EditText userEt	= (EditText) view.findViewById(R.id.username);

		AlertDialog.Builder builder	= new AlertDialog.Builder(this);
		
		builder.setView(view);
		builder.setPositiveButton("Connect", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				String username = userEt.getText().toString();
				
				if (username.equals("")) {
					Toast.makeText(Tictactoe.this, "Please insert username", Toast.LENGTH_SHORT).show();
					return;
				}
				
				mProgress.show();				
				mConnection.login(username, true);
				
				dialog.cancel();
				
				mUsername = username;
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		final AlertDialog connAlert		= builder.create();
		
		AlertDialog.Builder dcbuilder 	= new AlertDialog.Builder(this);
		
		dcbuilder.setMessage("Disconnect ?");
		dcbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				mConnection.disconnect();
				dialog.cancel();
			}
		});
		dcbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		final AlertDialog dcAlert = dcbuilder.create();
		
        mConnectBtn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (!mConnection.isConnected()) 
        			connAlert.show();
        		else {
        			dcAlert.show();
        			
        			mConnectBtn.setText("Connect");
        		}
        	}
        });
        
        mGridView.setNumColumns(3);
        
        mAdapter = new TicAdapter(this);
        
        mAdapter.setData(initTic());
        mAdapter.setLayoutParam(95);
        
        mGridView.setAdapter(mAdapter);
        mGridView.setEnabled(false);
        
        mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String json = "{\"move\": \"" + String.valueOf(position + 1) + "\"}";
				
				mConnection.send(json);
			}
        });
    }
    
    public void onSuccess() {
    	Tictactoe.this.runOnUiThread(new Runnable() { 
    		@Override
    		public void run() {
    			mProgress.dismiss();
    	    
    			mConnectBtn.setText("Disconnect");
    	    	mStatusTxt.setText("Playing with: (waiting ...)");
    	    	
    	    	Toast.makeText(Tictactoe.this, "Connected to server", Toast.LENGTH_SHORT).show();
    		}
    	});
    }
    
    public void onFail(final String error) {
    	Tictactoe.this.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mProgress.dismiss();
    			
    			mStatusTxt.setText("");
    			Toast.makeText(Tictactoe.this, error, Toast.LENGTH_SHORT).show();
    		}
    	});    	
    }
    
    public void onSendSuccess() {
    	Tictactoe.this.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mGridView.setEnabled(false);
    		}
    	});
    }
    
    public void onSendFail(final String error) {
    	Tictactoe.this.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mGridView.setEnabled(true);
    			
    			Toast.makeText(Tictactoe.this, error, Toast.LENGTH_SHORT).show();
    		}
    	});
    }
    
    public void onStart(final String username) {
    	Tictactoe.this.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mStatusTxt.setText("Playing with: " + username);
    		}
    	});
    }
    
    public void onBoard(final int[] board, final String status, final boolean move) {
    	Tictactoe.this.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mAdapter.setData(board);
    			mGridView.setAdapter(mAdapter);
    			
    			mGridView.setEnabled(true);
    			
    			if (move) android.util.Log.d("TEST", "moveing ...");
    			
    			android.util.Log.d("TEST", status);
    			
    			if (!status.equals("null")) {
    				mGridView.setEnabled(false);
    				
    				String msg = (status.equals("win")) ? "Congrats you win!" : "Poor, you loose!";
    				
    				AlertDialog.Builder builder	= new AlertDialog.Builder(Tictactoe.this);
    				
    				builder.setMessage(msg);
    				builder.setPositiveButton("Play Again", new DialogInterface.OnClickListener(){
    					public void onClick(DialogInterface dialog, int which){    						
    						mProgress.show();
    						
    						mConnection.login(mUsername, false);
    						
    						dialog.cancel();
    					}
    				});
    				
    				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {			
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						dialog.cancel();
    					}
    				});
    				
    				builder.create().show();
    			}
    		}
    	});
    }
    
    private int[] initTic() {
    	int[] data = new int[9];
    	
    	for (int i = 0; i < 9; i++) {
    		data[i] = 0;
    	}
    	
    	return data;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, 1, 0, "Settings").setIcon(android.R.drawable.ic_menu_manage);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	startActivity(new Intent(this, Preference.class));
    	
    	return true;
    }
}