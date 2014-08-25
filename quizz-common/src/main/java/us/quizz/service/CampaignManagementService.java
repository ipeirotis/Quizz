package us.quizz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.ads.adwords.jaxws.factory.AdWordsServices;
import com.google.api.ads.adwords.jaxws.v201406.cm.Ad;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroup;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupAd;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupAdOperation;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupAdServiceInterface;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupAdStatus;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupCriterionOperation;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupCriterionServiceInterface;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupOperation;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupServiceInterface;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdGroupStatus;
import com.google.api.ads.adwords.jaxws.v201406.cm.AdvertisingChannelType;
import com.google.api.ads.adwords.jaxws.v201406.cm.BiddableAdGroupCriterion;
import com.google.api.ads.adwords.jaxws.v201406.cm.BiddingStrategyConfiguration;
import com.google.api.ads.adwords.jaxws.v201406.cm.BiddingStrategyType;
import com.google.api.ads.adwords.jaxws.v201406.cm.Budget;
import com.google.api.ads.adwords.jaxws.v201406.cm.BudgetBudgetDeliveryMethod;
import com.google.api.ads.adwords.jaxws.v201406.cm.BudgetBudgetPeriod;
import com.google.api.ads.adwords.jaxws.v201406.cm.BudgetOperation;
import com.google.api.ads.adwords.jaxws.v201406.cm.BudgetServiceInterface;
import com.google.api.ads.adwords.jaxws.v201406.cm.Campaign;
import com.google.api.ads.adwords.jaxws.v201406.cm.CampaignOperation;
import com.google.api.ads.adwords.jaxws.v201406.cm.CampaignPage;
import com.google.api.ads.adwords.jaxws.v201406.cm.CampaignServiceInterface;
import com.google.api.ads.adwords.jaxws.v201406.cm.CampaignStatus;
import com.google.api.ads.adwords.jaxws.v201406.cm.CpcBid;
import com.google.api.ads.adwords.jaxws.v201406.cm.Keyword;
import com.google.api.ads.adwords.jaxws.v201406.cm.KeywordMatchSetting;
import com.google.api.ads.adwords.jaxws.v201406.cm.KeywordMatchType;
import com.google.api.ads.adwords.jaxws.v201406.cm.Money;
import com.google.api.ads.adwords.jaxws.v201406.cm.NetworkSetting;
import com.google.api.ads.adwords.jaxws.v201406.cm.Operator;
import com.google.api.ads.adwords.jaxws.v201406.cm.Selector;
import com.google.api.ads.adwords.jaxws.v201406.cm.TextAd;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.client.auth.oauth2.Credential;
import com.google.common.collect.Lists;

public class CampaignManagementService {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(CampaignManagementService.class.getName());
  private AdWordsServices adWordsServices = new AdWordsServices();

  private AdWordsSession getAdWordsSession() throws Exception{
    Credential credential =
        new OfflineCredentials.Builder()
            .forApi(com.google.api.ads.common.lib.auth.OfflineCredentials.Api.ADWORDS)
            .withClientSecrets(System.getProperty("API_ADWORDS_CLIENT_ID"), 
                System.getProperty("API_ADWORDS_CLIENT_SECRET"))
            .withRefreshToken(System.getProperty("API_ADWORDS_REFRESH_TOKEN"))
            .build()
            .generateCredential(); 

    AdWordsSession session =
        new AdWordsSession.Builder()
            .withClientCustomerId(System.getProperty("API_ADWORDS_CLIENT_CUSTOMER_ID"))
            .withDeveloperToken(System.getProperty("API_ADWORDS_DEVELOPER_TOKEN"))
            .withUserAgent(System.getProperty("API_ADWORDS_USER_AGENT"))
            .withOAuth2Credential(credential)
            .build();
    
    return session;
  }

  private CampaignServiceInterface getCampaignServiceInterface() throws Exception {
    return adWordsServices.get(getAdWordsSession(), CampaignServiceInterface.class);
  }

  private BudgetServiceInterface getBudgetServiceInterface() throws Exception {
    return adWordsServices.get(getAdWordsSession(), BudgetServiceInterface.class);
  }  

  private AdGroupServiceInterface getAdGroupServiceInterface() throws Exception {
    return adWordsServices.get(getAdWordsSession(), AdGroupServiceInterface.class);
  }

