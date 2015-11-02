<div id="create-category" bs-modal class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        <h5 class="modal-title title-uppercase">New category</h5>
      </div>
      <div class="modal-body">
          <form class="form-horizontal" name="categoryCreateForm" novalidate>
            <div class="row">
              <div class="col-sm-10 col-sm-offset-1">
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Name</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Name" name="name", ng-model="categoryCreated.name" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Description</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Description" name="description", ng-model="categoryCreated.description" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Version</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Version" name="version" ng-model="categoryCreated.version" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Start datetime</label>
                  <div class="col-sm-7">
                    <input type="datetime-local" class="form-control" name="startDateTime" ng-model="categoryCreated.validFor.startDateTime" required>
                  </div>
                </div>
              </div>
            </div>
          </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-primary" ng-click="createCategory(categoryCreated)" ng-disabled="!categoryCreateForm.$valid">Create</button>
      </div>
    </div>
  </div>
</div>