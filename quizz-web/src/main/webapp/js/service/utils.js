angular.module('quizz').factory('utils', function(){
	var utils = {
		shuffle: function(array) {
			var counter = array.length, temp, index;

			// While there are elements in the array
			while (counter--) {
				// Pick a random index
				index = (Math.random() * (counter + 1)) | 0;

				// And swap the last element with it
				temp = array[counter];
				array[counter] = array[index];
				array[index] = temp;
			}
			return array;
		},
		createUUID: function() {
		    // http://www.ietf.org/rfc/rfc4122.txt
		    var s = [];
		    var hexDigits = "0123456789abcdef";
		    for (var i = 0; i < 36; i++) {
		        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
		    }
		    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
		    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
		    s[8] = s[13] = s[18] = s[23] = "-";

		    var uuid = s.join("");
		    return uuid;
		},
	    isNormalNumber: function(value) {
			return ! (isNaN(value) || typeof value === undefined);
		},
		toPercentage: function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		},
		safeNumber: function(value) {
			return  this.isNormalNumber(value) ? value.toString() : "---" ;
		},
		toPercentage: function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		},
		toSafePercentage: function(fValue) {
			if (this.isNormalNumber(fValue))
				return this.toPercentage(fValue);
			else
				return "---";
		}
	};
	return utils;
});