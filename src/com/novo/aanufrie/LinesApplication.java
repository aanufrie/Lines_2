package com.novo.aanufrie;


import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class LinesApplication extends Application implements
		OnSharedPreferenceChangeListener {
	public SharedPreferences prefs;
	public EColor active_color = EColor.NONE;
	public EColor newballs[]  = new EColor[10];
	public EColor lastballs[] = new EColor[10];
	public EColor Myplane[][] = new EColor[9][9];
	public EColor MyplaneCopy[][] = new EColor[9][9];
	public coordinate Path[] = new coordinate[81];
	public coordinate active,to;
	public coordinate before, redo;
	public coordinate best_from, best_to;
	public coordinate lastballs_pos[] = new coordinate[10];
	public coordinate all_balls[] = new coordinate[36];
	public int currstep = 0;
	public boolean visited[][] = new boolean[9][9];
	public boolean Movement_in_Progress=false;
	public boolean Replacement_support=true;
	public boolean Rainbow_support=true;
	public boolean Ready = false;
	public long Changetime=System.currentTimeMillis();
	public MediaPlayer champPl;
	EColor curr_Color=EColor.YELLOW;
	EColor prev_Color=EColor.YELLOW;
	int cColor;
	int i=0;
	double increment=0xFFFFFFd/100000d;
	int ColorR = 0;
	int ColorR_weak = 0;
	public boolean Replacement_in_Progress=false;
	public int pathlength=0;
	int number_of_lines=0;
	int balls_in_lines =0;
	public int Freeroom = 0;
	public int lastFreeroom = 0;
	public int Score = 0;
	public int balls_count = 0;
	public boolean redo_is_active = false;
	public boolean robot_is_active = false;
	public Handler myHandler;
	public TextView ScoreView;
	public boolean Final = false;
	public long FinalPeriod;
	public int WT[][] = new int[6][6];
	
	 public enum EColor {
		  NONE(0),RED(1),GREEN(2),BLUE(3),YELLOW(4),BLACK(5),VIOLET(6),ORANGE(7),RAINBOW(8);
		  EColor(int value) {this.value=value;}
		  private final int value;
		  public int int_EColor() {
			 return value;  
		  }
	  }
	
	 public class coordinate{
		  public int i=0;
	      public int j=0;
	      public coordinate(int mi, int mj) {i = mi; j = mj;}
	 }
	 
	 public  Runnable myRunnable = new Runnable() {
	         public void run() {
	        	 ScoreView.setText(Integer.toString(Score)+ "  "); 
	         }
	      };
	 
     private int Weight(int kol_of_color,int kol_of_none) { 	      
        int weight = 0;
	       /*if (kol_of_color == 5 ) {
	    	   weight = 1000;
	       } else if (kol_of_color == 4 && kol_of_none == 1) {
	    	   weight = 300;
	       } else if (kol_of_color == 4) {
	    	   weight = 100;
	       } else if (kol_of_color == 3 && kol_of_none == 2) {
	           weight = 30 ; 
	       } else if (kol_of_color == 3 && kol_of_none == 1) {
	    	   weight = 20;
	       } else if (kol_of_color == 3) {
	    	   weight = 10;
	       } else if (kol_of_color == 2 && kol_of_none == 3) {
	    	   weight = 8;
	       } else if (kol_of_color == 2 && kol_of_none == 2) {
	    	   weight = 3;
	       }*/
           weight = WT[kol_of_color][kol_of_none]; 
	       return weight;
     }
 
      boolean bad_color(EColor color, int i, int j) {
         if (i >=0 && i < 9 && j >=0 && j < 9) {
        	 EColor mcolor = MyplaneCopy[i][j];
        	 if (mcolor != EColor.RAINBOW && mcolor != EColor.NONE && mcolor != color) {
        		 return false;
        	 }
         }
         return true;
      }
      
      boolean same_color(EColor color, int i, int j) {
          if (i >=0 && i < 9 && j >=0 && j < 9) {
         	 EColor mcolor = MyplaneCopy[i][j];
         	 if (mcolor == EColor.RAINBOW || mcolor == color) {
         		 return true;
         	 }
          }
          return false;
       }
    	  
      boolean is_good_space(EColor color, int curri, int currj, coordinate from, coordinate direction) {
         EColor ncolor = LinesApplication.EColor.NONE;
    	 // if there are balls which can be move to square by replacement than
         // this is good situation
         if(Replacement_support && direction.i != 0 && direction.j != 0 ) {
             if (same_color(color,curri+1,currj) || same_color(color,curri-1,currj) ||
            	 same_color(color,curri,currj+1) || same_color(color,curri,currj-1))
            	 return true;
         } 
         if (MyplaneCopy[curri][currj] == LinesApplication.EColor.NONE ){ 
             // if all squares around contains different colors this is bad situation
             if (direction.i != 0 && direction.j != 0 &&
                 bad_color(color, curri+1, currj) && bad_color(color, curri-1,currj) &&
            	 bad_color(color, curri, currj+1) && bad_color(color,curri,currj-1)) {
        		 return false;
        	 }
             else if (direction.i == 0 && 
            	 bad_color(color,curri-1,currj) && bad_color(color,curri+1,currj)) {
            	 return false;
             }
             else if (direction.j == 0 && 
                	 bad_color(color,curri,currj-1) && bad_color(color,curri,currj+1)) {
                	 return false;
                 }
        	 else {
    		     return true;
        	 }
    	 }
         else
        	return false;
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
	    	    	   	
	    	for(LinesApplication.EColor color:LinesApplication.EColor.values()) {
	    	
	    		if (color == LinesApplication.EColor.NONE  || 
	    			color == LinesApplication.EColor.RAINBOW	)
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
	    	       if(endi >9 || endj >9 || endi < 0 || endj < 0 ) {
	    	    	   break;
	    	       } 
	    	       else {
	    	           while((nexti != endi) && ( nextj != endj)) {
	    	                if(MyplaneCopy[nexti][nextj] == color || 
	    	                   MyplaneCopy[nexti][nextj] == LinesApplication.EColor.RAINBOW) {
	    	                  kol_of_color++;
	    	                }  
	    	                else if (is_good_space(color, nexti, nextj, from, direction)) { 
	    	                //if(MyplaneCopy[nexti][nextj] == LinesApplication.EColor.NONE) { 
	    	                   //if (there_is_candidate(color, from, nexti, nextj , direction) )
	    	                	  kol_of_none++;
	    	                }
	    	                nexti=nexti+direction.i;
	    	                nextj=nextj+direction.j;
	    	           }
	    	       }
	    	       cost = cost + Weight(kol_of_color, kol_of_none); 
	    	       i+= direction.i;
	    	       j+= direction.j;
	    	    } while(true);
	    	    //color++; 
	    	} 
	    	
	    	return cost;
	 }
	
	 public int Estimate_Position(coordinate from, coordinate to) {
	       int cost =0;
	 
	       LinesApplication.EColor color1 = MyplaneCopy[from.i][from.j];
	       LinesApplication.EColor color2 = MyplaneCopy[to.i][to.j];
	       MyplaneCopy[from.i][from.j] = color2;
	       MyplaneCopy[to.i][to.j]=color1;
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
	       MyplaneCopy[from.i][from.j] = color1;
	       MyplaneCopy[to.i][to.j]=color2;
	       
	       return cost;
	    }
	 
	   public void clear_visited() {
	       for(int i=0; i<9; i++){
	    	  for(int j=0; j<9; j++) {
	    		  visited[i][j] = false;
	    	  }
	       }
	    }
	 
	 public boolean find_path(coordinate from, coordinate to) {
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
	    	      !visited[myi][myj] && Myplane[myi][myj] == LinesApplication.EColor.NONE) {
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
	    
	    private void check_direction(EColor mycolor,coordinate ball_pos, int di, int dj){
	    	//LinesApplication.EColor mycolor = Myplane[ball_pos.i][ball_pos.j];
	        //if (mycolor == EColor.RAINBOW) { 
	        //}
	        	
	    	int i = ball_pos.i;
	        int j = ball_pos.j;
	    	
	    	while(true) {
	    	  i += di;
	    	  j += dj;
	    	  if(i>=0 && i<9 && j>=0 && j<9 && (mycolor == Myplane[i][j] 
	    		 || Myplane[i][j] == EColor.RAINBOW ) ) {
	    		  all_balls[balls_in_lines].i = i;
	    		  all_balls[balls_in_lines].j = j;
	    		  balls_in_lines++;
	    	  }
	    	  else {
	    		  return;
	    	  }
	    	}
	    }
	    
	    
	    private boolean check_for_lines_1(EColor mycolor, int x, int y) {
	        coordinate ball_pos = new coordinate(x,y);
		    	//LinesApplication.EColor mycolor = Myplane[ball_pos.i][ball_pos.j];
		    	int prev = balls_in_lines;
		    	int save_balls=balls_in_lines;
		        
		        //Log.d("lines", "check_for_lines:" + Integer.toString(Freeroom) + " " + Integer.toString(ball_pos.i) + " " + Integer.toString(ball_pos.j));
		        //ScoreView.setText(Integer.toString(Score) + "  ");
		        if(mycolor == LinesApplication.EColor.NONE) {
		        	Log.d("lines", "Ups");
		        	return false;
		        }
		        
		        //balls_in_lines=0;
		 		all_balls[balls_in_lines].i = ball_pos.i;
				all_balls[balls_in_lines].j = ball_pos.j;
				balls_in_lines++;
		        check_direction(mycolor,ball_pos,1,0);
		        check_direction(mycolor,ball_pos,-1,0);
		        if(balls_in_lines - prev < 5) { 
		        	balls_in_lines = prev;
		        }
		        else {
		        	if (balls_in_lines - prev == 9) {
		        		number_of_lines++;
		        	}
		        	number_of_lines++;
		        	//Log.d("lines", "1" + " " + Integer.toString(balls_in_lines));      	
		        }
		        prev = balls_in_lines;
		        balls_in_lines++;
		        check_direction(mycolor,ball_pos,1,1);
		        check_direction(mycolor,ball_pos,-1,-1);
		        if(balls_in_lines - prev < 5) { 
		        	balls_in_lines = prev;
		        }
		        else {
		        	if (balls_in_lines - prev == 9) {
		        		number_of_lines++;
		        	}
		        	//Log.d("lines", "2"+ " " + Integer.toString(balls_in_lines));  
		        	number_of_lines++;
		        }
		        prev = balls_in_lines;
		        balls_in_lines++; 
		        check_direction(mycolor,ball_pos,0,1);
		        check_direction(mycolor,ball_pos,0,-1);
		        if(balls_in_lines - prev < 5) { 
		        	balls_in_lines = prev;
		        }
		        else {
		        	if (balls_in_lines - prev == 9) {
		        		number_of_lines++;
		        	}
		        	//Log.d("lines", "3"+ " " + Integer.toString(balls_in_lines));  
		        	number_of_lines++;
		        }
		        prev = balls_in_lines;
		        balls_in_lines++;
		        check_direction(mycolor,ball_pos,1,-1);
		        check_direction(mycolor,ball_pos,-1,1);
		        if(balls_in_lines - prev < 5) {
		        	balls_in_lines = prev;
		        }
		        else {
		        	if (balls_in_lines - prev == 9) {
		        		number_of_lines++;
		        	}
		        	//Log.d("lines", "4"+ " " + Integer.toString(balls_in_lines));
		        	number_of_lines++;
		        }
                if(balls_in_lines-save_balls >= 5){ 
                	return true;
                }
                else {
                	balls_in_lines=save_balls;
		            return false;
                }
	    }
	    
	    public boolean check_for_lines(EColor mycolor, int x, int y) {
	        coordinate ball_pos = new coordinate(x,y);
	    	//LinesApplication.EColor mycolor = Myplane[ball_pos.i][ball_pos.j];
	    	int prev = 1;
	    	int save_balls=0;
	    	int save_num=0;
	        boolean ret = false;
	        
	        number_of_lines=0;
	        balls_in_lines=0;
	        
	        //Log.d("lines", "check_for_lines:" + Integer.toString(Freeroom) + " " + Integer.toString(ball_pos.i) + " " + Integer.toString(ball_pos.j));
	        //ScoreView.setText(Integer.toString(Score) + "  ");
	        if(mycolor == LinesApplication.EColor.NONE) {
	        	Log.d("lines", "Ups");
	        	return false;
	        }
	        if (mycolor == EColor.RAINBOW) {
	            for (EColor color: EColor.values()) {
	            	boolean mret;
	            	save_balls=balls_in_lines;
	            	save_num=number_of_lines;
	            	if (color != EColor.NONE && color != EColor.RAINBOW) {
	            		mret=check_for_lines_1(color,x,y);
	            		if (mret) {
	            			ret = true;
	            			number_of_lines=save_num+number_of_lines;
	            			balls_in_lines=save_balls+balls_in_lines;
	            		}
	            		else {
	            			balls_in_lines=save_balls;
	            			number_of_lines=save_num;
	            		}
	            	}
	            }
	        }
	        else {
	            ret = check_for_lines_1(mycolor,x,y);
	        } 	            
	        if(balls_in_lines >= 5) {
	            for(int i=0; i < balls_in_lines; i++) {
	            	//Log.d("lines", "all balls:"+ " " + Integer.toString(all_balls[i].i) + " " + Integer.toString(all_balls[i].j));
	            	Myplane[all_balls[i].i][all_balls[i].j] = LinesApplication.EColor.NONE;
	            	Freeroom++;
	            }
	        }
	        
	        //Log.d("lines", "Score: " + Integer.toString(number_of_lines)+" "+Integer.toString(balls_in_lines));
	        Score=Score+2*(number_of_lines*balls_in_lines);
            //Log.d("lines", "Score: " + Integer.toString(Score));
            return ret;
	    }
	 
	 
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub

	}
	   public void PlainCopy() {
	    	for(int i=0; i < 9; i++) {
	    		for(int j=0; j < 9; j++ ) {
	    			MyplaneCopy[i][j] = Myplane[i][j];
	    		}
	    	}
	    }
	   
	   
	  public int Find_Best_Movement() {
	       //LinesApplication.coordinate best_from = new LinesApplication.coordinate(0,0);
	       //LinesApplication.coordinate best_to = new LinesApplication.coordinate(0,0);
	       int best_cost = 0;
	       int cost = 0;
	       
	       Log.d("lines", "Find_Best_Movement start");     
	       //if (Movement_in_Progress)
	    //	   return;
	       // for working with copy of Myplane
	       PlainCopy();        
	                
	       for (int i=0; i<9; i++) {
	    	   for (int j=0; j<9; j++) {
	    		   if (Myplane[i][j] != LinesApplication.EColor.NONE) {
	    			   for (int k=0; k<9; k++) {
	    				   for (int m=0; m<9; m++) {
	    					  if(Replacement_support &&
	    						 ((i - k == 1 || k - i == 1) && j == m) ||
	    					     ((j - m == 1 || m - j == 1) && i == k)) { 
    							   cost = Estimate_Position(new LinesApplication.coordinate(i,j),new LinesApplication.coordinate(k,m));
    							   if (cost > 0)
    							       //Log.d("lines", "Find_Best_Movement " + Integer.toString(i)+":"+Integer.toString(j)+"->"+Integer.toString(k)+":"+Integer.toString(m)+" " + Integer.toString(cost));
    							   if (cost > best_cost) {
    								   best_cost = cost;
    								   best_from.i = i;
    								   best_from.j = j;
    								   best_to.i = k;
    								   best_to.j = m;
    							   } 
	    					   }
	    					   else { 
	    					      if(Myplane[k][m] == LinesApplication.EColor.NONE) {
	    						      clear_visited();
	    						      currstep=0;
	    						      if (find_path(new LinesApplication.coordinate(i,j),new LinesApplication.coordinate(k,m))) {
	    							      cost = Estimate_Position(new LinesApplication.coordinate(i,j),new LinesApplication.coordinate(k,m));
	    							      if (cost > 0)
	    							          //Log.d("lines", "Find_Best_Movement " + Integer.toString(i)+":"+Integer.toString(j)+"->"+Integer.toString(k)+":"+Integer.toString(m)+" " + Integer.toString(cost));
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
	       }
	       
	       return best_cost;
	       
	       /*if( best_cost > 0) {
	 	      Log.d("lines", "Find_Best_Movement "+Integer.toString(best_from.i)+" "+
	               Integer.toString(best_from.j)+" "+Integer.toString(best_to.i)+" "+Integer.toString(best_to.j) ); 
	 	      // These values are used by run(). Should be defined. 
	 	      active = best_from;
	 	      active_color = Myplane[best_from.i][best_from.j];
	 	      to = best_to;
	 	      saveStatus();
	 	      moveBall(best_from,best_to);
	       }*/
	  }
	  
	  public void saveStatus() {
	       for(int i=0; i< balls_count; i++) {
	    	   lastballs[i] = newballs[i];
	       }
	       redo.i = to.i; redo.j = to.j;
	       before.i = active.i; before.j = active.j;
	       redo_is_active = true;
	    }
	  
	  public void moveBall(LinesApplication.coordinate from, LinesApplication.coordinate to) {
	      LinesApplication.EColor mcol = Myplane[from.i][from.j];
	      //Log.d("lines", "moveBall Freeroom:" + Integer.toString(Freeroom)+ " " + Integer.toString(mcol.int_EColor()));
	      clear_visited();
	      pathlength = 1;
	      currstep = 0;
	      //ScoreView.setText(Integer.toString(Score++)+ "  "); 
	      if(Replacement_support &&
					 ((from.i - to.i == 1 || to.i - from.i == 1) && from.j - to.j == 0) ||
				     ((from.j - to.j == 1 || to.j - from.j == 1) && from.i - to.i == 0)) {
	    	  Replacement_in_Progress = true;
	    	  currstep=0;
	    	  int mycost = 0;
	    	  mycost = Estimate_Position(from,to);
	    	  Log.d("lines", "Score of movement : " + Integer.toString(mycost));
	      }
	      else {
	    	if(find_path(from,to)) {
	    	   int mycost = 0;
	    	   mycost = Estimate_Position(from,to);
	    	   Log.d("lines", "Score of movement : " + Integer.toString(mycost));
	           Movement_in_Progress = true;
	    	   //Log.d("lines", "moveBall pathlength:" + Integer.toString(pathlength));
	    	   currstep=0;
	       	   //for(int i=0; i<pathlength;i++) {
	    		   //Log.d("lines", "Path:" + Integer.toString(i) + " " + Integer.toString(Path[i].i) + " " + Integer.toString(Path[i].j)); 
	    	   //}
	        }
	      } 	    	
	      myHandler.post(myRunnable);
      }
	  
	  public void replaceBalls() {
	      Replacement_in_Progress = true;
  	      myHandler.post(myRunnable);
      }	 
	  
	  
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("lines", "LinesApplication onCreate");
	   	prefs = PreferenceManager.getDefaultSharedPreferences(this); 
    	prefs.registerOnSharedPreferenceChangeListener(this);
        active = new coordinate(0,0);
        to = new coordinate(0,0);
        before = new coordinate(0,0);
        redo   = new coordinate(0,0);
        best_from = new coordinate(0,0);
        best_to = new coordinate(0,0);
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
        champPl = MediaPlayer.create(getBaseContext(), R.raw.champ);
        
        for (int i=0; i < 6; i++) {
        	for (int j=0; j < 6; j++ ) {
        		WT[i][j] = 0;
        	}
        }

        // First table :) 
        WT[5][0]=2000; 
        WT[4][1]=300; WT[4][0]=100;
        WT[3][2]=30; WT[3][1] = 20; WT[3][0]=10;  
        WT[2][3]=8; WT[2][2]=3;
        
        	       /*if (kol_of_color == 5 ) {
 	    	   weight = 1000;
 	       } else if (kol_of_color == 4 && kol_of_none == 1) {
 	    	   weight = 300;
 	       } else if (kol_of_color == 4) {
 	    	   weight = 100;
 	       } else if (kol_of_color == 3 && kol_of_none == 2) {
 	           weight = 30 ; 
 	       } else if (kol_of_color == 3 && kol_of_none == 1) {
 	    	   weight = 20;
 	       } else if (kol_of_color == 3) {
 	    	   weight = 10;
 	       } else if (kol_of_color == 2 && kol_of_none == 3) {
 	    	   weight = 8;
 	       } else if (kol_of_color == 2 && kol_of_none == 2) {
 	    	   weight = 3;
 	       }*/
        		
        		
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		champPl.release();
		Log.d("lines", "LinesApplication onTerminate");
	}

}
