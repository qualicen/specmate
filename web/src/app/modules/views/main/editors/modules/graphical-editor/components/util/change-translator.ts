import { CEGModel } from '../../../../../../../../model/CEGModel';
import { Process } from '../../../../../../../../model/Process';
import { mxgraph } from 'mxgraph';
import { IContainer } from '../../../../../../../../model/IContainer';
import { ConnectionToolBase } from '../../../tool-pallette/tools/connection-tool-base';
import { IModelConnection } from '../../../../../../../../model/IModelConnection';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { CreateNodeToolBase } from '../../../tool-pallette/tools/create-node-tool-base';
import { Id } from '../../../../../../../../util/id';
import { SpecmateDataService } from '../../../../../../../data/modules/data-service/services/specmate-data.service';
import { Arrays } from '../../../../../../../../util/arrays';
import { ConverterBase } from '../../converters/converter-base';
import { NodeNameConverterProvider } from '../../providers/conversion/node-name-converter-provider';
import { DeleteToolBase } from '../../../tool-pallette/tools/delete-tool-base';
import { ToolProvider } from '../../providers/properties/tool-provider';
import { ToolBase } from '../../../tool-pallette/tools/tool-base';
import { ValuePair } from '../../providers/properties/value-pair';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});


export class ChangeTranslator {

    private contents: IContainer[];
    private nodeNameConverter: ConverterBase<IContainer, string|ValuePair>;
    public preventDataUpdates = false;

    constructor(private model: CEGModel | Process, private dataService: SpecmateDataService, private toolProvider: ToolProvider) {
        this.nodeNameConverter = this.nodeNameConverter = new NodeNameConverterProvider(this.model).nodeNameConverter;
    }

    public async translate(change: (mxgraph.mxTerminalChange | mxgraph.mxChildChange)): Promise<void> {
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
        } else if (change['style']) {
            // We have a mxStyleChange, this should only be the case on validation.
        } else if (change['cell'] !== undefined) {
            await this.translateChange(change as mxgraph.mxTerminalChange);
        }
    }


    private async translateDelete(change: mxgraph.mxChildChange): Promise<void> {
        const deleteTool = this.toolProvider.tools.find(tool => (tool as DeleteToolBase).isDeleteTool === true) as DeleteToolBase;
        deleteTool.element = this.contents.find(element => element.url === change.child.id);
        if (deleteTool.element === undefined) {
            return;
        }
        deleteTool.perform();
    }

    private async translateAdd(change: mxgraph.mxChildChange): Promise<void> {
        if (this.contents.find(element => element.url === change.child.id) !== undefined) {
            console.log('Child already addded');
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
        tool.source = this.contents.find(element => element.url === change.child.source.id) as IModelNode;
        tool.target = this.contents.find(element => element.url === change.child.target.id) as IModelNode;
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

    private async translateChange(change: mxgraph.mxTerminalChange | mxgraph.mxValueChange): Promise<void> {
        const element = this.contents.find(element => element.url === change.cell.id);
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
        if (this.nodeNameConverter) {
            const elementValues = this.nodeNameConverter.convertFrom(change.cell.value, element);
            for (const key in elementValues) {
                element[key] = elementValues[key];
            }
        } else {
            element['variable'] = change.cell.value;
        }
        element['x'] = change.cell.geometry.x;
        element['y'] = change.cell.geometry.y;
        element['width'] = change.cell.geometry.width;
        element['height'] = change.cell.geometry.height;
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
        let terminal = this.contents.find(element => element.url === terminalUrl) as IModelNode;
        if (terminal === undefined) {
            terminal = await this.dataService.readElement(terminalUrl, true) as IModelNode;
            this.contents = await this.dataService.readContents(this.model.url, true);
        }
        // The previously linked node
        const previousUrl = change.previous.id;
        let previous = this.contents.find(element => element.url === previousUrl) as IModelNode;
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
}
