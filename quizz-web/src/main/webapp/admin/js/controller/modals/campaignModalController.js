angular.module('quizz-admin').controller('CampaignModalController', 
    [ '$scope', '$routeParams', '$modalInstance', 'campaignService', 'campaign', 
      function($scope, $routeParams, $modalInstance, campaignService, campaign) {

  $scope.campaign = campaign || {status: 'PAUSED'};
  $scope.campaign['quizID'] = $routeParams.quizID;

  $scope.save = function (form) {
    if(form.$invalid) {
      $scope.notValidForm = true;
      return;
    }

    campaignService.createCampaign($scope.campaign, function(response) {
      // TODO
      console.log(response);
      $modalInstance.close();
    }, function(error) {
      //TODO: show error
      console.log(error);
      $modalInstance.close();
    });

    $modalInstance.close($scope.campaign);
  };

  $scope.close = function () {
    $modalInstance.dismiss('cancel');
  };

}]);