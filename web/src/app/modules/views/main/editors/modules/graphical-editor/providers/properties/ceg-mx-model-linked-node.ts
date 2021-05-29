import { xssEncode } from '../../components/util/xss-encoder';

export class CEGmxModelLinkedNode {
    public variable: string;
    public condition: string;

    constructor(variable: string, condition: string) {
        if (variable === undefined) {
            this.variable = undefined;
        } else {
            this.variable = xssEncode(variable);
        }
        if (condition === undefined) {
            this.condition = undefined;
        } else {
            this.condition = xssEncode(condition);
        }
    }

    public getHint(): string {
        if (this.variable === undefined) {
            return '';
        }
        return this.variable + ' ' + this.condition;
    }
}
