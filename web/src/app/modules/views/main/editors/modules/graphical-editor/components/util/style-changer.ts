import { mxgraph } from 'mxgraph';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export class StyleChanger {
    public static addStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, style: string): void {
        let existingStyle = graph.model.getStyle(vertex);
        if (existingStyle !== null && existingStyle !== undefined && existingStyle.length > 0) {
            if (!existingStyle.match(new RegExp(';*' + style + ';*'))) {
                const newStyle = StyleChanger.normalizeStyle(existingStyle + ';' + style);
                graph.model.setStyle(vertex, newStyle);
            }
        } else {
            graph.model.setStyle(vertex, style);
        }
    }

    public static removeStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, style: string): void {
        let existingStyle = graph.model.getStyle(vertex);
        if (existingStyle !== null && existingStyle !== undefined) {
            existingStyle = existingStyle.replace(new RegExp('^' + style), '');
            existingStyle = existingStyle.replace(new RegExp(';' + style), '');
            existingStyle = existingStyle.replace(new RegExp('^;*'), '');
            graph.model.setStyle(vertex, StyleChanger.normalizeStyle(existingStyle));
        }
    }

    public static replaceStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, searchStyle: string, replaceStyle: string): void {
        graph.getModel().beginUpdate();
        StyleChanger.removeStyle(vertex, graph, searchStyle);
        StyleChanger.addStyle(vertex, graph, replaceStyle);
        graph.getModel().endUpdate();
    }

    public static setStyle(cell: mxgraph.mxCell, graph: mxgraph.mxGraph, style: string): void {
        const currentStyleNormalized = StyleChanger.normalizeStyle(cell.getStyle());
        const newStyleNormalized = StyleChanger.normalizeStyle(style);
        if (currentStyleNormalized !== newStyleNormalized) {
            graph.model.setStyle(cell, newStyleNormalized);
        }
    }

    private static normalizeStyle(style: string): string {
        return style
            .split(';')
            .filter(stylePart => stylePart !== undefined)
            .map(stylePart => stylePart.trim())
            .sort()
            .join(';');
    }
}
