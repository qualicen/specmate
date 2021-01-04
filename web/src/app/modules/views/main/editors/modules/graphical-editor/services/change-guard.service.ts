import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { CEGNode } from 'src/app/model/CEGNode';
import { IContainer } from 'src/app/model/IContainer';
import { NavigatorService } from 'src/app/modules/navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { Type } from 'src/app/util/type';

@Injectable()
export class ChangeGuardService {

    private clearedElementUrls: string[] = [];

    constructor(private modal: ConfirmationModal, private translate: TranslateService, navigator: NavigatorService) {
        navigator.navigationStart.subscribe(() => this.reset());
    }

    public async guardSelectedElements(elements: IContainer[]): Promise<boolean> {
        let result = true;
        try {
            const guardedElements = elements.filter(e => this.isGuarded(e));
            result = await this.guardElements(guardedElements);
        } catch {
            result = false;
        }
        return result;
    }

    private async guardElements(elements: IContainer[]): Promise<boolean> {
        const unclearedElements = elements.filter(element => !this.isCleared(element));
        if (unclearedElements.length > 0) {
            try {
                await this.modal.confirmChange('ConfirmationRequired', this.getGuardMessage(unclearedElements));
                this.clear(elements);
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

    private clear(elements: IContainer[]) {
        for (const element of elements) {
            if (!this.isCleared(element)) {
                this.clearedElementUrls.push(element.url);
            }
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

    private getGuardMessage(elements: IContainer[]): string {
        let names = elements.map(element => this.getName(element)).join('\n');
        return this.translate.instant('Node') + ':\n' + names + '\n\n' + this.translate.instant('ChangeLinkedNode');
    }

    private getName(element: IContainer): string {
        if (Type.is(element, CEGNode)) {
            const node = element as CEGNode;
            return node.variable + ' ' + node.condition;
        }
        return element.name;
    }
}
