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
	this.id = ''
}

function Room() {
	this.key = ''
	this.users = []
	this.setJoined = function(isJoined) {
		this.isJoined = isJoined
		this.joinLabel = this.isJoined ? 'Leave room' : 'Join room'
	}
}

function RoomSet() {
	this.selectedRoom = ''
	this.roomSet = {}
	this.rooms = function() {
		return Object.keys(this.roomSet)
	}
	this.addRoom = function(room) {
		this.roomSet[room] = true
	}
}

function Editator($scope, $http) {
	
	$scope.jsonWithKey = function(messageObj) {
		var message = angular.copy(messageObj)
		message.key = $scope.room.isJoined ? $scope.room.key : $scope.roomSet.selectedRoom
		return angular.toJson(message)
	}
	
	$scope.content = new Content()

	$scope.room = new Room()
	$scope.room.setJoined(false)
	
	$scope.user = new User()
	
	$scope.roomSet = new RoomSet()
	$http.get('/rooms').success( function(data) {
		var json = angular.fromJson(data)
		angular.forEach(json.rooms, function(room) {
			$scope.roomSet.addRoom(room)
		})
	})
		
	$scope.updateNick = function() {
		if(this.room.isJoined) {
			$http.post('/nick', $scope.jsonWithKey(this.user))
		}
	}

	$scope.toggleJoinRoom = function() {
		$http.post('/joinToggle', $scope.jsonWithKey(this.user)).success( function(data) {
			$scope.room.setJoined(data.isJoined)
			$scope.roomSet.addRoom(data.roomKey)
			$scope.room.key = data.roomKey
			$scope.user = data.user
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
