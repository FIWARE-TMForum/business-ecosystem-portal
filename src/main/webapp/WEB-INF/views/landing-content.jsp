<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="jumbotron">
  <div class="row">
    <div class="col-sm-6"></div>
    <div class="col-sm-6">
      <div class="panel panel-default">
        <div class="panel-heading">
          <span class="panel-title">Sign in</span>
        </div>
        <div class="panel-body">
          <form name="signInForm" novalidate method="POST" action="<c:url value='j_spring_security_check' />">
            <c:if test="${ not empty param.err }">
            <div class="alert alert-danger">
              <span class="fa fa-times-circle"></span> The username and password do not match.
            </div>
            </c:if>
            <div class="form-group">
              <input class="form-control" type="text" name="username" ng-model="credentials.username" placeholder="Username or Email" required autofocus>
            </div>
            <div class="form-group">
              <input class="form-control" type="password" name="password" ng-model="credentials.password" placeholder="Password" required>
            </div>
            <div class="pull-right">
              <button type="submit" class="btn btn-primary" ng-disabled="!signInForm.$valid">Sign in</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>