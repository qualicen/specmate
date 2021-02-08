import { ChangeDetectionStrategy, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { Subscription } from 'rxjs';
import { CEGModel } from 'src/app/model/CEGModel';
import { Process } from 'src/app/model/Process';
import { ProcessConnection } from 'src/app/model/ProcessConnection';
import { UndoService } from 'src/app/modules/actions/modules/common-controls/services/undo.service';
import { NavigatorService } from 'src/app/modules/navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { IContainer } from '../../../../../../../model/IContainer';
import { IModelConnection } from '../../../../../../../model/IModelConnection';
import { IModelNode } from '../../../../../../../model/IModelNode';
import { Id } from '../../../../../../../util/id';
import { Type } from '../../../../../../../util/type';
import { Url } from '../../../../../../../util/url';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { ValidationService } from '../../../../../../forms/modules/validation/services/validation.service';
import { SelectedElementService } from '../../../../../side/modules/selected-element/services/selected-element.service';
import { EditorToolsService } from '../../tool-pallette/services/editor-tools.service';
import { ToolBase } from '../../tool-pallette/tools/tool-base';
import { ConverterBase } from '../converters/converter-base';
import { NodeNameConverterProvider } from '../providers/conversion/node-name-converter-provider';
import { CEGmxModelNode } from '../providers/properties/ceg-mx-model-node';
import { ElementProvider } from '../providers/properties/element-provider';
import { NameProvider } from '../providers/properties/name-provider';
import { ShapeData, ShapeProvider } from '../providers/properties/shape-provider';
import { VertexProvider } from '../providers/properties/vertex-provider';
import { GraphicalEditorService } from '../services/graphical-editor.service';
import { EditorKeyHandler } from './editor-components/editor-key-handler';
import { EditorPopup } from './editor-components/editor-popup';
import { EditorStyle } from './editor-components/editor-style';
import { GraphValidator } from './editor-components/graph-validator';
import { ChangeTranslator } from './util/change-translator';
import { StyleChanger } from './util/style-changer';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

@Component({
    moduleId: module.id.toString(),
    selector: 'graphical-editor',
    templateUrl: 'graphical-editor.component.html',
    styleUrls: ['graphical-editor.component.css'],
    changeDetection: ChangeDetectionStrategy.Default
})
export class GraphicalEditor implements OnDestroy {

    private graphContainerElement: ElementRef;

    private nameProvider: NameProvider;
    private elementProvider: ElementProvider;
    private nodeNameConverter: ConverterBase<any, string | CEGmxModelNode>;
    private shapeProvider: ShapeProvider;
    private changeTranslator: ChangeTranslator;
    private vertexProvider: VertexProvider;
    private graphValidator: GraphValidator;

    private isInGraphTransition = false;

    private _model: CEGModel | Process;
    private _contents: IContainer[];
    private zoomFactor = 1.0;

    private subscriptions: Subscription[] = [];

    private graphMouseMove: (evt: any) => void;

    constructor(
        private dataService: SpecmateDataService,
        private editorToolsService: EditorToolsService,
        private navigator: NavigatorService,
        private selectedElementService: SelectedElementService,
        private validationService: ValidationService,
        private translate: TranslateService,
        private undoService: UndoService,
        private modal: ConfirmationModal,
        private graphicalEditorService: GraphicalEditorService) {
        const navigationStartSubscription = this.navigator.navigationStart.subscribe(() => {
            this.isInGraphTransition = true;
        });
        this.subscriptions.push(navigationStartSubscription);
        const navigationCancelSubscription = this.navigator.navigationCancel.subscribe(() => {
            this.isInGraphTransition = false;
        });
        this.subscriptions.push(navigationCancelSubscription);
        const hasNavigatedSubscription = this.navigator.hasNavigated.subscribe(() => {
            if (this.graph !== undefined) {
                this.graph.popupMenuHandler.destroy();
            }
        });
        this.subscriptions.push(hasNavigatedSubscription);
        this.graphValidator = new GraphValidator(validationService, graphicalEditorService);
        this.validationService.onEnd(async () => {
            if (!this.isInGraphTransition && this.graph !== undefined && this.graph['destroyed'] !== true) {
                this.graphValidator.updateValidities(this.graph, this.model);

            }
        });
        let undoSubscription = this.undoService.undoPressed.subscribe(() => {
            this.undo();
        });
        this.subscriptions.push(undoSubscription);
        let redoSubscription = this.undoService.redoPressed.subscribe(() => {
            this.redo();
        });
        this.subscriptions.push(redoSubscription);

        this.graphicalEditorService.initModel.subscribe(async () => {
            await this.init();
            validationService.validateCurrent();
        });
    }

    /*********************** MX Graph ***********************/
    private _graph: mxgraph.mxGraph;
    private get graph(): mxgraph.mxGraph {
        return this._graph;
    }
    private set graph(graph: mxgraph.mxGraph) {
        this._graph = graph;
    }
    private undoManager: mxgraph.mxUndoManager;

    private popup: EditorPopup;

    private highlightedEdges: mxgraph.mxCell[] = [];

    /*
     * Construct the MXGraph
     */
    @ViewChild('mxGraphContainer', { static: false })
    public set graphContainer(graphContainer: ElementRef) {
        this.graphContainerElement = graphContainer;
        this.init();
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
    }

    /*
     * Initialize the MXGraph
     */
    private async init(): Promise<void> {
        if (this.graphContainerElement === undefined) {
            return;
        }

        this.graphicalEditorService.start('init');
        if (this.graph !== undefined) {
            this.destroyGraph();
        }

        await this.createGraph();

        this.isInGraphTransition = false;
        this.graphValidator.updateValidities(this.graph, this.model);
        this.undoManager.clear();
        this.graphicalEditorService.triggerGraphicalModelInitFinish();
        this.graphicalEditorService.end('init');
    }

    private async createGraph(): Promise<void> {
        mx.mxConnectionHandler.prototype.connectImage = new mx.mxImage('/assets/img/editor-tools/connector.png', 16, 16);
        mx.mxGraph.prototype.warningImage = new mx.mxImage('/assets/img/editor-tools/error_red.png', 19, 19);
        mx.mxGraph.prototype.foldingEnabled = false;
        mx.mxGraphHandler.prototype['guidesEnabled'] = true;
        mx.mxGraph.prototype.centerZoom = false;
        mx.mxGraph.prototype.allowNegativeCoordinates = false;
        mx.mxGraph.prototype.border = 25;
        mx.mxGraph.prototype.extendParents = true;
        mx.mxGraph.prototype.validationAlert = (message: string) => {
            this.modal.openOk(this.translate.instant('graphicalEditorErrorTitle'), message);
        };

        const cellEditorInit = mx.mxCellEditor.prototype.init;
        mx.mxCellEditor.prototype.init = function () {
            cellEditorInit.apply(this, arguments);
            this.textarea.style.resize = '';
        };

        mx.mxEvent.disableContextMenu(this.graphContainerElement.nativeElement);

        if (this.graphMouseMove === undefined) {
            this.graphMouseMove = mx.mxDragSource.prototype.mouseMove;
        }
        const mouseMove = this.graphMouseMove;

        mx.mxDragSource.prototype.mouseMove = function (evt: PointerEvent) {
            const graph = this.getGraphForEvent(evt);
            if (graph !== undefined && graph.destroyed === true) {
                return;
            }
            mouseMove.apply(this, arguments);
        };

        this.graph = new mx.mxGraph(this.graphContainerElement.nativeElement);

        this.graph.setGridEnabled(true);
        this.graph.setGridSize(15);
        this.graph.setConnectable(true);
        this.graph.setMultigraph(false);
        this.graph.setDropEnabled(false);
        this.graph.setAllowDanglingEdges(false);
        this.graph.zoomTo(this.zoomFactor, undefined);
        const rubberBand = new mx.mxRubberband(this.graph);
        rubberBand.reset();
        this.graph.setHtmlLabels(true);

        this.graph.setTooltips(true);
        this.graph.zoomFactor = 1.1;

        this.setFunctionGetPreferredSizeForCell(this.graph, this.shapeProvider);

        this.graph.addListener(mx.mxEvent.EDITING_STARTED, (sender: mxgraph.mxGraph, evt: mxgraph.mxEventObject) => {
            const cell = evt.properties.cell as mxgraph.mxCell;
            if (cell !== undefined &&
                (cell.id.endsWith(VertexProvider.ID_SUFFIX_VARIABLE) || cell.id.endsWith(VertexProvider.ID_SUFFIX_CONDITION))) {
                let parentWidth = cell.parent.geometry.width;
                cell.getGeometry().setRect(0, cell.geometry.y, parentWidth, cell.geometry.height);
                this.graph.getView().invalidate(cell);
                this.graph.getView().validate(cell);
            }
        });

        this.graph.addListener(mx.mxEvent.DOUBLE_CLICK, (sender: mxgraph.mxGraph, evt: mxgraph.mxEventObject) => {
            const cell = evt.properties.cell as mxgraph.mxCell;
            if (cell !== undefined && cell.id.endsWith(VertexProvider.ID_SUFFIX_TYPE)) {
                evt.consumed = true;
            }
        });

        this.graph.getModel().addListener(mx.mxEvent.CHANGE, async (sender: mxgraph.mxEventSource, evt: mxgraph.mxEventObject) => {
            const edit = evt.getProperty('edit') as mxgraph.mxUndoableEdit;

            const done: any[] = [];

            const isAddEdit = edit.changes.find(change => ChangeTranslator.isAddChange(change)) !== undefined;

            try {
                if (!isAddEdit) {
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && !filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents);
                        done.push(change);
                    }
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents);
                        done.push(change);
                    }
                } else {
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents);
                        done.push(change);
                    }
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && !filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents);
                        done.push(change);
                    }
                }

                // Filter duplicate style changes; We only need to adress the last one.
                const styleChangeMap: { [id: string]: mxgraph.mxStyleChange } = {};
                edit.changes.filter(filteredChange => filteredChange.style !== undefined)
                    .forEach(styleChange => {
                        styleChangeMap[styleChange.cell.id] = styleChange;
                        done.push(styleChange);
                    });
                for (const cellId in styleChangeMap) {
                    await this.changeTranslator.translate(styleChangeMap[cellId], this.graph, this.contents);
                }
                for (const change of edit.changes.filter(filteredChange => done.indexOf(filteredChange) < 0)) {
                    await this.changeTranslator.translate(change, this.graph, this.contents);
                }
            } catch (e) {
                // This is actually for debug purposes; However, mxgraph or the change translation fails silently without this.
                console.error(e);
                this.changeTranslator.preventDataUpdates = true;
                edit.undo();
                this.changeTranslator.preventDataUpdates = false;
            } finally {
                this.graph.getView().revalidate();
                this.undoService.setUndoEnabled(this.undoManager.canUndo());
                this.undoService.setRedoEnabled(this.undoManager.canRedo());
            }
        });

        // Set the focus to the container if a node is selected
        this.graph.addListener(mx.mxEvent.CLICK, () => {
            if (!this.graph.isEditing()) {
                this.graph.container.setAttribute('tabindex', '-1');
                this.graph.container.focus();
            }
        });

        this.graph.getSelectionModel().addListener(mx.mxEvent.CHANGE, async () => {
            let selectionCount = this.graph.getSelectionCount();
            this.graph.getModel().beginUpdate();

            // Dim all Edges
            for (const edge of this.highlightedEdges) {
                StyleChanger.replaceStyle(edge, this.graph, EditorStyle.EDGE_HIGHLIGHT_STYLE_NAME, EditorStyle.EDGE_DIM_STYLE_NAME);
            }
            this.highlightedEdges = [];

            if (selectionCount >= 1) {
                // Highlight All Edges
                let selections = this.graph.getSelectionModel().cells;
                if (selections.length === 1) {
                    if (selections[0].getParent() !== this.graph.getDefaultParent()) {
                        // We selected a child/ sublabel --> Select Parent instead
                        selections[0] = selections[0].getParent();
                    }
                }

                for (const cell of selections) {
                    if (cell.edge) {
                        this.highlightedEdges.push(cell);
                    } else {
                        this.highlightedEdges.push(...cell.edges);
                    }
                }
                for (const edge of this.highlightedEdges) {
                    StyleChanger.replaceStyle(edge, this.graph, EditorStyle.EDGE_DIM_STYLE_NAME, EditorStyle.EDGE_HIGHLIGHT_STYLE_NAME);
                }

                if (selectionCount === 1) {
                    let selection = selections[0];
                    const selectedElement = await this.dataService.readElement(selection.getId(), true);
                    this.selectedElementService.select(selectedElement);
                } else {
                    this.selectedElementService.deselect();
                }
            } else {
                this.selectedElementService.select(this.model);
            }
            this.graph.getModel().endUpdate();
        });

        VertexProvider.initRenderer(this.graph);
        EditorStyle.initEditorStyles(this.graph);
        EditorKeyHandler.initKeyHandler(this.graph, this.undoService);
        this.initUndoManager();

        this.popup = new EditorPopup(this.graph, this.contents, this.translate);
        this.popup.init();

        await this.initGraphicalModel();

        this.initTools();

        this.dataService.elementChanged.subscribe((url: string) => {
            const cells = this.graph.getModel().getChildCells(this.graph.getDefaultParent());
            const cell = cells.find(vertex => vertex.id === url);
            const modelElement = this.contents.find(node => node.url === url);
            if (cell === undefined || modelElement === undefined) {
                return;
            }
            this.changeTranslator.retranslate(modelElement, this.graph, cell);
        });
    }

    private destroyGraph(): void {
        this.graph.destroy();
        this.graph.dropEnabled = false;
        while (this.graph.mouseListeners.length > 0) {
            this.graph.mouseListeners.pop();
        }
        while (this.graph.eventListeners.length > 0) {
            this.graph.eventListeners.pop();
        }
        this.graph = undefined;
    }

    private initTools(): void {
        if (this.editorToolsService.tools === undefined) {
            return;
        }
        for (const tool of this.editorToolsService.tools) {
            tool.graph = this.graph;
            if (tool.isDragTool) {
                this.makeVertexTool(tool);
            } else if (tool.isClickTool) {
                this.makeClickTool(tool);
            }
        }
    }

    private makeVertexTool(tool: ToolBase) {
        const onDrop = (graph: mxgraph.mxGraph, evt: MouseEvent) => {
            this.graph.stopEditing(false);
            const initialData: ShapeData = this.shapeProvider.getInitialData(tool.style);
            const coords = this.graph.getPointForEvent(evt);
            const vertexUrl = Url.build([this.model.url, Id.uuid]);
            this.graph.startEditing(evt);
            try {
                if (Type.is(this.model, CEGModel)) {
                    this.vertexProvider.provideCEGNode(vertexUrl, coords.x, coords.y,
                        initialData.size.width, initialData.size.height, initialData.text as CEGmxModelNode);
                } else {
                    this.graph.insertVertex(
                        this.graph.getDefaultParent(),
                        vertexUrl,
                        initialData.text,
                        coords.x, coords.y,
                        initialData.size.width, initialData.size.height,
                        initialData.style);
                }
            }
            finally {
                this.graph.stopEditing(true);
            }
        };
        const domElement = this.editorToolsService.getDOMElement(tool);
        mx.mxEvent.removeAllListeners(domElement);
        mx.mxUtils.makeDraggable(domElement, this.graph, onDrop);
    }

    private makeClickTool(tool: ToolBase) {
        document.getElementById(tool.elementId).addEventListener('click', () => tool.perform(), false);
    }

    private initUndoManager(): void {
        this.undoManager = new mx.mxUndoManager(50);
        const listener = async (sender: mxgraph.mxEventSource, evt: mxgraph.mxEventObject) => {
            // StyleChanges are not added to the undo-stack, except an edge is negated (dashed line)
            const edit = evt.getProperty('edit');
            const isNotOnlyStyleChange = edit.changes.some((s: object) => s.constructor.name !== 'mxStyleChange');
            const isNegated = edit.changes.some(function test(s: any): boolean {
                if (s.constructor.name === 'mxStyleChange' && s.previous !== null) {
                    return (s.style as String).includes(EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE)
                        !== ((s.previous as String).includes(EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE));
                }
                return false;
            });
            if (isNotOnlyStyleChange || isNegated) {
                this.undoManager.undoableEditHappened(edit);
            }
        };
        this.graph.getModel().addListener(mx.mxEvent.UNDO, listener);
        this.graph.getView().addListener(mx.mxEvent.UNDO, listener);
    }

    private async initGraphicalModel(): Promise<void> {
        this._contents = await this.dataService.readContents(this.model.url, true);
        this.elementProvider = new ElementProvider(this.model, this._contents);
        this.nodeNameConverter = new NodeNameConverterProvider(this.model).nodeNameConverter;
        this.vertexProvider = new VertexProvider(this.model, this.graph, this.shapeProvider, this.nodeNameConverter);
        const parent = this.graph.getDefaultParent();
        this.changeTranslator.preventDataUpdates = true;

        if (Type.is(this.model, CEGModel)) {
            this.initCEGModel();
        }

        this.graph.getModel().beginUpdate();
        try {
            const vertexCache: { [url: string]: mxgraph.mxCell } = {};
            for (const node of this.elementProvider.nodes) {
                const vertex = this.vertexProvider.provideVertex(node as IModelNode);
                vertexCache[node.url] = vertex;
            }
            for (const connection of this.elementProvider.connections.map(element => element as IModelConnection)) {
                const sourceVertex = vertexCache[connection.source.url];
                const targetVertex = vertexCache[connection.target.url];
                const value = this.nodeNameConverter ? this.nodeNameConverter.convertTo(connection) : connection.name;
                const style = this.shapeProvider.getStyle(connection);
                let cell = this.graph.insertEdge(parent, connection.url, value, sourceVertex, targetVertex, style);
                if (Type.is(connection, ProcessConnection)) {
                    cell.geometry.x = (connection as ProcessConnection).labelX;
                    cell.geometry.y = (connection as ProcessConnection).labelY;
                }
            }

            if (Type.is(this.model, CEGModel)) {
                for (const url in vertexCache) {
                    const vertex = vertexCache[url];
                    const type = GraphicalEditor.getCEGNodeType(vertex);
                    StyleChanger.addStyle(vertex, this.graph, type);
                }
            }
        } finally {
            this.graph.getModel().endUpdate();
            this.changeTranslator.preventDataUpdates = false;
            this.validationService.validateCurrent();
        }
    }

    private async initCEGModel(): Promise<void> {
        this.graph.isCellEditable = function (cell) {
            let c = cell as mxgraph.mxCell;
            if (c.edge) {
                return false;
            }
            if (c.children !== undefined && c.children !== null && c.children.length > 0) {
                return false;
            }
            return true;
        };

        this.graph.graphHandler.setRemoveCellsFromParent(false);

        this.graph.isCellResizable = function (cell) {
            let geo = this.model.getGeometry(cell);
            return geo == null || !geo.relative;
        };
    }

    private setFunctionGetPreferredSizeForCell(graph: mxgraph.mxGraph, shapeProvider: ShapeProvider) {
        let graphGetPreferredSizeForCell = graph.getPreferredSizeForCell;
        graph.getPreferredSizeForCell = function (cell: mxgraph.mxCell) {
            if (cell.getId().endsWith(VertexProvider.ID_SUFFIX_TYPE)) {
                return undefined;
            }
            let result = graphGetPreferredSizeForCell.apply(this, arguments);
            if (result !== null) {
                let width = result.width;
                if (cell.children !== undefined && cell.children !== null) {
                    for (const child of cell.children) {
                        let resultChild = graph.getPreferredSizeForCell(child);
                        if (resultChild !== undefined) {
                            width = Math.max(width, resultChild.width);
                        }
                    }
                }
                if (cell.style !== undefined) {
                    let shapeData = shapeProvider.getInitialData(cell.style.split(';')[0]);
                    if (shapeData !== undefined) {
                        let minWidth = shapeData.size.width;
                        let originalHeight = cell.getGeometry().height;
                        result.width = Math.max(minWidth, width + shapeData.size.margin);
                        result.height = originalHeight;
                    }
                }
            }
            return result;
        };
    }

    /*********************** Editor Options ***********************/
    public get model(): CEGModel | Process {
        return this._model;
    }

    public static getCEGNodeType(cell: mxgraph.mxCell) {
        if (cell.isEdge()) {
            return '';
        }

        if (cell.edges === null) {
            // Node without Edges
            return EditorStyle.CAUSE_STYLE_NAME;
        }

        let hasIncommingEdges = false;
        let hasOutgoingEdges = false;
        for (const edge of cell.edges) {
            if (edge.source.id === cell.id) {
                hasOutgoingEdges = true;
            } else if (edge.target.id === cell.id) {
                hasIncommingEdges = true;
            }
        }

        if (hasIncommingEdges && hasOutgoingEdges) {
            return EditorStyle.INNER_STYLE_NAME;
        } else if (hasIncommingEdges) {
            return EditorStyle.EFFECT_STYLE_NAME;
        }
        return EditorStyle.CAUSE_STYLE_NAME;
    }

    private resetProviders(model: CEGModel | Process): void {
        this.shapeProvider = new ShapeProvider(model);
        this.nameProvider = new NameProvider(model, this.translate);
        this.editorToolsService.init(model);
        this.changeTranslator = new ChangeTranslator(model, this.dataService, this.editorToolsService.toolProvider, this.shapeProvider);
    }

    @Input()
    public set model(model: CEGModel | Process) {
        this.resetProviders(model);
        this._model = model;
        this.dataService.readContents(model.url, true).then((contents) => {
            this._contents = contents;
            this.elementProvider = new ElementProvider(this.model, this._contents);
            this.init();
        });
    }

    @Input()
    public set contents(contents: IContainer[]) { }

    public get contents(): IContainer[] {
        return this._contents;
    }

    public get isValid(): boolean {
        return this.validationService.isValid(this.model);
    }

    public zoomIn(): void {
        this.graph.zoomIn();
        this.zoomFactor = this.zoomFactor * this.graph.zoomFactor;
    }

    public zoomOut(): void {
        this.graph.zoomOut();
        this.zoomFactor = this.zoomFactor / this.graph.zoomFactor;
    }

    public resetZoom(): void {
        this.graph.zoomActual();
        this.zoomFactor = 1.0;
    }

    public get connections(): IContainer[] {
        if (!this.elementProvider) {
            return [];
        }
        return this.elementProvider.connections;
    }

    public get nodes(): IContainer[] {
        if (!this.elementProvider) {
            return [];
        }
        return this.elementProvider.nodes;
    }

    public get name(): string {
        if (!this.nameProvider) {
            return '';
        }
        return this.nameProvider.name;
    }

    public undo(): void {
        if (this.undoManager.canUndo()) {
            this.undoManager.undo();
        }
    }

    public redo(): void {
        if (this.undoManager.canRedo()) {
            this.undoManager.redo();
        }
    }
}
