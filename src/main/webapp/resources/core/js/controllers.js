/**
 * 
 */

angular.module('app')
    .controller('UserController', ['$scope', 'BASE_URL', 'userServ', function ($scope, BASE_URL, userServ) {
        $scope.credentials = {};
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
    .controller('HomeController', ['$scope', '$rootScope', 'BASE_URL', 'ProductCatalogue', 'categoryServ', function ($scope, $rootScope, BASE_URL, ProductCatalogue, categoryServ) {

        // CategoryController

        $scope.activeCategory = null;
        $scope.createdCategory = {};

        $scope.categoryList = categoryServ.collection;
        $scope.categoryPage = 0;

        $scope.showCategory = function showCategory(category) {
            if ($scope.activeCategory != null) {
                $scope.activeCategory.active = false;
            }

            if (category != null) {
                $scope.activeCategory = category;
                $scope.activeCategory.active = true;
                // request get:
            } else {
                $scope.activeCategory = null;
                // request get:
            }
        };

        $scope.showCategoryCreateModal = function showCategoryCreateModal() {
            $scope.modals['category-create-modal'].modal("show");
        };

        categoryServ.list();
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
    .controller('MyStockController', ['$scope', function ($scope) {
    }]);
