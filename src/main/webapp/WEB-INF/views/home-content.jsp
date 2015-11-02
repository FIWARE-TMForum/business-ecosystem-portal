<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row" ng-controller="HomeController">
  <div class="col-sm-3">
    <div class="panel panel-default panel-catalogue">
      <div class="panel-heading">
        <span class="panel-title title-uppercase">Catalogues</span>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <div class="input-group">
            <input type="text" class="form-control" placeholder="Search for catalogue">
            <div class="input-group-btn">
              <button type="button" class="btn btn-default"><i class="fa fa-search fa-flip-horizontal"></i></button>
              <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"><i class="fa fa-caret-down"></i></button>
              <ul class="dropdown-menu dropdown-menu-right">
                <li><a ng-click="showCatalogueCreateModal()"><i class="fa fa-plus-circle fa-fw"></i>&nbsp; Create new catalogue</a></li>
              </ul>
            </div>
          </div>
        </div>
        <div class="list-group">
          <a class="list-group-item" ng-class="{'active': !activeCatalogue}" ng-click="showCatalogue()"><i class="fa fa-home fa-fw"></i>&nbsp; Home</a>
          <a class="list-group-item active" ng-click="showCatalogue(activeCatalogue)" ng-hide="!activeCatalogue"><i class="fa fa-book fa-fw"></i>&nbsp; {{ activeCatalogue.name }}</a>
        </div>
        <hr>
        <div class="list-group">
          <a class="list-group-item" ng-class="{'disabled': catalogue.active}" ng-click="showCatalogue(catalogue)" ng-repeat="catalogue in catalogueList"><i class="fa fa-book fa-fw"></i>&nbsp; {{ catalogue.name }}</a>
        </div>
        <div class="text-center">
          <ul class="pagination pagination-sm">
            <li ng-class="{'disabled': cataloguePage == 0}"><a><i class="fa fa-arrow-left"></i></a></li>
            <li ng-class="{'disabled': cataloguePage == 0}"><a><i class="fa fa-arrow-right"></i></a></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="panel panel-default panel-category">
      <div class="panel-heading">
        <span class="panel-title title-uppercase">Categories</span>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <div class="input-group">
            <input type="text" class="form-control" placeholder="Search for category">
            <div class="input-group-btn">
              <button type="button" class="btn btn-default"><i class="fa fa-search fa-flip-horizontal"></i></button>
              <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"><i class="fa fa-caret-down"></i></button>
              <ul class="dropdown-menu dropdown-menu-right">
                <li><a ng-click="showCategoryCreateModal()"><i class="fa fa-plus-circle fa-fw"></i>&nbsp; Create new category</a></li>
              </ul>
            </div>
          </div>
        </div>
        <div class="list-group">
          <a class="list-group-item" ng-class="{'active': !activeCategory}" ng-click="showCategory()"><i class="fa fa-tags fa-fw"></i>&nbsp; All offerings</a>
          <a class="list-group-item active" ng-click="showCategory(activeCategory)" ng-hide="!activeCategory"><i class="fa fa-tag fa-fw"></i>&nbsp; {{ activeCategory.name }}</a>
        </div>
        <hr>
        <div class="list-group">
          <a class="list-group-item" ng-class="{'disabled': category.active}" ng-click="showCategory(category)" ng-repeat="category in categoryList"><i class="fa fa-tag fa-fw"></i>&nbsp; {{ category.name }}</a>
        </div>
        <div class="text-center">
          <ul class="pagination pagination-sm">
            <li ng-class="{'disabled': categoryPage == 0}"><a><i class="fa fa-arrow-left"></i></a></li>
            <li ng-class="{'disabled': categoryPage == 0}"><a><i class="fa fa-arrow-right"></i></a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>
  <div class="col-sm-9"></div>
</div>