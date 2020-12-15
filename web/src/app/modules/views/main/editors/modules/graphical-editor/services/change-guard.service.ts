import { Injectable } from '@angular/core';
import { IContainer } from 'src/app/model/IContainer';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';

@Injectable()
export class ChangeGuardService {

    private clearedElements: IContainer[] = [];

    constructor(private modal: ConfirmationModal) { }

    public async guardSelectedElements(elements: IContainer[]): Promise<boolean> {
        for (const element of elements) {
            try {
                const result = await this.guardElement(element);
                if (result === false) {
                    return false;
                }
            } catch {
                return false;
            }
        }
        return true;
    }

    private async guardElement(element: IContainer): Promise<boolean> {
        if (!this.isCleared(element)) {
            try {
                await this.modal.confirmChange('Change', 'JKAHDFKJHDAF');
                return true;
            } catch {
                return false;
            }
        }
        return true;
    }

    private isCleared(element: IContainer): boolean {
        return this.clearedElements.find(clearedElement => clearedElement.url === element.url) !== undefined;
    }
}
