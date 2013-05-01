package com.ipeirotis.crowdquiz.utils;

import java.io.IOException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
		
		//System.out.println(response);

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


	public static String getFreebaseTopic(String mid, String freebaseAttribute, String freebaseElement) throws IOException {

		GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/topic" + mid);

		url.put("filter", freebaseAttribute);
		url.put("limit", "1");
		
		url.put("key", FreebaseSearch.API_KEY);

		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		String response = httpResponse.parseAsString();
		

		try {
			JsonObject jo = jp.parse(response).getAsJsonObject();
			
			// Gson gson = new GsonBuilder().setPrettyPrinting().create();
			//String json = gson.toJson(jo);
			//System.out.println(json);
			
			// TODO: Have a proper test, instead of relying on Exceptions
			JsonObject property = jo.get("property").getAsJsonObject().get(freebaseAttribute).getAsJsonObject();
				
			// TODO: Examine how to handle returns with multiple values. For now, we get just get the first
			JsonObject value = property.get("values").getAsJsonArray().get(0).getAsJsonObject();
		
			String valuetype = property.get("valuetype").getAsString();
			String result = "";
			switch (valuetype) {
				case "float":
				case "int"  :
				case "bool" : 
				case "datetime": 
				case "uri": 
				case "string":
				case "key":
					// Get the "value"  or text attribute
					result = value.get("text").getAsString(); 
					break;
				case "object":
					// Get the id of the entity or the text
					 result = value.get("text").getAsString();
					break;
				case "compound":
					// Get the "values" thing, and then get the freebase attribute
					JsonObject attributes  = value.get("property").getAsJsonObject().get(freebaseElement).getAsJsonObject();
					JsonObject attribute  = attributes.get("values").getAsJsonArray().get(0).getAsJsonObject();
							
					result = attribute.get("text").getAsString();
					 break;
				default: break;
			}

			
			return result;
			
		} catch (Exception e) {
			//e.printStackTrace();
			return "";
		}

	}

	
	public static void main(String[] args) throws IOException {

		
		
		String name = FreebaseSearch.getFreebaseAttribute("/m/02mjmr", "name");
		String attribute = FreebaseSearch.getFreebaseTopic("/m/02mjmr", "/people/person/spouse_s", "/people/marriage/spouse");
		System.out.println(name + "\t" + attribute);

		name = FreebaseSearch.getFreebaseAttribute("/m/012xdf", "name");
		attribute = FreebaseSearch.getFreebaseTopic("/m/012xdf", "/people/person/spouse_s", "/people/marriage/spouse");
		System.out.println(name + "\t" + attribute);
		
		
		
		
	}

}