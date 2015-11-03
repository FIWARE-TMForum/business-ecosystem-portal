/**
 * 
 */

angular.module('app')
    .config(['$routeProvider', 'TEMPLATE_URL', function ($routeProvider, TEMPLATE_URL) {
        $routeProvider
            .when('/products', {
                templateUrl: TEMPLATE_URL + '/products.html',
                controller: 'ProductCtrl'
            })
            .when('/offerings', {
                templateUrl: TEMPLATE_URL + '/offerings.html',
                controller: 'ProductOfferingCtrl'
            })
            .otherwise({
                redirectTo: '/products'
            });
    }]);
