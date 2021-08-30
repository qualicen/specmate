import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { NgbDropdown, NgbDropdownConfig } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';
import { Config } from '../../../../../config/config';
import { CookiesService } from 'src/app/modules/common/modules/cookies/services/cookies-service';

@Component({
    selector: 'language-chooser',
    moduleId: module.id.toString(),
    templateUrl: 'language-chooser.component.html',
    providers: [NgbDropdownConfig]
})
export class LanguageChooser implements OnInit {

    private static LANGUAGE_KEY = 'specmate-display-language';

    public selectionIndex = 0;

    @ViewChild('dropdownRef', { static: false })
    set dropdownRef(ref: NgbDropdown) {
        this._dropdownRef = ref;
    }
    private _dropdownRef: NgbDropdown;

    constructor(private translate: TranslateService,
        private cookiesService: CookiesService,
        config: NgbDropdownConfig) {
        config.autoClose = true;
        config.placement = 'bottom-right';
    }

    public ngOnInit(): void {
        this.translate.addLangs(Config.LANGUAGES.map(languageObject => languageObject.code));
        const cookieLang = this.retrieveFromCookie();
        if (this.isValidLanguage(cookieLang)) {
            this.translate.setDefaultLang(cookieLang);
            this.language = cookieLang;
            return;
        }
        const browserLang = this.translate.getBrowserLang();
        if (Config.USE_BROWSER_LANGUAGE && this.translate.getLangs().indexOf(browserLang) > 0) {
            this.translate.setDefaultLang(browserLang);
            this.language = browserLang;
        } else {
            this.translate.setDefaultLang(Config.DEFAULT_LANGUAGE.code);
            this.language = Config.DEFAULT_LANGUAGE.code;
        }
    }

    public set language(language: string) {
        this.translate.use(language);
        this.setLangAttr(language);
        this.storeInCookie(language);
    }

    public get language(): string {
        return this.translate.currentLang;
    }

    public get otherLanguages(): string[] {
        return this.translate.getLangs();
    }

    public getLanguageName(code: string): string {
        return Config.LANGUAGES.find(languageObject => languageObject.code === code).name;
    }

    public get enabled(): boolean {
        return Config.LANGUAGE_CHOOSER_ENABLED;
    }

    private isValidLanguage(language: string) {
        const validLanguages = this.translate.getLangs();
        return language !== undefined && language !== null && language.length > 0 && validLanguages.indexOf(language) >= 0;
    }

    private storeInCookie(language: string): void {
        this.cookiesService.setCookie(LanguageChooser.LANGUAGE_KEY, language);
    }

    private setLangAttr(language: string): void {
        document.documentElement.lang = language;
    }

    private retrieveFromCookie(): string {
        const language = this.cookiesService.getCookie(LanguageChooser.LANGUAGE_KEY);
        if (language !== undefined) {
            // Removing trailing and leading quotation marks, because they are accidentially set in the ngx-cookie service.
            return language.replace(/(^")/, '').replace(/"$/, '');
        }
        return language;
    }

    public setSelectionIndex(newIndex: number) {
        this.selectionIndex = newIndex;
    }
}
