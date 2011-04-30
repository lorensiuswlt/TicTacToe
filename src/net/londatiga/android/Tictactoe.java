package net.londatiga.android;

import android.app.Activity;
import android.os.Bundle;

import android.widget.GridView;

public class Tictactoe extends Activity {
    private TicAdapter mAdapter;
    private GridView mGridView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mGridView	= (GridView) findViewById(R.id.gridView);

    }
    
    public class TicData {
    	int id;
    	String status;
    }
}