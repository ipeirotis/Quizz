package us.quizz.ofy;

import us.quizz.entities.Quiz;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
  static {
    register(Quiz.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }

  public static void register(Class<?> clazz) {
    factory().register(clazz);
  }
}
