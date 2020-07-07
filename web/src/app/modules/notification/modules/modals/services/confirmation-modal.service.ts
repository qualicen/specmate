import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { TypedModalContent } from '../components/typed-modal-content.component';
import { Dialogtype } from '../modal-dialog-type';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';
import { ModalService } from './modal-service';

@Injectable()
export class ConfirmationModal {
    constructor(private modalService: ModalService,
        private dataService: SpecmateDataService,
        private translate: TranslateService,
        private validator: ValidationService) { }

    public async openSave(message: string, withCancel = true, withDiscard = true): Promise<any> {
        const modalRef = this.modalService.open(TypedModalContent);
        await this.validator.validateCurrent();
        if (this.validator.isSavingEnabled()) {
            modalRef.componentInstance.options = Dialogtype.unsavedChangesDialog(message, withCancel, withDiscard);
        } else {
            let displayMessage = this.translate.instant('saveError.discard') + '\n' + this.validator.getValidationResultAsString(true);
            modalRef.componentInstance.options = Dialogtype.discardCancelDialog(displayMessage, withCancel);
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
                return this.openSave(message || this.translate.instant('confirmSave'), true, false);
            } else {
                let displayMessage = this.translate.instant('saveError.continue') + '\n' + this.validator.getValidationResultAsString(true);
                return this.openOk('saveError.title', displayMessage);
            }
        }
        return Promise.resolve();
    }
}
