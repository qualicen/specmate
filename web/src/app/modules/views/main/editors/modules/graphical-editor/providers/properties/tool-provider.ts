import { IContainer } from '../../../../../../../../model/IContainer';
import { SpecmateDataService } from '../../../../../../../data/modules/data-service/services/specmate-data.service';
import { SelectedElementService } from '../../../../../../side/modules/selected-element/services/selected-element.service';
import { CEGLayoutTool } from '../../../tool-pallette/tools/ceg/ceg-layout-tool';
import { CEGNodeTool } from '../../../tool-pallette/tools/ceg/ceg-node-tool';
import { DecisionTool } from '../../../tool-pallette/tools/process/decision-tool';
import { EndTool } from '../../../tool-pallette/tools/process/end-tool';
import { ProcessConnectionTool } from '../../../tool-pallette/tools/process/process-connection-tool';
import { ProcessDeleteTool } from '../../../tool-pallette/tools/process/process-delete-tool';
import { StartTool } from '../../../tool-pallette/tools/process/start-tool';
import { StepTool } from '../../../tool-pallette/tools/process/step-tool';
import { ToolBase } from '../../../tool-pallette/tools/tool-base';
import { ProviderBase } from './provider-base';
import { mxgraph } from 'mxgraph';
import { Url } from 'src/app/util/url';
import { Id } from 'src/app/util/id';
import { Type } from 'src/app/util/type';
import { CEGModel } from 'src/app/model/CEGModel';
import { ValuePair } from './value-pair';
import { ShapeData, ShapeProvider } from './shape-provider';
import { VertexProvider } from './vertex-provider';

export class ToolProvider extends ProviderBase {

    private _tools: ToolBase[];

    constructor(
        private model: IContainer,
        private dataService: SpecmateDataService,
        private selectedElementService: SelectedElementService) {
        super(model);
    }

    public get tools(): ToolBase[] {
        if (this._tools) {
            return this._tools;
        }
        if (this.isCEGModel) {
            this.createToolsForCEGModel();
        } else if (this.isProcessModel) {
            this.createToolsForProcess();
        } else {
            this.createEmptyTools();
        }

        return this._tools;
    }

    private createEmptyTools(): void {
        this._tools = [];
    }

    private createToolsForCEGModel(): void {
        this._tools = [
            new CEGNodeTool(this.dataService, this.selectedElementService, this.model),
            new CEGLayoutTool(this.dataService, this.selectedElementService, this.model)
        ];
    }

    private createToolsForProcess(): void {
        this._tools = [
            new StepTool(this.dataService, this.selectedElementService, this.model),
            new DecisionTool(this.model, this.dataService, this.selectedElementService),
            new StartTool(this.model, this.dataService, this.selectedElementService),
            new EndTool(this.model, this.dataService, this.selectedElementService)
        ];
    }

    public getDefaultTool(contents: IContainer[]): ToolBase {
        return contents && contents.length > 0 ? this.tools[0] : this.tools[1];
    }

    public async initTools(graph: mxgraph.mxGraph, shapeProvider: ShapeProvider, vertexPrivider: VertexProvider): Promise<void> {
        for (const tool of this.tools) {
            tool.setGraph(graph);
            if (tool.isVertexTool) {
                this.makeVertexTool(graph, shapeProvider, vertexPrivider, tool);
            } else {
                this.makeClickTool(tool);
            }
        }
    }

    private makeVertexTool(graph: mxgraph.mxGraph, shapeProvider: ShapeProvider, vertexPrivider: VertexProvider, tool: ToolBase) {
        const onDrop = (graph: mxgraph.mxGraph, evt: MouseEvent, cell: mxgraph.mxCell) => {
            graph.stopEditing(false);
            const initialData: ShapeData = shapeProvider.getInitialData(tool.style);
            const coords = graph.getPointForEvent(evt);
            const vertexUrl = Url.build([this.model.url, Id.uuid]);
            graph.startEditing(evt);
            try {
                if (Type.is(this.model, CEGModel)) {
                vertexPrivider.provideCEGNode(vertexUrl, coords.x, coords.y,
                    initialData.size.width, initialData.size.height, initialData.text as ValuePair);
                } else {
                graph.insertVertex(
                    graph.getDefaultParent(),
                    vertexUrl,
                    initialData.text,
                    coords.x, coords.y,
                    initialData.size.width, initialData.size.height,
                    initialData.style);
                }
            }
            finally {
                graph.stopEditing(true);
            }
        };
        mxgraph.mxUtils.makeDraggable(document.getElementById(tool.elementId), graph, onDrop);
    }

    private makeClickTool(tool: ToolBase) {
        document.getElementById(tool.elementId).addEventListener('click', (evt) => tool.perform(), false);
    }
}
