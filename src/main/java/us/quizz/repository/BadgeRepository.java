package us.quizz.repository;
import java.util.ArrayList;
import java.util.List;

import com.ipeirotis.crowdquiz.entities.Badge;
import com.ipeirotis.crowdquiz.entities.BadgeAssignment;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.utils.PMF;

public class BadgeRepository {
	public static List<Badge> checkForNewBadges(User user, String quizID, String numCorrectAnswers, String numTotalAnswers){
		List<Badge> badges = getBadges(user, quizID, numCorrectAnswers, numTotalAnswers);
		for(Badge b : badges){
			BadgeAssignment ba = new BadgeAssignment(user.getUserid(), b.getBadgename());
			PMF.singleMakePersistent(ba);
		}
		return badges;
	}
	
	public static List<Badge> getBadges(User user, String quizID, String numCorrectAnswers, String numTotalAnswers){
		List<Badge> badgelist = new ArrayList<Badge>();
		if(numTotalAnswers.equals("1")){
			badgelist.add(getBadge("Answered One"));
		} else if(numTotalAnswers.equals("5")){
			badgelist.add(getBadge("Answered Five"));
		} else if(numTotalAnswers.equals("10")){
			badgelist.add(getBadge("Answered Ten"));
		} else if(numTotalAnswers.equals("25")){
			badgelist.add(getBadge("Answered Twenty-Five"));
		} else if(numTotalAnswers.equals("50")){
			badgelist.add(getBadge("Answered Fifty"));
		}
		
		if(numCorrectAnswers.equals("1")){
			badgelist.add(getBadge("One Correct"));
		} else if(numCorrectAnswers.equals("5")){
			badgelist.add(getBadge("Five Correct"));
		} else if(numCorrectAnswers.equals("10")){
			badgelist.add(getBadge("Ten Correct"));
		} else if(numCorrectAnswers.equals("25")){
			badgelist.add(getBadge("Twenty-Five Correct"));
		} else if(numCorrectAnswers.equals("50")){
			badgelist.add(getBadge("Fifty Correct"));
		}
		return badgelist; 
	}
	
	public static Badge getBadge(String name){
		Badge badge = PMF.singleGetObjectById(Badge.class, Badge.generateKeyFromID(name));
		if(badge == null){
			Badge newbadge = new Badge(name);
			PMF.singleMakePersistent(newbadge);
			badge = newbadge;
		}
		return badge;
	}
	
	public static Boolean userHasBadge(User u, Badge b){
		BadgeAssignment ba = PMF.singleGetObjectById(BadgeAssignment.class, BadgeAssignment.generateKeyFromUserBadge(u.getUserid(), b.getBadgename()));
		return (ba != null);
	}
}
