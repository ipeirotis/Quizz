package us.quizz.env;

public enum Env {
  DEV("localhost:8888"), INT("intapp1.appspot.com"), PROD("crowd-power.appspot.com");

  private String domain;

  private Env(String domain) {
    this.domain = domain;
  }

  public String getDomain() {
    return domain;
  }
}
