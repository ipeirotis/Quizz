package us.quizz.di;

import us.quizz.servlets.DownloadUserAnswers;

import com.google.inject.servlet.ServletModule;

public class WebModule extends ServletModule {
  @Override
  protected void configureServlets() {
    super.configureServlets();

    serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
  }
}
