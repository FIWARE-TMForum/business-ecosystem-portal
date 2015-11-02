<div id="catalogue-create-modal" bs-modal class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        <h5 class="modal-title">New catalogue</h5>
      </div>
      <div class="modal-body">
        <div class="form-wizard">
          <div class="form-wizard-group">
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 1}" ng-click="currentStep != 1 && showStep(1)">1</a>
              <h6>General</h6>
            </div>
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 2}" ng-disabled="!catalogueStep1CreateForm.$valid || lastStepCompleted < 1" ng-click="currentStep != 2 && showStep(2)">2</a>
              <h6>Roles</h6>
            </div>
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 3}" ng-disabled="!catalogueStep1CreateForm.$valid || !catalogueStep2CreateForm.$valid || lastStepCompleted < 2" ng-click="currentStep != 3 && showStep(3)">3</a>
              <h6>Categories</h6>
            </div>
          </div>
        </div>
        <div ng-hide="currentStep != 1">
          <form class="form-horizontal" name="catalogueStep1CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-12">
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Name</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Name" name="name", ng-model="createdCatalogue.name" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Version</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Version" name="version" ng-model="createdCatalogue.version" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Start datetime</label>
                  <div class="col-sm-4">
                    <input type="date" class="form-control" name="startDate" ng-model="createdCatalogue.startDate">
                  </div>
                  <div class="col-sm-3">
                    <input type="time" class="form-control" name="startTime" ng-model="createdCatalogue.startTime">
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">End datetime</label>
                  <div class="col-sm-4">
                    <input type="date" class="form-control" name="endDate" ng-model="createdCatalogue.endDate">
                  </div>
                  <div class="col-sm-3">
                    <input type="time" class="form-control" name="endTime" ng-model="createdCatalogue.endTime">
                  </div>
                </div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep1CreateForm.$valid" ng-click="nextStep(2)">Next</button>
              </div>
            </div>
          </form>
        </div>
        <div ng-hide="currentStep != 2">
          <form class="form-horizontal" method="POST" name="catalogueStep2CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-12">
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Role</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Role" name="role", ng-model="newCatalogue.role">
                  </div>
                </div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep2CreateForm.$valid" ng-click="nextStep(3)">Next</button>
              </div>
            </div>
          </form>
        </div>
        <div ng-hide="currentStep != 3">
          <form class="form-horizontal" method="POST" name="catalogueStep3CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-12">
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Category</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Category" name="category", ng-model="newCatalogue.category">
                  </div>
                </div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep3CreateForm.$valid" ng-click="createCatalogue(newCatalogue)">Create</button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
