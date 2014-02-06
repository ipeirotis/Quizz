package us.quizz.endpoints;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.utils.ServletUtils;

import com.google.api.ads.adwords.jaxws.factory.AdWordsServices;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroup;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupAd;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupAdOperation;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupAdReturnValue;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupAdServiceInterface;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupAdStatus;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupCriterion;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupCriterionOperation;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupCriterionReturnValue;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupCriterionServiceInterface;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupOperation;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupReturnValue;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupServiceInterface;
import com.google.api.ads.adwords.jaxws.v201309.cm.AdGroupStatus;
import com.google.api.ads.adwords.jaxws.v201309.cm.ApiException;
import com.google.api.ads.adwords.jaxws.v201309.cm.ApiException_Exception;
import com.google.api.ads.adwords.jaxws.v201309.cm.BiddableAdGroupCriterion;
import com.google.api.ads.adwords.jaxws.v201309.cm.BiddingStrategyConfiguration;
import com.google.api.ads.adwords.jaxws.v201309.cm.BiddingStrategyType;
import com.google.api.ads.adwords.jaxws.v201309.cm.Budget;
import com.google.api.ads.adwords.jaxws.v201309.cm.BudgetBudgetDeliveryMethod;
import com.google.api.ads.adwords.jaxws.v201309.cm.BudgetBudgetPeriod;
import com.google.api.ads.adwords.jaxws.v201309.cm.Campaign;
import com.google.api.ads.adwords.jaxws.v201309.cm.CampaignOperation;
import com.google.api.ads.adwords.jaxws.v201309.cm.CampaignPage;
import com.google.api.ads.adwords.jaxws.v201309.cm.CampaignReturnValue;
import com.google.api.ads.adwords.jaxws.v201309.cm.CampaignServiceInterface;
import com.google.api.ads.adwords.jaxws.v201309.cm.CampaignStatus;
import com.google.api.ads.adwords.jaxws.v201309.cm.CpcBid;
import com.google.api.ads.adwords.jaxws.v201309.cm.Keyword;
import com.google.api.ads.adwords.jaxws.v201309.cm.KeywordMatchSetting;
import com.google.api.ads.adwords.jaxws.v201309.cm.KeywordMatchType;
import com.google.api.ads.adwords.jaxws.v201309.cm.Money;
import com.google.api.ads.adwords.jaxws.v201309.cm.NetworkSetting;
import com.google.api.ads.adwords.jaxws.v201309.cm.Operator;
import com.google.api.ads.adwords.jaxws.v201309.cm.Selector;
import com.google.api.ads.adwords.jaxws.v201309.cm.TextAd;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class CampaignManagementEndpoint {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CampaignManagementEndpoint.class.getName());

	private AdWordsServices	adWordsServices;
	private AdWordsSession session;
	
	@Inject
	private QuizRepository quizRepository;

	@ApiMethod(name = "adwords", path="adwords")
	public void adwords(HttpServletRequest req) throws Exception {
		Credential credential = new OfflineCredentials.Builder()
		.forApi(com.google.api.ads.common.lib.auth.OfflineCredentials.Api.ADWORDS)
		.fromFile()
		.build()
		.generateCredential(); 

		session = new AdWordsSession.Builder()
	        .fromFile()
	        .withOAuth2Credential(credential)
	        .build();
	
		adWordsServices = new AdWordsServices();
		
		ServletUtils.ensureParameters(req, "quizID", "budget", "cpcbid", "keywords", "adheadline",
				"adline1", "adline2");
		addCampaign(req);
		addAdGroup(req);
	}
	
	@SuppressWarnings("unused")
	private String test() throws Exception {
		// Get the CampaignService.
		CampaignServiceInterface campaignService =
				adWordsServices.get(session, CampaignServiceInterface.class);

		// Create selector
		Selector selector = new Selector();
		selector.getFields().addAll(Lists.newArrayList("Id", "Name"));

		// Get all campaigns
		CampaignPage page = campaignService.get(selector);

		StringBuilder sb = new StringBuilder();

		// Display campaigns
		if (page.getEntries() != null) {
			for (Campaign campaign : page.getEntries()) {
				sb.append("<h4>Campaign with name \"" + campaign.getName() + "\" and id \""
						+ campaign.getId() + "\" was found.</h4>");
			}
		} else {
			sb.append("No campaigns were found.");
		}
		return sb.toString();
	}
	
	private void addCampaign(HttpServletRequest req) throws Exception{
		String quizID = req.getParameter("quizID").trim();
		Integer dailyBudget = Integer.parseInt(req.getParameter("budget"));

		String campaignName = quizID;
		Campaign campaign = createCampaign(campaignName, dailyBudget);
		Long campaignId = publishCampaign(campaign);
		
		Quiz q = quizRepository.getQuiz(quizID);
		q.setCampaignid(campaignId);
		quizRepository.storeQuiz(q);
	}
	
	private void addAdGroup(HttpServletRequest req) throws Exception{		
		String quizID = req.getParameter("quizID").trim();
		String cpcbid = req.getParameter("cpcbid").trim();
		String keywords = req.getParameter("keywords").trim();
		String adheadline = req.getParameter("adheadline").trim();
		String adline1 = req.getParameter("adline1").trim();
		String adline2 = req.getParameter("adline2").trim();

		Quiz q = quizRepository.getQuiz(quizID);
		Long campaignId = q.getCampaignid();
		
		
		if (campaignId == null) {
			// All quizzes (should) have a corresponding ad campaign.
			// If we get a null, we just put the task back in the queue
			// and run the call again.
			// This happens either when the Quiz object has not yet 
			// persisted in the datastore the campaignId
			Queue queueAdgroup = QueueFactory.getQueue("adgroup");
			long delay = 10; // in seconds
			long etaMillis = System.currentTimeMillis() + delay * 1000L;
			TaskOptions taskOptions = Builder.withUrl("/campaignManagament")
					.param("quizID", quizID)
					.param("cpcbid", cpcbid)
					.param("keywords", keywords)
					.param("adheadline", adheadline)
					.param("adline1", adline1)
					.param("adline2", adline2)
					.method(TaskOptions.Method.POST)
					.etaMillis(etaMillis);
			//if (questionID != null) {
			//	taskOptions.param("questionID", questionID);
			//}
			queueAdgroup.add(taskOptions);
			return;
		}

		// TODO: REFQQ - midName ... used freebase name
		String midName = "default";
		String adGroupName = midName;

		AdGroup adgroup = createAdgroup(adGroupName, campaignId, Double.parseDouble(cpcbid));
		Long adgroupId = publishAdgroup(adgroup);

		String[] keyword = keywords.split(",");
		for (String k : keyword) {
			String bidKeyword = " " + k.trim().toLowerCase();
			addKeyword(bidKeyword.replaceAll("[^A-Za-z0-9 ]", " "), adgroupId);
		}

		String displayURL = "http://www.quizz.us";
		String targetURL = "http://www.quizz.us/startQuiz?quizID=" + URLEncoder.encode(quizID, "UTF-8");
		AdGroupAd ad = createTextAd(adheadline, adline1, adline2, displayURL, targetURL, adgroupId);
		Long textAdId = publishTextAd(ad);

		//if (questionID != null) { 
		//	Question eq = QuizQuestionRepository.getQuizQuestion(questionID);	
		//	eq.setAdTextId(textAdId);
		//	eq.setAdGroupId(adgroupId);
		//	QuizQuestionRepository.storeQuizQuestion(eq);
		//}
	}
 

	public Campaign createCampaign(String campaignName, Integer dailyBudget)
			throws Exception {

		// Create campaign.
		Campaign campaign = new Campaign();
		campaign.setName(campaignName + "#" + System.currentTimeMillis());
		campaign.setStatus(CampaignStatus.ACTIVE);

		BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
		biddingStrategyConfiguration
				.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC);
		campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

		// Create a budget, which can be shared by multiple campaigns.
		Budget budget = new Budget();
		budget.setName("#" + System.currentTimeMillis());
		Money budgetAmount = new Money();
		Long dailyAmount = dailyBudget * 1000000L;
		budgetAmount.setMicroAmount(dailyAmount);
		budget.setAmount(budgetAmount);
		budget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD);
		budget.setPeriod(BudgetBudgetPeriod.DAILY);
		campaign.setBudget(budget);

		// Set required keyword match to TRUE to allow for "the broadening of
		// exact and phrase
		// keyword matches for this campaign to include small variations such as
		// plurals,
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

		// Add the campaign
		return campaign;

	}

	/**
	 * @param campaign
	 * @throws RemoteException
	 * @throws ApiException_Exception
	 * @throws ApiException
	 */
	public Long publishCampaign(Campaign campaign) throws RemoteException,
			ApiException_Exception {

		// Get the CampaignService.
		CampaignServiceInterface campaignService = adWordsServices.get(session,
				CampaignServiceInterface.class);

		CampaignOperation operation = new CampaignOperation();
		operation.setOperand(campaign);
		operation.setOperator(Operator.ADD);
		List<CampaignOperation> operations = new ArrayList<CampaignOperation>();
		operations.add(operation);

		CampaignReturnValue result = campaignService.mutate(operations);
		if (result != null && result.getValue() != null) {
			for (Campaign campaignResult : result.getValue()) {
				System.out.println("Campaign with name \""
						+ campaignResult.getName() + "\" and id \""
						+ campaignResult.getId() + "\" was added.");
				return campaignResult.getId();
			}
		} else {
			System.out.println("No campaigns were added.");

		}
		return null;
	}

	/**
	 * @param dailyAmount
	 * @return
	 * @throws RemoteException
	 * @throws ApiException_Exception
	 * @throws ApiException
	 */
	/*
	 * private Budget getBudget(int dailyBudget) throws RemoteException,
	 * ApiException_Exception {
	 * 
	 * 
	 * 
	 * // Get the BudgetService. BudgetServiceInterface budgetService =
	 * adWordsServices.get(session, BudgetServiceInterface.class);
	 * 
	 * // Create a budget, which can be shared by multiple campaigns. Budget
	 * budget = new Budget(); budget.setName("#" + System.currentTimeMillis());
	 * Money budgetAmount = new Money();
	 * budgetAmount.setMicroAmount(dailyAmount); budget.setAmount(budgetAmount);
	 * budget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD);
	 * budget.setPeriod(BudgetBudgetPeriod.DAILY);
	 * 
	 * BudgetOperation budgetOperation = new BudgetOperation();
	 * budgetOperation.setOperand(budget);
	 * budgetOperation.setOperator(Operator.ADD); List<BudgetOperation>
	 * budgetoperations = new ArrayList<BudgetOperation>();
	 * budgetoperations.add(budgetOperation);
	 * 
	 * // Only the budgetId should be sent, all other fields will be ignored by
	 * CampaignService. //Long budgetId =
	 * budgetService.mutate(budgetoperations).getValue(0).getBudgetId(); Long
	 * budgetId =
	 * budgetService.mutate(budgetoperations).getValue().get(0).getBudgetId();
	 * budget.setBudgetId(budgetId); return budget; }
	 */

	public AdGroup createAdgroup(String adGroupName, Long campaignId,
			Double bidAmount) {

		// Create ad group.
		AdGroup adGroup = new AdGroup();
		adGroup.setName(adGroupName + "#" + System.currentTimeMillis());
		adGroup.setStatus(AdGroupStatus.ENABLED);
		adGroup.setCampaignId(campaignId);

		// Create ad group bid.
		BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
		CpcBid bid = new CpcBid();
		Money m = new Money();
		m.setMicroAmount(Math.round(bidAmount * 1000000L));
		bid.setBid(m);

		biddingStrategyConfiguration.getBids().add(bid);
		adGroup.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

		return adGroup;
	}

	public Long publishAdgroup(AdGroup adGroup) throws Exception {

		// Create operations.
		AdGroupOperation operation = new AdGroupOperation();
		operation.setOperand(adGroup);
		operation.setOperator(Operator.ADD);

		List<AdGroupOperation> operations = new ArrayList<AdGroupOperation>();
		operations.add(operation);

		// Add ad groups.
		AdGroupServiceInterface adGroupService = adWordsServices.get(session,
				AdGroupServiceInterface.class);

		AdGroupReturnValue result = adGroupService.mutate(operations);

		// Display new ad groups.
		for (AdGroup adGroupResult : result.getValue()) {
			System.out.println("Ad group with name \""
					+ adGroupResult.getName() + "\" and id \""
					+ adGroupResult.getId() + "\" was added.");
			return adGroupResult.getId();
		}
		return null;
	}

	public AdGroupAd createTextAd(String adHeadline, String adDescr1,
			String adDescr2, String displayURL, String targetURL, Long adGroupId) {

		// Create text ad.
		TextAd textAd = new TextAd();
		textAd.setHeadline(adHeadline);
		textAd.setDescription1(adDescr1);
		textAd.setDescription2(adDescr2);
		textAd.setDisplayUrl(displayURL);
		textAd.setUrl(targetURL);

		// Create ad group ad.
		AdGroupAd textAdGroupAd = new AdGroupAd();
		textAdGroupAd.setAdGroupId(adGroupId);
		textAdGroupAd.setAd(textAd);

		// You can optionally provide these field(s).
		textAdGroupAd.setStatus(AdGroupAdStatus.ENABLED);

		return textAdGroupAd;
	}

	public Long publishTextAd(AdGroupAd ad) throws Exception {

		// Get the AdGroupAdService.
		AdGroupAdServiceInterface adGroupAdService = adWordsServices.get(
				session, AdGroupAdServiceInterface.class);

		// Create operations.
		AdGroupAdOperation textAdGroupAdOperation = new AdGroupAdOperation();
		textAdGroupAdOperation.setOperand(ad);
		textAdGroupAdOperation.setOperator(Operator.ADD);

		List<AdGroupAdOperation> operations = new ArrayList<AdGroupAdOperation>();
		operations.add(textAdGroupAdOperation);

		// Add ads.
		AdGroupAdReturnValue result = adGroupAdService.mutate(operations);

		// Display ads.
		for (AdGroupAd adGroupAdResult : result.getValue()) {
			System.out.println("Ad with id  \""
					+ adGroupAdResult.getAd().getId() + "\"" + " and type \""
					+ adGroupAdResult.getAd().getAdType() + "\" was added.");
			return adGroupAdResult.getAd().getId();
		}
		return null;
	}

	public void addKeyword(String word, Long adGroupId) throws Exception {

		// Create keywords.
		Keyword keyword = new Keyword();
		keyword.setText(word);
		keyword.setMatchType(KeywordMatchType.EXACT);

		// Create biddable ad group criterion.
		BiddableAdGroupCriterion keywordBiddableAdGroupCriterion = new BiddableAdGroupCriterion();
		keywordBiddableAdGroupCriterion.setAdGroupId(adGroupId);
		keywordBiddableAdGroupCriterion.setCriterion(keyword);

		AdGroupCriterionOperation keywordAdGroupCriterionOperation = new AdGroupCriterionOperation();
		keywordAdGroupCriterionOperation
				.setOperand(keywordBiddableAdGroupCriterion);
		keywordAdGroupCriterionOperation.setOperator(Operator.ADD);

		List<AdGroupCriterionOperation> operations = new ArrayList<AdGroupCriterionOperation>();
		operations.add(keywordAdGroupCriterionOperation);

		AdGroupCriterionServiceInterface adGroupCriterionService = adWordsServices
				.get(session, AdGroupCriterionServiceInterface.class);
		// Add keywords.
		AdGroupCriterionReturnValue result = adGroupCriterionService
				.mutate(operations);

		// Display results.
		for (AdGroupCriterion adGroupCriterionResult : result.getValue()) {
			System.out.println("Keyword ad group criterion with ad group id \""
					+ adGroupCriterionResult.getAdGroupId()
					+ "\", criterion id \""
					+ adGroupCriterionResult.getCriterion().getId()
					+ "\", text \""
					+ ((Keyword) adGroupCriterionResult.getCriterion())
							.getText()
					+ "\" and match type \""
					+ ((Keyword) adGroupCriterionResult.getCriterion())
							.getMatchType() + "\" was added.");
		}
	}

	private void runTest() {

		try {
			String campaignName = "Test";
			Integer dailyBudget = 10;
			Campaign campaign = createCampaign(campaignName,
					dailyBudget);
			Long campaignId = publishCampaign(campaign);

			String adGroupName = "Test-Adgroup";
			Double cpcbid = 0.05;
			AdGroup adgroup = createAdgroup(adGroupName, campaignId,
					cpcbid);
			Long adgroupId = publishAdgroup(adgroup);
			for (int i = 1; i <= 5; i++) {
				String bidKeyword = "Ipeirotis " + i;
				addKeyword(bidKeyword, adgroupId);
			}

			String adHeadline = "This is a headline";
			String adDescr1 = "This is the first line";
			String adDescr2 = "This is the second line";
			String displayURL = "http://crowd-power.appspot.com";
			String targetURL = "http://crowd-power.appspot.com";
			AdGroupAd ad = createTextAd(adHeadline, adDescr1, adDescr2,
					displayURL, targetURL, adgroupId);
			// Long textAdId = service.publishTextAd(ad);
			publishTextAd(ad);
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Exception in Testing.", e);
			e.printStackTrace();
		}
	}

}