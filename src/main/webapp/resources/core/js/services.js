/**
 * 
 */

angular.module('app.services', ['ngResource'])
    .factory('userServ', ['$http', 'BASE_URL', function ($http, BASE_URL) {
        return {
            signOut: function signOut(next) {
                return $http.post(BASE_URL + '/logout')
                    .then(function onsuccess(response) {
                        window.location = BASE_URL;
                    }, function onerror(response) {
                    });
            }
        };
    }])
    .factory('ProductCatalogue', ['$resource', '$http', 'TMFORUM_URL', 'USER_PROFILE', function ($resource, $http, TMFORUM_URL, USER_PROFILE) {
        var service, Resource;

        $http.defaults.headers.common['AUTHORIZATION']= 'Bearer ' + USER_PROFILE.BEARER_TOKEN;

        Resource = $resource(TMFORUM_URL + '/api/catalogManagement/v2/catalog/:id', {id: '@id'},
            {});
        service = {
            PARTY_ROLES: {
                OWNER: 'Owner'
            },
            LIFECYCLE_STATUS: {
                ACTIVE: 'Active',
                LAUNCHED: 'Launched',
                RETIRED: 'Retired'
            },
            TYPE: 'Product Catalog',
            collection: [],
            list: function list() {
                return Resource.query(function (response) {
                    angular.copy(response, service.collection);
                }, function (response) {
                    var fakeData = [
                        {pk: 1, name: "Catalogue 1"},
                        {pk: 2, name: "Catalogue 2"},
                        {pk: 3, name: "Catalogue 3"},
                    ];

                    angular.copy(fakeData, service.collection);
                });
            },
            create: function create(catalogue, next) {
                var entry = new Resource(catalogue);

                return Resource.save(entry, function () {
                    service.collection.unshift(entry);
                    next(entry);
                }, function () {
                    service.collection.unshift(entry);
                    next(entry);
                });
            }
        };

        return service;
    }])
    .factory('ProductCategory', ['$resource', 'TMFORUM_URL', function ($resource, TMFORUM_URL) {
        var service, Resource;

        Resource = $resource(TMFORUM_URL + '/api/catalogManagement/v2/category/:id', {id: '@id'},
            {});

        service = {
            LIFECYCLE_STATUS: {
                ACTIVE: 'Active'
            },
            collection: [],
            list: function list() {
                return Resource.query(function (response) {
                    angular.copy(response, service.collection);
                }, function (response) {
                    var fakeData = [
                        {pk: 1, name: "Category 1"},
                        {pk: 2, name: "Category 2"},
                        {pk: 3, name: "Category 3"},
                    ];

                    angular.copy(fakeData, service.collection);
                });
            },
            create: function create(catalogue, next) {
                var entry = new Resource(catalogue);

                return Resource.save(entry, function () {
                    service.collection.unshift(entry);
                    next(entry);
                }, function () {
                    service.collection.unshift(entry);
                    next(entry);
                });
            }
        };

        return service;
    }]);
