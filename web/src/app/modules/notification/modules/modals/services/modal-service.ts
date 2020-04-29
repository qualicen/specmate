import { NgbModalOptions, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Injectable } from '@angular/core';

@Injectable()
export class ModalService {

    constructor(private ngbModalService: NgbModal) {
    }

    public open<T>(content: T, options?: NgbModalOptions): NgbModalRef {
        // Remove the focus of the current element, because this leads to errors in combination with forms
        (document.activeElement as HTMLElement).blur();
        return this.ngbModalService.open(content, options);
    }
    public dismissAll(reason?: any): void {
        return this.ngbModalService.dismissAll(reason);
    }
    public hasOpenModals(): boolean {
        return this.ngbModalService.hasOpenModals();
    }
}