  private AdGroupAdServiceInterface getAdGroupAdServiceInterface() throws Exception {
    return adWordsServices.get(getAdWordsSession(), AdGroupAdServiceInterface.class);
  }

  private AdGroupCriterionServiceInterface getAdGroupCriterionServiceInterface() throws Exception {
    return adWordsServices.get(getAdWordsSession(), AdGroupCriterionServiceInterface.class);
  }

  public List<Campaign> listCampaigns() throws Exception {
    // Get the CampaignService
    CampaignServiceInterface campaignService = getCampaignServiceInterface();

    // Create selector
    Selector selector = new Selector();
    selector.getFields().addAll(Lists.newArrayList("Id", "Name", "Status"));

    // Get all campaigns
    CampaignPage page = campaignService.get(selector);

    return page.getEntries();
  }

  public Campaign createCampaign(String name, Integer dailyBudget, CampaignStatus status) throws Exception {
    BudgetServiceInterface budgetService = getBudgetServiceInterface();
    // Create a budget, which can be shared by multiple campaigns
    Budget budget = new Budget();
    budget.setName(name + "_budget");
    Money budgetAmount = new Money();
    Long dailyAmount = dailyBudget * 1000000L;
    budgetAmount.setMicroAmount(dailyAmount);
    budget.setAmount(budgetAmount);
    budget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD);
    budget.setPeriod(BudgetBudgetPeriod.DAILY);

    BudgetOperation budgetOperation = new BudgetOperation();
    budgetOperation.setOperand(budget);
    budgetOperation.setOperator(Operator.ADD);
    List<BudgetOperation> budgetOperations = new ArrayList<BudgetOperation>();
    budgetOperations.add(budgetOperation);

    // Add the budget
    Long budgetId =
        budgetService.mutate(budgetOperations).getValue().get(0).getBudgetId();

    // Create campaign
    Campaign campaign = new Campaign();
    campaign.setName(name);
    campaign.setStatus(status);

    BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
    biddingStrategyConfiguration
        .setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC);
    campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

    // Set required keyword match to TRUE to allow for "the broadening of exact and phrase
    // keyword matches for this campaign to include small variations such as plurals,
    // common misspellings, diacriticals and acronyms"
    KeywordMatchSetting keywordMatch = new KeywordMatchSetting();
    keywordMatch.setOptIn(Boolean.TRUE);
    campaign.getSettings().add(keywordMatch);

    // Set the campaign network options to Search only
    NetworkSetting networkSetting = new NetworkSetting();
    networkSetting.setTargetGoogleSearch(true);
    networkSetting.setTargetSearchNetwork(true);
    networkSetting.setTargetContentNetwork(true);
    networkSetting.setTargetPartnerSearchNetwork(false);
    campaign.setNetworkSetting(networkSetting);
    campaign.setAdvertisingChannelType(AdvertisingChannelType.SEARCH);
    
    budget.setBudgetId(budgetId);
    campaign.setBudget(budget);

    CampaignServiceInterface campaignService = getCampaignServiceInterface();

    CampaignOperation operation = new CampaignOperation();
    operation.setOperand(campaign);
    operation.setOperator(Operator.ADD);
    List<CampaignOperation> operations = new ArrayList<CampaignOperation>();
    operations.add(operation);

    return campaignService.mutate(operations).getValue().get(0);
  }
  
  public void enableCampaign(Long campaignId) throws Exception {
    Campaign campaign = new Campaign();
    campaign.setId(campaignId);
    campaign.setStatus(CampaignStatus.ENABLED);

    CampaignOperation operation = new CampaignOperation();
    operation.setOperand(campaign);
    operation.setOperator(Operator.SET);
    List<CampaignOperation> operations = new ArrayList<CampaignOperation>();

    CampaignServiceInterface campaignService = getCampaignServiceInterface();
    campaignService.mutate(operations);
  }
  
  public void pauseCampaign(Long campaignId) throws Exception {
    Campaign campaign = new Campaign();
    campaign.setId(campaignId);
    campaign.setStatus(CampaignStatus.PAUSED);

    CampaignOperation operation = new CampaignOperation();
    operation.setOperand(campaign);
    operation.setOperator(Operator.SET);
    List<CampaignOperation> operations = new ArrayList<CampaignOperation>();

    CampaignServiceInterface campaignService = getCampaignServiceInterface();
    campaignService.mutate(operations);
  }

  public AdGroup createAdGroup(String adGroupName, Long campaignId, Double bidAmount) throws Exception {
    // Create ad group
    AdGroup adGroup = new AdGroup();
    adGroup.setName(adGroupName);
    adGroup.setStatus(AdGroupStatus.ENABLED);
    adGroup.setCampaignId(campaignId);

    // Create ad group bid
    BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
    CpcBid bid = new CpcBid();
    Money m = new Money();
    m.setMicroAmount(Math.round(bidAmount * 1000000L));
    bid.setBid(m);

    biddingStrategyConfiguration.getBids().add(bid);
    adGroup.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

    // Create operations
    AdGroupOperation operation = new AdGroupOperation();
    operation.setOperand(adGroup);
    operation.setOperator(Operator.ADD);

    List<AdGroupOperation> operations = new ArrayList<AdGroupOperation>();
    operations.add(operation);

    AdGroupServiceInterface adGroupService = getAdGroupServiceInterface();

    return adGroupService.mutate(operations).getValue().get(0);
  }

  public void pauseAd(Long adGroupId, Long adId) throws Exception {
    AdGroupAdServiceInterface adGroupAdService = getAdGroupAdServiceInterface();

    Ad ad = new Ad();
    ad.setId(adId);

    AdGroupAd adGroupAd = new AdGroupAd();
    adGroupAd.setAdGroupId(adGroupId);
    adGroupAd.setAd(ad);
    adGroupAd.setStatus(AdGroupAdStatus.PAUSED);

    AdGroupAdOperation operation = new AdGroupAdOperation();
    operation.setOperand(adGroupAd);
    operation.setOperator(Operator.SET);

    List<AdGroupAdOperation> operations = new ArrayList<AdGroupAdOperation>();
    operations.add(operation);
    adGroupAdService.mutate(operations);
  }

  public AdGroupAd createTextAd(String adHeadline, String description1,
      String description2, String displayURL, String targetURL, Long adGroupId) throws Exception {
    // Create text ad
    TextAd textAd = new TextAd();
    textAd.setHeadline(adHeadline);
    textAd.setDescription1(description1);
    textAd.setDescription2(description2);
    textAd.setDisplayUrl(displayURL);
    textAd.setUrl(targetURL);

    // Create ad group ad
    AdGroupAd textAdGroupAd = new AdGroupAd();
    textAdGroupAd.setAdGroupId(adGroupId);
    textAdGroupAd.setAd(textAd);
    textAdGroupAd.setStatus(AdGroupAdStatus.ENABLED);

    AdGroupAdServiceInterface adGroupAdService = getAdGroupAdServiceInterface();

    // Create operations
    AdGroupAdOperation operation = new AdGroupAdOperation();
    operation.setOperand(textAdGroupAd);
    operation.setOperator(Operator.ADD);

    List<AdGroupAdOperation> operations = new ArrayList<AdGroupAdOperation>();
    operations.add(operation);

    return adGroupAdService.mutate(operations).getValue().get(0);
  }

  public void addKeyword(String word, Long adGroupId) throws Exception {
    // Create keywords
    Keyword keyword = new Keyword();
    keyword.setText(word);
    keyword.setMatchType(KeywordMatchType.EXACT);

    // Create biddable ad group criterion
    BiddableAdGroupCriterion keywordBiddableAdGroupCriterion = new BiddableAdGroupCriterion();
    keywordBiddableAdGroupCriterion.setAdGroupId(adGroupId);
    keywordBiddableAdGroupCriterion.setCriterion(keyword);

    AdGroupCriterionOperation keywordAdGroupCriterionOperation = new AdGroupCriterionOperation();
    keywordAdGroupCriterionOperation.setOperand(keywordBiddableAdGroupCriterion);
    keywordAdGroupCriterionOperation.setOperator(Operator.ADD);

    List<AdGroupCriterionOperation> operations = new ArrayList<AdGroupCriterionOperation>();
    operations.add(keywordAdGroupCriterionOperation);

    AdGroupCriterionServiceInterface adGroupCriterionService = getAdGroupCriterionServiceInterface();

    adGroupCriterionService.mutate(operations);
  }
}
