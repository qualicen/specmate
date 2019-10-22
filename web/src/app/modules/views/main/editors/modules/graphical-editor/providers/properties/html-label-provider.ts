import { ProviderBase } from "./provider-base";
import { IContainer } from "src/app/model/IContainer";
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import {ValuePair} from './value-pair'
declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});

/**
 * Based on https://github.com/jgraph/mxgraph/blob/master/javascript/examples/editing.html
 */
export class HTMLLabelProvider extends ProviderBase {
    constructor(element: IContainer, private graph: mxgraph.mxGraph) {
        super(element);
    }

    public getFieldnameForEvent(cell: any, evt: any): string {
        if (evt != null) {
            // Finds the relative coordinates inside the cell
            var point = mx.mxUtils.convertPoint(this.graph.container,
                mx.mxEvent.getClientX(evt), mx.mxEvent.getClientY(evt));
            var state = this.graph.getView().getState(cell);
            
            if (state != null) {
                point.x -= state.x;
                point.y -= state.y;
                
                // Returns second if mouse in second half of cell
                if (point.y > state.height / 2) {
                    return ValuePair.CONDITION_KEY;
                }
            }
        }
        
        return ValuePair.VARIABLE_KEY;
    }

    public getLabel(cell: any) {
        if(cell.edge) {
            return '';
        }

        var table = document.createElement('table');
        table.style.height = '100%';
        table.style.width = '100%';
        
        var body = document.createElement('tbody');
        var tr1 = document.createElement('tr');
        var td1 = document.createElement('td');
        td1.style.textAlign = 'center';
        td1.style.fontSize = '12px';
        td1.style.color = '#774400';
        mx.mxUtils.write(td1, cell.value.first);
        
        var tr2 = document.createElement('tr');
        var td2 = document.createElement('td');
        td2.style.textAlign = 'center';
        td2.style.fontSize = '12px';
        td2.style.color = '#774400';
        mx.mxUtils.write(td2, cell.value.second);
        
        tr1.appendChild(td1);
        tr2.appendChild(td2);
        body.appendChild(tr1);
        body.appendChild(tr2);
        table.appendChild(body);
        
        return table.outerHTML;
    }

    public static currentFieldname: string;
    public getEditingValue(cell: any, evt: any): string {
        HTMLLabelProvider.currentFieldname = this.getFieldnameForEvent(cell, evt);
        evt.fieldname = HTMLLabelProvider.currentFieldname;
        return cell.value[evt.fieldname] || '';
    }

    public labelChanged(cell: any, newValue: any, trigger: any) {
        var name = (trigger != null) ? trigger.fieldname : HTMLLabelProvider.currentFieldname;	
        if (name != null) {
            // Clones the user object for correct undo and puts
            // the new value in the correct field.
            var value = mx.mxUtils.clone(cell.value);
            value[name] = newValue;
            newValue = value;
            arguments[1] = newValue;
            mx.mxGraph.prototype.labelChanged.apply(this, arguments);
        }
    }
}

