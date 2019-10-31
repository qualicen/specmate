import { mxgraph } from 'mxgraph';
import { CEGConnection } from 'src/app/model/CEGConnection';
import { CEGNode } from 'src/app/model/CEGNode';
import { Type } from 'src/app/util/type';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { IContainer } from '../../../../../../../../model/IContainer';
import { IModelConnection } from '../../../../../../../../model/IModelConnection';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { Process } from '../../../../../../../../model/Process';
import { Arrays } from '../../../../../../../../util/arrays';
import { Id } from '../../../../../../../../util/id';
import { SpecmateDataService } from '../../../../../../../data/modules/data-service/services/specmate-data.service';
import { ConnectionToolBase } from '../../../tool-pallette/tools/connection-tool-base';
import { CreateNodeToolBase } from '../../../tool-pallette/tools/create-node-tool-base';
import { DeleteToolBase } from '../../../tool-pallette/tools/delete-tool-base';
import { ToolBase } from '../../../tool-pallette/tools/tool-base';
import { ConverterBase } from '../../converters/converter-base';
import { NodeNameConverterProvider } from '../../providers/conversion/node-name-converter-provider';
import { ToolProvider } from '../../providers/properties/tool-provider';
import { ValuePair } from '../../providers/properties/value-pair';
import { EditorStyle } from '../editor-components/editor-style';
import { ShapeProvider } from '../../providers/properties/shape-provider';
import { StyleChanger } from './style-changer';


declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});


export class ChangeTranslator {

    private contents: IContainer[];
    private parentComponents: { [key: string]: IContainer };
    private nodeNameConverter: ConverterBase<IContainer, string | ValuePair>;
    public preventDataUpdates = false;

    constructor(private model: CEGModel | Process,
        private dataService: SpecmateDataService,
        private toolProvider: ToolProvider,
        private shapeProvider: ShapeProvider) {

        this.nodeNameConverter = this.nodeNameConverter = new NodeNameConverterProvider(this.model).nodeNameConverter;
        this.parentComponents = {};
    }

    private async getElement(id: string) {
        let element = this.contents.find(element => element.url === id);
        if (element === undefined) {
            element = this.parentComponents[id];
        }
        return element;
    }

    public async translate(change: (mxgraph.mxTerminalChange | mxgraph.mxChildChange | mxgraph.mxStyleChange)): Promise<void> {
        if (this.preventDataUpdates) {
            return;
        }

        this.contents = await this.dataService.readContents(this.model.url, true);
        if (change['cell'] === undefined && change['child'] !== undefined) {
            const childChange = change as mxgraph.mxChildChange;
            if (childChange.parent !== undefined && childChange.parent !== null) {
                await this.translateAdd(childChange);
            } else {
                await this.translateDelete(childChange);
            }
        } else if (change['style'] !== undefined) {
            await this.translateStyleChange(change as mxgraph.mxStyleChange);
        } else if (change['cell'] !== undefined) {
            await this.translateTerminalChange(change as mxgraph.mxTerminalChange);
        }
    }

    private async translateStyleChange(change: mxgraph.mxStyleChange): Promise<void> {
        const element = await this.getElement(change.cell.id);
        if (element === undefined) {
            return;
        }
        const prevStyles: string[] = change.previous.split(';');
        const currStyles: string[] = change.style.split(';');

        const newStyles = currStyles.filter(style => !Arrays.contains(prevStyles, style));
        const removedStyles = prevStyles.filter(style => !Arrays.contains(currStyles, style));

        if (Type.is(element, CEGConnection)) {
            const connection = (element as CEGConnection);
            let changeMade = false;
            if (Arrays.contains(newStyles, EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE)) {
                connection.negate = true;
                changeMade = true;
            } else if (Arrays.contains(removedStyles, EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE)) {
                connection.negate = false;
                changeMade = true;
            }

            if (changeMade) {
                await this.dataService.updateElement(connection, true, Id.uuid);
            }
        }
    }


