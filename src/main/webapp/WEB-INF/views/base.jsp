<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${ title }</title>
    <link rel="stylesheet" href="${ pageContext.request.contextPath }/resources/bootstrap-3.3.5/css/bootstrap.css">
    <link rel="stylesheet" href="${ pageContext.request.contextPath }/resources/font-awesome-4.4.0/css/font-awesome.css">
    <link rel="stylesheet" href="${ pageContext.request.contextPath }/resources/core/css/default-theme.css">

    <script src="${ pageContext.request.contextPath }/resources/jquery-1.11.3/js/jquery.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/bootstrap-3.3.5/js/bootstrap.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/angular-1.4.7/js/angular.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/angular-1.4.7/js/angular-resource.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/angular-1.4.7/js/angular-route.js"></script>
    <script>
        angular.module('app', ['ngRoute', 'ngResource', 'app.services', 'app.controllers'])
            .constant('URLS', {
                TEMPLATE: '${ pageContext.request.contextPath }/resources/core/templates',
                PRODUCT: 'http://130.206.121.54/DSPRODUCTCATALOG2/api/catalogManagement/v2/productSpecification/:id',
                PRODUCT_CATALOGUE: 'http://130.206.121.54/DSPRODUCTCATALOG2/api/catalogManagement/v2/catalog/:id',
                PRODUCT_CATEGORY: 'http://130.206.121.54/DSPRODUCTCATALOG2/api/catalogManagement/v2/category/:id'
            })
            .constant('LOGGED_USER', {
                ID: '${ user.userName }',
                ROLE: '${ viewName }',
                HREF: '${ pageContext.request.contextPath }/api/v2/user/${ user.userName }',
                BEARER_TOKEN: 'Bearer ${ user.accessToken }'
            });
    </script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/app.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/services/UserService.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/services/ProductService.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/services/ProductCatalogueService.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/services/ProductCategoryService.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers/UserController.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers/ProductController.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers/ProductOfferingController.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers/ProductCatalogueController.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers/ProductCategoryController.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/routes.js"></script>
  </head>
  <body ng-app="app" ng-controller="UserCtrl">
    <nav class="navbar navbar-default navbar-static-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-right">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="${ pageContext.request.contextPath }">
            <img src="${ pageContext.request.contextPath }/resources/core/images/tmforum-logo.png">
            <span class="text-right">Portal</span>
          </a>
        </div>
        <div id="navbar-right" class="collapse navbar-collapse">
          <ul class="nav navbar-nav navbar-right">
            <li ng-class="{'active': $userRole == 'Customer'}"><a href="${ pageContext.request.contextPath }"><i class="fa fa-newspaper-o fa-fw"></i>&nbsp; Marketplace</a></li>
            <li ng-class="{'active': $userRole == 'Seller'}"><a href="${ pageContext.request.contextPath }/mystock"><i class="fa fa-cubes fa-fw"></i>&nbsp; My stock</a></li>
            <li class="dropdown">
              <a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user fa-fw"></i>&nbsp; ${ user.displayName } <i class="fa fa-caret-down"></i></a>
              <ul class="dropdown-menu">
                <li><a data-toggle="modal" data-target="#settings-modal"><i class="fa fa-cog fa-fw"></i>&nbsp; Settings</a></li>
                <li><a ng-click="signOut()"><i class="fa fa-sign-out fa-fw"></i>&nbsp; Sign out</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <div class="container">
      <t:insertAttribute name="content" />
      <div id="settings-modal" class="modal fade" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
              <div class="modal-title">Settings</div>
            </div>
            <div class="modal-body">
              <div class="row">
                <div class="col-sm-10 col-sm-offset-1">
                  <ul class="nav nav-tabs">
                    <li class="active"><a href="#">Profile</a></li>
                  </ul>
                  <form name="userUpdateForm">
                    <div class="form-group">
                      <label>Username</label>
                      <input type="text" class="form-control" placeholder="${ user.userName }" readonly>
                    </div>
                    <div class="form-group">
                      <label>Email</label>
                      <input type="text" class="form-control" placeholder="${ user.email }" readonly>
                    </div>
                  </form>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <form name="signOutForm" method="post" action="${ pageContext.request.contextPath }/logout"></form>
    <footer class="clearfix">
      <hr class="fiware-line">
      <div class="col-sm-6">
        <ul class="list-inline">
          <li class="text-muted"><small>© 2015 CoNWeT Lab., Universidad Politécnica de Madrid</small></li>
        </ul>
      </div>
      <div class="col-sm-6">
        <ul class="list-inline pull-right">
          <li><small><a href="https://www.fiware.org">FIWARE</a></small></li>
          <li><small>·</small></li>
          <li><small><a href="https://www.tmforum.org">TM FORUM</a></small></li>
        </ul>
      </div>
    </footer>
  </body>
</html>