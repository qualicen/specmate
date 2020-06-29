import { UserSession } from '../../../../../model/UserSession';

export class UserToken {
    constructor(public token: String, public project: string, public session: UserSession) { }

    public static INVALID = new UserToken(undefined, undefined, undefined);

    public static isInvalid(token: UserToken): boolean {
        return token === undefined ||
            token === UserToken.INVALID ||
            (token.token === UserToken.INVALID.token &&
                token.project === UserToken.INVALID.project);
    }
}
