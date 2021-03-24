import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { IContainer } from '../../../../../../../model/IContainer';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { SelectedElementService } from '../../../../../side/modules/selected-element/services/selected-element.service';
import { ToolProvider } from '../../graphical-editor/providers/properties/tool-provider';
import { ToolBase } from '../tools/tool-base';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { ModalService } from 'src/app/modules/notification/modules/modals/services/modal-service';

@Injectable()
export class EditorToolsService {
    public activeTool: ToolBase;

    private model: IContainer;

    private providerMap: { [url: string]: ToolProvider };

    private toolDOMElements: { [name: string]: HTMLElement } = {};

    constructor(private dataService: SpecmateDataService,
        private navigator: NavigatorService,
        private selectedElementService: SelectedElementService,
        private translate: TranslateService,
        private modal: ConfirmationModal,
        private modalService: ModalService) {
        this.init(this.navigator.currentElement);
        this.navigator.hasNavigated.subscribe((model: IContainer) => this.init(model));
    }

    public init(model: IContainer): void {
        if (!model) {
            return;
        }
        this.model = model;
    }

    public get toolProvider(): ToolProvider {
        if (!this.model) {
            return undefined;
        }
        if (!this.providerMap) {
            this.providerMap = {};
        }
        if (!this.providerMap[this.model.url]) {
            this.providerMap[this.model.url] = new ToolProvider(this.model, this.dataService,
                this.selectedElementService, this.modal, this.translate, this.modalService);
        }
        return this.providerMap[this.model.url];
    }

    public get tools(): ToolBase[] {
        if (!this.toolProvider) {
            return undefined;
        }
        return this.toolProvider.tools;
    }

    public addDOMElement(tool: ToolBase, domElement: HTMLElement): void {
        this.toolDOMElements[tool.name + '-' + tool.parentName] = domElement;
    }

    public getDOMElement(tool: ToolBase): HTMLElement {
        return this.toolDOMElements[tool.name + '-' + tool.parentName];
    }
}
