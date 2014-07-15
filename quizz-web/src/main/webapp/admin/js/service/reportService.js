angular.module('quizz-admin').factory('reportService',
    ['$http', '$cacheFactory', function($http, $cacheFactory) {
  var LIMIT = 20;
  var pageTokens = [];
  var cache = $cacheFactory('domainsReportCache');

  function callbackWrapper(resp, success, error) {
    if (!resp.code && angular.isFunction(success)) {
      success(resp);
    } else if (angular.isFunction(error)) {
      error(resp);
    }
  };

  // All functions in reportService (except listQuizes) use gapi.client
  // to sends http request directly (instead of going through $http). Thus,
  // clients of these function should wrap the callback with $scope.$apply
  // to ensure that a digest cycle is kicked off to update the UI.
  return {
    loadAnswersReport: function(quizId, success, error) {
      var params = {};
      params.quizID = quizId;
      gapi.client.quizz.reports.multiChoiceAnswers(params).execute(
          function(resp) {
            callbackWrapper(resp, success, error);
          });
    },
    loadScoreByBrowserReport: function(success, error) {
      gapi.client.quizz.reports.scoreByBrowser().execute(function(resp) {
          callbackWrapper(resp, success, error);
      });
    },
    loadScoreByDomainReport: function(pageNumber, success, error) {
      if (pageNumber > 0 && pageNumber < pageTokens.length) {
        var items = cache.get(pageTokens[pageNumber]);
        if (items && angular.isFunction(success)) {
          success(items, false);
          return;
        }
      }

      var params = {};
      params.limit = LIMIT;
      if (pageNumber > 0) {
        params.cursor = pageTokens[pageNumber - 1];
      }
      gapi.client.quizz.reports.scoreByDomain(params).execute(
          function(response) {
            if (response.nextPageToken) {
              pageTokens.push(response.nextPageToken);
              cache.put(pageTokens[pageNumber], response.items);
            }
            if (angular.isFunction(success)) {
              success(response.items, true);
            }
          });
    },
    loadContributionQualityReport: function(success, error) {
      gapi.client.quizz.reports.contributionQuality().execute(function(resp) {
          callbackWrapper(resp, success, error);
      });
    },
    listQuizes: function(success, error) {
      $http.get(Config.api + '/listQuiz').success(success).error(error);
    }
  };
}]);
