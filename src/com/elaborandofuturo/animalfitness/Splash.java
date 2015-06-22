package com.elaborandofuturo.animalfitness;

import com.elaborandofuturo.animalfitness.ActivitySwitcher.AnimationFinishedListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;


public class Splash extends Activity {

	private final static int DURATION = 1500;
    private final static float DEPTH = 400.0f;
	private LinearLayout ll_splash;

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.splash);
	    
	    ll_splash = (LinearLayout)findViewById(R.id.ll_container);
	    AnimationFinishedListener al = new AnimationFinishedListener() {
			@Override
			public void onAnimationFinished() {
				ll_splash.clearAnimation();
				
				animatedStartActivity();
			}
		};
		apply3DRotation(0, 360, false, ll_splash, getWindowManager(), al);
	    
	  }

private static void apply3DRotation(float fromDegree, float toDegree,
        boolean reverse, View container, WindowManager windowManager,
        final AnimationFinishedListener listener) {
    Display display = windowManager.getDefaultDisplay();
    final float centerX = display.getWidth() / 2.0f;
    final float centerY = display.getHeight() / 2.0f;

    final Rotate3dAnimation a = new Rotate3dAnimation(fromDegree, toDegree,
            centerX, centerY, DEPTH, reverse);
    a.reset();
    a.setDuration(DURATION);
    a.setFillAfter(true);
    a.setInterpolator(new AccelerateInterpolator());
    if (listener != null) {
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.onAnimationFinished();
                
            }
        });
    }
    container.clearAnimation();
    container.startAnimation(a);
}

@SuppressLint("NewApi") 
private void animatedStartActivity() {
	
	Animation fadeOut = new AlphaAnimation(1, 0);
	fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
	fadeOut.setStartOffset(1000);
	fadeOut.setDuration(1000);

	
	fadeOut.setAnimationListener(new AnimationListener() {
        public void onAnimationEnd(Animation animation) {
        	ll_splash.setVisibility(View.GONE);
        	startactivity();
        }

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
	    }});

	ll_splash.startAnimation(fadeOut);
	
}

protected void startactivity() {
	
	
	Intent mainIntent = new Intent().setClass(Splash.this, MainActivity.class);
 	startActivity(mainIntent);
 	finish();
 	
	
		}
}