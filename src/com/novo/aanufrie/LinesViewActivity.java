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
  private TextView ScoreView;
  private boolean locker = true;
  private Thread thread;
  private int radiusBlack, radiusWhite;
  private boolean left = true;
  
  private boolean showPath = true;
  private coordinate Path[] = new coordinate[81];
  private int pathlength=0;
  private boolean Movement_in_Progress = false;
  private int currstep = 0;
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
  private coordinate active,to;
  private coordinate before, redo;
  EColor active_color = EColor.NONE;
  float xwidth=0;
  float xtop=0;
  final Handler myHandler = new Handler();
  String filename = "lines.save";
  static String UserName;
  static String MyScore;
  
  // next balls
  private EColor newballs[] = new EColor[10];
  private EColor lastballs[] = new EColor[10];
  private coordinate lastballs_pos[] = new coordinate[10];
  private int balls_count = 0;
  private int Freeroom = 0;
  private int lastFreeroom = 0;
  private boolean redo_is_active = false;
  private boolean robot_is_active = false;
  private int Score = 0;
    
  public EColor Myplane[][] = new EColor[9][9];
  public EColor MyplaneCopy[][] = new EColor[9][9];
  public int Colors[][] = new int[8][2];
  public boolean visited[][] = new boolean[9][9];
  private coordinate all_balls[] = new coordinate[36];
  int balls_in_lines =0;
  
  DbHelper dbHelper;
  //SQLiteDatabase db;
  //Cursor cursor;
  
  public class coordinate{
	  public int i=0;
      public int j=0;
      public coordinate(int mi, int mj) {i = mi; j = mj;}
  }

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
  
  
  public enum EColor {
	  NONE(0),RED(1),GREEN(2),BLUE(3),YELLOW(4),BLACK(5),VIOLET(6),ORANGE(7);
	  EColor(int value) {this.value=value;}
	  private final int value;
	  public int int_EColor() {
         /*int ret;		 
		 switch(this) {
		 case NONE:
		    ret = 0;
		    break;
		 case RED:
			ret = 1;
			break;
		 case GREEN:
			ret = 2;
			break;
		 case BLUE:
			ret = 3;
			break;
		 case YELLOW:
			ret = 4;
			break;			
		 case BLACK:
			ret = 5;
			break;
		 case VIOLET:
			ret = 6;
			break;
		 case ORANGE:
			ret = 7;
			break;	
		 default:
			ret = 0;
		 }*/
		 return value;  
	  }
  }
	
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
				saveGame();
				startActivity(new Intent(this, SettingActivity.class));
				balls_count=Integer.parseInt(newBallsString);
				loadGame();
				break;
	    }
		
		return true;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("lines", "onCreate" );
    	super.onCreate(savedInstanceState);
    	
       	newBallsString = ((LinesApplication)getApplication()).prefs.getString("newballs", "4");
    	Log.d("lines", "New Balls " + newBallsString );
    	filename="lines"+newBallsString+".save";
    	balls_count = Integer.parseInt(newBallsString);
    	setContentView(R.layout.activity_surface_view);
    	
    	newGameBtn = (Button) findViewById(R.id.buttonswap);
    	recBtn    = (Button) findViewById(R.id.buttonrec);
    	//loadBtn    = (Button) findViewById(R.id.buttonread);
    	redoBtn      = (Button) findViewById(R.id.buttonredo);
    	helpBtn      = (Button)findViewById(R.id.buttonhelp);
    	robotBtn     = (ToggleButton)findViewById(R.id.buttonrobot);
    	ScoreView = (TextView) findViewById(R.id.myScore);
    	surface = (SurfaceView) findViewById(R.id.mysurface);
    	
    	/* LinearLayout mainlayout = new LinearLayout(this);
    	mainlayout.setOrientation(LinearLayout.VERTICAL);
    	LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    	setContentView(mainlayout,layoutParam);
    	
    	SurfaceView surface = new MySurfaceView(this);
    	LayoutParams surfaceParam = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	setContentView(surface,surfaceParam);
    	//mainlayout.addView(surface,surfaceParam);
    	mainlayout.addView(surface);*/
    	
    	/*LayoutParams lpView = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	Button newGameBtn = new Button(this);
    	newGameBtn.setText("New Game");
    	setContentView(newGameBtn,lpView);
    	mainlayout.addView(newGameBtn);
    	
    	Button redoBtn = new Button(this);
    	newGameBtn.setText("Redo");
    	setContentView(redoBtn,lpView);
    	mainlayout.addView(redoBtn);
    	
    	Button recBtn = new Button(this);
    	newGameBtn.setText("Records");
    	setContentView(recBtn,lpView);
    	mainlayout.addView(recBtn);
    	
    	TextView ScoreView = new TextView(this);
    	ScoreView.setText("0  ");
    	setContentView(ScoreView,lpView);
    	mainlayout.addView(ScoreView);*/
        
    	//ScoreView.setText("110  ");        
        active = new coordinate(0,0);
        to = new coordinate(0,0);
        before = new coordinate(0,0);
        redo   = new coordinate(0,0);
        
        holder = surface.getHolder();
        

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
        		Find_Best_Movement();
        	}
        });
        
        robotBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
              		robot_is_active = true;
            		//Robot();
              		startService(new Intent(getBaseContext(),RobotService.class));
                	// The toggle is enabled
                } else {
                	robot_is_active = false;
                    // The toggle is disabled
                	stopService(new Intent(getBaseContext(),RobotService.class));
                }
            }
        });
        
            
        
        surface.setOnTouchListener(new OnTouchListener() {
        	
      @Override
      public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int i = (int)((x-20)/xwidth);
        int j = (int)((y-20)/xtop);
        
        if(i >= 9 || j >= 9 || i < 0 || j < 0)
        	return false;
        if(Movement_in_Progress)
        	return false;
        
        if(active.i >= 0 && active.j >= 0) {
        	if(Myplane[i][j] == EColor.NONE) {
        		to.i = i;
        		to.j = j;
        		Log.d("lines", "to: " + Integer.toString(active.i)+ " " + Integer.toString(active.j));
        		saveStatus();
        		moveBall(active,to);
        	}
        	else {
        	    active.i = i;
        	    active.j = j;
        	    active_color = Myplane[i][j];
        	    Log.d("lines", "to: " + Integer.toString(active.i)+ " " + Integer.toString(active.j)); 
        	}
        }
        else {
        	if(Myplane[i][j] != EColor.NONE) {
        		active.i = i;
        		active.j = j;
        		active_color = Myplane[i][j];
        		Log.d("lines", "to: " + Integer.toString(active.i)+ " " + Integer.toString(active.j));        
        	}
        }
           	
    	
        return false;
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
    		  Myplane[i][j] = (EColor.values())[(i+j)%8];
    	  }
      }
      for(int i=0; i < 81; i++) {
          Path[i] = new coordinate(0,0);
      }
      active.i = -1;
      active.j = -1;
      
      for(int i=0; i<10; i++) {
    	lastballs_pos[i] = new coordinate(0,0);
      }
      for(int i=0; i<36; i++) {
    	all_balls[i] = new coordinate(0,0);
      }
       
      if(!loadGame()) startNewGame();
      //UpdateGUI();
      
     }
  
     private void UpdateGUI() {
        //tv.setText(String.valueOf(i)); //This causes a runtime error.
        myHandler.post(myRunnable);
     }
    
     final Runnable myRunnable = new Runnable() {
         public void run() {
        	 ScoreView.setText(Integer.toString(Score)+ "  "); 
         }
      };
     
    private void clear_visited() {
       for(int i=0; i<9; i++){
    	  for(int j=0; j<9; j++) {
    		  visited[i][j] = false;
    	  }
       }
    }
    
    
    private boolean find_path(coordinate from, coordinate to) {
      coordinate nbs[] = new coordinate[4];
      coordinate next = new coordinate(0,0);
      coordinate curr = new coordinate(0,0);
      curr.i = from.i; curr.j = from.j;
      boolean found = false;
      //Log.d("lines", "findPath: From " + Integer.toString(from.i)+ " " + Integer.toString(from.j) );
      //Log.d("lines", "To " + Integer.toString(to.i)+ " " + Integer.toString(to.j) );
      //pathlength++;
      //currstep++;
      if(from.i < 0 || from.i >= 9 || from.j <0 || from.j >= 9) {
    	  return false; 
      }
      if(from.i == to.i && from.j == to.j) {
    	 //pathlength++;
    	 Path[currstep].i = to.i;
    	 Path[currstep].j = to.j;
    	 //Log.d("lines", "Path found "+ " " + Integer.toString(pathlength));
    	 //Log.d("lines", "addpath:" + Integer.toString(currstep)+ ": " + Integer.toString(to.i)+ " " + Integer.toString(to.j));
    	 return true;
      }
      nbs[0]=new coordinate(0,0);
      nbs[1]=new coordinate(0,0);
      nbs[2]=new coordinate(0,0);
      nbs[3]=new coordinate(0,0);
      if(from.i < to.i && from.j < to.j) {
    	  nbs[0].i = 1; nbs[0].j = 0;
    	  nbs[1].i = 0; nbs[1].j = 1;
    	  nbs[2].i = -1; nbs[2].j = 0;
    	  nbs[3].i = 0; nbs[3].j = -1;
      }
      if(from.i < to.i && from.j >= to.j) {
    	  nbs[0].i = 1; nbs[0].j = 0;
    	  nbs[1].i = 0; nbs[1].j = -1;
    	  nbs[2].i = -1; nbs[2].j = 0;
    	  nbs[3].i = 0; nbs[3].j = 1;
      }
      if(from.i >= to.i && from.j >= to.j) {
    	  nbs[0].i = -1; nbs[0].j = 0;
    	  nbs[1].i = 0; nbs[1].j = -1;
    	  nbs[2].i = 1; nbs[2].j = 0;
    	  nbs[3].i = 0; nbs[3].j = 1;
      }
      if(from.i >= to.i && from.j < to.j) {
    	  nbs[0].i = -1; nbs[0].j = 0;
    	  nbs[1].i = 0; nbs[1].j = 1;
    	  nbs[2].i = 1; nbs[2].j = 0;
    	  nbs[3].i = 0; nbs[3].j = -1;
      }
      visited[from.i][from.j] = true;
      pathlength++;
      currstep++;
      for(int i = 0; i< 4; i++) {
    	  int myi = from.i+nbs[i].i ;
    	  int myj = from.j+nbs[i].j ;
    	  //Log.d("lines", "findPath: myi " + Integer.toString(myi)+ "myj " + Integer.toString(myj) );
    	  if( myi >= 0 && myi < 9 && myj >= 0 && myj < 9 &&
    	      !visited[myi][myj] && Myplane[myi][myj] == EColor.NONE) {
    		  next.i = myi; next.j = myj;
      		  found = find_path(next,to);
    		  if(found) { 
    			currstep--;
    		   	Path[currstep].i = curr.i;
    	    	Path[currstep].j = curr.j;
    	    	 //Log.d("lines", "Addpath:" + Integer.toString(currstep)+ ": " + Integer.toString(curr.i)+ " " + Integer.toString(curr.j));
    	    	return true;
    		  }
    	  }
      }
	  pathlength--;
	  currstep--;   
      return false;
      
    }
    
    private void check_direction(coordinate ball_pos, int di, int dj){
    	EColor mycolor = Myplane[ball_pos.i][ball_pos.j];
        int i = ball_pos.i;
        int j = ball_pos.j;
    	
    	while(true) {
    	  i += di;
    	  j += dj;
    	  if(i>=0 && i<9 && j>=0 && j<9 && mycolor == Myplane[i][j]) {
    		  all_balls[balls_in_lines].i = i;
    		  all_balls[balls_in_lines].j = j;
    		  balls_in_lines++;
    	  }
    	  else {
    		  return;
    	  }
    	}
    }
    
    private boolean check_for_lines(coordinate ball_pos) {
        //coordinate direction = new coordinate(0,0);
    	EColor mycolor = Myplane[ball_pos.i][ball_pos.j];
    	int prev = 1;
        int number_of_lines = 0;
        
        
        Log.d("lines", "check_for_lines:" + Integer.toString(Freeroom) + " " + Integer.toString(ball_pos.i) + " " + Integer.toString(ball_pos.j));
        //ScoreView.setText(Integer.toString(Score) + "  ");
        if(mycolor == EColor.NONE) {
        	Log.d("lines", "Ups");
        	return false;
        }
        
        balls_in_lines=0;
 		all_balls[0].i = ball_pos.i;
		all_balls[0].j = ball_pos.j;
		balls_in_lines++;
        check_direction(ball_pos,1,0);
        check_direction(ball_pos,-1,0);
        if(balls_in_lines - prev < 4) { 
        	balls_in_lines = prev;
        }
        else {
        	number_of_lines++;
        	Log.d("lines", "1" + " " + Integer.toString(balls_in_lines));      	
        }
        prev = balls_in_lines;
        check_direction(ball_pos,1,1);
        check_direction(ball_pos,-1,-1);
        if(balls_in_lines - prev < 4) { 
        	balls_in_lines = prev;
        }
        else {
        	Log.d("lines", "2"+ " " + Integer.toString(balls_in_lines));  
        	number_of_lines++;
        }
        prev = balls_in_lines;
        check_direction(ball_pos,0,1);
        check_direction(ball_pos,0,-1);
        if(balls_in_lines - prev < 4) { 
        	balls_in_lines = prev;
        }
        else {
        	Log.d("lines", "3"+ " " + Integer.toString(balls_in_lines));  
        	number_of_lines++;
        }
        prev = balls_in_lines;
        check_direction(ball_pos,1,-1);
        check_direction(ball_pos,-1,1);
        if(balls_in_lines - prev < 4) {
        	balls_in_lines = prev;
        }
        else {
        	Log.d("lines", "4"+ " " + Integer.toString(balls_in_lines));
        	number_of_lines++;
        }
        
        if(balls_in_lines >= 5) {
            for(int i=0; i < balls_in_lines; i++) {
            	Log.d("lines", "all balls:"+ " " + Integer.toString(all_balls[i].i) + " " + Integer.toString(all_balls[i].j));
            	Myplane[all_balls[i].i][all_balls[i].j] = EColor.NONE;
            	Freeroom++;
            }
            Score = Score + 2*(number_of_lines * balls_in_lines);
            Log.d("lines", "Score: " + Integer.toString(Score));
            //ScoreView.setText(Integer.toString(Score) + "  ");
            //ScoreView.setText("110  ");
        	return true;
        }
        Log.d("lines", "Score: " + Integer.toString(Score));
        //ScoreView.setText(Integer.toString(Score) + "  ");
        return false; 
    }
    
    
    private void moveBall(coordinate from, coordinate to) {
      EColor mcol = Myplane[from.i][from.j];
      Log.d("lines", "moveBall Freeroom:" + Integer.toString(Freeroom)+ " " + Integer.toString(mcol.int_EColor()));
      clear_visited();
      pathlength = 1;
      currstep = 0;
      //ScoreView.setText(Integer.toString(Score++)+ "  "); 
      if(find_path(from,to)) {
         /*Myplane[from.i][from.j] = EColor.NONE;
         Myplane[to.i][to.j] = mcol;*/
    	 //Log.d("lines", "addpath:" + "0:"+ " " + Integer.toString(from.i)+ " " + Integer.toString(from.j));
    	 //Path[0].i = from.i;
	     //Path[0].j = from.j;
    	 Movement_in_Progress = true;
    	 Log.d("lines", "moveBall pathlength:" + Integer.toString(pathlength));
    	 currstep=0;
       	 for(int i=0; i<pathlength;i++) {
    		 Log.d("lines", "Path:" + Integer.toString(i) + " " + Integer.toString(Path[i].i) + " " + Integer.toString(Path[i].j)); 
    	 }
         /*if(!check_for_lines(to)) {
            ball_position();
            newballs();
         }*/
      }
      myHandler.post(myRunnable);
      //ScoreView.setText(Integer.toString(Score++)+ "  "); 
      //active.i = -1;
      //active.j = -1;
    		  
    }
    
    public void saveStatus() {
       for(int i=0; i< balls_count; i++) {
    	   lastballs[i] = newballs[i];
       }
       redo.i = to.i; redo.j = to.j;
       before.i = active.i; before.j = active.j;
       redo_is_active = true;
    }
    
    public void Redo() {
       if(redo_is_active) {
    	   for(int i=0; i < balls_count; i++) {
    		   newballs[i] = lastballs[i];
    		   Myplane[lastballs_pos[i].i][lastballs_pos[i].j] = (EColor.values())[0];
    		   Freeroom++;
    	   }
    	   Myplane[before.i][before.j]= Myplane[redo.i][redo.j];
    	   Myplane[redo.i][redo.j] = (EColor.values())[0];
       }
       redo_is_active = false;
       UpdateGUI();
    }
    
    public void PlainCopy() {
    	for(int i=0; i < 9; i++) {
    		for(int j=0; j < 9; j++ ) {
    			MyplaneCopy[i][j] = Myplane[i][j];
    		}
    	}
    }
    
    public int Estimate_Line(coordinate from, coordinate direction){
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
    	    	   	
    	for(EColor color:EColor.values()) {
    	
    		if (color == EColor.NONE )
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
    	                if(MyplaneCopy[nexti][nextj] == color)
    	                  kol_of_color++;
    	                if(MyplaneCopy[nexti][nextj] == EColor.NONE) 
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
    }
    
    public int Estimate_Position(coordinate from, coordinate to) {
       int cost =0;
 
       EColor color = MyplaneCopy[from.i][from.j];
       MyplaneCopy[from.i][from.j] = EColor.NONE;
       MyplaneCopy[to.i][to.j]=color;
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
       MyplaneCopy[from.i][from.j] = color;
       MyplaneCopy[to.i][to.j]=EColor.NONE;
       
       return cost;
    }
    
    public void Robot() {
    	while (robot_is_active && Freeroom != 0) {
    		Find_Best_Movement();
    		UpdateGUI();
    	}
    	
    }
    
    public void Find_Best_Movement() {
       coordinate best_from = new coordinate(0,0);
       coordinate best_to = new coordinate(0,0);
       int best_cost = 0;
       int cost = 0;
       
       Log.d("lines", "Find_Best_Movement start");     
       if (Movement_in_Progress)
    	   return;
       // for working with copy of Myplane
       PlainCopy();        
                
       for (int i=0; i<9; i++) {
    	   for (int j=0; j<9; j++) {
    		   if (Myplane[i][j] != EColor.NONE) {
    			   for (int k=0; k<9; k++) {
    				   for (int m=0; m<9; m++) {
    					   if(Myplane[k][m] == EColor.NONE) {
    						   clear_visited();
    						   if (find_path(new coordinate(i,j),new coordinate(k,m))) {
    							   cost = Estimate_Position(new coordinate(i,j),new coordinate(k,m));
    							   if (cost > 0)
    							       Log.d("lines", "Find_Best_Movement " + Integer.toString(i)+":"+Integer.toString(j)+"->"+Integer.toString(k)+":"+Integer.toString(m)+" " + Integer.toString(cost));
    							   if (cost > best_cost) {
    								   best_cost = cost;
    								   best_from.i = i;
    								   best_from.j = j;
    								   best_to.i = k;
    								   best_to.j = m;
    							   }
    						   }
    					   }
    				   }
    			   }
    		   }
    	   }
       }
        
       if( best_cost > 0) {
 	      Log.d("lines", "Find_Best_Movement "+Integer.toString(best_from.i)+" "+
               Integer.toString(best_from.j)+" "+Integer.toString(best_to.i)+" "+Integer.toString(best_to.j) ); 
 	      // These values are used by run(). Should be defined. 
 	      active = best_from;
 	      active_color = Myplane[best_from.i][best_from.j];
 	      to = best_to;
 	      saveStatus();
 	      moveBall(best_from,best_to);
       }
}
    
    public void startNewGame() {
    	Log.d("lines", "startNewGame "); 
    	for(int i=0; i<9; i++){
	    	  for(int j=0;j<9;j++){
	    		  Myplane[i][j] = (EColor.values())[0];
	    	  }
	   }
	      
  	   Freeroom = 9*9;
  	   Score = 0;
	   active.i = -1;
	   active.j = -1;
	   balls_count = 7;
	   Movement_in_Progress = false;
	   newballs();
	   ball_position();
       balls_count = Integer.parseInt(newBallsString);
       newballs();
       UpdateGUI();
    }

    public void saveGame() {
    	FileOutputStream outputStream;
    	Log.d("lines", "saveGame"+newBallsString);
    	balls_count = Integer.parseInt(newBallsString);
    	filename="lines"+newBallsString+".save";
    	try {
    	  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
    	  for (int i=0; i<9; i++) {
    	     for (int j=0; j<9 ; j++) {
    	    	 outputStream.write(((Integer.toString(Myplane[i][j].int_EColor())+";").getBytes()));
    	     }
    	  }
    	  for (int i=0; i<balls_count; i++) {
    	     outputStream.write(((Integer.toString(newballs[i].int_EColor())+";").getBytes())); 
    	  }
    	  outputStream.write((Integer.toString(Score)).getBytes());
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
        		   Myplane[i][j]= (EColor.values())[Integer.parseInt(mcolor[num++])];
        	       //num++;	   
        	   }
           }
           Log.d("lines","Newballs: "+newBallsString);
           balls_count = Integer.parseInt(newBallsString);
           for (int i=0; i<balls_count; i++ ) {
              newballs[i] = (EColor.values())[Integer.parseInt(mcolor[num++])];
              Log.d("lines","Newballs["+Integer.toString(i)+"]="+mcolor[num]);
              //num++;           		 
           }
           Log.d("lines","Score+Freeroom ");
           Score = Integer.parseInt(mcolor[num++]);
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
		
		if(nRec < 10 || Score > minScore) {
		   String myret; 
		   Log.d("lines","EndGame " + UserName +" "+Integer.toString(Score));
           Context context = this; 	
   		   //Intent intent = new Intent(context,EditRecActivity.class);
   		   Intent intent = new Intent(getBaseContext(),EditRecActivity.class);
           intent.putExtra("Score",Integer.toString(Score));
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
        int Score = Integer.parseInt(MyScore);
        Cursor cursor;
    	
      	Log.d("lines","insertRecord "+MyScore);
    	
    	dbHelper = new DbHelper(this); 
    	Log.d("lines","dbHelper");
    	    	
    	cursor = db.query(DbHelper.TABLE, null, DbHelper.C_BALLS_NUMBER+"="+Complexity, null, null, null,
		DbHelper.C_CREATED_AT + " DESC"); //
		//startManagingCursor(cursor); //
		// Iterate over all the data and print it out
		String user, score, output;
		
		if (cursor.moveToFirst()) {
		  do {
		      int rec_score;
		      nRec++;
		      ID   = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DbHelper.C_ID)));
		      user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); //
		      score = cursor.getString(cursor.getColumnIndex(DbHelper.C_SCORE));
		      rec_score = Integer.parseInt(score);
		      Log.d("lines","insertRecord move:  " + Integer.toString(rec_score));
		      if(minScore > rec_score) {
			      minScore = rec_score;
			      delID = ID;
		      }
	 	  } while (cursor.moveToNext());
		}
		Log.d("lines","insertRecord:  " + Integer.toString(minScore)+" "+Integer.toString(nRec));
		        		
			
		//myScore = Integer.parseInt(Score);
		if(nRec >= 10 && Score > minScore) {
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
            	Log.d("lines","info"+UserName+Integer.toString(Score));
                insertRecord(db,UserName,Integer.toString(Score),newBallsString);          	
  		        db.close();
            }
            else {
                Log.d("lines","RETURN CANCEL"); 
            }
        }
    }
    
    private void newballs() {
       Log.d("lines", "newballs "+ Integer.toString(balls_count));
       //balls_count = Integer.parseInt(newBallsString);
       //if(Freeroom < balls_count) {
       //   balls_count = Freeroom;
       //}	
       for(int i=0; i< balls_count; i++ ){
    	  int randnum = rand.nextInt();
    	  if(randnum<0) randnum = -randnum;
    	  Log.d("lines", "randnum "+ Integer.toString(randnum));
    	  newballs[i] = (EColor.values())[(randnum%7)+1];
         }  
    }
    
    private void calc_freeroom() {
    	Freeroom =0;
    	for (int i=0;i<9;i++) {
    		for (int j=0;j<9;j++) {
    			if(Myplane[i][j] == EColor.NONE) {
    				Freeroom++;
    			}
    		}
    	}
    }
    
    private void ball_position() {
       int n;
       Log.d("lines", "newballs "+ Integer.toString(balls_count));
       //if(Freeroom < balls_count) 
       //	   balls_count = Freeroom;
       for(int k=0; k < balls_count; k++) {
    	  n = rand.nextInt()%Freeroom;
    	  if(n<0) n=-n;
    	  int count = 0;
    	  boolean found = false;
    	  for(int i=0; i < 9; i++ ) {
    		  for(int j=0; j < 9; j++) {
    			  if(Myplane[i][j] == EColor.NONE) {
    				  if(count == n) {
    					  Myplane[i][j] = newballs[k];
    					  lastballs_pos[k].i = i;
    					  lastballs_pos[k].j = j;
    					  found = true;
    					  check_for_lines(new coordinate(i,j));
    					  Log.d("lines", "i: " + Integer.toString(i)+ "j: " + Integer.toString(j));
     				  }
    				  count ++;
    			  }
    			  if(found) break;
    		  }
    		  if(found) break;
    	  }
    	  Freeroom--;
    	  if (Freeroom == 0)
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
      if(Movement_in_Progress) {
    	  if(skip == 0) {
     	      //EColor mycolor = Myplane[active.i][active.j];
    		  //EColor mycolor = Myplane[Path[currstep].i][Path[currstep].j];
    		  Log.d("lines", "Color from: " + Integer.toString(active.i)+ " " + Integer.toString(active.j));
     	      Log.d("lines", "Path currstep: " + Integer.toString(currstep)+ " " + Integer.toString(pathlength));
     	      //ScoreView.setText(Integer.toString(Score) + "  ");
     	      
    	      if(currstep + 1 < pathlength) {
    	         Myplane[Path[currstep].i][Path[currstep].j] = EColor.NONE;
    	         currstep++;
    	         active = Path[currstep];
    	         Myplane[active.i][active.j] = active_color;
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
      if(Movement_in_Progress && currstep == pathlength - 1 ) {
    	  Log.d("lines", "End of movement: " + Integer.toString(currstep));
    	  Movement_in_Progress = false;
          clear_visited();
          skip = 2;
          
          if(!check_for_lines(active)) {
            ball_position();
            newballs();
            if(Freeroom == 0) {
            	EndGame();
            }
          } 
          else {
        	  redo_is_active = false;
          }
          active.i = -1;
          active.j = -1;
          UpdateGUI();
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
          if(Myplane[i][j] != EColor.NONE){

    	      
        	  if(i == active.i && j == active.j ) {
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
    	      EColor ecolor = Myplane[i][j];
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
  
    for(int i=0; i<balls_count; i++) {
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
          if(newballs[i] != EColor.NONE) {
        	  RectF oval;
	    	  if (vert == 1) {
        	     oval = new RectF(border+xwidth*i+ddd,msize+border+ddd,border+xwidth*(i+1)-ddd,msize+border+xtop-ddd);
	    	  }
        	  else {
        		 oval = new RectF(msize+border+ddd,border+xtop*i+ddd,msize+border+xwidth-ddd,border+xtop*(i+1)-ddd);
        	  }        		  
              
              EColor ecolor = newballs[i];
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
    saveGame();
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
	newBallsString = ((LinesApplication)getApplication()).prefs.getString("newballs", "4");
	balls_count = Integer.parseInt(newBallsString);
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
