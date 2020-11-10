import { ChangeDetectorRef, Component } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Config } from 'src/app/config/config';
import { ViewControllerService } from '../../../../views/controller/modules/view-controller/services/view-controller.service';
import { LoadingModalService } from '../../modals/services/loading-model-service';
import { OperationMonitorService } from '../services/operation-monitor.service';

enum DelayState {
    IDLE, START, DELAY
}

enum ModalState {
    CLOSED, OPENING, OPEN
}

@Component({
    moduleId: module.id.toString(),
    selector: 'operation-monitor',
    templateUrl: 'operation-monitor.component.html'
})
export class OperationMonitor {
    loadingModalRef: NgbModalRef;
    private delayTimer: any;
    private modalTimer: any;
    private delayState: DelayState = DelayState.IDLE;
    private modalState: ModalState = ModalState.CLOSED;

    private _isLoading: boolean;

    public set isLoading(isLoading: boolean) {
        this._isLoading = isLoading;
    }

    public get isLoading(): boolean {
        return this._isLoading;
    }

    constructor(
        private viewController: ViewControllerService,
        private changeDetectorRef: ChangeDetectorRef,
        private loadingModal: LoadingModalService,
        private operationMonitorService: OperationMonitorService) {

        this.operationMonitorService.stateChanged
            .subscribe(() => this.onInstant());
    }

    private onInstant(): void {
        if (this.delayState === DelayState.IDLE) {
            if (this.operationMonitorService.hasActiveOperation) {
                this.delayState = DelayState.START;
                this.setModalOpening();
                this.setIsLoading(true);
            }
        } else if (this.delayState === DelayState.START) {
            if (!this.operationMonitorService.hasActiveOperation) {
                this.delayState = DelayState.DELAY;
                this.delayTimer = setTimeout(() => {
                    this.delayState = DelayState.IDLE;
                    this.setModalClosed();
                    this.setIsLoading(false);
                }, Config.LOADING_DEBOUNCE_DELAY);
            }
        } else if (this.delayState === DelayState.DELAY) {
            if (this.operationMonitorService.hasActiveOperation) {
                this.delayState = DelayState.START;
                clearTimeout(this.delayTimer);
            }
        }
    }

    private setModalOpening() {
        if (this.modalState === ModalState.CLOSED) {
            this.modalState = ModalState.OPENING;
            this.modalTimer = setTimeout(() => {
                this.loadingModal.open();
                this.modalState = ModalState.OPEN;
            }, Config.LOADING_MODAL_DELAY);
        }
    }

    private setModalClosed() {
        if (this.modalState === ModalState.OPENING) {
            clearTimeout(this.modalTimer);
        }
        this.loadingModal.close();
        this.modalState = ModalState.CLOSED;
    }

    private setIsLoading(isLoading: boolean) {
        this.changeDetectorRef.detectChanges();
        this.isLoading = isLoading;
        this.changeDetectorRef.detectChanges();
    }

    public toggleLoggingView(): void {
        this.viewController.loggingOutputShown = !this.viewController.loggingOutputShown;
    }
}
