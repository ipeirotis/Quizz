angular.module('quizz').factory('utils', function() {
  var utils = {
    isNormalNumber: function(value) {
      return !(isNaN(value) || typeof value === undefined);
    },
    toPercentage: function(fValue) {
      return (100. * fValue).toFixed(0) + "%";
    },
    toSafePercentage: function(fValue) {
      if (this.isNormalNumber(fValue)) {
        return this.toPercentage(fValue);
      } else {
        return "---";
      }
    }
  };
  return utils;
});
