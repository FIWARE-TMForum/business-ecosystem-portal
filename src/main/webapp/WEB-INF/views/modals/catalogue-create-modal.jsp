<div id="create-catalogue" bs-modal class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        <h5 class="modal-title title-uppercase">New catalogue</h5>
      </div>
      <div class="modal-body">
        <div class="form-wizard">
          <div class="form-wizard-group">
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 1}" ng-click="showStep(1)">1</a>
              <h6>General</h6>
            </div>
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 2}" ng-click="showStep(2)" ng-disabled="!catalogueStep1CreateForm.$valid" >2</a>
              <h6>Parties</h6>
            </div>
            <div class="form-wizard-step">
              <a type="button" class="btn btn-default btn-circle" ng-class="{'active': currentStep == 3}" ng-click="showStep(3)" ng-disabled="!catalogueStep1CreateForm.$valid || !catalogueStep2CreateForm.$valid" >3</a>
              <h6>Categories</h6>
            </div>
          </div>
        </div>
        <div ng-hide="currentStep != 1">
          <form class="form-horizontal" name="catalogueStep1CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-10 col-sm-offset-1">
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Name</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Name" name="name", ng-model="catalogueCreated.name" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Version</label>
                  <div class="col-sm-7">
                    <input type="text" class="form-control" placeholder="Version" name="version" ng-model="catalogueCreated.version" required>
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">Start datetime</label>
                  <div class="col-sm-7">
                    <input type="datetime-local" class="form-control" name="startDateTime" ng-model="catalogueCreated.validFor.startDateTime" required min="{{minDatetime | date:'yyyy-MM-ddTHH:mm:ss'}}">
                  </div>
                </div>
                <div class="form-group">
                  <label class="col-sm-3 col-sm-offset-1 control-label">End datetime</label>
                  <div class="col-sm-7">
                    <input type="datetime-local" class="form-control" name="endDateTime" ng-model="catalogueCreated.validFor.endDateTime" min="{{catalogueCreated.validFor.startDateTime | date:'yyyy-MM-ddTHH:mm:ss'}}">
                  </div>
                </div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep1CreateForm.$valid" ng-click="showStep(2)">Next</button>
              </div>
            </div>
          </form>
        </div>
        <div ng-hide="currentStep != 2">
          <form class="form-horizontal" method="POST" name="catalogueStep2CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-12">
                <div class="alert alert-info">Not implemented yet.</div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep2CreateForm.$valid" ng-click="showStep(3)">Next</button>
              </div>
            </div>
          </form>
        </div>
        <div ng-hide="currentStep != 3">
          <form class="form-horizontal" method="POST" name="catalogueStep3CreateForm" novalidate>
            <div class="row">
              <div class="col-sm-12">
                <div class="alert alert-info">Not implemented yet.</div>
              </div>
              <div class="col-sm-12 text-right">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" ng-disabled="!catalogueStep3CreateForm.$valid" ng-click="createCatalogue(catalogueCreated)">Create</button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
