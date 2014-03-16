package us.quizz.di;

import com.google.inject.servlet.ServletModule;

import us.quizz.servlets.DownloadUserAnswers;

public class WebModule extends ServletModule {
  @Override
  protected void configureServlets() {
    super.configureServlets();

    serve("/admin/downloadUserAnswers").with(DownloadUserAnswers.class);
  }
}
