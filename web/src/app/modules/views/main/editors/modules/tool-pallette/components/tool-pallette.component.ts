import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { IContainer } from '../../../../../../../model/IContainer';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from '../../../../../../notification/modules/modals/services/confirmation-modal.service';
import { SelectedElementService } from '../../../../../side/modules/selected-element/services/selected-element.service';
import { ElementProvider } from '../../graphical-editor/providers/properties/element-provider';
import { EditorToolsService } from '../services/editor-tools.service';
import { ToolBase } from '../tools/tool-base';

@Component({
    moduleId: module.id.toString(),
    selector: 'tool-pallette',
    templateUrl: 'tool-pallette.component.html',
    styleUrls: ['tool-pallette.component.css']
})
export class ToolPallette {

    constructor(private editorToolsService: EditorToolsService) { }

    public get tools(): ToolBase[] {
        return this.editorToolsService.tools.filter(tool => tool.isVertexTool === true);
    }

    public get isVisible(): boolean {
        return this.tools && this.tools.length > 0;
    }
}
