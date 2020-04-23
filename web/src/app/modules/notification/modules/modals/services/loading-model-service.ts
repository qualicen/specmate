import { Injectable } from '@angular/core';
import { NgbModalConfig, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { LoggingService } from '../../../../views/side/modules/log-list/services/logging.service';
import { LoadingModalContent } from '../components/loading-modal-content.component';
import { ModalService } from './modal-service';

@Injectable()
export class LoadingModalService {

    private isOpen = false;
    private modalRef: NgbModalRef;

    constructor(private modalService: ModalService, private logger: LoggingService) {
    }

    public open() {
        let x: NgbModalConfig;
        if (!this.isOpen) {
            this.isOpen = true;
            this.modalRef = this.modalService.open(LoadingModalContent, {
                // small size
                size: 'sm',
                // don't allow to hide the modal by clicking the background
                backdrop: 'static',
                // don't allow to hide the modal by hitting ESC
                keyboard: false,
                // center modal vertically
                centered: true
            });

            this.modalRef.result.then((result) => {
                this.isOpen = false;
            }).catch((result) => {
                this.isOpen = false;
            });
        }
    }

    public close() {
        if (this.isOpen) {
            this.modalRef.close();
        }
    }
}