    private async translateDelete(change: mxgraph.mxChildChange): Promise<void> {
        const deleteTool = this.toolProvider.tools.find(tool => (tool as DeleteToolBase).isDeleteTool === true) as DeleteToolBase;
        deleteTool.element = await this.getElement(change.child.id);
        if (deleteTool.element === undefined) {
            return;
        }
        let keys = [];
        for (const key in this.parentComponents) {
            if (this.parentComponents.hasOwnProperty(key)) {
                const element = this.parentComponents[key];
                if (element === deleteTool.element) {
                    keys.push(key);
                }
            }
        }
        for (const key of keys) {
            delete this.parentComponents[key];
        }
        deleteTool.perform();
    }

    private async translateAdd(change: mxgraph.mxChildChange): Promise<void> {
        if (await this.getElement(change.child.id) !== undefined) {
            console.log('Child already addded');
            return;
        }
        if (change.child.getParent().getId().length > 1) {
            // The Node is a child node and has no representation in the model
            let parentElement = await this.getElement(change.child.getParent().getId());
            this.parentComponents[change.child.getId()] = parentElement;
            return;
        }

        let addedElement: IContainer = undefined;
        if (change.child.edge) {
            addedElement = await this.translateEdgeAdd(change);
        } else {
            addedElement = await this.translateNodeAdd(change);
        }
        change.child.setId(addedElement.url);
    }

    private async translateEdgeAdd(change: mxgraph.mxChildChange): Promise<IModelConnection> {
        const tool = this.determineTool(change) as ConnectionToolBase<any>;
        tool.source = await this.getElement(change.child.source.id) as IModelNode;
        tool.target = await this.getElement(change.child.target.id) as IModelNode;
        const connection = await tool.perform();
        change.child.id = connection.url;
        return connection;
    }

    private async translateNodeAdd(change: mxgraph.mxChildChange): Promise<IModelNode> {
        const tool = this.determineTool(change) as CreateNodeToolBase<IModelNode>;
        tool.name = change.child.value;
        tool.coords = { x: change.child.geometry.x, y: change.child.geometry.y };
        const node = await tool.perform();
        return node;
    }

    private async translateTerminalChange(change: mxgraph.mxTerminalChange | mxgraph.mxValueChange): Promise<void> {
        const element = await this.getElement(change.cell.id);
        if (element === undefined) {
            return;
        }
        if (change.cell.edge) {
            await this.translateEdgeChange(change, element as IModelConnection);
        } else {
            await this.translateNodeChange(change, element as IModelNode);
        }
    }

    private async translateNodeChange(change: mxgraph.mxTerminalChange | mxgraph.mxValueChange, element: IModelNode): Promise<void> {
        let cell = change.cell as mxgraph.mxCell;
        if (this.nodeNameConverter) {
            if (this.parentComponents[cell.getId()] !== undefined) {
                // The changed node is a sublabel / child
                cell = cell.getParent();
            }
            let value = cell.value;
            if (Type.is(element, CEGNode)) {
                value = new ValuePair(cell.children[0].value, cell.children[1].value);
            }
            const elementValues = this.nodeNameConverter.convertFrom(value, element);
            for (const key in elementValues) {
                element[key] = elementValues[key];
            }
        } else {
            // Keep change.cell to avoid having a parent a child value
            element['variable'] = change.cell.value;
        }
        element['x'] = cell.geometry.x;
        element['y'] = cell.geometry.y;
        element['width'] = cell.geometry.width;
        element['height'] = cell.geometry.height;
        await this.dataService.updateElement(element, true, Id.uuid);
    }

    private async translateEdgeChange(change: mxgraph.mxTerminalChange | mxgraph.mxValueChange,
        connection: IModelConnection): Promise<void> {

        if (change['terminal']) {
            await this.translateEdgeEndsChange(change as mxgraph.mxTerminalChange, connection);
        } else if (change['value']) {
            await this.translateEdgeValueChange(change as mxgraph.mxValueChange, connection);
        } else {
            return Promise.reject('Could not determine edge change.');
        }
    }

    private async translateEdgeValueChange(change: mxgraph.mxValueChange, connection: IModelConnection): Promise<void> {
        const elementValues = this.nodeNameConverter.convertFrom(change.cell.value, connection);
        for (const key in elementValues) {
            connection[key] = elementValues[key];
        }

        const changeId = Id.uuid;
        this.dataService.updateElement(connection, true, changeId);
    }

