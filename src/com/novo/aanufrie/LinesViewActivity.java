package com.novo.aanufrie;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

import com.novo.aanufrie.DbHelper;
import com.novo.aanufrie.R;


public class LinesViewActivity extends Activity implements OnSharedPreferenceChangeListener, Runnable {
  public LinesApplication myApp; 
  public int newBalls;
  String newBallsString;
  private Button newGameBtn;
  private Button recBtn;
  //private Button loadBtn;
  private Button redoBtn;
  private Button helpBtn;
  private ToggleButton robotBtn;
  private SurfaceView surface;
  private SurfaceHolder holder;
  //private TextView ScoreView;
  private boolean locker = true;
  private Thread thread;
  private int radiusBlack, radiusWhite;
  private boolean left = true;
  private MediaPlayer mP;  //MediaPlayer.create(getBaseContext(), R.raw.champ);
  
  private boolean showPath = true;
  //private coordinate Path[] = new coordinate[81];
  
  //private boolean Movement_in_Progress = false;
  
  private int skip = 0;
  static final int SAVE_OR_NOT = 0;
  
  Random rand = new Random();
  
  private static final int baseRadius = 20;
  private static final int maxRadius = 30;
  private static final int baseSpeed = 1;
  private int speed = 0;
  private int ddd = 5;
  private int jmp = 0;
  private int maxjmp = 4;
  //private coordinate active,to;
  //private coordinate before, redo;
  //EColor active_color = EColor.NONE;
  float xwidth=0;
  float xtop=0;
  final Handler myHandler = new Handler();
  String filename = "lines.save";
  static String UserName;
  static String MyScore;
  
  // next balls
  //private EColor newballs[] = new EColor[10];
  //private EColor lastballs[] = new EColor[10];
  //private coordinate lastballs_pos[] = new coordinate[10];
  //private int balls_count = 0;
  //private int Freeroom = 0;
  //private int lastFreeroom = 0;
  //private boolean redo_is_active = false;
  //private boolean robot_is_active = false;
  //private int Score = 0;
    
  //public EColor Myplane[][] = new EColor[9][9];
  //public EColor MyplaneCopy[][] = new EColor[9][9];
  public int Colors[][] = new int[8][2];
  
  //private coordinate all_balls[] = new coordinate[36];
  
  
  DbHelper dbHelper;
  //SQLiteDatabase db;
  //Cursor cursor;
  
  /*public class coordinate{
	  public int i=0;
      public int j=0;
      public coordinate(int mi, int mj) {i = mi; j = mj;}
  }*/

  public class position_rep{
	  public int n9;
	  public int n8;
	  public int n7;
	  public int n6;
	  public int n5;
	  public int n4;         // four balls with empty slot 
	  public int n4_a;       // four balls with ball inside
	  public int n3;       // three balls with 2 free positions inside
	  public int n3_a;       // three balls with 1 free position inside
	  public int n3_b;       // three balls without free positions inside
	  public int n2;         // two balls with free positions inside 
	  int estimation;
	  public position_rep() { n9=0; n8=0; n7=0; n6=0; n5=0;
	      n4=0; n4_a=0; n3=0; n3_a=0; n3_b=0; n2=0;
	  }
  }
  
  
  /*public enum EColor {
	  NONE(0),RED(1),GREEN(2),BLUE(3),YELLOW(4),BLACK(5),VIOLET(6),ORANGE(7);
	  EColor(int value) {this.value=value;}
	  private final int value;
	  public int int_EColor() {
   		 return value;  
	  }
  }*/
	
