'use strict';

var editator = angular.module('editator', [])

editator.directive('edEventsService', function() {
	return function(scope, element, attrs) {
		scope.connect(attrs.edEventsService)
	}
})
	
function Editator($scope, $http) {

	$scope.room = {
		'users': [ {'name': 'fred'}, {'name': 'bob'} ]
	}
	
	$scope.user = {
		'nick': ''
	}

	$scope.updateNick = function() {
		$http.post('/join', angular.toJson(this.user)).success(function(result){ alert(result); })
	}


	$scope.connect = function(serviceLocation) {
		alert(serviceLocation);
		var ws = new WebSocket(serviceLocation)

		ws.onmessage = function(evt) {
			var json = angular.fromJson(evt.data)
			$scope.$apply(function(){ $scope.room.users = json.users })
		}
	}
}
