package com.elaborandofuturo.animalfitness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.zxing.integration.android.IntentIntegrator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LeerTag extends Activity {

	private Activity act;
	JSONObject jomascota;
	String username;
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in
	// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 5000; // in
	// Milliseconds

	Button btn_leertag_leer;

	Button ib_leertag_ok;

	ImageView iv_leertag_foto;

	TextView tv_leertag_nombre;

	ImageView iv_leertag_volver;

	protected Button retrieveLocationButton;
	private Bitmap bm;
	private String nombre;
	private String id_mascota;
	private int tipo;
	public MyLocationListener mll;
	private String correo;
	ContenedorView con;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.leertag);
	}

	public LeerTag(Activity _act, String _username, String _nombre, Bitmap _bm,
			int _tipo, String _correo, String _id_mascota) {
		this.act = _act;
		this.id_mascota=_id_mascota;
		this.username = _username;
		this.bm = _bm;
		this.nombre = _nombre;
		this.tipo = _tipo;
		this.correo=_correo;
		Propiedades.intentos = 0;

		initcomponents();
	}

	private void initcomponents() {
		Propiedades.result = false;

		con = (ContenedorView) act.findViewById(R.id.contenido);
		ib_leertag_ok = (Button) act.findViewById(R.id.ib_leertag_ok);
		iv_leertag_foto = (ImageView) act.findViewById(R.id.iv_leertag_foto);
		tv_leertag_nombre = (TextView) act.findViewById(R.id.tv_leertag_nombre);
		iv_leertag_volver = (ImageView) act
				.findViewById(R.id.iv_leertag_volver);
		mostrardialogo();
	}

	protected void emergencia() {

		act.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(act, "Lo siento, no tiene mas intentos.",
						Toast.LENGTH_LONG).show();
				Toast.makeText(
						act,
						"La aplicación debería cerrarse, bloquearse, y notificar al DAS",
						Toast.LENGTH_LONG).show();
				Toast.makeText(act,
						", pero estamos desarrollando esa parte ;-)",
						Toast.LENGTH_LONG).show();
			}
		});

	}

	public String contenido() {
		String valor = "";
		try {
			String[] saltos = Propiedades.message.toString().split("\n");
			String contents = "";

			for (int i = 0; i < saltos.length; i++) {
				if (saltos[i].contains("Contents")) {
					contents = saltos[i];
				}
			}

			String[] puntos = contents.split(":");

			String split = "";

			for (int i = 1; i < puntos.length; i++) {
				valor = valor + split + puntos[i];
				split = ":";
			}
		} catch (Exception e) {
			valor = "";
		}

		return valor;
	}

	private void borrarmascota() {
		try {
			Propiedades.mascotas_recogidas = getList();

		} catch (Exception e) {
		}

		username = username.trim();

		Propiedades.mascotas_recogidas.remove(username);

		setList("mascotas");
		
		id_mascota=id_mascota.trim();
		
		Propiedades.ids_mascotas_recogidas.remove(id_mascota);
		
		setList("ids");

		if ((Propiedades.mascotas_recogidas.size() == 1 && Propiedades.mascotas_recogidas
				.get(0).contentEquals(""))
				|| Propiedades.mascotas_recogidas.isEmpty()) {
			detenerrastreo();
		}

		DBSQLiteHelper lsqlh = new DBSQLiteHelper(act, Propiedades.dbname,
				null, Propiedades.db_version);
		SQLiteDatabase db = lsqlh.getWritableDatabase();

		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);

		String id_paseo = settings.getString("id_paseo", "-1");

		try {
			if (db != null) {
				String str = "SELECT id FROM Recogidas WHERE id_mascota='"
						+ id_mascota + "' AND id_paseos='" + id_paseo + "'";

				Console.log(str);

				Cursor c = db.rawQuery(str, null);

				String result;

				if (c.moveToFirst()) {
					do {
						result = c.getString(0);
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						String currentDateandTime = sdf.format(new Date());
						str = "UPDATE Recogidas SET hora_dejada='"
								+ currentDateandTime + "' WHERE id='" + result
								+ "'";
						Console.log(str);

						db.execSQL(str);
					} while (c.moveToNext());
				}

			}
		} catch (Exception e) {
			Console.log(e.toString());
		} finally {
			db.close();
		}
		
		volver();

	}

	private void detenerrastreo() {
		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("rastreo", 0);
		editor.commit();

		turnGPSOff();
	}

	public void turnGPSOff() {
		runOnUiThread(new Runnable() {
				public void run() {
					final LocationManager manager = (LocationManager) act.getSystemService( Context.LOCATION_SERVICE );
					
					 if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
					    	final AlertDialog.Builder builder = new AlertDialog.Builder(act);
					        builder.setMessage("El GPS está activado, ¿desea deshabilitarlo?")
					               .setCancelable(false)
					               .setPositiveButton("Si", new DialogInterface.OnClickListener() {
					                   public void onClick(
					                		   @SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
					                       act.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					                   }
					               })
					               .setNegativeButton("No", new DialogInterface.OnClickListener() {
					                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
					                        dialog.cancel();
					                   }
					               });
					        AlertDialog alert = builder.create();
							alert.show();
					 }
				
					
				}});
		        
	    
	}

	protected void llenardatosxid() {

		ib_leertag_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					enviarcorreo();
					
					//ib_leertag_ok.setImageResource(R.drawable.checkicon);
				} catch (Exception e) {
				}
			}
		});

		act.runOnUiThread(new Runnable() {
			public void run() {
				iv_leertag_foto.setImageBitmap(bm);
				tv_leertag_nombre.setText(nombre);
			}
		});

		try {
			Propiedades.mascotas_recogidas = getList();
		} catch (Exception e) {
		}

		username = username.trim();

		enviarcorreoxjson(id_mascota);

		if (!Propiedades.mascotas_recogidas.contains(username)) {
			Propiedades.mascotas_recogidas.add(username);
		}

		DBSQLiteHelper lsqlh = new DBSQLiteHelper(act, Propiedades.dbname,
				null, Propiedades.db_version);
		SQLiteDatabase db = lsqlh.getWritableDatabase();

		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);

		String id_paseo = settings.getString("id_paseo", "-1");

		try {
			if (db != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String currentDateandTime = sdf.format(new Date());
				String str = "INSERT INTO Recogidas (id_mascota, hora_recogida, id_paseos) VALUES ('"
						+ id_mascota
						+ "','"
						+ currentDateandTime
						+ "',"
						+ id_paseo + ")";
				Console.log(str);

				db.execSQL(str);
			}
		} catch (Exception e) {

		} finally {
			db.close();
		}

		if (settings.getInt("rastreo", -1) != 1) {
			comenzarrastreo();
		}
		if (settings.getInt("alarma", -1) != 1) {
			setearalarmas();
		}

		setList("mascotas");
		
		Propiedades.ids_mascotas_recogidas.add(id_mascota);
		setList("ids");

	}

	private void setearalarmas() {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);

		Calendar th1 = Calendar.getInstance();
		th1.set(year, month, day, 8, 00);
		long whenth1 = th1.getTimeInMillis();

		Calendar th2 = Calendar.getInstance();
		th2.set(year, month, day, 19, 10);
		long whenth2 = th1.getTimeInMillis();

		Calendar cnow = Calendar.getInstance();
		cnow.set(year, month, day, hour, minute);
		long tnow = th1.getTimeInMillis();

		notificacionuno(year, month, day, hour, minute + 1);

		notificaciondos(year, month, day, hour, minute + 2);
	}

	private void notificaciondos(int year, int month, int day, int hour, int i) {
		Log.d("Alarma", "YA " + year + "-" + month + "-" + day + " " + hour
				+ ":" + i);

		Context ctx = act;

		Intent alarmIntent = new Intent(ctx, Notificacion.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0,
				alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) ctx
				.getSystemService(ctx.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, i, 0);

		long when = calendar.getTimeInMillis();

		// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
		// System.currentTimeMillis(), 5000, pendingIntent);
		alarmManager.set(AlarmManager.RTC, when, pendingIntent);
	}

	private void notificacionuno(int year, int month, int day, int hour, int i) {

	}

	@SuppressLint("NewApi")
	private void enviarcorreoxjson(String id) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://animalfitness.co/index.php?option=com_appfitnesssess");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			nameValuePairs.add(new BasicNameValuePair("id", id));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			// According to the JAVA API, InputStream constructor do nothing.
			// So we can't initialize InputStream although it is not an
			// interface
			InputStream inputStream = response.getEntity().getContent();

			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);

			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			StringBuilder stringBuilder = new StringBuilder();

			String bufferedStrChunk = null;

			while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
				stringBuilder.append(bufferedStrChunk);
			}

			JSONObject jo = new JSONObject(stringBuilder.toString());


		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setList(String str) {
		try {
			SharedPreferences settings = act.getSharedPreferences("perfil",
					Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();

			String i = ObjectSerializer
					.serialize(Propiedades.mascotas_recogidas);

			editor.putString(str, i);
			editor.commit();

		} catch (IOException e) {
			Console.log("Error " + e.toString());

		}

	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> getList() {
		ArrayList<String> al;
		try {
			SharedPreferences settings = act.getSharedPreferences("perfil",
					Context.MODE_PRIVATE);
			String hp = settings.getString("mascotas", "");

			if (hp.contentEquals("")) {
				al = new ArrayList<String>();
			} else {
				al = (ArrayList<String>) ObjectSerializer.deserialize(hp);
			}

			Console.log("Al menos try");
		} catch (IOException e) {
			al = new ArrayList<String>();
			Console.log("Cacheado ");
		}

		return al;
	}

	private void comenzarrastreo() {
		mll = new MyLocationListener();

		Propiedades.locationManager = (LocationManager) act
				.getSystemService(Context.LOCATION_SERVICE);

		act.runOnUiThread(new Runnable() {
			public void run() {
				Propiedades.locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						MINIMUM_TIME_BETWEEN_UPDATES,
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, mll);
			}
		});

		SharedPreferences settings = act.getSharedPreferences("perfil",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("rastreo", 1);
		editor.commit();
	}

	private void mostrardialogo() {
		Propiedades.result = false;
		Propiedades.intentos++;

		Log.d(Propiedades.TAG, "" + Propiedades.intentos);

		act.runOnUiThread(new Runnable() {
			public void run() {

				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle("Escanear el código QR de la mascota");
				builder.setMessage("Para poder continuar es necesario escanear el código de la mascota\nTiene "
						+ (4 - Propiedades.intentos) + " intentos");
				builder.setPositiveButton("Escanear",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								Thread t = new Thread() {
									@Override
									public void run() {
										// try {
										while (!Propiedades.result) {
										}
										if (tipo == 0) {
											if (contenido().trim()
													.contentEquals(username)) {
												llenardatosxid();
											}

											else {
												intentos();
											}
										} else {
											if (contenido().trim()
													.contentEquals(username)) {
												borrarmascota();
											}

											else {
												intentos();
											}
										}
									}
								};
								t.start();
								IntentIntegrator integrator = new IntentIntegrator(
										act);
								integrator
										.initiateScan(IntentIntegrator.QR_CODE_TYPES);
								dialog.dismiss();
							}
						});

				builder.setNegativeButton("Cancelar",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								volver();
								dialog.dismiss();
							}

						});

				AlertDialog alertDialog = builder.create();
				alertDialog.show();
				alertDialog.setCancelable(false);

			}
		});
	}

	private void intentos() {
		if (Propiedades.intentos < 3) {
			mostrardialogo();
		} else {
			emergencia();
		}
	}

	private void volver() {
		runOnUiThread(new Runnable() {
		     public void run() {
		    	con.removeAllViews();
		 		con.inicio(act);
		 		con.setTag("inicio");
		 		new Inicio(act);
		    }
		});
		
	}

	private class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			// String message = String.format(
			// "New Location \n Longitude: %1$s \n Latitude: %2$s",
			// location.getLongitude(), location.getLatitude());
			// Toast.makeText(act, message, Toast.LENGTH_LONG).show();
			SharedPreferences settings = act.getSharedPreferences("perfil",
					Context.MODE_PRIVATE);
			int rastreo = settings.getInt("rastreo", 0);
			String id = settings.getString("id_paseo", "-1");
			if (rastreo == 1) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String currentDateandTime = sdf.format(new Date());
				if (setposition(location.getLongitude(),
						location.getLatitude(), currentDateandTime)
						.contentEquals("-1")) {
					DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
							Propiedades.dbname, null, Propiedades.db_version);
					SQLiteDatabase db = lsqlh.getWritableDatabase();

					try {
						if (db != null) {

							String ex = "INSERT INTO Posiciones (id_paseos,longitud, latitud, tiempo, sync) VALUES ('"
									+ id
									+ "','"
									+ location.getLongitude()
									+ "','"
									+ location.getLatitude()
									+ "','"
									+ currentDateandTime + "','0')";
							db.execSQL(ex);
							Console.log(ex);
						}
					} catch (Exception e) {

					} finally {
						db.close();
					}
				} else {
					DBSQLiteHelper lsqlh = new DBSQLiteHelper(act,
							Propiedades.dbname, null, Propiedades.db_version);
					SQLiteDatabase db = lsqlh.getWritableDatabase();

					try {
						if (db != null) {
							String ex = "INSERT INTO Posiciones (id_paseos,longitud, latitud, tiempo, sync) VALUES ('"
									+ id
									+ "','"
									+ location.getLongitude()
									+ "','"
									+ location.getLatitude()
									+ "','"
									+ currentDateandTime + "','1')";
							db.execSQL(ex);
							Console.log(ex);
						}
					} catch (Exception e) {

					} finally {
						db.close();
					}
				}

			}
		}

		public void onStatusChanged(String s, int i, Bundle b) {
			// Toast.makeText(act, "Provider status changed", Toast.LENGTH_LONG)
			// .show();
		}

		public void onProviderDisabled(String s) {
			// Toast.makeText(act,
			// "Provider disabled by the user. GPS turned off",
			// Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String s) {
			// Toast.makeText(act,
			// "Provider enabled by the user. GPS turned on",
			// Toast.LENGTH_LONG).show();
		}

	}

	@SuppressLint("NewApi")
	private String setposition(double longitud, double latitud,
			String currentDateandTime) {
		Console.log(longitud + " " + latitud);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://animalfitness.co/index.php?option=com_appfitnessmap");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("id", Propiedades.id));
			nameValuePairs
					.add(new BasicNameValuePair("latitude", latitud + ""));
			nameValuePairs
					.add(new BasicNameValuePair("longitud", longitud + ""));
			nameValuePairs.add(new BasicNameValuePair("type", "recoge"));
			nameValuePairs.add(new BasicNameValuePair("mascotas",
					Propiedades.ids_mascotas_recogidas.toString()));
			nameValuePairs.add(new BasicNameValuePair("fechahora",
					currentDateandTime));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			// According to the JAVA API, InputStream constructor do nothing.
			// So we can't initialize InputStream although it is not an
			// interface
			InputStream inputStream = response.getEntity().getContent();

			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);

			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			StringBuilder stringBuilder = new StringBuilder();

			String bufferedStrChunk = null;

			while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
				stringBuilder.append(bufferedStrChunk);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			Log.d(Propiedades.TAG, e.toString());
			return "-1";
		}
	}

	@SuppressLint("NewApi")
	private void enviarcorreo() {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);
		try {
			GMailSender sender = new GMailSender("thaednevol@gmail.com",
					" orxwlwbrwhnmrjyd ");
			sender.sendMail("Animal Fitness", "El paseador con id "
					+ Propiedades.id + " ha recogido tu mascota " + nombre,
					correo, correo);
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle("Correo Enviado").setNegativeButton("Aceptar",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							volver();
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		} catch (Exception e) {
			Log.e("SendMail", e.getMessage(), e);
		}
	}
	
	@Override
	public void onBackPressed()
	{
	    volver();  
	}

}
