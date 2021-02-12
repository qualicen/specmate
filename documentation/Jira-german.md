# Specmate als Jira-Plugin

## Konfiguration

DAS MÜSSTE JMD. ANDERES SCHREIBEN!

## Nutzung von Specmate innerhalb von Jira-Projekten

Wenn Sie Specmate für Ihr Jira-Projekt verwenden wollen,
loggen Sie sich bei Jira ein. Unten rechts auf Ihrer Projekt-Seite
sehen Sie Specmate und ein Dropdown-Menü. Wenn Sie auf dieses
klicken, erscheint ein Link zur Anmeldung bei Specmate via Jira.

![](Images_ger/Jira-Projektseite.png "Jira-Projektseite")

Melden Sie sich bei Specmate mit Ihren Zugangsdaten für Jira an
und wählen Sie beim Dropdown-Menü "Projekt" Ihr Jira-Projekt aus,
für das Specmate verwendet wird.

![](Images_ger/Anmeldungspecmate.png "Anmeldung Specmate")

Nachdem Sie sich bei Specmate angemeldet haben, sehen Sie diese Ansicht:

![](Images_ger/Willkommen_jira.png "Willkommen_jira")


Wenn Sie Specmate von Jira aus verwenden, können Sie nicht nur
Specmate nutzen, um Ihr Projekt zu verbessern, sondern auch Ihre
Anforderungen direkt aus Jira importieren. Nachdem Sie sich bei
Specmate über Jira angemeldet haben, sehen Sie auf der linken
Seite im *Projektexplorer* Ihre in Jira angelegten Issues: Die Ordner repräsentieren die in Jira gespeicherten Epics, Ihre Stories und Tests sind sind in den Ordnern als Anforderungen gespeichert.

![](Images_ger/Jira-Ordnung.png "Jira-Ordnung")

Nachdem Sie ein Modell in Specmate erstellt haben (Genaueres
dazu erfahren Sie im Verlauf dieser Anleitung!), können Sie in
dem von Ihnen geöffneten Jira-Issue die erstellten Modelle inkl.
kleiner Vorschau-Bilder im Abschnitt "Specmate" sehen. Hier werden
Ihnen außerdem bereits angelegte Testspezifikationen angezeigt
und auch wieviele Testfälle diese je enthalten:

![](Images_ger/Jira-Anzeige-Minimodel-und-Testspezifikation.png "Jira-Anzeige-Minimodel-und-Testspezifikation")

Mittels der zwei grauen Buttons, können Sie direkt von Jira aus
ein neues CEG- oder Prozessmodell anlegen.

### CEG vs. Prozessmodell: Welche Modelle eignen sich wofür?

Ursache-Wirkungs-Diagramme (engl. Cause-Effect-Graphs, CEGs) eignen sich besonders zur Beschreibung für Anforderungen in der Form "Wenn... dann ..." und somit z.B. zur Beschreibung von  Geschäftsregeln. Prozessdiagramme eignen sich besonders zur Beschreibung von Geschäftsprozessen und sind daher vor allem für End-to-End-Tests geeignet.

## Ansichten und Übersichten in Specmate

### Projektexplorer

Haben Sie auf Basis Ihrer Anforderungen eine Modellart ausgewählt, klicken Sie links im *Projektexplorer* auf eine dem Modell zugrunde liegende Anforderung: So gelangen Sie zur Anforderungsübersicht, die folgendermaßen aussieht:

![](Images_ger/Project_Explorer.png "Project Explorer")


### Anforderungsübersicht

Wenn Sie hier nach unten scrollen, ergeben sich für Sie mehrere Optionen:

![](Images_ger/Anforderungsübersicht-ausführlich.png "Anforderungsübersicht ausführlich")

1. Hier können Sie Ihre aus Jira verwendete Anforderungsquelle ansehen
2. Hier können Sie ein neues Ursache-Wirkungs-Diagramm (CEG) anlegen. Sie gelangen , wenn Sie den Namen Ihres geplanten Modells eingegeben und auf den "Modell anlegen-Button" (3) geklickt haben, zum CEG-Modelleditor. Weiteres dazu finden Sie [hier](###CEG-Modelleditor)
3. siehe 2.
4. Hier können Sie ein bereits vorhandenes Modell, das Sie bei (10) oder der Bibliotheksansicht kopiert haben, einfügen.
5. Hier haben Sie Platz Ihre Modellanforderungen auszuformulieren.
6. Hier können Sie ein neues Prozessmodell anlegen. Sie gelangen , wenn Sie den Namen Ihres geplanten Modells eingegeben und auf den "Modell anlegen-Button" geklickt haben, zum Prozessmodell-Editor. Weiteres dazu finden Sie [hier](###Prozessmodell-Editor).
7. Hier können Sie eine neue Testspezifikation manuell anlegen oder…
8. …eine vorhandene Testspezifikation duplizieren.
9. Durch das Klicken auf das rote Papierkorbsymbol, können Sie einzelne Elemente jederzeit löschen.
10. Durch das Klicken der "Kopieren-Schaltfläche" können Sie Modelle kopieren und bei (4) wieder einfügen oder in der *Bibliotheksansicht* speichern.

### Bibliotheksansicht

## Modelleditoren

### CEG-Modelleditor

### Prozessmodell-Editor
