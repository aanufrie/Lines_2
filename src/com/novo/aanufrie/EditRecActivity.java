package com.novo.aanufrie;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditRecActivity extends Activity {
    String Score;
    private TextView ScoreView;
    private Button SaveBtn;
    private Button CancelBtn;
    private EditText UserName;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    Score = extras.getString("Score");
		}
		Log.d("lines", "EditRec "+Score );
		setContentView(R.layout.activity_edit_rec);
    	
		SaveBtn = (Button) findViewById(R.id.buttonOK);
    	CancelBtn   = (Button) findViewById(R.id.buttonCancel);
    	ScoreView = (TextView) findViewById(R.id.Score);
    	UserName = (EditText) findViewById(R.id.editName);
    	
    	ScoreView.setText("Your score is " + Score);
        SaveBtn.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View arg0) {
               Intent output = new Intent();
               String editUserName = UserName.getText().toString();
               output.putExtra("USERNAME", editUserName);
               //output.putExtra("SCORE", Score);
               setResult(RESULT_OK, output);
               Log.d("lines", "EditRec finish OK");
               finish();
            }
        });
        
       CancelBtn.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View arg0) {
               Intent output = new Intent();
               setResult(RESULT_CANCELED, output);
               Log.d("lines", "EditRec finish CANCEL");
               finish();
            }
        });
    	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_rec, menu);
		return true;
	}

}
