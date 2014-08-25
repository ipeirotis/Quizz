angular.module('quizz-admin').factory('campaignService', ['$http', function($http) {

  function callbackWrapper(resp, success, error) {
    if (!resp.code && angular.isFunction(success)) {
      success(resp);
    } else if (angular.isFunction(error)) {
      error(resp);
    }
  };

  return {
    listCampaigns: function(success, error) {
      gapi.client.adwords.campaign.list().execute(
        function(resp) {
          callbackWrapper(resp, success, error);
        });
    },

    createCampaign: function(campaign, success, error) {
      var params = {};
      params.campaign = campaign;
      gapi.client.adwords.campaign.create(params).execute(
        function(resp) {
          callbackWrapper(resp, success, error);
        });
    }
  };
}]);
