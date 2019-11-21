
export class Type {
    public static is(o1: { className: string }, o2: { className: string }): boolean {
        if (o1 === undefined || o2 === undefined) {
            return false;
        }
        if (o1.className !== undefined && o2.className !== undefined) {
            return o1.className === o2.className;
        } else if (o1.hasOwnProperty('___proxy') && o2.hasOwnProperty('___proxy')) {
            return true;
        }
        return false;
    }

    public static of(o: { className: string }): string {
        if (o) {
            return o.className;
        }
        return null;
    }
}
