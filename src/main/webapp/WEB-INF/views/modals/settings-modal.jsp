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

        <div class="form-group">
          <label for="exampleInputEmail1">Full name</label>
          <input type="email" class="form-control" id="exampleInputEmail1" placeholder="${ user.displayName }" readonly>
        </div>

        <div class="form-group">
          <label for="exampleInputEmail1">Email</label>
          <input type="email" class="form-control" id="exampleInputEmail1" placeholder="${ user.email }" readonly>
        </div>

      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>