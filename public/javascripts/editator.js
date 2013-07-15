'use strict';

var editator = angular.module('editator', [])

editator.directive('edEventsService', function() {
	return function(scope, element, attrs) {
		scope.events = new EditatorEvents(scope, attrs.edEventsService)
	}
})

function EditatorEvents(scope, serviceLocation) {
	this.serviceLocation = serviceLocation
	this.scope = scope
	this.ws;
	
	this.newEventHandler = function(scope) {
		return function(evt) {
			var msg = angular.fromJson(evt.data)
			var handler = scope.handlers[msg.type]
			if(handler) {
				scope.$apply(function(){ handler(msg) })
			}
		}
	}
	this.eventHandler;
	
	this.connect = function(roomKey, userId) {
		var wsLocation = this.serviceLocation.replace("{roomKey}", roomKey).replace("{userId}", userId)
		this.ws = new WebSocket(wsLocation)
		this.eventHandler = this.newEventHandler(this.scope)
		this.ws.addEventListener('message', this.eventHandler)
	}
	
	this.disconnect = function() {
		if(this.ws) {
			this.ws.removeEventListener('message', this.eventHandler)
			this.ws.close()
		}
	}
	
	this.notify = function(wasJoined, isJoined, roomKey, userId) {
		if(wasJoined != isJoined) {
			if(isJoined) {
				this.connect(roomKey, userId)
			}
			else {
				this.disconnect()
			}
		}
	}
}

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
	this.roomLabel = function() {
		return this.isJoined ? 'Room: ' + this.key : 'Lobby'
	}
	this.blah;
	this.messages = ['hello', 'world']
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
	
	$scope.chatMessage = function(blah) {
		var message = {
			'blah': blah,
			'user': $scope.user
		}
		return message;
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
			var wasJoined = $scope.room.isJoined
			$scope.room.setJoined(data.isJoined)
			$scope.roomSet.addRoom(data.roomKey)
			$scope.room.key = data.roomKey
			$scope.roomSet.selectedRoom = data.roomKey
			$scope.user = data.user
			$scope.events.notify(wasJoined, data.isJoined, data.roomKey, data.user.id)
		})
	}

	$scope.sendMessage = function() {
		$http.post('/chat', $scope.jsonWithKey($scope.chatMessage($scope.room.blah)))
	}

	$scope.handlers = {
		'memberUpdate': function(msg) {
			$scope.room.users = msg.members
		},
	}
}
