import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGNode } from 'src/app/model/CEGNode';
import { xssEncode } from '../../components/util/xss-encoder';

export class CEGmxModelNode {

    public variable: string;
    public condition: string;
    public type: string;
    public editField: string;

    constructor(variable: string, condition: string, type: string) {
        this.variable = xssEncode(variable);
        this.condition = xssEncode(condition);
        this.type = type;
    }

    public toString(): string {
        return this.variable + ' ' + this.condition;
    }
}
