import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { ViewControllerService } from '../../../../views/controller/modules/view-controller/services/view-controller.service';
import { LoadingModalService } from '../../modals/services/loading-model-service';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'operation-monitor',
    templateUrl: 'operation-monitor.component.html'
})
export class OperationMonitor implements OnDestroy {
    loadingModalRef: NgbModalRef;

    ngOnDestroy(): void {
        this.dataServiceSubscription.unsubscribe();
        if (this.changeDetectorRef) {
            this.changeDetectorRef.detach();
        }
    }

    public isLoading: boolean;
    private dataServiceSubscription: any;

    public get taskName(): string {
        return this.dataService.currentTaskName;
    }

    constructor(
        private dataService:
            SpecmateDataService,
        private viewController: ViewControllerService,
        private changeDetectorRef: ChangeDetectorRef,
        private loadingModal: LoadingModalService,
        private validationService: ValidationService) {

        this.isLoading = this.dataService.isLoading;
        this.dataServiceSubscription =
            // show loading modal after 1,5 seconds
            this.dataService.stateChanged.pipe().debounceTime(1500).subscribe(() => {
                if (this.dataService.isLoading) {
                    this.loadingModal.open();
                } else {
                    this.loadingModal.close();
                }
                this.changeDetectorRef.detectChanges();
                this.isLoading = this.dataService.isLoading;
                this.changeDetectorRef.detectChanges();
            });

        validationService.stateChanged.pipe().debounceTime(300).subscribe(() => {
            if (validationService.isValidating) {
                this.loadingModal.open();
            } else {
                this.loadingModal.close();
            }
            this.changeDetectorRef.detectChanges();
                this.isLoading = this.validationService.isValidating;
                this.changeDetectorRef.detectChanges();
        });
    }

    public toggleLoggingView(): void {
        this.viewController.loggingOutputShown = !this.viewController.loggingOutputShown;
    }
}
