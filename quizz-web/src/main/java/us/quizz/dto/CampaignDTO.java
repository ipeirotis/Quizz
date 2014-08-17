package us.quizz.dto;

import com.google.api.ads.adwords.jaxws.v201406.cm.CampaignStatus;

public class CampaignDTO {
  private String quizID;
  private String name;
  private Integer budget;
  private CampaignStatus status;

  public String getQuizID() {
    return quizID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getBudget() {
    return budget;
  }

  public void setBudget(Integer budget) {
    this.budget = budget;
  }

  public CampaignStatus getStatus() {
    return status;
  }

  public void setStatus(CampaignStatus status) {
    this.status = status;
  }
}
