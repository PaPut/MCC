var calendarApp = angular.module('monthApp',[]);
calendarApp.controller('AppCtrl', ['$scope', '$http', function($scope, $http) {
  console.log("Hello controller");
  $scope.title = "Choose a Month";

  $scope.refresh = function(month) {
    console.log($scope.month);
    var title = new Date(month).toString().split(" ");
    $scope.title = title[1] + " " + title[3];
    console.log('/month/' + month);
    $http.get('/month/' + month).success(function(response) {
      $scope.months = response;
      console.log("success!");
    });
  }
}]);
