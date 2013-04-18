package com.ipeirotis.adcrowdkg;

import java.io.IOException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FreebaseSearch {

	private static String							API_KEY					= "AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc";
	private static JsonParser					jp							= new JsonParser();
	private static HttpTransport			httpTransport		= new NetHttpTransport();
	private static HttpRequestFactory	requestFactory	= httpTransport.createRequestFactory();

	public static String getFreebaseName(String mid) throws IOException {

		GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
		url.put("query", mid);
		url.put("limit", "1");
		url.put("key", FreebaseSearch.API_KEY);

		HttpRequest request = requestFactory.buildGetRequest(url);
		//System.out.println(url.toString());
		HttpResponse httpResponse = request.execute();

		try {
		JsonObject jo = jp.parse(httpResponse.parseAsString()).getAsJsonObject();
		JsonObject result = jo.get("result").getAsJsonArray().get(0).getAsJsonObject();
		// Get the mid and ensure it is the same
		String returnedMid = result.get("mid").getAsString();
		if (!returnedMid.equals(mid))
			return "";
		// Return the name
		String returnedName = result.get("name").getAsString();
		if (returnedName!=null) return returnedName;
		else return "";
		} catch (Exception e) {
			return "";
		}

	}

	public static void main(String[] args) throws IOException {

		String entityName = FreebaseSearch.getFreebaseName("/m/01599");
		System.out.println("Entity Name: " + entityName);
	}

}