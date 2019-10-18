import { mxgraph } from 'mxgraph'; // Typings only - no code!

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});


export type Style = { [key: string]: string;}
/**
 * Stylesheet for the graphical editor.
 */
export class EditorStyle {

    public static readonly VALID_STYLE_NAME = 'VALID';
    public static readonly VALID_STYLE: Style = {};

    public static readonly INVALID_STYLE_NAME = 'INVALID';
    public static readonly INVALID_STYLE: Style = {};

    private static initStyles() {
        mx.mxConstants.HANDLE_FILLCOLOR = '#99ccff';
        mx.mxConstants.HANDLE_STROKECOLOR = '#0088cf';
        mx.mxConstants.VERTEX_SELECTION_COLOR = '#00a8ff';
        mx.mxConstants.EDGE_SELECTION_COLOR = '#00a8ff';
        mx.mxConstants.DEFAULT_FONTSIZE = 12;

        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_OPACITY]           = '100';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_FILLCOLOR]         = '#457fca';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_GRADIENTCOLOR]     = '#77A7D3';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_STROKE_OPACITY]    = '100';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_STROKECOLOR]       = '#346CB6';
        EditorStyle.VALID_STYLE[mx.mxConstants.STYLE_FONTCOLOR]         = '#ffffff';

        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_OPACITY]         = '75';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_FILLCOLOR]       = '#ffc3d9';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_STROKE_OPACITY]  = '100';
        EditorStyle.INVALID_STYLE[mx.mxConstants.STYLE_STROKECOLOR]     = '#ffc3d9';
    }

    public static initEditorStyles(graph: mxgraph.mxGraph) {
        EditorStyle.initStyles();

        const stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle(this.VALID_STYLE_NAME, EditorStyle.VALID_STYLE);
        stylesheet.putCellStyle(this.INVALID_STYLE_NAME, EditorStyle.INVALID_STYLE);

        const vertexStyle = graph.getStylesheet().getDefaultVertexStyle();
        vertexStyle[mx.mxConstants.STYLE_STROKECOLOR] = '#000000';
        graph.getStylesheet().getDefaultEdgeStyle()[mx.mxConstants.STYLE_STROKECOLOR] = '#000000';
    }
}