import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { DragulaService } from 'ng2-dragula';
import { IContainer } from '../../../../../model/IContainer';
import { IContentElement } from '../../../../../model/IContentElement';
import { IPositionable } from '../../../../../model/IPositionable';
import { Sort } from '../../../../../util/sort';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from '../../../../notification/modules/modals/services/confirmation-modal.service';
import { SpecmateViewBase } from './specmate-view-base';


export abstract class DraggableSupportingViewBase extends SpecmateViewBase {

    private element: IContainer;

    protected abstract get relevantElements(): (IContentElement & IPositionable)[];

    protected isDragging = false;

    /** The contents of the test specification */
    protected _contents: IContentElement[];

    public get contents(): IContentElement[] {
        return this._contents;
    }

    public set contents(contents: IContentElement[]) {
        this._contents = contents;
    }

    public get sortedContents(): IContentElement[] {
        return Sort.sortArrayBy(this.relevantElements, 'position');
    }

    public dndBagName = 'DND_BAG';

    constructor(
        dataService: SpecmateDataService,
        navigator: NavigatorService,
        route: ActivatedRoute,
        modal: ConfirmationModal,
        private dragulaService: DragulaService,
        translate: TranslateService) {
        super(dataService, navigator, route, modal, translate);
        this.dragulaService.dropModel(this.dndBagName)
            .subscribe(({ sourceIndex, targetIndex }) => this.onDropModel(sourceIndex, targetIndex));
    }

    private async onDropModel(sourceIndex: number, targetIndex: number): Promise<void> {
        const source = this.relevantElements[sourceIndex];
        const target = this.relevantElements[targetIndex];
        const sourceContentIndex = this.relevantElements.indexOf(source);
        const targetContentIndex = this.relevantElements.indexOf(target);
        this.relevantElements.splice(targetContentIndex, 0, this.relevantElements.splice(sourceContentIndex, 1)[0]);
        await this.sanitizeContentPositions(true);
        await this.readContents();
    }

    protected async sanitizeContentPositions(update: boolean): Promise<void> {
        await this.dataService.sanitizeContentPositions(this.relevantElements, update);
    }

    public async onElementResolved(element: IContainer): Promise<void> {
        this.element = element;
        return await this.readContents();
    }

    protected async onContentsRead(contents: IContainer[]): Promise<void> {
        // empty default implementation
    }

    /** Reads to the contents of the element  */
    public async readContents(): Promise<void> {
        if (!this.element) {
            return Promise.resolve();
        }
        this.contents = await this.dataService.readContents(this.element.url);
        this.onContentsRead(this.contents);
    }

    /** Callback handler for content changes */
    public async updateContents(): Promise<void> {
        await this.readContents();
    }
}
