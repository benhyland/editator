'use strict';

var editator = angular.module('editator', [])

editator.directive('edEventsService', function() {
	return function(scope, element, attrs) {
		scope.connect(attrs.edEventsService)
	}
})

function User() {
	this.nick = ''
	this.id = ''
}

function Editator($scope, $http) {

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
			console.log(json)
			$scope.room.setJoined(json.isJoined)
			$scope.user = json.user
		})
	}

	$scope.handlers = {
		'roomUpdate': function(msg) {
			$scope.room = msg.room
		},

		'userUpdate': function(msg) {
			$scope.user = msg.user
		}
	}

	$scope.connect = function(serviceLocation) {
		var ws = new WebSocket(serviceLocation)

		ws.addEventListener('message', function(evt) {
			var msg = angular.fromJson(evt.data)
			console.log(msg)
			var handler = $scope.handlers[msg.type]
			if(handler) {
				$scope.$apply(function(){ handler(msg) })
			}
		})
	}
}
