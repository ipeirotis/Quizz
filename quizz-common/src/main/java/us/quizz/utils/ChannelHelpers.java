package us.quizz.utils;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

import us.quizz.entities.User;

public class ChannelHelpers {
  private static int CHANNEL_VALIDITY = 60; // in minutes

  private static ChannelService channelService = ChannelServiceFactory
      .getChannelService();

  public static String generateUserQuizChannelID(User user, String quizID) {
    return generateUserQuizChannelID(user.getUserid(), quizID);
  }

  public static String generateUserQuizChannelID(String userId, String quizID) {
    // a little bit risky if user will start the same quizz in two windows
    // ...
    return userId + "_" + quizID;
  }

  /**
   * Returns channel token for JS client
   */
  public static String createChannel(String id) {
    return channelService.createChannel(id, CHANNEL_VALIDITY);
  }

  public static void sendMessage(String channelKey, String msg) {
    channelService.sendMessage(new ChannelMessage(channelKey, msg));
  }
}
