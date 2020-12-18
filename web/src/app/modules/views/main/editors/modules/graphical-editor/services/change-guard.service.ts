import { Injectable } from '@angular/core';
import { CEGNode } from 'src/app/model/CEGNode';
import { IContainer } from 'src/app/model/IContainer';
import { NavigatorService } from 'src/app/modules/navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { Type } from 'src/app/util/type';

@Injectable()
export class ChangeGuardService {

    private clearedElementUrls: string[] = [];

    constructor(private modal: ConfirmationModal, private navigator: NavigatorService) {
        navigator.navigationStart.subscribe(() => this.reset());
    }

    public async guardSelectedElements(elements: IContainer[]): Promise<boolean> {
        for (const element of elements.filter(e => this.isGuarded(e))) {
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
                this.clear(element);
                return true;
            } catch {
                return false;
            }
        }
        return true;
    }

    private isCleared(element: IContainer): boolean {
        return this.clearedElementUrls.find(clearedElementUrl => clearedElementUrl === element.url) !== undefined;
    }

    private clear(element: IContainer) {
        if (!this.isCleared(element)) {
            this.clearedElementUrls.push(element.url);
        }
    }

    private isGuarded(element: IContainer): boolean {
        if (Type.is(element, CEGNode)) {
            return (element as CEGNode).linksFrom?.length > 0;
        }
        return false;
    }

    private reset(): void {
        this.clearedElementUrls = [];
    }
}
