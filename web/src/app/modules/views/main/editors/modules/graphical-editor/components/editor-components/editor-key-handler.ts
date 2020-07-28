import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { UndoService } from 'src/app/modules/actions/modules/common-controls/services/undo.service';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

/**
 * Keybindings for the graphical editor
 */
export class EditorKeyHandler {
    public static initKeyHandler(graph: mxgraph.mxGraph, undoService: UndoService) {
        let keyHandler = new mx.mxKeyHandler(graph);

        // Support Mac command-key
        keyHandler.getFunction = function (evt) {
            if (evt != null) {
                return (mx.mxEvent.isControlDown(evt) || (mx.mxClient.IS_MAC && evt.metaKey))
                    ? this.controlKeys[evt.keyCode]
                    : this.normalKeys[evt.keyCode];
            }
            return null;
        };

        // Del
        keyHandler.bindKey(46, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            EditorKeyHandler.deleteSelectedCells(graph);
        });

        // Ctrl + Backspace
        keyHandler.bindControlKey(8, (evt: KeyboardEvent) => {
            EditorKeyHandler.deleteSelectedCells(graph);
        });
        // Backspace
        keyHandler.bindKey(8, (evt: KeyboardEvent) => {
            EditorKeyHandler.deleteSelectedCells(graph);
        });

        // Ctrl+A
        keyHandler.bindControlKey(65, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                graph.getSelectionModel().addCells(graph.getChildCells(graph.getDefaultParent()));
            }
        });

        // Ctrl+C
        keyHandler.bindControlKey(67, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                const sel = EditorKeyHandler.getFilteredSelectedCells(graph);
                mx.mxClipboard.copy(graph, sel);
            }
        });

        // Ctrl+V
        keyHandler.bindControlKey(86, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                const cell = mx.mxClipboard.paste(graph);
            }
        });

        // Ctrl+X
        keyHandler.bindControlKey(88, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                const sel = EditorKeyHandler.getFilteredSelectedCells(graph);
                mx.mxClipboard.copy(graph, sel);
                EditorKeyHandler.deleteSelectedCells(graph);
            }
        });

        // Ctrl+Z
        keyHandler.bindControlKey(90, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                undoService.undo();
            }
        });

        // Ctrl+Y
        keyHandler.bindControlKey(89, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                undoService.redo();
            }
        });

        return keyHandler;
    }

    private static stopEvent(evt: Event) {
        evt.preventDefault();
        evt.stopPropagation();
    }

    private static deleteSelectedCells(graph: mxgraph.mxGraph): void {
        if (graph.isEnabled()) {
            const selectedCells = EditorKeyHandler.getFilteredSelectedCells(graph);
            graph.removeCells(selectedCells, true);
        }
    }

    private static getFilteredSelectedCells(graph: mxgraph.mxGraph): mxgraph.mxCell[] {
        let cells = graph.getSelectionCells();
        let parent = graph.getDefaultParent();

        cells = cells.filter((cell: mxgraph.mxCell) => {
            return cell.getParent() === parent;
        });
        return cells;
    }
}
