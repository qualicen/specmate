import { mxgraph } from 'mxgraph'; // Typings only - no code!

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});


export type Style = { [key: string]: string; };

/**
 * Stylesheet for the graphical editor.
 */
export class EditorStyle {

    public static readonly VALID_STYLE_NAME = 'VALID';
    public static readonly INVALID_STYLE_NAME = 'INVALID';
    public static readonly VALID_STYLE: Style = {};
    public static readonly INVALID_STYLE: Style = {};

    public static readonly EDGE_HIGHLIGHT_STYLE_NAME = 'EDGE_HIGHLIGHT';
    public static readonly EDGE_DIM_STYLE_NAME = 'EDGE_DIM';
    public static readonly EDGE_HIGHLIGHT_STYLE: Style = {};
    public static readonly EDGE_DIM_STYLE: Style = {};

    public static readonly CAUSE_STYLE_NAME  = 'CAUSE';
    public static readonly INNER_STYLE_NAME  = 'INNER';
    public static readonly EFFECT_STYLE_NAME = 'EFFECT';
    public static readonly CAUSE_STYLE: Style = {};
    public static readonly INNER_STYLE: Style = {};
    public static readonly EFFECT_STYLE: Style = {};

    private static initStyles() {
        mx.mxConstants.HANDLE_FILLCOLOR = '#99ccff';
        mx.mxConstants.HANDLE_STROKECOLOR = '#0088cf';
        mx.mxConstants.VERTEX_SELECTION_COLOR = '#00a8ff';
        mx.mxConstants.EDGE_SELECTION_COLOR = '#00a8ff';
        mx.mxConstants.DEFAULT_FONTSIZE = 12;

        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_DASHED]                = '0';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_STROKEWIDTH]           = '3';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_STROKE_OPACITY]        = '100';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_STROKECOLOR]           = '#346CB6';

        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_DASHED]              = '0';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_STROKEWIDTH]         = '3';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_STROKE_OPACITY]      = '100';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_STROKECOLOR]         = '#ff0000';

        EditorStyle.CAUSE_STYLE[mx.mxConstants.STYLE_FILLCOLOR]             = '#39C4B8';
        EditorStyle.EFFECT_STYLE[mx.mxConstants.STYLE_FILLCOLOR]            = '#f6960d';
        EditorStyle.INNER_STYLE[mx.mxConstants.STYLE_FILLCOLOR]             = '#e0e026';

        EditorStyle.EDGE_HIGHLIGHT_STYLE[mx.mxConstants.STYLE_STROKECOLOR]  = '#000000';
        EditorStyle.EDGE_HIGHLIGHT_STYLE[mx.mxConstants.STYLE_STROKEWIDTH]  = '3';

        EditorStyle.EDGE_DIM_STYLE[mx.mxConstants.STYLE_STROKECOLOR]        = '#858585';
        EditorStyle.EDGE_DIM_STYLE[mx.mxConstants.STYLE_STROKEWIDTH]        = '2';
    }

    public static initEditorStyles(graph: mxgraph.mxGraph) {
        EditorStyle.initStyles();

        const stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle(EditorStyle.VALID_STYLE_NAME  , EditorStyle.VALID_STYLE);
        stylesheet.putCellStyle(EditorStyle.INVALID_STYLE_NAME, EditorStyle.INVALID_STYLE);
        stylesheet.putCellStyle(EditorStyle.CAUSE_STYLE_NAME  , EditorStyle.CAUSE_STYLE);
        stylesheet.putCellStyle(EditorStyle.EFFECT_STYLE_NAME , EditorStyle.EFFECT_STYLE);
        stylesheet.putCellStyle(EditorStyle.INNER_STYLE_NAME  , EditorStyle.INNER_STYLE);

        const vertexStyle = graph.getStylesheet().getDefaultVertexStyle();
        vertexStyle[mx.mxConstants.STYLE_STROKECOLOR] = '#000000';
        vertexStyle[mx.mxConstants.STYLE_DASHED] = '1';
        vertexStyle[mx.mxConstants.STYLE_DASH_PATTERN] = '4';

        stylesheet.putCellStyle(EditorStyle.EDGE_DIM_STYLE_NAME, EditorStyle.EDGE_DIM_STYLE);
        stylesheet.putCellStyle(EditorStyle.EDGE_HIGHLIGHT_STYLE_NAME, EditorStyle.EDGE_HIGHLIGHT_STYLE);

        const edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
        edgeStyle[mx.mxConstants.STYLE_STROKECOLOR] = EditorStyle.EDGE_DIM_STYLE[mx.mxConstants.STYLE_STROKECOLOR];
        edgeStyle[mx.mxConstants.STYLE_STROKEWIDTH] = EditorStyle.EDGE_DIM_STYLE[mx.mxConstants.STYLE_STROKEWIDTH];
    }
}
