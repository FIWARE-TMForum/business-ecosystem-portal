<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="row" ng-controller="MyStockController">
  <div class="col-sm-3">
    <div class="panel panel-default">
      <div class="panel-heading">
      </div>
      <div class="panel-body">
        <ul class="nav nav-pills nav-stacked">
          <li ng-class="{'active': activeController == 'ProductCtrl'}"><a href="#/products">Products</a></li>
          <li ng-class="{'active': activeController == 'ProductOfferingCtrl'}"><a href="#/offerings">Offerings</a></li>
        </ul>
      </div>
    </div>
  </div>
  <div class="col-sm-9">
    <div ng-view></div>
  </div>
  <t:insertTemplate template="/WEB-INF/views/modals/settings-modal.jsp" />
</div>