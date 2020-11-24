import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie';

@Injectable()
export class CookiesService {

  constructor(private ngxcookie: CookieService) { }

  public setCookie(cookieName: string, cookieValue: any) {
    let cookieOptions = {};

    if ( document.location.protocol == 'https:' ) {
        cookieOptions = { sameSite: 'none', secure: true };
    } else {
        cookieOptions = { sameSite: 'lax'};
    }

    this.ngxcookie.putObject(cookieName, cookieValue, cookieOptions );
  }

  public getCookie(cookieName: string): string {
    return this.ngxcookie.get(cookieName);
  }

  public getCookieObject (cookieName: string): object {
      return this.ngxcookie.getObject(cookieName);
  }

  public removeCookie(cookieName: string): void {
      this.ngxcookie.remove(cookieName);
  }
}
