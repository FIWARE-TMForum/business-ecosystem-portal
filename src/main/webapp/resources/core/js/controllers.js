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
    .controller('HomeController', ['$scope', '$rootScope', 'BASE_URL', 'catalogueServ', 'categoryServ', function ($scope, $rootScope, BASE_URL, catalogueServ, categoryServ) {

        // CatalogueController

        $scope.activeCatalogue = null;
        $scope.createdCatalogue = {};

        var createdCatalogue = angular.copy($scope.createdCatalogue);

        $scope.catalogueList = catalogueServ.collection;
        $scope.cataloguePage = 0;

        $scope.showCatalogue = function showCatalogue(catalogue) {
            if ($scope.activeCatalogue != null) {
                $scope.activeCatalogue.active = false;
            }

            if (catalogue != null) {
                $scope.activeCatalogue = catalogue;
                $scope.activeCatalogue.active = true;
                // request get:
            } else {
                $scope.activeCatalogue = null;
                // request get:
            }
        };

        catalogueServ.list();

        $scope.currentStep = 1;
        $scope.lastStepCompleted = 0;

        $scope.nextStep = function nextStep(step) {
            if ($scope.currentStep < step) {
                $scope.lastStepCompleted = $scope.currentStep;
            }
            $scope.currentStep = step;
        };

        $scope.showStep = function showStep(step) {
            $scope.currentStep = step;
        };

        $scope.showCatalogueCreateModal = function showCatalogueCreateModal() {
            $scope.createdCatalogue = angular.copy(createdCatalogue);
            $scope.currentStep = 1;
            //$scope.catalogueStep1CreateForm.$setPristine();
            $scope.modals['catalogue-create-modal'].modal("show");
        };

        $scope.createCatalogue = function createCatalogue(catalogue) {
            console.log(catalogue);
            $scope.closeModal();
            $rootScope.$broadcast('alert', "The catalogue <strong>{{ name }}</strong> was created successfully.", {name: catalogue.name});
        };

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
    .controller('MyStockController', ['$scope', function ($scope) {
    }]);
