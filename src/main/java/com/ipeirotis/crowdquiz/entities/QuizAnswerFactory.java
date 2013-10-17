package com.ipeirotis.crowdquiz.entities;

import com.google.gson.JsonObject;

public class QuizAnswerFactory {
	
	protected Answer getFreebaseAnswer(String mid, String name){
		Answer answer = new Answer();
		answer.setText(name);
		JsonObject jo = new JsonObject();
		jo.addProperty("mid", mid);
		return answer;
	}
	
//	public QuizAnswer getFreebaseGold(String mid, String name)

}
