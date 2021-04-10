import { ChangeDetectionStrategy, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { Subscription } from 'rxjs';
import { isFunction } from 'rxjs/internal-compatibility';
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
import { CEGmxModelLinkedNode } from '../providers/properties/ceg-mx-model-linked-node';
import { CEGmxModelNode } from '../providers/properties/ceg-mx-model-node';
import { ElementProvider } from '../providers/properties/element-provider';
import { NameProvider } from '../providers/properties/name-provider';
import { ShapeData, ShapeProvider } from '../providers/properties/shape-provider';
import { VertexProvider } from '../providers/properties/vertex-provider';
import { ChangeGuardService } from '../services/change-guard.service';
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
    private nodeNameConverter: ConverterBase<any, string | CEGmxModelNode | CEGmxModelLinkedNode>;
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
        private changeGuard: ChangeGuardService,
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
        let validationSubscription = this.validationService.onEnd(async () => {
            if (!this.isInGraphTransition && this.graph !== undefined && this.graph['destroyed'] !== true) {
                this.graphValidator.updateValidities(this.graph, this.model);

            }
        });
        this.subscriptions.push(validationSubscription);
        let undoSubscription = this.undoService.undoPressed.subscribe(() => {
            this.undo();
        });
        this.subscriptions.push(undoSubscription);
        let redoSubscription = this.undoService.redoPressed.subscribe(() => {
            this.redo();
        });
        this.subscriptions.push(redoSubscription);

        let initModelSubscription = this.graphicalEditorService.initModel.subscribe(async () => {
            await this.init();
            validationService.validateCurrent();
        });
        this.subscriptions.push(initModelSubscription);
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
        this.destroyGraph();
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

        this.graphicalEditorService.start(GraphicalEditorService.OP_INIT);
        if (this.graph !== undefined) {
            this.destroyGraph();
        }

        await this.createGraph();

        this.isInGraphTransition = false;
        this.graphValidator.updateValidities(this.graph, this.model);
        this.undoManager.clear();
        this.graphicalEditorService.triggerGraphicalModelInitFinish();
        this.graphicalEditorService.end(GraphicalEditorService.OP_INIT);
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

        this.graph.getModel().addListener(mx.mxEvent.CHANGE, async (sender: mxgraph.mxEventSource, evt: mxgraph.mxEventObject) => {
            const edit = evt.getProperty('edit') as mxgraph.mxUndoableEdit;
            const done: any[] = [];
            const isAddEdit = edit.changes.find(change => ChangeTranslator.isAddChange(change)) !== undefined;
            const compoundId = Id.uuid;

            try {
                if (!isAddEdit) {
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && !filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents, compoundId);
                        done.push(change);
                    }
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents, compoundId);
                        done.push(change);
                    }
                } else {
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents, compoundId);
                        done.push(change);
                    }
                    for (const change of edit.changes.filter(filteredChange => filteredChange.child && !filteredChange.child.vertex)) {
                        await this.changeTranslator.translate(change, this.graph, this.contents, compoundId);
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
                    await this.changeTranslator.translate(styleChangeMap[cellId], this.graph, this.contents, compoundId);
                }
                for (const change of edit.changes.filter(filteredChange => done.indexOf(filteredChange) < 0)) {
                    await this.changeTranslator.translate(change, this.graph, this.contents, compoundId);
                }
            } catch (e) {
                // This is actually for debug purposes; However, mxgraph or the change translation fails silently without this.
                console.error(e);
                this.changeTranslator.preventDataUpdates = true;
                edit.undo();
                this.changeTranslator.preventDataUpdates = false;
            } finally {
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

                // Retrieve all elements that are currently selected: `selections` contains all selected cells, and the cells carry
                // the id of the actual data elements. With this id, we retrieve the actual data elements from the data service.
                // As the readElement method in the data service is async we need to wait for all of the promises to get the actual
                // list of data elements.
                const selectedElements = await Promise.all(selections.map(c => this.dataService.readElement(c.getId(), true)));

                // We memorize the geomentry of a cell to be abeto reset it if the user cancels the operation, but modified the
                // cell in the background. Such a modification is however, only possible if only one cell is seleted.
                let cell = undefined;
                let geo = undefined;
                if (selectedElements.length === 1) {
                    cell = selections[0];
                    geo = this.graph.getCellGeometry(cell).clone();
                }

                // Evaluate all guards
                const guardResult = await this.changeGuard.guardSelectedElements(selectedElements);

                // Reset if the guard is false - the user clicked on cancel or there is another reason for cancelling the operation.
                if (!guardResult) {

                    // Deselect all elements
                    this.graph.getSelectionModel().clear();
                    this.selectedElementService.select(this.model);

                    // Reset the geometry of a possibly moved cell.
                    if (cell !== undefined && geo !== undefined) {
                        this.graph.getModel().setGeometry(cell, geo);
                        this.graph.refresh(cell);
                    }
                }
            } else {
                this.selectedElementService.select(this.model);
            }
            this.graph.getModel().endUpdate();
        });

        EditorStyle.initEditorStyles(this.graph);
        EditorKeyHandler.initKeyHandler(this.graph, this.undoService);
        this.initUndoManager();

        this.popup = new EditorPopup(this.graph, this.contents, this.translate);
        this.popup.init();

        await this.initGraphicalModel();

        this.initTools();

        let dataServiceSubscription = this.dataService.elementChanged.subscribe((url: string) => {
            const cells = this.graph.getModel().getChildCells(this.graph.getDefaultParent());
            const cell = cells.find(vertex => vertex.id === url);
            const modelElement = this.contents.find(node => node.url === url);
            if (cell === undefined) {
                return;
            }
            this.changeTranslator.retranslate(modelElement, this.graph, cell);
        });
        this.subscriptions.push(dataServiceSubscription);
    }

    private destroyGraph(): void {
        if (this.graph !== undefined) {
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
            this.graph.clearSelection();
            const initialData: ShapeData = mx.mxUtils.clone(this.shapeProvider.getInitialData(tool.style));
            const coords = this.graph.getPointForEvent(evt);
            const vertexUrl = Url.build([this.model.url, Id.uuid]);
            this.graph.startEditing(evt);
            try {
                this.graph.insertVertex(
                    this.graph.getDefaultParent(),
                    vertexUrl,
                    initialData.text,
                    coords.x, coords.y,
                    initialData.size.width, initialData.size.height,
                    initialData.style);
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
        document.getElementById(tool.elementId).addEventListener('click', (evt) => tool.perform(Id.uuid), false);
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
        this.vertexProvider
            = new VertexProvider(this.model, this.graph, this.shapeProvider, this.nodeNameConverter, this.dataService, this.translate);
        const parent = this.graph.getDefaultParent();
        this.changeTranslator.preventDataUpdates = true;

        if (Type.is(this.model, CEGModel)) {
            this.initCEGModel();
        }

        if (Type.is(this.model, Process)) {
            this.initProcessModel();
        }

        this.graph.getModel().beginUpdate();
        try {
            const vertexCache: { [url: string]: mxgraph.mxCell } = {};
            for (const node of this.elementProvider.nodes) {
                const vertex = await this.vertexProvider.provideVertex(node as IModelNode);
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
        const graph = this.graph;

        this.vertexProvider.initCEGVertexRenderer();

        this.graph.getTooltipForCell = function (cell: mxgraph.mxCell) {
            if (cell.value instanceof CEGmxModelNode || cell.value instanceof CEGmxModelLinkedNode) {
                return cell.value.getHint();
            }
            return mx.mxGraph.prototype.getTooltipForCell.apply(this, arguments);
        };

        let stopEditing = mx.mxCellEditor.prototype.stopEditing;
        mx.mxCellEditor.prototype.stopEditing = function (cancel) {
            stopEditing.apply(this, arguments);
            let state = (!cancel) ? this.graph.view.getState(this.editingCell) : null;
            if (state != null) {
                state.cell.value.editField = undefined;
            }
        };

        let getFieldnameForEvent = function (cell: any, evt: any) {
            if (evt != null) {
                let htmlElement = mx.mxEvent.getSource(evt);
                if (htmlElement.hasAttribute('data-node')) {
                    return htmlElement.getAttribute('data-node');
                } else {
                    let point = mx.mxUtils.convertPoint(graph.container,
                        mx.mxEvent.getClientX(evt), mx.mxEvent.getClientY(evt));
                    let state = graph.getView().getState(cell);

                    // if no field is clicked directly, variable is selected for the upper half of the cell, condition otherwise
                    if (state != null) {
                        point.x -= state.x;
                        point.y -= state.y;
                        if (point.y < state.height / 2) {
                            return VertexProvider.ID_VARIABLE;
                        } else {
                            return VertexProvider.ID_CONDITION;
                        }
                    }
                }
            }
            return VertexProvider.ID_VARIABLE;
        };

        this.graph.getEditingValue = function (cell, evt) {
            evt.fieldname = getFieldnameForEvent(cell, evt);
            cell.value.editField = evt.fieldname;
            return cell.value[evt.fieldname] || '';
        };

        this.graph.getModel().valueForCellChanged = function (cell, value) {
            if (cell.isVertex()) {
                if (value instanceof CEGmxModelNode) {
                    let previous = mx.mxUtils.clone(cell.value);
                    cell.value = value;
                    return previous;
                }
                let changedField = cell.value.editField;
                let previous = cell.value[changedField];
                cell.value[changedField] = value;
                return previous;
            }
            return '';
        };

        mx.mxConnectionHandler.prototype.isValidTarget = function (cell: mxgraph.mxCell) {
            return cell.value === undefined || cell.value === null || !(cell.value instanceof CEGmxModelLinkedNode);
        };
    }

    private async initProcessModel(): Promise<void> {
        this.setFunctionGetPreferredSizeForCell(this.graph, this.shapeProvider);
    }

    private setFunctionGetPreferredSizeForCell(graph: mxgraph.mxGraph, shapeProvider: ShapeProvider) {
        graph.getPreferredSizeForCell = function (cell: mxgraph.mxCell) {
            let result = mx.mxGraph.prototype.getPreferredSizeForCell.apply(this, arguments);
            if (result !== null) {
                let width = result.width;
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
