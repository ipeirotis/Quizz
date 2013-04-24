package com.ipeirotis.crowdquiz.ads;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroup;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupAd;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupAdOperation;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupAdReturnValue;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupAdServiceInterface;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupAdStatus;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupCriterion;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupCriterionOperation;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupCriterionReturnValue;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupCriterionServiceInterface;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupOperation;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupReturnValue;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupServiceInterface;
import com.google.api.ads.adwords.axis.v201302.cm.AdGroupStatus;
import com.google.api.ads.adwords.axis.v201302.cm.ApiException;
import com.google.api.ads.adwords.axis.v201302.cm.BiddableAdGroupCriterion;
import com.google.api.ads.adwords.axis.v201302.cm.BiddingStrategyConfiguration;
import com.google.api.ads.adwords.axis.v201302.cm.BiddingStrategyType;
import com.google.api.ads.adwords.axis.v201302.cm.Bids;
import com.google.api.ads.adwords.axis.v201302.cm.Budget;
import com.google.api.ads.adwords.axis.v201302.cm.BudgetBudgetDeliveryMethod;
import com.google.api.ads.adwords.axis.v201302.cm.BudgetBudgetPeriod;
import com.google.api.ads.adwords.axis.v201302.cm.BudgetOperation;
import com.google.api.ads.adwords.axis.v201302.cm.BudgetServiceInterface;
import com.google.api.ads.adwords.axis.v201302.cm.Campaign;
import com.google.api.ads.adwords.axis.v201302.cm.CampaignOperation;
import com.google.api.ads.adwords.axis.v201302.cm.CampaignReturnValue;
import com.google.api.ads.adwords.axis.v201302.cm.CampaignServiceInterface;
import com.google.api.ads.adwords.axis.v201302.cm.CampaignStatus;
import com.google.api.ads.adwords.axis.v201302.cm.CpcBid;
import com.google.api.ads.adwords.axis.v201302.cm.Keyword;
import com.google.api.ads.adwords.axis.v201302.cm.KeywordMatchSetting;
import com.google.api.ads.adwords.axis.v201302.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201302.cm.Money;
import com.google.api.ads.adwords.axis.v201302.cm.NetworkSetting;
import com.google.api.ads.adwords.axis.v201302.cm.Operator;
import com.google.api.ads.adwords.axis.v201302.cm.Setting;
import com.google.api.ads.adwords.axis.v201302.cm.TextAd;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.ClientLoginTokens;

@SuppressWarnings("serial")
public class AddCampaign extends HttpServlet {

	final static Logger			logger	= Logger.getLogger("java.util.logging.config.file");

	private AdWordsServices	adWordsServices;
	private AdWordsSession	session;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {
			AddCampaign service = new AddCampaign();

			String campaignName = "Test";
			Integer dailyBudget = 10;
			Campaign campaign = service.createCampaign(campaignName, dailyBudget);
			Long campaignId = service.publishCampaign(campaign);

			String adGroupName = "Test-Adgroup";
			Integer groupBudget = 10;
			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, groupBudget);
			Long adgroupId = service.publishAdgroup(adgroup);
			for (int i = 1; i <= 5; i++) {
				String bidKeyword = "Ipeirotis " + i;
				service.addKeyword(bidKeyword, adgroupId);
			}

