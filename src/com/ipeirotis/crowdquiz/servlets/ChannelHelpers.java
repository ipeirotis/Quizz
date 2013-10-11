package com.ipeirotis.crowdquiz.servlets;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.ipeirotis.crowdquiz.entities.User;

public class ChannelHelpers {
	
	protected static int CHANNEL_VALIDITY = 60; // in minutes
	
	protected ChannelService channelService =
			ChannelServiceFactory.getChannelService();

	public String generateUserRelationChannelID(User user, String relation) {
		return generateUserRelationChannelID(user.getUserid(), relation); 
	}
	
	public String generateUserRelationChannelID(String userId, String relation) {
		// a little bit risky if user will start the same quizz in two windows ...
		return userId + "_" + relation; 
	}
	
	/** Returns channel token for JS client
	 */
	public String createChannel(String id){
		return channelService.createChannel(id, CHANNEL_VALIDITY);
	}

	public void sendMessage(String channelKey, String msg){
		channelService.sendMessage(
				new ChannelMessage(channelKey, msg));
	}
}