import { Injectable } from '@angular/core';
import { TypedModalContent } from '../components/typed-modal-content.component';
import { Dialogtype } from '../modal-dialog-type';
import { ModalService } from './modal-service';

@Injectable()
export class SimpleModal {
    constructor(protected modalService: ModalService,
    ) { }

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
}
