import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { EditorStyle } from '../editor-components/editor-style';

export class CEGNodeTypeUtil {
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
}