    private async translateEdgeEndsChange(change: mxgraph.mxTerminalChange, connection: IModelConnection): Promise<void> {
        if (change.cell.target === undefined
            || change.cell.target === null
            || change.cell.source === undefined
            || change.cell.source === null) {
            throw new Error('Source or target not defined');
        }

        if (change.previous === null) {
            return;
        }

        // The new source of the connection
        const sourceUrl = change.cell.source.id;
        // The new target of the connection
        const targetUrl = change.cell.target.id;
        // The new linked node
        const terminalUrl = change.terminal.id;
        let terminal = await this.getElement(terminalUrl) as IModelNode;
        if (terminal === undefined) {
            terminal = await this.dataService.readElement(terminalUrl, true) as IModelNode;
            this.contents = await this.dataService.readContents(this.model.url, true);
        }
        // The previously linked node
        const previousUrl = change.previous.id;
        let previous = await this.getElement(previousUrl) as IModelNode;
        if (previous === undefined) {
            previous = await this.dataService.readElement(previousUrl, true) as IModelNode;
            this.contents = await this.dataService.readContents(this.model.url, true);
        }

        connection.source.url = sourceUrl;
        connection.target.url = targetUrl;

        let field: 'incomingConnections' | 'outgoingConnections';
        if (sourceUrl === terminalUrl) {
            // Source was changed
            field = 'outgoingConnections';
        } else if (targetUrl === terminalUrl) {
            // Target was changed
            field = 'incomingConnections';
        }

        const connectionProxy = previous[field].find(proxy => proxy.url === connection.url);
        Arrays.remove(previous[field], connectionProxy);
        terminal[field].push(connectionProxy);

        const changeId = Id.uuid;
        this.dataService.updateElement(previous, true, changeId);
        this.dataService.updateElement(terminal, true, changeId);
        this.dataService.updateElement(connection, true, changeId);
    }

    private determineTool(change: (mxgraph.mxTerminalChange | mxgraph.mxChildChange)): ToolBase {
        if (change['cell'] !== undefined) {
            // Change; Do nothing
        } else if (change['child'] !== undefined) {
            const childChange = change as mxgraph.mxChildChange;
            if (childChange.child.edge) {
                // edge change
                const edgeTools = this.toolProvider.tools.filter(tool => tool.isVertexTool === false);
                if (edgeTools.length > 1) {
                    return edgeTools.find(tool => tool.style === childChange.child.style);
                } else if (edgeTools.length === 1) {
                    return edgeTools[0];
                }
            } else {
                const vertexTools = this.toolProvider.tools.filter(tool => tool.isVertexTool === true);
                if (vertexTools.length > 1) {
                    return vertexTools.find(tool => tool.style === childChange.child.style);
                } else if (vertexTools.length === 1) {
                    return vertexTools[0];
                }
            }
        }
        throw new Error('Could not determine tool');
    }


    public retranslate(changedElement: IContainer, graph: mxgraph.mxGraph, cell: mxgraph.mxCell) {
        this.preventDataUpdates = true;
        let value = this.nodeNameConverter ? this.nodeNameConverter.convertTo(changedElement) : changedElement.name;
        if (value instanceof ValuePair) {
            for (const key in value) {
                if (value.hasOwnProperty(key)) {
                    const val = value[key];
                    let child = cell.children.find(s => s.getId().endsWith(key));
                    if (child !== undefined) {
                        graph.getModel().beginUpdate();
                        try {
                            graph.model.setValue(child, val);
                        }
                        finally {
                            graph.getModel().endUpdate();
                        }
                    }
                }
            }
        } else {
            graph.getModel().beginUpdate();
            try {
                if (value === cell.value) {
                    graph.model.setValue(cell, value);
                }
                StyleChanger.setStyle(cell, graph, this.shapeProvider.getStyle(changedElement));
            }
            finally {
                graph.getModel().endUpdate();
            }
        }
        this.preventDataUpdates = false;
    }
}
