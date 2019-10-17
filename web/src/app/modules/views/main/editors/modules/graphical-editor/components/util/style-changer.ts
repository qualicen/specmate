import { mxgraph } from 'mxgraph';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export class StyleChanger {
    public static addStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, style: string): void {
        let existingStyle = graph.model.getStyle(vertex);
        if (existingStyle !== null && existingStyle !== undefined) {
            graph.model.setStyle(vertex, existingStyle + ';' + style);
        } else {
            graph.model.setStyle(vertex, style);
        }
    }

    public static removeStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, style: string): void {
        let existingStyle = graph.model.getStyle(vertex);
        if (existingStyle !== null  && existingStyle !== undefined) {
            existingStyle = existingStyle.replace(new RegExp(';*' + style + ';*'), '');
        }
    }

    public static replaceStyle(vertex: mxgraph.mxCell, graph: mxgraph.mxGraph, searchStyle: string, replaceStyle: string): void {
        StyleChanger.removeStyle(vertex, graph, searchStyle);
        StyleChanger.addStyle(vertex, graph, replaceStyle);
    }
}