import { mxgraph } from 'mxgraph'; // Typings only - no code!

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});

/**
 * Keybindings for the graphical editor
 */
export class EditorKeyHandler {
    public static initKeyHandler(graph: mxgraph.mxGraph) {
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

        // Backspace
        keyHandler.bindControlKey(8, (evt: KeyboardEvent) => {
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
                mx.mxClipboard.copy(graph, graph.getSelectionCells());
            }
        });

        // Ctrl+V
        keyHandler.bindControlKey(86, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                mx.mxClipboard.paste(graph);
            }
        });

        // Ctrl+X
        keyHandler.bindControlKey(88, (evt: KeyboardEvent) => {
            EditorKeyHandler.stopEvent(evt);
            if (graph.isEnabled()) {
                mx.mxClipboard.copy(graph, graph.getSelectionCells());
                EditorKeyHandler.deleteSelectedCells(graph);
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
          const selectedCells = graph.getSelectionCells();
          graph.removeCells(selectedCells, true);
        }
      }
}
