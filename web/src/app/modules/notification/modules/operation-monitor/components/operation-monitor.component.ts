import { ChangeDetectorRef, Component } from '@angular/core';
import { ViewControllerService } from '../../../../views/controller/modules/view-controller/services/view-controller.service';
import { LoadingModalService } from '../../modals/services/loading-model-service';
import { OperationMonitorService } from '../services/operation-monitor.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'operation-monitor',
    templateUrl: 'operation-monitor.component.html'
})
export class OperationMonitor {

    private _isLoading: boolean;

    constructor(
        private viewController: ViewControllerService,
        private loadingModal: LoadingModalService,
        private operationMonitorService: OperationMonitorService,
        private changeDetectorRef: ChangeDetectorRef) {

        this.operationMonitorService.modalStateChanged
            .subscribe((open: boolean) => this.onModalStateChanged(open));

        this.operationMonitorService.loadingStateChanged
            .subscribe((isLoading: boolean) => this.isLoading = isLoading);
    }

    public get isLoading(): boolean {
        return this._isLoading;
    }

    public set isLoading(isLoading: boolean) {
        this.changeDetectorRef.detectChanges();
        this._isLoading = isLoading;
        this.changeDetectorRef.detectChanges();
    }

    private onModalStateChanged(open: boolean): void {
        if (open) {
            this.loadingModal.open();
        } else {
            this.loadingModal.close();
        }
    }

    public toggleLoggingView(): void {
        this.viewController.loggingOutputShown = !this.viewController.loggingOutputShown;
    }
}
