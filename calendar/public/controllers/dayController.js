var calendarApp = angular.module('dayApp',[]);
calendarApp.controller('AppCtrl', ['$scope', '$http', function($scope, $http) {
  console.log("Hello controller");
  $scope.title = "Choose a Date";

  $scope.refresh = function(day) {
    console.log($scope.day);
    var title = new Date(day).toString().split(" ");
    $scope.title = title[0] + " " + title[1] + " " + title[2];
    console.log('/day/' + day);
    $http.get('/day/' + day).success(function(response) {
      $scope.days = response;
      console.log("success!");
    });
  }
}]);
