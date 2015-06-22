package com.elaborandofuturo.animalfitness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class ContenedorView extends LinearLayout {
	public ContenedorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(context);
		
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ContenedorView(Context context, AttributeSet attrs) {
		
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 */
	public ContenedorView(Context context) {
		this(context, null);
	}


	private void initView(Context context) {
		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Log.d(Propiedades.TAG,inflater.toString());
	}
	
	public void login(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.login, this, true);
	}

	public void inicio(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.inicio, this, true);
	}

	public void leertag(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.leertag, this, true);
	}

	/*public void evolucion(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.evolucion, this, true);
	}

	public void reto(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.reto, this, true);
	}

	public void compartir(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.loginfb, this, true);
	}


	
/*	public void info(Activity act) {
		final LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.info, this, true);
	}*/
}
