import { TranslateService } from '@ngx-translate/core';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { IContainer } from '../../../../../../../../model/IContainer';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { Type } from '../../../../../../../../util/type';
import { EditorStyle } from '../../components/editor-components/editor-style';
import { GraphicalEditor } from '../../components/graphical-editor.component';
import { CEGNodeTypeUtil } from '../../components/util/ceg-node-type-util';
import { StyleChanger } from '../../components/util/style-changer';
import { ConverterBase } from '../../converters/converter-base';
import { CEGmxModelLinkedNode } from './ceg-mx-model-linked-node';
import { CEGmxModelNode } from './ceg-mx-model-node';
import { ProviderBase } from './provider-base';
import { ShapeProvider } from './shape-provider';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export enum NodeType { CAUSE, INNER, EFFECT }

export class VertexProvider extends ProviderBase {

    public static ID_VARIABLE = 'variable';
    public static ID_CONDITION = 'condition';

    constructor(element: IContainer,
        private graph: mxgraph.mxGraph,
        private shapeProvider: ShapeProvider,
        private nodeNameConverter: ConverterBase<any, CEGmxModelNode | CEGmxModelLinkedNode | string>,
        private dataService: SpecmateDataService,
        private translate: TranslateService) {
        super(element);
    }

    public async provideVertex(node: IModelNode, x?: number, y?: number): Promise<mxgraph.mxCell> {
        const width = node.width > 0 ? node.width : this.shapeProvider.getInitialSize(node).width;
        const height = node.height > 0 ? node.height : this.shapeProvider.getInitialSize(node).height;

        let value = (this.nodeNameConverter ? this.nodeNameConverter.convertTo(node) : node.name);
        if (Type.is(node, CEGLinkedNode)) {
            let n = node as CEGLinkedNode;
            let linkedNode = undefined;
            let variable = undefined;
            let condition = undefined;
            if (n.linkTo !== undefined) {
                linkedNode = await this.dataService.readElement(n.linkTo.url) as CEGNode;
                if (linkedNode !== undefined) {
                    variable = linkedNode.variable;
                    condition = linkedNode.condition;
                }
            }
            const data = new CEGmxModelLinkedNode(variable, condition);
            value = data;
        }

        const style = this.shapeProvider.getStyle(node);
        const parent = this.graph.getDefaultParent();
        const vertex = this.graph.insertVertex(parent, node.url, value, x || node.x, y || node.y, width, height, style);
        return vertex;
    }

    public initCEGVertexRenderer() {
        this.graph.getLabel = (cell: mxgraph.mxCell): any => {
            if (cell.isVertex()) {
                let value = cell.value;
                let table = document.createElement('table');
                table.id = cell.getId();
                table.style.height = '100%';
                table.style.width = '100%';

                let body = document.createElement('tbody');
                let tr1 = document.createElement('tr');
                let tdIcons = document.createElement('td');
                tdIcons.rowSpan = 2;
                tdIcons.style.width = '15px';

                let td1 = document.createElement('td');
                td1.style.textAlign = 'center';
                td1.style.color = '#774400';
                td1.style.fontWeight = 'bold';

                let tr2 = document.createElement('tr');
                let td2 = document.createElement('td');
                td2.style.textAlign = 'center';
                td2.style.color = '#774400';

                tr1.appendChild(tdIcons);
                tr1.appendChild(td1);
                tr2.appendChild(td2);
                body.appendChild(tr1);
                body.appendChild(tr2);
                if (value instanceof CEGmxModelLinkedNode) {
                    this.createCEGLinkedNodeRendering(cell, td1, td2);
                    mx.mxUtils.writeln(tdIcons, '🔗');
                } else if (value instanceof CEGmxModelNode) {
                    this.createCEGNodeRendering(cell, body, td1, td2);
                    tdIcons.rowSpan = 3;
                }
                this.createCEGIconsRendering(cell, tdIcons);
                this.createCEGColorRendering(cell);

                table.appendChild(body);
                return table;
            }
            return '';
        };
    }

    private createCEGNodeRendering(cell: mxgraph.mxCell,
        body: HTMLTableSectionElement, td1: HTMLTableDataCellElement, td2: HTMLTableDataCellElement) {
        let value = cell.value as CEGmxModelNode;
        td1.setAttribute('data-node', VertexProvider.ID_VARIABLE);
        if (value.variable === '') {
            mx.mxUtils.write(td1, '[' + this.translate.instant('typeHere') + ']');
            td1.style.fontStyle = 'italic';
            td1.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td1, value.variable);
        }

