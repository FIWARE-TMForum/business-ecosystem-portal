/**
 * 
 */

angular.module('app')
    .controller('UserController', ['$scope', '$route', 'BASE_URL', 'VIEW_NAME', 'userServ', function ($scope, $route, BASE_URL, VIEW_NAME, userServ) {
        $scope.credentials = {};
        $scope.view = VIEW_NAME;
        $scope.$route = $route;
        $scope.signOut = function signOut() {
            return userServ.signOut();
        };
    }])
    .controller('MessageController', ['$scope', '$interpolate', '$sce', function ($scope, $interpolate, $sce) {
        $scope.hidden = true;
        $scope.$on('alert', function (event, message, context) {
            $scope.message = $sce.trustAsHtml($interpolate(message)(context || {}));
            $scope.hidden = false;
        });
    }])
    .controller('HomeController', ['$scope', '$rootScope', function ($scope, $rootScope) {
    }])
    .controller('ProductCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
    }])
    .controller('ProductOfferingCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
    }])
    .controller('ProductCatalogueCtrl', ['$scope', '$rootScope', 'USER_PROFILE', 'ProductCatalogue', function ($scope, $rootScope, USER_PROFILE, ProductCatalogue) {

        $scope.catalogueList = ProductCatalogue.collection;
        $scope.catalogueSelected = null;
        $scope.catalogueCreated = {validFor: {}};

        $scope.currentStep = 1;

        var autocomplete = function autocomplete(catalogueCreated) {
            return angular.extend(catalogueCreated, {
                type: ProductCatalogue.TYPE,
                category: [],
                lifecycleStatus: ProductCatalogue.LIFECYCLE_STATUS.ACTIVE,
                relatedParty: [
                    {
                        id: USER_PROFILE.username,
                        role: ProductCatalogue.PARTY_ROLES.OWNER
                    }
                ]
            });
        };

        $scope.showStep = function showStep(step) {
            if ($scope.currentStep != step) {
                $scope.currentStep = step;
            }
        };

        $scope.showCreateForm = function showCreateForm() {
            $scope.minDateTime = new Date();
            $scope.catalogueCreated = {validFor: {startDateTime: new Date()}};
            $scope.currentStep = 1;
            $scope.modals['create-catalogue'].modal("show");
        };

        $scope.createCatalogue = function createCatalogue(catalogue) {
            ProductCatalogue.create(autocomplete(catalogue), function () {
                $scope.modals['create-catalogue'].modal('hide');
            });
        };

        $scope.selectCatalogue = function selectCatalogue(catalogue) {
            if ($scope.catalogueSelected != null) {
                $scope.catalogueSelected.active = false;
            }

            if (catalogue != null) {
                $scope.catalogueSelected = catalogue;
                $scope.catalogueSelected.active = true;
            } else {
                $scope.catalogueSelected = null;
            }
        };

        ProductCatalogue.list();
    }])
    .controller('ProductCategoryCtrl', ['$scope', '$rootScope', 'ProductCategory', function ($scope, $rootScope, ProductCategory) {

        $scope.categoryList = ProductCategory.collection;
        $scope.categorySelected = null;
        $scope.categoryCreated = {validFor: {}};

        var autocomplete = function autocomplete(categoryCreated) {
            return angular.extend(categoryCreated, {
                isRoot: true,
                lifecycleStatus: ProductCategory.LIFECYCLE_STATUS.ACTIVE
            });
        };

        $scope.showCreateForm = function showCreateForm() {
            $scope.categoryCreated = {validFor: {startDateTime: new Date()}};
            $scope.modals['create-category'].modal("show");
        };

        $scope.createCategory = function createCategory(category) {
            ProductCategory.create(autocomplete(category), function () {
                $scope.modals['create-category'].modal('hide');
            });
        };

        $scope.selectCategory = function selectCategory(category) {
            if ($scope.categorySelected != null) {
                $scope.categorySelected.active = false;
            }

            if (category != null) {
                $scope.categorySelected = category;
                $scope.categorySelected.active = true;
            } else {
                $scope.categorySelected = null;
            }
        };

        ProductCategory.list();
    }])
    .controller('MyStockController', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $scope.$on("$routeChangeStart", function (event, next) {
        	$scope.activeController = next.controller;
        });
    }]);
