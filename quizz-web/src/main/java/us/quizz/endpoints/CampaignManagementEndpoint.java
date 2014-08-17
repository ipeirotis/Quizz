package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import us.quizz.dto.CampaignDTO;
import us.quizz.entities.Quiz;
import us.quizz.service.CampaignManagementService;
import us.quizz.service.QuizService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import com.google.api.ads.adwords.jaxws.v201406.cm.Campaign;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;

@Api(name = "adwords",
    description = "The API for Quizz.us",
    version = "v1",
    clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
             Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
             API_EXPLORER_CLIENT_ID},
    scopes = {Constants.EMAIL_SCOPE})
public class CampaignManagementEndpoint {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(CampaignManagementEndpoint.class.getName());

  private QuizService quizService;
  private CampaignManagementService campaignManagementService;

  @Inject
  public CampaignManagementEndpoint(QuizService quizService, CampaignManagementService campaignManagementService) {
    this.quizService = quizService;
    this.campaignManagementService = campaignManagementService;
  }

  @ApiMethod(name = "campaign.create", path="campaign/create")
  public void createCampaign(CampaignDTO campaignDTO, User user) throws Exception {
    Security.verifyAuthenticatedUser(user);
    Campaign campaign = campaignManagementService.createCampaign(campaignDTO.getName(), 
        campaignDTO.getBudget(), campaignDTO.getStatus());

    Quiz q = quizService.get(campaignDTO.getQuizID());
    q.setCampaignid(campaign.getId());
    quizService.save(q);
  }

  @ApiMethod(name = "campaign.list", path="campaign/list")
  public List<Campaign> listCampaigns(User user) throws Exception {
    Security.verifyAuthenticatedUser(user);
    return campaignManagementService.listCampaigns();
  }

  @ApiMethod(name = "campaign.enable", path="campaign/enable")
  public void enableCampaign(@Named("campaignId") Long campaignId, User user) throws Exception {
    Security.verifyAuthenticatedUser(user);
    campaignManagementService.enableCampaign(campaignId);
  }

  @ApiMethod(name = "campaign.pause", path="campaign/pause")
  public void pauseCampaign(@Named("campaignId") Long campaignId, User user) throws Exception {
    Security.verifyAuthenticatedUser(user);
    campaignManagementService.pauseCampaign(campaignId);
  }
}
