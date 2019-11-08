import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { Injectable } from '@angular/core';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});

@Injectable()
export class MXGraphService {
    private graph: mxgraph.mxGraph;
}
