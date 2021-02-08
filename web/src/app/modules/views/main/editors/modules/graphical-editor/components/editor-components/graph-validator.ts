import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { CEGModel } from 'src/app/model/CEGModel';
import { CEGNode } from 'src/app/model/CEGNode';
import { IContainer } from 'src/app/model/IContainer';
import { ProcessDecision } from 'src/app/model/ProcessDecision';
import { ProcessEnd } from 'src/app/model/ProcessEnd';
import { ProcessStart } from 'src/app/model/ProcessStart';
import { ProcessStep } from 'src/app/model/ProcessStep';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';
import { Type } from 'src/app/util/type';
import { GraphicalEditorService } from '../../services/graphical-editor.service';
import { GraphicalEditor } from '../graphical-editor.component';
import { StyleChanger } from '../util/style-changer';
import { EditorStyle } from './editor-style';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export class GraphValidator {


    constructor(private validationService: ValidationService,
        private graphicalEditorService: GraphicalEditorService) {
    }

    public updateValidities(graph: mxgraph.mxGraph, model: IContainer) {
        if (graph === undefined) {
            return;
        }

        const vertices = graph.getChildCells(graph.getDefaultParent());
        if (vertices.length > 1) {
            this.graphicalEditorService.start(GraphicalEditorService.OP_VALIDATION);
        }

        const validationResult = this.validationService.getValidationResultsGrouped(model);
        for (let i = 0; i < vertices.length; i++) {
            let vertex = vertices[i];
            let resultItem = validationResult.find(element => element.element.url === vertex.id);
            if (resultItem !== undefined) {
                let validationMessage = resultItem.messages.reduce((a, b) => a + '<br>' + b);
                let overlays = graph.getCellOverlays(vertex);
                if (overlays != undefined && overlays.length === 1) {
                    let overlay = overlays[0];
                    if (validationMessage !== overlay.toString()) {
                        overlay.tooltip = validationMessage;
                    }
                } else {
                    StyleChanger.addStyle(vertex, graph, EditorStyle.INVALID_STYLE_NAME);
                    graph.addCellOverlay(vertex, this.createOverlay(graph, resultItem.element, validationMessage));
                }
            } else {
                StyleChanger.removeStyle(vertex, graph, EditorStyle.INVALID_STYLE_NAME);
                graph.removeCellOverlays(vertex);
            }

            if (Type.is(model, CEGModel)) {
                let nodeType = GraphicalEditor.getCEGNodeType(vertex);
                if (!vertex.style.match(new RegExp(';*' + nodeType + ';*'))) {
                    StyleChanger.removeStyle(vertex, graph, EditorStyle.CAUSE_STYLE_NAME);
                    StyleChanger.removeStyle(vertex, graph, EditorStyle.EFFECT_STYLE_NAME);
                    StyleChanger.removeStyle(vertex, graph, EditorStyle.INNER_STYLE_NAME);
                    StyleChanger.addStyle(vertex, graph, nodeType);
                }
            }
        }
        this.graphicalEditorService.end(GraphicalEditorService.OP_VALIDATION);
    }

    private createOverlay(graph: mxgraph.mxGraph, element: IContainer, message: string) {
        let align = mx.mxConstants.ALIGN_RIGHT;
        let offset;

        if (Type.is(element, CEGNode) || Type.is(element, ProcessStep)) {
            offset = new mx.mxPoint(-13, -12);
        }
        if (Type.is(element, ProcessStart) || Type.is(element, ProcessEnd)) {
            align = mx.mxConstants.ALIGN_CENTER;
            offset = new mx.mxPoint(0, -13);
        }
        if (Type.is(element, ProcessDecision)) {
            align = mx.mxConstants.ALIGN_CENTER;
            offset = new mx.mxPoint(0, -18);
        }
        return new mx.mxCellOverlay(graph.warningImage, message, align, undefined, offset);
    }
}
