import { MetaInfo } from '../model/meta/field-meta';

export class ValidationUtil {
    /** Validates if a name matches the rules described in the INamed meta property */
    public static isValidName(name: string): boolean {
        if (MetaInfo.INamed === undefined || MetaInfo.INamed[0] === undefined) {
            return true;
        }
        let validPattern = MetaInfo.INamed[0].allowedPattern;
        if (validPattern === undefined) {
            return true;
        }
        let validName: RegExp = new RegExp(validPattern);
        if (name === undefined) {
            return true;
        }
        if (!name.match(validName)) {
            return false;
        }
        return true;
    }

    public static compareStrTrimmed(s1: string, s2: string): boolean {
        if (s1 !== undefined && s2 !== undefined && s1 !== null && s2 !== null) {
            return s1.trim().toLowerCase() === s2.trim().toLowerCase();
        }
        return s1 === undefined && s2 === undefined || s1 === null && s2 === null;
    }
}
