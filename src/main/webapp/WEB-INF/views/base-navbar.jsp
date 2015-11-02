<li><a href="${ pageContext.request.contextPath }/mystock"><i class="fa fa-archive fa-fw"></i>&nbsp; My stock</a></li>
<li class="dropdown">
  <a class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user fa-fw"></i>&nbsp; ${ user.displayName } <i class="fa fa-caret-down"></i></a>
  <ul class="dropdown-menu">
    <li><a data-toggle="modal" data-target="#settings-modal"><i class="fa fa-cog fa-fw"></i>&nbsp; Settings</a></li>
    <li><a ng-click="signOut()"><i class="fa fa-sign-out fa-fw"></i>&nbsp; Sign out</a></li>
  </ul>
</li>