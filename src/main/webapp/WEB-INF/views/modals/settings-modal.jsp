<div id="settings-modal" bs-modal class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        <h5 class="modal-title title-uppercase">Settings</h5>
      </div>
      <div class="modal-body">
        <ul class="nav nav-tabs">
          <li class="active"><a href="#">Profile</a></li>
        </ul>
        <form class="form-horizontal" name="userUpdateForm" novalidate>
          <div class="row">
            <div class="col-sm-10 col-sm-offset-1">
              <div class="form-group">
                <label class="col-sm-3 col-sm-offset-1 control-label">Full name</label>
                <div class="col-sm-7">
                  <input type="text" class="form-control" placeholder="${ user.displayName }" readonly>
                </div>
              </div>
              <div class="form-group">
                <label class="col-sm-3 col-sm-offset-1 control-label">Email</label>
                <div class="col-sm-7">
                  <input type="text" class="form-control" placeholder="${ user.email }" readonly>
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>