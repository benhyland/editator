function Editator($scope) {
	$scope.room = {
		'users': [ {'name': 'fred'}, {'name': 'bob'} ]
	}
	
	$scope.user = {
		'nick': ''
	}

	$scope.updateNick = function() {
		// todo: send to server and subscribe to room change events - atmosphere?
		this.room.users.push({'name': this.user.nick})
	}
}
