import { Component } from '@angular/core';
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
        return this.editorToolsService.tools.filter(tool => !tool.isHidden);
    }

    public get isVisible(): boolean {
        return this.tools && this.tools.length > 0;
    }
}
