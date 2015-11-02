/**
 * 
 */

angular.module('app')
    .constant('API_URLS', {
        CATALOGUE: '/catalogManagement/catalog',
        CATEGORY: '/catalogManagement/category'
    })
    .constant('LIFECYCLE_STATUS', {
        ACTIVE: 'Active',
        LAUNCHED: 'Launched',
        RETIRED: 'Retired'
    })
    .constant('CATALOGUE_TYPES', {
        PRODUCT: 'Product Catalog',
        RESOURCE: 'Resource Catalog',
        SERVICE: 'Service Catalog'
    })
    .directive('bsModal', function () {
        return {
            restrict: 'A',
            link: function (scope, element) {
                scope.closeModal = function closeModal() {
                    element.modal('hide');
                };
            }
        };
    });
