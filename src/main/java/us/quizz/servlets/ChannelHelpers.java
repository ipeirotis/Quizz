package us.quizz.servlets;

import us.quizz.entities.User;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class ChannelHelpers {
	
	protected static int CHANNEL_VALIDITY = 60; // in minutes
	
	protected ChannelService channelService =
			ChannelServiceFactory.getChannelService();

	public String generateUserQuizChannelID(User user, String quizID) {
		return generateUserQuizChannelID(user.getUserid(), quizID); 
	}
	
	public String generateUserQuizChannelID(String userId, String quizID) {
		// a little bit risky if user will start the same quizz in two windows ...
		return userId + "_" + quizID; 
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
