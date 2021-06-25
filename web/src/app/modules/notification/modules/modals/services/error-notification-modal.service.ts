import { Injectable } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { LogElement } from '../../../../views/side/modules/log-list/services/log-element';
import { LoggingService } from '../../../../views/side/modules/log-list/services/logging.service';
import { TypedModalContent } from '../components/typed-modal-content.component';
import { Dialogtype } from '../modal-dialog-type';
import { ModalService } from './modal-service';

@Injectable()
export class ErrorNotificationModalService {

    private isOpen = false;

    constructor(private modalService: ModalService, private logger: LoggingService) {
        this.logger.logEvent.subscribe(logElement => {
            if (logElement.isError) {
                this.open(logElement.message).catch();
            }
        });
    }

    public open(message: string): Promise<any> {
        if (!this.isOpen) {
            this.isOpen = true;
            const modalRef = this.modalService.open(TypedModalContent);
            modalRef.componentInstance.options = Dialogtype.errorDialog(message);
            return modalRef.result.then((result: any) => {
                this.isOpen = false;
                return Promise.resolve(result);
            }).catch((result: any) => {
                this.isOpen = false;
                return Promise.reject(result);
            });
        }
        return Promise.reject('Modal already open.');
    }
}
