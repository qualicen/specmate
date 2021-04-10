import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGNode } from 'src/app/model/CEGNode';
import { xssEncode } from '../../components/util/xss-encoder';

export class CEGmxModelLinkedNode {
    public static VARIABLE_KEY = 'variable';
    public static CONDITION_KEY = 'condition';

    public variable: string;
    public condition: string;

    constructor(variable: string, condition: string) {
        this.variable = xssEncode(variable);
        this.condition = xssEncode(condition);
    }

    public toString(): string {
        return this.variable + ' ' + this.condition;
    }
}