        td2.setAttribute('data-node', VertexProvider.ID_CONDITION);
        if (value.condition === '') {
            mx.mxUtils.write(td2, '[' + this.translate.instant('typeHere') + ']');
            td2.style.fontStyle = 'italic';
            td2.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td2, value.condition);
        }

        if (cell.edges !== null) {
            let incomingEdges = cell.edges.filter(e => e.target === cell);
            if (incomingEdges.length > 1) {
                let tr3 = document.createElement('tr');
                let dropdown = document.createElement('select');
                let options = ['AND', 'OR'];
                let optionElements: HTMLOptionElement[] = [];
                for (const option of options) {
                    let optionElem = document.createElement('option');
                    optionElem.innerHTML = this.translate.instant(option);
                    optionElem.setAttribute('value', option);
                    if (option === value.type) {
                        optionElem.setAttribute('selected', 'true');
                        dropdown.value = option;
                    }
                    dropdown.appendChild(optionElem);
                    optionElements.push(optionElem);
                }
                tr3.appendChild(dropdown);
                body.appendChild(tr3);

                mx.mxEvent.addListener(dropdown, 'change', async (evt: mxgraph.mxEventObject) => {
                    value.type = dropdown.value;
                    let newValue = mx.mxUtils.clone(value);
                    newValue.type = dropdown.value;
                    this.graph.getModel().setValue(cell, newValue);
                });

            }
        }
    }

    private createCEGLinkedNodeRendering(cell: mxgraph.mxCell, td1: HTMLTableDataCellElement, td2: HTMLTableDataCellElement) {
        let value = cell.value as CEGmxModelLinkedNode;
        if (value.variable === undefined || value.condition === undefined) {
            mx.mxUtils.write(td1, '[' + this.translate.instant('hintAddLinkedNode') + ']');
            td1.style.fontStyle = 'italic';
            td1.style.opacity = '50%';

        } else {
            mx.mxUtils.write(td1, value.variable);
            mx.mxUtils.write(td2, value.condition);
        }
    }

    private createCEGIconsRendering(cell: mxgraph.mxCell, tdIcons: HTMLTableDataCellElement) {
        if (cell.edges != null && cell.edges.length > 0) {
            let incomingEdges = cell.edges.filter(e => e.target === cell);
            let outgoingEdges = cell.edges.filter(e => e.source === cell);
            if (incomingEdges.length > 0 && outgoingEdges.length > 0) {
                tdIcons.appendChild(this.createNodeIcon(NodeType.INNER));
            } else if (incomingEdges.length > 0) {
                tdIcons.appendChild(this.createNodeIcon(NodeType.EFFECT));
            } else if (outgoingEdges.length > 0) {
                tdIcons.appendChild(this.createNodeIcon(NodeType.CAUSE));
            }
        } else {
            let icon = document.createElement('i');
            icon.className = 'fa fa-exclamation-circle';
            icon.setAttribute('aria-hidden', 'true');
            tdIcons.appendChild(icon);
        }
    }
    private createCEGColorRendering(cell: mxgraph.mxCell) {
        let nodeType = CEGNodeTypeUtil.getCEGNodeType(cell);
        if (!cell.style.match(new RegExp(';*' + nodeType + ';*'))) {
            StyleChanger.removeStyle(cell, this.graph, EditorStyle.CAUSE_STYLE_NAME);
            StyleChanger.removeStyle(cell, this.graph, EditorStyle.EFFECT_STYLE_NAME);
            StyleChanger.removeStyle(cell, this.graph, EditorStyle.INNER_STYLE_NAME);
            StyleChanger.addStyle(cell, this.graph, nodeType);
        }
    }

    private createNodeIcon(type: NodeType): HTMLElement {
        let container = document.createElement('span');
        container.className = 'fa-stack';
        container.style.fontWeight = 'bold';
        container.setAttribute('aria-hidden', 'true');

        let box = document.createElement('i');
        box.className = 'fa fa-square-o fa-stack-1x';
        box.style.fontSize = '1.6em';
        box.style.fontWeight = 'bold';

        let arrow = document.createElement('i');
        arrow.className = 'fa fa-long-arrow-right fa-stack-1x';
        arrow.style.top = '-1px';
        arrow.style.fontWeight = 'bold';
        arrow.style.fontSize = '1.1em';
        container.appendChild(box);
        container.appendChild(arrow);

        if (type === NodeType.EFFECT) {
            arrow.style.left = '-7px';
        }
        if (type === NodeType.CAUSE) {
            arrow.style.left = '12px';
        }
        if (type === NodeType.INNER) {
            arrow.style.left = '-7px';
            let arrowRight = document.createElement('i');
            arrowRight.className = 'fa fa-long-arrow-right fa-stack-1x';
            arrowRight.style.top = '-1px';
            arrowRight.style.left = '12px';
            arrowRight.style.fontWeight = 'bold';
            arrowRight.style.fontSize = '1.1em';
            container.appendChild(arrowRight);
        }
        return container;
    }

    public initProcessVertexRenderer() {
        this.graph.getLabel = (cell: mxgraph.mxCell): any => {
            let div = document.createElement('div');
            div.id = cell.getId();
            mx.mxUtils.write(div, cell.value);
            return div;
        };
    }
}
