package us.quizz.repository;

import java.util.ArrayList;
import java.util.List;

import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.User;
import us.quizz.utils.PMF;

import com.google.appengine.api.datastore.Key;

public class BadgeRepository extends BaseRepository<Badge>{
	
	public BadgeRepository() {
		super(Badge.class);
	}
	
	@Override
	protected Key getKey(Badge item) {
		return item.getKey();
	}
	
	public List<Badge> checkForNewBadges(User user, String quizID,
			Integer correctanswers, Integer totalanswers) {
		List<Badge> badges = getBadges(user, quizID, correctanswers,
				totalanswers);
		for (Badge b : badges) {
			BadgeAssignment ba = new BadgeAssignment(user.getUserid(),
					b.getBadgename());
			PMF.singleMakePersistent(ba);
		}
		return badges;
	}

	public List<Badge> getBadges(User user, String quizID,
			Integer numCorrectAnswers, Integer numTotalAnswers) {
		List<Badge> badgelist = new ArrayList<Badge>();
		if (numTotalAnswers.equals(1)) {
			badgelist.add(getBadge("Answered One", "A1"));
		} else if (numTotalAnswers.equals(5)) {
			badgelist.add(getBadge("Answered Five", "A5"));
		} else if (numTotalAnswers.equals(10)) {
			badgelist.add(getBadge("Answered Ten", "A10"));
		} else if (numTotalAnswers.equals(25)) {
			badgelist.add(getBadge("Answered Twenty-Five", "A25"));
		} else if (numTotalAnswers.equals(50)) {
			badgelist.add(getBadge("Answered Fifty", "A50"));
		}

		if (numCorrectAnswers.equals(1)) {
			badgelist.add(getBadge("One Correct", "C1"));
		} else if (numCorrectAnswers.equals(5)) {
			badgelist.add(getBadge("Five Correct", "C5"));
		} else if (numCorrectAnswers.equals(10)) {
			badgelist.add(getBadge("Ten Correct", "C10"));
		} else if (numCorrectAnswers.equals(25)) {
			badgelist.add(getBadge("Twenty-Five Correct", "C25"));
		} else if (numCorrectAnswers.equals(50)) {
			badgelist.add(getBadge("Fifty Correct", "C50"));
		}
		return badgelist;
	}

	public Badge getBadge(String name, String sname) {
		Badge badge = PMF.singleGetObjectById(Badge.class,
				Badge.generateKeyFromID(name));
		if (badge == null) {
			Badge newbadge = new Badge(name, sname);
			PMF.singleMakePersistent(newbadge);
			badge = newbadge;
		}
		return badge;
	}

	public Boolean userHasBadge(User u, Badge b) {
		BadgeAssignment ba = PMF.singleGetObjectById(
				BadgeAssignment.class,
				BadgeAssignment.generateKeyFromUserBadge(u.getUserid(),
						b.getBadgename()));
		return (ba != null);
	}
}
