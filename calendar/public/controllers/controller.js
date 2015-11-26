var calendarApp = angular.module('calendarApp',[]);
calendarApp.controller('AppCtrl', ['$scope', '$http', function($scope, $http) {
  console.log("hello controller");

  // Refreshes by getting all the events from the database
  var refresh = function() {
      $http.get('/events').success(function(response) {
        $scope.events = response;
        $scope.event = ""; // Empties the input fields
      });
  };

  refresh();

  // Adds an event
  $scope.addEvent = function() {
    console.log($scope.event);
    $http.post('/events', $scope.event).success(function(response) {
      // console.log(response);
      refresh();
    });
  };

  // Removes an event
  $scope.remove = function(id, googId) {
    console.log("This is the id: " + id);
    console.log("This is googId: " + googId);
    $http.delete('/events/' + id + "/" + googId).success(function(response) {
      refresh();
    });
  };

  // Edits an event
  $scope.edit = function(id, googId) {
    $http.get('/events/' + id).success(function(response) {
      // Puts the response into the input fields
      var temp = response;
      temp.start = new Date(temp.start);
      temp.end = new Date(temp.end);
      $scope.event = response;
    });
  };

  // Updates the event, which is in "edit mode"
  $scope.update = function(id, googId) {
    console.log($scope.event.googId);
    $http.put('/events/' + $scope.event._id + "/" + $scope.event.googId, $scope.event).success(function(response) {
      refresh();
    });
  };

  // Clears the input fields
  $scope.deselect = function() {
    $scope.event = "";
  }

}]);