			String adHeadline = "This is a headline";
			String adDescr1 = "This is the first line";
			String adDescr2 = "This is the second line";
			String displayURL = "http://crowd-power.appspot.com";
			String targetURL = "http://crowd-power.appspot.com";
			AdGroupAd ad = service.createTextAd(adHeadline, adDescr1, adDescr2, displayURL, targetURL, adgroupId);
			Long textAdId = service.publishTextAd(ad);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in Testing.", e);
			e.printStackTrace();
		}
	}

	public AddCampaign() throws Exception {

		Configuration config = new PropertiesConfiguration();

		config.setProperty("api.adwords.developerToken", "xh5fcw0gDCw10sCVmBiMeg");
		config.setProperty("api.adwords.clientCustomerId", "681-322-0047");
		config.setProperty("api.adwords.email", "ipeirotis@gmail.com");
		config.setProperty("api.adwords.password", "armani12");

		config.setProperty("api.adwords.environment", "production");
		config.setProperty("api.adwords.soapModule", "com.google.api.ads.adwords.axis.AdWordsAxisModule ");
		config.setProperty("api.adwords.userAgent", "PanosIpeirotis-Test");

		// api.adwords.isPartialFailure=false
		// api.adwords.returnMoneyInMicros=true

		// Get a ClientLogin AuthToken.
		String clientLoginToken = new ClientLoginTokens.Builder().forApi(ClientLoginTokens.Api.ADWORDS).from(config)
				.build().requestToken();

		// Construct an AdWordsSession.
		this.session = new AdWordsSession.Builder().from(config).withClientLoginToken(clientLoginToken).build();

		this.adWordsServices = new AdWordsServices();
	}

	public Campaign createCampaign(String campaignName, Integer dailyBudget) throws Exception {

		// Create campaign.
		Campaign campaign = new Campaign();
		campaign.setName(campaignName + "#" + System.currentTimeMillis());
		campaign.setStatus(CampaignStatus.PAUSED);

		BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
		biddingStrategyConfiguration.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC);
		campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

		Budget budget = getBudget(dailyBudget);
		campaign.setBudget(budget);

		// Set required keyword match to TRUE to allow for "the broadening of exact and phrase
		// keyword matches for this campaign to include small variations such as plurals,
		// common misspellings, diacriticals and acronyms"
		KeywordMatchSetting keywordMatch = new KeywordMatchSetting();
		keywordMatch.setOptIn(Boolean.TRUE);
		campaign.setSettings(new Setting[] { keywordMatch });

		// Set the campaign network options to Search and Search Network.
		NetworkSetting networkSetting = new NetworkSetting();
		networkSetting.setTargetGoogleSearch(true);
		networkSetting.setTargetSearchNetwork(false);
		networkSetting.setTargetContentNetwork(false);
		networkSetting.setTargetPartnerSearchNetwork(false);
		campaign.setNetworkSetting(networkSetting);

		// Add the campaign
		return campaign;

		/*
		 * biddingStrategyConfiguration.setBiddingStrategyType(BiddingStrategyType.CONVERSION_OPTIMIZER);
		 * campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration);
		 * CampaignOperation updateoperation = new CampaignOperation();
		 * updateoperation.setOperand(campaign);
		 * updateoperation.setOperator(Operator.SET);
		 * operations = new CampaignOperation[] {updateoperation};
		 * result = campaignService.mutate(operations);
		 * 
		 * if (result != null && result.getValue() != null) {
		 * for (Campaign campaignResult : result.getValue()) {
		 * System.out.println("Campaign with name \""
		 * + campaignResult.getName() + "\" and id \""
		 * + campaignResult.getId() + "\" was added.");
		 * }
		 * } else {
		 * System.out.println("No campaigns were added.");
		 * }
		 */

	}

	/**
	 * @param campaign
	 * @throws RemoteException
	 * @throws ApiException
	 */
	public Long publishCampaign(Campaign campaign) throws RemoteException, ApiException {

		// Get the CampaignService.
		CampaignServiceInterface campaignService = adWordsServices.get(session, CampaignServiceInterface.class);

		CampaignOperation operation = new CampaignOperation();
		operation.setOperand(campaign);
		operation.setOperator(Operator.ADD);
		CampaignOperation[] operations = new CampaignOperation[] { operation };
		CampaignReturnValue result = campaignService.mutate(operations);
		if (result != null && result.getValue() != null) {
			for (Campaign campaignResult : result.getValue()) {
				System.out.println("Campaign with name \"" + campaignResult.getName() + "\" and id \"" + campaignResult.getId()
						+ "\" was added.");
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
	 * @throws ApiException
	 */
	private Budget getBudget(int dailyBudget) throws RemoteException, ApiException {

		Long dailyAmount = dailyBudget * 1000000L;

		// Get the BudgetService.
		BudgetServiceInterface budgetService = adWordsServices.get(session, BudgetServiceInterface.class);

		// Create a budget, which can be shared by multiple campaigns.
		Budget budget = new Budget();
		budget.setName("#" + System.currentTimeMillis());
		Money budgetAmount = new Money();
		budgetAmount.setMicroAmount(dailyAmount);
		budget.setAmount(budgetAmount);
		budget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD);
		budget.setPeriod(BudgetBudgetPeriod.DAILY);

		BudgetOperation budgetOperation = new BudgetOperation();
		budgetOperation.setOperand(budget);
		budgetOperation.setOperator(Operator.ADD);

		// Only the budgetId should be sent, all other fields will be ignored by CampaignService.
		Long budgetId = budgetService.mutate(new BudgetOperation[] { budgetOperation }).getValue(0).getBudgetId();
		budget.setBudgetId(budgetId);
		return budget;
	}

	public AdGroup createAdgroup(String adGroupName, Long campaignId, Integer bidAmount) {

		// Create ad group.
		AdGroup adGroup = new AdGroup();
		adGroup.setName(adGroupName + "#" + System.currentTimeMillis());
		adGroup.setStatus(AdGroupStatus.ENABLED);
		adGroup.setCampaignId(campaignId);

		// Create ad group bid.
		BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
		CpcBid bid = new CpcBid();
		bid.setBid(new Money(null, bidAmount * 1000000L));

		biddingStrategyConfiguration.setBids(new Bids[] { bid });
		adGroup.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

		return adGroup;
	}

	public Long publishAdgroup(AdGroup adGroup) throws Exception {

		// Create operations.
		AdGroupOperation operation = new AdGroupOperation();
		operation.setOperand(adGroup);
		operation.setOperator(Operator.ADD);

		AdGroupOperation[] operations = new AdGroupOperation[] { operation };

		// Add ad groups.
		AdGroupServiceInterface adGroupService = adWordsServices.get(session, AdGroupServiceInterface.class);

		AdGroupReturnValue result = adGroupService.mutate(operations);

		// Display new ad groups.
		for (AdGroup adGroupResult : result.getValue()) {
			System.out.println("Ad group with name \"" + adGroupResult.getName() + "\" and id \"" + adGroupResult.getId()
					+ "\" was added.");
			return adGroupResult.getId();
		}
		return null;
	}

	public AdGroupAd createTextAd(String adHeadline, String adDescr1, String adDescr2, String displayURL,
			String targetURL, Long adGroupId) {

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
		textAdGroupAd.setStatus(AdGroupAdStatus.PAUSED);

		return textAdGroupAd;
	}

	public Long publishTextAd(AdGroupAd ad) throws Exception {

		// Get the AdGroupAdService.
		AdGroupAdServiceInterface adGroupAdService = adWordsServices.get(session, AdGroupAdServiceInterface.class);

		// Create operations.
		AdGroupAdOperation textAdGroupAdOperation = new AdGroupAdOperation();
		textAdGroupAdOperation.setOperand(ad);
		textAdGroupAdOperation.setOperator(Operator.ADD);

		AdGroupAdOperation[] operations = new AdGroupAdOperation[] { textAdGroupAdOperation };

		// Add ads.
		AdGroupAdReturnValue result = adGroupAdService.mutate(operations);

		// Display ads.
		for (AdGroupAd adGroupAdResult : result.getValue()) {
			System.out.println("Ad with id  \"" + adGroupAdResult.getAd().getId() + "\"" + " and type \""
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
		keywordAdGroupCriterionOperation.setOperand(keywordBiddableAdGroupCriterion);
		keywordAdGroupCriterionOperation.setOperator(Operator.ADD);

		AdGroupCriterionOperation[] operations = new AdGroupCriterionOperation[] { keywordAdGroupCriterionOperation };

		AdGroupCriterionServiceInterface adGroupCriterionService = adWordsServices.get(session,
				AdGroupCriterionServiceInterface.class);
		// Add keywords.
		AdGroupCriterionReturnValue result = adGroupCriterionService.mutate(operations);

		// Display results.
		for (AdGroupCriterion adGroupCriterionResult : result.getValue()) {
			System.out.println("Keyword ad group criterion with ad group id \"" + adGroupCriterionResult.getAdGroupId()
					+ "\", criterion id \"" + adGroupCriterionResult.getCriterion().getId() + "\", text \""
					+ ((Keyword) adGroupCriterionResult.getCriterion()).getText() + "\" and match type \""
					+ ((Keyword) adGroupCriterionResult.getCriterion()).getMatchType() + "\" was added.");
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		try {
			AddCampaign service = new AddCampaign();

			String campaignName = "Test";
			Integer dailyBudget = 10;
			Campaign campaign = service.createCampaign(campaignName, dailyBudget);
			Long campaignId = service.publishCampaign(campaign);

			String adGroupName = "Test-Adgroup";
			Integer groupBudget = 10;
			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, groupBudget);
			Long adgroupId = service.publishAdgroup(adgroup);
			for (int i = 1; i <= 5; i++) {
				String bidKeyword = "Ipeirotis " + i;
				service.addKeyword(bidKeyword, adgroupId);
			}

			String adHeadline = "This is a headline";
			String adDescr1 = "This is the first line";
			String adDescr2 = "This is the second line";
			String displayURL = "http://crowd-power.appspot.com";
			String targetURL = "http://crowd-power.appspot.com";
			AdGroupAd ad = service.createTextAd(adHeadline, adDescr1, adDescr2, displayURL, targetURL, adgroupId);
			Long textAdId = service.publishTextAd(ad);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in Testing.", e);
			e.printStackTrace();
		}

	}

}
