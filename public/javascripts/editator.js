'use strict';

var editator = angular.module('editator', [])

editator.directive('edEventsService', function() {
	return function(scope, element, attrs) {
		scope.connect(attrs.edEventsService)
	}
})

function Content() {
	this.text = ''
}


function User() {
	this.nick = ''
	this.id;
}

function Editator($scope, $http) {

	$scope.content = new Content()

	$scope.room = {
		'users': [],
		'setJoined': function(isJoined) {
			this.isJoined = isJoined
			this.joinLabel = this.isJoined ? 'Leave room' : 'Join room'
		}
	}
	$scope.room.setJoined(false)
	
	$scope.user = new User()

	$scope.updateNick = function() {
		if(this.room.isJoined) {
			$http.post('/nick', angular.toJson(this.user))
		}
	}

	$scope.toggleJoinRoom = function() {
		$http.post('/joinToggle', angular.toJson(this.user)).success( function(data) {
			var json = angular.fromJson(data)
			$scope.room.setJoined(json.isJoined)
			$scope.user = json.user
		})
	}

	$scope.handlers = {
		'memberUpdate': function(msg) {
			$scope.room.users = msg.members
		},
	}

	$scope.connect = function(serviceLocation) {
		var ws = new WebSocket(serviceLocation)

		ws.addEventListener('message', function(evt) {
			var msg = angular.fromJson(evt.data)
			var handler = $scope.handlers[msg.type]
			if(handler) {
				$scope.$apply(function(){ handler(msg) })
			}
		})
	}
}
