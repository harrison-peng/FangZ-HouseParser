package com.server.project.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.server.project.api.Point;

public class PointCreator {
	Point point = new Point();

	public static void main(String[] args) throws Exception {
		Gson gson = new Gson();
		PointCreator cc = new PointCreator();

		// create point by road
		Point roadPoint = cc.createPointByRoad("台灣新北市中和區中山路三段");
		System.out.println(gson.toJson(roadPoint));

		// create point by specific address
		// Point coordinate =
		// cc.createPointBySpecificAddress("台北市文山區指南路二段100號");
		// System.out.println(gson.toJson(coordinate));
	}

	public Point createPointByRoad(String address) throws IOException {
		// connect to google map api
		URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=false");
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("user-agent", "Chrome/7.0.517.44");

		InputStream in = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));

		// parse the HTML
		String retVal = "";
		String line = null;
		while ((line = br.readLine()) != null) {
			retVal = retVal + line + "\n";
		}
		Document doc = Jsoup.parse(retVal);

		// find geometry coordinate
		String text = doc.text();
		int locationIndex = text.indexOf("location");
		int latIndex = text.indexOf("lat", locationIndex);
		int latStartIndex = text.indexOf(":", latIndex);
		int latEndIndex = text.indexOf(",", latStartIndex);
		String lat = text.substring(latStartIndex + 2, latEndIndex);
		int lngStartIndex = text.indexOf(":", latEndIndex);
		int lngEndIndex = text.indexOf("}", lngStartIndex);
		String lng = text.substring(lngStartIndex + 2, lngEndIndex);

		point.setLat(Double.valueOf(lat));
		point.setLng(Double.valueOf(lng));

		return point;
	}

	// public Point createPointBySpecificAddress(String address) throws
	// IOException {
	// // connect to google map api
	// URL url = new
	// URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + address
	// + "&sensor=false");
	// System.out.println(url);
	// URLConnection conn = url.openConnection();
	// conn.setRequestProperty("user-agent", "Chrome/7.0.517.44");
	//
	// InputStream in = conn.getInputStream();
	// BufferedReader br = new BufferedReader(new InputStreamReader(in,
	// "utf-8"));
	//
	// // parse the HTML
	// String retVal = "";
	// String line = null;
	// while ((line = br.readLine()) != null) {
	// retVal = retVal + line + "\n";
	// }
	// Document doc = Jsoup.parse(retVal);
	//
	// // find geometry coordinate
	// String text = doc.text();
	// int geoIndex = text.indexOf("geometry");
	// String geometry = text.substring(geoIndex + 27, geoIndex + 70);
	// System.out.println(geometry);
	//
	// // Json to AddressCoordinate
	// Gson gson = new Gson();
	// Point coordinate = gson.fromJson(geometry, Point.class);
	//
	// return coordinate;
	// }
}
