import { ChangeDetectionStrategy, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { CEGModel } from 'src/app/model/CEGModel';
import { CEGNode } from 'src/app/model/CEGNode';
import { ProcessDecision } from 'src/app/model/ProcessDecision';
import { ProcessEnd } from 'src/app/model/ProcessEnd';
import { ProcessStart } from 'src/app/model/ProcessStart';
import { ProcessStep } from 'src/app/model/ProcessStep';
import { UndoService } from 'src/app/modules/actions/modules/common-controls/services/undo.service';
import { IContainer } from '../../../../../../../model/IContainer';
import { IModelConnection } from '../../../../../../../model/IModelConnection';
import { IModelNode } from '../../../../../../../model/IModelNode';
import { Id } from '../../../../../../../util/id';
import { Type } from '../../../../../../../util/type';
import { Url } from '../../../../../../../util/url';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { ValidationService } from '../../../../../../forms/modules/validation/services/validation.service';
import { SelectedElementService } from '../../../../../side/modules/selected-element/services/selected-element.service';
import { ConverterBase } from '../converters/converter-base';
import { NodeNameConverterProvider } from '../providers/conversion/node-name-converter-provider';
import { ElementProvider } from '../providers/properties/element-provider';
import { NameProvider } from '../providers/properties/name-provider';
import { ShapeData, ShapeProvider } from '../providers/properties/shape-provider';
import { ToolProvider } from '../providers/properties/tool-provider';
import { ValuePair } from '../providers/properties/value-pair';
import { VertexProvider } from '../providers/properties/vertex-provider';
import { EditorKeyHandler } from './editor-components/editor-key-handler';
import { EditorPopup } from './editor-components/editor-popup';
import { EditorStyle } from './editor-components/editor-style';
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
export class GraphicalEditor {

  private nameProvider: NameProvider;
  private elementProvider: ElementProvider;
  private toolProvider: ToolProvider;
  private nodeNameConverter: ConverterBase<any, string | ValuePair>;
  private shapeProvider: ShapeProvider;
  private changeTranslator: ChangeTranslator;
  private vertexPrivider: VertexProvider;

  public isGridShown = true;

  private _model: IContainer;
  private _contents: IContainer[];

  constructor(
    private dataService: SpecmateDataService,
    private selectedElementService: SelectedElementService,
    private validationService: ValidationService,
    private translate: TranslateService,
    private undoService: UndoService) {
    this.validationService.validationFinished.subscribe(() => {
      this.updateValidities();
    });
    this.undoService.undoPressed.subscribe(() => {
      this.undo();
    });
    this.undoService.redoPressed.subscribe(() => {
      this.redo();
    });
  }

  /*********************** MX Graph ***********************/
  private graph: mxgraph.mxGraph;
  private undoManager: mxgraph.mxUndoManager;

  private popup: EditorPopup;

  private highlightedEdges: mxgraph.mxCell[] = [];

  /*
   * Construct the MXGraph
   */
  @ViewChild('mxGraphContainer')
  public set graphContainer(element: ElementRef) {
    if (this.graph !== undefined) {
      return;
    }

    mx.mxConnectionHandler.prototype.connectImage = new mx.mxImage('/assets/img/editor-tools/connector.png', 16, 16);
    mx.mxGraph.prototype.warningImage = new mx.mxImage('/assets/img/editor-tools/error_red.png', 20, 20);
    mx.mxGraphHandler.prototype['guidesEnabled'] = true;

    if (element === undefined) {
      return;
    }
    mx.mxEvent.disableContextMenu(element.nativeElement);

    this.graph = new mx.mxGraph(element.nativeElement);
    this.graph.setGridEnabled(true);
    this.graph.setConnectable(true);
    this.graph.setMultigraph(false);
    this.graph.setDropEnabled(false);
    const rubberBand = new mx.mxRubberband(this.graph);
    rubberBand.reset();

    this.graph.setTooltips(true);

    this.graph.getModel().addListener(mx.mxEvent.CHANGE, async (sender: mxgraph.mxEventSource, evt: mxgraph.mxEventObject) => {
      const edit = evt.getProperty('edit') as mxgraph.mxUndoableEdit;

      if (edit.undone === true || edit.redone === true) {
        this.undoService.setUndoEnabled(this.undoManager.canUndo());
        this.undoService.setRedoEnabled(this.undoManager.canRedo());
        return;
      }

      try {
        for (const change of edit.changes) {
          await this.changeTranslator.translate(change);
        }
      } catch (e) {
        this.changeTranslator.preventDataUpdates = true;
        edit.undo();
        this.changeTranslator.preventDataUpdates = false;
      } finally {
        this.undoService.setUndoEnabled(this.undoManager.canUndo());
        this.undoService.setRedoEnabled(this.undoManager.canRedo());
      }
    });

    // Set the focus to the container if a node is selected
    this.graph.addListener(mx.mxEvent.CLICK, function (sender: any, evt: any) {
      if (!this.graph.isEditing()) {
        this.graph.container.setAttribute('tabindex', '-1');
        this.graph.container.focus();
      }
    }.bind(this));

    this.graph.getSelectionModel().addListener(mx.mxEvent.CHANGE, async (args: any) => {
      let selectionCount = this.graph.getSelectionCount();

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
        }
      } else {
        this.selectedElementService.deselect();
      }
    });

    this.init();
  }

  /*
   * Initialize the MXGraph
   */
  private async init(): Promise<void> {
    if (this.graph === undefined || this.model === undefined) {
      return;
    }
    EditorStyle.initEditorStyles(this.graph);
    EditorKeyHandler.initKeyHandler(this.graph);
    this.initGraphicalModel();
    this.initTools();
    this.initUndoManager();

    this.popup = new EditorPopup(this.graph, this.translate, this.contents);
    this.popup.init();

    this.undoManager.clear();
    this.dataService.elementChanged.subscribe((url: string) => {
      const cells = this.graph.getModel().getChildCells(this.graph.getDefaultParent());
      const cell = cells.find(vertex => vertex.id === url);
      const node = this.nodes.find(node => node.url === url);
      if (cell === undefined || node === undefined) {
        return;
      }

      this.changeTranslator.retranslate(node, this.graph, cell);
    });
  }

  private initUndoManager(): void {
    this.undoManager = new mx.mxUndoManager(50);
    const listener = async (sender: mxgraph.mxEventSource, evt: mxgraph.mxEventObject) => {
      if (!evt.getProperty('edit').changes.some((s: object) => s.constructor.name === 'mxStyleChange')) {
        this.undoManager.undoableEditHappened(evt.getProperty('edit'));
      }
    };
    this.graph.getModel().addListener(mx.mxEvent.UNDO, listener);
    this.graph.getView().addListener(mx.mxEvent.UNDO, listener);
    this.undoManager.clear();
  }

  private async initGraphicalModel(): Promise<void> {
    this._contents = await this.dataService.readContents(this.model.url, true);
    this.elementProvider = new ElementProvider(this.model, this._contents);
    this.nodeNameConverter = new NodeNameConverterProvider(this.model).nodeNameConverter;
    this.vertexPrivider = new VertexProvider(this.model, this.graph, this.shapeProvider, this.nodeNameConverter);
    const parent = this.graph.getDefaultParent();
    this.changeTranslator.preventDataUpdates = true;

    if (Type.is(this.model, CEGModel)) {
      this.initCEGModel();
    }

    this.graph.getModel().beginUpdate();
    try {
      const vertexCache: { [url: string]: mxgraph.mxCell } = {};
      for (const node of this.elementProvider.nodes) {
        const vertex = this.vertexPrivider.provideVertex(node as IModelNode);
        vertexCache[node.url] = vertex;
      }
      for (const connection of this.elementProvider.connections.map(element => element as IModelConnection)) {
        const sourceVertex = vertexCache[connection.source.url];
        const targetVertex = vertexCache[connection.target.url];
        const value = this.nodeNameConverter ? this.nodeNameConverter.convertTo(connection) : connection.name;
        const style = this.shapeProvider.getStyle(connection);
        this.graph.insertEdge(parent, connection.url, value, sourceVertex, targetVertex, style);
      }

      for (const url in vertexCache) {
        const vertex = vertexCache[url];
        const type = this.getNodeType(vertex);
        StyleChanger.addStyle(vertex, this.graph, type);
      }
    } finally {
      this.graph.getModel().endUpdate();
      this.changeTranslator.preventDataUpdates = false;
      this.undoManager.clear();
      this.validationService.validateCurrent();
    }
  }

  private async initCEGModel(): Promise<void> {
    this.graph.setHtmlLabels(true);

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

    this.graph.isWrapping = function (cell) {
      return this.model.isCollapsed(cell);
    };

    this.graph.isCellResizable = function (cell) {
      let geo = this.model.getGeometry(cell);
      return geo == null || !geo.relative;
    };
  }

  private async initTools(): Promise<void> {
    const tools = this.toolProvider.tools;

    for (const tool of tools.filter(t => t.isVertexTool === true)) {
      const onDrop = (graph: mxgraph.mxGraph, evt: MouseEvent, cell: mxgraph.mxCell) => {
        this.graph.stopEditing(false);
        const initialData: ShapeData = this.shapeProvider.getInitialData(tool.style);
        const coords = graph.getPointForEvent(evt);
        const vertexUrl = Url.build([this.model.url, Id.uuid]);
        this.graph.startEditing(evt);
        try {
          if (Type.is(this.model, CEGModel)) {
            this.vertexPrivider.provideCEGNode(vertexUrl, coords.x, coords.y,
              initialData.size.width, initialData.size.height, initialData.text as ValuePair);
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
      mx.mxUtils.makeDraggable(document.getElementById(tool.elementId), this.graph, onDrop);
    }
  }

  private updateValidities(): void {
    if (this.graph === undefined) {
      return;
    }
    const vertices = this.graph.getModel().getChildVertices(this.graph.getDefaultParent());
    for (const vertex of vertices) {
      StyleChanger.replaceStyle(vertex, this.graph, EditorStyle.INVALID_STYLE_NAME, EditorStyle.VALID_STYLE_NAME);
      this.graph.setCellWarning(vertex, null);
    }
    const validationResult = this.validationService.getValidationResults(this.model);
    const invalidNodes = validationResult.filter(e => this.elementProvider.isNode(e.element));
    for (const invalidNode of invalidNodes) {
      const vertexId = invalidNode.element.url;
      const vertex = vertices.find(vertex => vertex.id === vertexId);
      StyleChanger.replaceStyle(vertex, this.graph, EditorStyle.VALID_STYLE_NAME, EditorStyle.INVALID_STYLE_NAME);
      const overlay = this.graph.setCellWarning(vertex, invalidNode.message);
      if (Type.is(invalidNode.element, CEGNode) || Type.is(invalidNode.element, ProcessStep)) {
        overlay.offset = new mx.mxPoint(-13, -12);
      }
      if (Type.is(invalidNode.element, ProcessStart) || Type.is(invalidNode.element, ProcessEnd)) {
        overlay.offset = new mx.mxPoint(-23, -12);
      }
      if (Type.is(invalidNode.element, ProcessDecision)) {
        overlay.offset = new mx.mxPoint(-28, -20);
      }
    }

    for (const vertex of vertices) {
      StyleChanger.removeStyle(vertex, this.graph, EditorStyle.CAUSE_STYLE_NAME);
      StyleChanger.removeStyle(vertex, this.graph, EditorStyle.EFFECT_STYLE_NAME);
      StyleChanger.removeStyle(vertex, this.graph, EditorStyle.INNER_STYLE_NAME);
      StyleChanger.addStyle(vertex, this.graph, this.getNodeType(vertex));
    }
  }

  /*********************** Editor Options ***********************/
  public get model(): IContainer {
    return this._model;
  }

  private getNodeType(cell: mxgraph.mxCell) {
    if (cell.edges === undefined || cell.edge) {
      // The cell is an edge
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

  @Input()
  public set model(model: IContainer) {
    this.toolProvider = new ToolProvider(model, this.dataService, this.selectedElementService);
    this.shapeProvider = new ShapeProvider(model);
    this.nameProvider = new NameProvider(model, this.translate);
    this.changeTranslator = new ChangeTranslator(model, this.dataService, this.toolProvider);
    this._model = model;
  }

  @Input()
  public set contents(contents: IContainer[]) {
    this._contents = contents;
    this.elementProvider = new ElementProvider(this.model, this._contents);
  }

  public get contents(): IContainer[] {
    return this._contents;
  }

  public get isValid(): boolean {
    return this.validationService.isValid(this.model);
  }

  public zoomIn(): void {
    this.graph.zoomIn();
  }

  public zoomOut(): void {
    this.graph.zoomOut();
  }

  public resetZoom(): void {
    this.graph.zoomActual();
  }

  public showGrid(): void {
    this.isGridShown = true;
  }

  public hideGrid(): void {
    this.isGridShown = false;
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
