package com.ipeirotis.crowdquiz.utils;

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

	public static String getFreebaseAttribute(String mid, String freebaseAttribute) throws IOException {

		GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");

		// TODO: Form this using a proper JSON library
		String query = "{\"mid\":\"" + mid + "\", \"" + freebaseAttribute + "\": null, \"name\": null}";

		url.put("query", query);
		url.put("limit", "1");
		url.put("key", FreebaseSearch.API_KEY);

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		String response = httpResponse.parseAsString();

		try {
			JsonObject jo = jp.parse(response).getAsJsonObject();
			JsonObject result = jo.get("result").getAsJsonObject();

			String returnedName = result.get(freebaseAttribute).getAsString();
			if (returnedName != null)
				return returnedName;
			else
				return "";
		} catch (Exception e) {
			return "";
		}

	}

	public static void main(String[] args) throws IOException {

		String name = FreebaseSearch.getFreebaseAttribute("/m/0cyhj_", "name");
		String calories = FreebaseSearch.getFreebaseAttribute("/m/0cyhj_", "/food/food/energy");
		System.out.println(name + "\t" + calories);

	}

}