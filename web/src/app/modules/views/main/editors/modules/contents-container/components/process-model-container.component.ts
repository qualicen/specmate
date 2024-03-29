import { Component, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ModelFactoryBase } from '../../../../../../../factory/model-factory-base';
import { ProcessFactory } from '../../../../../../../factory/process-factory';
import { IContainer } from '../../../../../../../model/IContainer';
import { Process } from '../../../../../../../model/Process';
import { Id } from '../../../../../../../util/id';
import { Type } from '../../../../../../../util/type';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from '../../../../../../notification/modules/modals/services/confirmation-modal.service';
import { AdditionalInformationService } from '../../../../../side/modules/links-actions/services/additional-information.service';
import { ClipboardService } from '../../tool-pallette/services/clipboard-service';
import { TestSpecificationContentContainerBase } from '../base/testspecification-generatable-content-container-base';
import { ContentsContainerService } from '../services/content-container.service';
import { GraphicalEditorService } from '../../graphical-editor/services/graphical-editor.service';
import { ModelImageService } from '../../graphical-editor/services/model-image.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'process-model-container',
    templateUrl: 'process-model-container.component.html',
    styleUrls: ['process-model-container.component.css']
})

export class ProcessModelContainer extends TestSpecificationContentContainerBase<Process> {

    constructor(dataService: SpecmateDataService,
        navigator: NavigatorService,
        translate: TranslateService,
        modal: ConfirmationModal,
        additionalInformationService: AdditionalInformationService,
        contentService: ContentsContainerService,
        clipboardService: ClipboardService,
        graphicalEditorService: GraphicalEditorService,
        protected modelImageService: ModelImageService) {
        super(dataService, navigator, translate, modal, clipboardService,
            contentService, additionalInformationService, graphicalEditorService, modelImageService);
    }

    public get parent(): IContainer {
        return this._parent;
    }

    @Input()
    public set parent(parent: IContainer) {
        super.setParent(parent);
    }

    protected condition = (element: IContainer) => Type.is(element, Process);

    public async createElement(name: string): Promise<Process> {
        let factory: ModelFactoryBase = new ProcessFactory(this.dataService);
        const element = await factory.create(this.parent, true, Id.uuid, name);
        return element as Process;
    }

}
