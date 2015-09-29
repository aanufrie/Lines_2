package com.novo.aanufrie;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RobotService extends Service{
    public LinesApplication myApp;
    private Updater updater;
    private boolean runFlag;
    
    
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.myApp=(LinesApplication)getApplication();
		this.updater = new Updater();
		runFlag=true;
		Log.d("lines","onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		Log.d("lines","RobotService start");
		this.updater.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.updater.interrupt();
		this.runFlag=false;
		this.updater = null;
		Log.d("lines","RobotService destroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class Updater extends Thread {
	   public Updater() {
		   super("RobotService-Updater");
	   }
	   
	   public void run() {
		   RobotService robotService = RobotService.this;
		   while (runFlag){
			  Log.d("lines","RobotService running"); 
			  
			  try {
	        	  if (!myApp.Movement_in_Progress){ 
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
			     Thread.sleep(1000);
			  } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
		   }
	   }
	}
}
