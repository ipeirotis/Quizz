package com.ipeirotis.crowdquiz.ads;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.api.ads.adwords.jaxws.factory.AdWordsServices;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroup;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAd;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAdOperation;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAdReturnValue;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAdServiceInterface;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAdStatus;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupCriterion;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupCriterionOperation;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupCriterionReturnValue;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupCriterionServiceInterface;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupOperation;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupReturnValue;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupServiceInterface;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupStatus;
import com.google.api.ads.adwords.jaxws.v201302.cm.ApiException;
import com.google.api.ads.adwords.jaxws.v201302.cm.ApiException_Exception;
import com.google.api.ads.adwords.jaxws.v201302.cm.BiddableAdGroupCriterion;
import com.google.api.ads.adwords.jaxws.v201302.cm.BiddingStrategyConfiguration;
import com.google.api.ads.adwords.jaxws.v201302.cm.BiddingStrategyType;
import com.google.api.ads.adwords.jaxws.v201302.cm.Budget;
import com.google.api.ads.adwords.jaxws.v201302.cm.BudgetBudgetDeliveryMethod;
import com.google.api.ads.adwords.jaxws.v201302.cm.BudgetBudgetPeriod;
import com.google.api.ads.adwords.jaxws.v201302.cm.Campaign;
import com.google.api.ads.adwords.jaxws.v201302.cm.CampaignOperation;
import com.google.api.ads.adwords.jaxws.v201302.cm.CampaignReturnValue;
import com.google.api.ads.adwords.jaxws.v201302.cm.CampaignServiceInterface;
import com.google.api.ads.adwords.jaxws.v201302.cm.CampaignStatus;
import com.google.api.ads.adwords.jaxws.v201302.cm.CpcBid;
import com.google.api.ads.adwords.jaxws.v201302.cm.Keyword;
import com.google.api.ads.adwords.jaxws.v201302.cm.KeywordMatchSetting;
import com.google.api.ads.adwords.jaxws.v201302.cm.KeywordMatchType;
import com.google.api.ads.adwords.jaxws.v201302.cm.Money;
import com.google.api.ads.adwords.jaxws.v201302.cm.NetworkSetting;
import com.google.api.ads.adwords.jaxws.v201302.cm.Operator;
import com.google.api.ads.adwords.jaxws.v201302.cm.TextAd;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.ClientLoginTokens;
import com.google.api.ads.common.lib.exception.ValidationException;

@SuppressWarnings("serial")
public class CampaignManagement extends HttpServlet {

	private static AdWordsServices	adWordsServices;
	private static AdWordsSession	session;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String action = "";
		try {
			action = req.getParameter("action");
			if (action == null) {
				return;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		switch (action) {
			case "addCampaign": 
		}
		
	}
	
	static {
		Configuration config = new PropertiesConfiguration();

		config.setProperty("api.adwords.developerToken", "xh5fcw0gDCw10sCVmBiMeg");
		config.setProperty("api.adwords.clientCustomerId", "681-322-0047");
		config.setProperty("api.adwords.email", "ipeirotis@gmail.com");
		config.setProperty("api.adwords.password", "armani12");

		config.setProperty("api.adwords.environment", "production");
		config.setProperty("api.adwords.soapModule", "com.google.api.ads.adwords.jaxws.AdWordsJaxWsModule");
		config.setProperty("api.adwords.userAgent", "PanosIpeirotis-Test");
		
		// api.adwords.isPartialFailure=false
		// api.adwords.returnMoneyInMicros=true

		// Get a ClientLogin AuthToken.
		String clientLoginToken = null;
		try {
			clientLoginToken = new ClientLoginTokens.Builder().forApi(ClientLoginTokens.Api.ADWORDS).from(config)
					.build().requestToken();
		} catch (IOException | ValidationException e) {
			e.printStackTrace();
		}

		// Construct an AdWordsSession.
		try {
			session = new AdWordsSession.Builder().from(config).withClientLoginToken(clientLoginToken).build();
		} catch (ValidationException e) {
			e.printStackTrace();
		}

		adWordsServices = new AdWordsServices();
	}

	public CampaignManagement() throws Exception {

		// NOTE: We used to have session and adWordsServices as instance variables, but it seems that having them as static works better. 
		// Need to test it further

	}

	public Campaign createCampaign(String campaignName, Integer dailyBudget) throws Exception {

		// Create campaign.
		Campaign campaign = new Campaign();
		campaign.setName(campaignName + "#" + System.currentTimeMillis());
		campaign.setStatus(CampaignStatus.ACTIVE);

		BiddingStrategyConfiguration biddingStrategyConfiguration = new BiddingStrategyConfiguration();
		biddingStrategyConfiguration.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC);
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

		// Set required keyword match to TRUE to allow for "the broadening of exact and phrase
		// keyword matches for this campaign to include small variations such as plurals,
		// common misspellings, diacriticals and acronyms"
		KeywordMatchSetting keywordMatch = new KeywordMatchSetting();
		keywordMatch.setOptIn(Boolean.TRUE);
		campaign.getSettings().add(keywordMatch);

		
		// Set the campaign network options to Search only
		NetworkSetting networkSetting = new NetworkSetting();
		networkSetting.setTargetGoogleSearch(true);
		networkSetting.setTargetSearchNetwork(false);
		networkSetting.setTargetContentNetwork(false);
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
	public Long publishCampaign(Campaign campaign) throws RemoteException, ApiException_Exception {

		// Get the CampaignService.
		CampaignServiceInterface campaignService = adWordsServices.get(session, CampaignServiceInterface.class);
		
		CampaignOperation operation = new CampaignOperation();
		operation.setOperand(campaign);
		operation.setOperator(Operator.ADD);
		List<CampaignOperation> operations = new ArrayList<CampaignOperation>();
		operations.add(operation);
	
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
	 * @throws ApiException_Exception 
	 * @throws ApiException
	 */
	/*
	private Budget getBudget(int dailyBudget) throws RemoteException, ApiException_Exception {

		

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
		List<BudgetOperation> budgetoperations = new ArrayList<BudgetOperation>();
		budgetoperations.add(budgetOperation);

		// Only the budgetId should be sent, all other fields will be ignored by CampaignService.
		//Long budgetId = budgetService.mutate(budgetoperations).getValue(0).getBudgetId();
		Long budgetId = budgetService.mutate(budgetoperations).getValue().get(0).getBudgetId();
		budget.setBudgetId(budgetId);
		return budget;
	}
	*/

	public AdGroup createAdgroup(String adGroupName, Long campaignId, Double bidAmount) {

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
		textAdGroupAd.setStatus(AdGroupAdStatus.ENABLED);

		return textAdGroupAd;
	}

	public Long publishTextAd(AdGroupAd ad) throws Exception {

		// Get the AdGroupAdService.
		AdGroupAdServiceInterface adGroupAdService = adWordsServices.get(session, AdGroupAdServiceInterface.class);

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

		List<AdGroupCriterionOperation> operations = new ArrayList<AdGroupCriterionOperation>();
		operations.add(keywordAdGroupCriterionOperation);

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

		runTest();

	}

	/**
	 * 
	 */
	private static void runTest() {

		try {
			CampaignManagement service = new CampaignManagement();

			String campaignName = "Test";
			Integer dailyBudget = 10;
			Campaign campaign = service.createCampaign(campaignName, dailyBudget);
			Long campaignId = service.publishCampaign(campaign);

			String adGroupName = "Test-Adgroup";
			Double cpcbid = 0.05;
			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, cpcbid);
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
			//logger.log(Level.SEVERE, "Exception in Testing.", e);
			e.printStackTrace();
		}
	}

}
