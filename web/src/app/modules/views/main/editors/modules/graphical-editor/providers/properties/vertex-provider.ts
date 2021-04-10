import * as he from 'he';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { ProcessNode } from 'src/app/model/ProcessNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { IContainer } from '../../../../../../../../model/IContainer';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { Type } from '../../../../../../../../util/type';
import { EditorStyle } from '../../components/editor-components/editor-style';
import { StyleChanger } from '../../components/util/style-changer';
import { ConverterBase } from '../../converters/converter-base';
import { ChangeGuardService } from '../../services/change-guard.service';
import { CEGmxModelLinkedNode } from './ceg-mx-model-linked-node';
import { CEGmxModelNode } from './ceg-mx-model-node';
import { ProviderBase } from './provider-base';
import { ShapeProvider } from './shape-provider';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export class VertexProvider extends ProviderBase {

    public static ID_VARIABLE = 'variable';
    public static ID_CONDITION = 'condition';

    constructor(element: IContainer,
        private graph: mxgraph.mxGraph,
        private shapeProvider: ShapeProvider,
        private nodeNameConverter: ConverterBase<any, CEGmxModelNode | CEGmxModelLinkedNode | string>,
        private dataService: SpecmateDataService) {
        super(element);
    }

    public async provideVertex(node: IModelNode, x?: number, y?: number): Promise<mxgraph.mxCell> {
        const width = node.width > 0 ? node.width : this.shapeProvider.getInitialSize(node).width;
        const height = node.height > 0 ? node.height : this.shapeProvider.getInitialSize(node).height;

        let value = (this.nodeNameConverter ? this.nodeNameConverter.convertTo(node) : node.name);
        if (Type.is(node, CEGLinkedNode)) {
            let n = node as CEGLinkedNode;
            let linkedNode = undefined;
            let variable = '';
            let condition = '';
            let type = '';
            if (n.linkTo !== undefined) {
                linkedNode = await this.dataService.readElement(n.linkTo.url) as CEGNode;
                if (linkedNode !== undefined) {
                    variable = linkedNode.variable;
                    condition = linkedNode.condition;
                    type = linkedNode.type;
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

    public static adjustChildrenPositions(cell: mxgraph.mxCell) {
        const g = cell.getGeometry();
        let parentWidth = g.width;
        if (cell.children !== undefined && cell.children !== null) {
            for (const child of cell.children) {
                const childGeometry = child.getGeometry();
                let x = (parentWidth - childGeometry.width) / 2 / parentWidth;
                child.getGeometry().setRect(x, childGeometry.y, childGeometry.width, childGeometry.height);
            }
        }
    }

    public static adjustChildrenCellSizes(cell: mxgraph.mxCell, shapeProvider: ShapeProvider, graph: mxgraph.mxGraph) {
        const g = cell.getGeometry();
        let parentWidth = g.width;
        if (cell.children !== undefined && cell.children !== null) {
            for (const child of cell.children) {
                const childGeometry = child.getGeometry();
                let preferredSize = graph.getPreferredSizeForCell(child);
                let shapedata = shapeProvider.getInitialData(cell.style.split(';')[0]);
                let widthMax = parentWidth - shapedata.size.margin;
                let width = Math.min(preferredSize.width, widthMax);
                child.getGeometry().setRect(childGeometry.x, childGeometry.y, width, childGeometry.height);
            }
        }
        VertexProvider.adjustChildrenPositions(cell);
    }

    public initCEGVertexRenderer() {
        this.graph.getLabel = (cell: mxgraph.mxCell): any => {
            if (cell.isVertex()) {
                let value = cell.value;
                let table = document.createElement('table');
                table.style.height = '100%';
                table.style.width = '100%';

                let body = document.createElement('tbody');
                let tr1 = document.createElement('tr');
                let tdIcons = document.createElement('td');
                tdIcons.rowSpan = 2;
                tdIcons.style.width = '15px';

                let td1 = document.createElement('td');
                td1.style.textAlign = 'center';
                td1.style.fontSize = '11px';
                td1.style.color = '#774400';
                td1.style.fontWeight = 'bold';

                let tr2 = document.createElement('tr');
                let td2 = document.createElement('td');
                td2.style.textAlign = 'center';
                td2.style.fontSize = '11px';
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

                table.appendChild(body);
                return table;
            }
            return '';
        };
    }

    private createCEGNodeRendering(cell: mxgraph.mxCell,
        body: HTMLTableSectionElement, td1: HTMLTableDataCellElement, td2: HTMLTableDataCellElement) {
        td1.setAttribute('data-node', VertexProvider.ID_VARIABLE);
        if (cell.value.variable === '') {
            mx.mxUtils.write(td1, '[type here]');
            td1.style.fontStyle = 'italic';
            td1.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td1, cell.value.variable);
        }

        td2.setAttribute('data-node', VertexProvider.ID_CONDITION);
        if (cell.value.condition === '') {
            mx.mxUtils.write(td2, '[type here]');
            td2.style.fontStyle = 'italic';
            td2.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td2, cell.value.condition);
        }

        if (cell.edges !== null) {
            let incomingEdges = cell.edges.filter(e => e.target === cell);
            if (incomingEdges.length > 1) {
                let tr3 = document.createElement('tr');
                let dropdown = document.createElement('select');
                dropdown.style.fontSize = '11px';
                let options = ['AND', 'OR'];
                let optionElements: HTMLOptionElement[] = [];
                for (const option of options) {
                    let optionElem = document.createElement('option');
                    optionElem.innerHTML = option;
                    optionElem.setAttribute('value', option);
                    if (option === cell.value.type) {
                        optionElem.setAttribute('selected', 'true');
                        dropdown.value = option;
                    }
                    dropdown.appendChild(optionElem);
                    optionElements.push(optionElem);
                }
                tr3.appendChild(dropdown);
                body.appendChild(tr3);

                mx.mxEvent.addListener(dropdown, 'change', async (evt: mxgraph.mxEventObject) => {
                    cell.value.type = dropdown.value;
                    let newValue = mx.mxUtils.clone(cell.value);
                    newValue.type = dropdown.value;
                    this.graph.getModel().setValue(cell, newValue);
                });

            }
        }
    }

    private createCEGLinkedNodeRendering(cell: mxgraph.mxCell, td1: HTMLTableDataCellElement, td2: HTMLTableDataCellElement) {
        if (cell.value.variable === '') {
            // TODO Text ersetzen
            mx.mxUtils.write(td1, '[No node linked]');
            td1.style.fontStyle = 'italic';
            td1.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td1, cell.value.variable);
        }
        if (cell.value.condition === '') {
            mx.mxUtils.write(td2, '[or empty node]');
            td2.style.fontStyle = 'italic';
            td2.style.opacity = '50%';
        } else {
            mx.mxUtils.write(td2, cell.value.condition);
        }
    }

    private createCEGIconsRendering(cell: mxgraph.mxCell, tdIcons: HTMLTableDataCellElement) {
        // TODO besser Icons für Cause/ Effect /Inner node finden
        if (cell.edges != null && cell.edges.length > 0) {
            let incomingEdges = cell.edges.filter(e => e.target === cell);
            let outgoingEdges = cell.edges.filter(e => e.source === cell);
            if (incomingEdges.length > 0 && outgoingEdges.length > 0) {
                let icon = document.createElement('i');
                icon.className = 'fa fa-battery-half';
                icon.setAttribute('aria-hidden', 'true');
                tdIcons.appendChild(icon);
            } else if (incomingEdges.length > 0) {
                let icon = document.createElement('i');
                icon.className = 'fa fa-battery-full';
                icon.setAttribute('aria-hidden', 'true');
                tdIcons.appendChild(icon);
            } else if (outgoingEdges.length > 0) {
                let icon = document.createElement('i');
                icon.className = 'fa fa-battery-empty';
                icon.setAttribute('aria-hidden', 'true');
                tdIcons.appendChild(icon);
            }
        } else {
            let icon = document.createElement('i');
            icon.className = 'fa fa-exclamation-circle';
            icon.setAttribute('aria-hidden', 'true');
            tdIcons.appendChild(icon);
        }
    }
}
