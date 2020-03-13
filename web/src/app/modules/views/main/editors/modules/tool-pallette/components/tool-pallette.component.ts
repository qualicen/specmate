import { AfterViewChecked, Component } from '@angular/core';
import { EditorToolsService } from '../services/editor-tools.service';
import { ToolBase } from '../tools/tool-base';

@Component({
    moduleId: module.id.toString(),
    selector: 'tool-pallette',
    templateUrl: 'tool-pallette.component.html',
    styleUrls: ['tool-pallette.component.css']
})
export class ToolPallette implements AfterViewChecked {

    constructor(private editorToolsService: EditorToolsService) { }

    public ngAfterViewChecked() {
        this.tools.forEach(tool => this.editorToolsService.addDOMElement(tool, document.getElementById(tool.elementId)));
    }

    public get tools(): ToolBase[] {
        if (this.editorToolsService.tools === undefined) {
            return [];
        }
        return this.editorToolsService.tools.filter(tool => !tool.isHidden);
    }

    public get isVisible(): boolean {
        return this.tools.length > 0;
    }

    public getClasses(tool: ToolBase): string[] {
        const classes = ['tool'];
        if (tool.isDragTool) {
            classes.push('drag-tool');
        } else if (tool.isClickTool) {
            classes.push('click-tool');
        }
        return classes;
    }
}
