angular.module('quizz-admin').controller('QuizController',
    ['$scope', '$routeParams', '$modal', 'templates', 'quizService', 'campaignService', function ($scope, $routeParams, $modal, templates, quizService, campaignService) {

      $scope.loadQuiz = function () {
        if($routeParams.quizID) {
          quizService.getQuiz($routeParams.quizID, function(response) {
            $scope.quiz = response;
            $scope.quizID = $scope.quiz.quizID;
            $scope.readyToShow = true;
          },
          function(error) {
            console.log(error);
          });
        }
      };

      $scope.loadQuiz();
      
      $scope.loadCampaigns = function() {
        campaignService.listCampaigns(
          function(response) {
            $scope.campaigns = response.items;
          },
          function(error) {
          });
      };
      $scope.loadCampaigns();

      $scope.showCampaignModal = function(campaign) {
        var modalInstance = $modal.open({
          templateUrl: templates.campaignModal,
          controller: 'CampaignModalController',
          resolve: {
            campaign: function () {
              return campaign;
            }
          }
        });

        modalInstance.result.then(function (campaign) {
          
        });
      };

      $scope.filterCampaigns= function(campaign){ return campaign.id === $scope.quiz.campaignid && $scope.quiz.campaignid; }
}]);
