package com.elaborandofuturo.animalfitness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends Activity {
	
	Activity act;
	ContenedorView con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        act=this;
        initcomponents();
    }


    private void initcomponents() {
    	con = (ContenedorView) this.findViewById(R.id.contenido);
    	SharedPreferences settings = act.getSharedPreferences("perfil", Context.MODE_PRIVATE);
    	String id = settings.getString("id","-1");
    	if (id.contentEquals("-1")){
    		login();
    	}
    	else {
    		Propiedades.id=id;
    		inicio();
    	}
		
	}


    private void login() {
		con.removeAllViews();
		con.login(act);
		con.setTag("login");
		new Login(act);
	}
    
    private void inicio() {
		ContenedorView con;
		con = (ContenedorView) act.findViewById(R.id.contenido);
		con.removeAllViews();
		con.inicio(act);
		con.setTag("inicio");
		new Inicio(act);
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	@Override
	public void onBackPressed()
	{
		ContenedorView con= (ContenedorView) act.findViewById(R.id.contenido);
		
		if (con.getTag().toString().contentEquals("login")){
			finish();
		}
		
		else {
			inicio();
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	    
	    Log.d(Propiedades.TAG,"HOLA "+requestCode+" "+resultCode);
	    
	    if (result != null) {
	      String contents = result.getContents();
	      if (contents != null) {
	    	  Propiedades.result=true;
	    	  Propiedades.message=result.toString();
	      } else {
	    	  Propiedades.result=false;
	      }
	    }
	  }
	
}
