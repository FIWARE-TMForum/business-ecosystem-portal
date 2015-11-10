<div class="row" ng-controller="UserSellerView">
  <div class="col-sm-12">
    <div class="panel panel-default">
      <div class="panel-body">
        <ul class="nav nav-pills nav-justified">
          <li ng-class="{'active': activeController == 'ProductView'}"><a href="#/products"><i class="fa fa-archive fa-fw"></i>&nbsp;Products</a></li>
          <li class="divider"></li>
          <li ng-class="{'active': activeController == 'CatalogueView'}"><a href="#/catalogues"><i class="fa fa-book fa-fw"></i>&nbsp;Catalogues</a></li>
          <li ng-class="{'active': activeController == 'OfferingView'}"><a href="#/offerings"><i class="fa fa-cube fa-fw"></i>&nbsp;Offerings</a></li>
        </ul>
      </div>
    </div>
  </div>
  <div class="col-sm-12" ng-view></div>
</div>