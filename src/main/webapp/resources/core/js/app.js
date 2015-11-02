/**
 * 
 */

angular.module('app')
    .constant('TMFORUM_URL', 'http://130.206.121.54/DSPRODUCTCATALOG2')
    .directive('bsModal', function () {
        var modals = {};

        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                modals[attrs.id] = element;
                scope.modals = modals;
            }
        };
    })
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.useXDomain = true;
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
    }]);
