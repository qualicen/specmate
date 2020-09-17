import { xssEncode } from '../../components/util/xss-encoder';

export class CEGmxModelNode {
    public static VARIABLE_KEY = 'variable';
    public static CONDITION_KEY = 'condition';
    public static TYPE_KEY = 'type';

    public variable: string;
    public condition: string;
    public type: string;

    constructor(public _variable: string, public _condition: string, public _type: string) {
        this.variable = xssEncode(_variable);
        this.condition = xssEncode(_condition);
        this.type = _type;
    }
}
