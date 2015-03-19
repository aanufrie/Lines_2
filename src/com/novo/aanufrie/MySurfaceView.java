package com.novo.aanufrie;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MySurfaceView extends SurfaceView {

	public MySurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}
	
	public MySurfaceView(Context context,AttributeSet attrs, int DefStyle){
		super(context,attrs,DefStyle);
		}
	
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		    int x = getMeasuredWidth();
		    int y = getMeasuredHeight();

		    int max = Math.max(x,y);
		    int min = Math.min(x, y);
		    if( x>y ) {
		    	setMeasuredDimension(min+100, min);
			    //Log.d("lines", "Hor Size: " + Integer.toString(min));      
		    }   
		    else {
		    	setMeasuredDimension(min, min+100);
			    //Log.d("lines", "Ver Size: " + Integer.toString(min));
		    }/*Log.d("lines", "Init size: " + Integer.toString(min)+ " " + Integer.toString(max));
		    int diff = max - min;
		    if(diff < 100) {
		       min = min - 100 + diff;	
		    }*/
		    
		  }
	
}