     /**
	 * display the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}
	@Override

	public boolean onOptionsItemSelected(MenuItem item) {
		//distribute the calls from the menus
		switch (item.getItemId())
		{
			case R.id.action_settings:
				Log.d("lines","Before SettingActivity" );
				saveGame();
				startActivity(new Intent(this, SettingActivity.class));
				newBallsString=myApp.prefs.getString("newballs","4");
				Log.d("lines","After SettingActivity");
				myApp.balls_count=Integer.parseInt(newBallsString);
				if (myApp.balls_count == 3 )
					myApp.Replacement_support = false;
				loadGame();
				break;
	    }
		
		return true;
	}
   
	public void onDestroy(Bundle savedInstanceState)	{
	  	Log.d("lines", "onDestroy" );
    	super.onCreate(savedInstanceState);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("lines", "onCreate" );
    	super.onCreate(savedInstanceState);
    	myApp = (LinesApplication)getApplication();
    	myApp.myHandler = new Handler();
    	newBallsString = myApp.prefs.getString("newballs", "4");
    	Log.d("lines", "New Balls " + newBallsString );
    	filename="lines"+newBallsString+".save";
    	myApp.balls_count = Integer.parseInt(newBallsString);
    	if (myApp.balls_count == 3 )
    		myApp.Replacement_support = false;
    	setContentView(R.layout.activity_surface_view);
    	
    	newGameBtn = (Button) findViewById(R.id.buttonswap);
    	recBtn    = (Button) findViewById(R.id.buttonrec);
    	//loadBtn    = (Button) findViewById(R.id.buttonread);
    	redoBtn      = (Button) findViewById(R.id.buttonredo);
    	helpBtn      = (Button)findViewById(R.id.buttonhelp);
    	robotBtn     = (ToggleButton)findViewById(R.id.buttonrobot);
    	myApp.ScoreView = (TextView) findViewById(R.id.myScore);
    	surface = (SurfaceView) findViewById(R.id.mysurface);
    	
        //active = new coordinate(0,0);
        //to = new coordinate(0,0);
        //before = new coordinate(0,0);
        //redo   = new coordinate(0,0);
        
        holder = surface.getHolder();
        mP=MediaPlayer.create(getBaseContext(), R.raw.jball);

        //And in the timer, we pass the runnable to the handler:

         //thread = new Thread(this);
        //thread.start();
        newGameBtn.setOnClickListener(new OnClickListener() {
                	
            @Override
            public void onClick(View arg0) {
               startNewGame();
            }
        });
        
        recBtn.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View arg0) {
               Context context = arg0.getContext();
			   Intent intent = new Intent(context ,RecActivity.class);
               context.startActivity(intent);
            }
        });    
        /*
        loadBtn.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View arg0) {
              loadGame();
            }
        });*/    
        
            
        redoBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		Redo();
        		//UpdateGUI();
        	}
        });

        helpBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		myApp.Find_Best_Movement(); 
   	 	        Log.d("lines", "Find_Best_Movement "+Integer.toString(myApp.best_from.i)+" "+
  	               Integer.toString(myApp.best_from.j)+" "+Integer.toString(myApp.best_to.i)+" "+Integer.toString(myApp.best_to.j) ); 
  	 	      // These values are used by run(). Should be defined. 
  	 	      myApp.active = myApp.best_from;
  	 	      myApp.active_color = myApp.Myplane[myApp.best_from.i][myApp.best_from.j];
  	 	      myApp.to = myApp.best_to;
  	 	      myApp.saveStatus();
  	 	      myApp.moveBall(myApp.best_from,myApp.best_to);
	       
        	}
        });
        
        robotBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
              		myApp.robot_is_active = true;
            		//Robot();
              		startService(new Intent(getBaseContext(),RobotService.class));
                	// The toggle is enabled
                } else {
                	myApp.robot_is_active = false;
                    // The toggle is disabled
                	stopService(new Intent(getBaseContext(),RobotService.class));
                }
            }
        });
        
            
        
        surface.setOnTouchListener(new OnTouchListener() {
        	
      @Override
      public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: 
        {
        	float x = event.getX();
            float y = event.getY();
            int i = (int)((x-20)/xwidth);
            int j = (int)((y-20)/xtop);
            
            if(i >= 9 || j >= 9 || i < 0 || j < 0)
            	return false;
            if(myApp.Movement_in_Progress || 
               myApp.Replacement_in_Progress )
            	return false;
            
            if(myApp.active.i >= 0 && myApp.active.j >= 0) {
            	if(myApp.Myplane[i][j] == LinesApplication.EColor.NONE) {
            		myApp.to.i = i;
            		myApp.to.j = j;
            		Log.d("lines", "to: " + Integer.toString(myApp.active.i)+ " " + Integer.toString(myApp.active.j));
            		myApp.saveStatus();
            		myApp.moveBall(myApp.active,myApp.to);
            	}
            	else {
            	    myApp.active.i = i;
            	    myApp.active.j = j;
            	    myApp.active_color = myApp.Myplane[i][j];
            	    Log.d("lines", "to: " + Integer.toString(myApp.active.i)+ " " + Integer.toString(myApp.active.j)); 
            	}
            }
            else {
            	if(myApp.Myplane[i][j] != LinesApplication.EColor.NONE) {
            		myApp.active.i = i;
            		myApp.active.j = j;
            		myApp.active_color = myApp.Myplane[i][j];
            		Log.d("lines", "to: " + Integer.toString(myApp.active.i)+ " " + Integer.toString(myApp.active.j));        
            	}
            }
               	
        	
            return true;
        }
        case MotionEvent.ACTION_MOVE:  
        	float x = event.getX();
            float y = event.getY();
            int i = (int)((x-20)/xwidth);
            int j = (int)((y-20)/xtop);
            if(!myApp.Replacement_support)
                return false;
            if(i >= 9 || j >= 9 || i < 0 || j < 0)
            	return false;
            if(myApp.Movement_in_Progress || 
               myApp.Replacement_in_Progress )
            	return false;
            
            if((myApp.active.i != i || myApp.active.j != j) &&
            	myApp.Myplane[i][j] != LinesApplication.EColor.NONE	) {
            	if ( ((i - myApp.active.i == 1 || myApp.active.i - i == 1) && myApp.active.j - j == 0) ||
            	     ((j - myApp.active.j == 1 || myApp.active.j - j == 1) && myApp.active.i - i == 0)) { 
            		myApp.to.i = i;
            		myApp.to.j = j;
            		Log.d("lines", "REPLACE: " + Integer.toString(myApp.active.i)+ " " + Integer.toString(myApp.active.j));
            		//myApp.saveStatus();
            		myApp.replaceBalls();
            	}
            }
            return true;
        }  
        return true; 
      }
	
    });      
      
      Colors[1][0]= getResources().getColor(R.color.color_red); 
      Colors[1][1]= getResources().getColor(R.color.color_red_weak);
      Colors[2][0]= getResources().getColor(R.color.color_green); 
      Colors[2][1]= getResources().getColor(R.color.color_green_weak);     
      Colors[3][0]= getResources().getColor(R.color.color_blue); 
      Colors[3][1]= getResources().getColor(R.color.color_blue_weak);       
      Colors[4][0]= getResources().getColor(R.color.color_yellow); 
      Colors[4][1]= getResources().getColor(R.color.color_yellow_weak);  
      Colors[5][0]= getResources().getColor(R.color.color_black); 
      Colors[5][1]= getResources().getColor(R.color.color_black_weak);
      Colors[6][0]= getResources().getColor(R.color.color_violet); 
      Colors[6][1]= getResources().getColor(R.color.color_violet_weak);
      Colors[7][0]= getResources().getColor(R.color.color_orange); 
      Colors[7][1]= getResources().getColor(R.color.color_orange_weak);
      
      for(int i=0; i<9; i++){
    	  for(int j=0;j<9;j++){
    		  myApp.Myplane[i][j] = (LinesApplication.EColor.values())[(i+j)%8];
    	  }
      }
 
       
      if(!loadGame()) startNewGame();
      //UpdateGUI();
      
     }
  
     private void UpdateGUI() {
        //tv.setText(String.valueOf(i)); //This causes a runtime error.
        myApp.myHandler.post(myApp.myRunnable);
     }
    
     final Runnable myRunnable = new Runnable() {
         public void run() {
        	 //myApp.ScoreView.setText(Integer.toString(myApp.Score)+ "  ");
        	 robotBtn.setChecked(false);
         }
      };
     
   
    
    /*private void moveBall(LinesApplication.coordinate from, LinesApplication.coordinate to) {
      LinesApplication.EColor mcol = myApp.Myplane[from.i][from.j];
      Log.d("lines", "moveBall Freeroom:" + Integer.toString(myApp.Freeroom)+ " " + Integer.toString(mcol.int_EColor()));
      myApp.clear_visited();
      myApp.pathlength = 1;
      myApp.currstep = 0;
       if(myApp.find_path(from,to)) {
    	 myApp.Movement_in_Progress = true;
    	 Log.d("lines", "moveBall pathlength:" + Integer.toString(myApp.pathlength));
    	 myApp.currstep=0;
       	 for(int i=0; i<myApp.pathlength;i++) {
    		 Log.d("lines", "Path:" + Integer.toString(i) + " " + Integer.toString(myApp.Path[i].i) + " " + Integer.toString(myApp.Path[i].j)); 
    	 }
      }
      myApp.myHandler.post(myApp.myRunnable);
    }*/
    
  
    
    public void Redo() {
       if(myApp.redo_is_active) {
    	   for(int i=0; i < myApp.balls_count; i++) {
    		   myApp.newballs[i] = myApp.lastballs[i];
    		   myApp.Myplane[myApp.lastballs_pos[i].i][myApp.lastballs_pos[i].j] = (LinesApplication.EColor.values())[0];
    		   myApp.Freeroom++;
    	   }
    	   myApp.Myplane[myApp.before.i][myApp.before.j]= myApp.Myplane[myApp.redo.i][myApp.redo.j];
    	   myApp.Myplane[myApp.redo.i][myApp.redo.j] = (LinesApplication.EColor.values())[0];
       }
       myApp.redo_is_active = false;
       UpdateGUI();
    }
    
 
    
    /*public int Estimate_Line(coordinate from, coordinate direction){
    	int cost = 0;
    	int i=0;
    	int j=0;
    	int nexti=0;
    	int nextj=0;
    	int kol_of_color=0;
    	int kol_of_none=0;
    	int endi=0;
    	int endj=0;
    	//EColor color = EColor.RED;
    	    	   	
    	for(LinesApplication.EColor color:LinesApplication.EColor.values()) {
    	
    		if (color == LinesApplication.EColor.NONE )
    			continue;
    		//Log.d("lines", "Estimate_Line: " + Integer.toString(color.int_EColor()));
    		//do {
    	    i = from.i;
    	    j = from.j;
    	    
    	    do {
    	       kol_of_color=0;
    	       kol_of_none=0;
    	       nexti = i;
    	       nextj = j;
    	       endi = i+5*direction.i;
        	   endj = j+5*direction.j;
    	       if(endi >=9 || endj >=9 || endi < 0 || endj < 0 ) {
    	    	   break;
    	       } 
    	       else {
    	           while((nexti == i || nexti != endi) &&
    	                 (nextj == j || nextj != endj)) {
    	                if(myApp.MyplaneCopy[nexti][nextj] == color)
    	                  kol_of_color++;
    	                if(myApp.MyplaneCopy[nexti][nextj] == LinesApplication.EColor.NONE) 
    	                  kol_of_none++;
    	                nexti=nexti+direction.i;
    	                nextj=nextj+direction.j;
    	           }
    	       }
    	       if (kol_of_color == 5 ) {
    	    	   cost = cost+1000;
    	       } else if (kol_of_color == 4 && kol_of_none == 1) {
    	    	   cost = cost+300;
    	       } else if (kol_of_color == 4) {
    	    	   cost = cost+100;
    	       } else if (kol_of_color == 3 && kol_of_none == 2) {
    	           cost = cost+30; 
    	       } else if (kol_of_color == 3 && kol_of_none == 1) {
    	    	   cost = cost+20;
    	       } else if (kol_of_color == 3) {
    	    	   cost = cost+10;
    	       } else if (kol_of_color == 2 && kol_of_none == 3) {
    	    	   cost = cost+8;
    	       } else if (kol_of_color == 2 && kol_of_none == 2) {
    	    	   cost = cost+3;
    	       }
    	       i+= direction.i;
    	       j+= direction.j;
    	    } while(true);
    	    //color++; 
    	} 
    	
    	return cost;
    }*/
    
    /*public int Estimate_Position(coordinate from, coordinate to) {
       int cost =0;
 
       LinesApplication.EColor color = myApp.MyplaneCopy[from.i][from.j];
       myApp.MyplaneCopy[from.i][from.j] = LinesApplication.EColor.NONE;
       myApp.MyplaneCopy[to.i][to.j]=color;
       cost += Estimate_Line(new coordinate(4,0),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(3,0),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(2,0),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(1,0),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(0,0),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(0,1),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(0,2),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(0,3),new coordinate(1,1));
       cost += Estimate_Line(new coordinate(0,4),new coordinate(1,1));
       
       cost += Estimate_Line(new coordinate(0,4),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(0,5),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(0,6),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(0,7),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(0,8),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(1,8),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(2,8),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(3,8),new coordinate(1,-1));
       cost += Estimate_Line(new coordinate(4,8),new coordinate(1,-1)); 
       
       for(int j=0; j < 9; j++) {
    	   cost += Estimate_Line(new coordinate(0,j),new coordinate(1,0));   
       }
       for(int i=0; i < 9; i++) {
    	   cost += Estimate_Line(new coordinate(i,0),new coordinate(0,1));
       }
       myApp.MyplaneCopy[from.i][from.j] = color;
       myApp.MyplaneCopy[to.i][to.j]=LinesApplication.EColor.NONE;
       
       return cost;
    }*/
    
    public void Robot() {
    	while (myApp.robot_is_active && myApp.Freeroom != 0) {
    		myApp.Find_Best_Movement();
    		UpdateGUI();
    	}
    	
    }
    
  
 
    
    public void startNewGame() {
    	Log.d("lines", "startNewGame ");
    	for(int i=0; i<9; i++){
	    	  for(int j=0;j<9;j++){
	    		  myApp.Myplane[i][j] = (LinesApplication.EColor.values())[0];
	    	  }
	   }
       if (myApp.robot_is_active) {
          	myApp.robot_is_active = false;
            stopService(new Intent(getBaseContext(),RobotService.class));
            myHandler.post(myRunnable);
            // disable element
       }   
  	   myApp.Freeroom = 9*9;
  	   myApp.Score = 0;
	   myApp.active.i = -1;
	   myApp.active.j = -1;
	   myApp.Movement_in_Progress = false;
	   newballs(7);
	   balls_position(7);
       newballs(myApp.balls_count);
       UpdateGUI();
    }

    public void saveGame() {
    	FileOutputStream outputStream;
    	Log.d("lines", "saveGame"+newBallsString);
    	//myApp.balls_count = Integer.parseInt(newBallsString);
    	filename="lines"+newBallsString+".save";
    	try {
    	  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
    	  for (int i=0; i<9; i++) {
    	     for (int j=0; j<9 ; j++) {
    	    	 outputStream.write(((Integer.toString(myApp.Myplane[i][j].int_EColor())+";").getBytes()));
    	     }
    	  }
    	  for (int i=0; i<myApp.balls_count; i++) {
    	     outputStream.write(((Integer.toString(myApp.newballs[i].int_EColor())+";").getBytes())); 
    	  }
    	  outputStream.write((Integer.toString(myApp.Score)).getBytes());
    	  //outputStream.write((Integer.toString(Score)+";").getBytes());
    	  //outputStream.write(Integer.toString(Freeroom).getBytes());
    	  
    	  outputStream.close();
    	} catch (Exception e) {
    	  e.printStackTrace();
    	}
    }
    
    public boolean loadGame() {
        filename="lines"+newBallsString+".save";
        Log.d("lines","loadGame "+filename);
        File file = new File(getFilesDir(),filename);
        if (!file.exists()) {
        	Log.d("lines","loadGame failed"+filename);
        	return false;
        }
        FileInputStream fin = null;
        try {
           fin = new FileInputStream(file);
           byte fileContent[] = new byte[(int)file.length()];
           fin.read(fileContent);
           String s = new String(fileContent);
           if (s.isEmpty()) return false;
           String[] mcolor = null;
           int num = 0;
           mcolor = s.split(";");
           Log.d("lines","File content: " + s);
           for (int i=0; i<9; i++) {
        	   for (int j=0; j<9; j++) {
        		   myApp.Myplane[i][j]= (LinesApplication.EColor.values())[Integer.parseInt(mcolor[num++])];
        	       //num++;	   
        	   }
           }
           Log.d("lines","Newballs: "+newBallsString);
           //myApp.balls_count = Integer.parseInt(newBallsString);
           for (int i=0; i<myApp.balls_count; i++ ) {
              myApp.newballs[i] = (LinesApplication.EColor.values())[Integer.parseInt(mcolor[num++])];
              Log.d("lines","Newballs["+Integer.toString(i)+"]="+mcolor[num]);
              //num++;           		 
           }
           Log.d("lines","myApp.Score+myApp.Freeroom ");
           myApp.Score = Integer.parseInt(mcolor[num++]);
           calc_freeroom();
           //Freeroom = Integer.parseInt(mcolor[num++]);
           //balls_count = 3;
        }
        catch (FileNotFoundException e) {
           Log.d("lines","File not found" + e);
        }
        catch (IOException ioe) {
           Log.d("lines","Exception while reading file " + ioe);
        }
    
        finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                Log.d("lines","Error while closing stream: " + ioe);
            }
        }
      Log.d("lines","LoadGame UpdateGUI");
      UpdateGUI();
      return true;
    }
    
    public void EndGame() {
    	int minScore = 100000;
    	int nRec = 0;
    	Date date = new Date(System.currentTimeMillis());
    	final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
      	Log.d("lines","EndGame");
      	Cursor cursor; 
      	
      	if (myApp.robot_is_active) {
          	myApp.robot_is_active = false;
            stopService(new Intent(getBaseContext(),RobotService.class));
            myHandler.post(myRunnable);
            // disable element
      	}
    	
    	dbHelper = new DbHelper(this); 
    	Log.d("lines","EndGame dbHelper created");
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	//SQLiteDatabase db = dbHelper.getReadableDatabase();
    	
    	cursor = db.query(DbHelper.TABLE, null, DbHelper.C_BALLS_NUMBER+"="+newBallsString, null, null, null,
		DbHelper.C_CREATED_AT + " DESC"); //
		//startManagingCursor(cursor); //
		// Iterate over all the data and print it out
		String user, score, output;
		if (cursor.moveToFirst()) {
			  do {
			      int rec_score;
			      nRec++;
			      user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); //
			      score = cursor.getString(cursor.getColumnIndex(DbHelper.C_SCORE));
			      rec_score = Integer.parseInt(score);
			      Log.d("lines","insertRecord move:  " + Integer.toString(rec_score));
			      if(minScore > rec_score) {
				      minScore = rec_score;
				  }
		 	  } while (cursor.moveToNext());
		}
		/*while (cursor.moveToNext()) { //
		  int rec_score;
		  nRec++;
		  user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); //
		  score = cursor.getString(cursor.getColumnIndex(DbHelper.C_SCORE));
		  rec_score = Integer.parseInt(score);
		  Log.d("lines","EndGame move:  " + Integer.toString(rec_score));
		  if(minScore > rec_score) {
			  minScore = rec_score;
		  }
		}*/
		Log.d("lines","EndGame:  " + Integer.toString(minScore));
		//db.close();
		
		/*if(nRec == 10 && Score > minScore) {
			  // Record with minimal score should be deleted
			  db.delete(DbHelper.TABLE, DbHelper.C_SCORE + "=" + minScore, null);	
		}*/
		
		if(nRec < 10 || myApp.Score > minScore) {
		   String myret; 
		   Log.d("lines","EndGame " + UserName +" "+Integer.toString(myApp.Score));
           Context context = this; 	
   		   //Intent intent = new Intent(context,EditRecActivity.class);
   		   Intent intent = new Intent(getBaseContext(),EditRecActivity.class);
           intent.putExtra("Score",Integer.toString(myApp.Score));
           startActivityForResult(intent,2);
		   //insertRecord(db,"Andrei",Integer.toString(Score),newBallsString);
           Log.d("lines","EndGame: OK for Safe" + UserName);
		}
		else {
			
		}
					
    	//Context context = this;
		//Intent intent = new Intent(context ,EditRecActivity.class);
        //context.startActivity(intent);   
		cursor.close();
		db.close();
    }
    
    protected void insertRecord(SQLiteDatabase db,String UserName, String MyScore, String Complexity) {
    	int nRec = 0;
    	int failed = 0;
    	int minScore = 100000;
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int ID = 0;
        int delID = 0;
        int score = Integer.parseInt(MyScore);
        Cursor cursor;
    	
      	Log.d("lines","insertRecord "+MyScore);
    	
    	dbHelper = new DbHelper(this); 
    	Log.d("lines","dbHelper");
    	    	
    	cursor = db.query(DbHelper.TABLE, null, DbHelper.C_BALLS_NUMBER+"="+Complexity, null, null, null,
		DbHelper.C_CREATED_AT + " DESC"); //
		//startManagingCursor(cursor); //
		// Iterate over all the data and print it out
		String user, sscore, output;
		
		if (cursor.moveToFirst()) {
		  do {
		      int rec_score;
		      nRec++;
		      ID   = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DbHelper.C_ID)));
		      user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); //
		      sscore = cursor.getString(cursor.getColumnIndex(DbHelper.C_SCORE));
		      rec_score = Integer.parseInt(sscore);
		      Log.d("lines","insertRecord move:  " + Integer.toString(rec_score));
		      if(minScore > rec_score) {
			      minScore = rec_score;
			      delID = ID;
		      }
	 	  } while (cursor.moveToNext());
		}
		Log.d("lines","insertRecord:  " + Integer.toString(minScore)+" "+Integer.toString(nRec));
		        		
			
		//myScore = Integer.parseInt(Score);
		if(nRec >= 10 && score > minScore) {
			  // Record with minimal score should be deleted
			Log.d("lines","insertRecord:  delete ID= " + Integer.toString(delID));
			ID = delID;
			db.delete(DbHelper.TABLE, DbHelper.C_ID + "=" + Integer.toString(delID), null);
		}
		else {
			if (nRec < 10) {
				ID = (Integer.parseInt(Complexity)-3)*10 + nRec;
			}
			else {
				ID = -1;
			}
		}
		
		if(ID >= 0) {
		   String myret;
		   Log.d("lines","onActivityRes:  insert " + UserName+" " + MyScore+ " "+ Complexity + " " + Integer.toString(ID));
		   ContentValues contentValues = new ContentValues();
		   contentValues.clear();
		   contentValues.put(dbHelper.C_ID, Integer.toString(ID));
		   contentValues.put(dbHelper.C_CREATED_AT,Integer.toString(mDay)+"-"+Integer.toString(mMonth)+"-"+Integer.toString(mYear));
		   contentValues.put(dbHelper.C_BALLS_NUMBER, Complexity);
		   //contentValues.put(dbHelper.C_BALLS_NUMBER, newBallsString);
		   contentValues.put(dbHelper.C_SCORE,MyScore);
		   contentValues.put(dbHelper.C_USER, UserName);
		
		   try {
		      db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		      SQLiteDatabase.CONFLICT_IGNORE); //
		   } 
		   catch(Exception e) {
			  e.printStackTrace();
			  failed = 1;
			  Log.d("lines","Insertion failed");
		   }
		   
		   //finally {
           //		Log.d("lines","Insertion failed");
		   //}
		  
	    }  
		Log.d("lines","RETURN SAVE");
		db.close();	
		cursor.close();
    }
    
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
            	String MyScore;
            	String UserName;
        		SQLiteDatabase db = dbHelper.getWritableDatabase();
                		
            	UserName = data.getStringExtra("USERNAME");
            	//MyScore = data.getStringExtra("SCORE");
            	Log.d("lines","info"+UserName+Integer.toString(myApp.Score));
                insertRecord(db,UserName,Integer.toString(myApp.Score),newBallsString);          	
  		        db.close();
            }
            else {
                Log.d("lines","RETURN CANCEL"); 
            }
        }
    }
    
    private void newballs(int num) {
       Log.d("lines", "newballs "+ Integer.toString(myApp.balls_count));
       //balls_count = Integer.parseInt(newBallsString);
       //if(Freeroom < balls_count) {
       //   balls_count = Freeroom;
       //}	
       for(int i=0; i< num; i++ ){
    	  int randnum = rand.nextInt();
    	  if(randnum<0) randnum = -randnum;
    	  Log.d("lines", "randnum "+ Integer.toString(randnum));
    	  myApp.newballs[i] = (LinesApplication.EColor.values())[(randnum%7)+1];
         }  
    }
    
    private void calc_freeroom() {
    	myApp.Freeroom =0;
    	for (int i=0;i<9;i++) {
    		for (int j=0;j<9;j++) {
    			if(myApp.Myplane[i][j] == LinesApplication.EColor.NONE) {
    				myApp.Freeroom++;
    			}
    		}
    	}
    }
    
    private void balls_position(int num) {
       int n;
       Log.d("lines", "newballs "+ Integer.toString(myApp.balls_count));
       calc_freeroom();
       //if(Freeroom < balls_count) 
       //	   balls_count = Freeroom;
       for(int k=0; k < num; k++) {
    	  n = rand.nextInt()%myApp.Freeroom;
    	  if(n<0) n=-n;
    	  int count = 0;
    	  boolean found = false;
    	  for(int i=0; i < 9; i++ ) {
    		  for(int j=0; j < 9; j++) {
    			  if(myApp.Myplane[i][j] == LinesApplication.EColor.NONE) {
    				  if(count == n) {
    					  myApp.Myplane[i][j] = myApp.newballs[k];
    					  myApp.lastballs_pos[k].i = i;
    					  myApp.lastballs_pos[k].j = j;
    					  found = true;
    					  if (myApp.check_for_lines(i,j))
    						  myApp.redo_is_active = false;
    					  Log.d("lines", "i: " + Integer.toString(i)+ "j: " + Integer.toString(j));
     				  }
    				  count ++;
    			  }
    			  if(found) break;
    		  }
    		  if(found) break;
    	  }
    	  myApp.Freeroom--;
    	  if (myApp.Freeroom == 0)
    		  break;
       }
       
       //ScoreView.setText(Integer.toString(Freeroom) + "  ");
    }
    
    
  @Override
  public void run() {
    // TODO Auto-generated method stub
    while(locker){
      //checks if the lockCanvas() method will be success,and if not, will check this statement again
      if(!holder.getSurface().isValid()){
        continue;
      }
      //ScoreView.setText(Integer.toString(Score) + "  ");
      if(myApp.Movement_in_Progress) {
    	  if(skip == 0) {
     	      //EColor mycolor = Myplane[active.i][active.j];
    		  //EColor mycolor = Myplane[Path[currstep].i][Path[currstep].j];
    		  Log.d("lines", "Color from: " + Integer.toString(myApp.active.i)+ " " + Integer.toString(myApp.active.j));
     	      Log.d("lines", "Path currstep: " + Integer.toString(myApp.currstep)+ " " + Integer.toString(myApp.pathlength));
     	      //ScoreView.setText(Integer.toString(Score) + "  ");
     	      
    	      if(myApp.currstep + 1 < myApp.pathlength) {
    	         //mP.start();
    	    	 myApp.Myplane[myApp.Path[myApp.currstep].i][myApp.Path[myApp.currstep].j] = LinesApplication.EColor.NONE;
    	         myApp.currstep++;
    	         myApp.active = myApp.Path[myApp.currstep];
    	         myApp.Myplane[myApp.active.i][myApp.active.j] = myApp.active_color;
    	         //Log.d("lines", "Set color: " + Integer.toString(active.i)+ " " + Integer.toString(active.j)+ " " + Integer.toString(mycolor.int_EColor()));
           	  }
    	      skip = 2;
    	  }
    	  else {
    		  skip--;
    	  }
       }
      
      Canvas canvas = holder.lockCanvas();
      //xwidth = (canvas.getWidth()-40)/9;
      //xtop = (canvas.getHeight()-40)/9;
      
      //ALL PAINT-JOB MAKE IN draw(canvas); method.
      draw(canvas);
      
      // End of painting to canvas. system will paint with this canvas,to the surface.
      holder.unlockCanvasAndPost(canvas);
      if(myApp.Movement_in_Progress && myApp.currstep == myApp.pathlength - 1 ) {
    	  Log.d("lines", "End of movement: " + Integer.toString(myApp.currstep));
    	  myApp.Movement_in_Progress = false;
          myApp.clear_visited();
          skip = 2;
          
          if(!myApp.check_for_lines(myApp.active.i,myApp.active.j)) {
            balls_position(myApp.balls_count);
            newballs(myApp.balls_count);
            if(myApp.Freeroom == 0) {
            	EndGame();
            }
          } 
          else {
        	  myApp.redo_is_active = false;
          }
          myApp.active.i = -1;
          myApp.active.j = -1;
          UpdateGUI();
      }
      if (myApp.Replacement_in_Progress) {
          LinesApplication.EColor mcol = myApp.Myplane[myApp.active.i][myApp.active.j];
	      LinesApplication.EColor mcol2 = myApp.Myplane[myApp.to.i][myApp.to.j];
	      myApp.Myplane[myApp.active.i][myApp.active.j] = mcol2;
	      myApp.Myplane[myApp.to.i][myApp.to.j] = mcol;
	      if(!myApp.check_for_lines(myApp.active.i,myApp.active.j) &&
             !myApp.check_for_lines(myApp.to.i,myApp.to.j)) {
	           balls_position(myApp.balls_count);
	           newballs(myApp.balls_count);
	           if(myApp.Freeroom == 0) {
	             EndGame();
	           }
               myApp.redo_is_active=false; 	           
	      }
	      else {
	    	  myApp.redo_is_active = false;
	      }
	      myApp.active.i = -1;
	      myApp.active.j = -1;
	      UpdateGUI();
	      myApp.Replacement_in_Progress=false;
      }
      
      
    }
  }
  
  private void draw_ball(Canvas canvas, Paint paint, int color, int radius, int x, int y) {
	   paint.setColor(color);
	   canvas.drawCircle(x, y, radius, paint);	  
  }
  
  private void draw_arc(Canvas canvas, Paint paint, int color, int color_week, RectF oval) {
	  float h = (oval.bottom - oval.top)/3;
	  float w = (oval.right - oval.left)/3;
	  RectF n_oval = new RectF(oval.left+w/2,oval.top+h/2,oval.left+2*w,oval.top+2*h);
	  float h1 = (n_oval.bottom - n_oval.top)/3;
	  float w1 = (n_oval.right - n_oval.left)/3;
	  RectF w_oval = new RectF(n_oval.left+w1/2,n_oval.top+h1/2,n_oval.left+2*w1,n_oval.top+2*h1);
	   paint.setColor(color);
	   //canvas.drawCircle(x, y, radius, paint);
	   canvas.drawArc(oval, 0, 360, true, paint);
	   paint.setColor(color_week);
	   canvas.drawArc(n_oval, 0, 360, true, paint);
	   paint.setColor(getResources().getColor(android.R.color.white));
	   canvas.drawArc(w_oval, 0, 360, true, paint);
 }
  
  
  /**This method deals with paint-works. Also will paint something in background*/
  private void draw(Canvas canvas) {
    // paint a background color
    canvas.drawColor(android.R.color.holo_blue_bright);
    int width,height, minsize;
    int msize;
    int vert = 0;
    
    if(jmp == maxjmp) {
    	speed=-1;
    } 
    if(jmp == 0) {
    	speed = 1;
    }
    jmp = jmp + speed;
    
    width = canvas.getWidth();
    height = canvas.getHeight();
    if (width < height)
       vert = 1;
    minsize = Math.min(width, height);
    msize = minsize;
    
    xwidth = (msize-40)/9;
    xtop =   (msize-40)/9;
    
    // paint a rectangular shape that fill the surface.
    int border = 20;
    RectF r = new RectF(border, border, msize-border, msize-border);
    Paint paint = new Paint();    
    paint.setARGB(200, 135, 135, 135); //paint color GRAY+SEMY TRANSPARENT 
    canvas.drawRect(r , paint );
    // paint a rectangular shape for new balls
    /*if (vert == 1) {
       r =	new RectF(border,msize+border,border+3*xwidth,msize+border+xtop);
    }
    else {
   	   r =	new RectF(msize+border,border,msize+border+xwidth,border+xtop*3);	
    }
    canvas.drawRect(r, paint);*/
    
    /*
     * i want to paint to circles, black and white. one of circles will
     * bounce, tile the button 'swap' pressed and then other circle begin bouncing.
     */
    calculateRadiuses();
    //paint left circle(black)
    //paint.setColor(getResources().getColor(android.R.color.black));
    //canvas.drawCircle(canvas.getWidth()/4, canvas.getHeight()/2, radiusBlack, paint);
    //draw_ball(canvas,paint,getResources().getColor(android.R.color.black),
    //		radiusBlack,canvas.getWidth()/4, canvas.getHeight()/2);
    int color_1 = getResources().getColor(R.color.color_1);
    int color_2 = getResources().getColor(R.color.color_2);
  
    for(int i=0; i<9; i++) {
       for(int j=0; j<9; j++) {
    	   float top,left,right,bottom = 0;
 	      RectF square = new RectF(20+xwidth*i,20+xtop*j,20+xwidth*(i+1),20+xtop*(j+1));
    	  
 	      if( (i+j)%2 == 0) {
 	    	  paint.setColor(color_1);  
 	      }
 	      else {
 	    	  paint.setColor(color_2); 
 	      }
 	      canvas.drawRect(square , paint );  
          if(myApp.Myplane[i][j] != LinesApplication.EColor.NONE){

    	      
        	  if(i == myApp.active.i && j == myApp.active.j ) {
    	    	  top = 20+xwidth*i+ddd-jmp;
    	    	  left = 20+xtop*j+ddd-jmp;
    	    	  bottom = 20+xwidth*(i+1)-ddd+jmp;
    	    	  right = 20+xtop*(j+1)-ddd+jmp;  
    	      }
    	      else {   
    	    	  top = 20+xwidth*i+ddd;
    	    	  left = 20+xtop*j+ddd;
    	    	  bottom = 20+xwidth*(i+1)-ddd;
    	    	  right = 20+xtop*(j+1)-ddd;
    	      }   
              RectF oval = new RectF(top,left,bottom,right);
    	      LinesApplication.EColor ecolor = myApp.Myplane[i][j];
    	      int color_ = Colors[ecolor.int_EColor()][0];
              int color_weak = Colors[ecolor.int_EColor()][1];
              draw_arc(canvas,paint,color_,color_weak,oval);
          }
    
          //paint right circle(white)
          //paint.setColor(getResources().getColor(android.R.color.white));
          //canvas.drawCircle(canvas.getWidth()/4*3, canvas.getHeight()/2, radiusWhite, paint);
          //draw_ball(canvas,paint,getResources().getColor(R.color.color_red),
          //		 radiusWhite,canvas.getWidth()/4*3, canvas.getHeight()/2);
    
          //RectF oval2 = new RectF(canvas.getWidth()/4*3-radiusWhite, canvas.getHeight()/2-radiusWhite,
    	  //	               canvas.getWidth()/4*3+radiusWhite, canvas.getHeight()/2+radiusWhite);
          //int color_red = getResources().getColor(R.color.color_red);
          //int color_red_weak = getResources().getColor(R.color.color_red_weak);
          //draw_arc(canvas,paint,color_red,color_red_weak,oval2);
       }
    }
    
    color_1 = getResources().getColor(R.color.color_1);
    color_2 = getResources().getColor(R.color.color_2);
  
    for(int i=0; i<myApp.balls_count; i++) {
          float top,left,right,bottom = 0;
 	      RectF square;
 	      
 	      if (vert == 1) {
        	  square =	new RectF(border+xwidth*i,msize+border,border+xwidth*(i+1),msize+border+xtop);
          }
          else {
         	  square =	new RectF(msize+border,border+xtop*i,msize+border+xwidth,border+xtop*(i+1));	
          }
 	          
 	      if( i%2 == 0) {
 	    	  paint.setColor(color_1);  
 	      }
 	      else {
 	    	  paint.setColor(color_2); 
 	      }
 	      canvas.drawRect(square , paint );  
          if(myApp.newballs[i] != LinesApplication.EColor.NONE) {
        	  RectF oval;
	    	  if (vert == 1) {
        	     oval = new RectF(border+xwidth*i+ddd,msize+border+ddd,border+xwidth*(i+1)-ddd,msize+border+xtop-ddd);
	    	  }
        	  else {
        		 oval = new RectF(msize+border+ddd,border+xtop*i+ddd,msize+border+xwidth-ddd,border+xtop*(i+1)-ddd);
        	  }        		  
              
              LinesApplication.EColor ecolor = myApp.newballs[i];
	          int color_ = Colors[ecolor.int_EColor()][0];
              int color_weak = Colors[ecolor.int_EColor()][1];
              draw_arc(canvas,paint,color_,color_weak,oval);  
          }
    } 
    
    
    
  }

  private void calculateRadiuses() {
    // TODO Auto-generated method stub
    if(left){
      updateSpeed(radiusBlack);
      radiusBlack += speed;
      radiusWhite = baseRadius;
    }
    else{
      updateSpeed(radiusWhite);
      radiusWhite += speed;
      radiusBlack = baseRadius;
    }
  }
  /**Change speed according to current radius size.
   * if, radius is bigger than maxRad the speed became negative otherwise 
   * if radius is smaller then baseRad speed will positive.
   * @param radius
   */
  private void updateSpeed(int radius) {
    // TODO Auto-generated method stub
    if(radius>=maxRadius){
      speed = -baseSpeed;
    }
    else if (radius<=baseRadius){
      speed = baseSpeed;
    }
    
  }

  @Override
  protected void onPause() {    
	Log.d("lines", "LinesViewActivity onPause");
	saveGame();
	stopService(new Intent(getBaseContext(),RobotService.class));
	super.onPause();
    pause();
  }
  
  private void pause() {
    //CLOSE LOCKER FOR run();
    locker = false;
    while(true){
      try {
        //WAIT UNTIL THREAD DIE, THEN EXIT WHILE LOOP AND RELEASE a thread
        thread.join();
      } catch (InterruptedException e) {e.printStackTrace();
      }
      break;
    }
    thread = null;
  }

  @Override
  protected void onResume() {
    Log.d("lines", "LinesViewActivity resume");
    newBallsString = myApp.prefs.getString("newballs", "4");
	myApp.balls_count = Integer.parseInt(newBallsString);
	if (myApp.balls_count == 3 )
		myApp.Replacement_support = false;
	loadGame();
    super.onResume();
    resume();    
  }

  private void resume() {
    //RESTART THREAD AND OPEN LOCKER FOR run();
    locker = true;
    thread = new Thread(this);
    thread.start();
  }

@Override
public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
		String key) {
	// TODO Auto-generated method stub
	Log.d("lines", "onSharedPreferenceChanges"+newBallsString);
	newBallsString = myApp.prefs.getString("newballs", "4");
	myApp.balls_count = Integer.parseInt(newBallsString);
	if (myApp.balls_count == 3 )
		myApp.Replacement_support = false;
	loadGame();
}
} 



/*
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SurfaceViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.surface_view, menu);
		return true;
	}

}*/
