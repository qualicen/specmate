import { Component } from '@angular/core';
import { IContainer } from '../../../../../../model/IContainer';
import { ProcessStep } from '../../../../../../model/ProcessStep';
import { Type } from '../../../../../../util/type';
import { HiddenFieldsProvider } from '../../../../main/editors/modules/graphical-editor/providers/properties/hidden-fields-provider';
import { SelectedElementService } from '../../selected-element/services/selected-element.service';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGNode } from 'src/app/model/CEGNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGModel } from 'src/app/model/CEGModel';
import { Url } from 'src/app/util/url';

@Component({
    moduleId: module.id.toString(),
    selector: 'properties-editor',
    templateUrl: 'properties-editor.component.html',
    styleUrls: ['properties-editor.component.css']
})
export class PropertiesEditor {

    public isCollapsed = false;

    private hiddenFieldsProvider: HiddenFieldsProvider;
    private _selectedElement: IContainer;
    private _linkedNode: CEGNode;
    private _linkedModel: CEGModel;
    private _linkingModels: CEGModel[];

    constructor(selectedElementService: SelectedElementService, private dataService: SpecmateDataService) {
        selectedElementService.selectionChanged.subscribe(async (elements: IContainer[]) => {
            this.onSelectionOrElementChange(elements);
        });

        dataService.elementChanged.subscribe(async (url: string) => {
            if (!this.dataService.hasElement(url)) {
                return;
            }
            const element = await dataService.readElement(url, true);
            this.onSelectionOrElementChange([element]);
        });
    }

    private async onSelectionOrElementChange(elements: IContainer[]) {
        this.hiddenFieldsProvider = new HiddenFieldsProvider(elements[0]);
        this._selectedElement = elements[0];

        this._linkedNode = undefined;
        this._linkingModels = [];
        if (Type.is(this.selectedElement, CEGLinkedNode)) {
            const node = this.selectedElement as CEGLinkedNode;
            if (node.linkTo !== undefined) {
                this._linkedNode = await this.dataService.readElement(node.linkTo.url, true) as CEGNode;
                this._linkedModel = await this.dataService.readElement(Url.parent(this._linkedNode.url), true) as CEGModel;
            }
        }
        if (Type.is(this.selectedElement, CEGNode)) {
            const node = this.selectedElement as CEGNode;
            for (const linkingNodeProxy of node.linksFrom) {
                const parentUrl = Url.parent(linkingNodeProxy.url);
                const linkingModel = await this.dataService.readElement(parentUrl, true) as CEGModel;
                if (linkingModel !== undefined && this._linkingModels.find(model => model.url === linkingModel.url) === undefined) {
                    this._linkingModels.push(linkingModel);
                }
            }
        }
    }

    public get selectedElement(): IContainer {
        return this._selectedElement;
    }

    public get hiddenFields(): string[] {
        if (!this.hiddenFieldsProvider) {
            return undefined;
        }
        return this.hiddenFieldsProvider.hiddenFields;
    }

    public get showTracing(): boolean {
        return Type.is(this.selectedElement, ProcessStep);
    }

    public get showLinkingButton(): boolean {
        return Type.is(this.selectedElement, CEGLinkedNode);
    }

    public get hasLinkedNode(): boolean {
        return this._linkedNode !== undefined && this._linkedModel !== undefined;
    }

    public get linkedModel(): CEGModel {
        return this._linkedModel;
    }

    public get hasLinkingModels(): boolean {
        return this._linkingModels !== undefined && this.linkingModels.length > 0;
    }

    public get linkingModels(): CEGModel[] {
        return this._linkingModels;
    }
}
