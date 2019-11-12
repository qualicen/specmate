export class CEGmxModelNode {
    public static VARIABLE_KEY = 'variable';
    public static CONDITION_KEY = 'condition';
    public static TYPE_KEY = 'type';
    constructor(public variable: string, public condition: string, public type: string) {}
}
