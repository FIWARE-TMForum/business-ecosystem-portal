<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="row" ng-controller="MyStockController">
  <div class="col-sm-3">
    <div class="panel panel-default">
      <div class="panel-heading">
      </div>
      <div class="panel-body">
      </div>
    </div>
  </div>
  <div class="col-sm-9"></div>
  <t:insertTemplate template="/WEB-INF/views/modals/settings-modal.jsp" />
</div>