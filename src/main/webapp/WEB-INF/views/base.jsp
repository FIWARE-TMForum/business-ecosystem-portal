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
    <script>
      angular.module('app', [])
        .constant('BASE_URL', "${ pageContext.request.contextPath }");
    </script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/utils.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/services.js"></script>
    <script src="${ pageContext.request.contextPath }/resources/core/js/controllers.js"></script>
  </head>
  <body ng-app="app" ng-controller="UserController">
    <nav class="navbar navbar-default navbar-static-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-right">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="${ pageContext.request.contextPath }">
            <img src="${ pageContext.request.contextPath }/resources/images/tmforum-logo.png">
            <p>Portal</p>
          </a>
        </div>
        <div class="collapse navbar-collapse" id="navbar-right">
          <t:insertAttribute name="navbar" ignore="true" />
        </div>
      </div>
    </nav>
    <div class="container">
      <div class="row">
          <div class="alert-manager" ng-controller="MessageController">
            <div class="alert alert-success" ng-class="{'hidden': hidden}">
              <strong>Done!</strong> <span class="alert-message" ng-bind-html="message"></span>
            </div>
          </div>
          <t:insertAttribute name="content" />
      </div>
    </div>
    <footer class="clearfix">
      <hr>
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
    <t:insertAttribute name="modals" ignore="true" />
  </body>
</html>