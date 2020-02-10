import { Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { TypedModalContent } from '../components/typed-modal-content.component';
import { Dialogtype } from '../modal-dialog-type';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';

@Injectable()
export class ConfirmationModal {
    constructor(private modalService: NgbModal,
        private dataService: SpecmateDataService,
        private translate: TranslateService,
        private validator: ValidationService) { }

    public async openSave(message: string, withCancel = true): Promise<any> {
        const modalRef = this.modalService.open(TypedModalContent);
        await this.validator.validateCurrent();
        if (this.validator.isSavingEnabled()) {
            modalRef.componentInstance.options = Dialogtype.unsavedChangesDialog(message, withCancel);
        } else {
            modalRef.componentInstance.options = Dialogtype.discardCancelDialog(this.translate.instant('saveError.discard'), withCancel);
        }
        return modalRef.result;
    }

    public confirmDelete(title: string, message: string): Promise<any> {
        const modalRef = this.modalService.open(TypedModalContent);
        modalRef.componentInstance.options = Dialogtype.okCancelDialog(title, message);
        return modalRef.result;
    }

    public openOk(title: string, message: string): Promise<any> {
        const modalRef = this.modalService.open(TypedModalContent);
        modalRef.componentInstance.options = Dialogtype.okDialog(title, message);
        return modalRef.result;
    }

    public openOkCancel(title: string, message: string): Promise<any> {
        const modalRef = this.modalService.open(TypedModalContent);
        modalRef.componentInstance.options = Dialogtype.okCancelDialog(title, message);
        return modalRef.result;
    }

    public async confirmSave(message?: string): Promise<void> {
        if (this.dataService.hasCommits) {
            await this.validator.validateCurrent();
            if (this.validator.isSavingEnabled()) {
                return this.openOkCancel('ConfirmationRequired', message || this.translate.instant('confirmSave'));
            } else {
                return this.openOk('saveError.title', this.translate.instant('saveError.continue'));
            }
        }
        return Promise.resolve();
    }
}
