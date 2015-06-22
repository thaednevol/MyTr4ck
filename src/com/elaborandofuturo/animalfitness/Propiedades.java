package com.elaborandofuturo.animalfitness;

import java.util.ArrayList;

import android.location.LocationManager;

public class Propiedades {
	
	public static final String dbname = "AnimalFitness";
	public static String TAG="AnimalFitness";
	public static boolean result;
	public static String message;
	public static int intentos;
	public static ArrayList<String> mascotas_recogidas = new ArrayList<String>();
	public static ArrayList<String> ids_mascotas_recogidas = new ArrayList<String>();
	public static String id="-1";
	public static String url="http://animalfitness.co/";
	public static String PATH;
	public static int db_version=3;
	public static LocationManager locationManager;

}
